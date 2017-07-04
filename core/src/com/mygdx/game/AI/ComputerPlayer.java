package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ComponentMappers;
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
    private Array<Team> teams;
    private int teamControlled;
    private int depthLevel;

    public ComputerPlayer(BoardManager b, Array<Team> t, int teamIndexControlled, int depth) {
        boards = b;
        teams = t;
        teamControlled = teamIndexControlled;
        depthLevel = depth;
    }

    /**
     * Gets the best turn by seeing Turn returns the highest heuristic value. Does not use recursion, so it only goes
     * to a depth of 1.
     * @return Array of the best turns for each entity.
     */
    public Array<Turn> getBestTurns(BoardState board, int team) {
        Array<Turn> turns = new Array<>();

        for (Entity e : teams.get(team).getEntities()) {

            //recursion check
            boolean inBoard = false;
            for (EntityValue value : board.getEntities().values()) {
                if (value.checkIdentity(e, teams.get(ComponentMappers.team.get(e).teamNumber)))
                    inBoard = true;
            }

            if (!stm.get(e).alive && inBoard) { //is alive check
                turns.add(null);
                continue;
            }

            int bestTurnVal = -9999999;
            int worstValue = 999;
            int curValue = 0;
            Array<Turn> allTurns = getAllPossibleTurns(e);
            Turn bestTurn = null;

            for (Turn t : allTurns) {
                curValue = board.copy().tryTurn(t).evaluate(team);
                worstValue = Math.min(curValue, worstValue);
                if (curValue > bestTurnVal) {
                    //System.out.print("!");
                    bestTurnVal = curValue;
                    bestTurn = t;
                }
                //System.out.print(curValue + ", ");
            }
            /*
            System.out.println("\nWorst: " + worstValue);
            System.out.println("Best: " + bestTurnVal);
            System.out.println("Best Turn: " + bestTurn);
            System.out.println("-----------------------------------------------");
            */

            turns.add(bestTurn);
            //update BoardState with last Entity's action
            board.tryTurn(bestTurn);
        }

        return turns;
    }

    public Array<Turn> getBestTurnsMinimax(BoardState board, int team, int depth) {
        Array<Turn> turns = new Array<>();
        Array<Entity> entities = teams.get(team).getEntities();

        for (int i = 0; i < entities.size; i++) {
            Entity e = entities.get(i);

            if (!stm.get(e).alive) {
                turns.add(null);
                continue;
            }

            int bestTurnVal = -9999999;
            int worstValue = 999;
            int curValue = 0;
            Array<Turn> allTurns = getAllPossibleTurns(e);
            Turn bestTurn = null;

            for (Turn t : allTurns) {
                curValue = getTurnValueMinimax(t, board, (team + 1) % teams.size, team, depth);
                worstValue = Math.min(curValue, worstValue);
                if (curValue > bestTurnVal) {
                    System.out.print("!!!");
                    bestTurnVal = curValue;
                    bestTurn = t;
                }

                System.out.print(t.toStringCondensed() + ": ");
                System.out.print(curValue + ", " + "\n");
                //System.out.print(curValue + ", ");
            }

            System.out.println("\nWorst: " + worstValue);
            System.out.println("Best: " + bestTurnVal);
            System.out.println("Best Turn: " + bestTurn);
            System.out.println("-----------------------------------------------");


            turns.add(bestTurn);
            //update BoardState with last Entity's action
            board.tryTurn(bestTurn);
        }

        return turns;
    }

    private int getTurnValueMinimax(Turn turn, BoardState board, int teamNo, int originalTeam, int depth) {
        BoardState newBoardState = board.copy().tryTurn(turn);
        Array<Turn> bestTurns = getBestTurns(newBoardState.tryTurn(turn), teamNo);
        if (depth > 0) {
            int best = -999999;
            for (Turn t : bestTurns) {
                if (t == null)
                    continue;
                best = Math.max(best, getTurnValueMinimax(turn, newBoardState.tryTurn(t), (teamNo + 1) % teams.size, originalTeam, depth - 1));
            }
            return best;
        } else {
            for (Turn t : bestTurns) {
                if (t == null)
                    continue;
                newBoardState.tryTurn(t);
            }
            return newBoardState.evaluate(originalTeam);
        }
    }

    public Array<Turn> getBestTurnsAlphaBetaPruning(BoardState board, int team, int depth) {
        Array<Turn> turns = new Array<>();
        Array<Entity> entities = teams.get(team).getEntities();

        for (int i = 0; i < entities.size; i++) {
            Entity e = entities.get(i);

            if (!stm.get(e).alive) {
                turns.add(null);
                continue;
            }

            int bestTurnVal = -9999999;
            int worstValue = 999;
            int curValue = 0;
            Array<Turn> allTurns = getAllPossibleTurns(e);
            Turn bestTurn = null;

            for (Turn t : allTurns) {
                curValue = getTurnValueMinimax(t, board, (team + 1) % teams.size, team, depth);
                worstValue = Math.min(curValue, worstValue);
                if (curValue > bestTurnVal) {
                    System.out.print("!!!");
                    bestTurnVal = curValue;
                    bestTurn = t;
                }
                System.out.print(t.toStringCondensed() + ": ");
                System.out.print(curValue + ", " + "\n");
            }

            System.out.println("\nWorst: " + worstValue);
            System.out.println("Best: " + bestTurnVal);
            System.out.println("Best Turn: " + bestTurn);
            System.out.println("-----------------------------------------------");


            turns.add(bestTurn);
            //update BoardState with last Entity's action
            board.tryTurn(bestTurn);
        }

        return turns;
    }

    private int getTurnValueAlphaBetaPruning(Turn turn, BoardState board, int teamNo, int originalTeam, int alpha, int beta, int depth) {
        BoardState newBoardState = board.copy().tryTurn(turn);
        Array<Turn> bestTurns = getBestTurns(newBoardState.tryTurn(turn), teamNo);
        if (depth > 0) {
            int best = -999999;
            for (Turn t : bestTurns) {
                if (t == null)
                    continue;
                best = Math.max(best, getTurnValueMinimax(turn, newBoardState.tryTurn(t), (teamNo + 1) % teams.size, originalTeam, depth - 1));
            }
            return best;
        } else {
            BoardState newBoard = board.copy();
            for (Turn t : bestTurns) {
                if (t == null)
                    continue;
                newBoard.copy().tryTurn(t);
            }
            return board.evaluate(originalTeam);
        }
    }

    private Array<Turn> getAllPossibleTurns(Entity e) {
        if (status.get(e).statusEffects.containsKey("Petrify") || status.get(e).statusEffects.containsKey("Freeze")) //handle petrify/freeze
            return new Array<Turn>(new Turn[]{new Turn(e, bm.get(e).pos.copy(), -1, 0)});

        Array<Turn> turns = new Array<>();
        Array<BoardPosition> possiblePositions = getPossiblePositions(bm.get(e).pos, stm.get(e).getModSpd(e));
        possiblePositions.add(bm.get(e).pos.copy()); //no movement
        for (BoardPosition pos : possiblePositions) {
            turns.add(new Turn(e, pos, -1, 0)); //no attack
            for (int i = 0; i < mvm.get(e).moveList.size; i++) {
                if (mvm.get(e).moveList.get(i).spCost() > stm.get(e).sp) //if it doesnt have enough sp, skip
                    continue;
                for (int j = 0; j < 4; j++) //All directions of attack
                    turns.add(new Turn(e, pos, i, j));
            }
        }

        return turns;
    }

    /**
     * @return Gets a random turn for all entities
     */
    public Array<Turn> getRandomTurns() {
        Array<Turn> turns = new Array<>();

        // Not the best way to do this, but is a good dummy system
        int numTries = 0; //to see if it can use an attack
        int attackChoice = -1;
        for (Entity e : teams.get(teamControlled).getEntities()) {
            if (!stm.get(e).alive) {
                turns.add(null);
                continue;
            }
            //decide if move is valid
            if (status.has(e) && state.get(e).canAttack) {
                while (numTries < 10) {
                    attackChoice = MathUtils.random(-1, mvm.get(e).moveList.size - 1);
                    if (attackChoice == -1)
                        break;
                    if (mvm.get(e).moveList.get(attackChoice).spCost() <= stm.get(e).sp)
                        break;
                    else
                        attackChoice = -1;
                    numTries++;
                }
            }
            //movement tiles
            Array<BoardPosition> possibleTiles = getPossiblePositions(bm.get(e).pos, stm.get(e).getModSpd(e));
            if (possibleTiles.size == 0 || !state.get(e).canMove)
                turns.add(new Turn(e, bm.get(e).pos, attackChoice, MathUtils.random(0, 3)));
            else
                turns.add(new Turn(e, possibleTiles.random(), attackChoice, MathUtils.random(0, 3)));
        }

        return turns;
    }

    /**
     * Algorithm that returns all positions that can be moved to based on speed. Calls a recursive method. Takes into account barriers and blockades, while
     * avoiding duplicates of the same tile.
     * @param bp Position that is being branched from
     * @param spd remaining tiles the entity can move
     * @return {@link Array} of {@link BoardPosition}s.
     */
    private Array<BoardPosition> getPossiblePositions(BoardPosition bp, int spd) {
        BoardPosition next = new BoardPosition(-1, -1);
        Array<BoardPosition> positions = new Array<>();

        if (spd == 0)
            return positions;

        //get spread of tiles upwards
        getPositionsSpread(bp, spd, positions, -1, 2);
        //get spread of tiles downwards
        getPositionsSpread(bp, spd, positions, -1, 0);

        //fill in remaining line of unfilled spaces
        getPositionsLine(bp, spd, positions, -1, false);

        return positions;
    }

    /**
     * Recursive algorithm that returns all positions in one direction that can be moved to based on speed. Takes into account barriers and blockades.
     * @param bp Position that is being branched from
     * @param spd remaining tiles the entity can move
     * @param positions {@link Array} of tiles that can be moved on
     * @param directionCameFrom direction the previous tile came from. Eliminates the need to check if the next tile is already in the
     *                          {@link Array}.
     *                          <p>-1: No direction(starting)
     *                          <p>0: top
     *                          <p>1: left
     *                          <p>2: bottom
     *                          <p>3: right
     * @param sourceDirection the direction it is branching from. This prevents the "U-Turns" that would overlap with other directions
     *                        <p>-1: No direction(starting)
     *                          <p>0: top
     *                          <p>1: left
     *                          <p>2: bottom
     *                          <p>3: right
     * @return {@link Array} of {@link BoardPosition}s.
     */
    private Array<BoardPosition> getPositionsSpread(BoardPosition bp, int spd, Array<BoardPosition> positions, int directionCameFrom, int sourceDirection) {
        BoardPosition next = new BoardPosition(-1, -1);

        if (spd == 0)
            return positions;

        for (int i = 0; i < 4; i++) {
            if (directionCameFrom == i || sourceDirection == i) //Already checked tile -> skip!
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
            getPositionsSpread(next, spd - 1, positions, (i + 2) % 4, sourceDirection);
        }

        return positions;
    }

    /**
     * Recursive algorithm that returns all tiles in one direction and that are in a line. Takes into account barriers and blockades.
     * @param bp Position that is being branched from
     * @param spd remaining tiles the entity can move
     * @param positions {@link Array} of tiles that can be moved on
     * @param directionCameFrom direction the previous tile came from. Eliminates the need to check if the next tile is already in the
     *                          {@link Array}.
     *                          <p>-1: No direction(starting)
     *                          <p>0: top
     *                          <p>1: left
     *                          <p>2: bottom
     *                          <p>3: right
     * @return {@link Array} of {@link BoardPosition}s.
     */
    private Array<BoardPosition> getPositionsLine(BoardPosition bp, int spd, Array<BoardPosition> positions, int directionCameFrom, boolean vertical) {
        BoardPosition next = new BoardPosition(-1, -1);

        if (spd == 0)
            return positions;

        for (int i = 0; i < 2; i++) {
            if (directionCameFrom == i) //Already checked tile -> skip!
                continue;

            //set position
            if (i == 0) {
                if (vertical)
                    next.set(bp.r - 1, bp.c);
                else
                    next.set(bp.r, bp.c - 1);
            } else if (i == 1) {
                if (vertical)
                    next.set(bp.r + 1, bp.c);
                else
                    next.set(bp.r, bp.c + 1);
            }

            //check if valid
            if (next.r >= BoardComponent.boards.getBoard().getRowSize() || next.r < 0
                    || next.c >= BoardComponent.boards.getBoard().getColumnSize() || next.c < 0
                    || BoardComponent.boards.getBoard().getTile(next.r, next.c).isOccupied())
                continue;

            //recursively call other tiles
            positions.add(next.copy());
            getPositionsLine(next, spd - 1, positions, (i + 1) % 2, vertical);
        }

        return positions;
    }

    public int getTeamSize() { return teams.get(teamControlled).getEntities().size; }

    public int getTeamControlled() { return teamControlled; }
}
