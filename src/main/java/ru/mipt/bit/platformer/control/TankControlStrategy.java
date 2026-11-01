package ru.mipt.bit.platformer.control;

import java.util.Optional;

public interface TankControlStrategy {
    Optional<TankAction> chooseAction();
}
