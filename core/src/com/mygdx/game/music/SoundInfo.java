package com.mygdx.game.music;

/**
 * Has the file path information for all sound effects in the game
 * @author Phillip O'Reggio
 */
public enum SoundInfo {
    //region Menus
    CONFIRM("Confirm", "soundEffects/menus/Confirm.wav"),
    SELECT("Select", "soundEffects/menus/Select.wav"),
    BACK("Back", "soundEffects/menus/Back.wav"),
    TURN_SHIFT1("Turn Shift 1", "soundEffects/menus/TurnShift1.wav"),
    TURN_SHIFT2("Turn Shift 2", "soundEffects/menus/TurnShift2.wav"),
    POWER("Power", "soundEffects/menus/Power.wav"),
    //endregion
    //region Moves
    VOOM("Voom", "soundEffects/battle/Voom.wav");
    //endregion

    public final String NAME;
    public final String FILE_PATH;

    private SoundInfo(String name, String filePath) {
        this.NAME = name;
        this.FILE_PATH = filePath;
    }

}