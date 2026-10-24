package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/** Runtime objects built from a layout already checked by its creation strategy. */
public class GameLevel {
    private final int width;
    private final int height;
    private final Tank playerTank;
    private final List<Tank> tanks;
    private final List<Tree> trees;
    private final Obstacle collisionObstacle;

    public GameLevel(int width, int height, GridPoint2 tankPosition, List<GridPoint2> treePositions) {
        this(width, height, tankPosition, treePositions, List.of());
    }

    private GameLevel(int width, int height, GridPoint2 playerPosition, List<GridPoint2> treePositions,
                      List<GridPoint2> aiPositions) {
        this.width = width;
        this.height = height;
        playerTank = new Tank(playerPosition);
        List<Tank> createdTanks = new ArrayList<>();
        createdTanks.add(playerTank);
        for (GridPoint2 position : aiPositions) {
            createdTanks.add(new Tank(position));
        }
        tanks = List.copyOf(createdTanks);
        List<Tree> createdTrees = new ArrayList<>();
        List<Obstacle> obstacles = new ArrayList<>();
        obstacles.add(new MapBorder(width, height));
        for (GridPoint2 position : treePositions) {
            Tree tree = new Tree(position);
            createdTrees.add(tree);
            obstacles.add(tree);
        }
        trees = List.copyOf(createdTrees);
        obstacles.addAll(tanks);
        collisionObstacle = new CompositeObstacle(obstacles.toArray(new Obstacle[0]));
    }

    /** Initialization only: copies stationary entities into a new aggregate with extra tanks. */
    public GameLevel withAdditionalTanks(List<GridPoint2> positions) {
        for (Tank tank : tanks) {
            if (!tank.isReadyToMove()) {
                throw new IllegalStateException("Additional tanks must be configured before movement");
            }
        }
        Set<GridPoint2> selected = new HashSet<>();
        for (GridPoint2 position : positions) {
            if (position == null || collisionObstacle.occupies(position) || !selected.add(new GridPoint2(position))) {
                throw new IllegalArgumentException("Invalid or occupied tank spawn: " + position);
            }
        }
        List<GridPoint2> aiPositions = new ArrayList<>();
        for (Tank tank : tanks) {
            if (tank != playerTank) {
                aiPositions.add(tank.getCoordinates());
            }
        }
        aiPositions.addAll(positions);
        return new GameLevel(width, height, playerTank.getCoordinates(),
                trees.stream().map(Tree::getCoordinates).toList(), aiPositions);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Tank getPlayerTank() {
        return playerTank;
    }

    public List<Tank> getTanks() {
        return tanks;
    }

    /** Compatibility with the single-player HW4 API. */
    public Tank getTank() {
        return getPlayerTank();
    }

    public List<Tree> getTrees() {
        return trees;
    }

    public Obstacle getCollisionObstacle() {
        return collisionObstacle;
    }
}
