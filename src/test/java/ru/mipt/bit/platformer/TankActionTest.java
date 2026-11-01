package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.mipt.bit.platformer.control.TankAction;
import ru.mipt.bit.platformer.model.Direction;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TankActionTest {
    @ParameterizedTest
    @EnumSource(Direction.class)
    void moveContainsItsDirection(Direction direction) {
        TankAction action = TankAction.move(direction);
        assertEquals(TankAction.Kind.MOVE, action.kind());
        assertEquals(Optional.of(direction), action.direction());
        assertEquals(action, TankAction.move(direction));
    }

    @Test
    void shootHasNoDirectionAndInvalidCombinationsAreRejected() {
        assertEquals(TankAction.Kind.SHOOT, TankAction.shoot().kind());
        assertTrue(TankAction.shoot().direction().isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> new TankAction(TankAction.Kind.MOVE, Optional.empty()));
        assertThrows(IllegalArgumentException.class,
                () -> new TankAction(TankAction.Kind.SHOOT, Optional.of(Direction.RIGHT)));
        assertThrows(NullPointerException.class, () -> TankAction.move(null));
        assertThrows(NullPointerException.class, () -> new TankAction(null, Optional.empty()));
        assertThrows(NullPointerException.class, () -> new TankAction(TankAction.Kind.SHOOT, null));
    }
}
