package com.mygdx.game.misc;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.actors.UIActor;
import com.mygdx.game.components.StatComponent;

import static com.mygdx.game.ComponentMappers.am;
import static com.mygdx.game.ComponentMappers.stm;

/**
 * Contains stat and visual information (as {@link StatComponent}s and {@link UIActor}s for each phase. Used in {@link com.mygdx.game.components.PhaseComponent}.
 */
public class Phase implements Comparable {
    private int healthThreshold; //if lower than this amount, go into phase
    private UIActor actor;
    private StatComponent stat;

    /**
     * Creates a phase to be used in PhaseComponent
     * @param threshold health threshold
     * @param newActor new Actor
     * @param newStats new Stat Component
     */
    public Phase(int threshold, UIActor newActor, StatComponent newStats) {
        healthThreshold = threshold;
        actor = newActor;
        stat = newStats;
    }

    /**
     * Applies the changes of this phase to an entity.
     * @param e entity being changed
     */
    public void applyPhase(Entity e) {
        int currentHealth = stm.get(e).hp;
        //apply health
        e.remove(StatComponent.class);
        e.add(stat);
        stm.get(e).hp = currentHealth;
        //apply visual
        am.get(e).actor = actor;
    }

    public int getHealthThreshold() {
        return healthThreshold;
    }

    @Override
    public int compareTo(Object o) {
        if (healthThreshold > ((Phase) o).healthThreshold)
            return 1;
        else if (healthThreshold < ((Phase) o).healthThreshold)
            return -1;
        else
            return 0;
    }
}