package com.OxGames.OxShell.Views;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ListView;

import com.OxGames.OxShell.Adapters.DetailAdapter;
import com.OxGames.OxShell.Data.KeyComboAction;
import com.OxGames.OxShell.Data.SettingsKeeper;
import com.OxGames.OxShell.Helpers.InputHandler;
import com.OxGames.OxShell.Helpers.MathHelpers;
import com.OxGames.OxShell.Interfaces.CustomViewListener;
import com.OxGames.OxShell.Data.DetailItem;
import com.OxGames.OxShell.Interfaces.InputReceiver;
import com.OxGames.OxShell.Interfaces.Refreshable;
import com.OxGames.OxShell.OxShellApp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlideTouchListView extends ListView implements InputReceiver, Refreshable {//, SlideTouchListener {
    //private SlideTouchHandler slideTouch = new SlideTouchHandler();
    private ArrayList<CustomViewListener> eventListeners = new ArrayList<>();
    protected InputHandler inputHandler;
    int properPosition = 0;

    public SlideTouchListView(Context context) {
        super(context);
        init();
    }
    public SlideTouchListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public SlideTouchListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init() {
        //slideTouch.addListener(this);
        inputHandler = new InputHandler();
        inputHandler.addKeyComboActions(Arrays.stream(SettingsKeeper.getPrimaryInput()).map(combo -> new KeyComboAction(combo, this::primaryAction)).toArray(KeyComboAction[]::new));
        inputHandler.addKeyComboActions(Arrays.stream(SettingsKeeper.getSecondaryInput()).map(combo -> new KeyComboAction(combo, this::secondaryAction)).toArray(KeyComboAction[]::new));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int storedPos = properPosition;
        refresh();
        setProperPosition(storedPos);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
    private float touchMarginTop = 50;
    private float touchMarginLeft = 50;
    private float touchMarginRight = 50;
    private float touchMarginBottom = 50;
    private float momentumDeceleration = 10000; // pixels per second per second
    private float touchDeadzone = 50;
    private float longPressTime = 300; // in milliseconds
    private long touchMoveStartTime = 0;
    private boolean touchInsideBorders = false;
    private boolean longPressed = false;
    private boolean isPressing = false;
    private float touchMoveDir = 0;
    private boolean touchVer = false;
    private float startTouchY;
    private float pseudoStartY = 0;
    private float prevTouchY = 0;
    private float accumulatedDiffY = 0;
    private float momentumY = 0;

    private float getBtnHeight() {
        return getAdapter() instanceof DetailAdapter ? ((DetailAdapter)getAdapter()).getBtnHeight() : 128;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //slideTouch.onTouchEvent(ev);
        //Log.d("SlideTouchListView", ev.toString());
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            touchMoveStartTime = SystemClock.uptimeMillis();
            longPressed = false;
            isPressing = true;
            touchVer = false;
            startTouchY = ev.getRawY();
            prevTouchY = startTouchY;
            touchInsideBorders = ev.getRawX() > touchMarginLeft && ev.getRawX() < OxShellApp.getDisplayWidth() - touchMarginRight && startTouchY > touchMarginTop && startTouchY < OxShellApp.getDisplayHeight() - touchMarginBottom;
            if (touchInsideBorders) {
                stopMomentum();
                Handler longPressHandler = new Handler();
                Runnable checkLongPress = new Runnable() {
                    @Override
                    public void run() {
                        if (isPressing && !touchVer && Math.abs(startTouchY - ev.getRawY()) < touchDeadzone) {
                            if (SystemClock.uptimeMillis() - touchMoveStartTime >= longPressTime) {
                                longPressed = true;
                                secondaryAction();
                            } else
                                longPressHandler.postDelayed(this, 10);
                        }
                    }
                };
                longPressHandler.postDelayed(checkLongPress, 10);
            }
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (longPressed)
                return true;
            float currentY = ev.getRawY();

            if (!touchVer && Math.abs(currentY - startTouchY) >= touchDeadzone) {
                touchMoveDir = Math.signum(currentY - startTouchY);
                touchMoveStartTime = SystemClock.uptimeMillis();
                pseudoStartY = currentY;//touchMoveDir * touchDeadzone + startTouchY;
                touchVer = true;
            }
            if (touchVer) {
                float diffY = prevTouchY - currentY;
                if (touchMoveDir != Math.signum(diffY)) {
                    // if the movement direction changed, then update the start time to reflect when the change happened (for momentum)
                    touchMoveDir = Math.signum(diffY);
                    touchMoveStartTime = SystemClock.uptimeMillis();
                    pseudoStartY = currentY;
                }
                //accumulatedDiffY += diffY;
                shiftBy(diffY);
                //accumulatedDiffY = accumulatedDiffY % getBtnHeight();
            }
            prevTouchY = currentY;
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            isPressing = false;
            stopMomentum();
            if (longPressed)
                return true;
            if (!touchVer && Math.abs(startTouchY - ev.getRawY()) < touchDeadzone) {
                // if the user did not scroll vertically and they're still within the deadzone
                primaryAction();
            } else if (Math.abs(pseudoStartY - ev.getRawY()) >= getBtnHeight()) {
                // else keep the momentum of where the user was scrolling
                long touchupTime = SystemClock.uptimeMillis();
                float totalTime = (touchupTime - touchMoveStartTime) / 1000f;
                if (touchVer)
                    momentumY = (pseudoStartY - ev.getRawY()) / totalTime; // pixels per second
                if ((touchVer && Math.abs(momentumY) > 0))
                    momentumHandler.post(momentumRunner);
            }
        }
        return true;
    }
    private void shiftBy(float amount) {
        //Log.d("SlideTouchListView", "Shifting by: " + amount);
        accumulatedDiffY += amount;
        int accumShift = (int)Math.floor(Math.abs(accumulatedDiffY / getBtnHeight()));
        if (accumShift >= 1) {
            for (int i = 0; i < accumShift; i++) {
                if (Math.signum(amount) < 0)
                    selectNextItem();
                else
                    selectPrevItem();
            }
            accumulatedDiffY = accumulatedDiffY % getBtnHeight();
        }
    }
    private void stopMomentum() {
        momentumY = 0;
        //momentumTravelDistX = 0;
        //momentumTravelDistY = 0;
        momentumHandler.removeCallbacks(momentumRunner);
    }
    private Handler momentumHandler = new Handler();
    private Runnable momentumRunner = new Runnable() {
        @Override
        public void run() {
            int milliInterval = MathHelpers.calculateMillisForFps(120);

            boolean hasMomentumY = Math.abs(momentumY) > 0;
            //Log.d("SlideTouchListView", "Has momentum: " + hasMomentumY + " amount: " + momentumY);
            if (hasMomentumY) {
                // calculate travel distance based on current momentum
                float momentumOffsetY = momentumY * (milliInterval / 1000f);
                //momentumTravelDistY += momentumOffsetY;
                // decelerate the momentum
                if (momentumY > 0)
                    momentumY = Math.max(momentumY - momentumDeceleration * (milliInterval / 1000f), 0);
                else
                    momentumY = Math.min(momentumY + momentumDeceleration * (milliInterval / 1000f), 0);
                // get the difference between the items passed to see what should be applied this moment
                //shiftY(momentumOffsetY, XMBView.this.colIndex);
                shiftBy(momentumOffsetY);
            }
            if (hasMomentumY)
                momentumHandler.postDelayed(this, milliInterval);
        }
    };

    public void addListener(CustomViewListener listener) {
        eventListeners.add(listener);
    }
    public void clearListeners() {
        eventListeners.clear();
    }

    @Override
    public boolean receiveKeyEvent(KeyEvent key_event) {
        //Log.d("SlideTouchListView", "Received key event");
//        if (key_event.getAction() == KeyEvent.ACTION_UP) {
//            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_A) {
//                primaryAction();
//                return true;
//            }
//            if (key_event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_Y) {
//                secondaryAction();
//                return true;
//            }
//        }
        if (inputHandler.onInputEvent(key_event))
            return true;
        if (key_event.getAction() == KeyEvent.ACTION_DOWN) {
            // action down since the action gets repeated when held
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                selectNextItem();
                return true;
            }
            if (key_event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                selectPrevItem();
                return true;
            }
        }
        return false;
    }

    public void setItemSelected(int index, boolean onOff) {
        // TODO: keep order of selection
        ((DetailItem)getItemAtPosition(index)).isSelected = onOff;
        invalidateViews();
    }
    public boolean isItemSelected(int index) {
        return ((DetailItem)getItemAtPosition(index)).isSelected;
    }
    public List<DetailItem> getSelectedItems() {
        List<DetailItem> selection = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            DetailItem currentItem = (DetailItem)getItemAtPosition(i);
            if (currentItem.isSelected)
                selection.add(currentItem);
        }
        if (selection.size() <= 0)
            selection.add((DetailItem)getItemAtPosition(properPosition));
        return selection;
    }
    private void highlightSelection() {
        for (int i = 0; i < getCount(); i++)
            ((DetailItem)getItemAtPosition(i)).isCurrentItem = (i == properPosition);
        invalidateViews();
    }
    public void selectNextItem() {
        //Log.d("SlideTouchListView", "Selecting next item");
        int total = getCount();
        int nextIndex = properPosition + 1;
        if (nextIndex >= total)
            nextIndex = total - 1;
        setProperPosition(nextIndex);
    }
    public void selectPrevItem() {
        //Log.d("SlideTouchListView", "Selecting prev item");
        int prevIndex = properPosition - 1;
        if (prevIndex < 0)
            prevIndex = 0;
        setProperPosition(prevIndex);
    }
    public void primaryAction() {
        CustomViewListener[] listeners = eventListeners.toArray(new CustomViewListener[0]);
        for (CustomViewListener el : listeners)
            el.onMakeSelection(properPosition);
    }
    public void secondaryAction() {

    }
    public void setProperPosition(int pos) {
//        Log.d("Explorer", "Setting position to " + pos);
        properPosition = pos; //Probably should clamp properPosition here
        highlightSelection();
        //DisplayMetrics displayMetrics = ActivityManager.getCurrentActivity().getDisplayMetrics();
        setSelectionFromTop(pos, (int)(OxShellApp.getDisplayHeight() * 0.5));

    }
    @Override
    public void refresh() {
        setProperPosition(properPosition);
    }
}