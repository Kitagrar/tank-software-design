package ru.mipt.bit.platformer.model;

import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Objects;

/** Runtime objects built from a layout already checked by its creation strategy. */
public class GameLevel {
    private final int width;
    private final int height;
    private final Tank playerTank;
    private final List<Tank> tanks;
    private final List<Tree> trees;
    private final Obstacle collisionObstacle;
    private final MapBorder border;
    private final List<Bullet> bullets = new ArrayList<>();
    private final Set<LevelObserver> observers = new LinkedHashSet<>();

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
        tanks = createdTanks;
        List<Tree> createdTrees = new ArrayList<>();
        List<Obstacle> obstacles = new ArrayList<>();
        border = new MapBorder(width, height);
        obstacles.add(border);
        for (GridPoint2 position : treePositions) {
            Tree tree = new Tree(position);
            createdTrees.add(tree);
            obstacles.add(tree);
        }
        trees = List.copyOf(createdTrees);
        // Keep the shared composite stable, but query current tank membership.
        obstacles.add(cell -> tanks.stream().anyMatch(tank -> tank.occupies(cell)));
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
        return List.copyOf(tanks);
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

    public boolean containsTank(Tank tank) {
        return tanks.contains(tank);
    }

    public List<Bullet> getBullets() {
        return List.copyOf(bullets);
    }

    public void addBullet(Bullet bullet) {
        Objects.requireNonNull(bullet);
        if (bullets.contains(bullet)) {
            throw new IllegalArgumentException("Bullet is already in this level");
        }
        // Occupied/outside spawn cells are valid shots, resolved at the next tick.
        bullets.add(bullet);
        notifyAdded(bullet);
    }

    public void removeBullet(Bullet bullet) {
        if (bullets.remove(bullet)) {
            notifyRemoved(bullet);
        }
    }

    public void removeTank(Tank tank) {
        Objects.requireNonNull(tank);
        if (tank.isAlive()) {
            throw new IllegalArgumentException("Only dead tanks may be removed");
        }
        if (tanks.remove(tank)) {
            notifyRemoved(tank);
        }
    }

    /** Replay initial entities through the same observer API as runtime additions. */
    public void addObserver(LevelObserver observer) {
        Objects.requireNonNull(observer);
        if (observers.add(observer)) {
            List<LevelEntity> initial = new ArrayList<>(tanks);
            initial.addAll(trees);
            initial.addAll(bullets);
            for (LevelEntity entity : initial) {
                if (!observers.contains(observer)) {
                    break;
                }
                observer.onObjectAdded(entity);
            }
        }
    }

    public void removeObserver(LevelObserver observer) {
        observers.remove(observer);
    }

    private void notifyAdded(LevelEntity entity) {
        for (LevelObserver observer : List.copyOf(observers)) {
            observer.onObjectAdded(entity);
        }
    }

    private void notifyRemoved(LevelEntity entity) {
        for (LevelObserver observer : List.copyOf(observers)) {
            observer.onObjectRemoved(entity);
        }
    }

    public void tick(float deltaTime) {
        if (!Float.isFinite(deltaTime) || deltaTime < 0) {
            throw new IllegalArgumentException("Delta time must be finite and non-negative");
        }
        for (Tank tank : List.copyOf(tanks)) {
            if (!tank.isAlive()) {
                removeTank(tank);
            }
        }
        // Check contacts before movement can release a tank's source cell.
        resolveCurrentContacts();
        for (Tank tank : List.copyOf(tanks)) {
            tank.updateMovement(deltaTime);
        }
        resolveCurrentContacts();

        // Advance all projectiles to the next cell event together. No projectile
        // runs its entire large delta ahead of another projectile's first step.
        double remaining = deltaTime;
        while (remaining > 0 && !bullets.isEmpty()) {
            List<Bullet> snapshot = List.copyOf(bullets);
            double untilStep = snapshot.stream().mapToDouble(Bullet::timeUntilStep).min().orElseThrow();
            double elapsed = Math.min(remaining, untilStep);
            for (Bullet bullet : snapshot) {
                bullet.elapse(elapsed);
            }
            remaining -= elapsed;
            for (Bullet bullet : snapshot) {
                if (bullets.contains(bullet) && bullet.timeUntilStep() == 0) {
                    bullet.step();
                    resolveBulletCollision(bullet);
                }
            }
        }
    }

    private void resolveCurrentContacts() {
        for (Bullet bullet : List.copyOf(bullets)) {
            if (bullets.contains(bullet)) {
                resolveBulletCollision(bullet);
            }
        }
    }

    private void resolveBulletCollision(Bullet bullet) {
        GridPoint2 cell = bullet.getCoordinates();
        if (border.occupies(cell) || trees.stream().anyMatch(tree -> tree.occupies(cell))) {
            removeBullet(bullet);
            return;
        }
        for (Tank tank : List.copyOf(tanks)) {
            if (bullet.canHit(tank) && tank.occupies(cell)) {
                tank.takeDamage(bullet.getDamage());
                removeBullet(bullet);
                if (!tank.isAlive()) {
                    removeTank(tank);
                }
                return;
            }
        }
        List<Bullet> contacts = bullets.stream()
                .filter(other -> other.getCoordinates().equals(cell)).toList();
        if (contacts.size() > 1) {
            // Remove the whole contact group before publishing any removal.
            bullets.removeAll(contacts);
            for (Bullet contact : contacts) {
                notifyRemoved(contact);
            }
        }
    }
}
