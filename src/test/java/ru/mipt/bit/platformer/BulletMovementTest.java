package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.mipt.bit.platformer.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BulletMovementTest {
    @ParameterizedTest
    @EnumSource(Direction.class)
    void accumulatesTimeAndMovesInFixedDirection(Direction direction) {
        GameLevel level = level();
        Bullet bullet = new Bullet(new GridPoint2(10, 10), direction, 7, 0.1f);
        level.addBullet(bullet);
        level.tick(0.04f);
        assertEquals(new GridPoint2(10, 10), bullet.getCoordinates());
        level.tick(0.06f);
        assertEquals(new GridPoint2(10, 10).add(direction.delta()), bullet.getCoordinates());
        level.tick(0.25f);
        assertEquals(new GridPoint2(10, 10).add(direction.delta().x * 3, direction.delta().y * 3), bullet.getCoordinates());
        level.tick(0.05f);
        assertEquals(new GridPoint2(10, 10).add(direction.delta().x * 4, direction.delta().y * 4), bullet.getCoordinates());
        assertEquals(direction, bullet.getDirection());
        assertEquals(7, bullet.getDamage());
    }

    @Test
    void largeTickAndSplitTicksHaveSameResultOnFreePath() {
        GameLevel large = level();
        GameLevel split = level();
        Bullet a = new Bullet(new GridPoint2(2, 2), Direction.RIGHT);
        Bullet b = new Bullet(new GridPoint2(2, 2), Direction.RIGHT);
        large.addBullet(a);
        split.addBullet(b);
        large.tick(0.75f);
        for (int i = 0; i < 15; i++) {
            split.tick(0.05f);
        }
        assertEquals(new GridPoint2(9, 2), a.getCoordinates());
        assertEquals(a.getCoordinates(), b.getCoordinates());
        large.tick(0.05f);
        split.tick(0.05f);
        assertEquals(new GridPoint2(10, 2), a.getCoordinates());
        assertEquals(a.getCoordinates(), b.getCoordinates());
    }

    @Test
    void coordinateOwnershipAndInvalidSettings() {
        GridPoint2 spawn = new GridPoint2(2, 2);
        Bullet bullet = new Bullet(spawn, Direction.RIGHT);
        spawn.set(9, 9);
        bullet.getCoordinates().set(8, 8);
        assertEquals(new GridPoint2(2, 2), bullet.getCoordinates());
        assertThrows(IllegalArgumentException.class, () -> new Bullet(spawn, Direction.UP, 0, 0.1f));
        assertThrows(IllegalArgumentException.class, () -> new Bullet(spawn, Direction.UP, -1, 0.1f));
        for (float duration : new float[]{0, -1, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new Bullet(spawn, Direction.UP, 1, duration));
        }
        GameLevel level = level();
        level.addBullet(bullet);
        for (float delta : new float[]{-1, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> level.tick(delta));
            assertEquals(new GridPoint2(2, 2), bullet.getCoordinates());
        }
    }

    private GameLevel level() {
        return new GameLevel(30, 30, new GridPoint2(0, 0), List.of());
    }
}
