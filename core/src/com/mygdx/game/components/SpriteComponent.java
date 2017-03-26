package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * For the sprite that the entity displays. Not usually used with {@code ActorComponent} since
 * Actors have Sprites
 * @author pnore_000
 */
public class SpriteComponent implements Component{
    public Sprite sprite;
    /**
     * z-index. Determines what things appear on top of others
     */
    public int z;

    public SpriteComponent(TextureRegion tex) {
        sprite = new Sprite(tex);
    }

    public SpriteComponent(Sprite spr) {
        sprite = spr;
    }

    public void draw(Batch batch) {
        batch.begin();
        sprite.draw(batch);
        batch.end();
    }
}
