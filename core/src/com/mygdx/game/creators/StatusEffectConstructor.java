package com.mygdx.game.creators;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.move_related.StatusEffect;
import com.mygdx.game.screens_ui.LerpColor;

import static com.mygdx.game.ComponentMappers.stm;
import static com.mygdx.game.ComponentMappers.vm;

/**
 * Class storing builder methods for {@link com.mygdx.game.move_related.StatusEffect}s
 *
 * @author Phillip O'Reggio
 */
public class StatusEffectConstructor {
    //TODO combine same status effects (offenseless and offenseless2 can be 1 method called offenseless that takes a parameter)
    //region Status Effects
    public static StatusEffect poison() {
        StatusEffect statusEffect = new StatusEffect("Poison", 2, new LerpColor(Color.GREEN, new Color(201f / 255f, 1f, 0f, 1f)), (e) -> {
            stm.get(e).hp -= 1;
            if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                vm.get(e).heavyDamageAnimation.setPlaying(true, true);
        }, (entity) -> entity.hp -= 1);

        statusEffect.setStatChanges(1, 1, 1, 1, 1);
        return statusEffect;
    }

    public static StatusEffect toxic() {
        StatusEffect statusEffect = new StatusEffect("Toxic", 3, new LerpColor(Color.GREEN, new Color(10f / 255f, 41f / 255f, 10f / 255f, 1f)),
                (e) -> {
                    stm.get(e).hp -= 2;
                    if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                        vm.get(e).heavyDamageAnimation.setPlaying(true, true);
                }, (entity) -> entity.hp -= 2);

        statusEffect.setStatChanges(1, 1, 1, 1, 1);
        return statusEffect;
    }

