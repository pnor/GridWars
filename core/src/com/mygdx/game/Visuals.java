package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

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

    public Visuals(Entity u, Array<BoardPosition> positions, GameTimer timr, Array<VisualEffect> visual, Array<Float> triggerTime) {
        user = u;
        targetPositions = positions;
        timer = timr;
        visuals = visual;
        triggerTimes = triggerTime;
    }

    public void play() {
        if (timer.checkIfFinished())
            isPlaying = false;
        if (isPlaying)
            playVisuals();
    }

    private void playVisuals() {
        if (currentVisual >= visuals.size || currentTime >= triggerTimes.size)
            return;
        if (timer.getTime() >= getNextTargetTime()) {
            visuals.get(currentVisual).doVisuals(user, targetPositions, engine, stage, boardManager);
            currentVisual += 1;
            currentTime += 1;
        }
    }

    private float getNextTargetTime() {
        float target = 0;
        for (int i = 0; i < currentTime; i++) {
            target += triggerTimes.get(currentTime);
        }
        return target;
    }

    public void reset() {
        timer.setTime(0);
        currentTime = 0;
        currentVisual = 0;
    }

    public void updateTimer(float dt) {
        timer.increaseTimer(dt);
    }

    public void setPlaying(boolean startPlaying) {
        isPlaying = startPlaying;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

}
