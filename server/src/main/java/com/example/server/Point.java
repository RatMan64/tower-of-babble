package com.example.server;

// an abstraction for a 2d position we should have made sooner
public class Point {
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(Object[] args) {
        x = (int) args[0];
        y = (int) args[1];
    }

    public Boolean equals(Point point) {
        return (this.x == point.x &&
                this.y == point.y);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        return hash;
    }

    public int compareTo(Point point) {
        if (this.x - point.x != 0) {
            return (this.x - point.x);
        } else {
            return (this.y - point.y);
        }
    }

    public String toString() {
        return ("<" + this.x + ", " + this.y + ">");
    }
}
