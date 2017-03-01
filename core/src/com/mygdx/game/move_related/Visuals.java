package com.mygdx.game.move_related;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.GameTimer;

/**
 * Represents the visual effect that plays when an attack is used.
 * @author Phillip O'Reggio
 */
public class Visuals {
    public static Engine engine;
    public static Stage stage;
    public static BoardManager boardManager;

    private Entity user;
    private Array<BoardPosition> targetPositions;

    private GameTimer timer;
    private Array<VisualEffect> visuals;
    private int currentVisual;
    private Array<Float> triggerTimes;
    private int currentTime;
    private boolean isPlaying;

    /**
     * Creates a {@code Visuals} object
     * @param u user
     * @param positions effected squares
     * @param timr timer that controls which Visual Effect plays
     * @param visual Array of {@code VisualEffect} objects
     * @param triggerTime Array of trigger times for animations
     */
    public Visuals(Entity u, Array<BoardPosition> positions, GameTimer timr, Array<VisualEffect> visual, Array<Float> triggerTime) {
        user = u;
        targetPositions = positions;
        timer = timr;
        visuals = visual;
        triggerTimes = triggerTime;
    }

    /**
     * Plays the animation. (Is called multiple times to play entire thing)
     */
    public void play() {
        if (timer.checkIfFinished())
            isPlaying = false;
        if (isPlaying)
            playVisuals();
    }

    /**
     * Plays the current visual
     */
    private void playVisuals() {
        if (currentVisual >= visuals.size || currentTime >= triggerTimes.size)
            return;
        if (timer.getTime() >= getNextTargetTime()) {
            visuals.get(currentVisual).doVisuals(user, targetPositions, engine, stage, boardManager);
            currentVisual += 1;
            currentTime += 1;
        }
    }

    /**
     * @return the next time at which a visual should play
     */
    private float getNextTargetTime() {
        float target = 0;
        for (int i = 0; i < currentTime; i++) {
            target += triggerTimes.get(currentTime);
        }
        return target;
    }

    /**
     * Resets this object so it can play from the start of its animation
     */
    public void reset() {
        timer.setTime(0);
        currentTime = 0;
        currentVisual = 0;
    }

    /**
     * Updates this object's timer
     * @param dt
     */
    public void updateTimer(float dt) {
        timer.increaseTimer(dt);
    }

    /**
     * Sets whether this object is playing or not
     * @param startPlaying whether its playing or not
     */
    public void setPlaying(boolean startPlaying) {
        isPlaying = startPlaying;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setTargetPositions(Array<BoardPosition> positions) {
        targetPositions = positions;
    }

}
