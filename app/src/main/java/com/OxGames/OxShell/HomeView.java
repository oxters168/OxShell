package com.OxGames.OxShell;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;

public class HomeView extends GridView {
    private float currentTouchX;
    private float currentTouchY;
    private float prevTouchX;
    private float prevTouchY;
    private float startTouchX;
    private float startTouchY;
    private boolean moved;

    public HomeView(Context context) {
        super(context);
        RefreshShownItems();
    }
    public HomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        RefreshShownItems();
    }
    public HomeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        RefreshShownItems();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        requestFocusFromTouch();

        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        currentTouchX = ev.getX();
        currentTouchY = ev.getY();

        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                moved = true;
                invalidate();
//                Log.d("Touch", "Diff = " + diff);
//                Log.d("Touch", "Action_Move (" + ev.getX() + ", " + ev.getY() + ")");
                break;
            case MotionEvent.ACTION_DOWN:
                moved = false;
                startTouchX = currentTouchX;
                startTouchY = currentTouchY;
//                Log.d("Touch", "Action_Down (" + ev.getX() + ", " + ev.getY() + ")");
                break;
            case MotionEvent.ACTION_UP:
                if (!moved) {
                    //Click
//                    Log.d("Touch", "Clicked");
//                    MakeSelection();
                    Intent intent = new Intent(HomeActivity.GetInstance(), ExplorerActivity.class);
//                    EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
//                    String message = editText.getText().toString();
//                    intent.putExtra(EXTRA_MESSAGE, message);
                    HomeActivity.GetInstance().startActivity(intent);
                }
                moved = false;
//                Log.d("Touch", "Action_Up (" + ev.getX() + ", " + ev.getY() + ")");
                break;
        }

        prevTouchX = currentTouchX;
        prevTouchY = currentTouchY;
        return true;
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // This is the key that will make the height equivalent to its width
    }

    public void RefreshShownItems() {
        HomeItem[] homeItems = new HomeItem[] {
                new HomeItem(HomeItem.Type.explorer, "Something"),
                new HomeItem(HomeItem.Type.explorer, "Wong"),
                new HomeItem(HomeItem.Type.explorer, "Wid"),
                new HomeItem(HomeItem.Type.explorer, "Dis"),
                new HomeItem(HomeItem.Type.explorer, "View"),
                new HomeItem(HomeItem.Type.explorer, "Lol"),
                new HomeItem(HomeItem.Type.explorer, "Out"),
                new HomeItem(HomeItem.Type.explorer, "Of"),
                new HomeItem(HomeItem.Type.explorer, "Stuff"),
                new HomeItem(HomeItem.Type.explorer, "GTG")
        };
        HomeAdapter customAdapter = new HomeAdapter(getContext(), homeItems);
        setAdapter(customAdapter);
    }
}