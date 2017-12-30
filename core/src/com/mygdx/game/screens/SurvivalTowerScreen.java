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
import com.mygdx.game.GridWars;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.components.AnimationComponent;
import com.mygdx.game.components.EventComponent;
import com.mygdx.game.components.LifetimeComponent;
import com.mygdx.game.components.PositionComponent;
import com.mygdx.game.creators.EntityConstructor;
import com.mygdx.game.misc.EventCompUtil;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.systems.EventSystem;
import com.mygdx.game.systems.LifetimeSystem;
import com.mygdx.game.systems.MovementSystem;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.*;

/**
 * @author Phillip O'Reggio
 */
public class SurvivalTowerScreen extends MenuScreen implements Screen {
    private Team team;
    private Image[] teamImages;
    private int level;
    private int healingPowerUp;
    private int spPowerUp;
    private Sprite backgroundProgressBar;
    //score keeping
    private int points;
    private int numberOfTurns;

    //TODO add highscores like statistics like highest floor reach, total turn count, damage taken, attacks used, etc.
    public SurvivalTowerScreen(Team playerTeam, int towerLevel, int healingPowerUpAmount, int spUpPowerUpAmount,
                               int points, int turnCount, GridWars game) {
        super(game);
        team = playerTeam;
        level = towerLevel;
        healingPowerUp = healingPowerUpAmount;
        spPowerUp = spUpPowerUpAmount;
        this.points = points;
        numberOfTurns = turnCount;
    }

