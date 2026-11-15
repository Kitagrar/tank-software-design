package ru.mipt.bit.platformer.port;

import ru.mipt.bit.platformer.model.Direction;

/** Application input-query port; keyboard mechanisms belong in adapters. */
public interface InputSource {
    boolean isPressed(Direction direction);

    default boolean isShootPressed() {
        return false;
    }

    default boolean isHealthTogglePressed() {
        return false;
    }
}
