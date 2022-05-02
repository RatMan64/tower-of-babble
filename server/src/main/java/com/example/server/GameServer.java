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

public class GameServer {

    public ConcurrentLinkedQueue<Event> in_events;
    public ConcurrentLinkedQueue<Event> out_events;
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
                Event e = out_events.poll();
                if (e == null) continue;
                for(ObjectOutputStream oos : out_streams){
                    try {
                        //oos.writeInt(e.type);
                        oos.writeInt(e.point.x);
                        oos.writeInt(e.point.y);
                        oos.writeInt(e.tile.owner_id);
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
                int cid = client_id;
                while (true) {
                    try {
                        // should just be placed tiles?
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


            //TODO: make thread safe
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
    }



    public void run(){
        while (true){
            //TODO: proper time handling
//            update_tiles(Instant.now().toEpochMilli());

            for(Event e = in_events.poll(); e != null; e = in_events.poll())
                handle_event(e);
        }
    }

    public void handle_event(Event e) {
        switch(e.type) {
            case PiecePlaced: {
                out_events.add(e);
            }
            default: return;
        }
    }

    public Event getEventFromStream(int cid, ObjectInputStream ois) throws IOException, ClassNotFoundException {
        /*Event.Type e = Event.Type.values()[ois.readInt()];
        switch (e){
            case PiecePlaced:{
                return new Event(e,
                        new Point((Object[]) ois.readObject()),
                        new Tile((Object[]) ois.readObject()));
            }
            default: return null;

        }*/
        return new Event(Event.Type.PiecePlaced,
                new Point(ois.readInt(), ois.readInt()),
                new Tile(cid));
    }

    public void update_tiles(long now) {
        //Kevin, if tile is 5 seconds old and isnt permenent yet place it
        tile_list.entrySet().stream()
                .filter(t -> !t.getValue().is_placed && 5 > now - t.getValue().getAge())
                .forEach(entry -> {
                    Point p = entry.getKey();
                    Tile t = entry.getValue();
                    // TODO: push permanent tile set event
                });

    }
}