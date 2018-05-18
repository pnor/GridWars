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
public class ComputerPlayer implements Runnable {
    public int DEBUG_TURNS_PROCESSED = 0;
    public long PROCESSING_TIME;

    private boolean processing = false;
    private Array<Turn> decidedTurns;
    private int teamControlled;
    private Array<Array<BoardPosition>> zoneLocations;
    private BoardState currentBoardState;

    private BoardManager boards;
    private Array<Team> teams;
    private Array<EntityTeamPairing> entityTeamPairings;
    private int depthLevel;
    /**
     * Whether it should only get the first attack for all controlled entities on team
     */
    private boolean getFirstAttackAlways;
    private int indexOfFirstAttackingTeams = -1;
    /**
     * Whether it adds something between -1 and 1 to depth each time.
     */
    private boolean randomizeDepthLevel = false;
    /**
     * Should be between 0 and 1 inclusive. Higher values means it has a higher change of not including a best move.
     */
    private float forgetBestMoveChance = 0;

    public ComputerPlayer(BoardManager b, Array<Team> t, int teamIndexControlled, int depth, boolean randomizeDepth, float forgetChance) {
        boards = b;
        teams = t;
        entityTeamPairings = new Array<>();
        for (int i = 0; i < teams.size; i++) {
            for (Entity e : teams.get(i).getEntities()) {
                entityTeamPairings.add(new EntityTeamPairing(e, i));
            }
        }
        teamControlled = teamIndexControlled;
        depthLevel = depth;
        decidedTurns = new Array<>();
        randomizeDepthLevel = randomizeDepth;
        forgetBestMoveChance = forgetChance;
    }

    public ComputerPlayer(BoardManager b, Array<Team> t, Array<Array<BoardPosition>> zones, int teamIndexControlled, int depth, boolean randomizeDepth, float forgetChance) {
        boards = b;
        teams = t;
        zoneLocations = zones;
        teamControlled = teamIndexControlled;
        depthLevel = depth;
        decidedTurns = new Array<>();
        randomizeDepthLevel = randomizeDepth;
        forgetBestMoveChance = forgetChance;

    }

    public void updateComputerPlayer(BoardState board) {
        currentBoardState = board;
    }

    @Override
    public void run() {

        //normal
        /*
        processing = true;
        decidedTurns.clear();
        PROCESSING_TIME = System.nanoTime();
        if (getFirstAttackAlways) {
            decidedTurns = getFirstAttacks(teamControlled);
        } else {
            if (randomizeDepthLevel) {
                int newDepth = depthLevel;
                newDepth = MathUtils.clamp(newDepth + MathUtils.random(-1, 1), 0, 999);
                decidedTurns = getBestTurnsBestTurnAssumption(currentBoardState, teamControlled, newDepth);
            } else
                decidedTurns = getBestTurnsBestTurnAssumption(currentBoardState, teamControlled, depthLevel);
        }
        System.out.println("TIME TO PROCESS: " +  ((float)(System.nanoTime() - PROCESSING_TIME) / 1000000000));
        System.out.println("TURNS PROCESSED : " + DEBUG_TURNS_PROCESSED);
        DEBUG_TURNS_PROCESSED = 0;
        processing = false;
        */

        //EXPERIMENTAL!

        processing = true;
        decidedTurns.clear();
        PROCESSING_TIME = System.nanoTime();
        if (getFirstAttackAlways) {
            decidedTurns = getFirstAttacks(teamControlled);
        } else {
            if (randomizeDepthLevel) {
                int newDepth = depthLevel;
                newDepth = MathUtils.clamp(newDepth + MathUtils.random(-1, 1), 0, 999);
                decidedTurns = expGetBestTurnsMinimax(currentBoardState, teamControlled);
            } else
                decidedTurns = getBestTurnsBestTurnAssumption(currentBoardState, teamControlled, depthLevel);
        }
        System.out.println("TIME TO PROCESS: " +  ((float)(System.nanoTime() - PROCESSING_TIME) / 1000000000));
        System.out.println("TURNS PROCESSED : " + DEBUG_TURNS_PROCESSED);
        DEBUG_TURNS_PROCESSED = 0;
        processing = false;

    }


    /**
     * Gets the best turn by seeing Turn returns the highest heuristic value. Does not use recursion, so it only goes
     * to a depth of 1. (Changes the BoardState object that is passed into it)
     * @return Array of the best turns for each entity.
     */
    public Array<Turn> getBestTurns(BoardState board, int team) {
        Array<Turn> turns = new Array<>();
        EntityValue entityValue = null;
        Entity e;

        for (int i = 0; i < teams.get(team).getEntities().size; i++) {
            e = teams.get(team).getEntities().get(i);

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
            Array<Turn> allTurns = getAllPossibleTurns(e, entityValue, board);
            Turn bestTurn = null;

            for (Turn t : allTurns) {
                curValue = board.copy().tryTurn(t).evaluate(team);
                worstValue = Math.min(curValue, worstValue);
                if (curValue > bestTurnVal) {
                    bestTurnVal = curValue;
                    bestTurn = t;
                }
            }

            turns.add(bestTurn);
            //update BoardState with last Entity's action
            board.tryTurn(bestTurn);
        }

        return turns;
    }

