package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.model.*;

import static org.junit.jupiter.api.Assertions.*;

class TankHealthTest {
    @Test
    void damageReducesHealthAndZeroIsDead() {
        Tank tank = new Tank(new GridPoint2(), 30);
        tank.takeDamage(11);
        assertEquals(19, tank.getHealth());
        assertTrue(tank.isAlive());
        tank.takeDamage(19);
        assertEquals(0, tank.getHealth());
        assertFalse(tank.isAlive());
        assertFalse(tank.tryMove(Direction.RIGHT, cell -> false));
        assertThrows(IllegalStateException.class, tank::shoot);
        tank.takeDamage(Integer.MAX_VALUE);
        assertEquals(0, tank.getHealth());
    }

    @Test
    void overkillIsClampedAndInvalidValuesAreRejected() {
        Tank tank = new Tank(new GridPoint2());
        assertEquals(Tank.DEFAULT_HEALTH, tank.getHealth());
        assertThrows(IllegalArgumentException.class, () -> tank.takeDamage(0));
        assertThrows(IllegalArgumentException.class, () -> tank.takeDamage(-1));
        assertThrows(IllegalArgumentException.class, () -> new Tank(new GridPoint2(), 0));
        assertThrows(IllegalArgumentException.class, () -> new Tank(new GridPoint2(), -1));
        tank.takeDamage(Integer.MAX_VALUE);
        assertFalse(tank.isAlive());
        assertEquals(0, tank.getHealth());
    }
}
