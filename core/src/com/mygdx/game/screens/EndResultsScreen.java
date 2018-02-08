package com.mygdx.game.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.rules_types.Rules;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.rules_types.ZoneRules;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;
import com.mygdx.game.ui.LerpColor;
import com.mygdx.game.music.Song;

import static com.mygdx.game.ComponentMappers.am;
import static com.mygdx.game.GridWars.backAtlas;
import static com.mygdx.game.GridWars.skin;

/**
 * Shows the results of a 2 player Zone or Death match.
 * @author Phillip O'Reggio
 */
public class EndResultsScreen extends MenuScreen implements Screen {
    private Rules rules;

    private Array<Team> teams;
    private int winningTeamIndex;

    private Label titleLbl;

    private Table team0Table;
    private Array<Image> team0Icons;
    private Label lbl0EntitiesRemaining;
    private Label lbl0TotalHealthRemaining;
    private Label lbl0TotalSpRemaing;
    private Label lbl0AttacksUsed;

    private Table team1Table;
    private Array<Image> team1Icons;
    private Label lbl1EntitiesRemaining;
    private Label lbl1TotalHealthRemaining;
    private Label lbl1TotalSpRemaing;
    private Label lbl1AttacksUsed;

    private Label lblMatchType;
    private Label lblTurnCount;
    private Label lblVictoryLabel;

    private Sprite backgroundLayer;
    
    /** Color of text on screen */
    private Color textColor;

    private float time = 0f;
    private int progress = 0;


    public EndResultsScreen(Array<Team> selectedTeams, int winningTeam, Rules r, GridWars gridWars) {
        super(gridWars);
        teams = selectedTeams;
        winningTeamIndex = winningTeam;
        rules = r;
    }

