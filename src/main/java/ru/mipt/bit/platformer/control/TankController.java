package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.command.Command;
import ru.mipt.bit.platformer.command.MoveCommand;
import ru.mipt.bit.platformer.command.ShootCommand;
import ru.mipt.bit.platformer.model.GameLevel;
import ru.mipt.bit.platformer.model.Tank;

import java.util.Objects;
import java.util.Optional;

public final class TankController {
    private final Tank tank;
    private final TankControlStrategy strategy;
    private final GameLevel level;

    public TankController(Tank tank, TankControlStrategy strategy, GameLevel level) {
        this.tank = Objects.requireNonNull(tank);
        this.strategy = Objects.requireNonNull(strategy);
        this.level = Objects.requireNonNull(level);
    }

    public Optional<Command> createCommand() {
        if (!tank.isAlive() || !level.containsTank(tank) || !tank.isReadyToMove()) {
            return Optional.empty();
        }
        return strategy.chooseAction().map(action -> switch (action.kind()) {
            case MOVE -> new MoveCommand(tank, action.direction().orElseThrow(), level.getCollisionObstacle());
            case SHOOT -> new ShootCommand(tank, level);
        });
    }

    /** Compatibility helper; the runtime collects commands before executing them. */
    public void update() {
        createCommand().ifPresent(Command::execute);
    }
}