    /**
     * Returns an Array of the best turns for a team of Entities. If an entity is dead, its turn will be null. Ties in heuristic values of turn is
     * drawbreaked by using shallower depth levels.
     * @param board {@link BoardState}
     * @param team {@link Team} that the best turns is getting retrieved
     * @param depth how many turns ahead it looks
     * @return {@link Array} of {@link Turn}s
     */
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
            Turn bestTurn;
            boolean willForget;

            for (Turn t : allTurns) {
                willForget = MathUtils.randomBoolean(forgetBestMoveChance) && bestTurns.size > 0;
                if (willForget)
                    System.out.print("?");
                curValue = bestTurnAssumption(board.copy().tryTurn(t), (team + 1) % teams.size, team, depth);
                worstValue = Math.min(curValue, worstValue);
                if (curValue > bestTurnVal && !willForget) {
                    System.out.print("!!!!");
                    bestTurnVal = curValue;
                    bestTurns.clear();
                    bestTurns.add(t);
                } else if (curValue == bestTurnVal && !willForget) {
                    System.out.print("~!!~");
                    bestTurns.add(t);
                }

                if (willForget && (curValue >bestTurnVal || curValue == bestTurnVal)) //debug
                    System.out.print("????");
                System.out.print(t.toStringCondensed() + ": ");
                System.out.print(curValue + ", " + "\n");
            }

            //resolve ties
            if (bestTurns.size > 1 && depth > 0) {
                System.out.println("Drawbreak");
                System.out.println("bestTurns : ");
                for (Turn t : bestTurns) {
                    System.out.println(t.toStringCondensed());
                }
                bestTurn = drawbreakBestTurns(bestTurns, board, team, depth);
            } else if (depth == 0) {
                bestTurn = bestTurns.random();
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
        //System.out.println(newBoardState);
        for (int i = 0; i <= depthLevel; i++) {
            DEBUG_TURNS_PROCESSED++;
            //change turns
            teamNo = (teamNo + 1) % teams.size;

            //do turn
            if (teamNo == indexOfFirstAttackingTeams) { //if on a team that only uses first attacks
                i -= 1; //do not count in depth deepness
                Array<Turn> firstAttackTurns = getFirstAttacks(teamNo);
                for (Turn t : firstAttackTurns)
                    newBoardState = newBoardState.tryTurn(t);
            }
            newBoardState.doTurnEffects(teamNo);
            getBestTurns(newBoardState, teamNo);
            /*
            System.out.println("A Step of GetBestTurns (Depth : " + i + " / " + depthLevel + ")");
            System.out.println(newBoardState);
            */
        }

        return newBoardState.evaluate(originalTeam);
    }

    public Array<Turn> expGetBestTurnsMinimax(BoardState board, int team) {
        Array<Turn> turns = new Array<>();
        EntityValue entityValue = null;
        Entity e;

        for (int i = 0; i < teams.get(team).getEntities().size; i++) {
            e = teams.get(team).getEntities().get(i);

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
            Array<Turn> allTurns = getAllPossibleTurns(e, entityValue, board);
            Turn bestTurn = null;
            int startIndex = -1;
            for (int j = 0; j < entityTeamPairings.size; j++) {
                if (entityTeamPairings.get(j).entity == teams.get(team).getEntities().get(0))
                    startIndex = j;
            }
            for (Turn t : allTurns) {
                curValue = expGetTurnValMinimax(board.copy().tryTurn(t), team, startIndex, 5, -9999999, 9999999);
                worstValue = Math.min(curValue, worstValue);
                if (curValue > bestTurnVal) {
                    bestTurnVal = curValue;
                    bestTurn = t;
                }
            }

            turns.add(bestTurn);
            //update BoardState with last Entity's action
            board.tryTurn(bestTurn);
        }

        return turns;
    }

