package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.Direction;

import java.util.Optional;

public interface TankControlStrategy {
    Optional<Direction> chooseDirection();
}
