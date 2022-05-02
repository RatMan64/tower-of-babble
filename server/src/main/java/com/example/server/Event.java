package com.example.server;

public class Event {
    Type type;
    Point point;
    Tile tile;

    public Event(Type type, Point point, Tile tile) {
        this.type = type;
        this.point = point;
        this.tile = tile;
    }
    public enum Type {
        PiecePlaced,
    }
}
