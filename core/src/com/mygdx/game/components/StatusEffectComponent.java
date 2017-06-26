package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.mygdx.game.move_related.StatusEffect;

/**
 * @author Phillip O'Reggio
 */
public class StatusEffectComponent implements Component {
    public final OrderedMap<String, StatusEffect> statusEffects;

    public StatusEffectComponent() {
        statusEffects = new OrderedMap<String, StatusEffect>();
    }

    public int getTotalStatusEffects() {
        return statusEffects.size;
    }

    public void addStatusEffect(StatusEffect effect, Entity e) {
        effect.doInitialEffect(e);
        statusEffects.put(effect.getName(), effect);
    }

    public void removeStatusEffect(Entity e, String name) {
        if (statusEffects.containsKey(name)) {
            statusEffects.get(name).doEndEffect(e);
            statusEffects.remove(name);
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

    public void removeAll(Entity e) {
        for (StatusEffect effect : statusEffects.values())
            effect.doEndEffect(e);
        statusEffects.clear();
    }

}
