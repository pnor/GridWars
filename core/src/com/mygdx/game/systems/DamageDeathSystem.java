package com.mygdx.game.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.mygdx.game.components.StatComponent;

import static com.mygdx.game.ComponentMappers.stm;
import static com.mygdx.game.ComponentMappers.vm;

/**
 * @author Phillip O'Reggio
 */
public class DamageDeathSystem extends IteratingSystem {

    public DamageDeathSystem() {
        super(Family.all(StatComponent.class).get());
    }

    @Override
    public void processEntity(Entity e, float deltaTime) {
        //note : death and damage animation won't occur at the same time
        //death
        if (stm.get(e).hp <= 0) {
            if (vm.has(e) && vm.get(e).deathAnimation == null) {
                stm.get(e).alive = false;
            } else {
                stm.get(e).alive = false;
                if (vm.get(e).deathAnimation.getTimer().checkIfFinished()) {
                    //Compensate for unfinished animations
                    if (isDamageAnimationPlaying(e) || isHeavyDamageAnimationPlaying(e) || isShufflingAnimationPlaying(e)) {
                        //Visuals.visualsArePlaying -= 1;
                        if (vm.get(e).damageAnimation != null) {
                            vm.get(e).damageAnimation.reset();
                            vm.get(e).damageAnimation.setPlaying(false, false);
                        }
                        if (vm.get(e).heavyDamageAnimation != null) {
                            vm.get(e).heavyDamageAnimation.reset();
                            vm.get(e).heavyDamageAnimation.setPlaying(false, false);
                        }
                        if (vm.get(e).shuffleAnimation != null) {
                            vm.get(e).shuffleAnimation.reset();
                            vm.get(e).shuffleAnimation.setPlaying(false, false);
                        }
                    }
                    stm.get(e).readyToRemoveFromGame = true;
                }

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
        //damage and shuffling
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
                if (vm.get(e).shuffleAnimation != null && vm.get(e).shuffleAnimation.getIsPlaying()) {
                    vm.get(e).shuffleAnimation.updateTimer(deltaTime);
                    vm.get(e).shuffleAnimation.play();
                }
            }
        }
    }

    /**
     * @param e Entity being checked
     * @return true if the entity has a damage animation and if its playing. False otherwise.
     */
    private boolean isDamageAnimationPlaying(Entity e) {
        return vm.get(e).damageAnimation != null && vm.get(e).damageAnimation.getIsPlaying();
    }

    /**
     * @param e Entity being checked
     * @return true if the entity has a heavy damage animation and if its playing. False otherwise.
     */
    private boolean isHeavyDamageAnimationPlaying(Entity e) {
        return vm.get(e).heavyDamageAnimation != null && vm.get(e).heavyDamageAnimation.getIsPlaying();
    }

    /**
     * @param e Entity being checked
     * @return true if the entity has a damage animation and if its playing. False otherwise.
     */
    private boolean isShufflingAnimationPlaying(Entity e) {
        return vm.get(e).shuffleAnimation != null && vm.get(e).shuffleAnimation.getIsPlaying();
    }
}
