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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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

    HashMap<String, GameObject> imgs;

    public World(int width_, int height_, SurfaceView surfaceView) {
        width = width_;
        height = height_;

        map = new ArrayList<>();

        // initialize the map
        for(int row = 0; row != height; ++row) {
            map.add(new ArrayList<>());
            for(int col = 0; col != width; ++col) {
                map.get(row).add(new Tile());
            }
        }

        // mark the bottom row reachable
        for(int col = 0; col != width; ++col) {
            map.get(height - 1).get(col).state = TileState.REACHABLE;
        }

        imgs = new HashMap<>();
        imgs.put("2a", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_2a)));
        imgs.put("2b", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_2b)));
        imgs.put("2c", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_2c)));

        imgs.put("3a", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_3a)));
        imgs.put("3b", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_3b)));

        imgs.put("4a", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_4a)));

        imgs.put("empty", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.empty_plot)));
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

    public boolean tryPlaceTile(int x, int y, String placedTile) {
        if(map.get(y).get(x).state == TileState.REACHABLE) {
            placeTile(x, y, placedTile);
            return true;
        }
        return false;
    }

    // place a tile at x y. Used client side when a request is acknowledged
    public void placeTile(int x, int y, String placedTile) {
        map.get(y).get(x).place(placedTile);
        updateSurrounding(x, y, placedTile);
        Log.d("placeTile", "Placed " + placedTile + " at " + x + ", " + y);
    }

    public void updateSurrounding(int x, int y, String placedTile) {
        if(placedTile.equals("2a") || placedTile.equals("3a") || placedTile.equals("3b") || placedTile.equals("4a")) {
            //above
            int targetY = y - 1;
            if(targetY >= 0) {
                Tile t = map.get(targetY).get(x);
                if(t.state == TileState.UNREACHABLE) {
                    map.get(targetY).get(x).state = TileState.REACHABLE;
                }
            }

            //below
            targetY = y + 1;
            if(targetY < height) {
                Tile t = map.get(targetY).get(x);
                if(t.state == TileState.UNREACHABLE) {
                    map.get(targetY).get(x).state = TileState.REACHABLE;
                }
            }
        }


        //left
        if(placedTile.equals("2c") || placedTile.equals("3a") || placedTile.equals("4a")) {
            int targetX = x - 1;
            if(targetX >= 0) {
                Tile t = map.get(y).get(targetX);
                if(t.state == TileState.UNREACHABLE) {
                    map.get(y).get(targetX).state = TileState.REACHABLE;
                }
            }
        }

        //right
        if(placedTile.equals("2b") || placedTile.equals("3b") || placedTile.equals("4a")) {
            int targetX = x + 1;
            if(targetX < width) {
                Tile t = map.get(y).get(targetX);
                if(t.state == TileState.UNREACHABLE) {
                    map.get(y).get(targetX).state = TileState.REACHABLE;
                }
            }
        }
    }

    public void update(int msElapsed) {
        for(ArrayList<Tile> row : map) {
            for(Tile tile : row) {
                boolean wasPlaced = tile.state != TileState.PLACED;
                tile.update(msElapsed);
                if(!wasPlaced && tile.state == TileState.PLACED) {

                }
            }
        }
    }

    public void draw(Canvas canvas, Camera camera) {
        for(int row = 0; row != height; ++row) {
            for(int col = 0; col != width; ++col) {
                Tile tile = map.get(row).get(col);
                GameObject renderImg = null;
                switch (tile.state) {
                    case REACHABLE: {
                        renderImg = imgs.get("empty");
                        break;
                    }
                    case PLACED: {
                        renderImg = imgs.get(tile.placedTile);
                        break;
                    }
                }
                if(renderImg != null) {
                    renderImg.setX(col * renderImg.getWidth() * 10);
                    renderImg.setY(row * renderImg.getHeight() * 10);
                    renderImg.draw(canvas, camera);
                }
            }
        }
    }
}
