package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.GridWars;
import com.mygdx.game.highscores.HighScore;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;
import music.Song;

import static com.mygdx.game.GridWars.atlas;
import static com.mygdx.game.GridWars.skin;

/**
 * Screen that is displayed when you get a Game Over in Survival mode
 * @author Phillip O'Reggio
 */
public class GameOverScreen extends MenuScreen implements Screen {
    private HighScore playerScore;
    private Label lblGameOver;
    private Label lblLastLevelReached;
    private Label lblScore;
    private HoverButton btnReturn;

    private float time = 0f;
    private int progress = 0;

    //which type of game over is it?
    private boolean playerGotNewHighScore;

    public GameOverScreen(int level, int score, int turns, Team team, GridWars gridWars) {
        super(gridWars);
        playerScore = new HighScore(team.getTeamName(), score, turns, level);
        playerScore.setTeamSprites(team);
    }

    @Override
    public void show() {
        super.show();

        //determine if player got a high score
        if (GRID_WARS.highScoreManager.getLowestScore().getScore() <= playerScore.getScore()) {
            playerGotNewHighScore = true;
        }
        //if they did add to highscores
        GRID_WARS.highScoreManager.addHighScoreObject(playerScore);
        GRID_WARS.highScoreManager.saveHighScores();

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        lblGameOver = new Label("Game Over", new Label.LabelStyle(fontGenerator.generateFont(param), Color.BLACK));
        lblGameOver.setColor(new Color(1, 1, 1, 0));
        param.size = 30;
        lblLastLevelReached = new Label("Floor : " + playerScore.getLastFloor(), new Label.LabelStyle(fontGenerator.generateFont(param), Color.BLACK));
        lblLastLevelReached.setColor(new Color(1, 1, 1, 0));
        lblScore = new Label("Score : " + playerScore.getScore(), new Label.LabelStyle(fontGenerator.generateFont(param), Color.BLACK));
        lblScore.setColor(new Color(1, 1, 1, 0));
        btnReturn = new HoverButton("Return", skin, Color.BLACK, Color.WHITE);
        btnReturn.setDisabled(true);
        btnReturn.setVisible(false);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == btnReturn) {
                        //set music
                        GRID_WARS.musicManager.setSong(Song.MENU_THEME);
                        if (playerGotNewHighScore)
                            GRID_WARS.setScreen(new HighScoreScreen(GRID_WARS));
                        else
                            GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
                    }
                }
            }
        };

        //set up tables and labels
        table.add(lblGameOver).padBottom(40).row();
        table.add(lblLastLevelReached).padBottom(60).row();
        table.add(lblScore).padBottom(60).row();
        table.add(btnReturn).size(160, 60);

        //set up background
        Sprite backLayer = new Sprite(atlas.findRegion("LightTile"));
        backLayer.setSize(1000, 900);
        background = new Background(backLayer,
                new Sprite[]{},
                new BackType[]{},
                null, null);

        //add listeners
        btnReturn.addListener(listener);

        //make save file unloadable
        GRID_WARS.saveDataManager.makeFileUnloadable();
        GRID_WARS.saveDataManager.saveSavedData();

        //set music
        GRID_WARS.musicManager.setSong(Song.GAME_OVER_THEME);

        fontGenerator.dispose();
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        time += delta;

        if (progress == 1)
            lblGameOver.setColor(new Color(
                    1,
                    1,
                    1,
                    MathUtils.clamp(time - 1, 0, 1)
            ));
        else if (progress == 2)
            lblLastLevelReached.setColor(new Color(
                    1,
                    1,
                    1,
                    MathUtils.clamp(time - 2f, 0, 1)
            ));
        else if (progress == 3)
            lblScore.setColor(new Color(
                    1,
                    1,
                    1,
                    MathUtils.clamp(time - 2f, 0, 1)
            ));
        else if (progress == 4)
            btnReturn.setColor(new Color(
                    0,
                    0,
                    0,
                    MathUtils.clamp((time - 3f), 0, 1)
            ));

        if (time >= 1f && progress == 0) {
            progress = 1;
        } else if (time >= 2f && progress == 1) {
            progress = 2;
        }  else if (time >= 3f && progress == 2) {
            progress= 3;
        }  else if (time >= 4f && progress == 3) {
            progress = 4;
            btnReturn.setVisible(true);
        } else if (time >= 5f && progress == 4) {
            progress = 6;
            btnReturn.setDisabled(false);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
