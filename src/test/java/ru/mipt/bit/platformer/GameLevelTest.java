package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.level.LevelValidator;
import ru.mipt.bit.platformer.level.RandomLevelCreationStrategy;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.GameLevel;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameLevelTest {
    @Test
    void additionalTanksHaveExplicitPlayerImmutableCollectionsAndLiveCollision() {
        GameLevel base = new GameLevel(5, 3, new GridPoint2(0, 0), List.of(new GridPoint2(4, 2)));
        GridPoint2 spawn = new GridPoint2(2, 1);
        List<GridPoint2> spawns = new ArrayList<>(List.of(spawn));
        GameLevel level = base.withAdditionalTanks(spawns);
        spawn.set(3, 2);
        spawns.clear();
        assertSame(level.getPlayerTank(), level.getTank());
        assertEquals(2, level.getTanks().size());
        assertEquals(1, level.getTanks().stream().filter(tank -> tank == level.getPlayerTank()).count());
        assertTrue(level.getTanks().contains(level.getPlayerTank()));
        assertEquals(new GridPoint2(0, 0), level.getPlayerTank().getCoordinates());
        assertEquals(1, base.getTanks().size());
        assertNotSame(base.getPlayerTank(), level.getPlayerTank());
        assertThrows(UnsupportedOperationException.class, () -> level.getTanks().clear());
        assertThrows(UnsupportedOperationException.class, () -> level.getTanks().add(new Tank(new GridPoint2(1, 1))));
        assertThrows(UnsupportedOperationException.class, () -> level.getTrees().clear());
        assertTrue(level.getCollisionObstacle().occupies(new GridPoint2(4, 2)));
        assertTrue(level.getCollisionObstacle().occupies(new GridPoint2(-1, 0)));
        for (Tank tank : level.getTanks()) {
            GridPoint2 source = tank.getCoordinates();
            assertTrue(level.getCollisionObstacle().occupies(source));
            assertTrue(tank.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
            assertTrue(level.getCollisionObstacle().occupies(source));
            assertTrue(level.getCollisionObstacle().occupies(tank.getDestinationCoordinates()));
            tank.updateMovement(0.4f);
            assertFalse(level.getCollisionObstacle().occupies(source));
            assertTrue(level.getCollisionObstacle().occupies(tank.getCoordinates()));
        }
        assertEquals(new GridPoint2(0, 0), base.getPlayerTank().getCoordinates());
    }

    @Test
    void rejectsInvalidAdditionalPositionsAndCopyingMovingTanks() {
        GameLevel base = new GameLevel(3, 3, new GridPoint2(0, 0), List.of(new GridPoint2(2, 2)));
        for (GridPoint2 position : List.of(new GridPoint2(0, 0), new GridPoint2(2, 2),
                new GridPoint2(-1, 0), new GridPoint2(3, 0))) {
            assertThrows(IllegalArgumentException.class, () -> base.withAdditionalTanks(List.of(position)));
        }
        assertThrows(IllegalArgumentException.class,
                () -> base.withAdditionalTanks(List.of(new GridPoint2(1, 1), new GridPoint2(1, 1))));
        GameLevel existing = base.withAdditionalTanks(List.of(new GridPoint2(1, 1)));
        assertThrows(IllegalArgumentException.class, () -> existing.withAdditionalTanks(List.of(new GridPoint2(1, 1))));
        assertTrue(base.getPlayerTank().tryMove(Direction.RIGHT, base.getCollisionObstacle()));
        assertThrows(IllegalStateException.class, () -> base.withAdditionalTanks(List.of()));
    }

    @Test
    void generatedCollisionObstacleBlocksEveryTree() {
        GameLevel level = new RandomLevelCreationStrategy(20, 30, new Random(12345), new LevelValidator()).create(10, 8);
        Tank tank = level.getTank();
        for (Tree tree : level.getTrees()) {
            GridPoint2 target = tree.getCoordinates();
            assertTrue(level.getCollisionObstacle().occupies(target));
            boolean testedApproach = false;
            for (Direction direction : Direction.values()) {
                GridPoint2 from = new GridPoint2(target).sub(direction.delta());
                if (from.x >= 0 && from.x < 10 && from.y >= 0 && from.y < 8
                        && !level.getCollisionObstacle().occupies(from)) {
                    tank.setCoordinates(from);
                    assertFalse(tank.tryMove(direction, level.getCollisionObstacle()));
                    assertEquals(from, tank.getCoordinates());
                    assertEquals(from, tank.getDestinationCoordinates());
                    testedApproach = true;
                }
            }
            // Interior trees can have no free neighbour; their occupancy is still checked above.
            if (!testedApproach) {
                GridPoint2 from = new GridPoint2(target).sub(Direction.RIGHT.delta());
                Tank probe = new Tank(from);
                assertFalse(probe.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
            }
        }
    }

    @Test
    void blocksAllFourMapBordersAndAllowsFreeMovement() {
        GameLevel level = new RandomLevelCreationStrategy(0, 0, new Random(1), new LevelValidator()).create(10, 8);
        Tank tank = level.getTank();
        List<GridPoint2> edges = List.of(new GridPoint2(0, 7), new GridPoint2(9, 0),
                new GridPoint2(0, 0), new GridPoint2(0, 0));
        Direction[] directions = {Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT};
        for (int i = 0; i < directions.length; i++) {
            tank.setCoordinates(edges.get(i));
            assertFalse(tank.tryMove(directions[i], level.getCollisionObstacle()));
            assertEquals(edges.get(i), tank.getDestinationCoordinates());
        }
        tank.setCoordinates(new GridPoint2(1, 1));
        assertTrue(tank.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
        tank.updateMovement(0.4f);
        assertEquals(new GridPoint2(2, 1), tank.getCoordinates());
    }

    @Test
    void ownsRuntimeObjectsWithoutExposingMutableTreeListOrInputPositions() {
        GridPoint2 tankPosition = new GridPoint2(0, 0);
        GridPoint2 treePosition = new GridPoint2(2, 2);
        List<GridPoint2> input = new ArrayList<>(List.of(treePosition));
        GameLevel level = new GameLevel(3, 3, tankPosition, input);
        tankPosition.set(1, 1);
        treePosition.set(1, 1);
        input.clear();
        assertEquals(new GridPoint2(0, 0), level.getTank().getCoordinates());
        assertEquals(new GridPoint2(2, 2), level.getTrees().get(0).getCoordinates());
        assertTrue(level.getCollisionObstacle().occupies(new GridPoint2(2, 2)));
        assertThrows(UnsupportedOperationException.class, () -> level.getTrees().clear());
        assertThrows(UnsupportedOperationException.class, () -> level.getTrees().add(new Tree(new GridPoint2(1, 1))));
    }
}
