package com.example.tower_of_babble;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.example.server.Point;
import com.example.server.Tile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameThread extends Thread{

    private boolean running;
    private GameSurface gameSurface;
    private SurfaceHolder surfaceHolder;
    private int user_id;
    private Socket socket;

    public GameThread(GameSurface gameSurface, SurfaceHolder surfaceHolder)  {
        this.gameSurface= gameSurface;
        this.surfaceHolder= surfaceHolder;
    }
    private void setup() throws IOException, ClassNotFoundException {

        // (kevin) 10.0.2.2 is the emulator host's localhost alias
        socket = new Socket("10.0.2.2", 8989);
        System.out.println("got conn");


        var oos = new ObjectOutputStream(socket.getOutputStream());
        var ois = new ObjectInputStream(socket.getInputStream());

        System.out.println("reading int");
        user_id = ois.readInt();
        System.out.println("got id: " + user_id);

        //upon just joining if some tiles are already placed
        //need to stop it?
        new Thread(() -> {
            while(true){
                Object[] initialTiles = new Object[0];
                try {
                    initialTiles = (Object[]) ois.readObject();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
                gameSurface.world.placeTile((int)initialTiles[0], (int)initialTiles[1], initialTiles[3].toString());
            }
        }).start();
    }

    @Override
    public void run() {
        // todo handle exceptions gracefully
        try {
            setup();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        long startTime = System.nanoTime();
        try {
            var oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Object[] tileArray;
        while(running) {
            Canvas canvas = null;

            try {
                // Get Canvas from Holder and lock it.
                canvas = this.surfaceHolder.lockCanvas();

                // Synchronized
                synchronized (canvas) {
                    this.gameSurface.update();
                    this.gameSurface.draw(canvas);
                }
            } catch (Exception e) {
                // Do nothing.
            } finally {
                if (canvas != null) {
                    // Unlock Canvas.
                    this.surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            long now = System.nanoTime();
            // Interval to redraw game
            // (Change nanoseconds to milliseconds)
            long waitTime = (now - startTime) / 1000000;
            if (waitTime < 10) {
                waitTime = 10; // Millisecond.
            }
            System.out.print(" Wait Time=" + waitTime);

            try {
                // Sleep.
                this.sleep(waitTime);
            } catch (InterruptedException e) {

            }
            startTime = System.nanoTime();
            System.out.print(".");
        }
    }

    public void setRunning(boolean running)  {
        this.running= running;
    }

    public void server_handler_thread(Socket s){

    }
}
