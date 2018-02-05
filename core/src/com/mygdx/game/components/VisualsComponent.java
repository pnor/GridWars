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
     * plays when the entity takes heavy damage
     */
    public Visuals heavyDamageAnimation;

    /**
     * plays when the entity is affected by a turn effect of a status effect
     */
    public Visuals shuffleAnimation;

    /**
     * Creates this object with preset values for damage and death animations
     * @param damageAnim damage animation
     * @param deathAnim death animation
     */
    public VisualsComponent(Visuals damageAnim, Visuals heavyAnim, Visuals deathAnim, Visuals shuffleAnim) {
        damageAnimation = damageAnim;
        heavyDamageAnimation = heavyAnim;
        deathAnimation = deathAnim;
        shuffleAnimation = shuffleAnim;
    }

    /**
     * Resets all the visuals of the component
     */
    public void resetVisuals() {
        damageAnimation.reset();
        heavyDamageAnimation.reset();
        deathAnimation.reset();
        shuffleAnimation.reset();
    }

}
