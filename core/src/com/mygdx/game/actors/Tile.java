package com.mygdx.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import static com.mygdx.game.GridWars.atlas;

/**
 * Representation of highscores single Tile Actor on highscores grid.
 * @author pnore_000
 */
public class Tile extends Group {
    private UIActor tileBack;
    private boolean isDark;
    private boolean usesCustomSprite;
    private Color color = Color.WHITE;
    private int r;
    private int c;

    private boolean lastSelected;
    private boolean isListening;
    private boolean invisible;

    /**
     * Creates highscores new tile with no set color.
     * @param rPos row position in relation to board
     * @param cPos column position in relation to board
     * @param isItDark if true, the tile will be dark. Else, will be lighter
     */
    public Tile(int rPos, int cPos, boolean isItDark, float size) {
        super();
        isDark = isItDark;
        r = rPos;
        c = cPos;

        UIActor tile;
        if (isItDark) {
            Sprite s = atlas.createSprite("DarkTile");
            s.setSize(size, size);
            tile = new SpriteActor(s, false);
        } else {
            Sprite s = atlas.createSprite("LightTile");
            s.setSize(size, size);
            tile = new SpriteActor(s, false);
        }

        this.addActor(tile);
        tileBack = tile;
        setSize(tile.getWidth(), tile.getHeight());
    }

    /**
     * Creates highscores new tile with highscores set color
     * @param rPos row position in relation to board
     * @param cPos column position in relation to board
     * @param isItDark if true, the tile will be dark. Else, will be lighter
     * @param c color of the tile
     * @param size siz of the tile
     */
    public Tile(int rPos, int cPos, boolean isItDark, Color c, float size) {
        super();
        isDark = isItDark;
        color = c;
        this.r = rPos;
        this.c = cPos;

        UIActor tile;
        if (isItDark) {
            Sprite s = atlas.createSprite("DarkTile");
            s.setColor(c);
            s.setSize(size, size);
            tile = new SpriteActor(s, false);
        } else {
            Sprite s = atlas.createSprite("LightTile");
            s.setColor(c);
            s.setSize(size, size);
            tile = new SpriteActor(s, false);
        }
        addActor(tile);
        tileBack = tile;
        setSize(tile.getWidth(), tile.getHeight());
    }

    /**
     * Creates highscores new tile with highscores set color
     * @param rPos row position in relation to board
     * @param cPos column position in relation to board
     * @param isItDark if true, the tile will be dark. Else, will be lighter
     * @param c color of the tile
     * @param spr Tile's sprite
     * @param size siz of the tile
     */
    public Tile(int rPos, int cPos, boolean isItDark, Color c, Sprite spr, float size) {
        super();
        usesCustomSprite = true;
        isDark = isItDark;
        color = c;
        this.r = rPos;
        this.c = cPos;

        UIActor tile;
        Sprite s = spr;
        s.setColor(c);
        s.setSize(size, size);
        tile = new SpriteActor(s, false);
        addActor(tile);
        tileBack = tile;
        setSize(tile.getWidth(), tile.getHeight());
    }

    /**
     * @return whether this has any actors on it. True if it has 1 or more actors and false otherwise.
     */
    public boolean isOccupied() {
        return getChildren().size >= 2;
    }

    /**
     * @return whether the tile is invisible.
     */
    public boolean isInvisible() {
        return invisible;
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
     * Adds highscores listener to the tile back that toggles whether it was last selected
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
     * creates highscores copy of this tile
     * @return copy of this object
     */
    public Tile copy() {
        if (color == null)
            return new Tile(r, c, isDark, getWidth());
        else
            return new Tile(r, c, isDark, color, getWidth());
    }

    /**
     * Creates highscores copy off this tile, with highscores predetermined position
     * @param rPos row position
     * @param cPos column position
     * @return copy
     */
    public Tile copy(int rPos, int cPos) {
        if (usesCustomSprite)
            return new Tile(rPos, cPos, isDark, getChildren().first().getColor(), new Sprite(((SpriteActor) getChildren().first()).getSprite()), getWidth());
        else if (color == null)
            return new Tile(rPos, cPos, isDark, getWidth());
        else
            return new Tile(rPos, cPos, isDark, color, getWidth());
    }

    public void setColor(Color c) {
        color = c;
        tileBack.shade(c);
    }

    public boolean getLastSelected() {
        return lastSelected;
    }

    public void setLastSelected(boolean b) {
        lastSelected = b;
    }

    public void setInvisible(boolean b) {
        invisible = b;
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

    /**
     * @return if the tile shows up on the board
     */
    public boolean getInvisble() {
        return invisible;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!invisible)
            super.draw(batch, parentAlpha);
    }
}
