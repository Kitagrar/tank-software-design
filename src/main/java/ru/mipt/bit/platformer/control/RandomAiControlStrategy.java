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
    public Optional<TankAction> chooseAction() {
        Direction[] directions = Direction.values();
        int choice = random.nextInt(directions.length + 1);
        return Optional.of(choice == directions.length
                ? TankAction.shoot() : TankAction.move(directions[choice]));
    }
}
