package com.mygdx.game.move_related;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;

import static com.mygdx.game.ComponentMappers.am;

/**
 * Class representing a status effect. Is used in {@link com.mygdx.game.components.StatusEffectComponent}.
 * @author Phillip O'Reggio
 */
public class StatusEffect {
    private String name;
    /**
     * Note: Since status effects turn count is incremented at the start of the holder's turn, this can make
     * the turn order appear different based on when it is inflicted. Inflicted someone with a 1-turn effect
     * BEFORE their turn will make it end when their turn comes. (Making it feel like less than a turn). Note that this
     * DOES NOT affect effects that deal turn effects. Since the turn effect happens before the effect is removed, a
     * 1-turn poison will hurt the holder once.
     */
    public final int DURATION;
    private int currentTurn;
    private final Color COLOR;
    private boolean isFinished;
    private TurnEffect turnEffect;
    private StatChanges statChanges;

    /**
     * Creates a {@link StatusEffect}.
     * @param n name. Is used as the key value in a {@link com.badlogic.gdx.utils.OrderedMap} so spelling matters! Is case-sensitive.
     * @param duration length of turn duration
     * @param color of the entity when inflicted with the condition
     * @param effect that happens when on each turn
     */
    public StatusEffect(String n, int duration, Color color, TurnEffect effect) {
        name = n;
        DURATION = duration;
        COLOR = color;
        turnEffect = effect;
    }

    /**
     * starts the initial effect on an Entity
     */
    public void doInitialEffect(Entity e) {
        if (am.has(e))
            am.get(e).actor.shade(COLOR);
    }

    /**
     * Applies the effects of the status effect by calling {@link TurnEffect}. Called at the beginning of the entity's turn
     * @param e Entity affected
     */
    public void doTurnEffect(Entity e) {
        turnEffect.doEffect(e);
        currentTurn += 1;
        if (currentTurn >= DURATION) {
            isFinished = true;
        }
    }

    public void setStatChanges(float maxHealth, float skill, float maxSkill, float attack, float defense, float speed) {
        statChanges = new StatChanges(maxHealth, skill, maxSkill, attack, defense, speed);
    }

    public StatChanges getStatChanges() {
        return statChanges;
    }

    public String getName() {
        return name;
    }

    public Color getColor() {
        return COLOR;
    }

    public boolean getIsFinished() {
        return isFinished;
    }

    /**
     * Class for the stat changes a {@link StatusEffect} causes.
     */
    public class StatChanges {
        /** Amount is multiplied to corresponding stat. Ex. : if hp in {@link StatChanges} is
         * 2, that means that the entity's health is multiplied by 2.
         */
        public final float maxHP, sp, maxSP, atk, def, spd;

        public StatChanges(float maxHealth, float skill, float maxSkill, float attack, float defense, float speed) {
            maxHP = maxHealth;
            sp = skill;
            maxSP = maxSkill;
            atk = attack;
            def = defense;
            spd = speed;
        }
    }

    /**
     * Turn Effect of a {@link com.mygdx.game.move_related.StatusEffect}.
     *
     * @author Phillip O'Reggio
     */
    public interface TurnEffect {
        void doEffect(Entity e);
    }
}
