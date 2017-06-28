package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.rules_types.Team;

import static com.mygdx.game.ComponentMappers.*;

/**
 * Class containing methods that a Computer Player would use.
 *
 * @author Phillip O'Reggio
 */
public /*abstract*/ class ComputerPlayer {
    private BoardManager boards;
    private Team team;

    public ComputerPlayer(BoardManager b, Team t) {
        boards = b;
        team = t;
    }

    private Array<Turn> getAllPossibleTurns(Entity e) {
        //...
        return null;
    }

    private Array<Turn> getBestTurns() {
        for (Entity e : team.getEntities()) {

        }

        return null;
    }

    /**
     * @return Gets a random turn for all entities
     */
    public Array<Turn> getRandomTurns() {
        Array<Turn> turns = new Array<>();

        // Not the best way to do this, but is a good dummy system
        int numTries = 0; //to see if it can use an attack
        int attackChoice = -1;
        for (Entity e : team.getEntities()) {
            if (!stm.get(e).alive) {
                turns.add(null);
                continue;
            }
            //decide if move is valid
            while (numTries < 10) {
                attackChoice = MathUtils.random(0, mvm.get(e).moveList.size - 1);
                if (mvm.get(e).moveList.get(attackChoice).spCost() <= stm.get(e).sp)
                    break;
                else
                    attackChoice = -1;
            }
            turns.add(new Turn(e, getPossiblePositions(bm.get(e).pos, stm.get(e).getModSpd(e), new Array<BoardPosition>(), -1).random(), attackChoice, 0));
        }

        return turns;
    }

    private Array<BoardPosition> getPossiblePositions(BoardPosition bp, int spd, Array<BoardPosition> positions, int directionCameFrom) {
        BoardPosition next = new BoardPosition(-1, -1);

        if (spd == 0)
            return positions;

        for (int i = 0; i < 4; i++) {
            if (directionCameFrom == i) //Already checked tile -> skip!
                continue;

            if (i == 0) //set position
                next.set(bp.r - 1, bp.c);
            else if (i == 1)
                next.set(bp.r, bp.c - 1);
            else if (i == 2)
                next.set(bp.r + 1, bp.c);
            else if (i == 3)
                next.set(bp.r, bp.c + 1);

            //check if valid
            if (next.r >= BoardComponent.boards.getBoard().getRowSize() || next.r < 0
                    || next.c >= BoardComponent.boards.getBoard().getColumnSize() || next.c < 0
                    || BoardComponent.boards.getBoard().getTile(next.r, next.c).isOccupied())
                continue;

            //recursively call other tiles
            positions.add(next.copy());
            getPossiblePositions(next, spd - 1, positions, (i + 2) % 4);
        }

        return positions;
    }

    public int getTeamSize() { return team.getEntities().size; }
}
