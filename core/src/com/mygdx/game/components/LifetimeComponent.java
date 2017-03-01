package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

/**
 * Represents time until an Entity disposes of itself
 * @author Phillip O'Reggio
 */
public class LifetimeComponent implements Component {
    public float currentTime;
    public float endTime;

    /**
     * Creates a {@code LifetimeComponent} with an end time of 1
     */
    public LifetimeComponent() {
        currentTime = 0;
        endTime = 1;
    }

    /**
     * Creates a {@code LifetimeComponent} with a set end time and current time
     * @param current time it starts ticking from
     * @param end time that the entity disposes itself
     */
    public LifetimeComponent(float current, float end) {
        currentTime = current;
        endTime = end;
    }
}
