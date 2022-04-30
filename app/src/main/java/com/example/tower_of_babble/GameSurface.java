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

    ArrayList<MenuOption> menu = new ArrayList<>();
    ArrayList<GameObject> grid = new ArrayList<>();
    MenuOption last;

    Camera cam = new Camera();
    Camera menuCam = new Camera();

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

                menu.stream()
                        .filter(o -> !o.tryClick(prevX, prevY))
                        .forEach(MenuOption::unselect);

                menu.stream()
                        .filter(MenuOption::selected)
                        .findFirst()
                        .ifPresent(mo ->  last = mo);

                if(last != null && !last.selected()){
                    var go = new GameObject(
                            (prevX - cam.getX()) - ((prevX - cam.getX())%160),
                            (prevY - cam.getY()) - ((prevY - cam.getY())%160),
                            16, 16,
                            last.current_selection().getBmp());
                    grid.add(go);
                }
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
        for(MenuOption frame : menu) {
            frame.draw(canvas, menuCam);
        }
        for(GameObject o : grid)
            o.draw(canvas, cam);
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Bitmap frame = BitmapFactory.decodeResource(this.getResources(), R.drawable.frame);

        MenuOption firstOption = new MenuOption(this, 100, 10);

        Bitmap twr2Aimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_2a);
        GameObject twr2Aobj = new GameObject(0, 0, 16, 16);
        twr2Aobj.setBmp(twr2Aimg);
        firstOption.addOption(twr2Aobj);

        Bitmap twr2Bimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_2b);
        GameObject twr2Bobj = new GameObject(0, 0, 16, 16);
        twr2Bobj.setBmp(twr2Bimg);
        firstOption.addOption(twr2Bobj);

        Bitmap twr2Cimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_2c);
        GameObject twr2Cobj = new GameObject(0, 0, 16, 16);
        twr2Cobj.setBmp(twr2Cimg);
        firstOption.addOption(twr2Cobj);

        menu.add(firstOption);

        MenuOption secondOption = new MenuOption(this, 400, 10);

        Bitmap twr3Aimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_3a);
        GameObject twr3Aobj = new GameObject(0, 0, 16, 16);
        twr3Aobj.setBmp(twr3Aimg);
        secondOption.addOption(twr3Aobj);

        Bitmap twr3Bimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_3b);
        GameObject twr3Bobj = new GameObject(0, 0, 16, 16);
        twr3Bobj.setBmp(twr3Bimg);
        secondOption.addOption(twr3Bobj);

        menu.add(secondOption);

        MenuOption thirdOption = new MenuOption(this, 700, 10);

        Bitmap twr4Aimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_4a);
        GameObject twr4Aobj = new GameObject(0, 0, 16, 16);
        twr4Aobj.setBmp(twr4Aimg);
        thirdOption.addOption(twr4Aobj);

        menu.add(thirdOption);




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
