package ru.mipt.bit.platformer;

import org.junit.jupiter.api.Test;
import ru.mipt.bit.platformer.command.ToggleHealthBarsCommand;
import ru.mipt.bit.platformer.graphics.HealthBarDisplay;

import static org.junit.jupiter.api.Assertions.*;

class ToggleHealthBarsCommandTest {
    @Test
    void constructionDoesNotToggleAndEachExecutionTogglesReceiverOnce() {
        FakeDisplay display = new FakeDisplay();
        ToggleHealthBarsCommand command = new ToggleHealthBarsCommand(display);
        assertFalse(display.areHealthBarsVisible());
        assertEquals(0, display.toggles);

        command.execute();
        assertTrue(display.areHealthBarsVisible());
        assertEquals(1, display.toggles);

        command.execute();
        assertFalse(display.areHealthBarsVisible());
        assertEquals(2, display.toggles);
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
