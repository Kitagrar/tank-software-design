package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.command.Command;
import ru.mipt.bit.platformer.command.MoveCommand;
import ru.mipt.bit.platformer.model.Obstacle;
import ru.mipt.bit.platformer.model.Tank;

import java.util.Objects;

public final class TankController {
    private final Tank tank;
    private final TankControlStrategy strategy;
    private final Obstacle obstacle;

    public TankController(Tank tank, TankControlStrategy strategy, Obstacle obstacle) {
        this.tank = Objects.requireNonNull(tank);
        this.strategy = Objects.requireNonNull(strategy);
        this.obstacle = Objects.requireNonNull(obstacle);
    }

    public void update() {
        if (!tank.isReadyToMove()) {
            return;
        }
        strategy.chooseDirection().ifPresent(direction -> {
            Command command = new MoveCommand(tank, direction, obstacle);
            command.execute();
        });
    }
}
