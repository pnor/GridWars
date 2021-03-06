package com.mygdx.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Actor that displays an animation.
 * @author pnore_000
 *
 */
public class AnimationActor extends UIActor {
    private Animation<TextureRegion> animation;
    private Sprite currentFrame;
    private Color shadeColor;
    private Vector2 size;
    private float time;
    private boolean stopUpdating;

    /**
     * Creates an {@code AnimationActor} with a looping animation. (is selectable)
     *
     * @param duration how long duration will take
     * @param s        images shown in animation
     */
    public AnimationActor(float duration, TextureRegion[] s) {
        super(s[0].getRegionWidth(), s[0].getRegionHeight(), true);
        animation = new Animation<>(duration, new Array<TextureRegion>(s), Animation.PlayMode.LOOP);
        currentFrame = new Sprite(animation.getKeyFrame(0));
    }

    /**
     * Creates an {@code AnimationActor} with an animation that is selectable.
     *
     * @param s        images shown in animation
     * @param playType how animation will play
     * @param duration how long duration will take
     * @param selectable whether it is selectable
     */
    public AnimationActor(TextureRegion[] s, Animation.PlayMode playType, float duration, boolean selectable) {
        super(s[0].getRegionWidth(), s[0].getRegionHeight(), selectable);
        animation = new Animation<>(duration, new Array<TextureRegion>(s), playType);
        currentFrame = new Sprite(animation.getKeyFrame(0));
    }

    @Override
    public void act(float delta) { //if this throws null pointer, its currentFrame. Give a defualt(?)
        if (!stopUpdating) {
            time += delta;
            if (currentFrame != new Sprite(animation.getKeyFrame(time)))
                currentFrame = new Sprite(animation.getKeyFrame(time));
        }
    }

    @Override
    public void setSize(float w, float h) {
        super.setSize(h, w);
        size = new Vector2(h, w);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        currentFrame.setPosition(getX(), getY());
        currentFrame.setScale(getScaleX());
        if (shadeColor != null && currentFrame.getColor() != shadeColor)
            currentFrame.setColor(shadeColor);
        if (size != null)
            currentFrame.setSize(size.x, size.y);
        currentFrame.draw(batch, parentAlpha);
    }

    @Override
    /**
     * If uninitialized, this will return Color.WHITE
     */
    public Color getColor() {
        if (shadeColor != null)
            return shadeColor;
        else
            return Color.WHITE;
    }

    /**
     * @return New {@link Sprite} representing the animation at time 0 seconds.
     */
    @Override
    public Sprite getSprite() {
        return new Sprite(animation.getKeyFrame(0));
    }

    public Sprite getCurrentFrame() {
        return currentFrame;
    }

    public Sprite getInitialFrame() {
        return new Sprite(animation.getKeyFrame(0));
    }

    public boolean getStopUpdating() {
        return stopUpdating;
    }

    public void setStopUpdating(boolean s) {
        stopUpdating = s;
    }

    @Override
    public void shade(Color c) {
        shadeColor = c;
    }
}
