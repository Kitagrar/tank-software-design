package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mipt.bit.platformer.model.*;

import static org.junit.jupiter.api.Assertions.*;

class TankShootingTest {
    @ParameterizedTest
    @ValueSource(ints = {100, 70, 15})
    void lightAndMediumCanShootOnlyWhenStationary(int health) {
        Tank tank = new Tank(new GridPoint2(1, 1));
        if (health < tank.getHealth()) {
            tank.takeDamage(tank.getHealth() - health);
        }
        assertTrue(tank.canShoot());
        assertEquals(new GridPoint2(2, 1), tank.shoot().getCoordinates());
        assertTrue(tank.tryMove(Direction.RIGHT, cell -> false));
        assertFalse(tank.canShoot());
        assertThrows(IllegalStateException.class, tank::shoot);
        tank.updateMovement(health > 70 ? 0.4f : 0.8f);
        assertTrue(tank.canShoot());
        assertEquals(new GridPoint2(3, 1), tank.shoot().getCoordinates());
    }

    @Test
    void startsFacingRightAndBlockedMoveDoesNotTurn() {
        Tank tank = new Tank(new GridPoint2(2, 2));
        assertEquals(Direction.RIGHT, tank.getDirection());
        assertEquals(0f, tank.getRotation());
        assertFalse(tank.tryMove(Direction.UP, cell -> true));
        assertEquals(Direction.RIGHT, tank.getDirection());
        assertEquals(0f, tank.getRotation());
        assertEquals(new GridPoint2(3, 2), tank.shoot().getCoordinates());
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void successfulMovementSetsFacingAndShotStartsOneCellAhead(Direction direction) {
        Tank tank = new Tank(new GridPoint2(3, 3));
        assertTrue(tank.tryMove(direction, cell -> false));
        assertEquals(direction, tank.getDirection());
        assertEquals(direction.rotation(), tank.getRotation());
        assertThrows(IllegalStateException.class, tank::shoot);
        tank.updateMovement(0.4f);
        GridPoint2 before = tank.getCoordinates();
        Bullet bullet = tank.shoot();
        assertEquals(new GridPoint2(before).add(direction.delta()), bullet.getCoordinates());
        assertEquals(direction, bullet.getDirection());
        assertEquals(Bullet.DEFAULT_DAMAGE, bullet.getDamage());
        assertEquals(before, tank.getCoordinates());
        bullet.getCoordinates().set(99, 99);
        assertEquals(new GridPoint2(before).add(direction.delta()), bullet.getCoordinates());
        tank.setCoordinates(new GridPoint2(1, 1));
        assertEquals(direction, tank.getDirection());
    }
}
