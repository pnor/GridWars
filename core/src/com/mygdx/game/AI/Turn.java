package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.boards.BoardPosition;

/**
 * @author Phillip O'Reggio
 */
public class Turn {
    /** Entity moving */
    public Entity entity;
    /** New Position on Board */
    public BoardPosition pos;
    /** Which attack from the {@link com.mygdx.game.components.MovesetComponent} it will use*/
    public int attack;
    /** orientation of the attack */
    public int direction;

    public Turn(Entity e, BoardPosition newPosition, int attackNo, int directionNo) {
        entity = e;
        pos = newPosition;
        attack = attackNo;
        direction = directionNo;
    }
}
