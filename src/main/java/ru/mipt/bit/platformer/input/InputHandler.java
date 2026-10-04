package ru.mipt.bit.platformer.input;

import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Movable;
import ru.mipt.bit.platformer.model.Obstacle;

public class InputHandler {
    private final InputSource inputSource;
    private final Movable movable;
    private final Obstacle obstacle;

    public InputHandler(InputSource inputSource, Movable movable, Obstacle obstacle) {
        this.inputSource = inputSource;
        this.movable = movable;
        this.obstacle = obstacle;
    }

    public void update() {
        for (Direction direction : Direction.values()) {
            if (inputSource.isPressed(direction)) {
                movable.tryMove(direction, obstacle);
                return;
            }
        }
    }
}
