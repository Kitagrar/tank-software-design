package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.input.InputHandler;
import ru.mipt.bit.platformer.input.InputSource;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Tank;
import ru.mipt.bit.platformer.model.Tree;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InputHandlerTest {
    @Test
    void handlerTranslatesPressedDirectionIntoTankMovement() {
        Tank tank = new Tank(new GridPoint2(1, 1));
        InputSource input = direction -> direction == Direction.RIGHT;
        InputHandler handler = new InputHandler(input, tank, new Tree(new GridPoint2(3, 3)));

        handler.update();

        assertEquals(new GridPoint2(2, 1), tank.getDestinationCoordinates());
    }
}
