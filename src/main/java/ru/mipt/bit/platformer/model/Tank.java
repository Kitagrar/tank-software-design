package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.state.TankState;
import ru.mipt.bit.platformer.model.state.LightTankState;
import ru.mipt.bit.platformer.model.state.MediumTankState;
import ru.mipt.bit.platformer.model.state.HeavyTankState;
import static com.badlogic.gdx.math.MathUtils.isEqual;

/** Domain entity and State-pattern context; health selects its movement/shooting policy. */
public class Tank implements Movable, Obstacle, LevelEntity {
    public static final int DEFAULT_HEALTH = 100;
    private static final float BASE_MOVEMENT_DURATION = 0.4f;

    private GridPoint2 coordinates;
    private GridPoint2 destinationCoordinates;
    private float movementProgress = 1f;
    private Direction direction = Direction.RIGHT;
    private int health;
    private final int maxHealth;
    private TankState state = LightTankState.INSTANCE;

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
        this.maxHealth = health;
    }

    public boolean tryMove(Direction direction, Obstacle obstacle) {
        if (!isAlive() || !isStationary()) {
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
        if (isStationary()) {
            return;
        }

        float duration = BASE_MOVEMENT_DURATION * state.movementDurationMultiplier();
        movementProgress += deltaTime / duration;
        if (movementProgress >= 1f || isEqual(movementProgress, 1f)) {
            movementProgress = 1f;
            coordinates.set(destinationCoordinates);
        }
    }

    public boolean isStationary() {
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
        if (!canShoot()) {
            throw new IllegalStateException("Tank must be alive, stationary and permitted to shoot by its state");
        }
        return new Bullet(new GridPoint2(coordinates).add(direction.delta()), direction, this);
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean canShoot() {
        return isAlive() && isStationary() && state.canShoot();
    }

    public void takeDamage(int damage) {
        if (damage <= 0) {
            throw new IllegalArgumentException("Damage must be positive");
        }
        health = Math.max(0, health - damage);
        if (isAlive()) {
            updateStateFromHealth();
        }
    }

    private void updateStateFromHealth() {
        long scaledHealth = (long) health * 100;
        if (scaledHealth > (long) maxHealth * 70) {
            state = LightTankState.INSTANCE;
        } else if (scaledHealth >= (long) maxHealth * 15) {
            state = MediumTankState.INSTANCE;
        } else {
            state = HeavyTankState.INSTANCE;
        }
    }

    public void setCoordinates(GridPoint2 coordinates) {
        this.coordinates.set(coordinates);
        this.destinationCoordinates.set(coordinates);
        this.movementProgress = 1f;
    }
}