    public static StatusEffect burn() {
        StatusEffect effect = new StatusEffect("Burn", 3, new LerpColor(Color.RED, new Color(1, 125f / 255f, 0f, 1f), .3f, Interpolation.sineOut),
                (e) -> {
                    if (stm.has(e) && MathUtils.randomBoolean()) {
                        stm.get(e).hp -= 1;
                        if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                            vm.get(e).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, (entity) -> {
            if (MathUtils.randomBoolean())
                entity.hp -= 1;
        });

        effect.setStatChanges(1, 1, .5f, 1, 1);
        return effect;
    }

    public static StatusEffect paralyze() {
        StatusEffect effect = new StatusEffect("Paralyze", 3, new LerpColor(Color.GRAY, Color.YELLOW, .4f, Interpolation.exp5In), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, .5f);
        return effect;
    }

    public static StatusEffect freeze() {
        StatusEffect effect = new StatusEffect("Freeze", 3, new LerpColor(new Color(.8f, .4f, 1, 1), Color.CYAN, .5f), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, .5f, 0);
        effect.setStopsAnimation(true);
        return effect;
    }

    public static StatusEffect shivers() {
        StatusEffect effect = new StatusEffect("Shivers", 2, new LerpColor(new Color(.8f, .8f, 1, 1), Color.WHITE), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, .5f, 1, 1, 1);
        return effect;
    }

    public static StatusEffect petrify() {
        StatusEffect effect = new StatusEffect("Petrify", 2, new Color(214f / 255f, 82f / 255f, 0, 1), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 2, 0);
        effect.setStopsAnimation(true);
        return effect;
    }

    public static StatusEffect stillness() {
        StatusEffect effect = new StatusEffect("Stillness", 2, new LerpColor(Color.WHITE, new Color(0, 140f / 255f, 1f, 1f), .7f,  Interpolation.sine), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, 1);
        effect.setStopsAnimation(true);
        return effect;
    }

    public static StatusEffect inept() {
        StatusEffect effect = new StatusEffect("Inept", 3, new LerpColor(Color.WHITE, Color.NAVY, .7f,  Interpolation.sine), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 0, 1, 1, 1);
        effect.setStopsAnimation(true);
        return effect;
    }

    public static StatusEffect curse() {
        StatusEffect effect = new StatusEffect("Curse", 4, new LerpColor(Color.GRAY, Color.BLACK, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, .5f, .5f, .5f);
        return effect;
    }

    public static StatusEffect defenseless() {
        StatusEffect effect =  new StatusEffect("Defenseless", 1, new LerpColor(Color.WHITE, Color.NAVY, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 0, 1);
        return effect;
    }

    public static StatusEffect defenseless2() {
        StatusEffect effect =  new StatusEffect("Defenseless", 2, new LerpColor(Color.WHITE, Color.NAVY, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 0, 1);
        return effect;
    }

    public static StatusEffect offenseless() {
        StatusEffect effect = new StatusEffect("Offenseless", 2, new LerpColor(Color.WHITE, Color.RED, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, .5f, 1, 1);
        return effect;
    }

    public static StatusEffect offenseless2() {
        StatusEffect effect = new StatusEffect("Offenseless", 3, new LerpColor(Color.WHITE, Color.RED, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, .5f, 1, 1);
        return effect;
    }

    public static StatusEffect speedUp() {
        StatusEffect effect = new StatusEffect("Quick", 1, new LerpColor(Color.WHITE, Color.CYAN, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, 2);
        return effect;
    }

    public static StatusEffect speedUp2() {
        StatusEffect effect = new StatusEffect("Quick", 2, new LerpColor(Color.WHITE, Color.CYAN, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, 2);
        return effect;
    }

    public static StatusEffect speedUpAmp() {
        StatusEffect effect = new StatusEffect("QuickII", 2, new LerpColor(Color.WHITE, Color.CYAN, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, 3);
        return effect;
    }

    public static StatusEffect attackUp() {
        StatusEffect effect = new StatusEffect("Power", 1, new LerpColor(Color.WHITE, Color.ORANGE, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 2, 1, 1);
        return effect;
    }

    public static StatusEffect attackUp2() {
        StatusEffect effect = new StatusEffect("Power", 2, new LerpColor(Color.WHITE, Color.ORANGE, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 2, 1, 1);
        return effect;
    }

    public static StatusEffect attackUp3() {
        StatusEffect effect = new StatusEffect("Power", 3, new LerpColor(Color.WHITE, Color.ORANGE, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 2, 1, 1);
        return effect;
    }

    public static StatusEffect guardUp() {
        StatusEffect effect = new StatusEffect("Guard", 1, new LerpColor(Color.WHITE, Color.BLUE, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 2, 1);
        return effect;
    }

    public static StatusEffect guardUp2() {
        StatusEffect effect = new StatusEffect("Guard", 2, new LerpColor(Color.WHITE, Color.BLUE, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 2, 1);
        return effect;
    }

    public static StatusEffect guardUpAmp() {
        StatusEffect effect = new StatusEffect("GuardII", 1, new LerpColor(Color.WHITE, Color.BLUE, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 3, 1);
        return effect;
    }

    public static StatusEffect healthUp() {
        StatusEffect effect = new StatusEffect("Durability", 3, new LerpColor(Color.WHITE, Color.GREEN, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(2, 1, 1, 2, 1);
        return effect;
    }

    public static StatusEffect charged() {
        StatusEffect effect = new StatusEffect("Charged", 3, new LerpColor(Color.ORANGE, Color.YELLOW, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 2, 2, 1, 2);
        return effect;
    }

    public static StatusEffect supercharged() {
        StatusEffect effect = new StatusEffect("Supercharged", 4, new LerpColor(Color.ORANGE, Color.CYAN, .3f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 2, 5, 1, 3);
        return effect;
    }

    public static StatusEffect berserk() {
        StatusEffect effect = new StatusEffect("Berserk", 10, new LerpColor(Color.RED, Color.BLUE, .2f, Interpolation.sine), (e) -> {
            stm.get(e).hp -= 1;
            stm.get(e).sp += 1;
            if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                vm.get(e).heavyDamageAnimation.setPlaying(true, true);
        }, (entity) -> {
            entity.hp -= 1;
            entity.sp += 1;
        });

        effect.setStatChanges(.5f, 1, 5, 0, 2);
        return effect;
    }

    public static StatusEffect regeneration() {
        StatusEffect effect = new StatusEffect("Regeneration", 3, new LerpColor(Color.PINK, new Color(.8f, 1, .8f, 1f), .5f, Interpolation.sineOut), (e) -> {
            if (stm.has(e)) {
                stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 1, 0, stm.get(e).getModMaxHp(e));
            }
            if (vm.has(e) && !vm.get(e).shuffleAnimation.getIsPlaying())
                vm.get(e).shuffleAnimation.setPlaying(true, true);
        }, (entity -> {
            entity.hp += 1;
        }));
        effect.setStatChanges(1, 1, 1, 1, 1);
        return effect;
    }

    public static StatusEffect regenerationPlus() {
        StatusEffect effect = new StatusEffect("Regeneration+", 3, new LerpColor(Color.GOLD, Color.PINK, Interpolation.sineOut), (e) -> {
            if (stm.has(e)) {
                stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 1, 0, stm.get(e).getModMaxHp(e));
                stm.get(e).sp = MathUtils.clamp(stm.get(e).sp + 1, 0, stm.get(e).getModMaxSp(e));
            }
            if (vm.has(e) && !vm.get(e).shuffleAnimation.getIsPlaying())
                vm.get(e).shuffleAnimation.setPlaying(true, true);
        }, (entity -> {
            entity.hp += 1;
            entity.sp += 1;
        }));
        effect.setStatChanges(1, 1, 1, 1, 1);
        return effect;
    }
    //endregion

    //TODO create status effect info creator methods for each status effect (is more efficient). Should be done AFTER above TODO!
}
