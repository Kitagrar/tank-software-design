package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.mipt.bit.platformer.level.FileLevelCreationStrategy;
import ru.mipt.bit.platformer.level.LevelValidator;
import ru.mipt.bit.platformer.model.GameLevel;
import ru.mipt.bit.platformer.model.Tree;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileLevelCreationStrategyTest {
    private final FileLevelCreationStrategy strategy =
            new FileLevelCreationStrategy("levels/level1.txt", new LevelValidator());

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r\n"})
    void parsesTankAndMultipleTreesWithTopRowAtHighY(String separator) {
        String layout = String.join(separator, "T__X", "____", "_T__") + separator;
        GameLevel level = strategy.parse(new StringReader(layout), 4, 3);
        assertEquals(new GridPoint2(3, 2), level.getTank().getCoordinates());
        assertEquals(List.of(new GridPoint2(0, 2), new GridPoint2(1, 0)),
                level.getTrees().stream().map(Tree::getCoordinates).toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "___\n___", "XX_\n___", "X?_\n___", "X__\n__", "X _\n___",
            "X__\n\n___", "X__\n___\n" + "\n"})
    void rejectsMalformedLayout(String layout) {
        assertThrows(IllegalArgumentException.class, () -> strategy.parse(new StringReader(layout), 3, 2));
    }

    @Test
    void rejectsDimensionsDifferentFromGraphicMap() {
        assertThrows(IllegalArgumentException.class, () -> strategy.create(9, 8));
        assertThrows(IllegalArgumentException.class, () -> strategy.create(10, 7));
        assertThrows(IllegalArgumentException.class, () -> strategy.create(0, 8));
    }

    @Test
    void loadsClasspathExample() {
        GameLevel level = strategy.create(10, 8);
        assertEquals(new GridPoint2(4, 3), level.getTank().getCoordinates());
        assertEquals(11, level.getTrees().size());
    }

    @Test
    void rejectsMissingResource() {
        FileLevelCreationStrategy missing = new FileLevelCreationStrategy("levels/missing.txt", new LevelValidator());
        assertThrows(IllegalArgumentException.class, () -> missing.create(10, 8));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "__T__\n__T__\nX_T__\n__T__\n__T__",
            "_____\n_____\nTTTTT\n__X__\n_____",
            "X____\n_TTT_\n_T_T_\n_TTT_\n_____",
            "X_T__\n__TT_\n___T_\n___TT\n_____",
            "XT\nT_"
    })
    void rejectsDisconnectedFiles(String layout) {
        String[] rows = layout.split("\n");
        assertThrows(IllegalArgumentException.class,
                () -> strategy.parse(new StringReader(layout), rows[0].length(), rows.length));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "__T__\n__T__\nX_T__\n_____\n__T__",
            "_____\n_____\nTT_TT\n__X__\n_____",
            "_____\n__X__\n_____"
    })
    void acceptsConnectedFilesIncludingWallsWithPassages(String layout) {
        String[] rows = layout.split("\n");
        assertDoesNotThrow(() -> strategy.parse(new StringReader(layout), rows[0].length(), rows.length));
    }
}
