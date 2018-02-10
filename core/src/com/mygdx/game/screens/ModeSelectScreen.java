package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.GridWars;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.GridWars.backAtlas;
import static com.mygdx.game.GridWars.skin;

/**
 * Screen that allows the player to choose between Zone rules or Death Match rules for a normal battle.
 * @author Phillip O'Reggio
 */
public class ModeSelectScreen extends MenuScreen implements Screen {
    private Label titleLbl;
    private HoverButton twoPlayerDeathMatch, twoPlayerZones, survival;

    public ModeSelectScreen(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        titleLbl = new Label("Select A Game Mode", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        twoPlayerDeathMatch = new HoverButton("2-Player Death Match", skin, Color.WHITE, Color.GREEN);
        twoPlayerZones = new HoverButton("2-Player Zone Match", skin, Color.WHITE, Color.RED);
        survival = new HoverButton("Survival", skin, Color.WHITE, Color.BLUE);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == twoPlayerDeathMatch) {
                        GRID_WARS.setScreen(new TeamSelectScreen(2, false, GRID_WARS));
                    } else if (actor == twoPlayerZones) {
                        GRID_WARS.setScreen(new TeamSelectScreen(2, true, GRID_WARS));
                    } else if (actor == survival) {
                        GRID_WARS.setScreen(new SurvivalModeOptions(GRID_WARS));
                    }
                }
            }
        };
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.DARK_GRAY);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeHoriz")));
        topLayer.setColor(Color.GRAY);
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.SCROLL_HORIZONTAL},
                null, null);

        twoPlayerDeathMatch.addListener(listener);
        twoPlayerZones.addListener(listener);
        survival.addListener(listener);
        table.add(titleLbl).padBottom(40).row();
        table.add(twoPlayerDeathMatch).size(350, 90).padBottom(10f).row();
        table.add(twoPlayerZones).size(350, 90).padBottom(10f).row();
        table.add(survival).size(350, 90).padBottom(10f).row();

        fontGenerator.dispose();
    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        //go back a screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
        }
    }
}
