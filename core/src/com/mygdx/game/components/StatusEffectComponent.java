package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.screens_ui.LerpColor;

import static com.mygdx.game.ComponentMappers.*;

/**
 * @author Phillip O'Reggio
 */
public class StatusEffectComponent implements Component {
    private int totalStatusEffects;
    public int getTotalStatusEffects() {
        return totalStatusEffects;
    }

    private boolean isPoisoned;
    private int currentPoisonTurn;
    public static final int poisonDuration = 3;
    public static final LerpColor poisonColor = new LerpColor(Color.GREEN, new Color(201f / 255f, 1f, 0f, 1f));

    /**
     * starts the poison effect on an Entity
     */
    public void poison(Entity e) {
        totalStatusEffects += 1;
        isPoisoned = true;
        if (am.has(e))
            am.get(e).actor.shade(poisonColor);
    }

    /**
     * Applies the effects of poison. Called at the beginning of the entity's turn
     * @param e Entity affected
     */
    public static void poisonTurnEffect(Entity e) {
        if (stm.has(e))
            stm.get(e).hp -= 1;
        if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
            vm.get(e).heavyDamageAnimation.setPlaying(true, true);
        status.get(e).currentPoisonTurn += 1;
        if (status.get(e).currentPoisonTurn >= StatusEffectComponent.poisonDuration)
            status.get(e).resetPoison();
    }

    public boolean isPoisoned() {
        return isPoisoned;
    }

    /**
     * resets the poison effect
     */
    public void resetPoison() {
        totalStatusEffects -= 1;
        currentPoisonTurn = 0;
        isPoisoned = false;
    }

    private boolean isBurned;
    private int currentBurnTurn;
    public static final int burnDuration = 3;
    public static final LerpColor burnColor = new LerpColor(Color.RED, new Color(1, 125f / 255f, 0f, 1f), .3f, Interpolation.sineOut);

    /**
     * starts the burn effect on an Entity
     */
    public void burn(Entity e) {
        totalStatusEffects += 1;
        isBurned = true;
        if (am.has(e))
            am.get(e).actor.shade(burnColor);
    }

    /**
     * Applies the effects of burn. Called at the beginning of the entity's turn
     * @param e Entity affected
     */
    public static void burnTurnEffect(Entity e) {
        if (stm.has(e) && (float) (Math.random()) < 0.5) {
            stm.get(e).hp -= 1;
            if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                vm.get(e).heavyDamageAnimation.setPlaying(true, true);
        }
        status.get(e).currentBurnTurn += 1;
        if (status.get(e).currentBurnTurn >= StatusEffectComponent.burnDuration)
            status.get(e).resetBurn();
    }

    public boolean isBurned() {
        return isBurned;
    }

    /**
     * resets the burn effect
     */
    public void resetBurn() {
        totalStatusEffects -= 1;
        currentBurnTurn = 0;
        isBurned = false;
    }

    private boolean isParalyzed;
    private int currentParalyzedTurn;
    public static final int paralyzeDuration = 3;
    public static final LerpColor paralyzeColor = new LerpColor(Color.GRAY, Color.YELLOW, .4f, Interpolation.exp5In);

    /**
     * starts the paralyze effect on an Entity
     */
    public void paralyze(Entity e) {
        totalStatusEffects += 1;
        isParalyzed = true;
        if (am.has(e))
            am.get(e).actor.shade(paralyzeColor);
    }

    /**
     * Applies the effects of paralyze. Called at the beginning of the entity's turn
     * @param e Entity affected
     */
    public static void paralyzeTurnEffect(Entity e) {
        status.get(e).currentParalyzedTurn += 1;
        if (status.get(e).currentParalyzedTurn >= StatusEffectComponent.paralyzeDuration)
            status.get(e).resetParalyze();
    }

    public boolean isParalyzed() {
        return isParalyzed;
    }

