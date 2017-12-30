package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;

import static com.mygdx.game.GridWars.backAtlas;
import static com.mygdx.game.GridWars.skin;

/**
 * Screen that allows the player to select what board they want to go on.
 * @author Phillip O'Reggio
 */
public class BoardSelectScreen extends MenuScreen implements Screen {
    private int maxTeams;
    private boolean zoneRules;
    private Array<Team> teams;
    private Vector2[] AIComputerControlledTeams;
    /**
     * 1 : basic 2 player <p>
     * 2 : basic zone 2 player <p>
     * 3 : basic zone 4 player <p>
     */
    private int board;

    public BoardSelectScreen(int max, boolean isZones, Array<Team> selectedTeams, Vector2[] AIControlled, GridWars gridWars) {
        super(gridWars);
        maxTeams = max;
        zoneRules = isZones;
        teams = selectedTeams;
        AIComputerControlledTeams = AIControlled;
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        Label titleLbl = new Label("Select A Board", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        HoverButton basic = new HoverButton("Basic", skin, Color.WHITE, Color.DARK_GRAY);
        HoverButton complex = new HoverButton("Complex", skin, Color.WHITE, Color.DARK_GRAY);
        HoverButton compact = new HoverButton("Compact", skin, Color.WHITE, Color.DARK_GRAY);
        HoverButton desert = new HoverButton("Desert", skin, Color.WHITE, Color.DARK_GRAY);
        HoverButton forest = new HoverButton("Forest", skin, Color.WHITE, Color.DARK_GRAY);
        HoverButton island = new HoverButton("Island", skin, Color.WHITE, Color.DARK_GRAY);



        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                board = (zoneRules) ? 2 : 1;

                if (((Button) actor).isPressed()) {
                    if (actor == basic) {
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, GRID_WARS));
                    } else if (actor == complex) {
                        board += 2;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, GRID_WARS));
                    } else if (actor == compact) {
                        board += 4;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, GRID_WARS));
                    } else if (actor == desert) {
                        board += 6;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, GRID_WARS));
                    } else if (actor == forest) {
                        board += 8;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, GRID_WARS));
                    } else if (actor == island) {
                        board += 10;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, GRID_WARS));
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
        compact.addListener(listener);
        desert.addListener(listener);
        forest.addListener(listener);
        island.addListener(listener);

        table.add();
        table.add();
        table.row();
        table.add(titleLbl).colspan(2).padBottom(40).row();
        table.add(basic).size(300, 70).padRight(20f);
        table.add(complex).size(300, 70).padBottom(20f).row();
        table.add(compact).size(300, 70).padRight(20f);
        table.add(desert).size(300, 70).padBottom(20f).row();
        table.add(forest).size(300, 70).padRight(20f);
        table.add(island).size(300, 70);
    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        //go back a screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GRID_WARS.setScreen(new TeamSelectScreen(maxTeams, zoneRules, GRID_WARS));
        }
    }
}
