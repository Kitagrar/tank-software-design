package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.control.TankController;
import ru.mipt.bit.platformer.control.TankAction;
import ru.mipt.bit.platformer.command.Command;
import ru.mipt.bit.platformer.model.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MultiTankCollisionTest {
    @Test
    void stationaryTankBlocksAndCellSwapIsForbidden() {
        GameLevel level = level(new GridPoint2(1, 1), new GridPoint2(2, 1));
        Tank a = level.getPlayerTank();
        Tank b = level.getTanks().get(1);
        assertFalse(a.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
        assertFalse(b.tryMove(Direction.LEFT, level.getCollisionObstacle()));
        assertTrue(a.isReadyToMove());
        assertTrue(b.isReadyToMove());
    }

    @Test
    void movingSourceAndDestinationBlockUntilSourceIsReleased() {
        GameLevel level = level(new GridPoint2(1, 1), new GridPoint2(0, 1), new GridPoint2(3, 1));
        Tank a = level.getPlayerTank();
        Tank sourceNeighbour = level.getTanks().get(1);
        Tank targetNeighbour = level.getTanks().get(2);
        assertTrue(a.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
        a.updateMovement(0.2f);
        assertFalse(sourceNeighbour.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
        assertFalse(targetNeighbour.tryMove(Direction.LEFT, level.getCollisionObstacle()));
        a.updateMovement(0.2f);
        assertFalse(targetNeighbour.tryMove(Direction.LEFT, level.getCollisionObstacle()));
        assertTrue(sourceNeighbour.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
    }

    @Test
    void sequentialControllersCannotReserveSameTargetInOneFrame() {
        GameLevel level = level(new GridPoint2(0, 1), new GridPoint2(2, 1));
        Tank a = level.getPlayerTank();
        Tank b = level.getTanks().get(1);
        Command first = new TankController(a, () -> Optional.of(TankAction.move(Direction.RIGHT)), level)
                .createCommand().orElseThrow();
        Command second = new TankController(b, () -> Optional.of(TankAction.move(Direction.LEFT)), level)
                .createCommand().orElseThrow();
        assertTrue(a.isReadyToMove());
        assertTrue(b.isReadyToMove());
        first.execute();
        second.execute();
        assertEquals(new GridPoint2(1, 1), a.getDestinationCoordinates());
        assertEquals(new GridPoint2(2, 1), b.getDestinationCoordinates());
        assertFalse(a.isReadyToMove());
        assertTrue(b.isReadyToMove());
    }

    @Test
    void selfInSharedObstacleDoesNotBlockLegalMove() {
        GameLevel level = level(new GridPoint2(1, 1));
        assertTrue(level.getPlayerTank().tryMove(Direction.UP, level.getCollisionObstacle()));
        assertTrue(level.getCollisionObstacle().occupies(new GridPoint2(1, 1)));
        assertTrue(level.getCollisionObstacle().occupies(new GridPoint2(1, 2)));
    }

    @Test
    void treesAndEveryBorderBlockPlayerAndAi() {
        GameLevel level = new GameLevel(5, 5, new GridPoint2(0, 0), List.of(new GridPoint2(2, 2)))
                .withAdditionalTanks(List.of(new GridPoint2(4, 4)));
        for (Tank tank : level.getTanks()) {
            for (Direction direction : Direction.values()) {
                tank.setCoordinates(new GridPoint2(2, 2).sub(direction.delta()));
                assertFalse(tank.tryMove(direction, level.getCollisionObstacle()));
            }
            GridPoint2[] edges = {new GridPoint2(0, 4), new GridPoint2(4, 0),
                    new GridPoint2(0, 0), new GridPoint2(0, 0)};
            Direction[] outward = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
            for (int i = 0; i < edges.length; i++) {
                tank.setCoordinates(edges[i]);
                assertFalse(tank.tryMove(outward[i], level.getCollisionObstacle()));
            }
        }
    }

    private GameLevel level(GridPoint2 player, GridPoint2... ai) {
        return new GameLevel(5, 3, player, List.of()).withAdditionalTanks(List.of(ai));
    }
}
