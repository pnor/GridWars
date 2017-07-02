package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.move_related.Move;

import static com.mygdx.game.ComponentMappers.*;

/**
 * Class used to evaluate the value of different moves on the board. Used by {@link ComputerPlayer} to determine a best move.
 *
 * @author Phillip O'Reggio
 */
public class BoardState {
    private ArrayMap<BoardPosition, EntityValue> entities;

    public BoardState(Array<Entity> e) {
        entities = new ArrayMap<>();
        for (Entity entity : e) {
            EntityValue value;
            if (stm.has(entity) && stm.get(entity).alive) {
                if (team.has(entity))
                    value = new EntityValue(bm.get(entity).pos, team.get(entity).teamNumber, stm.get(entity).hp,
                            stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).getModAtk(entity), stm.get(entity).getModDef(entity), 0);
                else {
                    value = new EntityValue(bm.get(entity).pos, -1, stm.get(entity).hp,
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
     *
     * @param t
     * @return
     */
    public BoardState tryTurn(Turn t) {
        //movement
        /*
        System.out.println("Contains key: " + entities.containsKey(new BoardPosition(6, 5)));
        System.out.println("Contains key  using array: " + entities.keys().toArray().contains(new BoardPosition(6, 5), false));
        System.out.println("is position same equals : " + bm.get(t.entity).pos.equals(new BoardPosition(6, 5)));
            */
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
                    System.out.println("Pos :" + t.pos);
                    System.out.println("move :" + move);
                    System.out.println("entities :" + entities.get(t.pos));

                }
            }

            for (BoardPosition pos : move.getOrientedAttackPositions(t.direction, move)) {
                BoardPosition newPos = pos.add(t.pos.r, t.pos.c);

                if (entities.containsKey(newPos)) {
                    EntityValue e = entities.get(newPos);
                    //System.out.println(e);
                    if (move.getPierces())
                        e.hp = MathUtils.clamp(e.hp - (int) (move.getAmpValue() * stm.get(t.entity).getModAtk(t.entity)), 0, 999);
                    else
                        e.hp = MathUtils.clamp(e.hp - (MathUtils.clamp((int) (move.getAmpValue() * stm.get(t.entity).getModAtk(t.entity)) - e.defense, 0, 999)), 0, 999);
                    e.statusEffect += move.getStatusEffectChanges();
                }
            }

        }

        return this;
    }

    public int evaluate(int homeTeam) {
        int val = 0;
        for (EntityValue e : entities.values())
            val += e.getValue(homeTeam);
        return val;
    }

    public BoardState copy() {
        ArrayMap<BoardPosition, EntityValue> map = new ArrayMap<>();
        for (EntityValue e : entities.values())
            map.put(e.pos.copy(), e.copy());

        return new BoardState(map);
    }
}
