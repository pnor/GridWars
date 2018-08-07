package com.mygdx.game.move_related;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.misc.GameTimer;
import com.mygdx.game.boards.BoardPosition;

/**
 * Represents the visual effect that plays when an attack is used.
 * @author Phillip O'Reggio
 */
public class Visuals {
    public String name;

    public static Engine engine;
    public static Stage stage;

    private Entity user;
    private Array<BoardPosition> targetPositions;

    private GameTimer timer;
    private Array<VisualEvent> visuals;
    private int currentVisual;

    private boolean isPlaying;
    private boolean autoReset;
    /**
     * greater than 0 when any kind of visuals are playing
     */
    public static int visualsArePlaying;

    /**
     * Creates a {@code Visuals} object
     * @param u user
     * @param positions effected squares
     * @param visual Array of {@code VisualEvent} objects
     */
    public Visuals(Entity u, Array<BoardPosition> positions, Array<VisualEvent> visual) {
        user = u;
        targetPositions = positions;

        float total = 0f;
        for (VisualEvent v : visual) {
            total += v.getTriggerTime() * v.getRepeatAmount();
        }
        timer = new GameTimer(total + .06f); //buffer room

        visuals = visual;
    }

    /**
     * Checks to see if it is time to play the current {@code VisualEffect}. If it is, it plays it.
     */
    private void playVisuals() {
        if (currentVisual >= visuals.size)
            return;
        if (timer.getTime() >= getNextTargetTime()) {
            VisualEvent cur = visuals.get(currentVisual);
            cur.doVisuals(user, targetPositions, engine, stage);
            cur.incrementRepeat(1);
            if (cur.getCurrentAmount() >= cur.getRepeatAmount()) {
                currentVisual += 1;
            }
        }
    }

    /**
     * Plays the animation (Is called multiple times to play entire thing).
     */
    public void play() {
        //if its done (both time wise and if it played all animations)
        if (isFinished()) {
            isPlaying = false;
            Visuals.visualsArePlaying -= 1;
            if (autoReset) {
                autoReset = true;
                isPlaying = false;
                reset();
            }
        }
        if (isPlaying)
            playVisuals();
    }

    /**
     * @return if the timer of this object is done and all {@link VisualEvent} have been played.
     */
    public boolean isFinished() {
        return timer.checkIfFinished() && currentVisual >= visuals.size;
    }

    /**
     * Resets this object so it can play from the start of its animation
     */
    public void reset() {
        timer.setTime(0);
        currentVisual = 0;
        for (VisualEvent v : visuals)
            v.resetRepeat();
    }

    /**
     * @return the next time at which a visual should play
     */
    private float getNextTargetTime() {
        float target = 0;

        for (int i = 0; i <= currentVisual; i++)
            target += visuals.get(i).getTriggerTime() * (visuals.get(i).getCurrentAmount());

        return target;
    }

    /**
     * Updates this object's timer
     * @param dt delta time
     */
    public void updateTimer(float dt) {
        timer.increaseTimer(dt);
    }

    public GameTimer getTimer() {
        return timer;
    }

    /**
     * Sets whether this object is playing or not
     * @param startPlaying whether its playing or not
     * @param autoreset whether it should reset itself after playing
     */
    public void setPlaying(boolean startPlaying, boolean autoreset) {
        if (!isPlaying && startPlaying)
            Visuals.visualsArePlaying += 1;
        else if (isPlaying && !startPlaying)
            Visuals.visualsArePlaying -= 1;

        isPlaying = startPlaying;
        autoReset = autoreset;
    }

    public Visuals createCopy(Entity newUser) {
        Array<BoardPosition> newRange = new Array<>();
        for (BoardPosition bp : targetPositions)
            newRange.add(bp.copy());

        Array<VisualEvent> newVisuals = new Array<>();
        for (VisualEvent v : visuals)
            newVisuals.add(v.copy());

        return new Visuals(newUser, newRange, newVisuals);
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setTargetPositions(Array<BoardPosition> positions) {
        targetPositions = positions;
    }
    
}
