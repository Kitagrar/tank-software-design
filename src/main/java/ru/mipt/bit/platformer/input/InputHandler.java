package ru.mipt.bit.platformer.input;

import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Obstacle;
import ru.mipt.bit.platformer.model.Tank;

public class InputHandler {
    private final InputSource inputSource;
    private final Tank tank;
    private final Obstacle obstacle;

    public InputHandler(InputSource inputSource, Tank tank, Obstacle obstacle) {
        this.inputSource = inputSource;
        this.tank = tank;
        this.obstacle = obstacle;
    }

    public void update() {
        for (Direction direction : Direction.values()) {
            if (inputSource.isPressed(direction)) {
                tank.tryMove(direction, obstacle);
                return;
            }
        }
    }
}
