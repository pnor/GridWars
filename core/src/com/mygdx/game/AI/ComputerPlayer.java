package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ComponentMappers;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.misc.Tuple;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.rules_types.Team;

import java.util.Comparator;

import static com.mygdx.game.ComponentMappers.*;

/**
 * Class containing methods that a Computer Player would use.
 *
 * @author Phillip O'Reggio
 */
public class ComputerPlayer implements Runnable {
    public int DEBUG_TURNS_PROCESSED = 0;
    public long PROCESSING_TIME;

    private volatile boolean processing = false;
    /**
     * Represents how far it is in processing computer's turns. Value is always between 0 and 4, with 1 meaning it is
     * processing the first of a team of 4 and 4 meaning it is on the last entity. 0 means it is not processing. If a team has
     * fewer than 4, it will skip 1, 2, etc. and go towards 4.
     */
    private byte progress = 0;
    private Array<Turn> decidedTurns;
    private int teamControlled;
    private Array<Array<BoardPosition>> zoneLocations; /** To win, Entities land on their team's zone */
    private BoardState currentBoardState;

    private BoardManager boards;
    private Array<Team> teams;
    private Array<EntityTeamPairing> entityTeamPairings;
    private int depthLevel;
    private Array<Turn> depthArrayResults;
    /**
     * Whether it should only get the first attack for all controlled entities on team. Typically false, and used for debugging purposes.
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

    /**
     * The Computer Player AI of the game. Comes in 4 difficulties, Easy, Normal, and Hard for normal AI play, and First-Attack for certain special stage props.
     * Uses a minimax algorithm to choose turns for a team. The algorithm is meant to run on a seperate thread, in order to not hang the main thread.
     * @param b Board manager
     * @param t teams
     * @param teamIndexControlled team computer controls
     * @param difficulty difficulty of computer
     */
    public ComputerPlayer(BoardManager b, Array<Team> t, int teamIndexControlled, ComputerPlayer.Difficulty difficulty) {
        boards = b;
        teams = t;
        entityTeamPairings = new Array<>();
        for (int i = 0; i < teams.size; i++) {
            for (Entity e : teams.get(i).getEntities()) {
                entityTeamPairings.add(new EntityTeamPairing(e, i));
            }
        }
        teamControlled = teamIndexControlled;
        decidedTurns = new Array<>();
        setDifficulty(difficulty);
    }

    /**
     * Creates a computer player to play zone match game mode.
     * @param b board manager
     * @param t teams
     * @param zones zones on the board
     * @param teamIndexControlled team computer controls
     * @param difficulty difficulty of computer
     *
     */
    public ComputerPlayer(BoardManager b, Array<Team> t, Array<Array<BoardPosition>> zones, int teamIndexControlled, ComputerPlayer.Difficulty difficulty) {
        boards = b;
        teams = t;
        entityTeamPairings = new Array<>();
        for (int i = 0; i < teams.size; i++) {
            for (Entity e : teams.get(i).getEntities()) {
                entityTeamPairings.add(new EntityTeamPairing(e, i));
            }
        }
        zoneLocations = zones;
        teamControlled = teamIndexControlled;
        decidedTurns = new Array<>();
        setDifficulty(difficulty);
    }

    public void updateComputerPlayer(BoardState board) {
        currentBoardState = board;
    }

    @Override
    public void run() {
        processing = true;
        decidedTurns.clear();
        PROCESSING_TIME = System.nanoTime();

        if (getFirstAttackAlways) {
            decidedTurns = getFirstAttacks(teamControlled);
        } else {
            if (randomizeDepthLevel) {
                int newDepth = depthLevel;
                newDepth = MathUtils.clamp(newDepth + MathUtils.random(-1, 1), 0, 999);
                System.out.println("Processing: atDepth: " + newDepth + ", teamControlled: " + teamControlled);

                decidedTurns = getBestTurnsNegamax(currentBoardState, teamControlled);
            } else {
                System.out.println("Processing: atDepth: " + depthLevel + ", teamControlled: " + teamControlled);
                decidedTurns = getBestTurnsNegamax(currentBoardState, teamControlled);
            }
        }

        //if thread was cancelled, escape method
        if (!processing) {
            //progress = 0;
            System.out.println("\nTHREAD STOPPED.");
            return;
        }

        System.out.println("\nTIME TO PROCESS: " +  ((float)(System.nanoTime() - PROCESSING_TIME) / 1000000000));
        System.out.println("TURNS PROCESSED : " + DEBUG_TURNS_PROCESSED);
        DEBUG_TURNS_PROCESSED = 0;
        processing = false;

    }

