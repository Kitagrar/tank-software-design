package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.List;

/** Runtime objects built from a layout already checked by its creation strategy. */
public class GameLevel {
    private final Tank tank;
    private final List<Tree> trees;
    private final Obstacle collisionObstacle;

    public GameLevel(int width, int height, GridPoint2 tankPosition, List<GridPoint2> treePositions) {
        tank = new Tank(tankPosition);
        List<Tree> createdTrees = new ArrayList<>();
        List<Obstacle> obstacles = new ArrayList<>();
        obstacles.add(new MapBorder(width, height));
        for (GridPoint2 position : treePositions) {
            Tree tree = new Tree(position);
            createdTrees.add(tree);
            obstacles.add(tree);
        }
        trees = List.copyOf(createdTrees);
        collisionObstacle = new CompositeObstacle(obstacles.toArray(new Obstacle[0]));
    }

    public Tank getTank() {
        return tank;
    }

    public List<Tree> getTrees() {
        return trees;
    }

    public Obstacle getCollisionObstacle() {
        return collisionObstacle;
    }
}
