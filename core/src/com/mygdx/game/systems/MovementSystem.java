package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.components.MovementComponent;
import com.mygdx.game.components.PositionComponent;

import static com.mygdx.game.ComponentMappers.mm;
import static com.mygdx.game.ComponentMappers.pm;

/**
 * System that allows entities to move across the screen
 *
 * @author pnore_000
 */
public class MovementSystem extends IteratingSystem {

    public MovementSystem() {
        super(Family.all(MovementComponent.class, PositionComponent.class).get());
    }

    @Override
    protected void processEntity(Entity e, float deltaTime) {
        pm.get(e).position = new Vector2(pm.get(e).position.x + mm.get(e).movement.x * deltaTime, pm.get(e).position.y + mm.get(e).movement.y * deltaTime);
    }
}
