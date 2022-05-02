package com.example.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;


public class GameServer {

    public ConcurrentLinkedQueue<Event> in_events;
    public ConcurrentLinkedQueue<Event> out_events;
    public ArrayList<ObjectOutputStream> out_streams;
    public ArrayList<Socket> conns;
    public HashMap<Point, Tile> tile_list;
    public int client_id = 0;
    public final ReentrantLock grid_lock = new ReentrantLock(true);

    public GameServer() throws IOException {
        in_events = new ConcurrentLinkedQueue<>();
        out_events = new ConcurrentLinkedQueue<>();
        out_streams = new ArrayList<>();
        conns = new ArrayList<>();
        tile_list = new HashMap<>();

        ServerSocket s = new ServerSocket(8989);

        new Thread(() -> {
            try {
                client_handler_thread(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        //setup event writer thread
        new Thread(() -> {
            while (true){
                var e = out_events.poll();
                if (e == null) continue;
                for(ObjectOutputStream oos : out_streams){
                    try {
                        Object[] a = tile_to_arr(e);
                        oos.writeObject(a);
                        oos.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.exit(-1);
                    }
                }
            }
        }).start();
    }


    public static void main(String[] args) throws IOException {
        System.out.println("starting server...");
        new GameServer().run();
    }

    public void client_handler_thread(ServerSocket s) throws IOException {
        // todo dynamic multiple connections
        while (true){
            Socket conn = s.accept();
            System.out.println("got conn");
            conns.add(conn);
            final ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());

            new Thread(() -> {
                while (true) {
                    try {
                        Tile t = new Tile((Object[]) ois.readObject());
                        Point p = new Point((Object[]) ois.readObject());
                        in_events.add(new Event(p,t));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            }).start();

            //(Kevin) Handle output streams
            final var oos = new ObjectOutputStream(conn.getOutputStream());
            oos.writeInt(client_id++);


            grid_lock.lock();
            tile_list.entrySet().stream()
                    .map(this::tile_to_arr)
//                    .forEach(oos::writeObject); could be like this but lol exceptions
                    .forEach(e -> {
                        try {
                            oos.writeObject(e);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            System.exit(-1);
                        }
                    });
            grid_lock.unlock();
            oos.flush();

            out_streams.add(oos);
            System.out.println("connected client: "+(client_id-1));
        }
    }



    public void run(){
        while (true){
            grid_lock.lock();

            /*
            //TODO: proper time handling
            update_tiles(Instant.now().toEpochMilli());
            //Kevin, if tile is 5 seconds old and isnt permenent yet place it
            tile_list.entrySet().stream()
                    .filter(t -> !t.getValue().is_placed && 5 > now - t.getValue().getAge())
                    .map(Event::new)
                    .forEach(out_events::add);

             */

            for(Event e = in_events.poll(); e != null; e = in_events.poll()){
                Point p = e.point;
                Tile t = e.tile;
                Tile old_t = tile_list.get(p);
                if(
                        old_t == null ||//      new tile is older
                        (!old_t.is_placed && 0 < t.getAge() - old_t.getAge() )
                ){
                    out_events.add(e);
                    tile_list.put(p,t);
                }
            }
            grid_lock.unlock();
        }
    }


    public Object[] tile_to_arr(Map.Entry<Point, Tile> e){
        Tile t = e.getValue();
        Point p = e.getKey();
        return new Object[]{ p.x, p.y, t.getAge(), t.owner_id };
    }

    public Object[] tile_to_arr(Event e){
        Tile t = e.tile;
        Point p = e.point;
        return new Object[]{ p.x, p.y, t.getAge(), t.owner_id };
    }

    public class Event {
        Point point;
        Tile tile;

        public Event(Point point, Tile tile) {
            this.point = point;
            this.tile = tile;
        }
        public Event(Map.Entry<Point, Tile> e){
            point = e.getKey();
            tile = e.getValue();
        }
    }
}