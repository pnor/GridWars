package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * @author Phillip O'Reggio
 */
public class MusicManager {
    private Music currentSong;
    private boolean isPlaying;
    /**
     * Volume of the music played on highscores scale of 0 to 1 where 0 is muted and 1 is max volume
     */
    private float volume = 1;

    public void makeSongAndPlayIt() {
        currentSong = Gdx.audio.newMusic(Gdx.files.internal("music/08_Password.ogg"));
        isPlaying = true;
        currentSong.setLooping(true);
        currentSong.setVolume(volume);
        currentSong.play();
    }

    /**
     * Changes the volume of the music and changes the stored volume amount
     * @param newVolume new music volume
     */
    public void setMusicVolume(float newVolume) {
        volume = newVolume;
        currentSong.setVolume(volume);
    }

}
