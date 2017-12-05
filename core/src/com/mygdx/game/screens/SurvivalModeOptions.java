package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
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
 * Screen containing buttons to go to Survival mode's high score table, or start the Survival
 * @author Phillip O'Reggio
 */
public class SurvivalModeOptions extends MenuScreen implements Screen {
    private Label titleLbl;
    private HoverButton startBtn, highScoreBtn;

    public SurvivalModeOptions(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        titleLbl = new Label("Select A Game Mode", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        startBtn = new HoverButton("Start", skin, Color.LIGHT_GRAY, Color.BLUE);
        highScoreBtn = new HoverButton("High Scores", skin, Color.LIGHT_GRAY, Color.ORANGE);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == startBtn) {
                        GRID_WARS.setScreen(new SurvivalTeamSelectScreen(GRID_WARS));
                    } else if (actor == highScoreBtn) {
                        GRID_WARS.setScreen(new HighScoreScreen(GRID_WARS));
                    }
                }
            }
        };
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.DARK_GRAY);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeHoriz")));
        topLayer.setColor(Color.BLUE);
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.SCROLL_HORIZONTAL},
                null, null);

        startBtn.addListener(listener);
        highScoreBtn.addListener(listener);
        table.add(titleLbl).padBottom(40).row();
        table.add(startBtn).size(350, 90).padBottom(10f).row();
        table.add(highScoreBtn).size(350, 90).padBottom(10f).row();

    }
}
