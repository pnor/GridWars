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
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.AI.ComputerPlayer;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.BackgroundConstructor;
import com.mygdx.game.music.Song;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.ui.HoverButton;
import com.mygdx.game.ui.LerpColorManager;
import javafx.util.Pair;

import static com.mygdx.game.GridWars.skin;

/**
 * Screen that allows the player to select what board they want to go on.
 * @author Phillip O'Reggio
 */
public class BoardSelectScreen extends MenuScreen implements Screen {
    private int maxTeams;
    private boolean zoneRules;
    private Array<Team> teams;
    private Pair<Integer, ComputerPlayer.Difficulty>[] AIComputerControlledTeams;
    /**
     * 1 : basic 2 player <p>
     * 2 : basic zone 2 player <p>
     * 3 : basic zone 4 player <p>
     */
    private int board;

    public BoardSelectScreen(int max, boolean isZones, Array<Team> selectedTeams, Pair<Integer, ComputerPlayer.Difficulty>[] AIControlled, GridWars gridWars) {
        super(gridWars);
        maxTeams = max;
        zoneRules = isZones;
        teams = selectedTeams;
        AIComputerControlledTeams = AIControlled;
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 50;
        Label titleLbl = new Label("Select A Board", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        HoverButton basic = new HoverButton("Basic", skin, Color.LIGHT_GRAY, Color.WHITE);
        HoverButton complex = new HoverButton("Complex", skin, Color.LIGHT_GRAY, Color.DARK_GRAY);
        HoverButton compact = new HoverButton("Compact", skin, Color.LIGHT_GRAY, Color.BLUE);
        HoverButton desert = new HoverButton("Desert", skin, Color.LIGHT_GRAY, Color.YELLOW);
        HoverButton forest = new HoverButton("Forest", skin, Color.LIGHT_GRAY, Color.GREEN);
        HoverButton island = new HoverButton("Island", skin, Color.LIGHT_GRAY, Color.SKY);



        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                board = (zoneRules) ? 2 : 1;

                if (((Button) actor).isPressed()) {
                    if (actor == basic) {
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, new LerpColorManager(), Song.STAGE_THEME, GRID_WARS));
                    } else if (actor == complex) {
                        board += 2;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, new LerpColorManager(), Song.STAGE_THEME_2, GRID_WARS));
                    } else if (actor == compact) {
                        board += 4;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, new LerpColorManager(), Song.STAGE_THEME_3, GRID_WARS));
                    } else if (actor == desert) {
                        board += 6;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, new LerpColorManager(), Song.STAGE_THEME_4, GRID_WARS));
                    } else if (actor == forest) {
                        board += 8;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, new LerpColorManager(), Song.STAGE_THEME_5, GRID_WARS));
                    } else if (actor == island) {
                        board += 10;
                        GRID_WARS.setScreen(new BattleScreen(teams, board, AIComputerControlledTeams, new LerpColorManager(), Song.STAGE_THEME, GRID_WARS));
                    }
                }
            }
        };

        background = BackgroundConstructor.makeMovingStripeBackground(Color.DARK_GRAY, Color.GRAY);

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

        table.add(basic).size(300, 70).padBottom(40f).padRight(40f);
        table.add(complex).size(300, 70).padBottom(40f).row();

        table.add(compact).size(300, 70).padBottom(40f).padRight(40f);
        table.add(desert).size(300, 70).padBottom(40f).row();

        table.add(forest).size(300, 70).padRight(40f);
        table.add(island).size(300, 70);
    }

    @Override
    public void render(float deltaTime) {
        super.render(deltaTime);
        //go back highscores screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GRID_WARS.setScreen(new TeamSelectScreen(maxTeams, zoneRules, GRID_WARS));
        }
    }
}
