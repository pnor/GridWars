package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.GameEvent;

/**
 * Component representing periodic changes to an entity at runtime. For example, it can change the values of components,
 * so the Entity will behave differently.
 * @author Phillip O'Reggio
 */
public class EventComponent implements Component{
    public float targetTime;
    public float currentTime;
    public boolean repeat;
    public boolean ticking;
    public GameEvent event;

    /**
     * Creates an {@code EventComponent} that changes the entity periodically
     * @param target target time
     * @param current current time
     * @param repeatAfterTarget whether it keeps looping the animation after the target time is passed
     * @param isTicking whether the the time till event is ticking, or not
     * @param evnt what happens when the time elapses
     */
    public EventComponent(float target, float current, boolean repeatAfterTarget, boolean isTicking, GameEvent evnt) {
        targetTime = target;
        currentTime = current;
        repeat = repeatAfterTarget;
        ticking = isTicking;
        event = evnt;
    }
}
