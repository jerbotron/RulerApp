package jeremy.tinderruler;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MyCustomSurfaceView extends SurfaceView implements Runnable {

    private SurfaceHolder surfaceHolder;
    private Ruler ruler;
    private Thread thread = null;

    volatile ForceTracker forceTracker;
    volatile SurfaceMode surfaceMode = SurfaceMode.DEFAULT;

    volatile boolean running = false;
    volatile float touchedX, touchedY, touchedXOld, touchedYOld, deltaX, deltaY;
    volatile float tickerY;

    float MAX_HEIGHT, MAX_WIDTH;

    public MyCustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Ignore Layout View errors
        if (isInEditMode()) return;

        surfaceHolder = getHolder();
        forceTracker = new ForceTracker();

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((RulerActivity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ruler = new Ruler(context, surfaceHolder, displayMetrics);
        MAX_HEIGHT = displayMetrics.heightPixels;
        MAX_WIDTH = displayMetrics.widthPixels;

        tickerY = MAX_HEIGHT/2;
        touchedX = MAX_WIDTH/2;
        touchedY = MAX_HEIGHT/2;
    }

    public void setMode(SurfaceMode mode) {
        surfaceMode = mode;
    }

    public void onResumeMySurfaceView(){
        Log.d("MyCustomSurfaceView: ", "resuming surface view");
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void onPauseMySurfaceView(){
        Log.d("MyCustomSurfaceView: ", "pausing surface view");
        boolean retry = true;
        running = false;
        while(retry){
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while(running){
            if(surfaceHolder.getSurface().isValid()){
                ruler.lockCanvas();
                ruler.setBackground();
                switch (surfaceMode) {
                    case MEASURE: {
                        ruler.measure(tickerY + deltaY);
                        break;
                    }
                    case GAME: {
                        ruler.drawGameUI();
                        if (ruler.drawFootball()) { // animation finished
                            // restart click
                            ruler.stopAnimation();
                        }
                        break;
                    }
                }
                ruler.drawInches();
                ruler.unlockCanvas();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchedX = event.getX();
        touchedY = event.getY();

        if (surfaceMode != SurfaceMode.DEFAULT) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    forceTracker.setStartTime(System.currentTimeMillis());
                    touchedXOld = touchedX;
                    touchedYOld = touchedY;
                }
                case MotionEvent.ACTION_MOVE: {
                    switch (surfaceMode) {
                        case MEASURE: {
                            if (tickerY + deltaY < 0) {
                                tickerY = 0;
                                touchedYOld = touchedY;
                            } else if (tickerY + deltaY > MAX_HEIGHT) {
                                tickerY = MAX_HEIGHT;
                                touchedYOld = touchedY;
                            }
                            break;
                        }
                    }
                    deltaX = touchedX - touchedXOld;
                    deltaY = touchedY - touchedYOld;
                    break;
                }
                case MotionEvent.ACTION_UP:
                    // Common updates
                    forceTracker.setEndTime(System.currentTimeMillis());
                    switch (surfaceMode) {
                        case MEASURE: {
                            tickerY += deltaY;
                            deltaY = 0;
                            break;
                        }
                        case GAME: {
                            if (forceTracker.getVelocityY() < 0) {
                                ruler.calculateFieldGoalPath(forceTracker.getVelocityX(),
                                        forceTracker.getVelocityY());
                                ruler.startAnimation();
                            }
                            break;
                        }
                    }
                default:
            }
        }
        return true; //processed
    }

    class ForceTracker {
        long startTime, endTime;
        ForceTracker() {
            startTime = 0;
            endTime = 0;
        }
        void setStartTime(long t) { startTime = t; }
        void setEndTime(long t) { endTime = t; }

        float getVelocityX() {
            long deltaT = (endTime - startTime > 0) ? endTime - startTime : 0;
            if (deltaT > 0) {
                return deltaX/deltaT;   // px/ms
            }
            return 0;
        }
        float getVelocityY() {
            long deltaT = (endTime - startTime > 0) ? endTime - startTime : 0;
            if (deltaT > 0) {
                return deltaY/deltaT;   // px/ms
            }
            return 0;
        }
    }
}
