package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

/**
 * Component that controls whether an Entity moves.
 * @author pnore_000
 */
public class MovementComponent implements Component {
    /**
     * Movement and direction in one variable. Moves the amount of the magnitude of the vector per second.
     */
    public Vector2 movement;

    public MovementComponent(Vector2 v) {
        movement = v;
    }

    public MovementComponent() {
    }
}
