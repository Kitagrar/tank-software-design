package ru.mipt.bit.platformer.model.state;

/** Domain State policy for movement duration and permission to shoot. */
public interface TankState {
    float movementDurationMultiplier();

    boolean canShoot();
}
