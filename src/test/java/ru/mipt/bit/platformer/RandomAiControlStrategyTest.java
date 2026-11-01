package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.control.RandomAiControlStrategy;
import ru.mipt.bit.platformer.control.TankAction;
import ru.mipt.bit.platformer.model.Direction;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RandomAiControlStrategyTest {
    @Test
    void fifthRandomValueProducesShoot() {
        int[] calls = {0};
        Random random = new Random(1) {
            @Override
            public int nextInt(int bound) {
                assertEquals(5, bound);
                calls[0]++;
                return 4;
            }
        };
        assertEquals(TankAction.shoot(), new RandomAiControlStrategy(random).chooseAction().orElseThrow());
        assertEquals(1, calls[0]);
    }

    @Test
    void sameSeedProducesSameSequenceOfValidDirections() {
        RandomAiControlStrategy first = new RandomAiControlStrategy(new Random(12345));
        RandomAiControlStrategy second = new RandomAiControlStrategy(new Random(12345));
        Set<TankAction> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            TankAction action = first.chooseAction().orElseThrow();
            assertEquals(action, second.chooseAction().orElseThrow());
            seen.add(action);
        }
        assertEquals(5, seen.size());
        assertTrue(seen.contains(TankAction.shoot()));
        for (Direction direction : Direction.values()) {
            assertTrue(seen.contains(TankAction.move(direction)));
        }
    }

    @Test
    void consumesExactlyOneInjectedRandomSelectionPerDecision() {
        int[] calls = {0};
        Random random = new Random(1) {
            @Override
            public int nextInt(int bound) {
                calls[0]++;
                assertEquals(5, bound);
                return 2;
            }
        };
        assertEquals(TankAction.move(Direction.DOWN), new RandomAiControlStrategy(random).chooseAction().orElseThrow());
        assertEquals(1, calls[0]);
    }
}
