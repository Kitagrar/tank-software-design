package ru.mipt.bit.platformer.model;

public interface LevelObserver {
    void onObjectAdded(LevelEntity entity);

    void onObjectRemoved(LevelEntity entity);
}
