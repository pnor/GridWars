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
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.GridWars.atlas;
import static com.mygdx.game.GridWars.skin;

/**
 * @author Phillip O'Reggio
 */
public class GameOverScreen extends MenuScreen implements Screen {
    private int lastLevel;
    private Label lblGameOver;
    private Label lblLastLevelReached;
    private HoverButton btnReturn;

    private float time = 0f;
    private int progress = 0;


    public GameOverScreen(int level, GridWars gridWars) {
        super(gridWars);
        lastLevel = level;
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        lblGameOver = new Label("Game Over", new Label.LabelStyle(fontGenerator.generateFont(param), Color.BLACK));
        lblGameOver.setColor(new Color(1, 1, 1, 0));
        param.size = 30;
        lblLastLevelReached = new Label("Floor : " + lastLevel, new Label.LabelStyle(fontGenerator.generateFont(param), Color.BLACK));
        lblLastLevelReached.setColor(new Color(1, 1, 1, 0));

        btnReturn = new HoverButton("Return", skin, Color.BLACK, Color.WHITE);
        btnReturn.setDisabled(true);
        btnReturn.setVisible(false);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == btnReturn)
                        GRID_WARS.setScreen(new TitleScreen(GRID_WARS));
                }
            }
        };

        //set up tables and labels
        table.add(lblGameOver).padBottom(40).row();
        table.add(lblLastLevelReached).padBottom(60).row();
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
            btnReturn.setVisible(true);
        }  else if (time >= 4f && progress == 3) {
            progress= 4;
            btnReturn.setDisabled(false);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
