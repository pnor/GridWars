package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.components.LifetimeComponent;

import static com.mygdx.game.ComponentMappers.lfm;

/**
 * @author Phillip O'Reggio
 */
public class LifetimeSystem extends IteratingSystem {
    public LifetimeSystem() {
        super(Family.all(LifetimeComponent.class).get());
    }

    @Override
    protected void processEntity(Entity e, float deltaTime) {
        LifetimeComponent life =lfm.get(e);
        life.currentTime += deltaTime;
        if (life.currentTime >= life.endTime) {
            e.removeAll();
            e = null;
        }
    }
}
