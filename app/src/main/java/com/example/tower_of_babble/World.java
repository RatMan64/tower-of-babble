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

        //milliseconds til tile is finalized, and placed permanently
        private int msTillPlace;

        private boolean reachableAbove;
        private boolean reachableBelow;
        private boolean reachableRight;
        private boolean reachableLeft;


        public Tile() {
            state = TileState.UNREACHABLE;
            placedTile = null;
            msTillPlace = 0;
            reachableAbove = false;
            reachableBelow = false;
            reachableLeft = false;
            reachableRight = false;
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

    private class PlaceableChecker {
        private boolean needsLeft;
        private boolean needsRight;
        private boolean needsAbove;
        private boolean needsBelow;

        public PlaceableChecker(boolean needsLeft_, boolean needsRight_, boolean needsAbove_, boolean needsBelow_) {
            needsLeft = needsLeft_;
            needsRight = needsRight_;
            needsAbove = needsAbove_;
            needsBelow = needsBelow_;
        }
    }

    private HashMap<String, PlaceableChecker> placements;

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
            map.get(height-1).get(col).reachableBelow = true;
        }

        imgs = new HashMap<>();
        imgs.put("2a", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_2a)));
        imgs.put("2b", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_2b)));
        imgs.put("2c", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_2c)));
        imgs.put("2d", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_2d)));
        imgs.put("2e", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_2e)));


        imgs.put("3a", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_3a)));
        imgs.put("3b", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_3b)));
        imgs.put("3c", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_3c)));
        imgs.put("3d", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_3d)));

        imgs.put("4a", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.tower_4a)));

        imgs.put("empty", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.empty_plot)));

        imgs.put("placing", new GameObject(0, 0, 16, 16, BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.placing)));


        placements = new HashMap<>();
        placements.put("2a", new PlaceableChecker(false, false, true, true));
        placements.put("2b", new PlaceableChecker(false, true, false, true));
        placements.put("2c", new PlaceableChecker(true, false, false, true));
        placements.put("2d", new PlaceableChecker(true, false, true, false));
        placements.put("2e", new PlaceableChecker(false, true, true, false));

        placements.put("3a", new PlaceableChecker(true, false, true, true));
        placements.put("3b", new PlaceableChecker(false, true, true, true));
        placements.put("3c", new PlaceableChecker(true, true, true, false));
        placements.put("3d", new PlaceableChecker(true, true, false, true));

        placements.put("4a", new PlaceableChecker(true, true, true, true));
    }

    // request to place a tile at x, y on the map. Should send a request to the server.
    public void requestPlace(int x, int y, String placedTile) {
        // This is temporary client code. Should be replaced by a request to the server
        tryBeginPlace(x, y, placedTile);
    }

    public boolean tryBeginPlace(int x, int y, String placedTile) {        Tile t = map.get(y).get(x);
        if(t.state == TileState.REACHABLE) {
            if(t.reachableAbove && placements.get(placedTile).needsAbove ||
                    t.reachableBelow && placements.get(placedTile).needsBelow ||
                    t.reachableLeft && placements.get(placedTile).needsLeft ||
                    t.reachableRight && placements.get(placedTile).needsRight) {
                beginPlace(x, y, placedTile);
                return true;
            }
        }
        return false;
    }

    // start placing a tile at x, y on the map. Used server side to place tiles
    public void beginPlace(int x, int y, String placedTile) {
        map.get(y).get(x).beginPlacing(placedTile);
    }

    public boolean tryPlaceTile(int x, int y, String placedTile) {
        Tile t = map.get(y).get(x);
        if(t.state == TileState.REACHABLE) {
            if(t.reachableAbove && placements.get(placedTile).needsAbove ||
                t.reachableBelow && placements.get(placedTile).needsBelow ||
                t.reachableLeft && placements.get(placedTile).needsLeft ||
                t.reachableRight && placements.get(placedTile).needsRight) {
                placeTile(x, y, placedTile);
                return true;
            }
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

        if(placements.get(placedTile).needsAbove) {
            int targetY = y - 1;
            if(targetY >= 0) {
                Tile t = map.get(targetY).get(x);
                if(t.state == TileState.UNREACHABLE || t.state == TileState.REACHABLE) {
                    t.state = TileState.REACHABLE;
                    t.reachableBelow = true;
                }
            }
        }

        if(placements.get(placedTile).needsBelow) {
            int targetY = y + 1;
            if(targetY < height) {
                Tile t = map.get(targetY).get(x);
                if(t.state == TileState.UNREACHABLE || t.state == TileState.REACHABLE) {
                    t.state = TileState.REACHABLE;
                    t.reachableAbove = true;
                }
            }
        }

        if(placements.get(placedTile).needsLeft) {
            int targetX = x - 1;
            if(targetX >= 0) {
                Tile t = map.get(y).get(targetX);
                if(t.state == TileState.UNREACHABLE || t.state == TileState.REACHABLE) {
                    t.state = TileState.REACHABLE;
                    t.reachableRight = true;
                }
            }

        }

        if(placements.get(placedTile).needsRight) {
            int targetX = x + 1;
            if(targetX < width) {
                Tile t = map.get(y).get(targetX);
                if(t.state == TileState.UNREACHABLE || t.state == TileState.REACHABLE) {
                    t.state = TileState.REACHABLE;
                    t.reachableLeft = true;
                }
            }

        }
    }

    public void update(int msElapsed) {
        int y = 0;
        for(ArrayList<Tile> row : map) {
            int x = 0;
            for(Tile tile : row) {
                boolean wasPlaced = tile.state != TileState.PLACED;
                tile.update(msElapsed);
                if(!wasPlaced && tile.state == TileState.PLACED) {
                    updateSurrounding(x, y, tile.placedTile);
                }
                ++x;
            }
            ++y;
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
                    case PLACING: {
                        renderImg = imgs.get("placing");
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
