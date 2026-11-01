package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.command.Command;
import ru.mipt.bit.platformer.command.MoveCommand;
import ru.mipt.bit.platformer.command.ShootCommand;
import ru.mipt.bit.platformer.control.*;
import ru.mipt.bit.platformer.model.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TankControllerTest {
    @Test
    void creatingMoveAndShootCommandsHasNoModelSideEffects() {
        GameLevel level = new GameLevel(5, 3, new GridPoint2(1, 1), List.of());
        Tank tank = level.getPlayerTank();
        Command move = new TankController(tank, () -> Optional.of(TankAction.move(Direction.UP)), level)
                .createCommand().orElseThrow();
        Command shoot = new TankController(tank, () -> Optional.of(TankAction.shoot()), level)
                .createCommand().orElseThrow();
        assertInstanceOf(MoveCommand.class, move);
        assertInstanceOf(ShootCommand.class, shoot);
        assertEquals(Direction.RIGHT, tank.getDirection());
        assertTrue(tank.isReadyToMove());
        assertTrue(level.getBullets().isEmpty());
        shoot.execute();
        assertEquals(1, level.getBullets().size());
        move.execute();
        assertEquals(Direction.UP, tank.getDirection());
        assertEquals(new GridPoint2(1, 2), tank.getDestinationCoordinates());
        assertFalse(tank.isReadyToMove());
    }

    @Test
    void noInputCreatesNoCommand() {
        GameLevel level = new GameLevel(5, 3, new GridPoint2(1, 1), List.of());
        TankController controller = new TankController(level.getPlayerTank(),
                new KeyboardControlStrategy(direction -> false), level);
        assertTrue(controller.createCommand().isEmpty());
        assertTrue(level.getBullets().isEmpty());
        assertTrue(level.getPlayerTank().isReadyToMove());
    }

    @Test
    void deadPlayerAndAiNeverAskStrategyAndStaleCommandsAreInert() {
        GameLevel level = new GameLevel(5, 3, new GridPoint2(0, 0), List.of())
                .withAdditionalTanks(List.of(new GridPoint2(3, 1)));
        int[] decisions = {0};
        for (Tank tank : level.getTanks()) {
            TankController controller = new TankController(tank, () -> {
                decisions[0]++;
                return Optional.of(TankAction.shoot());
            }, level);
            Command staleShot = controller.createCommand().orElseThrow();
            Command staleMove = new MoveCommand(tank, Direction.RIGHT, level.getCollisionObstacle());
            int before = decisions[0];
            GridPoint2 cell = tank.getCoordinates();
            tank.takeDamage(tank.getHealth());
            assertTrue(controller.createCommand().isEmpty());
            level.tick(0);
            assertTrue(controller.createCommand().isEmpty());
            controller.update();
            staleShot.execute();
            staleMove.execute();
            assertEquals(before, decisions[0]);
            assertEquals(cell, tank.getCoordinates());
            assertTrue(tank.isReadyToMove());
            assertFalse(level.containsTank(tank));
        }
        assertTrue(level.getTanks().isEmpty());
        assertTrue(level.getBullets().isEmpty());
        assertNotNull(level.getPlayerTank());
    }

    @Test
    void foreignTankDoesNotAskStrategy() {
        GameLevel level = new GameLevel(5, 3, new GridPoint2(0, 0), List.of());
        TankController controller = new TankController(new Tank(new GridPoint2(2, 1)), () -> {
            fail("Foreign tank must not ask strategy");
            return Optional.empty();
        }, level);
        assertTrue(controller.createCommand().isEmpty());
    }

    @Test
    void stationaryAiCanShootOnConsecutiveFrames() {
        GameLevel level = new GameLevel(5, 3, new GridPoint2(0, 0), List.of());
        Random random = new Random(1) {
            @Override
            public int nextInt(int bound) {
                return 4;
            }
        };
        TankController controller = new TankController(level.getPlayerTank(), new RandomAiControlStrategy(random), level);
        controller.createCommand().orElseThrow().execute();
        controller.createCommand().orElseThrow().execute();
        assertEquals(2, level.getBullets().size());
        assertTrue(level.getPlayerTank().isReadyToMove());
    }

    @Test
    void asksStrategyOnlyWhenReadyAndResumesAfterCompletion() {
        GameLevel level = new GameLevel(5, 1, new GridPoint2(0, 0), List.of());
        Tank tank = level.getPlayerTank();
        int[] decisions = {0};
        TankController controller = new TankController(tank, () -> {
            decisions[0]++;
            return Optional.of(TankAction.move(Direction.RIGHT));
        }, level);
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
        int[] collisionChecks = {0};
        GameLevel observedLevel = observedLevel(cell -> {
            collisionChecks[0]++;
            return false;
        });
        Tank tank = observedLevel.getPlayerTank();
        new TankController(tank, new KeyboardControlStrategy(direction -> false), observedLevel).update();
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
            GameLevel level = new GameLevel(5, 5, new GridPoint2(2, 2), List.of());
            Tank tank = level.getPlayerTank();
            new TankController(tank, strategy, level).update();
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
        GameLevel observedLevel = observedLevel(cell -> {
            attempts[0]++;
            return true;
        });
        Tank tank = observedLevel.getPlayerTank();
        TankController controller = new TankController(tank, new RandomAiControlStrategy(random), observedLevel);
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
        GameLevel level = new GameLevel(5, 1, new GridPoint2(0, 0), List.of());
        Tank tank = level.getPlayerTank();
        TankController controller = new TankController(tank, new RandomAiControlStrategy(random), level);
        controller.update();
        tank.updateMovement(0.1f);
        for (int frame = 0; frame < 10; frame++) {
            controller.update();
        }
        assertEquals(1, decisions[0]);
        assertEquals(new GridPoint2(1, 0), tank.getDestinationCoordinates());
    }

    private GameLevel observedLevel(Obstacle obstacle) {
        return new GameLevel(5, 1, new GridPoint2(0, 0), List.of()) {
            @Override
            public Obstacle getCollisionObstacle() {
                return obstacle;
            }
        };
    }
}
