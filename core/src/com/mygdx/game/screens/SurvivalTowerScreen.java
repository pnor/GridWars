package com.mygdx.game.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.AI.ComputerPlayer;
import com.mygdx.game.GridWars;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.components.*;
import com.mygdx.game.creators.EntityConstructor;
import com.mygdx.game.creators.StatusEffectConstructor;
import com.mygdx.game.highscores.SaveData;
import com.mygdx.game.misc.EventCompUtil;
import com.mygdx.game.music.*;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.systems.EventSystem;
import com.mygdx.game.systems.LifetimeSystem;
import com.mygdx.game.systems.MovementSystem;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;
import com.mygdx.game.ui.LerpColor;
import com.mygdx.game.ui.LerpColorManager;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.*;

/**
 * The screen that shows the Tower and your team's progress. Has methods to determine what floor to place the player on and what enemies should
 * populate said floor.
 *
 * @author Phillip O'Reggio
 */
public class SurvivalTowerScreen extends MenuScreen implements Screen {
    private Team team;
    private Image[] teamImages;
    private int level;
    private int healingPowerUp;
    private int spPowerUp;
    private int powerPowerUp;
    private int speedPowerUp;
    private Sprite backgroundProgressBar;
    //score keeping
    private int points;
    private int numberOfTurns;
    //Whether this is a loaded save
    private boolean loadedFromSave;
    //LerpColor Manager that is used throughout Survival Mode
    private static LerpColorManager survivalLerpColorManager;

    public SurvivalTowerScreen(Team playerTeam, int towerLevel, int healingPowerUpAmount, int spUpPowerUpAmount, int attackPowerUpAmount,
                               int speedPowerUpAmount, int points, int turnCount, boolean loadedFromSave, GridWars game) {
        super(game);
        team = playerTeam;
        level = towerLevel;
        healingPowerUp = healingPowerUpAmount;
        spPowerUp = spUpPowerUpAmount;
        powerPowerUp = attackPowerUpAmount;
        speedPowerUp = speedPowerUpAmount;
        this.points = points;
        numberOfTurns = turnCount;
        this.loadedFromSave = loadedFromSave;
        // Makes sure Survival mode always has a lerp color manager, even outside of battle screens
        if (survivalLerpColorManager == null) {
            survivalLerpColorManager = new LerpColorManager();
        }
        if (StatusEffectComponent.getLerpColorManager() != survivalLerpColorManager) {
            StatusEffectComponent.setLerpColorManager(survivalLerpColorManager);
        }
    }

    @Override
    public void render(float deltaTime) {
        //background animation
        stage.getBatch().begin();
        backgroundProgressBar.draw(stage.getBatch());

        stage.getBatch().end();
        super.render(deltaTime);
        engine.update(deltaTime);
        //Debug--Change floor level from Survival Select Screen
        if (GridWars.DEBUGGING) {
            checkDebugInputs();
        }
    }

