package com.example.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.time.Duration;
import java.time.Instant;

public class GameServer {
    public final int MAX_TILE_AGE = 10;
    public enum EventType {
        PiecePlaced,
    }

    public ConcurrentLinkedQueue<GameEvent> in_events;
    public ConcurrentLinkedQueue<GameEvent> out_events;
    public ArrayList<ObjectOutputStream> out_streams;
    public ArrayList<Socket> conns;
    public HashMap<Point, Tile> tile_list;
    public int client_id = 0;

    public GameServer() throws IOException {
        in_events = new ConcurrentLinkedQueue<>();
        out_events = new ConcurrentLinkedQueue<>();
        out_streams = new ArrayList<>();
        conns = new ArrayList<>();
        tile_list = new HashMap<>();

        ServerSocket s = new ServerSocket(8989);

        // todo dynamic multiple connections
        while (true){
            Socket conn = s.accept();
            System.out.println("got conn");
            conns.add(conn);
            final ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());

            new Thread(() -> {
                int cid = client_id;
                while (true) {
                    try {
                        in_events.add(getEventFromStream(cid, ois));
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            }).start();

            //(Kevin) Handle output streams
            final var oos = new ObjectOutputStream(conn.getOutputStream());
            oos.writeInt(client_id++);

            oos.writeInt(tile_list.size());
            for (Map.Entry<Point, Tile> entry : tile_list.entrySet()) {
                Point p = entry.getKey();
                oos.writeInt(p.x);
                oos.writeInt(p.y);
            }
            oos.flush();

            out_streams.add(oos);
            System.out.println("connected client: "+(client_id-1));
        }


        //kevin, setup event writer thread

        /* commented out because java doesnt compile with unreachable code
        new Thread(() -> {
            while (true){
                Event e = out_events.poll();
                if (e == null) continue;
                for(ObjectOutputStream oos : out_streams){
                    try {
                        oos.writeObject(e);
                        oos.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.exit(-1);
                    }
                }
            }
        }).start();
         */
    }


    public static void main(String[] args) throws IOException {
        System.out.println("starting server...");
        new GameServer().run();
    }



    public void run(){
        Instant lastTime = Instant.now();
        while (true){
            Instant newTime = Instant.now();
            long diff = Duration.between(lastTime, newTime).toMillis();
            lastTime = newTime;

            for(GameEvent ge = in_events.poll(); ge != null; ge = in_events.poll())
                handle_event(ge);

            update_tiles(diff);

        }
    }

    public void handle_event(GameEvent ge) {

    }

    public GameEvent getEventFromStream(int cid, ObjectInputStream ois) {
        int event = ois.readInt();
        EventType type;
        if (event == 0)
            type = EventType.PiecePlaced;

        if (type == EventType.PiecePlaced) {
            int x = ois.readInt();
            int y = ois.readInt();

            return new GameEvent(type, new Point(x, y), new Tile(cid));
        }

        return null;
    }

    public void update_tiles(long diff) {
        for (Map.Entry<Point, Tile> entry : tile_list.entrySet()) {
            Point key = entry.getKey();
            Tile value = entry.getValue();

            if (value.increaseAge(diff)) {
                // TODO: push permanent tile set event
                System.out.println("tile at " + key.toString() + " became permanent");
            }
        }
    }

    public class GameEvent {
        EventType type;
        Point point;
        Tile tile;

        public GameEvent(EventType type, Point point, Tile tile) {
            this.type = type;
            this.point = point;
            this.tile = tile;
        }
    }

    public class Tile {
        private long age;
        public int owner_id;

        public Tile(int owner_id) {
            this.age = 0;
            this.owner_id = owner_id;
        }

        public Tile(long age, int owner_id) {
            this.age = age;
            this.owner_id = owner_id;
        }

        long getAge() { return this.age; }

        boolean increaseAge(long time) {
            if ( this.age < MAX_TILE_AGE ) {
                this.age += time;
                return (this.age >= MAX_TILE_AGE);
            }
            return false;
        }

    }

    public class Point implements Comparable<Point> {
        public int x;
        public int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public Boolean equals(Point point) {
            return (this.x == point.x &&
                    this.y == point.y);
        }

        @Override
        public int hashCode() {
            int hash = 17;
            hash = hash * 31 + x;
            hash = hash * 31 + y;
            return hash;
        }

        @Override
        public int compareTo(Point point){
            if (this.x - point.x != 0) {
                return (this.x - point.x);
            } else {
                return (this.y - point.y);
            }
        }

        public String toString() {
            return ("<" + this.x + ", " + this.y + ">");
        }
    }
}