    public Array<Turn> getBestTurnsNegamax(BoardState board, int team) {
        Array<Turn> turns = new Array<>();
        EntityValue entityValue = null;
        Entity e;

        for (int i = 0; i < teams.get(team).getEntities().size; i++) {
            progress = (byte) (i + 1 + teams.get(team).getEntities().size - 4);
            e = teams.get(team).getEntities().get(i);

           // if dead, add a null turn
            if (!stm.get(e).alive) {
                turns.add(null);
                continue;
            }
            int bestTurnVal = -99999999;
            int curValue = 0;
            Array<Turn> allTurns = getFilteredPossibleTurns(e);
            //System.out.println("SIZE OF TURNS: " + allTurns.size);
            Turn bestTurn = null;

            // Get index of next entity turn after tested turn
            int startIndex = -1;
            for (int j = 0; j < entityTeamPairings.size; j++) {
                if (entityTeamPairings.get(j).entity == e)
                    startIndex = j;
            }
            startIndex = (startIndex + 1) % entityTeamPairings.size;

            // Get index of processed entity
            int curEntityIndex = teams.get(0).getEntities().size + i;
            System.out.print("Start-----------------------(" + startIndex + ")\n");

            //Arrange turns in order ot best to worst
            Array<Tuple<Integer, Turn>> orderedTurns = new Array<>(allTurns.size);
            for (Turn t : allTurns) {
                orderedTurns.add(new Tuple<Integer, Turn>(board.copy().tryTurn(t).evaluate(team), t));
            }
            orderedTurns.sort(new Comparator<Tuple<Integer, Turn>>() {
                @Override
                public int compare(Tuple<Integer, Turn> o1, Tuple<Integer, Turn> o2) {
                    return o1.value1 - o2.value1;
                }
            });

            // Go Through Each Possible Turn
            boolean gameCloseToEnding = board.isGameCloseToEnding(teams);
            for (Tuple<Integer, Turn> turnValPair : orderedTurns) {
                Turn t = turnValPair.value2;
                //forgot a move -> skip
                if (MathUtils.random() < forgetBestMoveChance) {
                    continue;
                }

                BoardState newBoardState = board.copy().tryTurn(t);
                // If the game is almost done, use a much smaller depth
                if (gameCloseToEnding) {
                    curValue = getTurnValNegamax(newBoardState, team, curEntityIndex, startIndex, depthLevel,
                            depthLevel * (teams.get(teamControlled).getEntities().size / 4), true, -9999999, 9999999);
                    System.out.println("|SMALL DEPTH");
                } else {
                    curValue = getTurnValNegamax(newBoardState, team, curEntityIndex, startIndex, depthLevel,
                            depthLevel * (teams.get(teamControlled).getEntities().size / 2), false, -9999999, 9999999);
                    System.out.println("|FULL DEPTH");
                }
                System.out.print("\ncur : " + curValue);
                if (curValue > bestTurnVal) {
                    bestTurnVal = curValue;
                    bestTurn = t;
                    System.out.print("~");
                }

                // if thread is stopped, cancel and return null
                if (!processing)
                    return null;
            }
            turns.add(bestTurn);
            //System.out.println("~-~-~-~-~-~-~-~-~-~-~-~-");
            //System.out.println("\nAt depth " + depthLevel + " the best value was " + bestTurnVal);
            //System.out.println(bestTurn);
            //System.out.println(bestTurn.showOnBoardStateToString(board));

            //update BoardState with last Entity's action
            if (bestTurn != null)
                board.tryTurn(bestTurn);
        }

        progress = 0;
        return turns;
    }

