package com.mygdx.game.music;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Class with enums representing each song. Songs with openers use {@link com.badlogic.gdx.audio.Music.OnCompletionListener} to switch to the rest of the song.
 * Has methods for switching stopping, starting, and resetting music.
 * @author Phillip O'Reggio
 */
public enum  Song {
    STAGE_THEME(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/04_Level_1_Opener.ogg")), Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/04_Level 1.ogg"))),
    STAGE_THEME_2(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/05_Level 2_Opener.ogg")), Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/05_Level 2.ogg"))),
    STAGE_THEME_3(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/06_Level 3.ogg")), true),
    STAGE_THEME_4(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/07_Level 4.ogg")), true),
    STAGE_THEME_5(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/06_BGM 2_Opener.ogg")), Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/06_BGM 2.ogg"))),
    STAGE_THEME_6(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/09_Level 6_Opener.ogg")), Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/09_Level 6.ogg"))),
    FINAL_BOSS_THEME(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/12_Bomberman Wars.ogg")), true),
    MENU_THEME(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/08 Password.ogg")), true),
    SURVIVAL_TOWER_THEME(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/16 Battle Options_Opener.ogg")), Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/16 Battle Options.ogg"))),
    GAME_RESULTS(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/26 Game Results.ogg")), false),
    GAME_OVER_THEME(Gdx.audio.newMusic(Gdx.files.internal("com/mygdx/game/music/14_Battle Draw.ogg")), false);

    private boolean hasOpener;
    private boolean loops;
    private boolean playingOpener;
    private Music opener;
    private Music music;

    /**
     * Creates a song that does not have an opener. Can loop or not loop when song finishes playing.
     */
    Song(Music song, boolean doesItLoop) {
        music = song;
        music.setLooping(doesItLoop);
        hasOpener = false;
        loops = doesItLoop;
    }

    /**
     * Creates a song that has an opener and loops
     * @param musicOpener The opening segment of the song
     * @param restOfSong the rest of the song
     */
    Song(Music musicOpener, Music restOfSong) {
        opener = musicOpener;
        music = restOfSong;
        opener.setOnCompletionListener(music1 -> {
            opener.stop();
            music.play();
        });
        music.setLooping(true);
        playingOpener = true;
        hasOpener = true;
        loops = true;
    }

    /**
     * Starts over the song, and plays it.
     */
    public void play() {
        startOver();
        if (hasOpener) {
            playingOpener = true;
            opener.setOnCompletionListener(music1 -> {
                opener.stop();
                music.play();
            });
            opener.play();
        } else {
            music.play();
        }
    }

    /**
     * Sets the song's playback time to 0.
     */
    public void startOver() {
        if (hasOpener) {
            opener.setPosition(0);
        }
        music.setPosition(0);
    }

    /**
     * Stops the song
     */
    public void stop() {
        if (playingOpener)
            opener.stop();
        else
            music.stop();
    }

    /**
     * Disposes of the song. This class should not be used after that happens.
     */
    public void dispose() {
        if (hasOpener) {
            opener.stop();
            opener.dispose();
        }
        music.stop();
        music.dispose();
        playingOpener = false;
    }

    public void setVolume(float volume) {
        if (hasOpener)
            opener.setVolume(volume);
        music.setVolume(volume);
    }

    public float getVolume() {
        return music.getVolume();
    }

    public boolean getLooping() {
        return loops;
    }

    public boolean getHasOpener() {
        return hasOpener;
    }

    public boolean getPlayingOpener() {
        return playingOpener;
    }

    public Music getMusic() {
        return music;
    }

    public Music getOpener() {
        return opener;
    }
}
