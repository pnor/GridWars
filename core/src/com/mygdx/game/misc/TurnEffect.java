package com.mygdx.game.misc;

import com.badlogic.ashley.core.Entity;

/**
 * Turn Effect of a {@link com.mygdx.game.move_related.StatusEffect}.
 *
 * @author Phillip O'Reggio
 */
public interface TurnEffect {
    void doEffect(Entity e);
}
