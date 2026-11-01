package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import static com.badlogic.gdx.math.MathUtils.isEqual;

public class Tank implements Movable, Obstacle, LevelEntity {
    public static final int DEFAULT_HEALTH = 100;
    private static final float MOVEMENT_SPEED = 0.4f;

    private GridPoint2 coordinates;
    private GridPoint2 destinationCoordinates;
    private float movementProgress = 1f;
    private Direction direction = Direction.RIGHT;
    private int health;

    public Tank(GridPoint2 initialCoordinates) {
        this(initialCoordinates, DEFAULT_HEALTH);
    }

    public Tank(GridPoint2 initialCoordinates, int health) {
        if (health <= 0) {
            throw new IllegalArgumentException("Initial health must be positive");
        }
        this.coordinates = new GridPoint2(initialCoordinates);
        this.destinationCoordinates = new GridPoint2(initialCoordinates);
        this.health = health;
    }

    public boolean tryMove(Direction direction, Obstacle obstacle) {
        if (!isAlive() || !isReadyToMove()) {
            return false;
        }

        GridPoint2 next = new GridPoint2(coordinates.x + direction.delta().x, coordinates.y + direction.delta().y);
        if (obstacle != null && obstacle.occupies(next)) {
            return false;
        }

        destinationCoordinates = next;
        movementProgress = 0f;
        this.direction = direction;
        return true;
    }

    public void updateMovement(float deltaTime) {
        if (isReadyToMove()) {
            return;
        }

        movementProgress += deltaTime / MOVEMENT_SPEED;
        if (movementProgress >= 1f || isEqual(movementProgress, 1f)) {
            movementProgress = 1f;
            coordinates.set(destinationCoordinates);
        }
    }

    public boolean isReadyToMove() {
        return isEqual(movementProgress, 1f);
    }

    @Override
    public boolean occupies(GridPoint2 cell) {
        return coordinates.equals(cell) || destinationCoordinates.equals(cell);
    }

    public GridPoint2 getCoordinates() {
        return new GridPoint2(coordinates);
    }

    public GridPoint2 getDestinationCoordinates() {
        return new GridPoint2(destinationCoordinates);
    }

    public float getMovementProgress() {
        return movementProgress;
    }

    public float getRotation() {
        return direction.rotation();
    }

    public Direction getDirection() {
        return direction;
    }

    public Bullet shoot() {
        if (!isAlive() || !isReadyToMove()) {
            throw new IllegalStateException("Only a living stationary tank can shoot");
        }
        return new Bullet(new GridPoint2(coordinates).add(direction.delta()), direction, this);
    }

    public int getHealth() {
        return health;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void takeDamage(int damage) {
        if (damage <= 0) {
            throw new IllegalArgumentException("Damage must be positive");
        }
        health = Math.max(0, health - damage);
    }

    public void setCoordinates(GridPoint2 coordinates) {
        this.coordinates.set(coordinates);
        this.destinationCoordinates.set(coordinates);
        this.movementProgress = 1f;
    }
}
