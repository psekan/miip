package cz.crcs.sekan.miip;

public class RenderPosition {
    private static final int X = 0;
    private static final int Y = 1;
    private static final int WIDTH = 2;
    private static final int HEIGHT = 3;
    private float borders[] = new float[4];

    RenderPosition(float x, float y, float width, float height) {
        borders[X] = x;
        borders[Y] = y;
        borders[WIDTH] = width;
        borders[HEIGHT] = height;
    }

    public float getX() {
        return borders[X];
    }

    public float getY() {
        return borders[Y];
    }

    public float getWidth() {
        return borders[WIDTH];
    }

    public float getHeight() {
        return borders[HEIGHT];
    }
}
