package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.GameLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class RandomLevelCreationStrategy implements LevelCreationStrategy {
    private final int treeCount;
    private final Random random;
    private final LevelValidator validator;

    public RandomLevelCreationStrategy(int treeCount, int maxTreeCount, Random random, LevelValidator validator) {
        if (maxTreeCount < 0 || treeCount < 0 || treeCount > maxTreeCount) {
            throw new IllegalArgumentException("Require 0 <= treeCount <= maxTreeCount, got "
                    + treeCount + " and " + maxTreeCount);
        }
        this.treeCount = treeCount;
        this.random = Objects.requireNonNull(random);
        this.validator = Objects.requireNonNull(validator);
    }

    @Override
    public GameLevel create(int width, int height) {
        int totalCells = validator.validateDimensions(width, height);
        if (treeCount >= totalCells) {
            throw new IllegalArgumentException("Tree count must leave at least one cell for Tank");
        }
        int start = random.nextInt(totalCells);
        GridPoint2 tankPosition = new GridPoint2(start % width, start / width);
        boolean[] free = new boolean[totalCells];
        boolean[] inFrontier = new boolean[totalCells];
        List<Integer> frontier = new ArrayList<>();
        free[start] = true;
        addNeighbours(start, width, height, free, inFrontier, frontier);

        // Every iteration adds one adjacent cell; no retries or intermediate BFS.
        int freeCellCount = totalCells - treeCount;
        for (int count = 1; count < freeCellCount; count++) {
            int cell = frontier.remove(random.nextInt(frontier.size()));
            inFrontier[cell] = false;
            free[cell] = true;
            addNeighbours(cell, width, height, free, inFrontier, frontier);
        }

        List<GridPoint2> treePositions = new ArrayList<>();
        for (int cell = 0; cell < totalCells; cell++) {
            if (!free[cell]) {
                treePositions.add(new GridPoint2(cell % width, cell / width));
            }
        }
        validator.validate(width, height, tankPosition, treePositions);
        return new GameLevel(width, height, tankPosition, treePositions);
    }

    private void addNeighbours(int cell, int width, int height, boolean[] free,
                               boolean[] inFrontier, List<Integer> frontier) {
        for (Direction direction : Direction.values()) {
            GridPoint2 delta = direction.delta();
            int x = cell % width + delta.x;
            int y = cell / width + delta.y;
            if (x >= 0 && x < width && y >= 0 && y < height) {
                int neighbour = y * width + x;
                if (!free[neighbour] && !inFrontier[neighbour]) {
                    frontier.add(neighbour);
                    inFrontier[neighbour] = true;
                }
            }
        }
    }
}
