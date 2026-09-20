package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.model.Direction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DirectionTest {
    @Test
    void directionContainsMovementVectorAndRotation() {
        assertEquals(1, Direction.RIGHT.delta().x);
        assertEquals(0, Direction.RIGHT.delta().y);
        assertEquals(0f, Direction.RIGHT.rotation());
        assertEquals(90f, Direction.UP.rotation());
    }
}
