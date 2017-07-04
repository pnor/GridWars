package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.rules_types.Team;

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
     * @param teams Teams used for an {@link EntityValue}'s checkIdentity method
     */
    public BoardState(Array<Entity> e, Array<Team> teams) {
        entities = new ArrayMap<>();
        for (Entity entity : e) {
            EntityValue value;
            if (stm.has(entity) && stm.get(entity).alive) {
                if (team.has(entity))
                    value = new EntityValue(bm.get(entity).pos, team.get(entity).teamNumber, teams.get(team.get(entity).teamNumber).getEntities().indexOf(entity, true), stm.get(entity).hp,
                            stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).getModAtk(entity), stm.get(entity).getModDef(entity), 0);
                else {
                    value = new EntityValue(bm.get(entity).pos, -1, -1, stm.get(entity).hp,
                            stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).getModAtk(entity), stm.get(entity).getModDef(entity), 0);
                }
            } else continue;

            // ... determine good vs bad status effects...
            //for now :
            if (status.has(entity))
                value.statusEffect = status.get(entity).statusEffects.size;
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
        //movement

        //System.out.println(entities);
        if (!t.pos.equals(bm.get(t.entity).pos))
            if (entities.containsKey(bm.get(t.entity).pos))
                entities.put(t.pos, entities.removeKey(bm.get(t.entity).pos));
        //System.out.println(entities);

        //attack
        if (t.attack != -1) {
            Move move = mvm.get(t.entity).moveList.get(t.attack);

            //deduct sp cost
            try {
                entities.get(t.pos).sp -= move.spCost();
            } catch (Exception e) {
                if (e instanceof NullPointerException) {
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
                    if (move.getPierces())
                        e.hp = MathUtils.clamp(e.hp - (int) (move.getAmpValue() * stm.get(t.entity).getModAtk(t.entity)), 0, 999);
                    else
                        e.hp = MathUtils.clamp(e.hp - (MathUtils.clamp((int) (move.getAmpValue() * stm.get(t.entity).getModAtk(t.entity)) - e.defense, 0, 999)), 0, 999);

                    //remove dead

                    if (e.hp <= 0) {
                        entities.removeKey(newPos);
                    }

                    e.statusEffect += move.getStatusEffectChanges();
                }
            }

        }

        return this;
    }

    /**
     * Evaulates the state of the board
     * @param homeTeam The team's prespective. Entities on that team will be added, while others are subtracted.
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
