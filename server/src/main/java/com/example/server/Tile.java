package com.example.server;


import java.util.Arrays;

public class Tile {
    private long age;
    public final int owner_id;
    public boolean is_placed;

    public static final int MAX_TILE_AGE = 10;

    public Tile(int owner_id) {
        this.age = 0;
        is_placed = false;
        this.owner_id = owner_id;
    }
    public Tile(Object[] args){
        age = (Long) args[0];
        owner_id = (int) args[1];

    }

    public Tile(long age, int owner_id) {
        this.age = age;
        this.owner_id = owner_id;
    }

    public Object[] to_obj_arr(){
        return Arrays.stream(Tile.class.getFields()).map(f -> {
            try { return f.get(this);
            } catch (IllegalAccessException e) {
                e.printStackTrace(); }
            return null;
        }).toArray();

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
