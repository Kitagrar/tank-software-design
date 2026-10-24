package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.control.*;
import ru.mipt.bit.platformer.model.*;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TankControllerTest {
    @Test
    void asksStrategyOnlyWhenReadyAndResumesAfterCompletion() {
        Tank tank = new Tank(new GridPoint2(0, 0));
        int[] decisions = {0};
        TankController controller = new TankController(tank, () -> {
            decisions[0]++;
            return Optional.of(Direction.RIGHT);
        }, new CompositeObstacle(new MapBorder(5, 1), tank));
        controller.update();
        assertEquals(new GridPoint2(1, 0), tank.getDestinationCoordinates());
        controller.update();
        tank.updateMovement(0.2f);
        controller.update();
        assertEquals(1, decisions[0]);
        tank.updateMovement(0.2f);
        controller.update();
        assertEquals(2, decisions[0]);
        assertEquals(new GridPoint2(2, 0), tank.getDestinationCoordinates());
    }

    @Test
    void emptyIntentionDoesNotRequestMovement() {
        Tank tank = new Tank(new GridPoint2(0, 0));
        int[] collisionChecks = {0};
        new TankController(tank, new KeyboardControlStrategy(direction -> false), cell -> {
            collisionChecks[0]++;
            return false;
        }).update();
        assertEquals(0, collisionChecks[0]);
        assertTrue(tank.isReadyToMove());
    }

    @Test
    void sameControllerSupportsKeyboardAndRandomAi() {
        TankControlStrategy[] strategies = {
                new KeyboardControlStrategy(direction -> direction == Direction.RIGHT),
                new RandomAiControlStrategy(new Random(5))
        };
        for (TankControlStrategy strategy : strategies) {
            Tank tank = new Tank(new GridPoint2(2, 2));
            new TankController(tank, strategy, new CompositeObstacle(new MapBorder(5, 5), tank)).update();
            assertFalse(tank.isReadyToMove());
            assertEquals(1, Math.abs(tank.getDestinationCoordinates().x - 2)
                    + Math.abs(tank.getDestinationCoordinates().y - 2));
        }
    }

    @Test
    void blockedAiMakesOneAttemptPerFrameWithoutRetries() {
        int[] decisions = {0};
        int[] attempts = {0};
        Random random = new Random(1) {
            @Override
            public int nextInt(int bound) {
                decisions[0]++;
                return 1;
            }
        };
        Tank tank = new Tank(new GridPoint2(0, 0));
        TankController controller = new TankController(tank, new RandomAiControlStrategy(random), cell -> {
            attempts[0]++;
            return true;
        });
        controller.update();
        assertEquals(1, decisions[0]);
        assertEquals(1, attempts[0]);
        assertTrue(tank.isReadyToMove());
        controller.update();
        assertEquals(2, decisions[0]);
        assertEquals(2, attempts[0]);
    }

    @Test
    void movingAiConsumesNoRandomValues() {
        int[] decisions = {0};
        Random random = new Random(1) {
            @Override
            public int nextInt(int bound) {
                decisions[0]++;
                return 1;
            }
        };
        Tank tank = new Tank(new GridPoint2(0, 0));
        TankController controller = new TankController(tank, new RandomAiControlStrategy(random), cell -> false);
        controller.update();
        tank.updateMovement(0.1f);
        for (int frame = 0; frame < 10; frame++) {
            controller.update();
        }
        assertEquals(1, decisions[0]);
        assertEquals(new GridPoint2(1, 0), tank.getDestinationCoordinates());
    }
}
