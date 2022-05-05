package com.example.tower_of_babble;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.example.server.GameServer;
import com.example.server.Point;
import com.example.server.Tile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

// run the game loop
public class GameThread extends Thread{

    private boolean running;
    private GameSurface gameSurface;
    private SurfaceHolder surfaceHolder;
    private int user_id;
    private Socket socket;

    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public GameThread(GameSurface gameSurface, SurfaceHolder surfaceHolder)  {
        this.gameSurface= gameSurface;
        this.surfaceHolder= surfaceHolder;
    }
    private void setup() throws IOException, ClassNotFoundException {

        // (kevin) 10.0.2.2 is the emulator host's localhost alias
        socket = new Socket("206.189.161.67", 8989);
        System.out.println("got conn");


        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());

        this.gameSurface.oos = oos;

        System.out.println("reading int");
        user_id = ois.readInt();
        System.out.println("got id: " + user_id);

        this.gameSurface.id = user_id;

        //upon just joining if some tiles are already placed
        //need to stop it?
        new Thread(() -> {
            while(true){
                try {
                    Object[] arr = (Object[]) ois.readObject();
                    Point p = new Point((int)arr[0], (int)arr[1]);
                    Tile t = new Tile((long)arr[2], (int)arr[3], (String)arr[4]);

                    gameSurface.world.beginPlace(p.x, p.y, t.tile_type);

                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
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
