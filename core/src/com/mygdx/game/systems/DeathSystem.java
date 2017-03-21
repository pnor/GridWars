package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.components.StatComponent;

import static com.mygdx.game.ComponentMappers.stm;

/**
 * @author Phillip O'Reggio
 */
public class DeathSystem extends IteratingSystem {

    public DeathSystem(BoardManager boards) {
        super(Family.all(StatComponent.class).get());
    }

    @Override
    public void processEntity(Entity e, float deltaTime) {
        if (stm.get(e).alive && stm.get(e).hp <= 0) {
            if (stm.get(e).deathAnimation == null) {
                stm.get(e).alive = false;
            } else {
                if (stm.get(e).deathAnimation.getTimer().checkIfFinished())
                    stm.get(e).alive = false;

                if (!stm.get(e).deathAnimation.getIsPlaying()) {
                    stm.get(e).deathAnimation.setPlaying(true);
                    stm.get(e).deathAnimation.updateTimer(deltaTime);
                    stm.get(e).deathAnimation.play();
                } else {
                    stm.get(e).deathAnimation.updateTimer(deltaTime);
                    stm.get(e).deathAnimation.play();
                }
            }
        }
    }
}
