package com.mygdx.game;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;

import static com.mygdx.game.ComponentMappers.bm;

/**
 * @author Phillip O
 * Represents an attack used by an entity
 */
public class Move {
    private String name;
    private Attack attack;
    /**
     * Represents effected tiles relative to user's position
     */
    private Array<BoardPosition> range;

    private Entity user;

    private BoardManager boards;
    private Stage stage;
    private Engine engine;

    /**
     * Creates a move that can be used
     * @param name2 name
     * @param usr user of move
     * @param rnge range
     * @param engne {@code Engine}
     * @param stge {@code Stage}
     * @param board {@code BoardManager}
     * @param atk effect of attack
     */
    public Move(String name2, Entity usr, Array<BoardPosition> rnge, Engine engne, Stage stge, BoardManager board, Attack atk) {
        name = name2;
        user = usr;
        range = rnge;
        engine = engne;
        stage = stge;
        boards = board;
        attack  = atk;
    }

    /**
     * Executes the effect of this object
     */
    public void useAttack() {
        for (BoardPosition bp : range) {
            bp = bp.add(bm.get(user).pos.r, bm.get(user).pos.c);
            if (boards.getCodeBoard().get(bp.r, bp.c) == null)
                continue;
            attack.effect(user, bp, boards);
            attack.doVisuals(engine, stage, boards);
        }
    }

    public String getName() {
        return name;
    }

    public Array<BoardPosition> getRange() {
        return range;
    }
}
