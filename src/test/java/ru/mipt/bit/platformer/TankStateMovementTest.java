package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Tank;

import static org.junit.jupiter.api.Assertions.*;

class TankStateMovementTest {
    @ParameterizedTest
    @CsvSource({"100, 0.4", "70, 0.8", "14, 1.2"})
    void cellMovementTakesStateSpecificDuration(int health, float duration) {
        Tank tank = movingTank(health);
        GridPoint2 source = tank.getCoordinates();
        GridPoint2 destination = tank.getDestinationCoordinates();
        tank.updateMovement(duration / 2);
        assertEquals(0.5f, tank.getMovementProgress(), 1e-6f);
        assertFalse(tank.isStationary());
        assertEquals(source, tank.getCoordinates());
        assertTrue(tank.occupies(source));
        assertTrue(tank.occupies(destination));
        assertFalse(tank.tryMove(Direction.UP, cell -> false));
        tank.updateMovement(duration / 2);
        assertTrue(tank.isStationary());
        assertEquals(destination, tank.getCoordinates());
        assertFalse(tank.occupies(source));
        assertTrue(tank.occupies(destination));
    }

    @Test
    void sameDeltaAdvancesLightMoreThanMediumMoreThanHeavy() {
        Tank light = movingTank(100);
        Tank medium = movingTank(70);
        Tank heavy = movingTank(14);
        for (Tank tank : new Tank[]{light, medium, heavy}) {
            tank.updateMovement(0.2f);
        }
        assertTrue(light.getMovementProgress() > medium.getMovementProgress());
        assertTrue(medium.getMovementProgress() > heavy.getMovementProgress());
    }

    @Test
    void mediumAndHeavyDoNotFinishAtOldDuration() {
        Tank medium = movingTank(70);
        Tank heavy = movingTank(14);
        medium.updateMovement(0.4f);
        heavy.updateMovement(0.8f);
        assertFalse(medium.isStationary());
        assertFalse(heavy.isStationary());
        assertEquals(0.5f, medium.getMovementProgress(), 1e-6f);
        assertEquals(2f / 3, heavy.getMovementProgress(), 1e-6f);
    }

    @Test
    void damageMidMovePreservesGeometryAndProgressButChangesSubsequentSpeed() {
        Tank tank = movingTank(100);
        GridPoint2 source = tank.getCoordinates();
        GridPoint2 destination = tank.getDestinationCoordinates();
        tank.updateMovement(0.1f);
        assertEquals(0.25f, tank.getMovementProgress(), 1e-6f);

        tank.takeDamage(30);
        assertEquals(0.25f, tank.getMovementProgress(), 1e-6f);
        assertEquals(source, tank.getCoordinates());
        assertEquals(destination, tank.getDestinationCoordinates());
        tank.updateMovement(0.2f);
        assertEquals(0.5f, tank.getMovementProgress(), 1e-6f);

        tank.takeDamage(56);
        assertEquals(0.5f, tank.getMovementProgress(), 1e-6f);
        assertEquals(source, tank.getCoordinates());
        assertEquals(destination, tank.getDestinationCoordinates());
        tank.updateMovement(0.3f);
        assertEquals(0.75f, tank.getMovementProgress(), 1e-6f);
        assertTrue(tank.occupies(source));
        assertTrue(tank.occupies(destination));
        tank.updateMovement(0.3f);
        assertTrue(tank.isStationary());
        assertEquals(destination, tank.getCoordinates());
        assertFalse(tank.occupies(source));
    }

    private Tank movingTank(int health) {
        Tank tank = new Tank(new GridPoint2(1, 1));
        if (health < tank.getHealth()) {
            tank.takeDamage(tank.getHealth() - health);
        }
        assertTrue(tank.tryMove(Direction.RIGHT, cell -> false));
        return tank;
    }
}
