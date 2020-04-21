package org.swampsoft.odroidTouchscreen;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.DataOutputStream;
import java.io.IOException;

public class ReadTouchThread extends Thread {

    // INIT variables here
    private XPT2046 xpt2046;

    private SharedPreferences sharedpreferences;

    private int screenResX = 800;
    private int screenResY = 480;
    private int shiftX = 0;
    private int shiftY = 0;
    private int touchResolutionX = 4096;
    private int touchResolutionY = 4096;
    private int touchResXAdjusted;
    private int touchResYAdjusted;

    boolean keepRunning = true;
    private boolean suEnabled = false;
    private boolean isTouchingDown = false;

    private float touchStartX;
    private float touchStartY;
    private float touchX;
    private float touchY;
    private long touchTime;
    private String cmd;

    private DataOutputStream dataOutputStream;

    ReadTouchThread(Context context){
        // try and start su
        startSu();

        // get saved settings and set them here
        sharedpreferences = context.getSharedPreferences("org.swampsoft.odroidTouchscreen", Context.MODE_PRIVATE);
        screenResX = sharedpreferences.getInt("screenResX", 800);
        screenResY = sharedpreferences.getInt("screenResY", 480);
        shiftX = sharedpreferences.getInt("shiftX", 0);
        shiftY = sharedpreferences.getInt("shiftY", 0);
        touchResolutionX = sharedpreferences.getInt("touchResolutionX", 4096);
        touchResolutionY = sharedpreferences.getInt("touchResolutionY", 4096);

        touchResXAdjusted = touchResolutionX/screenResX;
        touchResYAdjusted = touchResolutionY/screenResY;

        System.out.println("***** Using these variables: shiftX=" + shiftX + ", shiftY=" + shiftY + ", touchResolutionX=" + touchResolutionX + ", touchResolutionY=" + touchResolutionY + ", screenResX=" + screenResX + ", screenResY=" + screenResY);
    }

    public void run(){
        int x;
        int y;
        double pressure;

        if (!suEnabled){
            // su still not running, try and start su again
            startSu();
        }

        while (keepRunning) {
            // su needs to be enabled to access the GPIO and to run the commands needed to simulate a gesture
            if (suEnabled){
                x = xpt2046.readX();
                y = xpt2046.readY();
                pressure = xpt2046.readTouchPressure();

                // check if screen has been touched
                if (pressure < 3 && pressure > 0){
                    touchingDown(x,y);
                } else {
                    notTouched();
                }
            } else {
                System.out.println("***** SU not enabled!!! *****");
            }

            // Slow things down a bit
            try {
                // this is here to keep the loop from eating resources. Too high or low, and it has a hard time reading touches.
                // 10 is ok, but i want to save resources, and higher than 50 or so, wont catch all touches. 30 is just right, so far
                sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("***** ReadTouchThread stopped *****");
    }

    private void startSu(){
        // Don't touch this! it starts SU and initializes the XPT2046
        try {
            System.out.println("***** Initializing Touch Service *****");
            Process suProcess = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(suProcess.getOutputStream());
            suEnabled = true;
            System.out.println("SU enabled");
            // start driver!
            xpt2046 = new XPT2046();
            System.out.println("XPT2046 touch driver initialized");
            System.out.println("***** Touch Service Ready *****");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void touchingDown(int x, int y){
        float newX = (float)((x+shiftX)/touchResXAdjusted);
        float newY = (float)((y+shiftY)/touchResYAdjusted);

        if (!isTouchingDown){
            touchScreen(newX,newY);
        }
        touchX = newX;
        touchY = newY;
        isTouchingDown = true;
    }

    public void notTouched(){
        if (isTouchingDown){
            stopTouching();
        }
        isTouchingDown = false;
    }

    private void touchScreen(float x, float y){
        // get x,y info for tap command
        touchStartX = x;
        touchStartY = y;
        touchTime = System.currentTimeMillis();
        System.out.println("Touched Down: " + (int)x + ", " + (int)y);
    }

    private void stopTouching(){
        long guestureTime = System.currentTimeMillis() - touchTime;
        // figure out if its a tap, long tap, or swipe. Then make a command to execute (cmd)
        if (Math.abs(touchStartX - touchX) < 20 && Math.abs(touchStartY - touchY) < 20) {
            //tap, but long or short?
            if (guestureTime < 250){
                // short tap
                System.out.println("Tap - Short");
                cmd = "input tap " +  (int)touchX + " " + (int)touchY + "\n";
            } else {
                // long tap
                System.out.println("Tap - Long");
                cmd = "input swipe " +  (int)touchStartX + " " + (int)touchStartY + " " + (int)touchX + " " + (int)touchY + " " + guestureTime + "\n";
            }
        } else {
            // swipe
            if (guestureTime < 150) {
                guestureTime = 150; // read somewhere swipe doesnt work if its shorter than 150 ms. Didnt test it though
            }
            System.out.println("Swipe");
            cmd = "input swipe " +  (int)touchStartX + " " + (int)touchStartY + " " + (int)touchX + " " + (int)touchY + " " + guestureTime + "\n";
        }

        // run gesture command
        System.out.println("Running command: " + cmd);
        try {
            dataOutputStream.writeBytes(cmd);
            System.out.println("Command successful: " + cmd);
        } catch (IOException e) {
            System.out.println("Command failed: " + cmd);
            e.printStackTrace();
        }
    }
}
