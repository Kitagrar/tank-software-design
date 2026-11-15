package ru.mipt.bit.platformer.model.state;

/** Living domain State below 15% health: one-third-speed movement, no shooting. */
public final class HeavyTankState implements TankState {
    public static final HeavyTankState INSTANCE = new HeavyTankState();

    private HeavyTankState() {
    }

    @Override
    public float movementDurationMultiplier() {
        return 3f;
    }

    @Override
    public boolean canShoot() {
        return false;
    }
}
