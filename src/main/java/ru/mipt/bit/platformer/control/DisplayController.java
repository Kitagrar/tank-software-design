package ru.mipt.bit.platformer.control;

import ru.mipt.bit.platformer.command.Command;
import ru.mipt.bit.platformer.command.ToggleHealthBarsCommand;
import ru.mipt.bit.platformer.graphics.HealthBarDisplay;
import ru.mipt.bit.platformer.input.InputSource;

import java.util.Objects;
import java.util.Optional;

public final class DisplayController {
    private final InputSource inputSource;
    private final HealthBarDisplay display;

    public DisplayController(InputSource inputSource, HealthBarDisplay display) {
        this.inputSource = Objects.requireNonNull(inputSource);
        this.display = Objects.requireNonNull(display);
    }

    public Optional<Command> createCommand() {
        return inputSource.isHealthTogglePressed()
                ? Optional.of(new ToggleHealthBarsCommand(display)) : Optional.empty();
    }
}
