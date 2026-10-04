package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

public class CompositeObstacle implements Obstacle {
    private final Obstacle[] obstacles;

    public CompositeObstacle(Obstacle... obstacles) {
        this.obstacles = obstacles.clone();
    }

    @Override
    public boolean occupies(GridPoint2 coordinates) {
        for (Obstacle obstacle : obstacles) {
            if (obstacle != null && obstacle.occupies(coordinates)) {
                return true;
            }
        }
        return false;
    }
}
