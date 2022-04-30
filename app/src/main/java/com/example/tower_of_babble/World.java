package com.example.tower_of_babble;

/*
The world consists of a grid of tiles, and provides an interface for interacting with them.
Each tile has a few states
    - Unreachable: The tile currently can't be placed in
    - Reachable: The tile can be placed in
    - Placing: the tile's contents are being placed
    - Placed: a tower piece has been placed in the tile

By default the lowest layer of tiles is reachable.
When the user places a tile on the map, it updates the now reachable neighboring tiles.
 */

import android.graphics.Canvas;

import java.util.ArrayList;

public class World {
    public enum TileState {
        UNREACHABLE,
        REACHABLE,
        PLACING,
        PLACED
    }

    private class Tile {
        private TileState state;

        // Holds the queued tile when placing, and the placed tile when placed
        private String placedTile;

        //millisseconds till tile is finalized, and placed permanently
        private int msTillPlace;

        public Tile() {
            state = TileState.UNREACHABLE;
            placedTile = null;
            msTillPlace = 0;
        }

        // start the count down to place a tile
        public void beginPlacing(String placedTile_) {
            state = TileState.PLACING;
            placedTile = placedTile_;
            msTillPlace = 5000;
        }

        public void place(String placedTile_) {
            state = TileState.PLACED;
            placedTile = placedTile_;
        }

        public void update(int msElapsed) {
            switch (state) {
                case PLACING: {
                    msTillPlace -= msElapsed;
                    if (msTillPlace < 0) {
                        state = TileState.PLACED;
                    }
                }
            }
        }
    }

    private final int width;
    private final int height;

    private ArrayList<ArrayList<Tile>> map;

    public World(int width_, int height_) {
        width = width_;
        height = height_;

        // initialize the map
        for(int row = 0; row != height; ++row) {
            map.add(new ArrayList<>());
            for(int col = 0; col != width; ++col) {
                map.get(col).add(new Tile());
            }
        }

        // mark the bottom row reachable
        for(int col = 0; col != width; ++col) {
            map.get(height - 1).get(col).state = TileState.REACHABLE;
        }
    }

    // request to place a tile at x, y on the map. Should send a request to the server.
    public void requestPlace(int x, int y, String placedTile) {
        // This is temporary client code. Should be replaced by a request to the server
        beginPlace(x, y, placedTile);
    }

    // start placing a tile at x, y on the map. Used server side to place tiles
    public void beginPlace(int x, int y, String placedTile) {
        map.get(y).get(x).beginPlacing(placedTile);
    }

    // place a tile at x y. Used client side when a request is acknowledged
    public void placeTile(int x, int y, String placedTile) {
        map.get(y).get(x).place(placedTile);
    }

    public void update(int msElapsed) {
        for(ArrayList<Tile> row : map) {
            for(Tile tile : row) {
                tile.update(msElapsed);
            }
        }
    }

    public void draw(Canvas canvas, Camera camera) {

    }
}
