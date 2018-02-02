package music;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Class with enums representing each song. Songs with openers use {@link com.badlogic.gdx.audio.Music.OnCompletionListener} to switch to the rest of the song.
 * @author Phillip O'Reggio
 */
public enum  Song {
    LEVEL_1(Gdx.audio.newMusic(Gdx.files.internal("music/04_Level_1_Opener.ogg")), Gdx.audio.newMusic(Gdx.files.internal("music/04_Level 1.ogg"))),
    LEVEL_2(Gdx.audio.newMusic(Gdx.files.internal("music/05_Level 2_Opener.ogg")), Gdx.audio.newMusic(Gdx.files.internal("music/05_Level 2.ogg"))),
    LEVEL_3(Gdx.audio.newMusic(Gdx.files.internal("music/06_Level 3.ogg")), true),
    LEVEL_4(Gdx.audio.newMusic(Gdx.files.internal("music/07_Level 4.ogg")), true),
    LEVEL_6(Gdx.audio.newMusic(Gdx.files.internal("music/09_Level 6_Opener.ogg")), Gdx.audio.newMusic(Gdx.files.internal("music/09_Level 6.ogg"))),
    PASSWORD(Gdx.audio.newMusic(Gdx.files.internal("music/08 Password.ogg")), true),
    BATTLE_OPTIONS(Gdx.audio.newMusic(Gdx.files.internal("music/16 Battle Options_Opener.ogg")), Gdx.audio.newMusic(Gdx.files.internal("music/16 Battle Options.ogg"))),
    GAME_RESULTS(Gdx.audio.newMusic(Gdx.files.internal("music/26 Game Results.ogg")), false);

    private boolean hasOpener;
    private boolean loops;
    private Music music;

    /**
     * Creates a song that does not have an opener. Can loop or not loop when song finishes playing.
     */
    Song(Music song, boolean doesItLoop) {
        music = song;
        hasOpener = false;
        loops = doesItLoop;
    }

    /**
     * Creates a song that has an opener and loops
     * @param opener The opening segment of the song
     * @param restOfSong the rest of the song
     */
    Song(Music opener, Music restOfSong) {
        music = opener;
        music.setOnCompletionListener(music1 -> {
            float volume = music1.getVolume();
            music = restOfSong;
            music.setVolume(volume);
            music.setLooping(true);
            music.play();
        });
        hasOpener = true;
        loops = true;
    }

    /**
     * Plays the song.
     */
    public void play() {
        if (loops)
            music.setLooping(true);
        music.play();
    }

    /**
     * Stops the song
     */
    public void stop() {
        music.stop();
    }

    /**
     * Disposes of the song. This class should not be used after that happens.
     */
    public void dispose() {
        music.dispose();
    }

    public void setVolume(float volume) {
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
}
