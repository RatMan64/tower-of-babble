package com.example.tower_of_babble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.RequiresApi;

import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;

    ArrayList<MenuOption> menu = new ArrayList<>();
    World world = new World(10, 10, this);
    MenuOption last;
    String currentSelectedTag;
    boolean startmenu=true; // Riley for the begging

    // track if the user panned the camera.
    boolean moved;

    Camera cam = new Camera();
    Camera menuCam = new Camera();

    private int prevX = 0;
    private int prevY = 0;

    private long prevTimeMs = 0;

    public ObjectOutputStream oos;
    public int id;

    public GameSurface(Context context)  {
        super(context);

        // Make Game Surface focusable so it can handle events. .
        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);
    }

    public void update()  {
        long currTimeMs = System.currentTimeMillis();
        int elapsed = (int)(currTimeMs - prevTimeMs);
        world.update(elapsed);
        prevTimeMs = currTimeMs;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(startmenu){ //riley switch to the game
            startmenu = false;
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN: {
                prevX =  (int)event.getX();
                prevY = (int)event.getY();
                moved = false;
                //check if finger is over a tile they can place
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                int currX =  (int)event.getX();
                int currY = (int)event.getY();
                int deltaX = currX - prevX;
                int deltaY = currY - prevY;
                prevX = currX;
                prevY = currY;


                cam.move(deltaX, deltaY);
                moved = true;
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if(!moved) {
                    int upX = (int) event.getX();
                    int upY = (int) event.getY();
                    menu.stream()
                            .filter(o -> !o.tryClick(upX, upY))
                            .forEach(MenuOption::unselect);

                    menu.stream()
                            .filter(MenuOption::selected)
                            .findFirst()
                            .ifPresent(mo -> last = mo);

                    if(last != null) {
                        currentSelectedTag = last.currSelectedTag();

                        // imgs are 160 pixels across... this is really bad practice lol
                        int worldX = (int) (upX - cam.getX()) / 160;
                        int worldY = (int) (upY - cam.getY()) / 160;
                        if(worldX >= 0 && worldX < 10 && worldY >= 0 && worldY < 10) {
                            world.tryBeginPlace(worldX, worldY, currentSelectedTag, oos, id);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas)  {

        super.draw(canvas);
        if(!startmenu){ // dont draw game until palyer starts

            for(MenuOption frame : menu) {
                frame.draw(canvas, menuCam);
            }
            world.draw(canvas, cam);

        }else{
            // riley title and instuctions
            String title = "Tower of Babble";
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            float x =getWidth()/3;
            float y = getHeight()/3;
            canvas.drawText(title,x,y,paint );
            String instuction = "tap anywhere to begin";
            canvas.drawText(instuction, x, y+100, paint);
        }

    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Bitmap frame = BitmapFactory.decodeResource(this.getResources(), R.drawable.frame);


        MenuOption firstOption = new MenuOption(this, 100, 10);

        Bitmap twr2Aimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_2a);
        GameObject twr2Aobj = new GameObject(0, 0, 16, 16);
        twr2Aobj.setBmp(twr2Aimg);
        firstOption.addSubOption("2a", twr2Aobj);

        Bitmap twr2Bimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_2b);
        GameObject twr2Bobj = new GameObject(0, 0, 16, 16);
        twr2Bobj.setBmp(twr2Bimg);
        firstOption.addSubOption("2b", twr2Bobj);

        Bitmap twr2Cimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_2c);
        GameObject twr2Cobj = new GameObject(0, 0, 16, 16);
        twr2Cobj.setBmp(twr2Cimg);
        firstOption.addSubOption("2c", twr2Cobj);

        Bitmap twr2dimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_2d);
        GameObject twr2dobj = new GameObject(0, 0, 16, 16);
        twr2dobj.setBmp(twr2dimg);
        firstOption.addSubOption("2d", twr2dobj);

        Bitmap twr2eimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_2e);
        GameObject twr2eobj = new GameObject(0, 0, 16, 16);
        twr2eobj.setBmp(twr2eimg);
        firstOption.addSubOption("2e", twr2eobj);

        menu.add(firstOption);

        MenuOption secondOption = new MenuOption(this, 400, 10);

        Bitmap twr3Aimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_3a);
        GameObject twr3Aobj = new GameObject(0, 0, 16, 16);
        twr3Aobj.setBmp(twr3Aimg);
        secondOption.addSubOption("3a", twr3Aobj);

        Bitmap twr3Bimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_3b);
        GameObject twr3Bobj = new GameObject(0, 0, 16, 16);
        twr3Bobj.setBmp(twr3Bimg);
        secondOption.addSubOption("3b", twr3Bobj);

        Bitmap twr3cimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_3c);
        GameObject twr3cobj = new GameObject(0, 0, 16, 16);
        twr3cobj.setBmp(twr3cimg);
        secondOption.addSubOption("3c", twr3cobj);

        Bitmap twr3dimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_3d);
        GameObject twr3dobj = new GameObject(0, 0, 16, 16);
        twr3dobj.setBmp(twr3dimg);
        secondOption.addSubOption("3d", twr3dobj);

        menu.add(secondOption);

        MenuOption thirdOption = new MenuOption(this, 700, 10);

        Bitmap twr4Aimg = BitmapFactory.decodeResource(this.getResources(), R.drawable.tower_4a);
        GameObject twr4Aobj = new GameObject(0, 0, 16, 16);
        twr4Aobj.setBmp(twr4Aimg);
        thirdOption.addSubOption("4a", twr4Aobj);

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
