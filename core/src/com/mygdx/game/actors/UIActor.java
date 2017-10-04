package com.mygdx.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;


/**
 * @author pnore_000
 * Actor that would be displayed on the game board. Would not contain logic for operations; that is handled
 * by the entity system.
 */
public abstract class UIActor extends Actor {
    private boolean lastSelected;

    /**
     * Creates an {@code UIActor} with specified width and height
     * @param width width
     * @param height height
     * @param selectable is selectable
     */
    public UIActor(float width, float height, boolean selectable) {
        setWidth(width);
        setHeight(height);

        if (selectable)
            this.addListener(new InputListener() {
                public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                    lastSelected = true;
                    return false;
                }
            });
    }

    /**
     * Centers this so it fits in the middle of the tile
     */
    public void center() {
        setPosition((getParent().getWidth() - getWidth()) / 2, (getParent().getHeight() - getHeight()) / 2);
    }

    public boolean getLastSelected() {
        return lastSelected;
    }

    public void setLastSelected(boolean b) {
        lastSelected = b;
    }

    public abstract void shade(Color tint);
    public abstract Color getColor();
    /**
     * @return Copy of this actor's sprite.
     */
    public abstract Sprite getSprite();

    @Override
    public abstract void draw(Batch batch, float parentAlpha);
}
