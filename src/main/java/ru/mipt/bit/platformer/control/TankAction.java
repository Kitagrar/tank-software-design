package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.model.Direction;

import java.util.Objects;
import java.util.Optional;

public record TankAction(Kind kind, Optional<Direction> direction) {
    public enum Kind { MOVE, SHOOT }

    public TankAction {
        Objects.requireNonNull(kind);
        Objects.requireNonNull(direction);
        if ((kind == Kind.MOVE) != direction.isPresent()) {
            throw new IllegalArgumentException("Only MOVE must have a direction");
        }
    }

    public static TankAction move(Direction direction) {
        return new TankAction(Kind.MOVE, Optional.of(direction));
    }

    public static TankAction shoot() {
        return new TankAction(Kind.SHOOT, Optional.empty());
    }
}
