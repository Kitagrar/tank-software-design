package ru.mipt.bit.platformer.port;

/** Application output port for controlling health-bar visibility. */
public interface HealthBarDisplay {
    void toggleHealthBars();

    boolean areHealthBarsVisible();
}
