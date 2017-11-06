package com.mygdx.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.ui.LerpColor;

/**
 * Actor that displays a static sprite.
 * @author pnore_000
 */
public class SpriteActor extends UIActor{
    private Sprite sprite;
    private LerpColor lerpColor;

    /**
     * Creates a {@code UIActor} with height and width determined by sprite size. Is selectable by default
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
    public SpriteActor(Sprite s, boolean isSelectable) {
        super(s.getWidth(), s.getHeight(), isSelectable);
        sprite = s;
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
        if (tint instanceof LerpColor) { //new color is a lerpColor
            lerpColor = (LerpColor) tint;
        } else if (lerpColor != null) { //changing to a normal color from a lerpColor
            sprite.setColor(tint);
            lerpColor = null;
        } else
            sprite.setColor(tint);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void setSize(float w, float h) {
        super.setSize(h, w);
        sprite.setSize(h, w);
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
    public void draw(Batch batch, float parentAlpha) {
        sprite.setPosition(getX(), getY());
        sprite.setScale(getScaleX());
        if (lerpColor != null)
            sprite.setColor(lerpColor);
        sprite.draw(batch, parentAlpha);
    }
}
