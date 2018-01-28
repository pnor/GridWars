package com.mygdx.game.misc;

/**
 * A timer that updates by passing in the delta time.
 * @author Phillip O'Reggio
 */
public class GameTimer {
    private final float endTime;
    private float time = 0;

    /**
     * Constructs highscores {@code GameTimer} with highscores given time.
     * @param t time
     */
    public GameTimer(float t) {
        endTime = t;
    }

    /**
     * Constructs highscores {@code GameTimer} with the starting time of 90.
     */
    public GameTimer() {
        endTime = 3;
    }

    @Override
    public String toString() {
        return "" + (int) (time / 60f) + ":" + String.format("%02d", (int) (time % 60f));
    }

    /**
     * Checks if timer is finished. If the time is less than or equal to 0, returns true. Else returns false.
     * @return
     */
    public boolean checkIfFinished() {
        return (time >= endTime);
    }

    /**
     * Increases the timer by the given time
     * @param dt time between frames
     */
    public void increaseTimer(float dt) {
        time += dt;
    }

    /**
     * Decreases the timer by the given time
     * @param dt time between frames
     */
    public void decreaseTimer(float dt) {
        time -= dt;
    }

    /**
     * Returns the gap between the initial time on the {@code GameTimer} when it was initialized, and its
     * current time.
     * @return The difference between the initialized time and the current time.
     */
    public float getGap() {
        return endTime - time;
    }

    public GameTimer copy() {
        return new GameTimer(time);
    }

    public float getTime() {
        return time;
    }

    public float getEndTime() {
        return endTime;
    }

    public void setTime(float t) {
        time = t;
    }
}
