package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
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
    private ArrayMap<BoardPosition, EntityValue> entities;

    /**
     * Creates a {@link BoardState} using entities and their teams.
     * @param e Array of Entities
     */
    public BoardState(Array<Entity> e) {
        entities = new ArrayMap<>();
        for (Entity entity : e) {
            EntityValue value;
            if (stm.has(entity) && stm.get(entity).alive) {
                if (team.has(entity)) //on a team
                    if (!status.has(entity)) { //does not have status effect
                        value = new EntityValue(bm.get(entity).pos, team.get(entity).teamNumber, bm.get(entity).BOARD_ENTITY_ID, stm.get(entity).hp,
                                stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def);
                    } else { //does have status effect
                        Array<StatusEffect> currentStatusEffects = status.get(entity).statusEffects.values().toArray();
                        StatusEffectInfo[] statusInfos = new StatusEffectInfo[status.get(entity).statusEffects.size];
                        for (int i = 0; i < currentStatusEffects.size; i++)
                            statusInfos[i] = currentStatusEffects.get(i).createStatusEffectInfo();

                        value = new EntityValue(bm.get(entity).pos, team.get(entity).teamNumber, bm.get(entity).BOARD_ENTITY_ID, stm.get(entity).hp,
                            stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def, statusInfos);
                    }
                else { //not on a team
                    if (!status.has(entity))
                        value = new EntityValue(bm.get(entity).pos, -1, -1, stm.get(entity).hp,
                            stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def);
                    else {
                        Array<StatusEffect> currentStatusEffects = status.get(entity).statusEffects.values().toArray();
                        StatusEffectInfo[] statusInfos = new StatusEffectInfo[status.get(entity).statusEffects.size];
                        for (int i = 0; i < currentStatusEffects.size; i++)
                            statusInfos[i] = currentStatusEffects.get(i).createStatusEffectInfo();

                        value = new EntityValue(bm.get(entity).pos, -1, -1, stm.get(entity).hp,
                                stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def, statusInfos);
                    }
                }
            } else continue;

            entities.put(bm.get(entity).pos, value);
        }
    }

    public BoardState(ArrayMap<BoardPosition, EntityValue> entityMap) {
        entities = entityMap;
    }

    /**
     * Changes the state of the {@link BoardState} based on the effects of the {@link Turn}.
     * @param t Turn
     * @return The {@link BoardState} for chaining
     */
    public BoardState tryTurn(Turn t) {
        EntityValue effectedEntity = null; //entity value representing entity playing out turn

        //get User
        Array<EntityValue> entityValues = entities.values().toArray();
        for (int i = 0; i < entityValues.size; i++)
            if (entityValues.get(i).checkIdentity(t.entity))
                effectedEntity = entityValues.get(i);

        if (effectedEntity == null) //user died in the process, do nothing
                return this;


        /*
        if (effectedEntity == null) {
            System.out.println("xxxxxxxxxxxxxx effectedEntity is null xxxxxxxxxxxxxxxxx");
            System.out.println(t);
            System.out.println("Position : " + t.pos + "   Entity : " + nm.get(t.entity).name);
            System.out.println("Entity ID : " + bm.get(t.entity).BOARD_ENTITY_ID);
            System.out.println("All entity value size : " + entityValues.size);
            System.out.println("Board Entity IDs: ");
            for (EntityValue ev : entityValues)
                System.out.println(ev.BOARD_ENTITY_ID);
            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        }
        */

        //movement
        if (!t.pos.equals(effectedEntity.pos))
            if (entities.containsKey(effectedEntity.pos))
                entities.put(t.pos, entities.removeKey(effectedEntity.pos));


        //attack
        if (t.attack != -1) {
            Move move = mvm.get(t.entity).moveList.get(t.attack);

            //deduct sp cost
            try {
                effectedEntity.sp -= move.spCost();
            } catch (Exception e) {
                if (e instanceof NullPointerException) {
                    //System.out.println("!");
                    /*
                    System.out.println("Pos :" + t.pos);
                    System.out.println("move :" + move);
                    System.out.println("entities :" + entities.get(t.pos));
                    */
                    //System.out.println("No Entity where there should be an entity in BoardState Hashmap.");
                }
            }

            for (BoardPosition pos : move.getOrientedAttackPositions(t.direction, move)) {
                BoardPosition newPos = pos.add(t.pos.r, t.pos.c);

                if (entities.containsKey(newPos)) {
                    EntityValue e = entities.get(newPos);
                    //damage
                    if (move.moveInfo().pierces)
                        e.hp = MathUtils.clamp(e.hp - (int) (move.moveInfo().ampValue * effectedEntity.getModAtk()), 0, e.maxHp);
                    else
                        e.hp = MathUtils.clamp(e.hp - (MathUtils.clamp((int) (move.moveInfo().ampValue * effectedEntity.getModAtk()) - e.getModDef(), 0, 999)), 0, e.maxHp);

                    //status
                    if (move.moveInfo().statusEffects != null && e.acceptsStatusEffects)
                        e.statusEffectInfos.addAll(move.moveInfo().statusEffects);

                    //misc
                    if (move.moveInfo().miscEffects != null)
                        move.moveInfo().miscEffects.doMiscEffects(e);

                    //remove dead

                    if (e.hp <= 0) {
                        entities.removeKey(newPos);
                        /*
                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Entity killed~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                        System.out.println("Entity's ID: " + e.BOARD_ENTITY_ID);
                        System.out.println("Entity pos : " + e.pos);
                        System.out.println("Entity toString : " + e);
                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                        */
                    }
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
        Array<EntityValue> entityValues = entities.values().toArray();
        try {
            for (EntityValue e : entityValues) {
                if (e.team == team && e.statusEffectInfos != null && e.statusEffectInfos.size > 0)
                    for (StatusEffectInfo s : e.statusEffectInfos)
                        s.turnEffectInfo.doTurnEffect(e);
            }
        } catch (Exception e) {
            System.out.println("----------------------- Exception in doTurnEffects -------------------------------");
            System.out.println("BoardState.doTurnEffects has caused an exception! : Excpetion is type " + e.getClass());
            System.out.println("Entity Values : " + entityValues);
            System.out.println("-------------------------------------");
        }
    }

    /**
     * Evaluates the state of the board
     * @param homeTeam The team's perspective. Entities on that team will be added, while others are subtracted.
     * @return integer representing the value of all {@link EntityValue}s added together
     */
    public int evaluate(int homeTeam) {
        int val = 0;
        for (EntityValue e : entities.values())
            val += e.getValue(homeTeam);
        return val;
    }

    /**
     * @return A copy of this object
     */
    public BoardState copy() {
        ArrayMap<BoardPosition, EntityValue> map = new ArrayMap<>();
        for (EntityValue e : entities.values())
            map.put(e.pos.copy(), e.copy());

        return new BoardState(map);
    }

    public ArrayMap<BoardPosition, EntityValue> getEntities() {
        return entities;
    }
}
