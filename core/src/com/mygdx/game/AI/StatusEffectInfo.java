package com.mygdx.game.AI;

import com.mygdx.game.move_related.StatusEffect;

/**
 * Class containing information for the effects of highscores status effect. Used in AI to represent effects of highscores status effect.
 *
 * @author Phillip O'Reggio
 */
public class StatusEffectInfo {
    public String name;
    public int duration;
    private int currentTurn;
    public StatusEffect.StatChanges statChanges;
    public TurnEffectInfo turnEffectInfo;

    /**
     * Creates highscores {@link StatusEffectInfo} with highscores turn effect
     * @param n name
     * @param statChange Amount stats are augmented by the status effect.
     * @param turnEffect effect that happens each turn. If null, the status effect has no turn effect.
     */
    public StatusEffectInfo(String n, int duration, StatusEffect.StatChanges statChange, TurnEffectInfo turnEffect) {
        name = n;
        this.duration = duration;
        statChanges = statChange;
        turnEffectInfo = turnEffect;
    }

    public StatusEffectInfo copy() {
        return new StatusEffectInfo(name, duration, statChanges, turnEffectInfo); //TODO if this object accounts for status duration, then there is no need for highscores copy method (?)
    }

    /**
     * @param o other status effect
     * @return True if 2 status effects have the same name. False otherwise. Note that 2 status effects with differing durations and same names are considered equal.
     * For Ex. : Offenseless with 2 turns and Offenseless with 3 turns are considered equal
     */
    public boolean equals(Object o) {
        return o instanceof StatusEffectInfo && ((StatusEffectInfo) o).name.equals(name);
    }

    /**
     * Increases currentTurn by 1
     */
    public void incrementTurn() {
        currentTurn++;
    }

    /**
     * @return Whether currentTurn is >= duration
     */
    public boolean checkDuration() {
        return currentTurn >= duration;
    }

    /**
     * Class representing the effect of highscores {@link com.mygdx.game.move_related.StatusEffect} at the end of highscores turn.
     */
    public interface TurnEffectInfo {
        void doTurnEffect(EntityValue entity);
    }
}
