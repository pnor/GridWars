package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.move_related.Visuals;

/**
 * Class for visual actions storage (for Entities on the board)
 * @author Phillip O'Reggio
 */
public class VisualsComponent implements Component {
    /**
     * plays when the entity dies
     */
    public Visuals deathAnimation;

    /**
     * plays when the entity takes damage
     */
    public Visuals damageAnimation;

    /**
     * Creates this object with preset values for damage and death animations
     * @param damageAnim damage animation
     * @param deathAnim death animation
     */
    public VisualsComponent(Visuals damageAnim, Visuals deathAnim) {
        damageAnimation = damageAnim;
        deathAnimation = deathAnim;
    }

}