    @Override
    public void show() {
        super.show();
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        
        //figure out text color. If the background is a bright color, it should be Black instead of white
        Color winningColor = teams.get(winningTeamIndex).getTeamColor();
        if ((winningColor.r + winningColor.g + winningColor.b) / 3 > .5f) { //bright
            textColor = Color.BLACK;
        } else { //dark
            textColor = Color.WHITE;
        }
        param.size = 50;
        titleLbl = new Label("Results", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        param.size = 30;
        lblVictoryLabel = new Label(teams.get(winningTeamIndex).getTeamName() + " Wins!", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        lblVictoryLabel.setColor(Color.CLEAR);
        HoverButton btnReturn = new HoverButton("Return", skin, Color.WHITE, Color.DARK_GRAY);

        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == btnReturn) {
                        GRID_WARS.musicManager.setSong(Song.MENU_THEME);
                        GRID_WARS.setScreen(new ModeSelectScreen(GRID_WARS));
                    }
                }
            }
        };

        //set up tables and labels
        team0Table = new Table();
        team0Icons = new Array<>();
        for (Entity e : teams.get(0).getEntities()) {
            if (am.get(e).actor instanceof SpriteActor)
                team0Icons.add(new Image(((SpriteActor) am.get(e).actor).getSprite()));
            else
                team0Icons.add(new Image(((AnimationActor) am.get(e).actor).getInitialFrame()));
        }
        for (Image i : team0Icons)
            i.setColor(Color.CLEAR);
        lbl0AttacksUsed = new Label("Attacks Used : " + teams.get(0).getTotalAttacksUsed(), skin);
        lbl0AttacksUsed.setColor(Color.CLEAR);
        lbl0TotalSpRemaing = new Label("Average Sp Remaining : " + teams.get(0).getAverageSp(), skin);
        lbl0TotalSpRemaing.setColor(Color.CLEAR);
        lbl0EntitiesRemaining = new Label("Team Members Remaining : " + (teams.get(0).getEntities().size - teams.get(0).getAmontDead()), skin);
        lbl0EntitiesRemaining.setColor(Color.CLEAR);
        lbl0TotalHealthRemaining = new Label("Average Health Remaining : " + teams.get(0).getAverageHealth() + " / " + teams.get(0).getAverageMaxHealth(), skin);
        lbl0TotalHealthRemaining.setColor(Color.CLEAR);
        for (Image image : team0Icons)
            team0Table.add(image).padLeft(5);
        team0Table.add().row();
        team0Table.add(lbl0AttacksUsed).colspan(team0Icons.size).padTop(20).padBottom(20).row();
        team0Table.add(lbl0TotalSpRemaing).colspan(team0Icons.size).padBottom(20).row();
        team0Table.add(lbl0TotalHealthRemaining).colspan(team0Icons.size).padBottom(20).row();
        team0Table.add(lbl0EntitiesRemaining).colspan(team0Icons.size).padBottom(20).row();

        team1Table = new Table();
        team1Icons = new Array<>();
        for (Entity e : teams.get(1).getEntities()) {
            if (am.get(e).actor instanceof SpriteActor)
                team1Icons.add(new Image(((SpriteActor) am.get(e).actor).getSprite()));
            else
                team1Icons.add(new Image(((AnimationActor) am.get(e).actor).getInitialFrame()));
        }
        for (Image i : team1Icons)
            i.setColor(Color.CLEAR);
        lbl1AttacksUsed = new Label("Attacks Used : " + teams.get(1).getTotalAttacksUsed(), skin);
        lbl1AttacksUsed.setColor(Color.CLEAR);
        lbl1TotalSpRemaing = new Label("Average Sp Remaining : " + teams.get(1).getAverageSp(), skin);
        lbl1TotalSpRemaing.setColor(Color.CLEAR);
        lbl1EntitiesRemaining = new Label("Team Members Remaining : " + teams.get(0).getAmontDead(), skin);
        lbl1EntitiesRemaining.setColor(Color.CLEAR);
        lbl1TotalHealthRemaining = new Label("Average Health Remaining : " + teams.get(1).getAverageHealth() + " / " + teams.get(1).getAverageMaxHealth(), skin);
        lbl1TotalHealthRemaining.setColor(Color.CLEAR);
        for (Image image : team1Icons)
            team1Table.add(image).padLeft(5);
        team1Table.add().row();
        team1Table.add(lbl1AttacksUsed).colspan(team1Icons.size).padTop(20).padBottom(20).row();
        team1Table.add(lbl1TotalSpRemaing).colspan(team1Icons.size).padBottom(20).row();
        team1Table.add(lbl1TotalHealthRemaining).colspan(team1Icons.size).padBottom(20).row();
        team1Table.add(lbl1EntitiesRemaining).colspan(team1Icons.size).padBottom(20).row();

        lblMatchType = new Label("", skin);
        if (rules instanceof ZoneRules)
            lblMatchType.setText("Zone Match");
        else
            lblMatchType.setText("Death Match");
        lblMatchType.setColor(Color.CLEAR);
        lblTurnCount = new Label("Turn Count : " + rules.getTurnCount(), skin);
        lblTurnCount.setColor(Color.CLEAR);

        //set up background
        backgroundLayer = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLayer.setColor(Color.BLACK);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("FadeHoriz")));
        topLayer.setColor(new Color(1, 1, 1, .7f));
        background = new Background(backgroundLayer,
                new Sprite[]{topLayer},
                new BackType[]{BackType.SCROLL_HORIZONTAL},
                null, null);

        //add listeners
        btnReturn.addListener(listener);

        //add all to table
        table.add();
        table.add().row();
        table.add(titleLbl).colspan(2).padBottom(40).row();
        table.add(lblMatchType).colspan(2).padBottom(20).row();
        table.add(lblTurnCount).colspan(2).padBottom(80).row();
        table.add(team0Table).padRight(60);
        table.add(team1Table);
        table.add().row();
        table.add(lblVictoryLabel).colspan(2).padTop(80).padBottom(20).row();
        table.add(btnReturn).colspan(2).size(180, 50).row();

        //set music
        GRID_WARS.musicManager.setSong(Song.GAME_RESULTS);

        fontGenerator.dispose();
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        time += delta;

        if (time >= 1f && progress == 0) {
            lblMatchType.setColor(Color.WHITE);
            progress = 1;
        } else if (time >= 1.5f && progress == 1) {
            lblTurnCount.setColor(Color.WHITE);
            progress = 2;
        }  else if (time >= 2f && progress == 2) {
            for (Image i : team0Icons)
                i.setColor(Color.WHITE);
            for (Image i : team1Icons)
                i.setColor(Color.WHITE);
            progress= 3;
        } else if (time >= 2.5f && progress == 3) {
            lbl0AttacksUsed.setColor(Color.WHITE);
            lbl1AttacksUsed.setColor(Color.WHITE);
            progress = 4;
        } else if (time >= 3f && progress == 4) {
            lbl0TotalSpRemaing.setColor(Color.WHITE);
            lbl1TotalSpRemaing.setColor(Color.WHITE);
            progress = 5;
        } else if (time >= 3.5f && progress == 5) {
            lbl0TotalHealthRemaining.setColor(Color.WHITE);
            lbl1TotalHealthRemaining.setColor(Color.WHITE);
            progress = 6;
        } else if (time >= 4f && progress == 6) {
            lbl0EntitiesRemaining.setColor(Color.WHITE);
            lbl1EntitiesRemaining.setColor(Color.WHITE);
            progress = 7;
        } else if (time >= 6f && progress == 7) {
            if (winningTeamIndex == 0)
                for (Image i : team1Icons)
                    i.setColor(Color.BLACK);
            else
                for (Image i : team0Icons)
                    i.setColor(Color.BLACK);
            lblVictoryLabel.setColor(textColor);
            if (teams.get(winningTeamIndex).getTeamColor() instanceof LerpColor)
                backgroundLayer.setColor(((LerpColor) teams.get(winningTeamIndex).getTeamColor()).getMiddleColor());
            else
                backgroundLayer.setColor(teams.get(winningTeamIndex).getTeamColor());
            //set all labels to textColor
            titleLbl.setColor(textColor);
            lblVictoryLabel.setColor(textColor);
            lblMatchType.setColor(textColor);
            lblTurnCount.setColor(textColor);
            lbl0AttacksUsed.setColor(textColor);
            lbl1AttacksUsed.setColor(textColor);
            lbl0TotalSpRemaing.setColor(textColor);
            lbl1TotalSpRemaing.setColor(textColor);
            lbl0TotalHealthRemaining.setColor(textColor);
            lbl1TotalHealthRemaining.setColor(textColor);
            lbl0EntitiesRemaining.setColor(textColor);
            lbl1EntitiesRemaining.setColor(textColor);

            progress = 8;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }


}
