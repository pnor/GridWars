package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.boards.BoardPosition;

import static com.mygdx.game.ComponentMappers.*;

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

    public String showOnBoardStateToString(BoardState boardState) {
        //region unchanged from BoardState
        final Array<BoardPosition> POSITIONS = boardState.getEntities().getAllPositions(); // all positions
        final Array<BoardPosition> ZONES = new Array<>();

        if (boardState.getZones() != null) {
            for (int i = 0; i < boardState.getZones().get(0).size; i++)
                ZONES.add(boardState.getZones().get(0).get(i));
            for (int i = 0; i < boardState.getZones().get(1).size; i++)
                ZONES.add(boardState.getZones().get(1).get(i));
        }


        // get assumed dimensions of board
        int largestRowSize = -1;
        for (int i = 0; i < POSITIONS.size; i++) { // iterate over positions of entities
            largestRowSize = Math.max(POSITIONS.get(i).r, largestRowSize);
        }
        if (boardState.getZones() != null) {
            for (int i = 0; i < ZONES.size; i++) { // iterate over zone locations
                largestRowSize = Math.max(ZONES.get(i).r, largestRowSize);
            }
        }
        final int ASSUMED_ROW_SIZE = largestRowSize;

        int largestColSize = -1;
        for (int i = 0; i < POSITIONS.size; i++) { // iterate over positions of entities
            largestColSize = Math.max(POSITIONS.get(i).c, largestColSize);
        }
        if (boardState.getZones() != null) {
            for (int i = 0; i < ZONES.size; i++) { // iterate over positions of entities
                largestColSize = Math.max(ZONES.get(i).c, largestColSize);
            }
        }
        final int ASSUMED_COL_SIZE = largestColSize;

        StringBuilder outputString = new StringBuilder();

        // print row header
        outputString.append("    "); // 4 spaces
        for (int i = 0; i <= ASSUMED_COL_SIZE; i++) {
            outputString.append(i + " "); // Col Numbers
        }
        outputString.append("\n");
        //endregion

        // listed in order of display priority form lowest (top) to highest (bottom)
        final char MOVE_HIT_EMPTY = 'M';
        final char MOVE_HIT_TEAMMATE = 'T';
        final char MOVE_HIT_ENEMY = 'E';
        final char MOVE_HIT_OBJECT = 'Z';
        final char OBJECT_ICON = 'X';
        final char TEAM_2_ICON = '2';
        final char TEAM_0_ICON = '0';
        final char TEAM_1_ICON = '1';
        final char ZONE_ICON = 'Z';



        // get spaces affected by zones
        Array<BoardPosition> spacesMoveHits = null;
        if (attack != -1) {
            spacesMoveHits = mvm.get(entity).moveList.get(attack).getOrientedAttackPositions(direction, mvm.get(entity).moveList.get(attack));
            for (int i = 0; i < spacesMoveHits.size; i++) {
                spacesMoveHits.set(i, spacesMoveHits.get(i).add(pos.r, pos.c));
            }
        }

        // print board (With Moves Spaces Highlighted
        for (int i = 0; i <= ASSUMED_ROW_SIZE; i++) {
            outputString.append(" " + i + " |");
            for (int j = 0; j <= ASSUMED_COL_SIZE; j++) {
                char entityZoneChar = '?'; //char representing whats going on that space
                //region get what char to display
                BoardPosition curPos = new BoardPosition(i, j);
                for (ObjectMap.Entry<BoardPosition, EntityValue> posPair : boardState.getEntities().getPositionEntityValuePairs()) { //entities
                    if (curPos.equals(posPair.key)) { // team 1
                        if (posPair.value.team == 1) {
                            entityZoneChar = TEAM_1_ICON;
                            break;
                        }
                        if (posPair.value.team == 0) { // team 0
                            entityZoneChar = TEAM_0_ICON;
                            break;
                        }
                        if (posPair.value.team == 2) { // team 2
                            entityZoneChar = TEAM_2_ICON;
                            break;
                        }
                        if (posPair.value.team == -1) { // object
                            entityZoneChar = OBJECT_ICON;
                            break;
                        }
                    }
                }
                // If the space is included in the list of spaces a move targets
                if (spacesMoveHits != null) {
                    for (BoardPosition spaceHitByMove : spacesMoveHits) {
                        if (entityZoneChar == '?') { // Empty space
                            if (curPos.equals(spaceHitByMove)) {
                                entityZoneChar = MOVE_HIT_EMPTY;
                            }
                        } else { // Move hit something
                            if (curPos.equals(spaceHitByMove)) {
                                if ((entityZoneChar == TEAM_0_ICON && team.get(entity).teamNumber == 0) ||
                                        (entityZoneChar == TEAM_1_ICON && team.get(entity).teamNumber == 1)) // same team as user
                                    entityZoneChar = MOVE_HIT_TEAMMATE;
                                else if ((entityZoneChar == TEAM_0_ICON && team.get(entity).teamNumber != 0) ||
                                        (entityZoneChar == TEAM_1_ICON && team.get(entity).teamNumber != 1))
                                    entityZoneChar = MOVE_HIT_ENEMY;
                                else if (entityZoneChar == OBJECT_ICON || entityZoneChar == TEAM_2_ICON)
                                    entityZoneChar = MOVE_HIT_OBJECT;
                            }
                        }
                    }
                }
                // if haven't found it yet, look to see if its a zone
                if (boardState.getZones() != null && entityZoneChar == '?') {
                    for (BoardPosition bp : ZONES) {
                        if (curPos.equals(bp)) {
                            entityZoneChar = ZONE_ICON;
                            break;
                        }
                    }
                }
                // the space is nothing: default to ' '
                if (entityZoneChar == '?')
                    entityZoneChar = ' ';
                //endregion
                outputString.append(entityZoneChar + "|");
            }
            outputString.append('\n');
        }
        // print health of remaining entities alive
        Array<EntityValue> entityValues = boardState.getEntities().getEntityValues();
        for (EntityValue ev : entityValues) {
            outputString.append(ev.BOARD_ENTITY_ID + " T: " + ev.team + "  " + ev.hp + " / " + ev.maxHp + '\n');
        }
        // print number of entities still alive
        outputString.append("Number Still Alive:  0| " + boardState.getLiveEntityCount().get(0) + "  1| " + boardState.getLiveEntityCount().get(1) +
                "  2| " + boardState.getLiveEntityCount().get(2) + "\n");

        /**
         * Example of output
         *     0 1 2
         *  0 | | | |
         *  1 | | | |
         *  2 | | | |
         *  3 | | | |
         *  4 | | | |
         *
         * Number Still Alive:   0: 1   1: 3  2: 0
         */

        return outputString.toString();
    }
}
