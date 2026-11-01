package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.model.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LevelObserverTest {
    @Test
    void subscriptionReplaysEveryExistingEntityOnceAndDoesNotReplayOnDuplicateSubscribe() {
        GameLevel level = level();
        Bullet bullet = new Bullet(new GridPoint2(3, 1), Direction.RIGHT);
        level.addBullet(bullet);
        RecordingObserver observer = new RecordingObserver();
        level.addObserver(observer);
        List<LevelEntity> expected = new ArrayList<>(level.getTanks());
        expected.addAll(level.getTrees());
        expected.add(bullet);
        assertEquals(expected, observer.added);
        assertTrue(observer.removed.isEmpty());
        level.addObserver(observer);
        assertEquals(expected, observer.added);
    }

    @Test
    void runtimeAddRemoveEventsAreUniqueAndSeeUpdatedLogicalState() {
        GameLevel level = level();
        RecordingObserver observer = new RecordingObserver() {
            @Override
            public void onObjectAdded(LevelEntity entity) {
                if (entity instanceof Bullet bullet) {
                    assertTrue(level.getBullets().contains(bullet));
                }
                super.onObjectAdded(entity);
            }

            @Override
            public void onObjectRemoved(LevelEntity entity) {
                if (entity instanceof Bullet bullet) {
                    assertFalse(level.getBullets().contains(bullet));
                } else if (entity instanceof Tank tank) {
                    assertFalse(level.containsTank(tank));
                    assertFalse(level.getCollisionObstacle().occupies(tank.getCoordinates()));
                }
                super.onObjectRemoved(entity);
            }
        };
        level.addObserver(observer);
        observer.added.clear();
        Bullet bullet = new Bullet(new GridPoint2(3, 1), Direction.RIGHT);
        level.addBullet(bullet);
        assertEquals(List.of(bullet), observer.added);
        assertThrows(IllegalArgumentException.class, () -> level.addBullet(bullet));
        assertThrows(NullPointerException.class, () -> level.addBullet(null));
        assertEquals(List.of(bullet), observer.added);
        level.removeBullet(bullet);
        level.removeBullet(bullet);
        assertEquals(List.of(bullet), observer.removed);
        Tank target = level.getTanks().get(1);
        Bullet lethal = new Bullet(target.getCoordinates(), Direction.UP, target.getHealth(), 0.1f);
        level.addBullet(lethal);
        level.tick(0);
        assertEquals(List.of(bullet, lethal, target), observer.removed);
        level.removeTank(target);
        assertEquals(List.of(bullet, lethal, target), observer.removed);
    }

    @Test
    void unsubscribeStopsFutureNotificationsAndLateSubscriberGetsOnlyCurrentObjects() {
        GameLevel level = level();
        RecordingObserver old = new RecordingObserver();
        level.addObserver(old);
        old.added.clear();
        level.removeObserver(old);
        Bullet bullet = new Bullet(new GridPoint2(3, 1), Direction.RIGHT);
        level.addBullet(bullet);
        level.removeBullet(bullet);
        assertTrue(old.added.isEmpty());
        assertTrue(old.removed.isEmpty());
        RecordingObserver late = new RecordingObserver();
        level.addObserver(late);
        assertFalse(late.added.contains(bullet));
        assertEquals(level.getTanks().size() + level.getTrees().size(), late.added.size());
    }

    @Test
    void selfUnsubscribeDuringNotificationDoesNotBreakOtherObservers() {
        GameLevel level = level();
        RecordingObserver selfRemoving = new RecordingObserver() {
            @Override
            public void onObjectAdded(LevelEntity entity) {
                super.onObjectAdded(entity);
                if (entity instanceof Bullet) {
                    level.removeObserver(this);
                }
            }
        };
        RecordingObserver other = new RecordingObserver();
        level.addObserver(selfRemoving);
        level.addObserver(other);
        selfRemoving.added.clear();
        other.added.clear();
        Bullet bullet = new Bullet(new GridPoint2(3, 1), Direction.RIGHT);
        level.addBullet(bullet);
        level.removeBullet(bullet);
        assertEquals(List.of(bullet), selfRemoving.added);
        assertTrue(selfRemoving.removed.isEmpty());
        assertEquals(List.of(bullet), other.added);
        assertEquals(List.of(bullet), other.removed);
    }

    @Test
    void bothCollidingBulletsAreAbsentBeforeEitherRemovalCallback() {
        GameLevel level = level();
        Bullet a = new Bullet(new GridPoint2(3, 1), Direction.RIGHT);
        Bullet b = new Bullet(new GridPoint2(3, 1), Direction.LEFT);
        level.addBullet(a);
        level.addBullet(b);
        RecordingObserver observer = new RecordingObserver() {
            @Override
            public void onObjectRemoved(LevelEntity entity) {
                assertTrue(level.getBullets().isEmpty());
                super.onObjectRemoved(entity);
            }
        };
        level.addObserver(observer);
        level.tick(1);
        assertEquals(List.of(a, b), observer.removed);
    }

    @Test
    void collectionsAreImmutableSnapshotsAndLiveTankCannotBeRemoved() {
        GameLevel level = level();
        List<Tank> originalTanks = level.getTanks();
        List<Bullet> originalBullets = level.getBullets();
        assertThrows(UnsupportedOperationException.class, () -> originalTanks.clear());
        assertThrows(UnsupportedOperationException.class, () -> level.getTrees().clear());
        assertThrows(UnsupportedOperationException.class, () -> originalBullets.add(
                new Bullet(new GridPoint2(3, 1), Direction.RIGHT)));
        assertThrows(IllegalArgumentException.class, () -> level.removeTank(level.getPlayerTank()));
        Tank target = level.getTanks().get(1);
        target.takeDamage(target.getHealth());
        level.removeTank(target);
        assertTrue(originalTanks.contains(target));
        assertFalse(level.getTanks().contains(target));
        level.addBullet(new Bullet(new GridPoint2(3, 1), Direction.RIGHT));
        assertTrue(originalBullets.isEmpty());
        assertEquals(1, level.getBullets().size());
    }

    @Test
    void multipleRemovalsDuringTickAreSafeAndEachEventIsPublishedOnce() {
        GameLevel level = level();
        RecordingObserver observer = new RecordingObserver();
        level.addObserver(observer);
        List<Tank> tanks = level.getTanks();
        for (Tank tank : tanks) {
            level.addBullet(new Bullet(tank.getCoordinates(), Direction.UP, tank.getHealth(), 0.1f));
        }
        level.tick(1);
        assertTrue(level.getTanks().isEmpty());
        assertTrue(level.getBullets().isEmpty());
        assertEquals(4, observer.removed.size());
        for (Tank tank : tanks) {
            assertEquals(1, observer.removed.stream().filter(entity -> entity == tank).count());
        }
        level.tick(1);
        assertEquals(4, observer.removed.size());
    }

    private GameLevel level() {
        return new GameLevel(6, 3, new GridPoint2(0, 0), List.of(new GridPoint2(5, 2)))
                .withAdditionalTanks(List.of(new GridPoint2(2, 1)));
    }

    private static class RecordingObserver implements LevelObserver {
        final List<LevelEntity> added = new ArrayList<>();
        final List<LevelEntity> removed = new ArrayList<>();

        @Override
        public void onObjectAdded(LevelEntity entity) {
            added.add(entity);
        }

        @Override
        public void onObjectRemoved(LevelEntity entity) {
            removed.add(entity);
        }
    }
}
