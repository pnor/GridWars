package com.mygdx.game.music;

/**
 * Contains the information for the filepath of classes and whether the song would loop
 * @author Phillip O'Reggio
 */
public enum SongInfo {
    STAGE_THEME("music/04_Level_1_Opener.ogg", "music/04_Level 1.ogg", (byte) 1),
    STAGE_THEME_2("music/05_Level 2_Opener.ogg", "music/05_Level 2.ogg", (byte) 2),
    STAGE_THEME_3("music/06_Level 3.ogg", true, (byte) 3),
    STAGE_THEME_4("music/07_Level 4.ogg", true, (byte) 4),
    STAGE_THEME_5("music/06_BGM 2_Opener.ogg", "music/06_BGM 2.ogg", (byte) 5),
    STAGE_ALT_1("music/08_BGM 4_Opener.ogg", "music/08_BGM 4.ogg", (byte) 6),
    STAGE_ALT_2("music/12 Bgm 3.ogg", true, (byte) 7),
    STAGE_ALT_3("music/BombFM3_Opener.ogg", "music/BombFM3.ogg", (byte) 8),
    STAGE_ALT_4("music/BombFM13.ogg", true, (byte) 9),
    DANGER_THEME("music/19_Dastardly Bomber_Opener.ogg", "music/19_Dastardly Bomber.ogg", (byte) 10),
    BOSS_THEME("music/22 sirius ii_Opener.ogg", "music/22 sirius ii.ogg", (byte) 11),
    BOSS_THEME_2("music/23 sirius iii_Opener.ogg", "music/23 sirius iii.ogg", (byte) 12),
    FINAL_BOSS_THEME("music/12_Bomberman Wars.ogg", true, (byte) 13),
    MENU_THEME("music/08 Password.ogg", true, (byte) 14),
    SURVIVAL_TOWER_THEME("music/16 Battle Options_Opener.ogg", "music/16 Battle Options.ogg", (byte) 15),
    SURVIVAL_TOWER_THEME_2("music/Battle Options 2.ogg", true, (byte) 16),
    GAME_RESULTS("music/26 Game Results.ogg", false, (byte) 17),
    GAME_RESULTS_SURVIVAL("music/26_Multi-Player Victory_Opener.ogg", "music/26_Multi-Player Victory.ogg", (byte) 18),
    GAME_OVER_THEME("music/14_Battle Draw.ogg", false, (byte) 19);

    public final String INTRO_PATH;
    public final String MAIN_PATH;
    public final boolean LOOPS;
    public final byte ID;
    

    /**
     * Information representing a song that has an opener and loops
     * @param INTRO_PATH The opening segment of the song
     * @param MAIN_PATH the rest of the song
     */
    private SongInfo(String INTRO_PATH, String MAIN_PATH, byte ID) {
        this.INTRO_PATH = INTRO_PATH;
        this.MAIN_PATH = MAIN_PATH;
        this.LOOPS = true;
        this.ID = ID;
    }

    /**
     * Information representing a song that does not have an opener. Can or cannot loop when song finishes playing.
     */
    private SongInfo(String MAIN_PATH, boolean loops, byte ID) {
        this.MAIN_PATH = MAIN_PATH;
        this.INTRO_PATH = null;
        this.LOOPS = loops;
        this.ID = ID;
    }
}