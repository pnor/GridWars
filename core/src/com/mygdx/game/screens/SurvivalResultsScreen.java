package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.systems.EventSystem;
import com.mygdx.game.systems.LifetimeSystem;
import com.mygdx.game.systems.MovementSystem;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;

import static com.mygdx.game.GridWars.backAtlas;
import static com.mygdx.game.GridWars.engine;

/**
 * @author Phillip O'Reggio
 */
public class SurvivalResultsScreen extends MenuScreen implements Screen {
    private Array<Team> teams;

    public SurvivalResultsScreen(Array<Team> selectedTeams, GridWars gridWars) {
        super(gridWars);
        teams = selectedTeams;
    }

    @Override
    public void show() {
        super.show();

        //set up engine with more systems (for particle effects)
        engine.addSystem(new EventSystem());
        engine.addSystem(new LifetimeSystem());
        engine.addSystem(new MovementSystem());

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        Label titleLbl = new Label("Success!", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));

        //set up background
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(0.4f, .7f, 1f, 1));
        Sprite sun = new Sprite(backAtlas.findRegion("RadiusFade"));
        sun.setColor(new Color(1, .4f, 0, .7f));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagStripeHoriz"));
        overlay.setColor(new Color(1, 1, 1, .2f));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("DiagCheckerBackground"));
        overlay2.setColor(new Color(1, 1, 1, .1f));
        Sprite overlay3 = new Sprite(backAtlas.findRegion("CloudBackground"));
        overlay3.setColor(new Color(1, 1, 1, .3f));
        background = new Background(
                back,
                new Sprite[] {sun, overlay, overlay2, overlay3},
                new BackType[] {BackType.NO_MOVE, BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_SLOW ,BackType.SCROLL_HORIZONTAL},
                null, null);
    }

}
