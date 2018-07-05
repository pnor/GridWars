package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.move_related.StatusEffect;

import static com.mygdx.game.ComponentMappers.*;

/**
 * Class used to evaluate the value of different moves on the board. Used by {@link ComputerPlayer} to determine a best move.
 *
 * @author Phillip O'Reggio
 */
public class BoardState {
    private EntityMap entities;
    private Array<Array<BoardPosition>> zones;
    /** Keeps count of how many entities per team there are.
     * Index represents team number while value at index represents number of live entities
     */
    private Array<Integer> liveEntityCount;

    /**
     * Creates a {@link BoardState} using entities and their teams.
     * @param e Array of Entities
     */
    public BoardState(Array<Entity> e, Array<Array<BoardPosition>> boardZones) {
        liveEntityCount = new Array<Integer>(new Integer[]{0, 0, 0});
        entities = new EntityMap();
        for (Entity entity : e) {
            EntityValue value;
            if (stm.has(entity) && stm.get(entity).alive) {
                if (team.has(entity)) { //on a team
                    if (!status.has(entity)) { //does not have status effect
                        value = new EntityValue(bm.get(entity).pos, team.get(entity).teamNumber, bm.get(entity).BOARD_ENTITY_ID, stm.get(entity).hp,
                                stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def, 0);
                    } else { //does have status effect
                        Array<StatusEffect> currentStatusEffects = status.get(entity).getStatusEffects();
                        StatusEffectInfo[] statusInfos = new StatusEffectInfo[status.get(entity).getTotalStatusEffects()];
                        for (int i = 0; i < currentStatusEffects.size; i++)
                            statusInfos[i] = currentStatusEffects.get(i).createStatusEffectInfo();

                        value = new EntityValue(bm.get(entity).pos, team.get(entity).teamNumber, bm.get(entity).BOARD_ENTITY_ID, stm.get(entity).hp,
                                stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def, statusInfos, 0);
                    }
                } else { //not on a team
                    if (!status.has(entity)) {
                        value = new EntityValue(bm.get(entity).pos, -1, -1, stm.get(entity).hp,
                                stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def, 0);
                    } else {
                        Array<StatusEffect> currentStatusEffects = status.get(entity).getStatusEffects();
                        StatusEffectInfo[] statusInfos = new StatusEffectInfo[status.get(entity).getTotalStatusEffects()];
                        for (int i = 0; i < currentStatusEffects.size; i++)
                            statusInfos[i] = currentStatusEffects.get(i).createStatusEffectInfo();

                        value = new EntityValue(bm.get(entity).pos, -1, -1, stm.get(entity).hp,
                                stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def, statusInfos, 0);
                    }
                }
            } else continue;

            //add new entityValue to liveEntityCount
            if (value.team != -1) {
                liveEntityCount.set(value.team, liveEntityCount.get(value.team) + 1);
            }
            entities.put(entity, bm.get(entity).pos, value);
        }

        zones = boardZones;
    }

    /**
     * Creates a board state using values that would normally be gotten from a pre-existing board state
     * @param entityMap Map of entities on the board
     * @param boardZones Zones
     * @param entitiesAliveCount Number of Entities from each team that are alive
     */
    public BoardState(EntityMap entityMap, Array<Array<BoardPosition>> boardZones, Array<Integer> entitiesAliveCount) {
        entities = entityMap;
        zones = boardZones;
        liveEntityCount = new Array<Integer>(entitiesAliveCount);
    }

    /**
     * Changes the state of the {@link BoardState} based on the effects of the {@link Turn}.
     * @param t Turn
     * @return The {@link BoardState} for chaining
     */
    public BoardState tryTurn(Turn t) {
        //get User
        EntityValue userEntity = entities.get(t.entity);

        if (userEntity == null) //user died, do nothing
                return this;

        //movement
        if (!t.pos.equals(userEntity.pos))
            if (entities.containsKey(userEntity.pos)) {
                entities.put(t.entity, t.pos, entities.remove(t.entity));
                userEntity.pos = t.pos.copy();
            }

        //attack
        if (t.attack != -1) {
            Move move = mvm.get(t.entity).moveList.get(t.attack);

            //deduct sp cost
            userEntity.sp -= move.spCost();

            for (BoardPosition pos : move.getOrientedAttackPositions(t.direction, move)) {
                BoardPosition newPos = pos.add(t.pos.r, t.pos.c);

                if (entities.containsKey(newPos)) {
                    EntityValue e = entities.get(newPos); //entity targeted by attack
                    //damage
                    int oldHp = e.hp;
                    if (move.moveInfo().pierces)
                        e.hp = MathUtils.clamp(e.hp - (int) (move.moveInfo().ampValue * userEntity.getModAtk()), 0, e.maxHp);
                    else
                        e.hp = MathUtils.clamp(e.hp - (MathUtils.clamp((int) (move.moveInfo().ampValue * userEntity.getModAtk()) - e.getModDef(), 0, 999)), 0, e.maxHp);

                    //discourage hitting allies with damaging attacks
                    /*
                    if (userEntity.team == e.team && oldHp > e.hp)
                        e.arbitraryValue -= 30 * (oldHp - e.hp);
                     */

                    //status
                    if (move.moveInfo().statusEffects != null && e.acceptsStatusEffects) {
                        for (StatusEffectInfo status : move.moveInfo().statusEffects) {
                            if (!e.statusEffectInfos.contains(status, false))
                                e.statusEffectInfos.add(status);
                        }
                    }

                    //misc effects
                    if (move.moveInfo().miscEffects != null)
                        move.moveInfo().miscEffects.doMiscEffects(e, userEntity);

                    //clamp hp to max hp
                    if (e.hp > e.maxHp)
                        e.hp = e.maxHp;

                    //remove dead
                    if (e.hp <= 0) {
                        entities.remove(e);
                        liveEntityCount.set(e.team, liveEntityCount.get(e.team) - 1);
                    }
                } else { //attacking on an empty space
                    //NOW pruned so this should never be an issue
                    //System.out.println("occuring"); // <- or is it?
                }
            }

        }

        return this;
    }

