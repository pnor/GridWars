package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

/**
 * Phillip O
 * What happens when a move is used. Is used mainly in the {@code Move} class.
 */
public interface Attack {
    /**
     * Represents effect the attack has on the stats of an entity
     * @param e user
     * @param bp position of where the effect will take place
     * @param boards {@code BoardManager}
     */
    void effect(Entity e, BoardPosition bp, BoardManager boards);

    public void doVisuals(Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager);

}
