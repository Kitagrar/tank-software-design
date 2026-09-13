package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import static com.badlogic.gdx.math.MathUtils.isEqual;

public class Tank {
    private static final float MOVEMENT_SPEED = 0.4f;

    private GridPoint2 coordinates;
    private GridPoint2 destinationCoordinates;
    private float movementProgress = 1f;
    private float rotation;

    public Tank(GridPoint2 initialCoordinates) {
        this.coordinates = new GridPoint2(initialCoordinates);
        this.destinationCoordinates = new GridPoint2(initialCoordinates);
        this.rotation = 0f;
    }

    public boolean tryMove(Direction direction, Tree obstacle) {
        if (!isEqual(movementProgress, 1f)) {
            return false;
        }

        GridPoint2 next = new GridPoint2(coordinates.x + direction.delta().x, coordinates.y + direction.delta().y);
        if (obstacle != null && obstacle.getCoordinates().equals(next)) {
            return false;
        }

        destinationCoordinates = next;
        movementProgress = 0f;
        rotation = direction.rotation();
        return true;
    }

    public void updateMovement(float deltaTime) {
        if (isEqual(movementProgress, 1f)) {
            return;
        }

        movementProgress += deltaTime / MOVEMENT_SPEED;
        if (movementProgress >= 1f) {
            movementProgress = 1f;
            coordinates.set(destinationCoordinates);
        }
    }

    public GridPoint2 getCoordinates() {
        return coordinates;
    }

    public GridPoint2 getDestinationCoordinates() {
        return destinationCoordinates;
    }

    public float getMovementProgress() {
        return movementProgress;
    }

    public float getRotation() {
        return rotation;
    }

    public void setCoordinates(GridPoint2 coordinates) {
        this.coordinates.set(coordinates);
        this.destinationCoordinates.set(coordinates);
        this.movementProgress = 1f;
    }
}
