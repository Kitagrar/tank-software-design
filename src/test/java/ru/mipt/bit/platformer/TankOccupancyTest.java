package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Tank;

import static org.junit.jupiter.api.Assertions.*;

class TankOccupancyTest {
    @Test
    void stationaryTankOccupiesOnlyItsCurrentCell() {
        Tank tank = new Tank(new GridPoint2(1, 1));
        assertTrue(tank.occupies(new GridPoint2(1, 1)));
        assertFalse(tank.occupies(new GridPoint2(2, 1)));
        assertTrue(tank.isReadyToMove());
    }

    @Test
    void movingTankReservesBothCellsUntilItFinishes() {
        Tank tank = new Tank(new GridPoint2(1, 1));
        assertTrue(tank.tryMove(Direction.RIGHT, null));
        assertTrue(tank.occupies(new GridPoint2(1, 1)));
        assertTrue(tank.occupies(new GridPoint2(2, 1)));
        assertFalse(tank.occupies(new GridPoint2(3, 1)));
        tank.updateMovement(0.2f);
        assertFalse(tank.isReadyToMove());
        assertTrue(tank.occupies(new GridPoint2(1, 1)));
        assertFalse(tank.tryMove(Direction.UP, null));
        assertEquals(new GridPoint2(2, 1), tank.getDestinationCoordinates());
        tank.updateMovement(0.2f);
        assertTrue(tank.isReadyToMove());
        assertFalse(tank.occupies(new GridPoint2(1, 1)));
        assertTrue(tank.occupies(new GridPoint2(2, 1)));
    }

    @Test
    void nearCompleteProgressSnapsCoordinatesBeforeAcceptingAnotherMove() {
        Tank tank = new Tank(new GridPoint2(0, 0));
        tank.tryMove(Direction.RIGHT, null);
        tank.updateMovement(0.4f * (1f - 0.0000001f));
        assertTrue(tank.isReadyToMove());
        assertEquals(new GridPoint2(1, 0), tank.getCoordinates());
        assertFalse(tank.occupies(new GridPoint2(0, 0)));
        assertTrue(tank.tryMove(Direction.RIGHT, tank));
        assertEquals(new GridPoint2(2, 0), tank.getDestinationCoordinates());
    }
}
