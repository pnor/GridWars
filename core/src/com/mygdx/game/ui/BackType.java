package com.mygdx.game.ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.components.PositionComponent;
import com.mygdx.game.components.SpriteComponent;

import static com.mygdx.game.ComponentMappers.pm;
import static com.mygdx.game.ComponentMappers.sm;

/**
 * @author Phillip O'Reggio
 */
public enum BackType {
    NO_MOVE(new Vector2(0, 0), 1000, 0, null, null, 0),
    SCROLL_VERTICAL(new Vector2(0, -30), 900, 0, null, null, 0),
    SCROLL_VERTICAL_SLOW(new Vector2(0, -10), 900, 0, null, null, 0),
    SCROLL_VERTICAL_FAST(new Vector2(0, -45), 900, 0, null, null, 0),
    SCROLL_VERTICAL_FASTER(new Vector2(0, -60), 900, 0, null, null, 0),
    SCROLL_VERTICAL_FASTEST(new Vector2(0, -100), 900, 0, null, null, 0),
    SCROLL_HORIZONTAL(new Vector2(-120, 0), 1000, 0, null, null, 0),
    SCROLL_HORIZONTAL_SLOW(new Vector2(-60, 0), 1000, 0, null, null, 0),
    SCROLL_HORIZONTAL_FAST(new Vector2(-180, 0), 1000, 0, null, null, 0),
    SCROLL_HORIZONTAL_FASTER(new Vector2(-240, 0), 1000, 0, null, null, 0),
    ROTATE(null, 0, 30, null, null, 0),
    FADE_COLOR(null, 0, 0, null, null, 0),
    SCROLL_VERTICAL_HORIZONTAL_SPRITE(new Vector2(0, -30), 1000, 0, null, null, 90);

    private Entity e;
    private Vector2 movement;
    private float loopPoint;
    private float rotateAmount;
    private Color startColor;
    private Color destinationColor;
    //for interpolate
    private float progress = 0.1f;
    private boolean progressIncreasing;

    BackType(Vector2 move, float loop, float rotate, Color start, Color destination, float rotationOfSprite) {
        e = new Entity();
        e.add(new PositionComponent(new Vector2(0,0), 0, 0, rotationOfSprite));

        movement = move;
        loopPoint = loop;
        rotateAmount = rotate;
        startColor = start;
        destinationColor = destination;
    }

    public void update(float deltaTime) {
        switch (this) {
            case SCROLL_VERTICAL:
            case SCROLL_VERTICAL_SLOW:
            case SCROLL_VERTICAL_FAST:
            case SCROLL_VERTICAL_FASTER:
            case SCROLL_VERTICAL_FASTEST:
                pm.get(e).position.add(movement.x * deltaTime, movement.y * deltaTime);
                if (Math.abs(pm.get(e).position.y) > loopPoint)
                    pm.get(e).position.set(pm.get(e).position.x, 0);
                break;
            case SCROLL_HORIZONTAL:
            case SCROLL_HORIZONTAL_SLOW:
            case SCROLL_HORIZONTAL_FAST:
            case SCROLL_HORIZONTAL_FASTER:
                pm.get(e).position.add(movement.x * deltaTime, movement.y * deltaTime);
                if (Math.abs(pm.get(e).position.x) >= loopPoint)
                    pm.get(e).position.set(0, pm.get(e).position.y);
                break;

            case SCROLL_VERTICAL_HORIZONTAL_SPRITE:
                pm.get(e).position.add(movement.x * deltaTime, movement.y * deltaTime);
                if (Math.abs(pm.get(e).position.y) > loopPoint)
                    pm.get(e).position.set(pm.get(e).position.x, 0);
                break;

            case ROTATE:
                pm.get(e).rotation += rotateAmount * deltaTime;
                break;

            case FADE_COLOR:
                if (progress >= 10f)
                    progressIncreasing = false;
                else if (progress <= 0)
                    progressIncreasing = true;

                if (progressIncreasing)
                    progress += deltaTime;
                else
                    progress -= deltaTime;

                sm.get(e).sprite.setColor(new Color(MathUtils.lerp(startColor.r, destinationColor.r, progress / 10f),
                        MathUtils.lerp(startColor.g, destinationColor.g, progress / 10f),
                        MathUtils.lerp(startColor.b, destinationColor.b, progress / 10f),
                        MathUtils.lerp(startColor.a, destinationColor.a, progress / 10f)));
                break;
        }
    }

    public void setImage(Sprite spr) {
        e.add(new SpriteComponent(spr));
        pm.get(e).width = spr.getWidth();
        pm.get(e).height = spr.getHeight();
    }

    public void setEntity(Entity entity) {
        e = entity;
    }

    public void setColors(Color start, Color dest) {
        startColor = start;
        destinationColor = dest;
        sm.get(e).sprite.setColor(startColor);
    }

    public Entity getEntity() {
        return e;
    }
}
