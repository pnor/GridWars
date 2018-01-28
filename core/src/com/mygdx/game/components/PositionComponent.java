package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

import java.awt.geom.Point2D;

/**
 * For the position of an Entity on highscores {@code Stage}. Has utility methods such as finding the center.
 * @author pnore_000
 */
public class PositionComponent implements Component {
    /**
     * coordinate
     */
    public Vector2 position;
    public float height, width;
    /**
     * in relation to sprite
     */
    public Vector2 origin;
    public float rotation;

    public PositionComponent(Vector2 pos, float h, float w, float r) {
        position = pos;
        height = h;
        width = w;
        origin = new Vector2(pos.x + w / 2, pos.y + h / 2);
        rotation = r;
    }

    /**
     * @return coordinate of the center (in relation to the entire stage)
     */
    public Vector2 getCenter() {
        return new Vector2(position.x + width, position.y + height);
    }

    /**
     * Sets rotation so it is turned towards highscores coordinate
     * @param point location to look at
     */
    public void lookAt(Point2D.Float point) {
        float direction = (float) Math.atan2(point.y - (position.y + origin.y), point.x - (position.x + origin.x));
        rotation = (float) Math.toDegrees(direction);
    }
}
