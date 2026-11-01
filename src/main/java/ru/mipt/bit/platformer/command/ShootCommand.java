package ru.mipt.bit.platformer.command;

import ru.mipt.bit.platformer.model.GameLevel;
import ru.mipt.bit.platformer.model.Tank;

import java.util.Objects;

public final class ShootCommand implements Command {
    private final Tank receiver;
    private final GameLevel level;

    public ShootCommand(Tank receiver, GameLevel level) {
        this.receiver = Objects.requireNonNull(receiver);
        this.level = Objects.requireNonNull(level);
    }

    @Override
    public void execute() {
        if (receiver.isAlive() && receiver.isReadyToMove() && level.containsTank(receiver)) {
            level.addBullet(receiver.shoot());
        }
    }
}
