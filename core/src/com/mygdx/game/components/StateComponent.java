package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

/**
 * Represents the current state of an entity on the board. Has no effect on stats, not to be confused with {@code StatComponent}.
 * @author Phillip O'Reggio
 */
public class StateComponent implements Component {
    public boolean canAttack;
    public boolean canMove;
}
