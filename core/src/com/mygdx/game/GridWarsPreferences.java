package com.mygdx.game;

/**
 * Contains the key values for all of GridWars' saved data stored in preferences.
 */
public class GridWarsPreferences {
    // Name of Options Preferences
    public static String GRIDWARS_OPTIONS = "GridWars Options";
    public static String NOT_FIRST_TIME = "Not First Time"; /*** Whether this is the first time the user started the game. Stores Boolean */
    public static String MOVE_ANIMATION = "Move Animation"; /** Whether move animations show or not. Stores boolean */
    public static String AI_TURN_SPEED = "AI Turn Speed"; /** How quickly computer turns play out. Stores an int */
    public static String ANIMATE_BACKGROUND = "Animate Background"; /** Whether the background is animated. Stores boolean */
    public static String MUSIC_VOLUME = "Music Volume"; /** Music Volume. Stores a float between 0 and 1 */
    public static String SOUND_FX_VOLUME = "Sound FX Volume"; /** Sound Effects VOlume. Stores a float between 0 and 1 */
    public static String BEAT_THE_GAME = "Beat the Game"; /** Whether the player has beaten the game. Stores boolean */
    
    // Default Values
    public static boolean DEFAULT_MOVE_ANIMATION = true;
    public static int DEFAULT_AI_TURN_SPEED = 1;
    public static boolean DEFAULT_ANIMATE_BACKGROUND = true;
    public static float DEFAULT_MUSIC = .5f;
    public static float DEFAULT_SOUND_FX = .3f;
    // AI Turn Speeds (0: Slow, 1: Medium, 2: Fast, 3: Very Fast)
    // Movement
    public static float[] MOVEMENT_WAIT_TIME = {1f, .5f, .1f, 0.02f};
    public static float[] ATTACK_WAIT_TIME = {1.5f, 1f, .3f, 0.05f};
    public static float[] DISPLAY_WAIT_TIME = {1f, .5f, .25f, .08f};
}

