package cz.crcs.sekan.miip;

import java.util.Arrays;

public class Border {
    private static final int TOP = 0;
    private static final int RIGHT = 1;
    private static final int BOTTOM = 2;
    private static final int LEFT = 3;
    private float borders[] = new float[4];

    Border(float size) {
        Arrays.fill(borders, size);
    }

    Border(float width, float height) {
        borders[RIGHT] = borders[LEFT] = width;
        borders[TOP] = borders[BOTTOM] = height;
    }

    Border(float top, float right, float bottom, float left) {
        borders[TOP] = top;
        borders[BOTTOM] = bottom;
        borders[LEFT] = left;
        borders[RIGHT] = right;
    }

    public float getTop() {
        return borders[TOP];
    }

    public float getRight() {
        return borders[RIGHT];
    }

    public float getBottom() {
        return borders[BOTTOM];
    }

    public float getLeft() {
        return borders[LEFT];
    }
}
