package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

import java.util.Objects;

/** Discrete projectile state; GameLevel coordinates time and contacts. */
public final class Bullet implements LevelEntity {
    public static final int DEFAULT_DAMAGE = 25;
    public static final float DEFAULT_SECONDS_PER_CELL = 0.1f;
    // Float deltaTime sums may differ by a few ulps at a cell boundary.
    private static final double TIME_EPSILON = 1e-7;

    private final GridPoint2 coordinates;
    private final Direction direction;
    private final int damage;
    private final Tank shooter;
    private final double secondsPerCell;
    private double elapsed;

    public Bullet(GridPoint2 coordinates, Direction direction) {
        this(coordinates, direction, DEFAULT_DAMAGE, DEFAULT_SECONDS_PER_CELL);
    }

    Bullet(GridPoint2 coordinates, Direction direction, Tank shooter) {
        this(coordinates, direction, DEFAULT_DAMAGE, DEFAULT_SECONDS_PER_CELL, shooter);
    }

    public Bullet(GridPoint2 coordinates, Direction direction, int damage, float secondsPerCell) {
        this(coordinates, direction, damage, secondsPerCell, null);
    }

    private Bullet(GridPoint2 coordinates, Direction direction, int damage, float secondsPerCell, Tank shooter) {
        if (damage <= 0 || !Float.isFinite(secondsPerCell) || secondsPerCell <= 0) {
            throw new IllegalArgumentException("Damage and finite cell duration must be positive");
        }
        this.coordinates = new GridPoint2(Objects.requireNonNull(coordinates));
        this.direction = Objects.requireNonNull(direction);
        this.damage = damage;
        this.shooter = shooter;
        this.secondsPerCell = secondsPerCell;
    }

    public GridPoint2 getCoordinates() {
        return new GridPoint2(coordinates);
    }

    public Direction getDirection() {
        return direction;
    }

    public int getDamage() {
        return damage;
    }

    /** A tank can reserve its own bullet's spawn cell on the next frame. */
    boolean canHit(Tank tank) {
        return tank != shooter;
    }

    double timeUntilStep() {
        double remaining = secondsPerCell - elapsed;
        return remaining <= Math.min(TIME_EPSILON, secondsPerCell * 1e-6) ? 0 : remaining;
    }

    void elapse(double deltaTime) {
        elapsed += deltaTime;
    }

    void step() {
        elapsed = Math.max(0, elapsed - secondsPerCell);
        coordinates.add(direction.delta());
    }
}
