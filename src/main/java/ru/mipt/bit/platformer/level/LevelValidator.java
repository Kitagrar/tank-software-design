package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.Direction;

import java.util.ArrayDeque;
import java.util.List;

public class LevelValidator {
    public int validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0 || (long) width * height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Level dimensions must be positive with an int-sized area");
        }
        return width * height;
    }

    public void validate(int width, int height, GridPoint2 tankPosition, List<GridPoint2> treePositions) {
        int totalCells = validateDimensions(width, height);
        validatePosition(tankPosition, width, height, "Tank");
        boolean[] blocked = new boolean[totalCells];
        for (GridPoint2 tree : treePositions) {
            validatePosition(tree, width, height, "Tree");
            int cell = tree.y * width + tree.x;
            if (blocked[cell]) {
                throw new IllegalArgumentException("Duplicate Tree position: " + tree);
            }
            blocked[cell] = true;
        }
        int start = tankPosition.y * width + tankPosition.x;
        if (blocked[start]) {
            throw new IllegalArgumentException("Tank overlaps a Tree: " + tankPosition);
        }

        boolean[] visited = new boolean[totalCells];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        visited[start] = true;
        int reachableFreeCells = 0;
        while (!queue.isEmpty()) {
            int cell = queue.removeFirst();
            reachableFreeCells++;
            for (Direction direction : Direction.values()) {
                GridPoint2 delta = direction.delta();
                int x = cell % width + delta.x;
                int y = cell / width + delta.y;
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    int neighbour = y * width + x;
                    if (!blocked[neighbour] && !visited[neighbour]) {
                        visited[neighbour] = true;
                        queue.addLast(neighbour);
                    }
                }
            }
        }
        int allFreeCells = totalCells - treePositions.size();
        if (reachableFreeCells != allFreeCells) {
            throw new IllegalArgumentException("Disconnected free area: reachable " + reachableFreeCells
                    + " of " + allFreeCells + " cells");
        }
    }

    private void validatePosition(GridPoint2 position, int width, int height, String entity) {
        if (position == null || position.x < 0 || position.x >= width
                || position.y < 0 || position.y >= height) {
            throw new IllegalArgumentException(entity + " position outside level: " + position);
        }
    }
}
