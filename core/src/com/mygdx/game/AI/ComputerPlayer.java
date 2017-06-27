package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.rules_types.Team;

import static com.mygdx.game.ComponentMappers.bm;
import static com.mygdx.game.ComponentMappers.mvm;
import static com.mygdx.game.ComponentMappers.stm;

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

    private Turn getBestTurn() {
        return null;
    }

    public Array<Turn> getAllTurns() {
        Array<Turn> turns = new Array<>();

        for (Entity e : team.getEntities()) {
            turns.add(new Turn(e, getPossiblePositions(bm.get(e).pos, stm.get(e).getModSpd(e), new Array<BoardPosition>(), -1).random(), MathUtils.random(-1, mvm.get(e).moveList.size - 1), 0));
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
