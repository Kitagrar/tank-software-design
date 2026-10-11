package ru.mipt.bit.platformer.level;

import ru.mipt.bit.platformer.model.GameLevel;

public interface LevelCreationStrategy {
    GameLevel create(int width, int height);
}