    /**
     * Does the end of turn effects on an Entity
     * @param team team which is having turn effects inflicted on
     */
    public void doTurnEffects(int team) {
        Array<EntityValue> entityValues = entities.getEntityValues();

        for (EntityValue e : entityValues) {
            if (e.team == team && e.statusEffectInfos != null && e.statusEffectInfos.size > 0) {
                //increment SP
                e.sp++;
                for (StatusEffectInfo s : e.statusEffectInfos) {
                    if (s.turnEffectInfo != null) {
                        s.turnEffectInfo.doTurnEffect(e);
                    }
                    s.incrementTurn();
                    if (s.checkDuration())
                        e.statusEffectInfos.removeValue(s, true);
                }
            }
        }
    }

    /**
     * Evaluates the state of the board. If a piece is on its zone, will return a very higher number, acting as infinity.
     * @param homeTeam The team's perspective. Entities on that team will be added, while others are subtracted.
     * @return integer representing the value of all {@link EntityValue}s added together
     */
    public int evaluate(int homeTeam) {
        int val = 0;
        for (EntityValue e : entities.getEntityValues()) {
            //zone check
            /*
            if (zones != null && e.team != -1 && zones.get(e.team).contains(e.pos, false)) {
                if (e.team == homeTeam)
                    return 9999999;
                else
                    return -9999999;
            }
            */

            val += e.getValue(homeTeam);
        }
        return val;
    }

    /**
     * @return index of the team still on the board. Does not count the 1st attack only teams, and will return -1 if no teams qualifies.
     */
    public int getLastTeamStanding() {
        if (liveEntityCount.get(0) <= 0)
            return 1;
        else if (liveEntityCount.get(1) <= 0)
            return 0;
        else
            return -1;
    }

    /**
     * @param bp position that is being checked
     * @return whether the chosen position is occupied by an entity
     */
    public boolean isOccupied(BoardPosition bp) {
        return entities.get(bp) != null;
    }

    /**
     * @return A copy of this object
     */
    public BoardState copy() {
        EntityMap map = new EntityMap();
        for (EntityValue e : entities.getEntityValues()) {
            map.put(entities.getKeyEntity(e), e.pos.copy(), e.copy());
        }

        return new BoardState(map, zones, liveEntityCount);
    }

    public Array<Integer> getLiveEntityCount() {
        return liveEntityCount;
    }

    public EntityMap getEntities() {
        return entities;
    }

    /**
     * @return a visualization of the board based on entities and zones
     */
    @Override
    public String toString() {
        final Array<BoardPosition> POSITIONS = entities.getAllPositions(); // all positions
        final Array<BoardPosition> ZONES = new Array<>();

        if (zones != null) {
            for (int i = 0; i < this.zones.get(0).size; i++)
                ZONES.add(this.zones.get(0).get(i));
            for (int i = 0; i < this.zones.get(1).size; i++)
                ZONES.add(this.zones.get(1).get(i));
        }


        // get assumed dimensions of board
        int largestRowSize = -1;
        for (int i = 0; i < POSITIONS.size; i++) { // iterate over positions of entities
            largestRowSize = Math.max(POSITIONS.get(i).r, largestRowSize);
        }
        if (zones != null) {
            for (int i = 0; i < ZONES.size; i++) { // iterate over zone locations
                largestRowSize = Math.max(ZONES.get(i).r, largestRowSize);
            }
        }
        final int ASSUMED_ROW_SIZE = largestRowSize;

        int largestColSize = -1;
        for (int i = 0; i < POSITIONS.size; i++) { // iterate over positions of entities
            largestColSize = Math.max(POSITIONS.get(i).c, largestColSize);
        }
        if (zones != null) {
            for (int i = 0; i < ZONES.size; i++) { // iterate over positions of entities
                largestColSize = Math.max(ZONES.get(i).c, largestColSize);
            }
        }
        final int ASSUMED_COL_SIZE = largestColSize;

        // listed in order of display priority form lowest (top) to highest (bottom)
        final char OBJECT_ICON = 'X';
        final char ZONE_ICON = 'Z';
        final char TEAM_2_ICON = '2';
        final char TEAM_0_ICON = '0';
        final char TEAM_1_ICON = '1';

        StringBuilder outputString = new StringBuilder();

        // print row header
        outputString.append("    "); // 4 spaces
        for (int i = 0; i <= ASSUMED_COL_SIZE; i++) {
            outputString.append(i + " "); // Col Numbers
        }
        outputString.append("\n");

        // print board
        for (int i = 0; i <= ASSUMED_ROW_SIZE; i++) {
            outputString.append(" " + i + " |");
            for (int j = 0; j <= ASSUMED_COL_SIZE; j++) {
                char entityZoneChar = '?'; //char representing whats going on that space
                //region get what char to display
                BoardPosition curPos = new BoardPosition(i, j);
                for (ObjectMap.Entry<BoardPosition, EntityValue> posPair : entities.getPositionEntityValuePairs()) { //entities
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
                // if haven't found it yet, look to see if its a zone
                if (zones != null) {
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

        // print number of entities still alive
        outputString.append("Number Still Alive:   0| " + liveEntityCount.get(0) + "   1| " + liveEntityCount.get(1) +
                "   2| " + liveEntityCount.get(2) + "\n");
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
