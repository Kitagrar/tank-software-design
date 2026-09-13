package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.Tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TankMovementTest {

    @Test
    void tankShouldNotMoveIntoTree() {
        Tank tank = new Tank(new GridPoint2(1, 1));
        Tree tree = new Tree(new GridPoint2(2, 1));

        assertFalse(tank.tryMove(Direction.RIGHT, tree));
        assertEquals(new GridPoint2(1, 1), tank.getCoordinates());
        assertEquals(new GridPoint2(1, 1), tank.getDestinationCoordinates());
    }

    @Test
    void tankShouldMoveWhenPathIsFree() {
        Tank tank = new Tank(new GridPoint2(1, 1));
        Tree tree = new Tree(new GridPoint2(3, 3));

        assertTrue(tank.tryMove(Direction.RIGHT, tree));
        assertEquals(new GridPoint2(2, 1), tank.getDestinationCoordinates());
    }

    @Test
    void tankShouldAdvanceProgressDuringMovement() {
        Tank tank = new Tank(new GridPoint2(1, 1));
        tank.tryMove(Direction.UP, new Tree(new GridPoint2(3, 3)));

        tank.updateMovement(0.2f);

        assertTrue(tank.getMovementProgress() > 0f);
        assertTrue(tank.getMovementProgress() <= 1f);
    }
}
