package com.mygdx.game.AI;

import com.mygdx.game.move_related.StatusEffect;

/**
 * Class containing information for the effects of a status effect. Used in AI to represent effects of a status effect.
 *
 * @author Phillip O'Reggio
 */
public class StatusEffectInfo {
    public String name;
    public StatusEffect.StatChanges statChanges;
    public TurnEffectInfo turnEffectInfo;

    /**
     * Creates a {@link StatusEffectInfo} with a turn effect
     * @param n name
     * @param statChange Amount stats are augmented by the status effect.
     * @param turnEffect effect that happens each turn. If null, the status effect has no turn effect.
     */
    public StatusEffectInfo(String n, StatusEffect.StatChanges statChange, TurnEffectInfo turnEffect) {
        name = n;
        statChanges = statChange;
        turnEffectInfo = turnEffect;
    }

    public StatusEffectInfo copy() {
        return new StatusEffectInfo(name, statChanges, turnEffectInfo); //TODO if this object accounts for status duration, then there is no need for a copy method
    }

    /**
     * Class representing the effect of a {@link com.mygdx.game.move_related.StatusEffect} at the end of a turn.
     */
    public interface TurnEffectInfo {
        void doTurnEffect(EntityValue entity);
    }
}
