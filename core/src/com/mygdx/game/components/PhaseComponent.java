package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.misc.Phase;
import com.mygdx.game.rules_types.Rules;


/**
 * Component representing board entities with phases. Component contains data for the thresholds of each phase, and what stats/appearances it has.
 * Is used in the {@link Rules} class, where the game will check if it should move onto the next phase.
 *
 * @author Phillip O'Reggio
 */
public class PhaseComponent implements Component{
    public final Array<Phase> phases;
    public int currentPhaseIndex;

    public PhaseComponent(Phase... phases) {
        this.phases = new Array<Phase>(phases);
        this.phases.sort();
    }
}
