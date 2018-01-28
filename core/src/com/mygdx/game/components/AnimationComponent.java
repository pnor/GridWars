package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Component for Entities that play an animation. (not to be confused with {@code AnimationActor} which is for actors)
 * @author pnore_000
 */
public class AnimationComponent implements Component {
    public Animation animation;
    public Color shadeColor = Color.WHITE;
    public float currentTime;
    public boolean isAnimating = true;

    private float x;
    private float y;
    private float width;
    private float height;
    private float rotation;

    /**
     * z-index. Determines what things appear on top of others
     */
    public int z;

    /**
     * Creates an {@link AnimationComponent}
     * @param time seconds per frame. Note that the first frame is shown at first at time = 0.
     * @param textureRegions images to show in animation
     * @param playMode how it plays
     */
    public AnimationComponent(float time, TextureRegion[] textureRegions, Animation.PlayMode playMode) {
        animation = new Animation(time, new Array<TextureRegion>(textureRegions), playMode);
        animation.setPlayMode(playMode);
    }

    /**
     * Creates an {@link AnimationComponent} with highscores specified shade color.
     * @param time seconds per frame. Note that the first frame is shown at first at time = 0.
     * @param textureRegions images to show in animation
     * @param color Color that all the frames will be shaded with
     * @param playMode how it plays
     */
    public AnimationComponent(float time, TextureRegion[] textureRegions, Color color, Animation.PlayMode playMode) {
        animation = new Animation(time, new Array<TextureRegion>(textureRegions), playMode);
        shadeColor = color;
        animation.setPlayMode(playMode);
    }

    public void update(float delta) {
        if (isAnimating)
            currentTime += delta;
    }

    public Sprite getSprite() {
        return new Sprite(animation.getKeyFrame(currentTime));
    }

    public void setSpriteLocation(float x2, float y2) {
        x = x2;
        y = y2;
    }

    public void setSpriteRotation(float newDegrees) {
        rotation = newDegrees;
    }

    public void setSpriteSize(float h, float w) {
        height = h;
        width = w;
    }

    public void addSpriteSize(float h, float w) {
        height += h;
        width += w;
    }

    public void draw(Batch batch) {
        Sprite drawSprite = getSprite();
        drawSprite.setPosition(x, y);
        drawSprite.setSize(width, height);
        drawSprite.setOriginCenter();
        drawSprite.setRotation(rotation);
        drawSprite.setColor(shadeColor);

        batch.begin();
        drawSprite.draw(batch);
        batch.end();
    }
}
