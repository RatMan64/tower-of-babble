package com.example.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import jdk.jfr.Event;

public class GameServer {
    public ConcurrentLinkedQueue<Event> in_events;
    public ConcurrentLinkedQueue<Event> out_events;
    public ArrayList<ObjectOutputStream> out_streams;
    public ArrayList<Socket> conns;
    public int client_id = 0;

    public GameServer() throws IOException {
        in_events = new ConcurrentLinkedQueue<>();
        out_events = new ConcurrentLinkedQueue<>();
        out_streams = new ArrayList<>();
        conns = new ArrayList<>();

        ServerSocket s = new ServerSocket(8989);

        // todo dynamic multiple connections
        while (true){
            Socket conn = s.accept();
            System.out.println("got conn");
            conns.add(conn);
            final ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());

            new Thread(() -> {
                while (true) {
                    try {
                        in_events.add((Event) ois.readObject());
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                        System.exit(-1);
                    }
                }
            }).start();

            //(Kevin) Handle output streams
            final var oos = new ObjectOutputStream(conn.getOutputStream());
            oos.writeInt(client_id++);
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
        while (true){
            for(Event e = in_events.poll(); e != null; e = in_events.poll())
                handle_event(e);



        }
    }

    private void handle_event(Event e){

    }
}