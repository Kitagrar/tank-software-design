package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.GameLevel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileLevelCreationStrategy implements LevelCreationStrategy {
    private final String resourceName;
    private final LevelValidator validator;

    public FileLevelCreationStrategy(String resourceName, LevelValidator validator) {
        this.resourceName = Objects.requireNonNull(resourceName);
        this.validator = Objects.requireNonNull(validator);
    }

    @Override
    public GameLevel create(int width, int height) {
        validator.validateDimensions(width, height);
        InputStream stream = FileLevelCreationStrategy.class.getClassLoader().getResourceAsStream(resourceName);
        if (stream == null) {
            throw new IllegalArgumentException("Level resource not found: " + resourceName);
        }
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return parse(reader, width, height);
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read level resource: " + resourceName, exception);
        }
    }

    /** Parses without closing the caller's reader. */
    public GameLevel parse(Reader reader, int width, int height) {
        validator.validateDimensions(width, height);
        BufferedReader buffered = new BufferedReader(reader);
        List<String> rows = new ArrayList<>();
        try {
            String line;
            while ((line = buffered.readLine()) != null) {
                rows.add(line);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Cannot read level layout", exception);
        }
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Level file is empty");
        }
        if (rows.size() != height) {
            throw new IllegalArgumentException("Expected " + height + " rows, got " + rows.size());
        }
        GridPoint2 tankPosition = null;
        List<GridPoint2> treePositions = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            String line = rows.get(row);
            if (line.length() != width) {
                throw new IllegalArgumentException("Row " + (row + 1) + " must contain exactly " + width + " cells");
            }
            for (int column = 0; column < width; column++) {
                GridPoint2 position = new GridPoint2(column, height - 1 - row);
                switch (line.charAt(column)) {
                    case 'T':
                        treePositions.add(position);
                        break;
                    case 'X':
                        if (tankPosition != null) {
                            throw new IllegalArgumentException("Level must contain exactly one X");
                        }
                        tankPosition = position;
                        break;
                    case '_':
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown character at row " + (row + 1)
                                + ", column " + (column + 1) + ": " + line.charAt(column));
                }
            }
        }
        if (tankPosition == null) {
            throw new IllegalArgumentException("Level must contain exactly one X");
        }
        validator.validate(width, height, tankPosition, treePositions);
        return new GameLevel(width, height, tankPosition, treePositions);
    }
}
