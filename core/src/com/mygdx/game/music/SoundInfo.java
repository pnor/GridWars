package com.mygdx.game.music;

/**
 * Has the file path information for all sound effects in the game
 * @author Phillip O'Reggio
 */
public enum SoundInfo {
    //region Menus
    CONFIRM("Confirm", "soundEffects/menus/Confirm.wav"),
    SELECT("Select", "soundEffects/menus/Select.wav"),
    BACK("Back", "soundEffects/menus/Back.wav");
    //endregion
    //region Moves
    //endregion

    public final String NAME;
    public final String FILE_PATH;

    private SoundInfo(String name, String filePath) {
        this.NAME = name;
        this.FILE_PATH = filePath;
    }

}