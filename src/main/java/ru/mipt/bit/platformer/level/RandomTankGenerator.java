package ru.mipt.bit.platformer.level;

import com.badlogic.gdx.math.GridPoint2;
import ru.mipt.bit.platformer.model.GameLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class RandomTankGenerator {
    private final Random random;

    public RandomTankGenerator(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    public List<GridPoint2> generatePositions(GameLevel level, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("AI tank count must be non-negative");
        }
        List<GridPoint2> candidates = new ArrayList<>();
        for (int y = 0; y < level.getHeight(); y++) {
            for (int x = 0; x < level.getWidth(); x++) {
                GridPoint2 cell = new GridPoint2(x, y);
                if (!level.getCollisionObstacle().occupies(cell)) {
                    candidates.add(cell);
                }
            }
        }
        if (count > candidates.size()) {
            throw new IllegalArgumentException("Requested " + count + " AI tanks, only "
                    + candidates.size() + " spawn cells available");
        }
        Collections.shuffle(candidates, random);
        return List.copyOf(candidates.subList(0, count));
    }
}
