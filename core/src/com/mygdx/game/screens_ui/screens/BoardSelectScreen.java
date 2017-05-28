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
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.screens_ui.BackType;
import com.mygdx.game.screens_ui.Background;
import com.mygdx.game.screens_ui.HoverButton;

import static com.mygdx.game.GridWars.backAtlas;
import static com.mygdx.game.GridWars.skin;

/**
 * @author Phillip O'Reggio
 */
public class BoardSelectScreen extends MenuScreen implements Screen {
    private int maxTeams;
    private boolean zoneRules;
    private Array<Team> teams;
    /**
     * 1 : basic 2 player <p>
     * 2 : basic zone 2 player <p>
     * 3 : basic zone 4 player <p>
     */
    private int board;

    private Label titleLbl;
    private HoverButton basic;
    private HoverButton complex;

    public BoardSelectScreen(int max, boolean isZones, Array<Team> selectedTeams, GridWars gridWars) {
        super(gridWars);
        maxTeams = max;
        zoneRules = isZones;
        teams = selectedTeams;
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        titleLbl = new Label("Select A Board", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        basic = new HoverButton("Basic", skin, Color.WHITE, Color.DARK_GRAY);
        complex = new HoverButton("Complex", skin, Color.WHITE, Color.DARK_GRAY);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (maxTeams == 2)
                    board = (zoneRules) ? 2 : 1;
                else if (maxTeams == 4 && zoneRules)
                    board = 3;

                if (((Button) actor).isPressed()) {
                    if (actor == basic) {
                        GRID_WARS.setScreen(new BattleScreen(teams, board, GRID_WARS));
                    } else if (actor == complex) {
                        board += 3;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, GRID_WARS));
                    }
                }
            }
        };
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.BLACK);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeOverlay")));
        topLayer.setColor(new Color(1, 0, 0, .7f));
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.SCROLL_HORIZONTAL},
                null, null);

        basic.addListener(listener);
        complex.addListener(listener);

        table.add(titleLbl).padBottom(40).row();
        table.add(basic).size(350, 90).row();
        table.add(complex).size(350, 90).row();

    }
}
