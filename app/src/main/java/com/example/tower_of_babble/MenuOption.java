package com.example.tower_of_babble;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceView;

import java.util.ArrayList;

public class MenuOption {
    private final int MagicScaleX = 10;
    private final int MagicScaleY = 10;

    private GameObject frame;
    private int currentOption;
    private ArrayList<GameObject> options;
    private int optionCount;
    private boolean selected;

    private int x;
    private int y;

    public MenuOption(SurfaceView surfaceView, int x_, int y_) {
        frame = new GameObject(x_, y_, 20, 20);
        frame.setBmp(BitmapFactory.decodeResource(surfaceView.getResources(), R.drawable.frame));

        currentOption = 0;
        options = new ArrayList<>();
        optionCount = 0;
        selected = false;
        x = x_;
        y = y_;
    }

    public void addOption(GameObject image) {
        image.setX(x + 2 * MagicScaleX);
        image.setY(y + 2 * MagicScaleY);
        options.add(image);
        optionCount++;
    }

    public void unselect() {
        selected = false;
    }

    public boolean tryClick(float x_, float y_) {
        if (x_ > x && y_ > y && x_ < x + frame.getWidth() * MagicScaleX && y_ < y + frame.getHeight() * MagicScaleY) {
            click();
            return true;
        }
        return false;
    }

    public void click() {
        if(!selected) {
            selected = true;
            Log.d("click", "slected");
;        } else {
            ++currentOption;
            currentOption %= optionCount;
            Log.d("click", "swapped");
        }
    }

    public void draw(Canvas canvas, Camera camera) {
        frame.draw(canvas, camera);
        options.get(currentOption).draw(canvas, camera);
    }
}
