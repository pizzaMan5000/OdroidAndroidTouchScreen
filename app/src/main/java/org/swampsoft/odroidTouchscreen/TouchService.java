package org.swampsoft.odroidTouchscreen;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class TouchService extends AccessibilityService {

    AccessibilityServiceInfo info;

    private ReadTouchThread readTouchThread;

    public TouchService(){
    }

    static {
        System.loadLibrary("wpi_android");
    }

    @Override
    public void onServiceConnected(){

        // todo change all of this info crap, or not? don't think it hurts to leave it out
        //info = new AccessibilityServiceInfo();

        // stuff from the Google tutorial:
        //info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED | AccessibilityEvent.TYPE_VIEW_FOCUSED;
        //info.packageNames = new String[]{"org.swampsoft.odroidc1spitest"};
        //info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        //info.notificationTimeout = 100;
        //this.setServiceInfo(info);

        // start thread to read touches
        readTouchThread = new ReadTouchThread(this);
        readTouchThread.start();
        System.out.println("Odroid Touchscreen Service Started");
        Toast.makeText(getApplicationContext(), "Odroid Touchscreen Service Started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {
        //readTouchThread.keepRunning = false;
    }

    @Override
    public void onRebind(Intent intent){
        // start over the readTouchThread
        readTouchThread = new ReadTouchThread(this);
        readTouchThread.start();
    }

    @Override
    public boolean onUnbind(Intent intent){
        // stop readTouchThread loop
        readTouchThread.keepRunning = false;
        return true;
    }
}