    /**
     * @param board Current {@link BoardState}
     * @param team index of current team controlled
     * @param processedEntityIndex the entity that is being processed. Should not change in recursive calls. Used to skip rest of team members.
     * @param curEntityIndex current entity being processed
     * @param depth depth level search is at
     * @param endDepth level the search ends at
     * @param skipTeammates whether it will skip teammates when searching at depth levels
     * @param alpha
     * @param beta
     * @return value of a turn evaluated at a given depth using negamax.
     */
    public int getTurnValNegamax(BoardState board, int team, int processedEntityIndex, int curEntityIndex, int depth, int endDepth, boolean skipTeammates, int alpha, int beta) {
        //get the entity value
        boolean inBoard = false;
        EntityValue entityValue = null;
        // get the index of the next entity to be processed. Only processes 1 entity from the home team.
        int nextIndex = (curEntityIndex + 1) % entityTeamPairings.size;
        //region skip members on same team
        //NOTE: Causes stack overflows
        if (skipTeammates) {
            if (entityTeamPairings.get(nextIndex).team == teamControlled) { // moving onto team controlled -> skip to original processed entity
                nextIndex = processedEntityIndex;
            } else if (curEntityIndex == processedEntityIndex) { // was on the processed entity -> skip to next team
                if (teamControlled == 0)
                    nextIndex = teams.get(0).getEntities().size;
                else if (teamControlled == 1) {
                    if (indexOfFirstAttackingTeams != -1) // jump straight to non-attacking team
                        nextIndex = teams.get(0).getEntities().size + teams.get(1).getEntities().size - 1;
                    else // jump straight to first team
                        nextIndex = 0;
                }
            }
        }
        //endregion

        //If depth limit has been approached, get a value
        if (depth > endDepth) {
            return board.evaluate(team);
        }

        // End of Turn Effects
        if (depth > 1) {
            if (curEntityIndex == 0) {
                board.doTurnEffects(0);
            } else if (curEntityIndex == teams.get(0).getEntities().size) { // Start of Second Team Turn
                board.doTurnEffects(1);
            } else if (teams.size == 2 && curEntityIndex == teams.get(0).getEntities().size - 1 + teams.get(1).getEntities().size) { // If they're 3 teams, and start of 3rd team turn
                board.doTurnEffects(2);
            }
        }

        // Total Knockout: If no entities from a team are alive, then don't evaluate turn after that
        if (board.getLastTeamStanding() != -1) {
            if (board.getLastTeamStanding() == teamControlled)
                return 9999999 - depth * 30;
            else if (board.getLastTeamStanding() != teamControlled)
                return -9999999 + depth * 30;
        }

        // Zone Rules: If it's on a zone -> Don't Evaluate Turns after that
        if (zoneLocations != null) {
            for (int i = 0; i < zoneLocations.size; i++) { // i is which team's zones is being processeds
                for (BoardPosition zone : zoneLocations.get(i)) {
                    if (board.getEntities().get(zone) != null) { // Base Win Condition
                        if (teamControlled == i && i == board.getEntities().get(zone).team) { // team Controlled win
                            return board.evaluate(team) + 9000;
                        } else if (teamControlled != i && i == board.getEntities().get(zone).team) { // enemy win
                            //System.out.println("(Player) TeamController: " + teamControlled + " |Entity team: " + board.getEntities().get(zone).team + "  |Zone Team: " + i);
                            return board.evaluate(team) - 9000;
                        }
                    }
                }
            }
        }

        // check alive
        if (board.getEntities().containsKey(entityTeamPairings.get(curEntityIndex).entity)) {
            inBoard = true;
            entityValue = board.getEntities().get(entityTeamPairings.get(curEntityIndex).entity);
        } else { // is dead -> don't do anything and skip
            if (entityTeamPairings.get(nextIndex).team == team) {
                if (!skipTeammates) {
                    return getTurnValNegamax(board.copy(), entityTeamPairings.get(nextIndex).team, processedEntityIndex,
                            nextIndex, depth, endDepth, skipTeammates, alpha, beta);
                } else { // To avoid stack overflow
                    return board.evaluate(team);
                }
            } else
                return -getTurnValNegamax(board.copy(), entityTeamPairings.get(nextIndex).team, processedEntityIndex,
                        nextIndex, depth, endDepth, skipTeammates, -alpha, -beta);
        }

        // First Turn Entities
        if (entityTeamPairings.get(curEntityIndex).team == indexOfFirstAttackingTeams) {
            Turn firstAttackTurn = new Turn(entityTeamPairings.get(curEntityIndex).entity, entityValue.pos, 0, 0);
            if (entityValue.sp >= mvm.get(entityTeamPairings.get(curEntityIndex).entity).moveList.first().spCost())
                return getTurnValNegamax(board.copy().tryTurn(firstAttackTurn), entityTeamPairings.get(nextIndex).team,
                        processedEntityIndex, nextIndex, depth, endDepth, skipTeammates, alpha, beta);
            else
                return -getTurnValNegamax(board.copy(), entityTeamPairings.get(nextIndex).team, processedEntityIndex,
                        nextIndex, depth, endDepth, skipTeammates, alpha, beta);
        }

        // Negamax
        Array<Turn> entityTurns = getFilteredPossibleTurns(entityTeamPairings.get(curEntityIndex).entity, entityValue, board);
        int bestVal = -999999999;
        for (Turn t : entityTurns) {
            DEBUG_TURNS_PROCESSED += 1;
            int value;
            if (entityTeamPairings.get(nextIndex).team == team)
                value = getTurnValNegamax(board.copy().tryTurn(t), entityTeamPairings.get(nextIndex).team,
                        processedEntityIndex, nextIndex, depth + 1, endDepth, skipTeammates, alpha, beta);
            else
                value = -getTurnValNegamax(board.copy().tryTurn(t), entityTeamPairings.get(nextIndex).team,
                        processedEntityIndex, nextIndex, depth + 1, endDepth, skipTeammates, -alpha, -beta);
            bestVal = Math.max(value, bestVal);
            alpha = Math.max(alpha, bestVal);
            if (beta <= alpha) {
                break;
            }
        }
        return bestVal;
    }


