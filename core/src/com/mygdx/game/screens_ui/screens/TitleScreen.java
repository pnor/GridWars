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
public class TitleScreen extends MenuScreen implements Screen {

    private Label titleLbl;
    private HoverButton startBtn;

    public TitleScreen(GridWars gridWars) {
        super(gridWars);
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 120;
        param.color = Color.RED;
        param.shadowOffsetX = -5;
        param.shadowOffsetY = 5;
        titleLbl = new Label("Grid Wars", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        startBtn = new HoverButton("Start", skin, Color.WHITE, Color.GOLD);
        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == startBtn) {
                        GRID_WARS.createTesterBattleScreen();
                    }
                }
            }
        };
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.GRAY);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeOverlay")));
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.FADE_COLOR},
                Color.WHITE, Color.CYAN);

        startBtn.addListener(listener);
        table.add(titleLbl).padBottom(40).row();
        table.add(startBtn).size(300, 90);
    }
}