    public int expGetTurnValMinimax(BoardState board, int team, int curEntityIndex, int depth, int alpha, int beta) {
        if (depth == depthLevel) { // end of depth
            return board.evaluate(team);
        }

        // process each entity in team
        for (EntityTeamPairing entityTeamPair : entityTeamPairings) {
            //get the entity value
            boolean inBoard = false;
            EntityValue entityValue = null;

            //check alive
            for (EntityValue value : board.getEntities().values()) {
                if (value.checkIdentity(entityTeamPair.entity)) {
                    inBoard = true;
                    entityValue = value;
                    break;
                }
            }
            if (!stm.get(entityTeamPair.entity).alive || !inBoard) { //is alive check
                continue;
            }

            Array<Turn> entityTurns = getAllPossibleTurns(entityTeamPairings.get(curEntityIndex).entity, entityValue, board);
            int bestVal = -99999999;
            for (Turn t : entityTurns) {
                if (team == teamControlled) { // Computer team
                    if (depth > 24) { //get a value
                        int bestTurnVal = -99999999;
                        int curValue = 0;
                        Array<Turn> allTurns = getAllPossibleTurns(entityTeamPair.entity, entityValue, board);
                        Turn bestTurn = null;
                        for (Turn turn : allTurns) {
                            curValue = board.copy().tryTurn(turn).evaluate(team);
                            if (curValue > bestTurnVal) {
                                bestTurnVal = curValue;
                                bestTurn = turn;
                            }
                        }
                        return board.tryTurn(bestTurn).evaluate(team);
                    }
                    bestVal = -9999999;
                    int nextIndex = (curEntityIndex + 1) % (teams.get(0).getEntities().size + teams.get(1).getEntities().size);
                    int value = expGetTurnValMinimax(board.copy().tryTurn(t), entityTeamPairings.get(nextIndex).team, nextIndex, depth + 1, alpha, beta);
                    bestVal = Math.max(value, bestVal);
                    alpha = Math.max(alpha, bestVal);
                    if (beta <= alpha)
                        break;
                    return bestVal;
                } else { // Enemy Team
                    if (depth > 24) { //get a value
                        int bestTurnVal = -99999999;
                        int curValue = 0;
                        Array<Turn> allTurns = getAllPossibleTurns(entityTeamPair.entity, entityValue, board);
                        Turn bestTurn = null;
                        for (Turn turn : allTurns) {
                            curValue = board.copy().tryTurn(turn).evaluate(team);
                            if (curValue > bestTurnVal) {
                                bestTurnVal = curValue;
                                bestTurn = turn;
                            }
                        }
                        return board.tryTurn(bestTurn).evaluate(team);
                    }
                    bestVal = 9999999;
                    int nextIndex = (curEntityIndex + 1) % (teams.get(0).getEntities().size + teams.get(1).getEntities().size);
                    int value = expGetTurnValMinimax(board.copy().tryTurn(t), entityTeamPairings.get(nextIndex).team, nextIndex, depth + 1, alpha, beta);
                    bestVal = Math.min(value, bestVal);
                    beta = Math.min(beta, bestVal);
                    if (beta <= alpha)
                        break;
                    return bestVal;
                }
            }
        }
        return -1;
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
     * Gets the turn that uses the entities first move without rotating.
     * @return Array of the turns that makes entity use first move
     */
    public Array<Turn> getFirstAttacks(int team) {
        Array<Turn> turns = new Array<>();
        Entity e;

        for (int i = 0; i < teams.get(team).getEntities().size; i++) {
            e = teams.get(team).getEntities().get(i);

            Turn waveTurn = null;
            if (mvm.get(e).moveList.first().spCost() > stm.get(e).sp) //not enough sp
                waveTurn = new Turn(e, bm.get(e).pos.copy(), -1, 0);
            else
                waveTurn = new Turn(e, bm.get(e).pos.copy(), 0, 0);

            turns.add(waveTurn);
            System.out.println("First Turn Result: " + waveTurn);
            System.out.println("************");
        }

        return turns;
    }


    /**
     * Retrieves all possible turns an {@link Entity} can make on the board.
     * @param e Entity
     * @return {@link Array} of all possible moves for one Entity
     */
    private Array<Turn> getAllPossibleTurns(Entity e) {
        if (status.has(e) && (status.get(e).contains("Petrify") || status.get(e).contains("Freeze"))) //handle petrify/freeze (non moving conditions)
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
        if (ev.acceptsStatusEffects)
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
        getPositionsSpread(bp, bp, spd, positions, -1, 2, boardState, true);

        //filter out copies horizontal
        filterCopySpaces(positions);

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
                        || boardState.isOccupied(new BoardPosition(next.r, next.c))
                        || BoardComponent.boards.getBoard().getTile(next.r, next.c).isInvisible())
                    continue;
            } else {
                    if (next.r >= BoardComponent.boards.getBoard().getRowSize() || next.r < 0
                            || next.c >= BoardComponent.boards.getBoard().getColumnSize() || next.c < 0
                            || BoardComponent.boards.getBoard().getTile(next.r, next.c).isOccupied()
                            || BoardComponent.boards.getBoard().getTile(next.r, next.c).isInvisible())
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

    public void setTeamControlled(int i) {
        teamControlled = i;
    }

    public void setDepthLevel(int d) {
        depthLevel = d;
    }

    public void setForgetBestMoveChance(float f) {
        forgetBestMoveChance = f;
    }

    public void setGetFirstAttackAlways(boolean b) { getFirstAttackAlways = b; }

    public void setIndexOfFirstAttackingTeams(int i) { indexOfFirstAttackingTeams = i; }
    /**
     * @return turns resulting from AI processing
     */
    public Array<Turn> getDecidedTurns() {
        return decidedTurns;
    }

    public boolean getProcessing() {
        return processing;
    }

    public int getTeamSize() { return teams.get(teamControlled).getEntities().size; }

    public boolean getUsingFirstAttack() {return getFirstAttackAlways; }
    public int getIndexOfFirstAttack() { return indexOfFirstAttackingTeams; }
    public int getTeamControlled() { return teamControlled; }

    /**
     * Small class to group an entity and their team.
     */
    private class EntityTeamPairing {
        public Entity entity;
        public int team;

        public EntityTeamPairing(Entity entity, int team) {
            this.entity = entity;
            this.team = team;
        }
    }
}
