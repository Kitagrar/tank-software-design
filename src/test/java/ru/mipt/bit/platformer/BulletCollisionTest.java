package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mipt.bit.platformer.command.ShootCommand;
import ru.mipt.bit.platformer.command.Command;
import ru.mipt.bit.platformer.control.RandomAiControlStrategy;
import ru.mipt.bit.platformer.control.TankController;
import ru.mipt.bit.platformer.model.*;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BulletCollisionTest {
    @Test
    void shooterMovingIntoItsNewBulletDoesNotDamageItself() {
        GameLevel level = new GameLevel(8, 3, new GridPoint2(1, 1), List.of());
        Tank shooter = level.getPlayerTank();
        new ShootCommand(shooter, level).execute();
        level.tick(1f / 60);
        assertEquals(new GridPoint2(2, 1), level.getBullets().get(0).getCoordinates());
        assertTrue(shooter.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
        level.tick(1f / 60);
        assertEquals(Tank.DEFAULT_HEALTH, shooter.getHealth());
        assertEquals(1, level.getBullets().size());
    }

    @Test
    void isolatedRandomAiCannotKillItselfOverManyFrames() {
        GameLevel level = new GameLevel(10, 8, new GridPoint2(0, 0), List.of())
                .withAdditionalTanks(List.of(new GridPoint2(4, 3)));
        Tank ai = level.getTanks().get(1);
        TankController controller = new TankController(ai, new RandomAiControlStrategy(new Random(67890)), level);
        for (int frame = 0; frame < 6000; frame++) {
            controller.createCommand().ifPresent(Command::execute);
            level.tick(1f / 60);
            assertEquals(Tank.DEFAULT_HEALTH, ai.getHealth(), "AI health at frame " + frame);
            assertTrue(level.containsTank(ai));
        }
    }

    @Test
    void movementWithoutShotsNeverRemovesTanksOrChangesHealth() {
        GameLevel level = new GameLevel(10, 8, new GridPoint2(0, 0), List.of())
                .withAdditionalTanks(List.of(new GridPoint2(4, 3), new GridPoint2(6, 6)));
        List<Tank> initial = level.getTanks();
        Random random = new Random(12345);
        for (int frame = 0; frame < 6000; frame++) {
            for (Tank tank : initial) {
                tank.tryMove(Direction.values()[random.nextInt(4)], level.getCollisionObstacle());
            }
            level.tick(1f / 60);
        }
        assertEquals(initial, level.getTanks());
        for (Tank tank : initial) {
            assertEquals(Tank.DEFAULT_HEALTH, tank.getHealth());
        }
    }

    @Test
    void aiShotStillDamagesAnotherAi() {
        GameLevel level = new GameLevel(8, 3, new GridPoint2(0, 0), List.of())
                .withAdditionalTanks(List.of(new GridPoint2(2, 1), new GridPoint2(3, 1)));
        Tank shooter = level.getTanks().get(1);
        Tank target = level.getTanks().get(2);
        new ShootCommand(shooter, level).execute();
        level.tick(0);
        assertEquals(Tank.DEFAULT_HEALTH, shooter.getHealth());
        assertEquals(Tank.DEFAULT_HEALTH - Bullet.DEFAULT_DAMAGE, target.getHealth());
        assertTrue(level.getBullets().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 1})
    void adjacentTankIsHitBeforeAnyBulletMovement(double delta) {
        GameLevel level = new GameLevel(6, 3, new GridPoint2(1, 1), List.of())
                .withAdditionalTanks(List.of(new GridPoint2(2, 1)));
        Tank target = level.getTanks().get(1);
        int health = target.getHealth();
        new ShootCommand(level.getPlayerTank(), level).execute();
        Bullet bullet = level.getBullets().get(0);
        assertEquals(new GridPoint2(2, 1), bullet.getCoordinates());
        level.tick((float) delta);
        assertEquals(health - bullet.getDamage(), target.getHealth());
        assertTrue(level.containsTank(target));
        assertTrue(level.getBullets().isEmpty());
        level.tick(1);
        assertEquals(health - bullet.getDamage(), target.getHealth());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, 1})
    void adjacentLethalShotRemovesTarget(double delta) {
        GameLevel level = new GameLevel(6, 3, new GridPoint2(1, 1), List.of())
                .withAdditionalTanks(List.of(new GridPoint2(2, 1)));
        Tank target = level.getTanks().get(1);
        target.takeDamage(target.getHealth() - Bullet.DEFAULT_DAMAGE);
        new ShootCommand(level.getPlayerTank(), level).execute();
        level.tick((float) delta);
        assertFalse(target.isAlive());
        assertFalse(level.containsTank(target));
        assertTrue(level.getBullets().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4})
    void treeStopsBulletAtSpawnOrIntermediateCellEvenWithLargeDelta(int treeX) {
        GameLevel level = new GameLevel(20, 3, new GridPoint2(0, 0), List.of(new GridPoint2(treeX, 1)));
        Tree tree = level.getTrees().get(0);
        Bullet bullet = new Bullet(new GridPoint2(1, 1), Direction.RIGHT);
        level.addBullet(bullet);
        level.tick(1);
        assertTrue(level.getBullets().isEmpty());
        assertEquals(new GridPoint2(treeX, 1), bullet.getCoordinates());
        assertEquals(List.of(tree), level.getTrees());
        assertTrue(level.getCollisionObstacle().occupies(tree.getCoordinates()));
    }

    @Test
    void largeDeltaDoesNotTunnelThroughTankAndUsesConfiguredDamage() {
        GameLevel level = new GameLevel(20, 3, new GridPoint2(4, 1), List.of());
        Tank target = level.getPlayerTank();
        int health = target.getHealth();
        Bullet bullet = new Bullet(new GridPoint2(1, 1), Direction.RIGHT, 13, 0.1f);
        level.addBullet(bullet);
        level.tick(1);
        assertEquals(health - 13, target.getHealth());
        assertEquals(new GridPoint2(4, 1), bullet.getCoordinates());
        assertTrue(level.getBullets().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void movingTankIsHitAtSourceAndDestinationBeforeSourceCanBeReleased(int bulletX) {
        GameLevel level = new GameLevel(6, 3, new GridPoint2(1, 1), List.of());
        Tank target = level.getPlayerTank();
        assertTrue(target.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
        Bullet bullet = new Bullet(new GridPoint2(bulletX, 1), Direction.UP, 7, 0.1f);
        level.addBullet(bullet);
        level.tick(0.4f);
        assertEquals(Tank.DEFAULT_HEALTH - 7, target.getHealth());
        assertTrue(level.getBullets().isEmpty());
        assertEquals(new GridPoint2(2, 1), target.getCoordinates());
    }

    @Test
    void bulletDoesNotBlockTankMovementButContactDealsDamage() {
        GameLevel level = new GameLevel(6, 3, new GridPoint2(1, 1), List.of());
        Tank target = level.getPlayerTank();
        Bullet bullet = new Bullet(new GridPoint2(2, 1), Direction.RIGHT);
        level.addBullet(bullet);
        assertFalse(level.getCollisionObstacle().occupies(bullet.getCoordinates()));
        assertTrue(target.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
        level.tick(0);
        assertEquals(Tank.DEFAULT_HEALTH - bullet.getDamage(), target.getHealth());
        assertTrue(level.getBullets().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void flyingBulletHitsEitherCellOfTankStillInMotion(int targetX) {
        GameLevel level = new GameLevel(6, 5, new GridPoint2(2, 2), List.of());
        Tank target = level.getPlayerTank();
        assertTrue(target.tryMove(Direction.RIGHT, level.getCollisionObstacle()));
        Bullet bullet = new Bullet(new GridPoint2(targetX, 1), Direction.UP, 9, 0.1f);
        level.addBullet(bullet);
        level.tick(0.1f);
        assertFalse(target.isReadyToMove());
        assertEquals(Tank.DEFAULT_HEALTH - 9, target.getHealth());
        assertTrue(level.getBullets().isEmpty());
        assertEquals(new GridPoint2(targetX, 2), bullet.getCoordinates());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sameCellBulletsBothDisappearBeforeMovingRegardlessOfRegistration(boolean reverse) {
        GameLevel level = emptyLevel();
        Bullet a = new Bullet(new GridPoint2(3, 1), Direction.RIGHT);
        Bullet b = new Bullet(new GridPoint2(3, 1), Direction.LEFT);
        addPair(level, a, b, reverse);
        level.tick(0);
        assertTrue(level.getBullets().isEmpty());
        assertEquals(new GridPoint2(3, 1), a.getCoordinates());
        assertEquals(new GridPoint2(3, 1), b.getCoordinates());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void adjacentOpposingBulletsBothDisappearAndRemovedSnapshotEntryCannotHitTank(boolean reverse) {
        GameLevel level = new GameLevel(20, 3, new GridPoint2(2, 1), List.of());
        Bullet a = new Bullet(new GridPoint2(3, 1), Direction.RIGHT);
        Bullet b = new Bullet(new GridPoint2(4, 1), Direction.LEFT);
        addPair(level, a, b, reverse);
        level.tick(1);
        assertTrue(level.getBullets().isEmpty());
        assertEquals(Tank.DEFAULT_HEALTH, level.getPlayerTank().getHealth());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void bulletsEnteringSameTargetInOneStepBothDisappearEvenWithLargeDelta(boolean reverse) {
        GameLevel level = emptyLevel();
        Bullet a = new Bullet(new GridPoint2(2, 1), Direction.RIGHT);
        Bullet b = new Bullet(new GridPoint2(4, 1), Direction.LEFT);
        addPair(level, a, b, reverse);
        level.tick(1);
        assertTrue(level.getBullets().isEmpty());
        assertEquals(new GridPoint2(3, 1), a.getCoordinates());
        assertEquals(new GridPoint2(3, 1), b.getCoordinates());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void largeDeltaCannotPassAnIntermediateBullet(boolean reverse) {
        GameLevel level = emptyLevel();
        Bullet fast = new Bullet(new GridPoint2(1, 1), Direction.RIGHT, 5, 0.1f);
        Bullet slow = new Bullet(new GridPoint2(4, 1), Direction.RIGHT, 5, 1f);
        addPair(level, fast, slow, reverse);
        level.tick(0.5f);
        assertTrue(level.getBullets().isEmpty());
        assertEquals(new GridPoint2(4, 1), fast.getCoordinates());
        assertEquals(new GridPoint2(4, 1), slow.getCoordinates());
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void boundaryRemovesBulletBothAtSpawnAndAfterFlight(Direction direction) {
        GameLevel level = new GameLevel(5, 5, new GridPoint2(0, 0), List.of());
        GridPoint2 edge = switch (direction) {
            case UP -> new GridPoint2(2, 4);
            case RIGHT -> new GridPoint2(4, 2);
            case DOWN -> new GridPoint2(2, 0);
            case LEFT -> new GridPoint2(0, 2);
        };
        Bullet outside = new Bullet(new GridPoint2(edge).add(direction.delta()), direction);
        level.addBullet(outside);
        level.tick(0);
        assertTrue(level.getBullets().isEmpty());
        Bullet atEdge = new Bullet(edge, direction);
        level.addBullet(atEdge);
        level.tick(0.1f);
        assertTrue(level.getBullets().isEmpty());
        assertEquals(outside.getCoordinates(), atEdge.getCoordinates());
    }

    @Test
    void lethalMovingTankHitReleasesBothCellsThroughSameObstacleReference() {
        GameLevel level = new GameLevel(6, 3, new GridPoint2(1, 1), List.of());
        Tank tank = level.getPlayerTank();
        Obstacle original = level.getCollisionObstacle();
        assertTrue(tank.tryMove(Direction.RIGHT, original));
        GridPoint2 source = tank.getCoordinates();
        GridPoint2 destination = tank.getDestinationCoordinates();
        assertTrue(original.occupies(source));
        assertTrue(original.occupies(destination));
        level.addBullet(new Bullet(destination, Direction.UP, tank.getHealth(), 0.1f));
        level.tick(0);
        assertSame(original, level.getCollisionObstacle());
        assertFalse(original.occupies(source));
        assertFalse(original.occupies(destination));
        assertTrue(level.getTanks().isEmpty());
        assertSame(tank, level.getPlayerTank());
    }

    private GameLevel emptyLevel() {
        return new GameLevel(20, 3, new GridPoint2(0, 0), List.of());
    }

    private void addPair(GameLevel level, Bullet a, Bullet b, boolean reverse) {
        level.addBullet(reverse ? b : a);
        level.addBullet(reverse ? a : b);
    }
}
