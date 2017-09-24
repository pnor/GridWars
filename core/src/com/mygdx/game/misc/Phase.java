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
    private int upperHealthThreshold;
    private int lowerHealthThreshold;
    private UIActor actor;
    private StatComponent stat;

    /**
     * Creates a phase to be used in PhaseComponent
     * @param upperBounds upper health threshold
     * @param lowerBounds lower health threshold
     * @param newActor new Actor
     * @param newStats new Stat Component
     */
    public Phase(int upperBounds, int lowerBounds, UIActor newActor, StatComponent newStats) {
        upperHealthThreshold = upperBounds;
        lowerHealthThreshold = lowerBounds;
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

    /**
     * Whether the health number lies between the upper and lower health thresholds.
     * @param health entity health
     * @return The number is within (lower Threshold, upper Threshold]
     */
    public boolean withinThreshold(int health) {
        return health <= upperHealthThreshold && health > lowerHealthThreshold;
    }

    /**
     * Compares based on upper health thresholds.
     */
    @Override
    public int compareTo(Object o) {
        if (upperHealthThreshold > ((Phase) o).upperHealthThreshold)
            return 1;
        else if (upperHealthThreshold < ((Phase) o).upperHealthThreshold)
            return -1;
        else
            return 0;
    }
}