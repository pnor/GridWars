package com.mygdx.game.music;

/**
 * Has the file path information for all sound effects in the game
 * @author Phillip O'Reggio
 */
public enum SoundInfo {
    //region Menus
    CONFIRM("Confirm", "soundEffects/menus/Confirm.ogg"),
    SELECT("Select", "soundEffects/menus/Select.ogg"),
    BACK("Back", "soundEffects/menus/Back.ogg"),
    TURN_SHIFT1("Turn Shift 1", "soundEffects/menus/TurnShift1.ogg"),
    TURN_SHIFT2("Turn Shift 2", "soundEffects/menus/TurnShift2.ogg"),
    POWER("Power", "soundEffects/menus/Power.ogg"),
    //endregion
    //region Moves
    BOOM("Boom", "soundEffects/battle/Boom.ogg"),
    BOOM_DECAY("Boom Decay", "soundEffects/battle/BoomDecay.ogg"),
    BUBBLE("Bubble", "soundEffects/battle/Bubble.ogg"),
    BUBBLE2("Bubble 2", "soundEffects/battle/Bubble2.ogg"),
    BUBBLE_BURST("Bubble Burst", "soundEffects/battle/BubbleBurst.ogg"),
    BUFF("Buff", "soundEffects/battle/Buff.ogg"),
    CLAW("Claw", "soundEffects/battle/Claw.ogg"),
    CURIOUS("Curious", "soundEffects/battle/Curious.ogg"),
    DEBUFF("Debuff", "soundEffects/battle/Debuff.ogg"),
    DRIPPING("Dripping", "soundEffects/battle/Dripping.ogg"),
    FANCY_BOOM("Fancy Boom", "soundEffects/battle/FancyBoom.ogg"),
    FIRE_START("Fire Start", "soundEffects/battle/FireStart.ogg"),
    LASER("Laser", "soundEffects/battle/Laser.ogg"),
    LASER_ALT("Laser Alt", "soundEffects/battle/LaserAlt.ogg"),
    LASER_LONG("Laser Long", "soundEffects/battle/LaserLong.ogg"),
    MINI_STAR_WOOSH("Mini Star Woosh", "soundEffects/battle/MiniStarWoosh.ogg"),
    MYSTERY("Mystery", "soundEffects/battle/Mystery.ogg"),
    SPACE_CAW("Space Claw", "soundEffects/battle/SpaceClaw.ogg"),
    SPACE_OUT("Space Out", "soundEffects/battle/SpaceOut.ogg"),
    SPACE_SHOCK("Space Shock", "soundEffects/battle/SpaceShock.ogg"),
    SPIRAL("Spiral", "soundEffects/battle/Spiral.ogg"),
    STAR_WOOSH("Star Woosh", "soundEffects/battle/StarWoosh.ogg"),
    STATUS_ZAP("Status Zap", "soundEffects/battle/StatusZap.ogg"),
    SWIRL_WIND("Swirl Wind", "soundEffects/battle/SwirlWind.ogg"),
    SWORD_SWIPE("Sword Swipe", "soundEffects/battle/SwordSwipe.ogg"),
    VOOM("Voom", "soundEffects/battle/Voom.ogg"),
    WARP("Warp", "soundEffects/battle/Warp.ogg"),
    WARP_FAST("Warp Fast", "soundEffects/battle/WarpFast.ogg");
    //endregion

    public final String NAME;
    public final String FILE_PATH;

    private SoundInfo(String name, String filePath) {
        this.NAME = name;
        this.FILE_PATH = filePath;
    }

    @Override 
    public String toString() {
        return NAME + ": " + FILE_PATH;
    }

}