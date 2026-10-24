package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.control.RandomAiControlStrategy;
import ru.mipt.bit.platformer.model.Direction;

import java.util.EnumSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RandomAiControlStrategyTest {
    @Test
    void sameSeedProducesSameSequenceOfValidDirections() {
        RandomAiControlStrategy first = new RandomAiControlStrategy(new Random(12345));
        RandomAiControlStrategy second = new RandomAiControlStrategy(new Random(12345));
        EnumSet<Direction> seen = EnumSet.noneOf(Direction.class);
        for (int i = 0; i < 100; i++) {
            Direction direction = first.chooseDirection().orElseThrow();
            assertEquals(direction, second.chooseDirection().orElseThrow());
            seen.add(direction);
        }
        assertEquals(EnumSet.allOf(Direction.class), seen);
    }

    @Test
    void consumesExactlyOneInjectedRandomSelectionPerDecision() {
        int[] calls = {0};
        Random random = new Random(1) {
            @Override
            public int nextInt(int bound) {
                calls[0]++;
                assertEquals(4, bound);
                return 2;
            }
        };
        assertEquals(Direction.DOWN, new RandomAiControlStrategy(random).chooseDirection().orElseThrow());
        assertEquals(1, calls[0]);
    }
}
