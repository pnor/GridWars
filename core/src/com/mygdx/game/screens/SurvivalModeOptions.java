package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.GridWars.skin;

/**
 * Screen containing buttons to go to Survival mode's high score table, or start the Survival
 * @author Phillip O'Reggio
 */
public class SurvivalModeOptions extends MenuScreen implements Screen {
    private Label titleLbl;
    private HoverButton startBtn, highScoreBtn, loadBtn;

    public SurvivalModeOptions(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        titleLbl = new Label("Survival", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        startBtn = new HoverButton("Start", skin, Color.LIGHT_GRAY, Color.BLUE);
        loadBtn = new HoverButton("Load", skin, Color.LIGHT_GRAY, Color.TEAL);
        GRID_WARS.saveDataManager.updateSaveDataWithFile();
        if (!GRID_WARS.saveDataManager.fileIsLoadable())
            loadBtn.setDisabled(true);
        highScoreBtn = new HoverButton("High Scores", skin, Color.LIGHT_GRAY, Color.ORANGE);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == startBtn) {
                        GRID_WARS.setScreen(new SurvivalTeamSelectScreen(GRID_WARS));
                    } else if (actor == highScoreBtn) {
                        GRID_WARS.setScreen(new HighScoreScreen(GRID_WARS));
                    } else if (actor == loadBtn) {
                        GRID_WARS.setScreen(new SurvivalTowerScreen(
                                GRID_WARS.saveDataManager.getTeamFromData(),
                                GRID_WARS.saveDataManager.getFloor(),
                                GRID_WARS.saveDataManager.getHealthPowerUps(),
                                GRID_WARS.saveDataManager.getSPPowerUps(),
                                GRID_WARS.saveDataManager.getAttackPowerUps(),
                                GRID_WARS.saveDataManager.getSpeedPowerUps(),
                                GRID_WARS.saveDataManager.getPoints(),
                                GRID_WARS.saveDataManager.getTurns(),
                                true,
                                GRID_WARS));
                    }
                }
            }
        };
       background = BackgroundConstructor.makeMovingStripeBackground(new Color(0.1f, 0.1f, 0.1f, 1), Color.DARK_GRAY);

        startBtn.addListener(listener);
        loadBtn.addListener(listener);
        highScoreBtn.addListener(listener);
        table.add(titleLbl).padBottom(40).row();
        table.add(startBtn).size(350, 90).padBottom(40f).row();
        table.add(loadBtn).size(350, 90).padBottom(40f).row();
        table.add(highScoreBtn).size(350, 90).padBottom(40f).row();

    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        //go back a screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GRID_WARS.setScreen(new ModeSelectScreen(GRID_WARS));
        }
    }
}
