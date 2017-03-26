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

    public HoverButton(String text, Skin skin) {
        super(text, skin);
        this.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hover = true;
                setColor(Color.RED);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hover = false;
                setColor(Color.WHITE);
            }
        });
    }

    public HoverButton(String text, TextButton.TextButtonStyle style) {
        super(text, style);
        this.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hover = true;
                setColor(Color.RED);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hover = false;
                setColor(Color.WHITE);
            }
        });
    }

    public boolean getHover() {
        return hover;
    }

    public void setHover(boolean value) {
        hover = value;
    }
}
