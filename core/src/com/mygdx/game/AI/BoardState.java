package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
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
    private Array<Array<BoardPosition>> zones;

    /**
     * Creates a {@link BoardState} using entities and their teams.
     * @param e Array of Entities
     */
    public BoardState(Array<Entity> e, Array<Array<BoardPosition>> boardZones) {
        entities = new ArrayMap<>();
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
                    if (!status.has(entity))
                        value = new EntityValue(bm.get(entity).pos, -1, -1, stm.get(entity).hp,
                            stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def, 0);
                    else {
                        Array<StatusEffect> currentStatusEffects = status.get(entity).getStatusEffects();
                        StatusEffectInfo[] statusInfos = new StatusEffectInfo[status.get(entity).getTotalStatusEffects()];
                        for (int i = 0; i < currentStatusEffects.size; i++)
                            statusInfos[i] = currentStatusEffects.get(i).createStatusEffectInfo();

                        value = new EntityValue(bm.get(entity).pos, -1, -1, stm.get(entity).hp,
                                stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).atk, stm.get(entity).def, statusInfos, 0);
                    }
                }
            } else continue;

            entities.put(bm.get(entity).pos, value);
        }

        zones = boardZones;
    }

    public BoardState(ArrayMap<BoardPosition, EntityValue> entityMap, Array<Array<BoardPosition>> boardZones) {
        entities = entityMap;
        zones = boardZones;
    }

    /**
     * Changes the state of the {@link BoardState} based on the effects of the {@link Turn}.
     * @param t Turn
     * @return The {@link BoardState} for chaining
     */
    public BoardState tryTurn(Turn t) {
        EntityValue userEntity = null; //entity value representing entity playing out turn

        //get User
        Array<EntityValue> entityValues = entities.values().toArray();
        EntityValue cur = null;
        try {
            for (int i = 0; i < entityValues.size; i++) {
                cur = entityValues.get(i);
                if (entityValues.get(i).checkIdentity(t.entity))
                    userEntity = entityValues.get(i);
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("entityValues.get(i) : " + cur);
            System.out.println("t.entity : " + t.entity);
            Gdx.app.exit();
        }

        if (userEntity == null) //user died, do nothing
                return this;

        //movement
        if (!t.pos.equals(userEntity.pos))
            if (entities.containsKey(userEntity.pos)) {
                entities.put(t.pos, entities.removeKey(userEntity.pos));
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
                    if (userEntity.team == e.team && oldHp > e.hp)
                        e.arbitraryValue -= 30 * (oldHp - e.hp);

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
                    if (e.hp <= 0)
                        entities.removeKey(newPos);
                } else { //attacking on an empty space
                    //discourage attacking empty spaces compared to not attacking at all
                    //single hitting moves weighted heavier than spread attacks
                    userEntity.arbitraryValue = (move.getRange().size <= 1)?
                            userEntity.arbitraryValue - 30 : userEntity.arbitraryValue - 30 / move.getRange().size;
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

        for (EntityValue e : entityValues) {
            if (e.team == team && e.statusEffectInfos != null && e.statusEffectInfos.size > 0)
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

    /**
     * Evaluates the state of the board. If a piece is on its zone, will return a very higher number, acting as infinity.
     * @param homeTeam The team's perspective. Entities on that team will be added, while others are subtracted.
     * @return integer representing the value of all {@link EntityValue}s added together
     */
    public int evaluate(int homeTeam) {
        int val = 0;
        for (EntityValue e : entities.values()) {
            //zone check
            if (zones != null && e.team != -1 && zones.get(e.team).contains(e.pos, false)) {
                if (e.team == homeTeam)
                    return 9999999;
                else
                    return -9999999;
            }

            val += e.getValue(homeTeam);
        }
        return val;
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
        ArrayMap<BoardPosition, EntityValue> map = new ArrayMap<>();
        for (EntityValue e : entities.values())
            map.put(e.pos.copy(), e.copy());

        return new BoardState(map, zones);
    }

    public ArrayMap<BoardPosition, EntityValue> getEntities() {
        return entities;
    }
}
