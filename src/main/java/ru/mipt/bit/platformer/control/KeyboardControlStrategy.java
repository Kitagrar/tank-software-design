package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.input.InputSource;
import ru.mipt.bit.platformer.model.Direction;

import java.util.Objects;
import java.util.Optional;

public final class KeyboardControlStrategy implements TankControlStrategy {
    private final InputSource inputSource;

    public KeyboardControlStrategy(InputSource inputSource) {
        this.inputSource = Objects.requireNonNull(inputSource);
    }

    @Override
    public Optional<TankAction> chooseAction() {
        if (inputSource.isShootPressed()) {
            return Optional.of(TankAction.shoot());
        }
        for (Direction direction : Direction.values()) {
            if (inputSource.isPressed(direction)) {
                return Optional.of(TankAction.move(direction));
            }
        }
        return Optional.empty();
    }
}
