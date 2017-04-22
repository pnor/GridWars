package com.mygdx.game.screens_ui;

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
    SCROLL_VERTICAL(new Vector2(0, -30), 450, 0, null, null),
    SCROLL_HORIZONTAL(new Vector2(-120, 0), 500, 0, null, null),
    ROTATE(null, 0, 30, null, null),
    FADE_COLOR(null, 0, 0, null, null);

    private Entity e;
    private Vector2 movement;
    private float loopPoint;
    private float rotateAmount;
    private Color startColor;
    private Color destinationColor;
    //for interpolate
    private float progress = 0.1f;
    private boolean progressIncreasing;

    BackType(Vector2 move, float loop, float rotate, Color start, Color destination) {
        e = new Entity();
        e.add(new PositionComponent(new Vector2(0,0), 0, 0, 0));

        movement = move;
        loopPoint = loop;
        rotateAmount = rotate;
        startColor = start;
        destinationColor = destination;
    }

    public void update(float deltaTime) {
        switch (this) {
            case SCROLL_VERTICAL:
                pm.get(e).position.add(movement.x * deltaTime, movement.y * deltaTime);
                if (Math.abs(pm.get(e).position.y) > loopPoint)
                    pm.get(e).position.set(pm.get(e).position.x, 0);
                break;

            case SCROLL_HORIZONTAL:
                pm.get(e).position.add(movement.x * deltaTime, movement.y * deltaTime);
                if (Math.abs(pm.get(e).position.x) > loopPoint)
                    pm.get(e).position.set(0, pm.get(e).position.y);
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

    public void setColors(Color start, Color dest) {
        startColor = start;
        destinationColor = dest;
        sm.get(e).sprite.setColor(startColor);
    }

    public Entity getEntity() {
        return e;
    }
}
