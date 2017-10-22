package com.mygdx.game.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.StringBuilder;

/**
 * Gradually displays text
 * @author Phillip O'Reggio
 */
public class GradualLabel extends Label {
    private float timeBetweenLetters;
    private float currentTime;
    private String targetText;
    private boolean writingText;
    private int currentIndex;

    /**
     * Creates a label that displays its text gradually, character by character.
     * @param time time between letters
     * @param text full text
     * @param skin skin
     */
    public GradualLabel(float time, CharSequence text, Skin skin) {
        super(text, skin);
        timeBetweenLetters = time;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (writingText) {
            currentTime += delta;
            if (currentTime >= timeBetweenLetters) {
                currentTime = 0;
                super.setText(getText().toString() + targetText.charAt(currentIndex));
                currentIndex++;
                if (currentIndex >= targetText.length()) {
                    writingText = false;
                    currentIndex = 0;
                }
            }
        }
    }

    /**
     * Sets the target text, and makes this label begin displaying that text.
     * @param newText New final message
     */
    @Override
    public void setText(CharSequence newText) {
        targetText = newText.toString();
        super.setText("");
        currentTime = 0;
        currentIndex = 0;
        writingText = true;
    }

    /**
     * @return current text in the label (not target text)
     */
    @Override
    public StringBuilder getText() {
        return super.getText();
    }

    /**
     * @return the target text
     */
    public String getTargetText() {
        return targetText;
    }
}
