package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Tank;

import static org.junit.jupiter.api.Assertions.*;

class TankStateTest {
    @ParameterizedTest
    @ValueSource(ints = {100, 80, 1, Integer.MAX_VALUE})
    void fullInitialHealthHasLightBehaviour(int maxHealth) {
        Tank tank = new Tank(new GridPoint2(), maxHealth);
        assertTrue(tank.canShoot());
        assertTrue(tank.tryMove(Direction.RIGHT, cell -> false));
        tank.updateMovement(0.2f);
        assertEquals(0.5f, tank.getMovementProgress(), 1e-6f);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 71, 1, true", "100, 70, 2, true",
            "100, 15, 2, true", "100, 14, 3, false", "100, 1, 3, false",
            "80, 57, 1, true", "80, 56, 2, true",
            "80, 12, 2, true", "80, 11, 3, false",
            "81, 57, 1, true", "81, 56, 2, true",
            "81, 13, 2, true", "81, 12, 3, false",
            "2147483647, 1503238553, 1, true", "2147483647, 1503238552, 2, true",
            "2147483647, 322122548, 2, true", "2147483647, 322122547, 3, false"
    })
    void exactHealthThresholdsControlObservableBehaviour(int maxHealth, int health,
                                                        float multiplier, boolean shooting) {
        Tank tank = new Tank(new GridPoint2(), maxHealth);
        tank.takeDamage(maxHealth - health);
        assertEquals(health, tank.getHealth());
        assertEquals(maxHealth, tank.getMaxHealth());
        assertEquals(shooting, tank.canShoot());
        assertTrue(tank.tryMove(Direction.RIGHT, cell -> false));
        tank.updateMovement(0.2f);
        assertEquals(0.5f / multiplier, tank.getMovementProgress(), 1e-6f);
    }

    @Test
    void damageImmediatelyTransitionsFromLightToMediumToHeavy() {
        Tank tank = new Tank(new GridPoint2());
        tank.takeDamage(30);
        assertTrue(tank.canShoot());
        assertTrue(tank.tryMove(Direction.RIGHT, cell -> false));
        tank.updateMovement(0.4f);
        assertEquals(0.5f, tank.getMovementProgress(), 1e-6f);
        tank.updateMovement(0.4f);
        assertTrue(tank.isReadyToMove());
        tank.takeDamage(56);
        assertEquals(14, tank.getHealth());
        assertFalse(tank.canShoot());
        assertTrue(tank.tryMove(Direction.RIGHT, cell -> false));
        tank.updateMovement(0.4f);
        assertEquals(1f / 3, tank.getMovementProgress(), 1e-6f);
    }

    @Test
    void damageCanSkipMediumAndEnterHeavyDirectly() {
        Tank tank = new Tank(new GridPoint2());
        tank.takeDamage(86);
        assertTrue(tank.isAlive());
        assertFalse(tank.canShoot());
        assertThrows(IllegalStateException.class, tank::shoot);
        assertTrue(tank.tryMove(Direction.RIGHT, cell -> false));
        tank.updateMovement(0.4f);
        assertEquals(1f / 3, tank.getMovementProgress(), 1e-6f);
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 70, 14})
    void lethalDamageFromEveryLivingStateKeepsExistingDeathRules(int healthBeforeDeath) {
        Tank tank = new Tank(new GridPoint2());
        if (healthBeforeDeath < tank.getHealth()) {
            tank.takeDamage(tank.getHealth() - healthBeforeDeath);
        }
        tank.takeDamage(healthBeforeDeath);
        assertEquals(0, tank.getHealth());
        assertFalse(tank.isAlive());
        assertFalse(tank.canShoot());
        assertFalse(tank.tryMove(Direction.RIGHT, cell -> false));
        assertThrows(IllegalStateException.class, tank::shoot);
        tank.takeDamage(1);
        assertEquals(0, tank.getHealth());
        assertFalse(tank.canShoot());
        assertEquals(100, tank.getMaxHealth());
    }
}
