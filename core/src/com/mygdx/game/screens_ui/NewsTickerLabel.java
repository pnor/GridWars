package com.mygdx.game.screens_ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Displays text like a news ticker.
 * @author Phillip O'Reggio
 */
public class NewsTickerLabel extends Label {
    private String message;
    private int charactersDisplayed;
    /** time per letter */
    private float tickSpeed;
    private float currentTime;

    private boolean direction = true;
    private float endPauseTime;
    private boolean pausingAtEnd;
    private int currentIndex;

    /**
     * @param skin skin
     * @param displayedMessage full message
     * @param characters amount of characters shown at once
     * @param tick time between letters
     */
    public NewsTickerLabel(Skin skin, String displayedMessage, int characters, float tick, float endPause) {
        super("", skin);
        if (characters <= 0)
            throw new IllegalArgumentException("Characters displayed at once can't be less than or equal to 0");

        message = displayedMessage;
        charactersDisplayed = characters;
        tickSpeed = tick;
        endPauseTime = endPause;
        if (message.length() > charactersDisplayed)
            super.setText(message.substring(0, charactersDisplayed - 1));
        else
            super.setText(message);
    }

    /**
     * @param style label style
     * @param displayedMessage full message
     * @param characters amount of characters shown at once
     * @param tick time between letters
     */
    public NewsTickerLabel(LabelStyle style, String displayedMessage, int characters, float tick, float endPause) {
        super("", style);
        if (characters <= 0)
            throw new IllegalArgumentException("Characters displayed at once can't be less than or equal to 0");

        message = displayedMessage;
        charactersDisplayed = characters;
        tickSpeed = tick;
        endPauseTime = endPause;
        if (message.length() > charactersDisplayed)
            super.setText(message.substring(0, charactersDisplayed - 1));
        else
            super.setText(message);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (message.length() > charactersDisplayed) {
            currentTime += delta;

            if (pausingAtEnd) {
                if (currentTime >= endPauseTime) {
                    pausingAtEnd = false;
                    currentTime = 0;
                    return;
                } else return;
            }

            if (currentTime >= tickSpeed) {
                currentTime = 0;
                if (direction)
                    currentIndex += 1;
                else
                    currentIndex -= 1;
                if (currentIndex + charactersDisplayed == message.length() || currentIndex <= 0) {
                    direction = !direction;
                    pausingAtEnd = true;
                }

                super.setText(message.substring(currentIndex, currentIndex + charactersDisplayed));
            }
        }
    }

    /**
     * Resets the ticker so it begins at the beginning
     */
    public void reset() {
        currentIndex = 0;
        currentTime = 0f;
        direction = true;

        if (message.length() > charactersDisplayed)
            super.setText(message.substring(0, charactersDisplayed - 1));
        else
            super.setText(message);
    }

    /**
     * Sets the message. Makes the News Ticker effect restart.
     * @param newText New message
     */
    @Override
    public void setText(CharSequence newText) {
        if (message.length() > charactersDisplayed)
            super.setText(message.substring(0, charactersDisplayed - 1));
        else
            super.setText(message);

        message = newText.toString();
        reset();
    }

    public void setTickSpeed(float speed) {
        tickSpeed = speed;
        reset();
    }

    public void setCharactersDisplayed(int c) {
        charactersDisplayed = c;
        reset();
    }

    /**
     * @return current text in the label (not target text)
     */
    @Override
    public StringBuilder getText() {
        return super.getText();
    }

    /**
     * @return the full message
     */
    public String getMessage() {
        return message;
    }

}
