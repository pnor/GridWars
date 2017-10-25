package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mygdx.game.components.AnimationComponent;
import com.mygdx.game.components.SpriteComponent;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;

import java.util.Comparator;

import static com.mygdx.game.ComponentMappers.*;

/**
 * System in charge of drawing Entities (excluding those with actors)
 * @author pnore_000
 */
public class DrawingSystem extends SortedIteratingSystem{
   private Batch batch;

    public DrawingSystem(Batch b) {
        super(Family.one(SpriteComponent.class, AnimationComponent.class).get(), new ZComparator());
        batch = b;
    }

    @Override
    public void processEntity(Entity e, float deltaTime) {
        if (pm.has(e)) {
            if (sm.has(e)) {
                sm.get(e).sprite.setPosition(pm.get(e).position.x, pm.get(e).position.y);
                sm.get(e).sprite.setSize(pm.get(e).width, pm.get(e).height);
                sm.get(e).sprite.setRotation(pm.get(e).rotation);
                sm.get(e).draw(batch);
            } else {
                animm.get(e).currentTime += deltaTime;
                animm.get(e).setSpriteLocation(pm.get(e).position.x, pm.get(e).position.y);
                animm.get(e).setSpriteSize(pm.get(e).height, pm.get(e).width);
                animm.get(e).setSpriteRotation(pm.get(e).rotation);
                animm.get(e).draw(batch);
            }
        }
    }

    /**
     * Draws the Entity's located in a {@code Background} object
     * @param back Background of the screen
     */
    public void drawBackground(Background back, float deltaTime) {
        //draw static background layer
        if (sm.has(back.getBackLayer())) {
            sm.get(back.getBackLayer()).draw(batch);
        } else {
            animm.get(back.getBackLayer()).currentTime += deltaTime;
            animm.get(back.getBackLayer()).draw(batch);
        }

        //draw rest of layers
        for (BackType b : back.getLayers()) {
            if (sm.has(b.getEntity())) {
                sm.get(b.getEntity()).sprite.setPosition(pm.get(b.getEntity()).position.x, pm.get(b.getEntity()).position.y);
                sm.get(b.getEntity()).sprite.setSize(pm.get(b.getEntity()).width, pm.get(b.getEntity()).height);
                sm.get(b.getEntity()).sprite.setRotation(pm.get(b.getEntity()).rotation);
                sm.get(b.getEntity()).draw(batch);
            } else {
                animm.get(b.getEntity()).currentTime += deltaTime;
                animm.get(b.getEntity()).setSpriteLocation(pm.get(b.getEntity()).position.x, pm.get(b.getEntity()).position.y);
                animm.get(b.getEntity()).setSpriteSize(pm.get(b.getEntity()).height, pm.get(b.getEntity()).width);
                animm.get(b.getEntity()).setSpriteRotation(pm.get(b.getEntity()).rotation);
                animm.get(b.getEntity()).draw(batch);
            }
        }
    }

    public static class ZComparator implements Comparator<Entity> {
        @Override
        public int compare(Entity o1, Entity o2) {
            //getting z indeces --
            int z1, z2;
            if (sm.has(o1))
                z1 = sm.get(o1).z;
            else
                z1 = animm.get(o1).z;

            if (sm.has(o2))
                z2 = sm.get(o2).z;
            else
                z2 = animm.get(o2).z;
            //--

            if (z1 < z2)
                return -1;
            else if (z1 > z2)
                return 1;
            else
                return 0;
        }
    }
}
