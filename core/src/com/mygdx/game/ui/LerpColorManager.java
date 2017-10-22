package com.mygdx.game.ui;

import com.badlogic.gdx.utils.ObjectSet;

/**
 * Manages all LerpColors in game and updates them. Will not store duplicates.
 * @author Phillip O'Reggio
 */
public class LerpColorManager {
    /**
     * All colors that will be updated
     */
    private final ObjectSet<LerpColor> LERPCOLORS_IN_USE;

    public LerpColorManager() {
        LERPCOLORS_IN_USE = new ObjectSet<>();
    }

    /**
     * Adds a lerp color to be updated by the game.
     * @param color being added
     */
    public void registerLerpColor(LerpColor color) {
        LERPCOLORS_IN_USE.add(color);
    }

    /**
     * Removes a color from the manager
     * @param color being removed
     * @return True if LerpColor was in the set and operation was successful. False otherwise.
     */
    public boolean remove(LerpColor color) {
        return LERPCOLORS_IN_USE.remove(color);
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
