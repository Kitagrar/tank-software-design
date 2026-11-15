package ru.mipt.bit.platformer.level;

import ru.mipt.bit.platformer.model.GameLevel;

/** Initial-level factory contract; not a dedicated external-resource source port. */
public interface LevelCreationStrategy {
    GameLevel create(int width, int height);
}
