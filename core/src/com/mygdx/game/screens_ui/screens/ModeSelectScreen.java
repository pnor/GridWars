package com.mygdx.game.screens_ui.screens;

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
import com.mygdx.game.screens_ui.BackType;
import com.mygdx.game.screens_ui.Background;
import com.mygdx.game.screens_ui.HoverButton;

import static com.mygdx.game.GridWars.backAtlas;
import static com.mygdx.game.GridWars.skin;

/**
 * @author Phillip O'Reggio
 */
public class ModeSelectScreen extends MenuScreen implements Screen {
    private Label titleLbl;
    private HoverButton twoPlayerDeathMatch, twoPlayerZones, fourPlayerZones;

    public ModeSelectScreen(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        titleLbl = new Label("Select A Game Mode", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        twoPlayerDeathMatch = new HoverButton("2-Player Death Match", skin, Color.CYAN, Color.DARK_GRAY);
        twoPlayerZones = new HoverButton("2-Player Zone Match", skin, Color.GREEN, Color.DARK_GRAY);
        fourPlayerZones = new HoverButton("4-Player Zone Match", skin, Color.RED, Color.DARK_GRAY);
        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == twoPlayerDeathMatch) {
                        GRID_WARS.setScreen(new TeamSelectScreen(2, false, GRID_WARS));
                    } else if (actor == twoPlayerZones) {
                        GRID_WARS.setScreen(new TeamSelectScreen(2, true, GRID_WARS));
                    } else if (actor == fourPlayerZones) {
                        GRID_WARS.setScreen(new TeamSelectScreen(4, true, GRID_WARS));
                    }
                }
            }
        };
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.GRAY);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeHoriz")));
        topLayer.setColor(new Color(1, 0, 0, .7f));
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.SCROLL_HORIZONTAL},
                null, null);

        twoPlayerDeathMatch.addListener(listener);
        twoPlayerZones.addListener(listener);
        fourPlayerZones.addListener(listener);
        table.add(titleLbl).padBottom(40).row();
        table.add(twoPlayerDeathMatch).size(350, 90).padBottom(10f).row();
        table.add(twoPlayerZones).size(350, 90).padBottom(10f).row();
        table.add(fourPlayerZones).size(350, 90).padBottom(10f).row();
    }
}
