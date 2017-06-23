package com.mygdx.game.move_related;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.BoardPosition;

/**
 * Class containing data that is used in {@code Visuals}. Has information about when it should be triggered, how many times to play it, and more.
 * @author Phillip O'Reggio
 */
public class VisualEvent {

    private float triggerTime;
    private VisualEffect visualEffect;
    //for repeating
    private final int repeatAmount;
    private int currentAmount;

    /**
     * @param effect visual effect
     * @param time time till it triggers (from the last event triggered). Is the space after this event.
     * @param repeat amount of times it will repeat in a row(1 means it plays once, 2 means twice, and so on)
     */
    public VisualEvent(VisualEffect effect, float time, int repeat) {
        visualEffect = effect;
        triggerTime = time;
        repeatAmount = repeat;
    }

    public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
        visualEffect.doVisuals(user, targetPositions);
    }

    /**
     * Resets the repeat counter
     */
    public void resetRepeat() {
        currentAmount = 0;
    }

    /**
     * Copies exactly
     * @return copy
     */
    public VisualEvent copy() {
        return new VisualEvent(visualEffect, triggerTime, repeatAmount);
    }

    /**
     * Copies, but with a changed trigger time
     * @param trigTime trigger time
     * @return copy
     */
    public VisualEvent copy(float trigTime) {
        return new VisualEvent(visualEffect, trigTime, repeatAmount);
    }

    /**
     * Copies, but with a changed repeat amount
     * @param newRepeat repeat amount
     * @return copy
     */
    public VisualEvent copy(int newRepeat) {
        return new VisualEvent(visualEffect, triggerTime, newRepeat);
    }

    /**
     * Copies, but with a changed trigger time and repeat amount
     * @param trigTime trigger time
     * @param repeat repeat amount
     * @return copy
     */
    public VisualEvent copy(float trigTime, int repeat) {
        return new VisualEvent(visualEffect, trigTime, repeat);
    }

    public void setTriggerTime(float t) {
        triggerTime = t;
    }

    public void incrementRepeat(int amount) {
        currentAmount += amount;
    }

    public float getTriggerTime() {
        return triggerTime;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public int getRepeatAmount() {
        return repeatAmount;
    }
}
