package com.mygdx.game.misc;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;

import static com.mygdx.game.ComponentMappers.*;

/**
 * Class containing convenience methods for commonly used {@link GameEvent}s
 *
 * @author Phillip O'Reggio
 */
public class EventCompUtil {
    /**
     * @return {@link GameEvent} that will become more transparent.
     * @param amount of times this needs to be called until it is fully transparent. (note: will not work as well
     *               colors that have a transparency of <1
     */
    public static GameEvent fadeOut(int amount) {
        return (entity, engine) -> {
            if (sm.has(entity)) { //sprite
                Sprite sprite = sm.get(entity).sprite;
                sprite.setColor(
                        sprite.getColor().r,
                        sprite.getColor().g,
                        sprite.getColor().b,
                        MathUtils.clamp(sprite.getColor().a - 1f / amount, 0, 1));
            } else { //animation
                Color color = animm.get(entity).shadeColor;
                color = new Color(
                        color.r,
                        color.g,
                        color.b,
                        MathUtils.clamp(color.a - 1f / amount, 0, 1));
                animm.get(entity).shadeColor = color;
            }
        };
    }

    /**
     * @return {@link GameEvent} that will become more opaque.
     * @param amount of times this needs to be called until it is fully opaque. (note: will not work as well
     *               colors that have a transparency of <1
     */
    public static GameEvent fadeIn(int amount) {
        return (entity, engine) -> {
            if (sm.has(entity)) { //sprite
                Sprite sprite = sm.get(entity).sprite;
                sprite.setColor(
                        sprite.getColor().r,
                        sprite.getColor().g,
                        sprite.getColor().b,
                        MathUtils.clamp(sprite.getColor().a + 1f / amount, 0, 1));
            } else { //animation
                Color color = animm.get(entity).shadeColor;
                color = new Color(
                        color.r,
                        color.g,
                        color.b,
                        MathUtils.clamp(color.a + 1f / amount, 0, 1));
                animm.get(entity).shadeColor = color;
            }
        };
    }

    /**
     * @return {@link GameEvent} that will become more transparent after a set amount of time
     * @param timesUntilFade Amount of times this method will be called until it begins fading
     * @param amount of times this needs to be called until it is fully transparent. (note: will not work as well
     *               colors that have a transparency of <1
     */
    public static GameEvent fadeOutAfter(int timesUntilFade, int amount) {
        return new GameEvent() {
            private int timesCalled;
            @Override
            public void event(Entity e, Engine engine) {
                timesCalled++;
                if (timesCalled >= timesUntilFade) {
                    if (sm.has(e)) { //sprite
                        Sprite sprite = sm.get(e).sprite;
                        sprite.setColor(
                                sprite.getColor().r,
                                sprite.getColor().g,
                                sprite.getColor().b,
                                MathUtils.clamp(sprite.getColor().a - 1f / amount, 0, 1));
                    } else { //animation
                        Color color = animm.get(e).shadeColor;
                        color = new Color(
                                color.r,
                                color.g,
                                color.b,
                                MathUtils.clamp(color.a - 1f / amount, 0, 1));
                        animm.get(e).shadeColor = color;

                    }
                }
            }
        };
    }

    public static GameEvent fadeInThenOut(int amountIn, int space, int amountOut) {
        return new GameEvent() {
            private int timesRun;
            @Override
            public void event(Entity e, Engine engine) {
                timesRun++;

                if (timesRun < amountIn) {
                    if (sm.has(e)) { //sprite
                        Sprite sprite = sm.get(e).sprite;
                        sprite.setColor(
                                sprite.getColor().r,
                                sprite.getColor().g,
                                sprite.getColor().b,
                                MathUtils.clamp(sprite.getColor().a + 1f / amountIn, 0, 1));
                    } else { //animation
                        Color color = animm.get(e).shadeColor;
                        color = new Color(
                                color.r,
                                color.g,
                                color.b,
                                MathUtils.clamp(color.a + 1f / amountIn, 0, 1));
                        animm.get(e).shadeColor = color;
                    }
                } else if (timesRun >= amountIn && timesRun < space) {
                    //nothing
                } else {
                    if (sm.has(e)) { //sprite
                        Sprite sprite = sm.get(e).sprite;
                        sprite.setColor(
                                sprite.getColor().r,
                                sprite.getColor().g,
                                sprite.getColor().b,
                                MathUtils.clamp(sprite.getColor().a - 1f / amountOut, 0, 1));
                    } else { //animation
                        Color color = animm.get(e).shadeColor;
                        color = new Color(
                                color.r,
                                color.g,
                                color.b,
                                MathUtils.clamp(color.a - 1f / amountOut, 0, 1));
                        animm.get(e).shadeColor = color;
                    }
                }
            }
        };
    }

    /**
     * @param amount degrees rotated per method call
     * @return {@link GameEvent} that rotates the entity
     */
    public static GameEvent rotate(float amount) {
        return (entity, engine) -> {
            pm.get(entity).rotation += amount;
        };
    }
}