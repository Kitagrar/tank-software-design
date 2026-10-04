package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

public class MapBorder implements Obstacle {
    private final int width;
    private final int height;

    public MapBorder(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean occupies(GridPoint2 coordinates) {
        return coordinates.x < 0
                || coordinates.y < 0
                || coordinates.x >= width
                || coordinates.y >= height;
    }
}
