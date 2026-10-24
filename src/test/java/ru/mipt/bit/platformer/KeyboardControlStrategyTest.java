package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.mipt.bit.platformer.control.KeyboardControlStrategy;
import ru.mipt.bit.platformer.model.Direction;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardControlStrategyTest {
    @ParameterizedTest
    @EnumSource(Direction.class)
    void readsEachDirectionThroughInputSource(Direction pressed) {
        KeyboardControlStrategy strategy = new KeyboardControlStrategy(direction -> direction == pressed);
        assertEquals(Optional.of(pressed), strategy.chooseDirection());
    }

    @Test
    void noKeyProducesNoIntention() {
        assertTrue(new KeyboardControlStrategy(direction -> false).chooseDirection().isEmpty());
    }

    @Test
    void preservesExistingPriorityWhenSeveralKeysArePressed() {
        assertEquals(Optional.of(Direction.UP), new KeyboardControlStrategy(direction -> true).chooseDirection());
        assertEquals(Optional.of(Direction.RIGHT),
                new KeyboardControlStrategy(direction -> direction != Direction.UP).chooseDirection());
    }
}
