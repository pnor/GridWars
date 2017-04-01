package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.components.StatComponent;

import static com.mygdx.game.ComponentMappers.stm;
import static com.mygdx.game.ComponentMappers.vm;

/**
 * @author Phillip O'Reggio
 */
public class DamageDeathSystem extends IteratingSystem {

    public DamageDeathSystem(BoardManager boards) {
        super(Family.all(StatComponent.class).get());
    }

    @Override
    public void processEntity(Entity e, float deltaTime) {
        //note : death and damage animation won't occur at the same time
        //death
        if (stm.get(e).alive) {
            if (stm.get(e).hp <= 0) {
                if (vm.has(e) && vm.get(e).deathAnimation == null) {
                    stm.get(e).alive = false;
                } else {
                    if (vm.get(e).deathAnimation.getTimer().checkIfFinished())
                        stm.get(e).alive = false;

                    if (!vm.get(e).deathAnimation.getIsPlaying()) {
                        vm.get(e).deathAnimation.setPlaying(true, false);
                        vm.get(e).deathAnimation.updateTimer(deltaTime);
                        vm.get(e).deathAnimation.play();
                    } else {
                        vm.get(e).deathAnimation.updateTimer(deltaTime);
                        vm.get(e).deathAnimation.play();
                    }
                }
            }
            //damage
            else {
                if (vm.has(e)) {
                    if (vm.get(e).damageAnimation != null && vm.get(e).damageAnimation.getIsPlaying()) {
                        vm.get(e).damageAnimation.updateTimer(deltaTime);
                        vm.get(e).damageAnimation.play();
                    }
                    if (vm.get(e).heavyDamageAnimation != null && vm.get(e).heavyDamageAnimation.getIsPlaying()) {
                        vm.get(e).heavyDamageAnimation.updateTimer(deltaTime);
                        vm.get(e).heavyDamageAnimation.play();
                    }
                }
            }
        }
    }
}
