package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mygdx.game.move_related.StatusEffect;
import com.mygdx.game.ui.LerpColor;
import com.mygdx.game.ui.LerpColorManager;

/**
 * @author Phillip O'Reggio
 */
public class StatusEffectComponent implements Component {
    private static LerpColorManager lerpColorManager;
    private final OrderedMap<String, StatusEffect> statusEffects;

    public StatusEffectComponent() {
        statusEffects = new OrderedMap<String, StatusEffect>();
    }

    public int getTotalStatusEffects() {
        return statusEffects.size;
    }

    public void addStatusEffect(StatusEffect effect, Entity e) {
        effect.doInitialEffect(e);
        statusEffects.put(effect.getName(), effect);
        if (effect.getColor() instanceof LerpColor)
            lerpColorManager.registerLerpColor((LerpColor) effect.getColor());
    }

    public void removeStatusEffect(Entity e, String name) {
        if (statusEffects.containsKey(name)) {
            statusEffects.get(name).doEndEffect(e);
            StatusEffect statusEffect = statusEffects.remove(name);
            if (statusEffect.getColor() instanceof LerpColor)
                lerpColorManager.remove((LerpColor) statusEffect.getColor());
        }
    }

    public void removeStatusEffect(Entity e, String... statusEffectNames) {
        Array<String> names = new Array<String>(statusEffectNames);
        for (String s : names)
            if (statusEffects.containsKey(s)) {
                statusEffects.get(s).doEndEffect(e);
                statusEffects.remove(s);
            }
    }

    public boolean contains(String key) {
        return statusEffects.containsKey(key);
    }

    public StatusEffect getStatusEffect(String key) {
        return statusEffects.get(key);
    }

    public void removeAll(Entity e) {
        for (StatusEffect effect : statusEffects.values())
            effect.doEndEffect(e);
        statusEffects.clear();
    }

    public static void setLerpColorManager(LerpColorManager manager) {
        lerpColorManager = manager;
    }

    public static LerpColorManager getLerpColorManager() {
        return lerpColorManager;
    }

    public Array<StatusEffect> getStatusEffects() {
        return statusEffects.values().toArray();
    }
}
