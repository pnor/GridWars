package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.mygdx.game.components.AnimationComponent;
import com.mygdx.game.components.SpriteComponent;

import java.util.Comparator;

import static com.mygdx.game.ComponentMappers.*;

/**
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
                sm.get(e).setSpriteLocation(pm.get(e).position.x, pm.get(e).position.y);
                sm.get(e).draw(batch);
            } else {
                animm.get(e).currentTime += deltaTime;
                animm.get(e).setSpriteLocation(pm.get(e).position.x, pm.get(e).position.y);
                animm.get(e).draw(batch);
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
