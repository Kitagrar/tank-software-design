package ru.mipt.bit.platformer.model;

/** Core output port for entity lifecycle, including initial synchronization. */
public interface LevelObserver {
    void onObjectAdded(LevelEntity entity);

    void onObjectRemoved(LevelEntity entity);
}
