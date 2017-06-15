package com.mygdx.game.move_related;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.BoardPosition;

/**
 * Represents a single visual effect. Used in {@code Visuals} class which plays them all in sequence.
 * @author Phillip O'Reggio
 */
public interface VisualEffect {
    public void doVisuals(Entity user, Array<BoardPosition> targetPositions);
}
