package ru.mipt.bit.platformer.command;

import ru.mipt.bit.platformer.graphics.HealthBarDisplay;

import java.util.Objects;

public final class ToggleHealthBarsCommand implements Command {
    private final HealthBarDisplay receiver;

    public ToggleHealthBarsCommand(HealthBarDisplay receiver) {
        this.receiver = Objects.requireNonNull(receiver);
    }

    @Override
    public void execute() {
        receiver.toggleHealthBars();
    }
}
