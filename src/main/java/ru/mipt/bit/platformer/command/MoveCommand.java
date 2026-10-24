package ru.mipt.bit.platformer.command;

import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.Movable;
import ru.mipt.bit.platformer.model.Obstacle;

import java.util.Objects;

public final class MoveCommand implements Command {
    private final Movable receiver;
    private final Direction direction;
    private final Obstacle obstacle;

    public MoveCommand(Movable receiver, Direction direction, Obstacle obstacle) {
        this.receiver = Objects.requireNonNull(receiver);
        this.direction = Objects.requireNonNull(direction);
        this.obstacle = Objects.requireNonNull(obstacle);
    }

    @Override
    public void execute() {
        receiver.tryMove(direction, obstacle);
    }
}
