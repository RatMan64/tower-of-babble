package com.example.tower_of_babble;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class GameObject {
    private float x;
    private float y;
    private int width;
    private int height;
    private Bitmap bmp;

    public GameObject(float x_, float y_, int width_, int height_) {
        this.x = x_;
        this.y = y_;
        this.width = width_;
        this.height = height_;
        this.bmp = null;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = Bitmap.createScaledBitmap(bmp,width * 10, height * 10, false);
    }

    void draw(Canvas canvas, Camera cam) {
        canvas.drawBitmap(bmp, cam.getX() + x, cam.getY() + y, null);
    }

    float getX() {
        return this.x;
    }

    float getY() {
        return this.y;
    }

    void setX(float x_) {
        x = x_;
    }

    void setY(float y_) {
        y = y_;
    }

    float getWidth() {
        return width;
    }

    float getHeight() {
        return height;
    }
}
