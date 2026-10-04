package ru.mipt.bit.platformer.model;

public interface Movable {
    boolean tryMove(Direction direction, Obstacle obstacle);
}
