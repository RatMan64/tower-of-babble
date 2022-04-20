package com.example.tower_of_babble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;

    ArrayList<ArrayList<GameObject>> map = new ArrayList<>();

    Camera cam = new Camera();

    private int prevX = 0;
    private int prevY = 0;

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


                cam.move(deltaX, deltaY);
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
        for(ArrayList<GameObject> arr : map) {
            for(GameObject g : arr) {
                g.draw(canvas, cam);
            }
        }
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Bitmap b = BitmapFactory.decodeResource(this.getResources(),R.drawable.empty_plot);
        Bitmap scaledB = Bitmap.createScaledBitmap(b, 120, 120, false);

        this.gameThread = new GameThread(this,holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();

        for(int i = 0; i != 100; ++i) {
            map.add(new ArrayList<>());
            for(int j = 0; j != 100; ++j) {
                GameObject g = new GameObject(i, j, 120, 120);
                map.get(i).add(g);
                g.setBmp(scaledB);
            }
        }
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
