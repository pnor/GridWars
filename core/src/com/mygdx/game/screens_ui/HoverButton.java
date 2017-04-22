package com.mygdx.game.screens_ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Class for buttons that do something when the mouse hovers over them.
 * @author Phillip O'Reggio
 */
public class HoverButton extends TextButton {

    private boolean hover;
    private Color defualtColor = Color.WHITE;
    private Color highlightColor = Color.RED;

    public HoverButton(String text, Skin skin) {
        super(text, skin);
        this.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!isDisabled()) {
                    hover = true;
                    HoverButton.super.setColor(highlightColor);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (!isDisabled()) {
                    hover = false;
                    setColor(defualtColor);
                }
            }
        });
    }

    public HoverButton(String text, TextButton.TextButtonStyle style) {
        super(text, style);
        this.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!isDisabled()) {
                    hover = true;
                    HoverButton.super.setColor(highlightColor);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (!isDisabled()) {
                    hover = false;
                    setColor(defualtColor);
                }
            }
        });
    }

    public HoverButton(String text, Skin skin, Color defualtCol, Color highlightCol) {
        super(text, skin);
        defualtColor = defualtCol;
        super.setColor(defualtCol);
        highlightColor = highlightCol;

        this.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!isDisabled()) {
                    hover = true;
                    HoverButton.super.setColor(highlightColor);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (!isDisabled()) {
                    hover = false;
                    setColor(defualtColor);
                }
            }
        });
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        defualtColor = color;
    }

    public boolean getHover() {
        return hover;
    }

    public void setHover(boolean value) {
        hover = value;
    }
}