    /**
     * resets the paralyze effect
     */
    public void resetParalyze() {
        totalStatusEffects -= 1;
        currentParalyzedTurn = 0;
        isParalyzed = false;
    }

    private boolean isPetrified;
    private int currentPetrifyTurn;
    public static final int petrifyDuration = 2;
    public static final Color petrifyColor = new Color(214f / 255f, 82f / 255f, 0, 1);

    /**
     * starts the petrify effect on an Entity
     */
    public void petrify(Entity e) {
        totalStatusEffects += 1;
        isPetrified = true;
        if (am.has(e))
            am.get(e).actor.shade(petrifyColor);
        if (am.get(e).actor instanceof AnimationActor)
            ((AnimationActor) am.get(e).actor).setStopUpdating(true);
    }

    /**
     * Applies the effects of petrify. Called at the beginning of the entity's turn
     * @param e Entity affected.
     */
    public static void petrifyTurnEffect(Entity e) {
        status.get(e).currentPetrifyTurn += 1;
        if (status.get(e).currentPetrifyTurn >= StatusEffectComponent.petrifyDuration)
            status.get(e).resetPetrify(e);
    }

    public boolean isPetrified() {
        return isPetrified;
    }

    /**
     * resets the petrify effect
     * @param e Entity affected. (for the stop animation of this status effect)
     */
    public void resetPetrify(Entity e) {
        totalStatusEffects -= 1;
        currentPetrifyTurn = 0;
        isPetrified = false;
        if (am.get(e).actor instanceof AnimationActor)
            ((AnimationActor) am.get(e).actor).setStopUpdating(false);
    }

    private boolean isStill;
    private int currentStillnessTurn;
    public static final int stillnessDuration = 3;
    public static final Color stillnessColor = new LerpColor(Color.WHITE, new Color(0, 140f / 255f, 1f, 1f), .7f,  Interpolation.sine);
    //new LerpColor(Color.WHITE, new Color(0, 140f / 255f, 1f, 1f), .7f,  Interpolation.sine);

    /**
     * starts the stillness effect on an Entity
     */
    public void still(Entity e) {
        totalStatusEffects += 1;
        isStill = true;
        if (am.has(e))
            am.get(e).actor.shade(stillnessColor);
    }

    /**
     * Applies the effects of paralyze. Called at the beginning of the entity's turn
     * @param e Entity affected.
     */
    public static void stillnessTurnEffect(Entity e) {
        status.get(e).currentStillnessTurn += 1;
        if (status.get(e).currentStillnessTurn >= StatusEffectComponent.stillnessDuration)
            status.get(e).resetStillness();
    }

    public boolean isStill() {
        return isStill;
    }

    /**
     * resets the paralyze effect
     */
    public void resetStillness() {
        totalStatusEffects -= 1;
        currentStillnessTurn = 0;
        isStill = false;
    }

    private boolean isCursed;
    private int currentCurseTurn;
    public static final int curseDuration = 3;
    public static final Color curseColor = new LerpColor(Color.GRAY, Color.BLACK, .5f, Interpolation.fade);

    /**
     * starts the stillness effect on an Entity
     */
    public void curse(Entity e) {
        totalStatusEffects += 1;
        isCursed = true;
        if (am.has(e))
            am.get(e).actor.shade(curseColor);
    }

    /**
     * Applies the effects of paralyze. Called at the beginning of the entity's turn
     * @param e Entity affected.
     */
    public static void curseTurnEffect(Entity e) {
        status.get(e).currentCurseTurn += 1;
        if (status.get(e).currentCurseTurn >= StatusEffectComponent.curseDuration)
            status.get(e).resetCurse();
    }

    public boolean isCursed() {
        return isCursed;
    }

    /**
     * resets the paralyze effect
     */
    public void resetCurse() {
        totalStatusEffects -= 1;
        currentCurseTurn = 0;
        isCursed = false;
    }
}
