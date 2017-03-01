package com.mygdx.game;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Class for the buttons representing attacks on the battle board.
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
                System.out.println("ENTER   hover : " + hover);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hover = false;
                System.out.println("EXIT   hover : " + hover);
            }
        });
    }

    public HoverButton(String text, TextButton.TextButtonStyle style) {
        super(text, style);
        this.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hover = true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                hover = false;
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
