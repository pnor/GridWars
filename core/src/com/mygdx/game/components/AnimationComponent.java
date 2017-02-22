package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Component for Entities that play an animation.
 * @author pnore_000
 */
public class AnimationComponent implements Component {
    public Animation animation;
    public Color shadeColor = Color.WHITE;
    private float x;
    private float y;
    public float currentTime;
    /**
     * z-index. Determines what things appear on top of others
     */
    public int z;

    /**
     * Creates an [@code AnimationComponent}
     * @param time time of animation. (0 if you want start)
     * @param textureRegions images to show in animation
     * @param playMode how it plays
     */
    public AnimationComponent(float time, TextureRegion[] textureRegions, Animation.PlayMode playMode) {
        animation = new Animation(time, new Array<TextureRegion>(textureRegions), playMode);
        animation.setPlayMode(playMode);
    }

    public void update(float delta) {
        currentTime += delta;
    }

    public Sprite getSprite() {
        return new Sprite(animation.getKeyFrame(currentTime));
    }

    public void setSpriteLocation(float x2, float y2) {
        x = x2;
        y = y2;
    }

    public void draw(Batch batch) {
        Sprite drawSprite = getSprite();
        drawSprite.setPosition(x, y);
        drawSprite.setColor(shadeColor);

        batch.begin();
        drawSprite.draw(batch);
        batch.end();
    }
}
