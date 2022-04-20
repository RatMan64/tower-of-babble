package com.example.tower_of_babble;

public class Camera {
    private float x;
    private float y;

    public Camera() {
        this.x = 0;
        this.y = 0;
    }

    public Camera(float x_, float y_) {
        this.x = x_;
        this.y = y_;
    }

    float getX() {
        return this.x;
    }

    float getY() {
        return this.y;
    }

    void move(float dx, float dy) {
        this.x += dx;
        this.y += dy;
    }

    void setPos(float x_, float y_) {
        this.x = x_;
        this.y = y_;
    }

    void setX(float x_) {
        this.x = x_;
    }

    void setY(float y_) {
        this.y = y_;
    }
}
