package com.example.tower_of_babble;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceView;

import java.util.ArrayList;

// a menu option with multiple selections
public class MenuOption {
    private class SubOption {
        private String tag;
        private GameObject img;

        public SubOption(String tag_, GameObject img_) {
            tag = tag_;
            img = img_;
        }
    }
    private final int MagicScaleX = 10;
    private final int MagicScaleY = 10;

    private GameObject frame;
    private int currentOption;
    private ArrayList<SubOption> options;
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

    public void addSubOption(String tag, GameObject image) {
        image.setX(x + 2 * MagicScaleX);
        image.setY(y + 2 * MagicScaleY);
        options.add(new SubOption(tag, image));
        optionCount++;
    }

    public boolean selected(){return selected;}

    public String currSelectedTag(){
        return options.get(currentOption).tag;
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
;        } else {
            ++currentOption;
            currentOption %= optionCount;
        }
    }

    public void draw(Canvas canvas, Camera camera) {
        frame.draw(canvas, camera);
        options.get(currentOption).img.draw(canvas, camera);
    }
}