    @Override
    public void show() {
        super.show();
        //set up engine with more systems (For particle effects)
        engine.addSystem(new EventSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new MovementSystem());

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        Label titleLbl = new Label("Tower Survival", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        HoverButton btnRestore = new HoverButton("Restore", skin, Color.GRAY, Color.GREEN);
        HoverButton btnSpUp = new HoverButton("SP Up", skin, Color.GRAY, Color.ORANGE);
        HoverButton btnSpeedUp = new HoverButton("Speed +", skin, Color.GRAY, Color.CYAN);
        HoverButton btnPowerUp = new HoverButton("Power +", skin, Color.GRAY, Color.RED);
        HoverButton btnContinue = new HoverButton("Continue", skin, Color.WHITE, Color.GREEN);
        HoverButton btnSave = new HoverButton("Save", skin, Color.WHITE, Color.TEAL);
        teamImages = new Image[4];
        for (int i = 3; i >= team.getEntities().size; i--)
            teamImages[i] = new Image(atlas.createSprite("cube"));
        for (int i = 0; i < team.getEntities().size; i++) {
            Sprite s;
            if (am.get(team.getEntities().get(i)).actor instanceof SpriteActor)
                s = ((SpriteActor) am.get(team.getEntities().get(i)).actor).getSprite();
            else
                s = ((AnimationActor) am.get(team.getEntities().get(i)).actor).getInitialFrame();
            teamImages[i] = new Image(s);
            float healthPercent = (float) stm.get(team.getEntities().get(i)).hp / (float) stm.get(team.getEntities().get(i)).maxHP;
            Color color = new Color(1, healthPercent, healthPercent, 1);
            teamImages[i].setColor(color);
        }
        Label lblHealthPower = new Label("Remaining : " + healingPowerUp, skin);
        Label lblSPPower = new Label("Remaining : " + spPowerUp, skin);
        Label lblPower = new Label("Remaining : " + powerPowerUp, skin);
        Label lblSpeedUp = new Label("Remaining : " + speedPowerUp, skin);

        //background progress bar
        backgroundProgressBar = new Sprite(backAtlas.createSprite("BlankBackground"));
        backgroundProgressBar.setSize(1000, 900);
        Color towerColor;
        //level 1-9
        if (level >= 1 && level <= 10)
            towerColor = Color.RED;
        //level 11-19
        else if (level >= 11 && level <= 20)
            towerColor = Color.BLUE;
        //level 21-30
        else if (level >= 21 && level <= 30)
            towerColor = Color.YELLOW;
        //level 31-40
        else if (level >= 31 && level <= 40)
            towerColor = Color.PURPLE;
        //level 41-49
        else if (level >= 41 && level <= 49)
            towerColor = Color.CYAN;
        //50
        else
            towerColor = Color.WHITE;
        backgroundProgressBar.setColor(towerColor);
        backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == btnRestore) {
                        if (healingPowerUp > 0) {
                            GRID_WARS.soundManager.playSound(SoundInfo.POWER);
                            btnRestore.setDisabled(true);
                            healingPowerUp--;
                            lblHealthPower.setText("Remaining : " + healingPowerUp);
                            createParticleEffect(0);
                            for (Entity e : team.getEntities()) {
                                stm.get(e).hp = stm.get(e).maxHP;
                                stm.get(e).setAlive();
                            }
                            for (Image image : teamImages) {
                                image.setColor(Color.WHITE);
                            } 
                        } else {
                            GRID_WARS.soundManager.playSound(SoundInfo.BACK);
                        }
                    } else if (actor == btnSpUp) {
                        if (spPowerUp > 0) {
                            btnSpUp.setDisabled(true);
                            GRID_WARS.soundManager.playSound(SoundInfo.POWER);
                            spPowerUp--;
                            lblSPPower.setText("Remaining : " + spPowerUp);
                            createParticleEffect(1);
                            for (Entity e : team.getEntities()) {
                                stm.get(e).sp = stm.get(e).getModMaxSp(e);
                            }
                        } else {
                            GRID_WARS.soundManager.playSound(SoundInfo.BACK);
                        }
                    } else if (actor == btnPowerUp) {
                        if (powerPowerUp > 0) {
                            GRID_WARS.soundManager.playSound(SoundInfo.POWER);
                            btnPowerUp.setDisabled(true);
                            powerPowerUp--;
                            lblPower.setText("Remaining : " + powerPowerUp);
                            createParticleEffect(2);
                            for (Entity e : team.getEntities()) {
                                if (status.has(e))
                                    status.get(e).addStatusEffect(StatusEffectConstructor.attackUp(3), e);
                            }
                        } else {
                            GRID_WARS.soundManager.playSound(SoundInfo.BACK);
                        }
                    } else if (actor == btnSpeedUp) {
                        if (speedPowerUp > 0) {
                            GRID_WARS.soundManager.playSound(SoundInfo.POWER);
                            btnSpeedUp.setDisabled(true);
                            speedPowerUp--;
                            lblSpeedUp.setText("Remaining : " + speedPowerUp);
                            createParticleEffect(3);
                            for (Entity e : team.getEntities()) {
                                if (status.has(e))
                                    status.get(e).addStatusEffect(StatusEffectConstructor.speedUp(3), e);
                            }
                        } else {
                            GRID_WARS.soundManager.playSound(SoundInfo.BACK);
                        }
                    } else if (actor == btnContinue) {
                        GRID_WARS.soundManager.playSound(SoundInfo.CONFIRM);
                        //get the song
                        Song song = getFloorLevelSong();
                        Team attackingObjectsTeam = getFloorLevelAttackingObjects();
                        if (attackingObjectsTeam == null) // floor has no attacking objects
                            GRID_WARS.setScreen(new SurvivalBattleScreen(team, getFloorLevelTeam(), getComputerDifficulty(),
                                    level, healingPowerUp, spPowerUp, powerPowerUp, speedPowerUp, points, numberOfTurns, loadedFromSave,
                                    survivalLerpColorManager, song, GRID_WARS));
                        else
                            GRID_WARS.setScreen(new SurvivalBattleScreen(team, getFloorLevelTeam(), attackingObjectsTeam,
                                    getComputerDifficulty(), level, healingPowerUp, spPowerUp, powerPowerUp, speedPowerUp, points, numberOfTurns, loadedFromSave,
                                    survivalLerpColorManager, song, GRID_WARS));
                    } else if (actor == btnSave) {
                        StatusEffectComponent.setLerpColorManager(null);
                        GRID_WARS.saveDataManager.setSavedData(new SaveData(team, healingPowerUp, spPowerUp, powerPowerUp, speedPowerUp, points, numberOfTurns, level));
                        GRID_WARS.saveDataManager.saveSavedData();
                        GRID_WARS.soundManager.playSound(SoundInfo.BACK);
                        GRID_WARS.musicManager.setSong(SongInfo.MENU_THEME);
                        GRID_WARS.setScreen(new SurvivalModeOptions(GRID_WARS));
                    }
                }
            }
        };

        // Background
        Sprite transparentBack = new Sprite(backAtlas.findRegion("BlankBackground"));
        transparentBack.setColor(0, 0, 0, 0);
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("TowerBackground"));
        Sprite overlay;
        if (level >= 1 && level <= 10) { //level 1-9
            overlay = new Sprite(backAtlas.findRegion("DiagStripeHoriz"));
        } else if (level >= 11 && level <= 20) { //level 11-19
            overlay = new Sprite(backAtlas.findRegion("SpeedBackground"));
        } else if (level >= 21 && level <= 30) {//level 21-30
            overlay = new Sprite(backAtlas.findRegion("WavesHoriz"));
        } else if (level >= 31 && level <= 40) { //level 31-40
            overlay = new Sprite(backAtlas.findRegion("SimpleRoundedZag"));
        }  else if (level >= 41 && level <= 49) { //level 41-49
            Sprite ov = backAtlas.createSprite("CubeBackground");
            ov.setColor(new Color(1, 1, 1, 0.1f));
            overlay = new Sprite(backAtlas.findRegion("CubeBackground"));
        } else { //level 50
            overlay = new Sprite(backAtlas.findRegion("FadeBackground"));
        }
        if (level >= 41 && level <= 49) { // Should be lighter for Cube one
            overlay.setColor(0, 0, 0, .05f);
        } else {
            overlay.setColor(0, 0, 0, .2f);
        }
        background = new Background(transparentBack,
                new Sprite[]{overlay, backgroundLay},
                new BackType[]{BackType.SCROLL_HORIZONTAL, BackType.NO_MOVE},
                null, null);

        btnRestore.addListener(listener);
        btnSpUp.addListener(listener);
        btnPowerUp.addListener(listener);
        btnSpeedUp.addListener(listener);
        btnSave.addListener(listener);
        btnContinue.addListener(listener);

        Table offsetTable = new Table();
        offsetTable.add();
        offsetTable.add();
        offsetTable.add();
        offsetTable.add().row();
        offsetTable.add(titleLbl).colspan(4).padBottom(40).row();
        Label floorLevelLbl = new Label("Level " + level, skin);
        if (level == 10 || level == 20 || level == 30 || level == 50 || level == 40 || level == 43 || level == 47) { //if boss indicate
            floorLevelLbl.setColor(Color.RED);
        }
        offsetTable.add(floorLevelLbl).colspan(4).padBottom(20).row();
        //Info about Performance
        offsetTable.add(new Label("Points : " + points, skin)).colspan(4).padBottom(20).row();
        offsetTable.add(new Label("Turn Count : " + numberOfTurns, skin)).colspan(4).padBottom(30).row();
        //Team Images
        offsetTable.add(teamImages[0]).padRight(20f).padBottom(20f);
        offsetTable.add(teamImages[1]).padRight(20f).padBottom(20f);
        offsetTable.add(teamImages[2]).padRight(20f).padBottom(20f);
        offsetTable.add(teamImages[3]).padBottom(20f).row();
        //Power Ups
        offsetTable.add(btnRestore).colspan(2).size(100, 40).padBottom(20f).padRight(30f);
        offsetTable.add(lblHealthPower).colspan(2).size(80, 40).row();
        offsetTable.add(btnSpUp).colspan(2).size(100, 40).padBottom(20f).padRight(30f);
        offsetTable.add(lblSPPower).colspan(2).size(80, 40).row();
        offsetTable.add(btnPowerUp).colspan(2).size(100, 40).padBottom(20f).padRight(30f);
        offsetTable.add(lblPower).colspan(2).size(80, 40).row();
        offsetTable.add(btnSpeedUp).colspan(2).size(100, 40).padBottom(30f).padRight(30f);
        offsetTable.add(lblSpeedUp).colspan(2).size(80, 40).row();
        //Save and Continue Buttons
        offsetTable.add(btnSave).colspan(2).size(170, 40).padRight(20);
        offsetTable.add(btnContinue).colspan(2).size(170, 40).row();
        table.add().padRight(200f);
        table.add(offsetTable);

        //FIXME Mirrorman HOTFIX (can be a lot better done)
        // Fixes situation where mirrorman can boost its base attack
        for (Entity e : team.getEntities()) {
            if (nm.get(e).serializeID == 12) {
                stm.get(e).atk = 1;
                am.get(e).actor.setSize(64, 64);
            }
        }

        //set music
        if (level <= 30) {
            GRID_WARS.musicManager.setSong(SongInfo.SURVIVAL_TOWER_THEME);
        } else {
            GRID_WARS.musicManager.setSong(SongInfo.SURVIVAL_TOWER_THEME_2);
        }  
    }

    private Team getFloorLevelTeam() {
        switch (level) {
            //region level 1 - 10
            case 1 :
                return new Team("Enemy",
                        new Color(1, .7f, .7f, 1),
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1)
                        }));
            case 2 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.spider(1),
                                EntityConstructor.spider(1),
                                EntityConstructor.stoneLeo(1)
                        }));
            case 3 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.slimeman(1),
                                EntityConstructor.spider(1),
                                EntityConstructor.slimeman(1),
                                EntityConstructor.slimeman(1)
                        }));
            case 4 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.canman(1),
                                EntityConstructor.canman(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.canman(1)
                        }));
            case 5 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.spider(1),
                                EntityConstructor.spider(1),
                                EntityConstructor.lethalSpider(1)
                        }));
            case 6 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.golem(1),
                                EntityConstructor.stoneSword(1),
                                EntityConstructor.stoneSword(1),
                                EntityConstructor.golem(1)
                        }));
            case 7 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.canman(1),
                                EntityConstructor.toxicCanman(1),
                                EntityConstructor.golem(1),
                                EntityConstructor.medicanMan(1)
                        }));
            case 8 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.spider(1),
                                EntityConstructor.spider(1),
                                EntityConstructor.golem(1),
                                EntityConstructor.golem(1)
                        }));
            case 9 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.stoneLion(1)
                        }));
            case 10 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.blazePneuma(1)
                        }));
            //endregion
            //region level 11 - 20
            case 11 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.book(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.book(1)
                        }));
            case 12 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.immoralSpider(1),
                                EntityConstructor.book(1),
                                EntityConstructor.book(1),
                                EntityConstructor.immoralSpider(1)
                        }));
            case 13 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.scaleman(1)
                        }));
            case 14 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.book(1),
                                EntityConstructor.book(1),
                                EntityConstructor.advancedBook(1),
                                EntityConstructor.book(1)
                        }));
            case 15 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.chemMan(1),
                                EntityConstructor.chemMan(1),
                                EntityConstructor.chemMan(1),
                                EntityConstructor.chemMan(1)
                        }));
            case 16 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.stoneLion(1),
                                EntityConstructor.stoneLion(1),
                                EntityConstructor.book(1),
                                EntityConstructor.spider(1)
                        }));
            case 17 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.book(1),
                                EntityConstructor.immoralSpider(1),
                                EntityConstructor.immoralSpider(1),
                                EntityConstructor.book(1)
                        }));
            case 18 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.toxicCanman(1),
                                EntityConstructor.redGolem(1),
                                EntityConstructor.redGolem(1),
                                EntityConstructor.toxicCanman(1)
                        }));
            case 19 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.yellowLion(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.medicanMan(1),
                                EntityConstructor.toxicCanman(1)
                        }));
            case 20 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.aquaPneuma(1)
                        }));
            //endregion
            //region level 21 - 30
            case 21 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.golemMK2(1),
                                EntityConstructor.chemMan(1),
                                EntityConstructor.alkaliMan(1),
                                EntityConstructor.redGolem(1)
                        }));
            case 22 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.book(1),
                                EntityConstructor.advancedBook(1),
                                EntityConstructor.golemMK2(1),
                                EntityConstructor.medicanMan(1)
                        }));
            case 23 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.blueSword(1),
                                EntityConstructor.chemMan(1),
                                EntityConstructor.chemMan(1),
                                EntityConstructor.blueSword(1)
                        }));
            case 24 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.romanceBook(1),
                                EntityConstructor.scaleman(1),
                                EntityConstructor.scaleman(1),
                                EntityConstructor.romanceBook(1)
                        }));
            case 25 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.stoneLion(1),
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.advancedBook(1),
                                EntityConstructor.yellowLion(1)
                        }));
            case 26 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.golemMK2(1),
                                EntityConstructor.romanceBook(1),
                                EntityConstructor.romanceBook(1),
                                EntityConstructor.golemMK2(1)
                        }));
            case 27 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.fancyBook(1),
                                EntityConstructor.lethalSpider(1)
                        }));
            case 28 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.book(1),
                                EntityConstructor.immoralSpider(1),
                                EntityConstructor.fancyBook(1),
                                EntityConstructor.advancedBook(1)
                        }));
            case 29 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.telegolem(1),
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.telegolem(1)
                        }));
            case 30 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.electroPnuema(1)
                        }));
            //endregion
            //region Level 31 - 40
            case 31 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.golemMK2(1),
                                EntityConstructor.greenOrb(1),
                                EntityConstructor.golemMK2(1),
                                EntityConstructor.redGolem(1)
                        }));
            case 32 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.blueOrb(1),
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.fancyBook(1)
                        }));
            case 33 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.stoneLion(1),
                                EntityConstructor.redOrb(1),
                                EntityConstructor.stoneLion(1),
                                EntityConstructor.stoneLion(1)
                        }));
            case 34 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.alkaliMan(1),
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.alkaliMan(1),
                                EntityConstructor.golemMK3(1)
                        }));
            case 35 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.advancedBook(1),
                                EntityConstructor.golemMK3(1),
                                EntityConstructor.fancyBook(1)
                        }));
            case 36 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.redOrb(1),
                                EntityConstructor.scaleman(1),
                                EntityConstructor.telegolem(1),
                                EntityConstructor.scaleman(1)
                        }));
            case 37 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.chromeMan(1),
                                EntityConstructor.chromeMan(1)
                        }));
            case 38 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.chemMan(1),
                                EntityConstructor.orb(1),
                                EntityConstructor.alkaliMan(1),
                                EntityConstructor.golem(1)
                        }));
            case 39 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.telegolem(1),
                                EntityConstructor.fancyBook(1)
                        }));
            case 40 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.romanceBook(1),
                                EntityConstructor.blazePneumaAlt(1),
                                EntityConstructor.blazePneumaAlt(1),
                                EntityConstructor.blueOrb(1)
                        }));
            //endregion
            //region level 41 - 50
            case 41 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.telegolem(1),
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.eliteSpider(1)
                        }));
            case 42 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.golemTypeX(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.golemTypeX(1)
                        }));
            case 43 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.greenOrb(1),
                                EntityConstructor.aquaPneumaAlt(1),
                                EntityConstructor.aquaPneumaAlt(1),
                                EntityConstructor.romanceBook(1)
                        }));
            case 44 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.gargoyle(1),
                                EntityConstructor.gargoyle(1),
                                EntityConstructor.gargoyle(1),
                                EntityConstructor.gargoyle(1)
                        }));
            case 45 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.golemTypeX(1),
                                EntityConstructor.gargoyle(1),
                                EntityConstructor.archgargoyle(1),
                                EntityConstructor.eliteBook(1)
                        }));
            case 46 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.blueLion(1),
                                EntityConstructor.telegolem(1),
                                EntityConstructor.telegolem(1),
                                EntityConstructor.blueLion(1)
                        }));
            case 47 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.electroPnuemaAlt(1),
                                EntityConstructor.blazePneumaAlt(1),
                                EntityConstructor.aquaPneumaAlt(1)
                        }));
            case 48 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.chromeMan(1),
                                EntityConstructor.gargoyle(1),
                                EntityConstructor.archgargoyle(1),
                                EntityConstructor.chromeMan(1)
                        }));
            case 49 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.medicanMan(1),
                                EntityConstructor.eliteBook(1),
                                EntityConstructor.blueLion(1),
                                EntityConstructor.archgargoyle(1)
                        }));
            case 50:
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.dragonPneuma(1),
                                EntityConstructor.sentinelSword(1),
                                EntityConstructor.sentinelSword(1),
                                EntityConstructor.sentinelSword(1)
                        }));
            //endregion
        }
        return null;
    }

    private Team getFloorLevelAttackingObjects() {
        switch (level) {
            //region levels with towers
            case 17 :
                return new Team("Neutral",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.weakenTower(1),
                                EntityConstructor.weakenTower(1)
                        }));
            case 22 :
                return new Team("Neutral",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.weakenTower(1),
                                EntityConstructor.weakenTower(1)
                        }));
            case 23 :
                return new Team("Neutral",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.powerTower(1)
                        }));
            case 35 :
                return new Team("Neutral",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.weakenTower(1)
                        }));
            case 43 :
                return new Team("Neutral",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.powerTower(1),
                                EntityConstructor.weakenTower(1)
                        }));
            case 46 :
                return new Team("Neutral",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.guardTower(1),
                                EntityConstructor.powerTower(1)
                        }));
            case 47 :
                return new Team("Neutral",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.guardTower(1),
                                EntityConstructor.powerTower(1),
                                EntityConstructor.guardTower(1),
                                EntityConstructor.powerTower(1)
                        }));
                //endregion
            default: return null;
        }
    }

    private Song getFloorLevelSong() {
        // level 1-9
        if (level == 5 || level == 8)
            return new Song(SongInfo.STAGE_ALT_1);
        else if (level >= 1 && level <= 9)
            return new Song(SongInfo.STAGE_THEME);
        // level 11-19
        else if (level == 13 || level == 17)
            return new Song(SongInfo.STAGE_ALT_1);
        else if (level == 12 || level == 15 || level == 18)
            return new Song(SongInfo.STAGE_ALT_2);
        else if (level == 19) 
            return new Song(SongInfo.STAGE_THEME);
        else if (level >= 11 && level <= 19)
            return new Song(SongInfo.STAGE_THEME_4);
        // level 21-29
        else if (level == 23)
            return new Song(SongInfo.STAGE_THEME);
        else if (level == 24 || level == 27)
            return new Song(SongInfo.STAGE_THEME_3);
        else if (level == 25 || level == 29)
            return new Song(SongInfo.STAGE_ALT_2);
        else if (level >= 21 && level <= 29)
            return new Song(SongInfo.STAGE_THEME_2);
        // level 31-39
        else if (level == 31 || level == 33 || level == 36 || level == 37 || level == 38)
            return new Song(SongInfo.STAGE_ALT_3);
        else if (level >= 31 && level <= 39)
            return new Song(SongInfo.STAGE_THEME_3);
        // level 41-49
        else if (level == 41)
            return new Song(SongInfo.STAGE_ALT_4);
        else if (level == 46 || level == 49)
            return new Song(SongInfo.DANGER_THEME);
        else if (level == 42 || (level >= 44 && level <= 45) || level == 48)
            return new Song(SongInfo.STAGE_THEME_5);
        // Alternate bosses
        else if (level == 40 || level == 43 || level == 47)
            return new Song(SongInfo.BOSS_THEME_2);
        //final boss
        else if (level == 50)
            return new Song(SongInfo.FINAL_BOSS_THEME);
        //boss
        else
            return new Song(SongInfo.BOSS_THEME);
    }

    private ComputerPlayer.Difficulty getComputerDifficulty() {
        //bosses
        if (level == 10 || level == 20 || level == 30 || level == 50 || level == 40 || level == 43 || level == 47) {
            return ComputerPlayer.Difficulty.HARD;
        }
        //normal stages
        if (level > 0 && level <= 17)
            return ComputerPlayer.Difficulty.EASY;
        else if (level > 17 && level <= 29)
            return ComputerPlayer.Difficulty.NORMAL;
        else
            return ComputerPlayer.Difficulty.HARD;
    }

    private void createParticleEffect(int particleType) {
        for (int i = 0; i < team.getEntities().size; i++) {
            Vector2 position;
            position = teamImages[i].localToStageCoordinates(new Vector2(0, 0));
            position.add(-teamImages[i].getWidth(), -teamImages[i].getHeight());
            engine.addEntity(createParticle(position, particleType));
        }
    }

    private Entity createParticle(Vector2 position, int particleType) {
        Entity entity = new Entity();
        switch (particleType) {
            case 0:
                entity.add(new PositionComponent(position, 200, 200, 0));
                entity.add(new LifetimeComponent(0, .16f));
                entity.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explodeGreen"),
                                atlas.findRegion("explodeGreen2"),
                                atlas.findRegion("explodeGreen3"),
                                atlas.findRegion("explodeGreen4"),
                                atlas.findRegion("explodeGreen5"),
                                atlas.findRegion("explodeGreen6")},
                        Animation.PlayMode.NORMAL));
                entity.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                return entity;
            case 1:
                entity.add(new PositionComponent(position, 200, 200, 0));
                entity.add(new LifetimeComponent(0, .16f));
                entity.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Color.CYAN,
                        Animation.PlayMode.NORMAL));
                entity.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                return entity;
            case 2:
                entity.add(new PositionComponent(position, 200, 200, 0));
                entity.add(new LifetimeComponent(0, .18f));
                entity.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.RED,
                        Animation.PlayMode.NORMAL));
                entity.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(9)));
                return entity;
            case 3:
                entity.add(new PositionComponent(position, 200, 200, 0));
                entity.add(new LifetimeComponent(0, .18f));
                entity.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.CYAN,
                        Animation.PlayMode.NORMAL));
                entity.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(9)));
                return entity;
        }
        return null;
    }

    /**
	 * Checks to see if any Debugging keys were pressed. <br>
	 * - Q : Increases Level by 1 <br>
	 * - W : Decreases Level by 1 <br>
	 * - E : Increases Level as long as held <br>
	 * - R : Decreases Level as long as held
	 */
    private void checkDebugInputs() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            level++;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);
            System.out.println("DEBUG: Level : " + level);

        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            level--;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);
            System.out.println("DEBUG: Level : " + level);
        }
        //HOld
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            level++;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);
            System.out.println("DEBUG: Level : " + level);
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            level--;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);
            System.out.println("DEBUG: Level : " + level);
        }
    }

    public static void clearSurvivalLerpColorManager() {
        survivalLerpColorManager = null;
    }
}
