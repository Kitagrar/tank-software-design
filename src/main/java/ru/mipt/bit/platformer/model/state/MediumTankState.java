package ru.mipt.bit.platformer.model.state;

/** Domain State at 15% through 70% health: half-speed movement and shooting. */
public final class MediumTankState implements TankState {
    public static final MediumTankState INSTANCE = new MediumTankState();

    private MediumTankState() {
    }

    @Override
    public float movementDurationMultiplier() {
        return 2f;
    }

    @Override
    public boolean canShoot() {
        return true;
    }
}
