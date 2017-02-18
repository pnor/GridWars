package com.mygdx.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import static com.mygdx.game.GridWars.atlas;

/**
 * @author pnore_000
 */
public class Tile extends Group {
    private UIActor tileBack;
    private boolean isDark;
    private Color color;
    private int r;
    private int c;

    private boolean lastSelected;
    private boolean isListening;

    /**
     * Creates a new tile with no set color.
     * @param rPos row position in relation to board
     * @param cPos column position in relation to board
     * @param isItDark if true, the tile will be dark. Else, will be lighter
     */
    public Tile(int rPos, int cPos, boolean isItDark) {
        super();
        isDark = isItDark;
        r = rPos;
        c = cPos;

        UIActor tile;
        if (isItDark)
            tile = new SpriteActor(atlas.createSprite("DarkTile"), false);
        else
            tile = new SpriteActor(atlas.createSprite("LightTile"), false);

        this.addActor(tile);
        tileBack = tile;
        setSize(tile.getWidth(), tile.getHeight());
    }

    /**
     * Creates a new tile with a set color
     * @param rPos row position in relation to board
     * @param cPos column position in relation to board
     * @param isItDark if true, the tile will be dark. Else, will be lighter
     * @param c color of the tile
     */
    public Tile(int rPos, int cPos, boolean isItDark, Color c) {
        super();
        isDark = isItDark;
        color = c;
        r = rPos;
        this.c = cPos;

        UIActor tile;
        if (isItDark) {
            Sprite s = atlas.createSprite("DarkTile");
            s.setColor(c);
            tile = new SpriteActor(s, false);
        } else {
            Sprite s = atlas.createSprite("LightTile");
            s.setColor(c);
            tile = new SpriteActor(s, false);
        }
        addActor(tile);
        tileBack = tile;
        setSize(tile.getWidth(), tile.getHeight());
    }

    /**
     * Colors the tile background of this Tile.
     * @param c Color
     */
    public void shadeTile(Color c) {
       tileBack.shade(c);
    }

    /**
     * Changes the color back to its original color
     */
    public void revertTileColor() {
        tileBack.shade(color);
    }

    /**
     * Adds a listener to the tile back that toggles whether it was last selected
     */
    public void startListening() {
        if (!isListening) {
            tileBack.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    lastSelected = true;
                    return false;
                }
            });
            isListening = true;
        }
    }

    /**
     * Removes all listeners from the tile back
     */
    public void stopListening() {
        tileBack.clearListeners();
        isListening = false;
    }

    /**
     * creates a copy of this tile
     * @return copy of this object
     */
    public Tile copy() {
        if (color == null)
            return new Tile(r, c, isDark);
        else
            return new Tile(r, c, isDark, color);
    }

    /**
     * Creates a copy off this tile, with a predetermined position
     * @param rPos row position
     * @param cPos column position
     * @return copy
     */
    public Tile copy(int rPos, int cPos) {
        if (color == null)
            return new Tile(rPos, cPos, isDark);
        else
            return new Tile(rPos, cPos, isDark, color);
    }

    public boolean getLastSelected() {
        return lastSelected;
    }

    public void setLastSelected(boolean b) {
        lastSelected = b;
    }

    public boolean getIsListening() {
        return isListening;
    }

    /**
     * @return true if dark, false if light
     */
    public boolean getShade() {
        return isDark;
    }

    /**
     * @return the color. Can be null!
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return row of this {@code Tile}
     */
    public int getRow() {
        return r;
    }

    /**
     * @return column of this {@code Tile}
     */
    public int getColumn() {
        return c;
    }
}
