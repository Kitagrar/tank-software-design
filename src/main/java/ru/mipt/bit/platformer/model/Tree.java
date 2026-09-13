package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;

public class Tree {
    private final GridPoint2 coordinates;
    private final Rectangle rectangle;

    public Tree(GridPoint2 coordinates) {
        this.coordinates = new GridPoint2(coordinates);
        this.rectangle = new Rectangle();
    }

    public GridPoint2 getCoordinates() {
        return coordinates;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }
}
