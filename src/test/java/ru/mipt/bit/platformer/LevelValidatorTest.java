package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mipt.bit.platformer.level.LevelValidator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LevelValidatorTest {
    private final LevelValidator validator = new LevelValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "__T__\n__T__\nX_T__\n__T__\n__T__",
            "_____\n_____\nTTTTT\n__X__\n_____",
            "X____\n_TTT_\n_T_T_\n_TTT_\n_____",
            "X_T__\n__TT_\n___T_\n___TT\n_____",
            "XT\nT_"
    })
    void rejectsDisconnectedFreeArea(String layout) {
        assertThrows(IllegalArgumentException.class, () -> validateLayout(layout));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "__T__\n__T__\nX_T__\n_____\n__T__",
            "_____\n_____\nTT_TT\n__X__\n_____",
            "_____\n__X__\n_____",
            "X"
    })
    void acceptsConnectedFreeArea(String layout) {
        assertDoesNotThrow(() -> validateLayout(layout));
    }

    private void validateLayout(String layout) {
        String[] rows = layout.split("\n");
        GridPoint2 tank = null;
        List<GridPoint2> trees = new ArrayList<>();
        for (int row = 0; row < rows.length; row++) {
            for (int x = 0; x < rows[row].length(); x++) {
                GridPoint2 point = new GridPoint2(x, rows.length - 1 - row);
                if (rows[row].charAt(x) == 'X') {
                    tank = point;
                } else if (rows[row].charAt(x) == 'T') {
                    trees.add(point);
                }
            }
        }
        validator.validate(rows[0].length(), rows.length, tank, trees);
    }

    @Test
    void rejectsInvalidDimensionsAndAreaOverflow() {
        assertThrows(IllegalArgumentException.class, () -> validator.validateDimensions(0, 8));
        assertThrows(IllegalArgumentException.class, () -> validator.validateDimensions(10, -1));
        assertThrows(IllegalArgumentException.class, () -> validator.validateDimensions(50_000, 50_000));
    }

    @Test
    void rejectsTankOutsideBounds() {
        for (GridPoint2 point : List.of(new GridPoint2(-1, 0), new GridPoint2(3, 0),
                new GridPoint2(0, -1), new GridPoint2(0, 3))) {
            assertThrows(IllegalArgumentException.class, () -> validator.validate(3, 3, point, List.of()));
        }
    }

    @Test
    void rejectsTreeOutsideBounds() {
        for (GridPoint2 point : List.of(new GridPoint2(-1, 0), new GridPoint2(3, 0),
                new GridPoint2(0, -1), new GridPoint2(0, 3))) {
            assertThrows(IllegalArgumentException.class,
                    () -> validator.validate(3, 3, new GridPoint2(1, 1), List.of(point)));
        }
    }

    @Test
    void rejectsDuplicateTrees() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(3, 3, new GridPoint2(0, 0),
                List.of(new GridPoint2(1, 1), new GridPoint2(1, 1))));
    }

    @Test
    void rejectsTankOverlappingTree() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(3, 3, new GridPoint2(1, 1),
                List.of(new GridPoint2(1, 1))));
    }
}
