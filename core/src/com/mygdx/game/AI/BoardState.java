package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.move_related.Move;

import static com.mygdx.game.ComponentMappers.*;

/**
 * Class used to evaluate the value of different moves on the board. Used by {@link ComputerPlayer} to determine a best move.
 *
 * @author Phillip O'Reggio
 */
public class BoardState {
    private ObjectMap<BoardPosition, EntityValue> entities;

    public BoardState(Array<Entity> e) {
        entities = new ObjectMap<>();
        for (Entity entity : e) {
            EntityValue value = new EntityValue(bm.get(entity).pos, team.get(entity).teamNumber, stm.get(entity).hp,
                    stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).getModAtk(entity), stm.get(entity).getModDef(entity), 0);
            // ... determine good vs bad status effects...
            //for now :
            value.statusEffect = status.get(entity).statusEffects.size;
            entities.put(bm.get(entity).pos, value);
        }
    }

    public BoardState(ObjectMap<BoardPosition, EntityValue> entityMap) {
        entities = entityMap;
    }

    public BoardState tryAttack(BoardPosition userPos, Array<BoardPosition> range, int power, int statusEffectChange, boolean pierce) {
        for (BoardPosition pos : range) {
            pos.add(userPos.r, userPos.c);
            if (entities.containsKey(pos)) {
                EntityValue e = entities.get(pos);
                if (pierce)
                    e.hp = MathUtils.clamp(e.hp - power, 0, 999);
                else
                    e.hp = MathUtils.clamp(e.hp - (MathUtils.clamp(power - e.defense, 0, 999)), 0, 999);
                e.statusEffect += statusEffectChange;
            }
        }
        return this;
    }

    public BoardState moveEntity(BoardPosition oldPos, BoardPosition newPos) {
        if (entities.containsKey(oldPos))
            entities.put(newPos, entities.remove(oldPos));

        return this;
    }

    /**
     *
     * @param t
     * @return
     */
    public BoardState tryTurn(Turn t) {
        //movement
        System.out.println("Contains key: " + entities.containsKey(new BoardPosition(6, 5)));
        System.out.println("Contains key  using array: " + entities.keys().toArray().contains(new BoardPosition(6, 5), false));
        System.out.println("equals : " + bm.get(t.entity).pos.equals(new BoardPosition(6, 5)));

        //System.out.println(entities);
        if (!t.pos.equals(bm.get(t.entity).pos))
            if (entities.containsKey(bm.get(t.entity).pos))
                entities.put(t.pos, entities.remove(bm.get(t.entity).pos));
        //System.out.println(entities);

        //attack
        if (t.attack != -1) {
            Move move = mvm.get(t.entity).moveList.get(t.attack);

            //deduct sp cost
            EntityValue ea = entities.get(bm.get(t.entity).pos);
            EntityValue eaa = entities.get(t.pos);
            /*
            System.out.println(bm.get(t.entity).pos);
            System.out.println(t.pos);
            System.out.println(entities.containsKey(bm.get(t.entity).pos));
            System.out.println("ewuals" +bm.get(t.entity).pos.equals(new BoardPosition(6, 5)));
            */

            //entities.get(bm.get(t.entity).pos).sp -= move.spCost();

            for (BoardPosition pos : move.getRange()) {
                pos.add(bm.get(t.entity).pos.r, bm.get(t.entity).pos.c);
                if (entities.containsKey(pos)) {
                    EntityValue e = entities.get(pos);
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

    public int evaluate() {
        int val = 0;
        for (EntityValue e : entities.values())
            val += e.getValue();
        return val;
    }

    public BoardState copy() {
        ObjectMap<BoardPosition, EntityValue> map = new ObjectMap<>();
        for (EntityValue e : entities.values())
            map.put(e.pos.copy(), e.copy());

        return new BoardState(map);
    }
}
