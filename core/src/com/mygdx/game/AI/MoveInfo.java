package com.mygdx.game.AI;

/**
 * Class containing information for the effects of a {@link com.mygdx.game.move_related.Move}. Is used in {@link BoardState}
 * to properly change the values in a {@link EntityValue} to reflect the effect of a move. This allows the computer to choose
 * a good turn, based on the likely state of the board.
 *
 * @author Phillip O'Reggio
 */
public class MoveInfo {
    /**
     * Whether it ignores the targets defense
     */
    public boolean pierces;
    /**
     * Amount it multiplies the user's attack stat
     */
    public float ampValue;
    /**
     * Status effects it inflicts
     */
    public StatusEffectInfo[] statusEffects;
    /**
     * Other effects
     */
    public MiscEffects miscEffects;

    /**
     * Creates a {@link MoveInfo} object that represents a move with no special effects are status effect causes to it.
     * @param pierce whether it ignores the opponent's defense
     * @param amp how much the attack alters the base attack of the user. For example, a double damage attack would have a
     *            amp value of 2.
     */
    public MoveInfo(boolean pierce, float amp) {
        pierces = pierce;
        ampValue = amp;
    }

    /**
     * Creates a {@link MoveInfo} object that represents a move that causes a status effect.
     * @param pierce whether it pierces
     * @param amp hom much the attack alters the base attack of the user. For example, a double damage attack would have a
     *            amp value of 2.
     * @param statusEffectInformation what status effects it cuases
     */
    public MoveInfo(boolean pierce, float amp, StatusEffectInfo... statusEffectInformation) {
        pierces = pierce;
        ampValue = amp;
        statusEffects = statusEffectInformation;
    }

    /**
     * Creates a {@link MoveInfo} object that represents a move with other misc. effects.
     * @param pierce whether it pierces
     * @param amp hom much the attack alters the base attack of the user. For example, a double damage attack would have an
     *            amp value of 2.
     * @param miscEffect the misc. effect caused by this attack
     */
    public MoveInfo(boolean pierce, float amp, MiscEffects miscEffect) {
        pierces = pierce;
        ampValue = amp;
        miscEffects = miscEffect;
    }

    /**
     * Creates a {@link MoveInfo} object that represents a move with other misc. effects. and causes a status effect.
     * @param pierce whether it pierces
     * @param amp how much the attack alters the base attack of the user. For example, a double damage attack would have an amp value
     *            of 2.
     * @param statusEffectInformation status effect caused by this attack
     * @param miscEffect the misc. effect caused by this attack
     */
    public MoveInfo(boolean pierce, float amp, StatusEffectInfo statusEffectInformation, MiscEffects miscEffect) {
        pierces = pierce;
        ampValue = amp;
        statusEffects = new StatusEffectInfo[]{statusEffectInformation};
        miscEffects = miscEffect;
    }

    /**
     * Creates a {@link MoveInfo} object that represents a move with other misc. effects. and causes multiple status effects.
     * @param pierce whether it pierces
     * @param amp how much the attack alters the base attack of the user. For example, a double damage attack would have an amp value
     *            of 2.
     * @param statusEffectInformation status effects caused by this attack
     * @param miscEffect the misc. effect caused by this attack
     */
    public MoveInfo(boolean pierce, float amp, StatusEffectInfo[] statusEffectInformation, MiscEffects miscEffect) {
        pierces = pierce;
        ampValue = amp;
        statusEffects = statusEffectInformation;
        miscEffects = miscEffect;
    }

    /**
     * Class representing all miscellaneous effects of an attack. For example, giving sp to an Entity, or healing them a
     * set amount.
     */
    public interface MiscEffects {
        /**
         * Miscellaneous effect caused from an attack
         * @param entity enemy
         */
        public void doMiscEffects(EntityValue entity);
    }
}
