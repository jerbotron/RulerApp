package jeremy.tinderruler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;

import java.util.LinkedList;
import java.util.Random;

class Ruler {
    private Context callingContext;
    private SurfaceHolder surfaceHolder;
    private Canvas canvas;
    private Paint paint;
    private float MAX_WIDTH, MAX_HEIGHT, xDPI, yDPI;

    private Random random;
    private boolean animationStart = false, newGame = false;
    private float windVel;
    private LinkedList<Point> fieldGoalPath;

    Ruler(Context callingContext, SurfaceHolder surfaceHolder, DisplayMetrics displayMetrics) {
        this.callingContext = callingContext;
        this.surfaceHolder = surfaceHolder;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        MAX_WIDTH = displayMetrics.widthPixels;
        MAX_HEIGHT = displayMetrics.heightPixels;
        xDPI = displayMetrics.xdpi;
        yDPI = displayMetrics.ydpi;

        random = new Random();
        windVel = getNewWindVel();
        initFootballLocation();
    }

    // Helpers to lock/unlock surfaceView canvas in order to update drawings
    // ** Must be called before drawing on canvas **
    void lockCanvas() {
        canvas = surfaceHolder.lockCanvas();
    }
    void unlockCanvas() {
        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    void setBackground() {
        paint.setColor(ContextCompat.getColor(callingContext, R.color.colorPrimary));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

        Bitmap logo = BitmapFactory.decodeResource(callingContext.getResources(), R.drawable.flame);
        canvas.drawBitmap(logo, canvas.getWidth()/2 - 32, canvas.getHeight()/2 - 64, paint);
    }

    void drawInches() {
        paint.setColor(ContextCompat.getColor(callingContext, R.color.colorAccent));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        float maxWidth = canvas.getWidth();
        float maxHeight = canvas.getHeight();
        float yDPI_16th = yDPI/16;

        int heightCount = Math.round(maxHeight/yDPI_16th);
        for (int y = 0; y < heightCount; y++) {
            int tickLength;
            if (y % 16 == 0) {
                tickLength = 100;
                drawTick(String.valueOf(y/16), maxWidth - tickLength, y*yDPI_16th);
                drawTick(String.valueOf(y/16), tickLength + 90, y*yDPI_16th);
            } else if (y % 8 == 0) {
                tickLength = 80;
            } else if (y % 4 == 0) {
                tickLength = 50;
            } else if (y % 2 == 0) {
                tickLength = 35;
            }
            else {
                tickLength = 25;
            }
            paint.setStrokeWidth(5);
            canvas.drawLine(0, y*yDPI_16th, tickLength, y*yDPI_16th, paint);
            canvas.drawLine(maxWidth, y*yDPI_16th, maxWidth - tickLength, y*yDPI_16th, paint);
        }
    }

    /**************************
     * Measure Mode Functions *
     **************************/
    void measure(float yPos) {
        yPos = (yPos < 0) ? 0 : yPos;
        yPos = (yPos > canvas.getHeight() ? canvas.getHeight() : yPos);

        paint.setColor(ContextCompat.getColor(callingContext, R.color.colorPrimaryDark));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, canvas.getWidth(), yPos, paint);

        // display measurement text
        float width = canvas.getWidth();
        double inches = Math.round(yPos/yDPI*100)/100D;
        canvas.save();
        canvas.rotate(90, width/2, yPos);
        if (yPos > canvas.getHeight()*7/8) {
            paint.setColor(ContextCompat.getColor(callingContext, R.color.colorAccent));
            canvas.drawText(String.valueOf(inches + " in"), width/2 - 200, yPos, paint);
        } else {
            canvas.drawText(String.valueOf(inches + " in"), width/2 + 50, yPos, paint);
        }
        canvas.restore();
    }

    private void drawTick(String tick, float x, float y) {
        paint.setStrokeWidth(1);
        paint.setTextSize(50);
        canvas.save();
        canvas.rotate(90, x, y);
        canvas.drawText(tick, x - 15, y + 60, paint);
        canvas.restore();
    }

    /**********************************
     * Field Goal Game Mode Functions *
     **********************************/
    void startAnimation() {
        animationStart = true;
    }
    void stopAnimation() {
        animationStart = false;
        newGame = true;
    }

    void drawGameUI() {
        drawGoalPost();
        if (newGame) {
            windVel = getNewWindVel();
            newGame = false;
        }
        drawWind();
    }

