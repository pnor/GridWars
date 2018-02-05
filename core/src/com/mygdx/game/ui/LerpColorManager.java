package com.mygdx.game.ui;

import com.badlogic.gdx.utils.Array;

/**
 * Manages all LerpColors in game and updates them. Will not store duplicates.
 * @author Phillip O'Reggio
 */
public class LerpColorManager {
    /**
     * All colors that will be updated
     */
    private final Array<LerpColor> LERPCOLORS_IN_USE;

    public LerpColorManager() {
        LERPCOLORS_IN_USE = new Array<LerpColor>();
    }

    /**
     * Adds a lerp color to be updated by the game.
     * @param color being added
     */
    public boolean registerLerpColor(LerpColor color) {
        //check if already contains color (Starting from end and going to beginning)
        for (int i = LERPCOLORS_IN_USE.size - 1; i >= 0; i--) {
            if (LERPCOLORS_IN_USE.get(i) == color) //if contains color, don't add
                return false;
        }
        LERPCOLORS_IN_USE.add(color);
        return true;
    }

    /**
     * Removes a color from the manager
     * @param color being removed
     * @return True if LerpColor was in the set and operation was successful. False otherwise.
     */
    public boolean remove(LerpColor color) {
        return LERPCOLORS_IN_USE.removeValue(color, true);
    }

    /**
     * Removes all LerpColors from update array
     */
    public void clear() {
        LERPCOLORS_IN_USE.clear();
    }

    public void update(float deltaTime) {
        for (LerpColor color : LERPCOLORS_IN_USE)
            color.update(deltaTime);
    }
}
