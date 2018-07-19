package com.mygdx.game.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.GridWars;
import com.mygdx.game.components.*;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.misc.EventCompUtil;
import com.mygdx.game.music.Song;
import com.mygdx.game.systems.EventSystem;
import com.mygdx.game.systems.LifetimeSystem;
import com.mygdx.game.systems.MovementSystem;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.GridWars.*;

/**
 * @author Phillip O'Reggio
 */
public class TitleScreen extends MenuScreen implements Screen {

    private Label titleLbl;
    private HoverButton startBtn;
    private HoverButton tutorialBtn;
    private HoverButton optionBtn;

    // not null if player beat the game
    private boolean beatTheGame;
    private EventSystem eventSystem;
    private LifetimeSystem lifetimeSystem;
    private MovementSystem movementSystem;

    public TitleScreen(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 120;
        param.color = Color.WHITE;
        param.shadowOffsetX = -4;
        param.shadowOffsetY = -4;
        param.shadowColor = Color.LIGHT_GRAY;
        titleLbl = new Label("Grid Wars", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        startBtn = new HoverButton("Start", skin, Color.WHITE, Color.DARK_GRAY);
        tutorialBtn = new HoverButton("Tutorial", skin, Color.WHITE, Color.DARK_GRAY);
        optionBtn = new HoverButton("Options", skin, Color.WHITE, Color.DARK_GRAY);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    engine.removeSystem(eventSystem);
                    engine.removeSystem(movementSystem);
                    engine.removeSystem(lifetimeSystem);
                    if (actor == startBtn) {
                        GRID_WARS.setScreen(new ModeSelectScreen(GRID_WARS));
                    } else if (actor == tutorialBtn) {
                        GRID_WARS.setScreen(new TutorialScreenObjective(GRID_WARS));
                    } else if (actor == optionBtn) {
                        GRID_WARS.setScreen(new OptionsScreen(GRID_WARS));
                    }
                }
            }
        };

        //background = BackgroundConstructor.makeTitleScreenBackground();
        background = BackgroundConstructor.makeNewTitle();

        startBtn.addListener(listener);
        tutorialBtn.addListener(listener);
        optionBtn.addListener(listener);

        table.add(titleLbl).padBottom(80f).row();
        table.add(startBtn).size(300, 90).padBottom(40).row();
        table.add(tutorialBtn).size(300, 90).padBottom(40).row();
        table.add(optionBtn).size(300, 90);

        // IF BEAT THE GAME set up engine for potential particle effects
        //set up engine with more systems (for particle effects)
        beatTheGame = Gdx.app.getPreferences("GridWars Options").getBoolean("Beat the Game");
        if (beatTheGame) {
            eventSystem = new EventSystem();
            lifetimeSystem = new LifetimeSystem();
            movementSystem = new MovementSystem();
            engine.addSystem(eventSystem);
            engine.addSystem(lifetimeSystem);
            engine.addSystem(movementSystem);
        }

        //set music
        if (GRID_WARS.musicManager.getSong() != Song.MENU_THEME)
            GRID_WARS.musicManager.setSong(Song.MENU_THEME);

        fontGenerator.dispose();
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        engine.update(delta);
        if (beatTheGame) {
            if (MathUtils.randomBoolean(.02f)) {
                Entity sparkle = new Entity();
                Vector2 position = new Vector2(MathUtils.random(0, stage.getWidth()), MathUtils.random(0, stage.getHeight()));
                sparkle.add(new PositionComponent(position, 16, 16, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 20)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.GOLD);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.03f, true, EventCompUtil.fadeOutAfter(10, 10)));

                engine.addEntity(sparkle);
            }
        }
    }
}
