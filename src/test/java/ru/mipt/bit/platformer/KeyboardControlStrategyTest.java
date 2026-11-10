package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.mipt.bit.platformer.control.KeyboardControlStrategy;
import ru.mipt.bit.platformer.control.TankAction;
import ru.mipt.bit.platformer.input.InputSource;
import ru.mipt.bit.platformer.model.Direction;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardControlStrategyTest {
    @Test
    void spaceProducesShootAndTakesPriorityOverMovement() {
        InputSource input = new InputSource() {
            @Override
            public boolean isPressed(Direction direction) {
                return true;
            }

            @Override
            public boolean isShootPressed() {
                return true;
            }
        };
        assertEquals(Optional.of(TankAction.shoot()), new KeyboardControlStrategy(input).chooseAction());
    }

    @Test
    void movementOnlyLambdaHasShootingDisabled() {
        InputSource input = direction -> false;
        assertFalse(input.isShootPressed());
        assertFalse(input.isHealthTogglePressed());
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void readsEachDirectionThroughInputSource(Direction pressed) {
        KeyboardControlStrategy strategy = new KeyboardControlStrategy(direction -> direction == pressed);
        assertEquals(Optional.of(TankAction.move(pressed)), strategy.chooseAction());
    }

    @Test
    void noKeyProducesNoIntention() {
        assertTrue(new KeyboardControlStrategy(direction -> false).chooseAction().isEmpty());
    }

    @Test
    void preservesExistingPriorityWhenSeveralKeysArePressed() {
        assertEquals(Optional.of(TankAction.move(Direction.UP)), new KeyboardControlStrategy(direction -> true).chooseAction());
        assertEquals(Optional.of(TankAction.move(Direction.RIGHT)),
                new KeyboardControlStrategy(direction -> direction != Direction.UP).chooseAction());
    }
}
