package ru.mipt.bit.platformer.model.state;

/** Domain State above 70% health: normal movement and shooting. */
public final class LightTankState implements TankState {
    public static final LightTankState INSTANCE = new LightTankState();

    private LightTankState() {
    }

    @Override
    public float movementDurationMultiplier() {
        return 1f;
    }

    @Override
    public boolean canShoot() {
        return true;
    }
}
