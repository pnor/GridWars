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
import music.Song;

import static com.mygdx.game.GridWars.backAtlas;
import static com.mygdx.game.GridWars.skin;

/**
 * @author Phillip O'Reggio
 */
public class TitleScreen extends MenuScreen implements Screen {

    private Label titleLbl;
    private HoverButton startBtn;
    private HoverButton optionBtn;

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
        optionBtn = new HoverButton("Option", skin, Color.WHITE, Color.DARK_GRAY);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == startBtn) {
                        GRID_WARS.setScreen(new ModeSelectScreen(GRID_WARS));
                    } else if (actor == optionBtn) {
                        GRID_WARS.setScreen(new OptionsScreen(GRID_WARS));
                    }
                }
            }
        };
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.DARK_GRAY);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeOverlay")));
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.FADE_COLOR},
                Color.GRAY, Color.ORANGE);

        startBtn.addListener(listener);
        optionBtn.addListener(listener);

        table.add(titleLbl).padBottom(80f).row();
        table.add(startBtn).size(300, 90).padBottom(40).row();
        table.add(optionBtn).size(300, 90);

        //set music
        if (GRID_WARS.musicManager.getSong() != Song.MENU_THEME)
            GRID_WARS.musicManager.setSong(Song.MENU_THEME);

        fontGenerator.dispose();
    }
}
