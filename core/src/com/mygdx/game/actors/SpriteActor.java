package com.mygdx.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.screens_ui.LerpColor;

/**
 * Actor that displays a static sprite.
 * @author pnore_000
 */
public class SpriteActor extends UIActor{
    private Sprite sprite;
    private LerpColor lerpColor;

    /**
     * Creates a {@code UIActor} with height and width determined by sprite size
     * @param s sprite
     */
    public SpriteActor(Sprite s) {
        super(s.getWidth(), s.getHeight(), true);
        sprite = s;
    }

    /**
     * Creates a {@code UIActor} with height and width determined by sprite size, and can be selected
     * @param s sprite
     */
    public SpriteActor(Sprite s, boolean c, boolean isSelectable) {
        super(c, s.getWidth(), s.getHeight());
        sprite = s;
    }

    /**
     * Creates a {@code UIActor} with height and width determined by sprite size. Can chose whether
     * the sprite centers when moved on board
     * @param s Sprite
     * @param c whether the sprite centers when moved on board
     */
    public SpriteActor(Sprite s, boolean c) {
        super(s.getWidth(), s.getHeight(), c);
        sprite = s;
    }

    /**
     * Creates a {@code UIActor} with specified height and width
     * @param s sprite
     * @param h height
     * @param w width
     */
    public SpriteActor(Sprite s, float h, float w) {
        super(w, h, true);
        sprite = s;
    }

    /**
     * Creates a {@code UIActor} with specified height and width.  Can chose whether
     * the sprite centers when moved on board.
     * @param s sprite
     * @param h height
     * @param w width
     * @param c whether the sprite centers when moved on board
     */
    public SpriteActor(Sprite s, float h, float w, boolean c) {
        super(w, h, c);
        sprite = s;
    }

    /**
     * Creates a {@code UIActor} using a texture region
     * @param tex texture region
     */
    public SpriteActor(TextureRegion tex) {
        super(tex.getRegionWidth(), tex.getRegionHeight(), true);
        sprite = new Sprite(tex);
    }

    /**
     * Creates a {@code UIActor} using a texture region with a set height and width
     * @param tex texture region
     * @param h height
     * @param w width
     */
    public SpriteActor(TextureRegion tex, float h, float w) {
        super(h, w, true);
        sprite = new Sprite(tex);
    }

    @Override
    public void shade(Color tint) {
        if (tint instanceof LerpColor)
            lerpColor = (LerpColor) tint;
        else if (lerpColor != null) {
            sprite.setColor(tint);
            lerpColor = null;
        } else
            sprite.setColor(tint);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (lerpColor != null) {
            lerpColor.update(delta);
        }
    }

    /**
     * @return the sprite. Note that it's NOT a copy!
     */
    public Sprite getSprite() {
        return sprite;
    }

    @Override
    public Color getColor() {
        if (lerpColor != null)
            return lerpColor;
        else
            return sprite.getColor();
    }

    @Override
    public void setColor(Color c) {
        sprite.setColor(c);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        sprite.setPosition(getX(), getY());
        sprite.setScale(getScaleX());
        if (lerpColor != null)
            sprite.setColor(lerpColor);
        sprite.draw(batch, parentAlpha);
    }
}
