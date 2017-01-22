package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.components.MovementComponent;
import com.mygdx.game.components.PositionComponent;

import java.awt.geom.Point2D;

import static com.mygdx.game.ComponentMappers.mm;
import static com.mygdx.game.ComponentMappers.pm;

/**
 * @author pnore_000
 */
public class MovementSystem extends IteratingSystem {

    public MovementSystem() {
        super(Family.all(MovementComponent.class, PositionComponent.class).get());
    }

    @Override
    protected void processEntity(Entity e, float deltaTime) {
        pm.get(e).position = new Point2D.Float(pm.get(e).position.x + mm.get(e).movement.x, pm.get(e).position.y + mm.get(e).movement.y);
    }
}