    //--------------------
    /**
     * Gets the best turn by seeing Turn returns the highest heuristic value. Does not use recursion, so it only goes
     * to a depth of 1. (Changes the BoardState object that is passed into it)
     * @return Array of the best turns for each entity.
     */
    public Array<Turn> getBestTurns(BoardState board, int team, boolean useForgetChance) {
        Array<Turn> turns = new Array<>();
        EntityValue entityValue = null;
        Entity e;

        for (int i = 0; i < teams.get(team).getEntities().size; i++) {
            e = teams.get(team).getEntities().get(i);

            //recursion check -> if it dies in a later turn
            boolean inBoard = false;
            if (board.getEntities().containsKey(e)) {
                inBoard = true;
                entityValue = board.getEntities().get(e);
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
                if (useForgetChance && MathUtils.random() < forgetBestMoveChance)
                    continue;
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
            //System.out.println("First Turn Result: " + waveTurn);
            //System.out.println("************");
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

        // Handle speed changes from status effects
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
                if (mvm.get(e).moveList.get(i).spCost() > stm.get(e).sp) //if it doesn't have enough sp, skip
                    continue;
                for (int j = 0; j < 4; j++) //All directions of attack
                    turns.add(new Turn(e, pos, i, j));
            }
        }

        return turns;
    }