    @Override
    public void render(float deltaTime) {
        stage.getBatch().begin();
        backgroundProgressBar.draw(stage.getBatch());
        stage.getBatch().end();
        super.render(deltaTime);
        engine.update(deltaTime);
        //Press
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            level++;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);
            System.out.println("Level : " + level);

        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            level--;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);
            System.out.println("Level : " + level);
        }
        //HOld
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            level++;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);
            System.out.println("Level : " + level);

        }
        else if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            level--;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);
            System.out.println("Level : " + level);
        }
    }

    @Override
    public void show() {
        super.show();

        //set up engine with more systems (For particle effects)
        engine.addSystem(new EventSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new MovementSystem());

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        Label titleLbl = new Label("Tower Survival", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        HoverButton btnRestore = new HoverButton("Restore", skin, Color.GREEN, Color.DARK_GRAY);
        HoverButton btnSpUp = new HoverButton("SP UP", skin, Color.ORANGE, Color.DARK_GRAY);
        HoverButton btnContinue = new HoverButton("Continue", skin, Color.NAVY, Color.BLUE);
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

        //background progress bar
        backgroundProgressBar = new Sprite(backAtlas.createSprite("BlankBackground"));
        backgroundProgressBar.setSize(1000, 900);
        backgroundProgressBar.setColor(Color.RED);
        backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == btnRestore) {
                        if (healingPowerUp > 0) {
                            healingPowerUp--;
                            lblHealthPower.setText("Remaining : " + healingPowerUp);
                            createParticleEffect(0);
                            for (Entity e : team.getEntities()) {
                                stm.get(e).hp = stm.get(e).maxHP;
                                status.get(e).removeAll(e);
                                stm.get(e).setAlive();
                            }
                            for (Image image : teamImages) {
                                image.setColor(Color.WHITE);
                            }
                        }
                    } else if (actor == btnSpUp) {
                        if (spPowerUp > 0) {
                            spPowerUp--;
                            lblSPPower.setText("Remaining : " + spPowerUp);
                            createParticleEffect(1);
                            for (Entity e : team.getEntities()) {
                                stm.get(e).sp = stm.get(e).getModMaxSp(e);
                            }
                        }
                    } else if (actor == btnContinue) {
                        Team attackingObjectsTeam = getFloorLevelAttackingObjects();
                        if (attackingObjectsTeam == null) // floor has no attacking objects
                            GRID_WARS.setScreen(new SurvivalBattleScreen(team, getFloorLevelTeam(), getComputerDifficulty(),
                                    level, healingPowerUp, spPowerUp, points, numberOfTurns, GRID_WARS));
                        else
                            GRID_WARS.setScreen(new SurvivalBattleScreen(team, getFloorLevelTeam(), attackingObjectsTeam,
                                    getComputerDifficulty(), level, healingPowerUp, spPowerUp,  points, numberOfTurns,
                                    GRID_WARS));
                    }
                }
            }
        };
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("TowerBackground"));
        background = new Background(backgroundLay,
                new Sprite[]{},
                new BackType[]{},
                null, null);

        btnRestore.addListener(listener);
        btnSpUp.addListener(listener);
        btnContinue.addListener(listener);

        Table offsetTable = new Table();
        offsetTable.add();
        offsetTable.add();
        offsetTable.add();
        offsetTable.add().row();
        offsetTable.add(titleLbl).colspan(4).padBottom(40).row();
        offsetTable.add(new Label("Level " + level, skin)).colspan(4).padBottom(10).row();
        offsetTable.add(new Label("Points : " + points, skin)).colspan(4).padBottom(10).row();
        offsetTable.add(new Label("Turn Count : " + numberOfTurns, skin)).colspan(4).padBottom(20).row();
        offsetTable.add(teamImages[0]).padRight(20f).padBottom(20f);
        offsetTable.add(teamImages[1]).padRight(20f).padBottom(20f);
        offsetTable.add(teamImages[2]).padRight(20f).padBottom(20f);
        offsetTable.add(teamImages[3]).padBottom(20f).row();
        offsetTable.add(btnRestore).colspan(2).size(80, 40).padBottom(20f).padRight(30f);
        offsetTable.add(lblHealthPower).colspan(2).size(80, 40).padBottom(20f).row();
        offsetTable.add(btnSpUp).colspan(2).size(80, 40).padBottom(20f).padRight(30f);
        offsetTable.add(lblSPPower).colspan(2).size(80, 40).padBottom(20f).row();
        offsetTable.add(btnContinue).colspan(4).size(180, 40).padBottom(10f).row();
        //offsetTable.debug();
        //offsetTable.setPosition(offsetTable.getX() + 200, offsetTable.getY());
        table.add().padRight(200f);
        table.add(offsetTable);

        //Healing Player Team somewhat
        for (Entity e : team.getEntities()) {
            //has status effects
            if (status.has(e) && status.get(e).getTotalStatusEffects() >= 1)
                status.get(e).removeAll(e);

            //healing entities
            //if alive, add one third of total health. Always heals at least 1 and at most 4.
            if (stm.get(e).alive)
                stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + MathUtils.clamp(stm.get(e).maxHP / 3, 1, 4), 0, stm.get(e).maxHP);
            else {
                stm.get(e).setAlive();
                stm.get(e).hp = 1;
                stm.get(e).sp = 0;
            }
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
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1)
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
                                EntityConstructor.alkaliMan(1),
                                EntityConstructor.alkaliMan(1),
                                EntityConstructor.golemMK2(1)
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
                                EntityConstructor.blueSword(1),
                                EntityConstructor.blueSword(1),
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
                                EntityConstructor.spider(1),
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.advancedBook(1),
                                EntityConstructor.yellowLion(1)
                        }));
            case 26 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.golemMK2(1),
                                EntityConstructor.book(1),
                                EntityConstructor.romanceBook(1),
                                EntityConstructor.golemMK2(1)
                        }));
            case 27 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.advancedBook(1),
                                EntityConstructor.fancyBook(1),
                                EntityConstructor.fancyBook(1),
                                EntityConstructor.redGolem(1)
                        }));
            case 28 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.book(1),
                                EntityConstructor.golemMK2(1),
                                EntityConstructor.immoralSpider(1),
                                EntityConstructor.book(1)
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
                                EntityConstructor.chromeMan(1),
                                EntityConstructor.chromeMan(1),
                                EntityConstructor.telegolem(1),
                                EntityConstructor.chromeMan(1)
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
            //region 41 - 50
            case 41 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.telegolem(1),
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.eliteSpider(1),
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
                                EntityConstructor.aquaPneumaAlt(1),
                                EntityConstructor.greenOrb(1),
                                EntityConstructor.romanceBook(1),
                                EntityConstructor.blueOrb(1)
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
                                EntityConstructor.romanceBook(1),
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
                                EntityConstructor.eliteSpider(1),
                                EntityConstructor.eliteSpider(1)
                        }));
            case 48 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.chromeMan(1),
                                EntityConstructor.archgargoyle(1),
                                EntityConstructor.archgargoyle(1),
                                EntityConstructor.chromeMan(1)
                        }));
            case 49 :
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.blueLion(1),
                                EntityConstructor.eliteBook(1),
                                EntityConstructor.blueLion(1),
                                EntityConstructor.archgargoyle(1)
                        }));
            case 50:
                return new Team("Enemy",
                        Color.RED,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.dragonPneuma(1),
                                EntityConstructor.blueSword(1),
                                EntityConstructor.blueSword(1),
                                EntityConstructor.blueSword(1)
                        }));
            //endregion
        }
        return null;
    }

    private Team getFloorLevelAttackingObjects() {
        switch (level) {
            //region levels with towers
            case 17 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.weakenTower(1),
                                EntityConstructor.weakenTower(1)
                        }));
            case 22 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.weakenTower(1),
                                EntityConstructor.weakenTower(1)
                        }));
            case 23 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.powerTower(1)
                        }));
            case 35 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.weakenTower(1)
                        }));
            case 43 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.powerTower(1),
                                EntityConstructor.weakenTower(1)
                        }));
            case 46 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.guardTower(1),
                                EntityConstructor.powerTower(1)
                        }));
            case 47 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.guardTower(1),
                                EntityConstructor.weakenTower(1),
                                EntityConstructor.powerTower(1),
                                EntityConstructor.powerTower(1)
                        }));
                //endregion
            default: return null;
        }
    }

    private int getComputerDifficulty() {
        if (level > 0 && level <= 12)
            return 1;
        else if (level > 12 && level <= 28)
            return 2;
        else
            return 3;
    }

    private void createParticleEffect(int particleType) {
        for (int i = 0; i < team.getEntities().size; i++) {
            Vector2 position;
            position = teamImages[i].localToStageCoordinates(new Vector2(0, 0));
            position.add(-teamImages[i].getWidth(), -teamImages[i].getHeight());
            System.out.println(position);
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
        }
        return null;
    }
}
