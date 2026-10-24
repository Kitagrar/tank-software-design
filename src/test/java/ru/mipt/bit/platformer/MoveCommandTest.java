package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.command.Command;
import ru.mipt.bit.platformer.command.MoveCommand;
import ru.mipt.bit.platformer.model.*;

import static org.junit.jupiter.api.Assertions.*;

class MoveCommandTest {
    @Test
    void delegatesDirectionAndSameObstacleExactlyOnceOnExecution() {
        Obstacle obstacle = cell -> false;
        int[] calls = {0};
        Movable receiver = (direction, receivedObstacle) -> {
            calls[0]++;
            assertEquals(Direction.LEFT, direction);
            assertSame(obstacle, receivedObstacle);
            return true;
        };
        Command command = new MoveCommand(receiver, Direction.LEFT, obstacle);
        assertEquals(0, calls[0]);
        command.execute();
        assertEquals(1, calls[0]);
    }

    @Test
    void blockedCommandDoesNotChangeMovementState() {
        Tank tank = new Tank(new GridPoint2(1, 1));
        new MoveCommand(tank, Direction.RIGHT, new Tree(new GridPoint2(2, 1))).execute();
        assertEquals(new GridPoint2(1, 1), tank.getCoordinates());
        assertEquals(new GridPoint2(1, 1), tank.getDestinationCoordinates());
        assertTrue(tank.isReadyToMove());
    }
}
