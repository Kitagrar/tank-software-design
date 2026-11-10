package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.command.Command;
import ru.mipt.bit.platformer.command.ShootCommand;
import ru.mipt.bit.platformer.command.ToggleHealthBarsCommand;
import ru.mipt.bit.platformer.control.DisplayController;
import ru.mipt.bit.platformer.control.KeyboardControlStrategy;
import ru.mipt.bit.platformer.control.TankController;
import ru.mipt.bit.platformer.graphics.HealthBarDisplay;
import ru.mipt.bit.platformer.input.InputSource;
import ru.mipt.bit.platformer.model.Direction;
import ru.mipt.bit.platformer.model.GameLevel;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DisplayControllerTest {
    @Test
    void inputCreatesDeferredToggleAndReleaseDoesNotRepeatIt() {
        FakeInput input = new FakeInput();
        FakeDisplay display = new FakeDisplay();
        DisplayController controller = new DisplayController(input, display);
        assertTrue(controller.createCommand().isEmpty());

        input.toggle = true;
        Command command = controller.createCommand().orElseThrow();
        assertInstanceOf(ToggleHealthBarsCommand.class, command);
        assertFalse(display.areHealthBarsVisible());
        assertEquals(0, display.toggles);
        command.execute();
        assertTrue(display.areHealthBarsVisible());
        assertEquals(1, display.toggles);

        input.toggle = false;
        assertTrue(controller.createCommand().isEmpty());
        assertEquals(1, display.toggles);
        input.toggle = true;
        controller.createCommand().orElseThrow().execute();
        assertFalse(display.areHealthBarsVisible());
        assertEquals(2, display.toggles);
    }

    @Test
    void movementAndShootingDoNotCreateDisplayCommandWithoutL() {
        FakeInput input = new FakeInput();
        input.move = true;
        input.shoot = true;
        FakeDisplay display = new FakeDisplay();
        assertTrue(new DisplayController(input, display).createCommand().isEmpty());
        assertFalse(display.areHealthBarsVisible());
        assertEquals(0, display.toggles);
    }

    @Test
    void sharedInputCreatesIndependentDisplayAndTankCommandsBeforeExecution() {
        FakeInput input = new FakeInput();
        input.toggle = true;
        input.shoot = true;
        FakeDisplay display = new FakeDisplay();
        GameLevel level = new GameLevel(5, 3, new GridPoint2(1, 1), List.of());
        DisplayController displayController = new DisplayController(input, display);
        TankController tankController = new TankController(level.getPlayerTank(),
                new KeyboardControlStrategy(input), level);

        Command toggle = displayController.createCommand().orElseThrow();
        Command shoot = tankController.createCommand().orElseThrow();
        assertInstanceOf(ShootCommand.class, shoot);
        assertFalse(display.areHealthBarsVisible());
        assertTrue(level.getBullets().isEmpty());
        List.of(toggle, shoot).forEach(Command::execute);
        assertTrue(display.areHealthBarsVisible());
        assertEquals(1, level.getBullets().size());

        level.getPlayerTank().takeDamage(level.getPlayerTank().getHealth());
        level.tick(0);
        assertTrue(tankController.createCommand().isEmpty());
        displayController.createCommand().orElseThrow().execute();
        assertFalse(display.areHealthBarsVisible());
    }

    private static final class FakeInput implements InputSource {
        private boolean toggle;
        private boolean move;
        private boolean shoot;

        @Override
        public boolean isPressed(Direction direction) {
            return move;
        }

        @Override
        public boolean isShootPressed() {
            return shoot;
        }

        @Override
        public boolean isHealthTogglePressed() {
            return toggle;
        }
    }

    private static final class FakeDisplay implements HealthBarDisplay {
        private boolean visible;
        private int toggles;

        @Override
        public void toggleHealthBars() {
            visible = !visible;
            toggles++;
        }

        @Override
        public boolean areHealthBarsVisible() {
            return visible;
        }
    }
}
