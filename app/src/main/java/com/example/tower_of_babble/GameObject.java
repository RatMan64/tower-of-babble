package com.example.tower_of_babble;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class GameObject {
    private float x;
    private float y;
    private float width;
    private float height;
    private Bitmap bmp;

    public GameObject(float x_, float y_, float width_, float height_) {
        this.x = x_;
        this.y = y_;
        this.width = width_;
        this.height = height_;
        this.bmp = null;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    void draw(Canvas canvas, Camera cam) {
        canvas.drawBitmap(bmp, cam.getX() + x * width, cam.getY() + y * height, null);
    }
}
