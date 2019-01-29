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
    AFFIRM("Afirm", "soundEffects/battle/Affirm.ogg"),
    BEEP_DECAY("Bubble Burst", "soundEffects/battle/BeepDecay.ogg"),
    BLIP_UP("Blip Up", "soundEffects/battle/BlipUp.ogg"),
    BOOM("Boom", "soundEffects/battle/Boom.ogg"),
    BOOM_DECAY("Boom Decay", "soundEffects/battle/BoomDecay.ogg"),
    BREATH_HEAVY("Breath Heavy", "soundEffects/battle/BreathHeavy.ogg"),
    BREATH_LIGHT("Breath Light", "soundEffects/battle/BreathLight.ogg"),
    BUBBLE("Bubble", "soundEffects/battle/Bubble.ogg"),
    BUBBLE2("Bubble 2", "soundEffects/battle/Bubble2.ogg"),
    BUBBLE_BURST("Bubble Burst", "soundEffects/battle/BubbleBurst.ogg"),
    BUFF("Buff", "soundEffects/battle/Buff.ogg"),
    BURN_DOWN("Burn Down", "soundEffects/battle/BurnDown.ogg"),
    BURN_UP("Burn Up", "soundEffects/battle/BurnUp.ogg"),
    CLAW("Claw", "soundEffects/battle/Claw.ogg"),
    COIN("Coin", "soundEffects/battle/Coin.ogg"),
    COMPUTER("Computer", "soundEffects/battle/Computer.ogg"),
    CURIOUS("Curious", "soundEffects/battle/Curious.ogg"),
    DEBUFF("Debuff", "soundEffects/battle/Debuff.ogg"),
    DEEP("Deep", "soundEffects/battle/Deep.ogg"),
    DEEP_SHIFT("Deep Shift", "soundEffects/battle/DeepShift.ogg"),
    DOWNER("Downer", "soundEffects/battle/Downer.ogg"),
    DRIPPING("Dripping", "soundEffects/battle/Dripping.ogg"),
    FANCY_BOOM("Fancy Boom", "soundEffects/battle/FancyBoom.ogg"),
    FIRE_BURNING("Fire Burning", "soundEffects/battle/FireBurning.ogg"),
    FIRE_BURNING_LOW("Fire Burning", "soundEffects/battle/FireBurningLow.ogg"),
    FIRE_START("Fire Start", "soundEffects/battle/FireStart.ogg"),
    FLYBY("Flyby", "soundEffects/battle/FlyBy.ogg"),
    FORM_SHIFT("Form Shift", "soundEffects/battle/FormShift.ogg"),
    FUTURE("Future", "soundEffects/battle/Future.ogg"),
    GOO("Goo", "soundEffects/battle/Goo.ogg"),
    GUNSHOT("Gunshot", "soundEffects/battle/Gunshot.ogg"),
    HIGH("High", "soundEffects/battle/High.ogg"),
    HIT("Hit", "soundEffects/battle/Hit.ogg"),
    HUNGER("Hunger", "soundEffects/battle/Hunger.ogg"),
    KNOCK("Knock", "soundEffects/battle/Knock.ogg"),
    LASER("Laser", "soundEffects/battle/Laser.ogg"),
    LASER_ALT("Laser Alt", "soundEffects/battle/LaserAlt.ogg"),
    LASER_LONG("Laser Long", "soundEffects/battle/LaserLong.ogg"),
    MINI_STAR_WOOSH("Mini Star Woosh", "soundEffects/battle/MiniStarWoosh.ogg"),
    MYSTERY("Mystery", "soundEffects/battle/Mystery.ogg"),
    PING("Ping", "soundEffects/battle/Ping.ogg"),
    RAPID_SAW("Rapid Saw", "soundEffects/battle/RapidSaw.ogg"),
    RINGING("Ringing", "soundEffects/battle/Ringing.ogg"),
    SHIM("Shim", "soundEffects/battle/Shim.ogg"),
    SLIDE_DOWN("Slide Down", "soundEffects/battle/SlideDown.ogg"),
    SLIDE_UP("Slide Up", "soundEffects/battle/SlideUp.ogg"),
    SPACE_CAW("Space Claw", "soundEffects/battle/SpaceClaw.ogg"),
    SPACE_OUT("Space Out", "soundEffects/battle/SpaceOut.ogg"),
    SPACE_SHOCK("Space Shock", "soundEffects/battle/SpaceShock.ogg"),
    SPIRAL("Spiral", "soundEffects/battle/Spiral.ogg"),
    STAR_PASS("Star Pass", "soundEffects/battle/StarPass.ogg"),
    STAR_WOOSH("Star Woosh", "soundEffects/battle/StarWoosh.ogg"),
    STATUS_ZAP("Status Zap", "soundEffects/battle/StatusZap.ogg"),
    STRANGE("Strange", "soundEffects/battle/Strange.ogg"),
    SWIRL_WIND("Swirl Wind", "soundEffects/battle/SwirlWind.ogg"),
    SWORD_SWIPE("Sword Swipe", "soundEffects/battle/SwordSwipe.ogg"),
    VEEM("Veem", "soundEffects/battle/Veem.ogg"),
    VOOM("Voom", "soundEffects/battle/Voom.ogg"),
    WARP("Warp", "soundEffects/battle/Warp.ogg"),
    WARP_FAST("Warp Fast", "soundEffects/battle/WarpFast.ogg"),
    WIP_REPEAT("Wip Repeat", "soundEffects/battle/WipRepeat.ogg"),
    ZEP("Zep", "soundEffects/battle/Zep.ogg");
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