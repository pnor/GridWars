package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.boards.BoardPosition;

import static com.mygdx.game.ComponentMappers.nm;

/**
 * @author Phillip O'Reggio
 */
public class Turn{
    /** Entity moving */
    public Entity entity;
    /** New Position on Board */
    public BoardPosition pos;
    /** Which attack from the {@link com.mygdx.game.components.MovesetComponent} it will use*/
    public int attack;
    /** orientation of the attack */
    public int direction;

    /**
     * Creates a Turn Object that is used by BoardState to simulate the effect of a turn on a game state.
     * @param e Entity
     * @param newPosition Position being moved to
     * @param attackNo Which attack being used. -1 means no attack was used that turn
     * @param directionNo what direction the attack is oriented
     */
    public Turn(Entity e, BoardPosition newPosition, int attackNo, int directionNo) {
        entity = e;
        pos = newPosition;
        attack = attackNo;
        direction = directionNo;
    }

    public String toString() {
        return "Turn :\t \n" +
                        "\t Entity : " + nm.get(entity).name + "  || " + entity.toString() + "\n" +
                        "\t BoardPosition : " + pos + "\n" +
                        "\t Attack Index : " + attack +
                        "\t Direction of Attack : " + direction + " (from last attack)";
    }

    public String toStringCondensed() {
        return "Position: " + pos + " || Attack# " + attack + " || Direction: " + direction;
    }
}
