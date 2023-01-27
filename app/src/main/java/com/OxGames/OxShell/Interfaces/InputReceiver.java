package com.OxGames.OxShell.Interfaces;

import android.view.KeyEvent;
//import android.view.MotionEvent;

public interface InputReceiver {
//    boolean receiveMotionEvent(MotionEvent motion_event);
    boolean receiveKeyEvent(KeyEvent key_event);
}
