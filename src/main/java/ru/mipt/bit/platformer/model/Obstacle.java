package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

public interface Obstacle {
    boolean occupies(GridPoint2 coordinates);
}
