package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.components.EventComponent;

import static com.mygdx.game.ComponentMappers.em;

/**
 * @author Phillip O'Reggio
 */
public class EventSystem extends IteratingSystem {

    public EventSystem() {
        super(Family.all(EventComponent.class).get());
    }

    @Override
    protected void processEntity(Entity e, float deltaTime) {
        EventComponent event;
        event = em.get(e);

        if (event.ticking) {
            event.currentTime += deltaTime;
            if (event.currentTime >= event.targetTime) {
                event.currentTime -= event.targetTime;
                event.event.event(e, getEngine());

                if (!event.repeat)
                    event.ticking = false;
            }
        }
    }

}
