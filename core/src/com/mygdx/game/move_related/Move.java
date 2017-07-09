package com.mygdx.game.move_related;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.AI.MoveInfo;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.BoardComponent;

import static com.mygdx.game.ComponentMappers.bm;

/**
 * @author Phillip O
 * Represents an attack used by an entity
 */
public class Move {
    private String name;
    private String attackMessage;
    private int spCost;
    private Attack attack;
    private Visuals visuals;
    /**
     * Represents effected tiles relative to user's position
     */
    private Array<BoardPosition> range;

    private Entity user;

    private MoveInfo moveInfo; //for AI

    /**
     * Creates a move that can be used. The move's attack message is displayed when it is used.
     * @param name2 name
     * @param message message that is displayed when move is used. {@code null} will show the default message.
     * @param usr user of move
     * @param cost amount of sp to use the move
     * @param rnge range
     * @param atk effect of attack
     * @param vis visual effect
     */
    public Move(String name2, String message, Entity usr, int cost, Array<BoardPosition> rnge,
                Attack atk, Visuals vis, MoveInfo info) {
        name = name2;
        attackMessage = message;
        user = usr;
        spCost = cost;
        range = rnge;
        attack  = atk;
        visuals = vis;
        moveInfo = info;
    }

    /**
     * Creates a move that can be used. The generic message, "(move name) was used" or "(Entity name) used (move name)", is displayed
     * when used
     *
     * @param name2 name
     * @param usr user of move
     * @param cost amount of sp to use the move
     * @param rnge range
     * @param atk effect of attack
     * @param vis visual effect
     */
    public Move(String name2, Entity usr, int cost, Array<BoardPosition> rnge, Attack atk, Visuals vis, MoveInfo info) {
        name = name2;
        user = usr;
        spCost = cost;
        range = rnge;
        attack  = atk;
        visuals = vis;
        moveInfo = info;
    }

    /**
     * Executes the effect of this object
     */
    public void useAttack() {
        Array<BoardPosition> effectedPositions = new Array<BoardPosition>();
        for (BoardPosition bp : range) {
            bp = bp.add(bm.get(user).pos.r, bm.get(user).pos.c);
            effectedPositions.add(bp.copy());
            try {
                if (BoardComponent.boards.getCodeBoard().get(bp.r, bp.c) == null)
                    continue;
            } catch (IndexOutOfBoundsException e) {
                continue;
            }

            attack.effect(user, bp);
        }
        visuals.setPlaying(true, false);
    }

    /**
     * Changes the effected squares of an attack based on direction. Note that this will change the range of an
     * Attack, not return a copy!
     * @param clockwise whether the range will be spun clockwise(true) or counterclockwise(false)
     * @param move move to be oriented
     */
    public static void orientAttack(boolean clockwise, Move move) {
        Array<BoardPosition> boardPositions = move.getRange();
        if (clockwise) {
            for (BoardPosition bp : boardPositions) {
                //swap r and c
                int temp = bp.r;
                bp.r = bp.c;
                bp.c = -temp;
            }
        } else {
            for (BoardPosition bp : boardPositions) {
                //make negative
                int temp = bp.r;
                bp.r = -bp.c;
                bp.c = temp;
            }
        }
        move.getVisuals().setTargetPositions(move.getRange());

    }

    /**
     * Changes the effected squares of an attack based on direction. Note that this will change the range of an
     * Attack, not return a copy!
     * @param relativeDirection direction that it will be spun.
     *                          <p> 0 : No rotation; same as before
     *                          <p> 1 : 90 Degrees clockwise
     *                          <p> 2 : 180 Degrees
     *                          <p> 0 : 270 Degree clockwise
     *
     * @param move move to be oriented
     */
    public Array<BoardPosition> getOrientedAttackPositions(int relativeDirection, Move move) {
        Array<BoardPosition> boardPositions = new Array<>();
        for (BoardPosition bp : move.getRange())
            boardPositions.add(bp.copy());

        relativeDirection %= 4;
        if (relativeDirection == 0) {
            return boardPositions;
        } else if (relativeDirection == 1) {
            for (BoardPosition bp : boardPositions) {
                //swap r and c
                int temp = bp.r;
                bp.r = bp.c;
                bp.c = -temp;
            }
        } else if (relativeDirection == 2) {
            for (BoardPosition bp : boardPositions) {
                bp.r *= -1;
                bp.c *= -1;
            }
        } else {
            for (BoardPosition bp : boardPositions) {
                int temp = bp.r;
                bp.r = -bp.c;
                bp.c = temp;
            }
        }

        return boardPositions;
    }

    public void updateVisuals(float dt) {
        visuals.updateTimer(dt);
    }

    public int spCost() {
        return spCost;
    }

    public Visuals getVisuals() {
        return visuals;
    }

    public String getName() {
        return name;
    }

    public String getAttackMessage() {
        return attackMessage;
    }

    public Array<BoardPosition> getRange() {
        return range;
    }

    public MoveInfo moveInfo() { return moveInfo;}
}
