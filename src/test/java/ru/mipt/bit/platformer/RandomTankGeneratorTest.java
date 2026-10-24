package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.level.*;
import ru.mipt.bit.platformer.model.GameLevel;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RandomTankGeneratorTest {
    @Test
    void worksWithBothHw4StrategiesAndExcludesAllOccupiedCells() {
        LevelCreationStrategy[] strategies = {
                new FileLevelCreationStrategy("levels/level1.txt", new LevelValidator()),
                new RandomLevelCreationStrategy(20, 30, new Random(1), new LevelValidator())
        };
        for (LevelCreationStrategy strategy : strategies) {
            GameLevel base = strategy.create(10, 8).withAdditionalTanks(List.of());
            for (int seed = 0; seed < 10; seed++) {
                List<GridPoint2> positions = new RandomTankGenerator(new Random(seed)).generatePositions(base, 5);
                assertEquals(5, positions.size());
                assertEquals(5, new HashSet<>(positions).size());
                for (GridPoint2 position : positions) {
                    assertTrue(position.x >= 0 && position.x < 10 && position.y >= 0 && position.y < 8);
                    assertFalse(base.getCollisionObstacle().occupies(position));
                }
            }
        }
    }

    @Test
    void sameSeedReproducesSpawnPositions() {
        GameLevel level = new GameLevel(10, 8, new GridPoint2(0, 0), List.of());
        assertEquals(new RandomTankGenerator(new Random(12345)).generatePositions(level, 10),
                new RandomTankGenerator(new Random(12345)).generatePositions(level, 10));
    }

    @Test
    void supportsZeroAndFullCapacityAndRejectsImpossibleCounts() {
        GameLevel level = new GameLevel(3, 1, new GridPoint2(0, 0), List.of(new GridPoint2(2, 0)));
        RandomTankGenerator generator = new RandomTankGenerator(new Random(1));
        assertTrue(generator.generatePositions(level, 0).isEmpty());
        assertEquals(List.of(new GridPoint2(1, 0)), generator.generatePositions(level, 1));
        assertThrows(IllegalArgumentException.class, () -> generator.generatePositions(level, -1));
        assertThrows(IllegalArgumentException.class, () -> generator.generatePositions(level, 2));
        GameLevel singleCell = new GameLevel(1, 1, new GridPoint2(0, 0), List.of());
        assertTrue(generator.generatePositions(singleCell, 0).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> generator.generatePositions(singleCell, 1));
    }

    @Test
    void excludesExistingAdditionalTanksAndBothMovingEndpoints() {
        GameLevel level = new GameLevel(4, 1, new GridPoint2(0, 0), List.of())
                .withAdditionalTanks(List.of(new GridPoint2(3, 0)));
        assertTrue(level.getPlayerTank().tryMove(ru.mipt.bit.platformer.model.Direction.RIGHT, level.getCollisionObstacle()));
        assertEquals(List.of(new GridPoint2(2, 0)), new RandomTankGenerator(new Random(1)).generatePositions(level, 1));
    }
}
