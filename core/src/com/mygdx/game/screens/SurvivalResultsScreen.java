package com.mygdx.game.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.mygdx.game.GridWars;
import com.mygdx.game.components.*;
import com.mygdx.game.highscores.HighScore;
import com.mygdx.game.misc.EventCompUtil;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.systems.EventSystem;
import com.mygdx.game.systems.LifetimeSystem;
import com.mygdx.game.systems.MovementSystem;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;
import com.mygdx.game.music.Song;

import static com.mygdx.game.ComponentMappers.am;
import static com.mygdx.game.ComponentMappers.sm;
import static com.mygdx.game.GridWars.*;

/**
 * Screen that shows the player results after completing all 50 floors of Survival
 * @author Phillip O'Reggio
 */
public class SurvivalResultsScreen extends MenuScreen implements Screen {
    private Team team;
    private int points;
    private int turnCount;

    //UI elements
    private Image[] teamImages;
    private Label lblPoints;
    private Label lblTotalTurns;

    //highscore related
    private boolean playerGotNewHighScore;
    private HighScore playerScore;

    //time keeping
    private float currentTime;
    private int displayProgress;
    /** Time between each display of an UI element */
    private final float INTERVAL_TIME = .4f;
    /** Time before doing anything on the screen */
    private final float BUFFER_TIME = 1f;

    //whether this was loaded form save file
    private boolean loadedFromSave;

    public SurvivalResultsScreen(int level, int score, int turns, boolean loadedFromSave, Team team, GridWars gridWars) {
        super(gridWars);
        playerScore = new HighScore(team.getTeamName(), score, turns, level);
        playerScore.setTeamSprites(team);
        this.team = team;
        this.points = score;
        this.turnCount = turns;
        this.loadedFromSave  = loadedFromSave;
    }

    @Override
    public void show() {
        super.show();

        //set up engine with more systems (for particle effects)
        engine.addSystem(new EventSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new MovementSystem());

        //determine if player got a high score
        if (GRID_WARS.highScoreManager.getLowestScore().getScore() <= playerScore.getScore()) {
            playerGotNewHighScore = true;
        }
        //if they did add to highscores
        GRID_WARS.highScoreManager.addHighScoreObject(playerScore);
        GRID_WARS.highScoreManager.saveHighScores();

        //Set "Beat the Game" data to true
        Preferences preferences = Gdx.app.getPreferences("GridWars Options");
        preferences.putBoolean("Beat the Game", true);
        preferences.flush();

        //make save file unloadable
        if (loadedFromSave) {
            GRID_WARS.saveDataManager.makeFileUnloadable();
            GRID_WARS.saveDataManager.saveSavedData();
        }

        //create Labels
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        Label titleLbl = new Label("Success!", new Label.LabelStyle(fontGenerator.generateFont(param), Color.BLACK));
        param.size = 20;
        lblPoints = new Label("Points : " + points, new Label.LabelStyle(fontGenerator.generateFont(param), Color.BLACK));
        lblTotalTurns = new Label("Turn Count : " + turnCount, new Label.LabelStyle(fontGenerator.generateFont(param), Color.BLACK));
        //set up images and put in table
        teamImages = new Image[4];
        for (int i = 0; i < teamImages.length; i++)
            teamImages[i] = new Image();
        for (int i = 0; i < teamImages.length; i++) {
            if (i < team.getEntities().size) {
                teamImages[i].setDrawable(new SpriteDrawable(am.get(team.getEntities().get(i)).actor.getSprite()));
            } else
                teamImages[i].setDrawable(new SpriteDrawable(atlas.createSprite("cube")));
        }
        //Create button
        HoverButton btnReturn = new HoverButton("Return", skin, Color.WHITE, Color.DARK_GRAY);
        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == btnReturn) {
                        GRID_WARS.musicManager.setSong(Song.MENU_THEME);
                        if (playerGotNewHighScore)
                            GRID_WARS.setScreen(new HighScoreScreen(GRID_WARS));
                        else
                            GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
                    }
                }
            }
        };
        btnReturn.addListener(listener);

        //add to main table
        table.add(titleLbl).padBottom(80).colspan(4).row();
        table.add(teamImages[0]).padRight(50);
        table.add(teamImages[1]).padRight(50);
        table.add(teamImages[2]).padRight(50);
        table.add(teamImages[3]).row();
        table.add(lblPoints).padTop(60).padBottom(60).colspan(4).row();
        table.add(lblTotalTurns).padBottom(60).colspan(4).row();
        table.add(btnReturn).colspan(4).size(200, 60);

        //make everything invisible for appearing effect
        for (Image i : teamImages)
            i.setVisible(false);
        lblPoints.setVisible(false);
        lblTotalTurns.setVisible(false);

        //set up background
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(0.4f, .7f, 1f, 1));
        Sprite sun = new Sprite(backAtlas.findRegion("RadiusFade"));
        sun.setColor(new Color(1, .4f, 0, .5f));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagStripeHoriz"));
        overlay.setColor(new Color(1, 1, 1, .2f));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("DiagCheckerBackground"));
        overlay2.setColor(new Color(1, 1, 1, .1f));
        Sprite overlay3 = new Sprite(backAtlas.findRegion("CloudBackground"));
        overlay3.setColor(new Color(1, 1, 1, .3f));
        background = new Background(
                back,
                new Sprite[] {sun, overlay, overlay2, overlay3},
                new BackType[] {BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_SLOW ,BackType.SCROLL_HORIZONTAL_SLOW},
                new Color(1, .4f, 0, .5f), new Color(1, .9f, .6f, .8f));
        setBackgound(background);

        //add screen transition entity
        Entity whiteCover = new Entity();
        Sprite brightness = (atlas.createSprite("LightTile"));
        whiteCover.add(new SpriteComponent(brightness));
        brightness.setColor(new Color(1, 1, 1, 1));
        whiteCover.add(new PositionComponent(new Vector2(0, 0), stage.getHeight(), stage.getWidth(), 0));
        whiteCover.add(new EventComponent(.005f, 0, true, true, (entity, engine) -> {
            sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().cpy().add(0, 0, 0, -.05f));
        }));
        engine.addEntity(whiteCover);

        //set music
        GRID_WARS.musicManager.setSong(Song.GAME_RESULTS_SURVIVAL);
    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        engine.update(deltaTime);
        currentTime += deltaTime;
        //update variable displayProgress
        if (currentTime >= BUFFER_TIME && displayProgress == 0) {
            displayProgress++;
            currentTime = 0;
        }
        if (currentTime >= INTERVAL_TIME && displayProgress <= 7) {
            displayProgress++;
            currentTime = 0;
        }
        //show UI elements
        if (displayProgress == 2)
            teamImages[0].setVisible(true);
        else if (displayProgress == 3)
            teamImages[1].setVisible(true);
        else if (displayProgress == 4)
            teamImages[2].setVisible(true);
        else if (displayProgress == 5)
            teamImages[3].setVisible(true);
        else if (displayProgress == 6)
            lblPoints.setVisible(true);
        else if (displayProgress == 7)
            lblTotalTurns.setVisible(true);
        else if (displayProgress == 8) {
            //generate sparkle
            if (MathUtils.randomBoolean(.2f)) {
                Entity sparkle = new Entity();
                Vector2 position = new Vector2(MathUtils.random(0, stage.getWidth()), MathUtils.random(0, stage.getHeight()));
                sparkle.add(new PositionComponent(position, 23, 23, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 30)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.03f, true, EventCompUtil.fadeOutAfter(10, 10)));

                engine.addEntity(sparkle);
            }
        }
    }
}
