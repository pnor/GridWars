package com.mygdx.game.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * @author pnore_000
 *
 */
public class AnimationActor extends UIActor{
    private Animation animation;
    private Sprite currentFrame;
    private Color shadeColor;
    private float time;

    /**
     * Creates an {@code AnimationActor} with a looping animation.
     * @param duration how long duration will take
     * @param s images shown in animation
     */
    public AnimationActor(float duration, TextureRegion[] s) {
        super(s[0].getRegionWidth(), s[0].getRegionHeight(), true);
        animation = new Animation(duration, new Array<TextureRegion>(s), Animation.PlayMode.LOOP);
    }

    /**
     * Creates an {@code AnimationActor} with an animation.
     * @param duration how long duration will take
     * @param s images shown in animation
     * @param playType how animation will play
     */
    public AnimationActor(float duration, TextureRegion[] s, Animation.PlayMode playType) {
        super(s[0].getRegionWidth(), s[0].getRegionHeight(), true);
        animation = new Animation(duration, new Array<TextureRegion>(s), playType);
    }

    /**
     * Creates an {@code AnimationActor} with an animation that is selectable.
     * @param s images shown in animation
     * @param playType how animation will play
     * @param duration how long duration will take
     */
    public AnimationActor(TextureRegion[] s, Animation.PlayMode playType, float duration) {
        super(true, s[0].getRegionWidth(), s[0].getRegionHeight());
        animation = new Animation(duration, new Array<TextureRegion>(s), playType);
    }

    @Override
    public void shade(Color c) {
        shadeColor = c;
    }

    @Override
    public void act(float delta) { //if this throws null pointer, its currentFrame. Give a defualt(?)
        time += delta;
        if (currentFrame != new Sprite(animation.getKeyFrame(time)))
            currentFrame = new Sprite(animation.getKeyFrame(time));
    }

    @Override
    public Color getColor() {
        return shadeColor;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        currentFrame.setPosition(getX(), getY());
        if (shadeColor != null && currentFrame.getColor() != shadeColor)
            currentFrame.setColor(shadeColor);
        currentFrame.draw(batch, parentAlpha);
    }
}