    // return true if the animation is complete
    boolean drawFootball() {
        Bitmap bitmap = BitmapFactory.decodeResource(callingContext.getResources(), R.drawable.football);
        Point p;
        if (animationStart) {
            p = fieldGoalPath.poll();
        } else {
            p = fieldGoalPath.peek();
        }
        canvas.drawBitmap(bitmap, p.x, p.y, paint);
        boolean fieldGoalIsGood = isFieldGoalGood(p.x, p.y);
        if (fieldGoalPath.isEmpty()) {
            if (fieldGoalIsGood) vibrateDevice();
            initFootballLocation();
            return true;
        }
        return false;
    }

    void calculateFieldGoalPath(float velX, float velY) {
        if (!animationStart) {
            final float ANIMATION_DELTA_T = 1000F;
            final float ANIMATION_DELTA_T_STEP = 10F;
            final float WIND_VELOCITY_DAMPER = 0.1F;

            Point p = fieldGoalPath.peek(); // get current location of football
            float xStart = p.x;
            float yStart = p.y;
            float yEnd = yStart + velY * ANIMATION_DELTA_T;

            // x = my + b;
            float  b = xStart - velX/velY * yStart;

            float y = yStart;
            float x = (velX/velY) * y + b;
            float windX = 0;
            while (y >= yEnd && y >= 0 && x >= 0 && x <= MAX_WIDTH) {
                fieldGoalPath.add(new Point(x+windX, y));
                y+=(velY * ANIMATION_DELTA_T_STEP);
                x = (velX/velY) * y + b;
                windX = (windVel * (yStart - y)) * WIND_VELOCITY_DAMPER;
            }
        }
    }

    private float getNewWindVel() {
        int windDirection = (random.nextBoolean()) ? 1 : -1;
        return windDirection * random.nextFloat() * (5);
    }

    private void drawWind() {
        paint.setColor(ContextCompat.getColor(callingContext, R.color.colorAccent));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(1);
        paint.setTextSize(50);

        Bitmap bitmapWind = BitmapFactory.decodeResource(callingContext.getResources(), R.drawable.wind);

        float xStart = canvas.getWidth()/2 - 50;
        float yStart = 50;
        canvas.drawBitmap(bitmapWind, xStart, yStart, paint);
        if (windVel < 0) {
            Bitmap bitmapArrow = BitmapFactory.decodeResource(callingContext.getResources(), R.drawable.arrow_left);
            canvas.drawBitmap(bitmapArrow, xStart + 100, yStart, paint);
        } else {
            Bitmap bitmapArrow = BitmapFactory.decodeResource(callingContext.getResources(), R.drawable.arrow_right);
            canvas.drawBitmap(bitmapArrow, xStart - 100, yStart, paint);
        }
        String windVelText = String.valueOf(Math.round(windVel*100)/100D);
        canvas.drawText(windVelText, xStart - 10, yStart + 150, paint);
    }

    private void drawGoalPost() {
        paint.setColor(ContextCompat.getColor(callingContext, R.color.colorGoalPost));
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        float goalPostWidth = canvas.getWidth()/2;
        float goalPostHeight = goalPostWidth/2;
        float goalPostThickness = 50;
        float xStart = canvas.getWidth()/4;
        canvas.drawRect(xStart, goalPostHeight , xStart * 3, goalPostThickness + goalPostHeight, paint);
        canvas.drawRect(xStart, goalPostThickness, xStart + goalPostThickness, goalPostThickness + goalPostHeight, paint);
        canvas.drawRect(xStart * 3, goalPostThickness, xStart * 3 - goalPostThickness, goalPostThickness + goalPostHeight, paint);
        canvas.drawRect(goalPostWidth - goalPostThickness/2, goalPostHeight, goalPostWidth + goalPostThickness/2, goalPostHeight + 100, paint);
    }

    private void initFootballLocation() {
        fieldGoalPath = new LinkedList<>();
        float xMin = MAX_WIDTH/5;
        float xMax = MAX_WIDTH*4/5;
        float startX = random.nextFloat() * (xMax - xMin) + xMin;
        float startY = MAX_HEIGHT*4/5;
        fieldGoalPath.add(new Point(startX, startY));
    }

    private boolean isFieldGoalGood(float xPos, float yPos) {
        float goalPostWidth = canvas.getWidth()/2;
        float goalPostHeight = goalPostWidth/2;
        float goalPostThickness = 50;
        float xStart = canvas.getWidth()/4;
        float xEnd = xStart * 3;
        return (yPos <= goalPostHeight && xPos > xStart && xPos < xEnd - goalPostThickness);
    }

    private void vibrateDevice() {
        Vibrator v = (Vibrator) callingContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(250);
    }

    /**********************************************
     * Point class to track animation coordinates *
     **********************************************/
    private class Point {
        float x;
        float y;

        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
