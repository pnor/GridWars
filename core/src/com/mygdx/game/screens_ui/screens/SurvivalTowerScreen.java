package com.mygdx.game.screens_ui.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
import com.mygdx.game.creators.EntityConstructor;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.screens_ui.BackType;
import com.mygdx.game.screens_ui.Background;
import com.mygdx.game.screens_ui.HoverButton;

import static com.mygdx.game.ComponentMappers.am;
import static com.mygdx.game.ComponentMappers.stm;
import static com.mygdx.game.GridWars.*;

/**
 * @author Phillip O'Reggio
 */
public class SurvivalTowerScreen extends MenuScreen implements Screen {
    private Team team;
    private int level;
    private int healingPowerUp;
    private int spPowerUp;
    private Sprite backgroundProgressBar;

    //TODO add highscore like statistics like highest floor reach, total turn count, damage taken, attacks used, etc.
    public SurvivalTowerScreen(Team playerTeam, int towerLevel, int healingPowerUpAmount, int spUpPowerUpAmount, GridWars game) {
        super(game);
        team = playerTeam;
        level = towerLevel;
        healingPowerUp = healingPowerUpAmount;
        spPowerUp = spUpPowerUpAmount;
    }

    @Override
    public void render(float deltaTime) {
        stage.getBatch().begin();
        backgroundProgressBar.draw(stage.getBatch());
        stage.getBatch().end();
        super.render(deltaTime);
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            level++;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);

        }
        else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            level--;
            backgroundProgressBar.setPosition(0, ((float) level / 50f) * 500 - 700);

        }
    }

    @Override
    public void show() {
        super.show();

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        Label titleLbl = new Label("Tower Survival", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        HoverButton btnRestore = new HoverButton("Restore", skin, Color.GREEN, Color.DARK_GRAY);
        HoverButton btnSpUp = new HoverButton("SP UP", skin, Color.ORANGE, Color.DARK_GRAY);
        HoverButton btnContinue = new HoverButton("Continue", skin, Color.NAVY, Color.BLUE);
        Image[] teamImages = new Image[4];
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
                        /* TODO implement feature
                        if (healingPowerUp > 0) {
                            healingPowerUp--;
                            for (Entity e : team.getEntities()) {
                                stm.get(e).hp = stm.get(e).maxHP;
                                status.get(e).removeAll(e);
                                stm.get(e).setAlive();
                            }
                        }
                        */
                    } else if (actor == btnSpUp) {
                        /* TODO implement feature
                        if (spPowerUp > 0) {
                            spPowerUp--;
                            for (Entity e : team.getEntities()) {
                                stm.get(e).sp = stm.get(e).getModMaxSp(e);
                            }
                        }
                        */
                    } else if (actor == btnContinue) {
                        if (level > 10)
                            GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
                        GRID_WARS.setScreen(new SurvivalBattleScreen(team, getFloorLevelTeam(), getComputerDifficulty(), level, healingPowerUp, spPowerUp, GRID_WARS));
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
        offsetTable.add(new Label("Level " + level, skin)).colspan(4).padBottom(20).row();
        offsetTable.add(teamImages[0]).padRight(20f).padBottom(20f);
        offsetTable.add(teamImages[1]).padRight(20f).padBottom(20f);
        offsetTable.add(teamImages[2]).padRight(20f).padBottom(20f);
        offsetTable.add(teamImages[3]).padBottom(20f).row();
        offsetTable.add(btnRestore).colspan(2).size(80, 40).padBottom(20f).padRight(30f);
        offsetTable.add(new Label("Remaining : " + healingPowerUp, skin)).colspan(2).size(80, 40).padBottom(20f).row();
        offsetTable.add(btnSpUp).colspan(2).size(80, 40).padBottom(20f).padRight(30f);
        offsetTable.add(new Label("Remaining : " + spPowerUp, skin)).colspan(2).size(80, 40).padBottom(20f).row();
        offsetTable.add(btnContinue).colspan(4).size(180, 40).padBottom(10f).row();
        offsetTable.debug();

        //offsetTable.setPosition(offsetTable.getX() + 200, offsetTable.getY());
        table.add().padRight(200f);
        table.add(offsetTable);
    }

    private Team getFloorLevelTeam() {
        switch (level) {
            case 1 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1),
                                EntityConstructor.stoneLeo(1)
                        }));
            case 2 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.spider(1),
                                EntityConstructor.spider(1),
                                EntityConstructor.canman(1),
                                EntityConstructor.spider(1)
                        }));
            case 3 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.canman(1),
                                EntityConstructor.slimeman(1),
                                EntityConstructor.canman(1),
                                EntityConstructor.slimeman(1)
                        }));
            case 4 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.immoralSpider(1),
                                EntityConstructor.toxicCanman(1),
                                EntityConstructor.canman(1),
                                EntityConstructor.immoralSpider(1)
                        }));
            case 5 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.medicanMan(1),
                                EntityConstructor.toxicCanman(1),
                                EntityConstructor.golem(1),
                                EntityConstructor.golem(1)
                        }));
            case 6 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.book(1),
                                EntityConstructor.stoneSword(1),
                                EntityConstructor.stoneSword(1),
                                EntityConstructor.book(1)
                        }));
            case 7 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.advancedBook(1),
                                EntityConstructor.redGolem(1),
                                EntityConstructor.redGolem(1),
                                EntityConstructor.immoralSpider(1)
                        }));
            case 8 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.lethalSpider(1),
                                EntityConstructor.golemMK2(1)
                        }));
            case 9 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.scaleman(1),
                                EntityConstructor.immoralSpider(1),
                                EntityConstructor.golemMK2(1),
                                EntityConstructor.scaleman(1)
                        }));
            case 10 :
                return new Team("Enemy",
                        Color.WHITE,
                        new Array<Entity>(new Entity[] {
                                EntityConstructor.golemMK3(1),
                                EntityConstructor.fancyBook(1),
                                EntityConstructor.chromeMan(1),
                                EntityConstructor.golemMK3(1)
                        }));
        }
        return null;
    }

    private int getComputerDifficulty() {
        switch (level) {
            case 1 :
            case 2 :
            case 3 :
            case 4 :
            case 5 :
            case 6 :
            case 7 :
            case 8 :
            case 9 :
            case 10 :
                return 2;
        }
        return -999;
    }
}
