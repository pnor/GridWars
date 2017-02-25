package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a single visual effect. Used in {@code Visuals} class which plays them all in sequence.
 * @author Phillip O'Reggio
 */
public interface VisualEffect {

    public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager);
}
