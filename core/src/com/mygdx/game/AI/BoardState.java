package com.mygdx.game.AI;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.mygdx.game.boards.BoardPosition;

import static com.mygdx.game.ComponentMappers.*;

/**
 * Class used to evaluate the value of different moves on the board. Used by {@link ComputerPlayer} to determine a best move.
 *
 * @author Phillip O'Reggio
 */
public class BoardState {
    private ObjectMap<BoardPosition, EntityValue> entities;

    public BoardState(Array<Entity> e) {
        for (Entity entity : e) {
            EntityValue value = new EntityValue(bm.get(entity).pos, team.get(entity).teamNumber, stm.get(entity).hp,
                    stm.get(entity).getModMaxHp(entity), stm.get(entity).sp, stm.get(entity).getModAtk(entity), stm.get(entity).getModDef(entity), 0);
            // ... determine good vs bad status effects...
            //for now :
            value.statusEffect = status.get(entity).statusEffects.size;
            entities.put(bm.get(entity).pos, value);
        }
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
}
