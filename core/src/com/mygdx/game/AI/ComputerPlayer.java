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
    public int DEBUG_TURNS_PROCESSED = 0;

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
        EntityValue entityValue = null;

        for (Entity e : teams.get(team).getEntities()) {
            //System.out.println("+++++++++++++++++++++++++ Next Entity in getBestTurns +++++++++++++++++++++++++++++ ******************************");

            //recursion check -> if it dies in a later turn
            boolean inBoard = false;
            for (EntityValue value : board.getEntities().values()) {
                if (value.checkIdentity(e)) {
                    inBoard = true;
                    entityValue = value;
                    break;
                }
            }

            if (!stm.get(e).alive || !inBoard) { //is alive check
                turns.add(null);
                continue;
            }

            int bestTurnVal = -99999999;
            int worstValue = 99999999;
            int curValue = 0;
            //Array<Turn> allTurns = getAllPossibleTurns(e);
            Array<Turn> allTurns = getAllPossibleTurns(e, entityValue, board);
            Turn bestTurn = null;

            for (Turn t : allTurns) {
                curValue = board.copy().tryTurn(t).evaluate(team);
                worstValue = Math.min(curValue, worstValue);
                if (curValue > bestTurnVal) {
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

    /**
     * Retrieves the best turns for an entire team by using a minimax algorithm to look ahead several turns.
     * @param board {@link BoardState} of the current board
     * @param team team that is having turns generated for
     * @param depth amount of turns deep  it goes. Should not be <= 0! 1 represents 1 turn deep, so the team's turn, then the enemy's likely turn.
     * @return {@link Array} of the best turns for the team
     */
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

    /**
     * Ranks each turn with an integer. Each turn is explored several turns deep, bsaed on the value of depth using a minimax algorithm.
     * @param turn Turn being ranked
     * @param board current {@link BoardState}
     * @param teamNo Which team's turn is being processed
     * @param originalTeam team of the stem turn.
     * @param depth amount of turns deep  it goes. Should not be <= 0! 1 represents 1 turn deep, so the team's turn, then the enemy's likely turn.
     * @return integer ranking of the turn
     */
    private int getTurnValueMinimax(Turn turn, BoardState board, int teamNo, int originalTeam, int depth) {
        BoardState newBoardState = board.copy().tryTurn(turn);
        Array<Turn> bestTurns = getBestTurns(newBoardState.tryTurn(turn), teamNo);
        if (depth > 0) {
            int best = -999999;
            for (Turn t : bestTurns) {
                DEBUG_TURNS_PROCESSED++;

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

    /**
     * Retrieves the best turns for an entire team by using a alpha-beta pruning algorithm to look ahead several turns.
     * @param board {@link BoardState} of the current board
     * @param team team that is having turns generated for
     * @param depth amount of turns deep  it goes. Should not be <= 0! 1 represents 1 turn deep, so the team's turn, then the enemy's likely turn.
     * @return {@link Array} of the best turns for the team
     */
    public Array<Turn> getBestTurnsAlphaBetaPruning(BoardState board, int team, int depth) {
        Array<Turn> turns = new Array<>();
        Array<Entity> entities = teams.get(team).getEntities();

        for (int i = 0; i < entities.size; i++) {
            Entity e = entities.get(i);

            if (!stm.get(e).alive) { //is alive check
                turns.add(null);
                continue;
            }

            int bestTurnVal = -9999999;
            int worstValue = 9999999;
            int curValue = 0;
            Array<Turn> allTurns = getAllPossibleTurns(e);
            Turn bestTurn = null;

            for (Turn t : allTurns) {
                curValue = getTurnValueAlphaBetaPruning(board.copy().tryTurn(t), (team + 1) % teams.size, team, false, -99999, 99999, depth);
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

    /**
     * Ranks each turn with an integer. Each turn is explored several turns deep, bsaed on the value of depth using a minimax algorithm.
     * @param board current {@link BoardState}
     * @param teamNo Which team's turn is being processed
     * @param originalTeam team of the stem turn.
     * @param alpha max value used for alpha-beta pruning
     * @param beta min value used for alpha-beta pruning
     * @param maximising whether it trying to get the highest possible value or the lowest possible value
     * @param depth amount of turns deep  it goes. Should not be <= 0! 1 represents 1 turn deep, so the team's turn, then the enemy's likely turn.
     * @return integer ranking of the turn
     */
    private int getTurnValueAlphaBetaPruning(BoardState board, int teamNo, int originalTeam, boolean maximising, int alpha, int beta, int depth) {
        BoardState newBoardState = board.copy();
        newBoardState.doTurnEffects(teamNo);
        Array<Turn> bestTurns = getBestTurns(newBoardState.copy(), teamNo);
        int best;
        if (maximising) {
            if (depth > 0) {
                best = -999999;
                for (Turn t : bestTurns) {
                    DEBUG_TURNS_PROCESSED++;

                    if (t == null)
                        continue;
                    best = Math.max(best, getTurnValueAlphaBetaPruning(newBoardState.tryTurn(t), (teamNo + 1) % teams.size, originalTeam, !maximising, alpha, beta, depth - 1));
                    alpha = Math.max(best, alpha);
                    if (beta <= alpha)
                        break;
                }
                return best;
            } else {
                for (Turn t : bestTurns) {
                    if (t == null)
                        continue;
                    newBoardState.tryTurn(t);
                }
                return board.evaluate(originalTeam);
            }
        } else {
            if (depth > 0) {
                best = 999999;
                for (Turn t : bestTurns) {
                    DEBUG_TURNS_PROCESSED++;

                    if (t == null)
                        continue;
                    best = Math.min(best, getTurnValueAlphaBetaPruning(newBoardState.tryTurn(t), (teamNo + 1) % teams.size, originalTeam, !maximising, alpha, beta, depth - 1));
                    beta = Math.min(best, alpha);
                    if (beta <= alpha)
                        break;
                }
                return best;
            } else {
                for (Turn t : bestTurns) {
                    if (t == null)
                        continue;
                    newBoardState.tryTurn(t);
                }
                return board.evaluate(teamNo);
            }
        }
    }

    public Array<Turn> getBestTurnsBestTurnAssumption(BoardState board, int team, int depth) {
        Array<Turn> turns = new Array<>();
        Array<Entity> entities = teams.get(team).getEntities();

        for (int i = 0; i < entities.size; i++) {
            Entity e = entities.get(i);

            if (!stm.get(e).alive) { //is alive check
                turns.add(null);
                continue;
            }

            int bestTurnVal = -9999999;
            int worstValue = 9999999;
            int curValue = 0;
            Array<Turn> allTurns = getAllPossibleTurns(e);
            Array<Turn> bestTurns = new Array<>(); //for possible ties
            Turn bestTurn = null;

            for (Turn t : allTurns) {
                curValue = bestTurnAssumption(board.copy().tryTurn(t), (team + 1) % teams.size, team, depth);
                worstValue = Math.min(curValue, worstValue);
                if (curValue > bestTurnVal) {
                    System.out.print("!!!!");
                    bestTurnVal = curValue;
                    bestTurns.clear();
                    bestTurns.add(t);
                } else if (curValue == bestTurnVal) {
                    System.out.print("~!!~");
                    bestTurns.add(t);
                }

                System.out.print(t.toStringCondensed() + ": ");
                System.out.print(curValue + ", " + "\n");
            }

            //resolve ties
            if (bestTurns.size > 1) {
                System.out.println("Drawbreak");
                System.out.println("bestTurns : ");
                for (Turn t : bestTurns) {
                    System.out.println(t.toStringCondensed());
                }
                bestTurn = drawbreakBestTurns(bestTurns, board, team, depth);
            } else {
                bestTurn = bestTurns.first();
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

    private int bestTurnAssumption(BoardState board, int teamNo, int originalTeam, int depthLevel) {
        BoardState newBoardState = board.copy();
        for (int i = 0; i < depthLevel; i++) {
            DEBUG_TURNS_PROCESSED++;
            //change turns
            teamNo = (teamNo + 1) % teams.size;

            //do turn
            newBoardState.doTurnEffects(teamNo);
            getBestTurns(newBoardState, teamNo);
        }

        return newBoardState.evaluate(originalTeam);
    }

    /**
     * Resolves a tie in the value of multiple turns by comparing their values at lower depth levels. If there is still tied turns, then it will randomly return
     * one of the multiple turns.
     * @param bestTurns Turns that are tied for the best value
     * @param boardState board state
     * @param team of entity
     * @param depthLevel depth level that was used before that resulted in a tie. (Not the depth that will be first used!)
     * @return the best Turn
     */
    private Turn drawbreakBestTurns(Array<Turn> bestTurns, BoardState boardState, int team, int depthLevel) {
        int bestTurnVal = -9999999;
        int curValue = 0;
        Array<Turn> currentBestTurns = bestTurns;
        Array<Turn> newBestTurns = new Array<>();

        for (int i = depthLevel - 1; i >= 0; i--) {
            for (Turn t : currentBestTurns) {
                curValue = bestTurnAssumption(boardState.copy().tryTurn(t), (team + 1) % teams.size, team, i);

                System.out.print("\n(DrawBreak) : " + t.toStringCondensed() + " = " + curValue);

                if (curValue > bestTurnVal) {
                    newBestTurns.clear();
                    newBestTurns.add(t);
                    bestTurnVal = curValue;
                    System.out.print("!!!");
                } else if (curValue == bestTurnVal) {
                    newBestTurns.add(t);
                    System.out.print("~!~");

                }
            }

            if (newBestTurns.size == 1) {
                System.out.println("\nDrawbreak best Turn : " + newBestTurns.first().toStringCondensed() + "= " + bestTurnVal);
                System.out.println("Broke the tie at depth of " + i);
                return newBestTurns.first();
            } else if (i != 0) {
                bestTurnVal = -9999999;
                curValue = 0;
                currentBestTurns = new Array<Turn>(newBestTurns.toArray());
                newBestTurns.clear();
                System.out.print("\n~~~ i = " + i + " ~~~");
            }
        }

        //still a tie
        System.out.println("\nFull Tie !");
        return newBestTurns.random();
    }


    /**
     * Retrieves all possible turns an {@link Entity} can make on the board.
     * @param e Entity
     * @return {@link Array} of all possible moves for one Entity
     */
    private Array<Turn> getAllPossibleTurns(Entity e) {
        if (status.get(e).statusEffects.containsKey("Petrify") || status.get(e).statusEffects.containsKey("Freeze")) //handle petrify/freeze
            return new Array<Turn>(new Turn[]{new Turn(e, bm.get(e).pos.copy(), -1, 0)});

        Array<Turn> turns = new Array<>();
        Array<BoardPosition> possiblePositions = getPossiblePositions(bm.get(e).pos, stm.get(e).getModSpd(e), null);
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
     * Retrieves all possible turns an {@link Entity} can make on the board.
     * @param e Entity. Used for Moves from moveset
     * @param ev EntityValue. Used for getting stats affected by Statuses, etc.
     * @return {@link Array} of all possible moves for one Entity
     */
    private Array<Turn> getAllPossibleTurns(Entity e, EntityValue ev, BoardState boardState) {
        boolean hasNonMovingStatus = false;
        for (StatusEffectInfo s : ev.statusEffectInfos)
            if (s.name.equals("Petrify") || s.name.equals("Freeze"))
                hasNonMovingStatus = true;

        if (hasNonMovingStatus) //handle petrify/freeze
            return new Array<Turn>(new Turn[]{new Turn(e, bm.get(e).pos.copy(), -1, 0)});

        Array<Turn> turns = new Array<>();
        int speedVal = stm.get(e).spd;
        if (ev.statusEffectInfos != null) {
            for (StatusEffectInfo status : ev.statusEffectInfos) {
                if (status.statChanges == null) continue;
                speedVal = (int) (speedVal * status.statChanges.spd);
            }
        }

        Array<BoardPosition> possiblePositions = getPossiblePositions(ev.pos, speedVal, boardState);
        possiblePositions.add(ev.pos.copy()); //no movement

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
            Array<BoardPosition> possibleTiles = getPossiblePositions(bm.get(e).pos, stm.get(e).getModSpd(e), null);
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
     * @param boardState Used to check if a space is occupied. If this value is null, will use the {@link com.mygdx.game.boards.Board} from
     *                   {@link BoardComponent}.
     * @return {@link Array} of {@link BoardPosition}s.
     */
    private Array<BoardPosition> getPossiblePositions(BoardPosition bp, int spd, BoardState boardState) {
        BoardPosition next = new BoardPosition(-1, -1);
        Array<BoardPosition> positions = new Array<>();


        if (spd == 0)
            return positions;

        //get spread of tiles upwards
        getPositionsSpread(bp, bp, spd, positions, -1, 0, boardState, true);

        //get spread of tiles downwards
        getPositionsSpread(bp, bp, spd, positions, -1, 2, boardState, false);

        return positions;
    }

    /**
     * Recursive algorithm that returns all positions in one direction that can be moved to based on speed. Takes into account barriers and blockades.
     * @param sourceBp the original location being branched from
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
     * @param boardState Used to check if a space is occupied. If this value is null, will use the {@link com.mygdx.game.boards.Board} from
     *                   {@link BoardComponent}.
     * @param includeHorizontal Whether it includes spaces directly horizontal of the origin position
     * @return {@link Array} of {@link BoardPosition}s.
     */
    private Array<BoardPosition> getPositionsSpread(BoardPosition sourceBp, BoardPosition bp, int spd, Array<BoardPosition> positions, int directionCameFrom, int sourceDirection, BoardState boardState, boolean includeHorizontal) {
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
            if (boardState != null) {
                if (next.r >= BoardComponent.boards.getBoard().getRowSize() || next.r < 0
                        || next.c >= BoardComponent.boards.getBoard().getColumnSize() || next.c < 0
                        || boardState.isOccupied(new BoardPosition(next.r, next.c)))
                    continue;
            } else {
                    if (next.r >= BoardComponent.boards.getBoard().getRowSize() || next.r < 0
                            || next.c >= BoardComponent.boards.getBoard().getColumnSize() || next.c < 0
                            || BoardComponent.boards.getBoard().getTile(next.r, next.c).isOccupied())
                        continue;
            }
            if (!includeHorizontal && next.r == sourceBp.r)
                continue;

            //recursively call other tiles
            positions.add(next.copy());
            getPositionsSpread(sourceBp, next, spd - 1, positions, (i + 2) % 4, sourceDirection, boardState, includeHorizontal);
        }

        return positions;
    }

    /**
     * Filters duplicates in an Array of positions. Will filter any duplicates of any kind.
     * @param positions Array being filtered.
     */
    public void filterCopySpaces(Array<BoardPosition> positions) {
        for (int i = 0; i < positions.size; i++) {
            for (int j = 0; j < positions.size; j++) {
                if (i == j)
                    continue;
                if (positions.get(i).equals(positions.get(j))) {
                    positions.removeIndex(j);
                    j--;
                }
            }
        }
    }

    public int getTeamSize() { return teams.get(teamControlled).getEntities().size; }

    public int getTeamControlled() { return teamControlled; }
}
