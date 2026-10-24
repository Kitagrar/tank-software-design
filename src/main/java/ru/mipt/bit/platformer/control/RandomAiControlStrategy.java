package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.Direction;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public final class RandomAiControlStrategy implements TankControlStrategy {
    private final Random random;

    public RandomAiControlStrategy(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    @Override
    public Optional<Direction> chooseDirection() {
        Direction[] directions = Direction.values();
        return Optional.of(directions[random.nextInt(directions.length)]);
    }
}