    /**
     * Filters out turns that are likely to have near identical outcomes to other turns. Things like using a move on an empty space
     * will be removed from the list.
     */
    private Array<Turn> getFilteredPossibleTurns(Entity e, EntityValue ev, BoardState boardState) {
        Array<Turn> allTurns = getAllPossibleTurns(e, ev, boardState);

        for (int i = 0; i < allTurns.size; i++) {
            // Filtering based on Moves that don't hit anything ---
            // Go through all positions a move targets
            if (allTurns.get(i).attack != -1) {
                Move move = mvm.get(allTurns.get(i).entity).moveList.get(allTurns.get(i).attack);
                BoardState afterMovementBoardState = boardState.copy().tryTurnMovementOnly(allTurns.get(i));
                boolean willHitSomething = false;
                for (BoardPosition pos : move.getOrientedAttackPositions(allTurns.get(i).direction, move)) {
                    BoardPosition newPos = pos.add(allTurns.get(i).pos.r, allTurns.get(i).pos.c);
                    if (afterMovementBoardState.getEntities().containsKey(newPos)) {
                        willHitSomething = true;
                        break;
                    }
                }
                //Remove from list
                if (!willHitSomething) {
                    allTurns.removeIndex(i--);
                }
            }
        }

        return allTurns;
    }

    /**
     * Filters out turns that are likely to have near identical outcomes to other turns. Things like using a move on an empty space
     * will be removed from the list. Uses the current board state stored by the ComputerPlayer
     */
    private Array<Turn> getFilteredPossibleTurns(Entity e) {
        Array<Turn> allTurns = getAllPossibleTurns(e);

        for (int i = 0; i < allTurns.size; i++) {
            // Filtering based on Moves that don't hit anything ---
            // Go through all positions a move targets
            if (allTurns.get(i).attack != -1) {
                Move move = mvm.get(allTurns.get(i).entity).moveList.get(allTurns.get(i).attack);
                boolean willHitSomething = false;
                BoardState afterMovementBoardState = currentBoardState.copy().tryTurnMovementOnly(allTurns.get(i));
                for (BoardPosition pos : move.getOrientedAttackPositions(allTurns.get(i).direction, move)) {
                    BoardPosition newPos = pos.add(allTurns.get(i).pos.r, allTurns.get(i).pos.c);
                    if (afterMovementBoardState.getEntities().containsKey(newPos)) {
                        willHitSomething = true;
                        break;
                    }
                }
                //Remove from list
                if (!willHitSomething) {
                    allTurns.removeIndex(i--);
                }
            }
        }
        return allTurns;
    }

    /**
     * @return Gets a random turn for all entities
     */
    public Array<Turn> getRandomTurns() {
        Array<Turn> turns = new Array<>();

        // Not the best way to do this, but is a good dummy //System
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

    /**
     * Stops the thread. happens while it is hap
     */
    public void stopThread() {
        processing = false;
    }

    /**
     * Sets the difficulty level of the computer
     */
    public void setDifficulty(ComputerPlayer.Difficulty difficulty) {
        switch(difficulty) {
            case FIRST_ATTACK:
                setGetFirstAttackAlways(true);
                depthLevel = 0;
                forgetBestMoveChance = 0;
                randomizeDepthLevel = false;
                break;
            case EASY:
                setGetFirstAttackAlways(false);
                depthLevel = 0;
                forgetBestMoveChance = .4f;
                randomizeDepthLevel = false;
                break;
            case NORMAL:
                setGetFirstAttackAlways(false);
                depthLevel = 2;//1;
                forgetBestMoveChance = .1f;//.3f;
                randomizeDepthLevel = false;
                break;
            case HARD:
                setGetFirstAttackAlways(false);
                depthLevel = 3;//2;
                forgetBestMoveChance = .05f;
                randomizeDepthLevel = false;
        }
    }

    public void setTeamControlled(int i) {
        teamControlled = i;
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

    public byte getProgress() { return progress; }

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

        @Override
        public String toString() {
            return nm.get(entity).name + " | " + ComponentMappers.team.get(entity).teamNumber;
        }
    }

    /**
     * Enum for the levels of difficulty the computer has
     */
    public enum Difficulty {
        EASY, NORMAL, HARD, FIRST_ATTACK
    }
}
