package com.mygdx.game.move_related;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameTimer;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.screens_ui.BattleScreen;

/**
 * Represents the visual effect that plays when an attack is used.
 * @author Phillip O'Reggio
 */
public class Visuals {
    private BattleScreen battleScreen;
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
    private boolean autoReset;
    public boolean effectsUI;
    /**
     * true when any kind of visuals are playing
     */
    public static boolean visualsArePlaying;

    /**
     * Creates a {@code Visuals} object
     * @param screen {@code BattleScreen}
     * @param u user
     * @param positions effected squares
     * @param timr timer that controls which Visual Effect plays
     * @param visual Array of {@code VisualEffect} objects
     * @param triggerTime Array of trigger times for animations
     * @param effectUI whether this will enable the UI on the {@code BattleScreen}
     */
    public Visuals(BattleScreen screen, Entity u, Array<BoardPosition> positions, GameTimer timr, Array<VisualEffect> visual, Array<Float> triggerTime, boolean effectUI) {
        battleScreen = screen;
        user = u;
        targetPositions = positions;
        timer = timr;
        visuals = visual;
        triggerTimes = triggerTime;
        effectsUI = effectUI;
    }

    /**
     * Checks to see if it is time to play the current {@code VisualEffect}. If it is, it plays it.
     */
    private void playVisuals() {
        if (currentVisual >= visuals.size || currentTime >= triggerTimes.size)
            return;

        if (!Visuals.visualsArePlaying)
            Visuals.visualsArePlaying = true;

        if (timer.getTime() >= getNextTargetTime()) {
            visuals.get(currentVisual).doVisuals(user, targetPositions, engine, stage, boardManager);
            currentVisual += 1;
            currentTime += 1;
        }
    }

    /**
     * Plays the animation (Is called multiple times to play entire thing).
     */
    public void play() {
        if (timer.checkIfFinished()) {
            if (effectsUI)
                battleScreen.enableUI();
            isPlaying = false;
            Visuals.visualsArePlaying = false;
            if (autoReset) {
                autoReset = false;
                reset();
            }
        }
        if (isPlaying)
            playVisuals();
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

    public GameTimer getTimer() {
        return timer;
    }

    /**
     * Sets whether this object is playing or not
     * @param startPlaying whether its playing or not
     * @param autoreset whether it should reset itself after playing
     */
    public void setPlaying(boolean startPlaying, boolean autoreset) {
        isPlaying = startPlaying;
        autoReset = autoreset;
        Visuals.visualsArePlaying = true;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setTargetPositions(Array<BoardPosition> positions) {
        targetPositions = positions;
    }

}
