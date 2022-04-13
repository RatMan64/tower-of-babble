package com.example.tower_of_babble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    public Bitmap tbm;

    private int prevX = 0;
    private int prevY = 0;
    private int offsetX = 0;
    private int offsetY = 0;

    public GameSurface(Context context)  {
        super(context);

        // Make Game Surface focusable so it can handle events. .
        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);
    }

    public void update()  {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                prevX =  (int)event.getX();
                prevY = (int)event.getY();
                Log.d("DOWN", "it's down");
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                int currX =  (int)event.getX();
                int currY = (int)event.getY();
                int deltaX = currX - prevX;
                int deltaY = currY - prevY;
                Log.d("DELTA", "x: " + deltaX + ", y: " + deltaY);
                prevX = currX;
                prevY = currY;

                offsetX += deltaX;
                offsetY += deltaY;
                Log.d("MOVE", "It moved.");
                return true;
            }
            case MotionEvent.ACTION_UP: {
                Log.d("UP", "Its up.");
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas)  {
        super.draw(canvas);

        canvas.drawBitmap(tbm, offsetX, offsetY, null);
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Bitmap b = BitmapFactory.decodeResource(this.getResources(),R.drawable.grassblock);
        tbm = Bitmap.createScaledBitmap(b, 120, 120, false);

        this.gameThread = new GameThread(this,holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry= true;
        while(retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = true;
        }
    }
}
