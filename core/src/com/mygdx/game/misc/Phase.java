package com.mygdx.game.misc;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.UIActor;
import com.mygdx.game.components.StatComponent;
import com.mygdx.game.move_related.StatusEffect;

import static com.mygdx.game.ComponentMappers.*;

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
        //apply health ---
        e.remove(StatComponent.class);
        e.add(stat);
        stm.get(e).hp = currentHealth;
        //apply actor ---
        //handling stop animation caused from a status effect
        if (actor instanceof AnimationActor && am.get(e).actor instanceof AnimationActor)
            //changing from animation actor to animation actor
            ((AnimationActor) actor).setStopUpdating(((AnimationActor) am.get(e).actor).getStopUpdating());
        else if (actor instanceof AnimationActor) {
            //changing to animation actor; will have to search status effects to see if its updating
            boolean hasNonAnimatingStatus = false;
            for (StatusEffect s : status.get(e).getStatusEffects()) {
                if (s.stopsAnimation()) {
                    hasNonAnimatingStatus = true;
                    break;
                }
            }
            ((AnimationActor) actor).setStopUpdating(hasNonAnimatingStatus);
        }
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