package com.mygdx.game.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;


/**
 * @author pnore_000
 * Actor that would be displayed on the game board. Would not contain logic for operations; that is handled
 * by the entity system.
 */
public abstract class UIActor extends Actor {
    private boolean center = true;
    private boolean lastSelected;

    /**
     * Creates an {@code UIActor} with specified width and height
     * @param width width
     * @param height height
     * @param c whether to center when moved on board
     */
    public UIActor(float width, float height, boolean c) {
        center = c;
        setWidth(width);
        setHeight(height);
    }

    /**
     * Creates an {@code UIActor} with specified width and height that is selectable
     * @param c whether to center when moved on board
     * @param width width
     * @param height height
     */
    public UIActor(boolean c, float width, float height) {
        center = c;
        setWidth(width);
        setHeight(height);

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

    /**
     * Sets whether this actor should center itself when added, or moved on the board.
     * @param c whether to center
     */
    public void setShouldCenter(boolean c) {
        center = c;
    }

    public boolean getLastSelected() {
        return lastSelected;
    }

    public void setLastSelected(boolean b) {
        lastSelected = b;
    }

    public abstract void shade(Color tint);
    public abstract Color getColor();
    @Override
    public abstract void draw(Batch batch, float parentAlpha);
}
