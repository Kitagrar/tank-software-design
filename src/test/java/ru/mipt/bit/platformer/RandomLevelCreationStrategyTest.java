package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.level.LevelValidator;
import ru.mipt.bit.platformer.level.RandomLevelCreationStrategy;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.GameLevel;
import ru.mipt.bit.platformer.model.Tree;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RandomLevelCreationStrategyTest {
    @Test
    void generatesExactTreeCountUniquePositionsAndConnectedFreeArea() {
        for (int seed = 0; seed < 20; seed++) {
            for (int count : new int[]{0, 1, 20, 79}) {
                GameLevel level = strategy(count, 79, seed).create(10, 8);
                assertLayout(level, 10, 8, count);
            }
        }
    }

    @Test
    void supportsSingleCellAndNarrowMaps() {
        assertLayout(strategy(0, 0, 1).create(1, 1), 1, 1, 0);
        assertLayout(strategy(4, 4, 1).create(1, 8), 1, 8, 4);
        assertLayout(strategy(4, 4, 1).create(8, 1), 8, 1, 4);
    }

    @Test
    void reproducesLayoutWithSameSeed() {
        GameLevel first = strategy(20, 30, 12345).create(10, 8);
        GameLevel second = strategy(20, 30, 12345).create(10, 8);
        assertEquals(first.getTank().getCoordinates(), second.getTank().getCoordinates());
        assertEquals(positions(first), positions(second));
    }

    @Test
    void rejectsNegativeCountsAndExceededMaximum() {
        assertThrows(IllegalArgumentException.class, () -> strategy(-1, 20, 1));
        assertThrows(IllegalArgumentException.class, () -> strategy(0, -1, 1));
        assertThrows(IllegalArgumentException.class, () -> strategy(21, 20, 1));
    }

    @Test
    void rejectsImpossibleCapacityAndInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> strategy(80, 100, 1).create(10, 8));
        assertThrows(IllegalArgumentException.class, () -> strategy(81, 100, 1).create(10, 8));
        assertThrows(IllegalArgumentException.class, () -> strategy(0, 100, 1).create(0, 8));
        assertThrows(IllegalArgumentException.class, () -> strategy(0, 100, 1).create(10, -1));
        assertThrows(IllegalArgumentException.class, () -> strategy(0, 100, 1).create(50_000, 50_000));
    }

    @Test
    void alwaysRunsIndependentValidatorBeforeReturningLevel() {
        LevelValidator rejectingValidator = new LevelValidator() {
            @Override
            public void validate(int width, int height, GridPoint2 tankPosition, List<GridPoint2> treePositions) {
                super.validate(width, height, tankPosition, treePositions);
                throw new IllegalArgumentException("Validator rejected layout");
            }
        };
        RandomLevelCreationStrategy strategy = new RandomLevelCreationStrategy(10, 20, new Random(1), rejectingValidator);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> strategy.create(10, 8));
        assertEquals("Validator rejected layout", error.getMessage());
    }

    private RandomLevelCreationStrategy strategy(int count, int maximum, long seed) {
        return new RandomLevelCreationStrategy(count, maximum, new Random(seed), new LevelValidator());
    }

    private List<GridPoint2> positions(GameLevel level) {
        return level.getTrees().stream().map(Tree::getCoordinates).toList();
    }

    private void assertLayout(GameLevel level, int width, int height, int treeCount) {
        GridPoint2 tank = level.getTank().getCoordinates();
        assertInside(tank, width, height);
        List<GridPoint2> trees = positions(level);
        assertEquals(treeCount, trees.size());
        Set<GridPoint2> blocked = new HashSet<>(trees);
        assertEquals(treeCount, blocked.size());
        assertFalse(blocked.contains(tank));
        trees.forEach(tree -> assertInside(tree, width, height));

        // Independent traversal checks the observable result, not the frontier implementation.
        Set<GridPoint2> reached = new HashSet<>();
        ArrayDeque<GridPoint2> queue = new ArrayDeque<>();
        queue.add(tank);
        reached.add(tank);
        while (!queue.isEmpty()) {
            GridPoint2 point = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                GridPoint2 next = new GridPoint2(point).add(direction.delta());
                if (next.x >= 0 && next.x < width && next.y >= 0 && next.y < height
                        && !blocked.contains(next) && reached.add(next)) {
                    queue.addLast(next);
                }
            }
        }
        assertEquals(width * height - treeCount, reached.size());
    }

    private void assertInside(GridPoint2 point, int width, int height) {
        assertTrue(point.x >= 0 && point.x < width && point.y >= 0 && point.y < height);
    }
}
