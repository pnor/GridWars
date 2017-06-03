package com.mygdx.game.move_related;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.misc.TurnEffect;

import static com.mygdx.game.ComponentMappers.am;

/**
 * Class representing a status effect. Is used in {@link com.mygdx.game.components.StatusEffectComponent}.
 * @author Phillip O'Reggio
 */
public class StatusEffect {
    private String name;
    public final int DURATION;
    private int currentTurn;
    private final Color COLOR;
    private boolean isFinished;
    private TurnEffect turnEffect;

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

    public String getName() {
        return name;
    }

    public Color getColor() {
        return COLOR;
    }

    public boolean getIsFinished() {
        return isFinished;
    }
}
