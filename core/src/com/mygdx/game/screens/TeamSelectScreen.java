package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.EntityConstructor;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;
import com.mygdx.game.ui.HoverButton;
import com.mygdx.game.ui.LerpColor;

import java.util.HashMap;

import static com.mygdx.game.ComponentMappers.am;
import static com.mygdx.game.GridWars.*;

/**
 * @author Phillip O'Reggio
 */
public class TeamSelectScreen extends MenuScreen implements Screen {
    private Label titleLbl;

    //team info table
    private Table teamCustomizeTable;
    private Label teamNameLbl, teamColorLbl;
    private TextField teamName, teamColor;
    private HashMap<String, Color> colorChoices;

    //selected teams
    private Table selectedTeamsIconsTable;
    private Array<Image> teamImages;

    //character buttons
    private Table characterBtnTable;
    /**
     * 0 : canight <p>
     * 1 : catdroid <p>
     * 2 : firebull <p>
     * 3 : icebird <p>
     * 4 : fish <p>
     * 5 : turtle <p>
     * 6 : fox <p>
     * 7 : thunderdog <p>
     * 8 : mummy <p>
     * 9 : squid <p>
     * 10 : steamdragon <p>
     * 11 : jellygirl <p>
     * 12 : mirrorman <p>
     * 13 : random <p>
     */
    private Array<ImageButton> characterBtns;

    //character portraits
    private Table portraitTable;
    private Array<Image> characterPortraits;

    //AI
    private Table AIControlTable;
    private Label AIDescription;
    private CheckBox AIEasyCheckBox;
    private CheckBox AINormalCheckBox;
    private CheckBox AIHardCheckBox;
    private ButtonGroup<CheckBox> AICheckBoxGroup;
    
    //menu control buttons
    private Table menuBtnTable;
    private HoverButton okBtn, backBtn, clearBtn, lastTeamBtn, nextBtn;

    //game info
    private int maxTeams, curTeam, currentEntity;
    private final int MAX_ENTITY_PER_TEAM = 4;
    private boolean zones;
    private Array<Team> teams;
    /**
     * x-coordinate of the vector is team index. y-coordinate is the difficulty. 1 is easy, 2 is normal, 3 is hard.
     */
    private Array<Vector2> AIControlledTeams = new Array<>();
    
    //misc
    /** number representing alternate color choices for players */
    private int altNumber;

    /**
     * Creates highscores team selection screen
     * @param max max number of teams in the round.
     * @param isZones whether game is using zone rules
     */
    public TeamSelectScreen(int max, boolean isZones, GridWars gridWars) {
        super(gridWars);
        maxTeams = max;
        zones = isZones;
        teamImages = new Array<Image>(maxTeams);
        for (int i = 0; i < maxTeams; i++)
            teamImages.add(new Image(atlas.findRegion("cubelight")));

        setUpColorChoices();
    }

    @Override
    public void show() {
        super.show();
        EntityConstructor.initialize(engine, stage);

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        NinePatch tableBack = new NinePatch(new Texture(Gdx.files.internal("spritesAndBackgrounds/TableBackground.png")), 33, 33, 33, 33);
        NinePatchDrawable tableBackground = new NinePatchDrawable(tableBack);

        teams = new Array<Team>();
        for (int i = 0; i < maxTeams; i++)
            teams.add(new Team("", new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f)));
        switch (maxTeams) { //Give default names
            case 2 :
                teams.get(1).setTeamName("Beta");
            case 1 :
                teams.get(0).setTeamName("Alpha");
        }
        //UI stuff
        selectedTeamsIconsTable = new Table();
        characterBtnTable = new Table();
        portraitTable = new Table();
        AIControlTable = new Table();
        menuBtnTable = new Table();
        teamCustomizeTable = new Table();

        //backgrounds?
        /*
        selectedTeamsIconsTable.setBackground(tableBackground);
        AIControlTable.setBackground(tableBackground);
        portraitTable.setBackground(tableBackground);
        */
        characterBtnTable.setBackground(tableBackground);


        param.size = 65;
        titleLbl = new Label("Choose Your Team", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        teamNameLbl = new Label("Team", skin);
        teamName = new TextField("", skin);
        teamColorLbl = new Label("Color", skin);
        teamColor = new TextField("", skin);
        AIDescription = new Label("Computer Control : ", skin);
        AIEasyCheckBox = new CheckBox("Easy", skin);
        AINormalCheckBox = new CheckBox("Normal", skin);
        AIHardCheckBox = new CheckBox("Hard", skin);
        AICheckBoxGroup = new ButtonGroup<>(AIEasyCheckBox, AINormalCheckBox, AIHardCheckBox);
        AICheckBoxGroup.setMaxCheckCount(1);
        okBtn = new HoverButton("OK", skin, Color.GRAY, Color.BLUE);
        backBtn = new HoverButton("Back", skin, Color.GRAY, Color.ORANGE);
        clearBtn = new HoverButton("Clear", skin, Color.GRAY, Color.RED);
        lastTeamBtn = new HoverButton("Last Team", skin, Color.GRAY, Color.YELLOW);
        nextBtn = new HoverButton("Next", skin, Color.GRAY, Color.GREEN);

        //set up character buttons
        characterBtns = new Array<ImageButton>(new ImageButton[]{
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("Canight"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("catdroid"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("firebull"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("icebird"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("fish"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("turtle"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("fox"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("thunderdog"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("mummy"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("squid"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("steamdragon"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("jellygirl"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("mirrorman"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("pheonix"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("acidsnake"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("mystery")))
        });
        //set up character portraits
        characterPortraits = new Array<Image>(new Image[]{new Image(
                new TextureRegionDrawable(atlas.findRegion("cube"))), new Image(new TextureRegionDrawable(atlas.findRegion("cube"))),
                new Image(new TextureRegionDrawable(atlas.findRegion("cube"))), new Image(new TextureRegionDrawable(atlas.findRegion("cube")))});

        //listeners----------------
        ChangeListener teamSelectionListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == okBtn) { //OK Button
                       confirmSelection();
                    } else if (actor == backBtn) //Back Button
                        removeLastSelection();
                    else if (actor == clearBtn) {
                       clearSelection();
                    } else if (actor == lastTeamBtn) {
                        goToLastTeam();
                    } else if (actor == nextBtn) {
                        goToNextScreen();
                    }
                }
            }
        };
        ChangeListener characterListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            //character buttons
            if (currentEntity <= 3) {
                if (actor != null) {
                    if (actor == characterBtns.get(0)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.canight(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(1)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.catdroid(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(2)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.pyrobull(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(3)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.freezird(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(4)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.medicarp(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(5)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.thoughtoise(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(6)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.vulpedge(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(7)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.thundog(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(8)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.mummy(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(9)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.squizerd(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(10)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.wyvrapor(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(11)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.jellymiss(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(12)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.mirrorman(curTeam, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(13)) {
                        teams.get(curTeam).getEntities().add(EntityConstructor.pheonix(0, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else if (actor == characterBtns.get(14)) {
                    teams.get(curTeam).getEntities().add(EntityConstructor.acidsnake(0, altNumber));
                        characterPortraits.get(currentEntity).setDrawable(
                                new TextureRegionDrawable(am.get(teams.get(curTeam).getEntities().peek()).actor.getSprite()));
                    } else { //random
                        characterPortraits.get(currentEntity).setDrawable(new TextureRegionDrawable(atlas.findRegion("mystery")));
                        //teams.get(curTeam).getEntities().add(EntityConstructor.AITester(curTeam, altNumber));
                        int randomIndex = MathUtils.random(0, 12);
                        int randomColor = (MathUtils.randomBoolean(.05f))? 1 : 0;
                        switch (randomIndex) {
                            case 0 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.canight(curTeam, randomColor));
                                break;
                            case 1 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.catdroid(curTeam, randomColor));
                                break;
                            case 2 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.pyrobull(curTeam, randomColor));
                                break;
                            case 3 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.freezird(curTeam, randomColor));
                                break;
                            case 4 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.medicarp(curTeam, randomColor));
                                break;
                            case 5 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.thoughtoise(curTeam, randomColor));
                                break;
                            case 6 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.vulpedge(curTeam, randomColor));
                                break;
                            case 7 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.thundog(curTeam, randomColor));
                                break;
                            case 8 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.mummy(curTeam, randomColor));
                                break;
                            case 9 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.squizerd(curTeam, randomColor));
                                break;
                            case 10 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.wyvrapor(curTeam, randomColor));
                                break;
                            case 11 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.jellymiss(curTeam, randomColor));
                                break;
                            case 12 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.mirrorman(curTeam, randomColor));
                                break;
                            case 13 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.pheonix(curTeam, randomColor));
                                break;
                            case 14 :
                                teams.get(curTeam).getEntities().add(EntityConstructor.acidsnake(curTeam, randomColor));
                                break;
                        }
                    }

                    currentEntity++;
                }
            }
            }
        };

        TextField.TextFieldListener nameListener = new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                String input = textField.getText().trim();
                if (input.length() <= 0)
                    return;
                teams.get(curTeam).setTeamName(textField.getText());
            }
        };
        TextField.TextFieldListener colorListener = new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                String input = textField.getText().trim();
                if (input.length() <= 1)
                    return;
                Color color = getColorFromChoices(input);
                if (color != null) {
                    if (color instanceof LerpColor) {
                        textField.setColor(((LerpColor) color).getMiddleColor());
                        teams.get(curTeam).setTeamColor(color);
                    } else {
                        textField.setColor(color);
                        teams.get(curTeam).setTeamColor(color);
                    }
                } else { //random color
                    teams.get(curTeam).setTeamColor(new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
                    textField.setColor(Color.WHITE);
                }
            }
        };

        //backgrounds----------------
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.DARK_GRAY);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeHoriz")));
        topLayer.setColor(Color.GRAY);
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.SCROLL_HORIZONTAL_SLOW},
                null, null);

        //listeners
        okBtn.addListener(teamSelectionListener);
        backBtn.addListener(teamSelectionListener);
        clearBtn.addListener(teamSelectionListener);
        lastTeamBtn.addListener(teamSelectionListener);
        nextBtn.addListener(teamSelectionListener);
        for (int i = 0; i < characterBtns.size; i++)
            characterBtns.get(i).addListener(characterListener);
        teamName.setTextFieldListener(nameListener);
        teamColor.setTextFieldListener(colorListener);

        //Table set ups and arranging
        table.add(titleLbl).colspan(2).padBottom(40).row();

        //selected teams so far table
        for (int i = 0; i < teamImages.size; i++)
            selectedTeamsIconsTable.add(teamImages.get(i)).padRight(10f);
        table.add(selectedTeamsIconsTable).padBottom(20).colspan(2).center().row();

        //team customization table
        teamCustomizeTable.add(teamNameLbl).padRight(5f);
        teamCustomizeTable.add(teamName).padRight(10f);
        teamCustomizeTable.add(teamColorLbl).padRight(5f);
        teamCustomizeTable.add(teamColor);
        table.add(teamCustomizeTable).colspan(2).padBottom(10f).row();

        //team character buttons table
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j <= characterBtns.size / 2; j++) {
                if (j == characterBtns.size / 2)
                    characterBtnTable.add(characterBtns.get( ((i*8) + j) - 1 ) ).row();
                else
                    characterBtnTable.add(characterBtns.get( ((i*8) + j) - 1 ) );
            }
        }
        table.add(characterBtnTable).colspan(2).padBottom(20f).row();

        //team portraits
        for (int i = 0; i < characterPortraits.size; i++) {
            portraitTable.add(characterPortraits.get(i)).padRight(10f);
        }
        table.add(portraitTable).padBottom(30f).colspan(2).row();

        //AI control table
        AIControlTable.add(AIDescription).padRight(30);
        AIControlTable.add(AIEasyCheckBox).padRight(30);
        AIControlTable.add(AINormalCheckBox).padRight(30);
        AIControlTable.add(AIHardCheckBox);

        table.add(AIControlTable).colspan(2).padBottom(30f).row();

        //menu buttons table
        menuBtnTable.add(okBtn).size(150, 50);
        menuBtnTable.add(backBtn).size(150, 50);
        menuBtnTable.add(clearBtn).size(150, 50);
        menuBtnTable.add(lastTeamBtn).size(150, 50);
        menuBtnTable.add(nextBtn).size(150, 50);
        table.add(menuBtnTable).colspan(2);

        //table.debug();
        //characterBtnTable.debug();
        //portraitTable.debug();
    }

    /**
     * Confirms the selection of highscores team. If the team is empty, it does nothing. If there is still teams to be chosen, it will change
     * the current team and entity variables and clear the {@code Image}s, to allow the next team to be chosen. If the there
     * is no more teams, it will move on to the {@code BoardSelectScreen}.
     */
    private void confirmSelection() {
        if (teams.get(curTeam).getEntities().size <= 0) //empty team
            return;
        else { //else -> move to next team
            //if AI controlled, store team index in array
            if (AICheckBoxGroup.getChecked() != null) {
                int difficulty = 0;
                if (AICheckBoxGroup.getChecked() == AIEasyCheckBox)
                    difficulty = 1;
                else if (AICheckBoxGroup.getChecked() == AINormalCheckBox)
                    difficulty = 2;
                else
                    difficulty = 3;
                AIControlledTeams.add(new Vector2(curTeam, difficulty));
                AICheckBoxGroup.uncheckAll();
            }

            teamName.setText("");
            teamColor.setText("");
            teamColor.setColor(Color.WHITE);
            currentEntity = 0;
            //color team icon
            for (int i = 0; i < characterPortraits.size; i++)
                characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
            if (teams.get(curTeam).getTeamColor() instanceof LerpColor)
                teamImages.get(curTeam).setColor(((LerpColor) teams.get(curTeam).getTeamColor()).getMiddleColor());
            else
                teamImages.get(curTeam).setColor(teams.get(curTeam).getTeamColor());
            //disable buttons if last team was selected
            if (curTeam >= maxTeams - 1)
                disableSelectionButtons();
            //increment current Team, while keeping it in bounds
            curTeam = (curTeam + 1 < maxTeams) ? curTeam + 1 : curTeam;
        }
    }

    /**
     * Removes the last entity selection.
     */
    private void removeLastSelection() {
        if (currentEntity <= 0 || teams.get(curTeam).getEntities().size <= 0) {
            return;
        }
        characterPortraits.get(currentEntity - 1).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
        teams.get(curTeam).getEntities().pop();
        currentEntity--;
    }

    /**
     * Clears all the selected entities on highscores team.
     */
    private void clearSelection() {
        teams.get(curTeam).getEntities().clear();
        for (int i = 0; i < characterPortraits.size; i++)
            characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
        AICheckBoxGroup.uncheckAll();
        currentEntity = 0;
    }

    /**
     * Goes back to the last team. Does this by clearing all data from the current team and the team before it.
     */
    private void goToLastTeam() {
        if (curTeam <= 0)
            return;
        //if selection buttons was disabled, re-enable.
        if (okBtn.isDisabled()) {
            enableSelectionButtons();
        }
        //Clear team name and color boxes
        teamName.setText("");
        teamColor.setText("");
        teamColor.setColor(Color.WHITE);
        //clear teams at current and last slot
        String genericTeamName = "___";
        if (curTeam == 0) genericTeamName = "Alpha";
        else if (curTeam == 1) genericTeamName = "Beta";
        teams.get(curTeam).getEntities().clear();
        teams.get(curTeam).setTeamName(genericTeamName);
        teams.get(curTeam).setTeamColor(new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
        teams.get(curTeam - 1).getEntities().clear();
        teams.get(curTeam - 1).setTeamName("" + (curTeam - 1));
        teams.get(curTeam - 1).setTeamColor(new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
        //clear AI list
        //search for curTeam
        boolean hasCurTeam = false;
        int indexOfTeam = -1;
        for (Vector2 v : AIControlledTeams) {
            if (v.x == curTeam) {
                hasCurTeam = true;
                indexOfTeam = AIControlledTeams.indexOf(v, true);
                break;
            }
        }
        if (hasCurTeam)
            AIControlledTeams.removeIndex(indexOfTeam);
        //search for curTeam - 1
        hasCurTeam = false;
        indexOfTeam = -1;
        for (Vector2 v : AIControlledTeams) {
            if (v.x == curTeam) {
                hasCurTeam = true;
                indexOfTeam = AIControlledTeams.indexOf(v, true);
                break;
            }
        }
        if (hasCurTeam)
            AIControlledTeams.removeIndex(indexOfTeam);
        AICheckBoxGroup.uncheckAll();
        //clear portrait images
        for (int i = 0; i < characterPortraits.size; i++)
            characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
        //color team icon
        teamImages.get(curTeam).setColor(Color.WHITE);
        teamImages.get(curTeam - 1).setColor(Color.WHITE);
        curTeam--;
        currentEntity = 0;
    }

    /**
     * Continues to the next screen if all teams have been set with at least one entity.
     */
    public void goToNextScreen() {
        if (teams.get(maxTeams - 1).getEntities().size > 0)
            GRID_WARS.setScreen(new BoardSelectScreen(maxTeams, zones, teams, AIControlledTeams.toArray(Vector2.class), GRID_WARS));
    }

    /**
     * Disables buttons used to control selecting teams
     */
    public void disableSelectionButtons() {
        okBtn.setDisabled(true);
        backBtn.setDisabled(true);
        clearBtn.setDisabled(true);
        for (ImageButton i : characterBtns)
            i.setDisabled(true);
    }

    /**
     * Enables buttons used to control selecting teams
     */
    public void enableSelectionButtons() {
        okBtn.setDisabled(false);
        backBtn.setDisabled(false);
        clearBtn.setDisabled(false);
        for (ImageButton i : characterBtns)
            i.setDisabled(false);
    }

    private void setUpColorChoices() {
        colorChoices = new HashMap<String, Color>();

        colorChoices.put("blue", Color.BLUE);
        colorChoices.put("black", Color.BLACK);
        colorChoices.put("brown", Color.BROWN);

        colorChoices.put("chartreuse", Color.CHARTREUSE);
        colorChoices.put("cyan", Color.CYAN);

        colorChoices.put("dark gray", Color.DARK_GRAY);

        colorChoices.put("firebrick", Color.FIREBRICK);
        colorChoices.put("forest", Color.FOREST);

        colorChoices.put("glow", new LerpColor(new Color(1, 1, .8f, 1), Color.GOLD, 5f));
        colorChoices.put("goldenrod", Color.GOLDENROD);
        colorChoices.put("green", Color.GREEN);
        colorChoices.put("gray", Color.GRAY);
        colorChoices.put("ghost", new Color(.6f, .6f, .6f, .65f));

        colorChoices.put("jared", new LerpColor(new Color(.1f, .1f, .1f, .5f), Color.NAVY, 4f));

        colorChoices.put("light gray", Color.LIGHT_GRAY);
        colorChoices.put("lime", Color.LIME);

        colorChoices.put("magenta", Color.MAGENTA);
        colorChoices.put("maroon", Color.MAROON);

        colorChoices.put("orange", Color.ORANGE);
        colorChoices.put("olive", Color.OLIVE);

        colorChoices.put("pink", Color.PINK);
        colorChoices.put("purple", Color.PURPLE);

        colorChoices.put("red", Color.RED);
        colorChoices.put("royal", Color.ROYAL);

        colorChoices.put("salmon", Color.SALMON);
        colorChoices.put("scarlet", Color.SCARLET);
        colorChoices.put("sea", new LerpColor(Color.BLUE, Color.CYAN, 6f));
        colorChoices.put("sky", Color.SKY);
        colorChoices.put("slate", Color.SLATE);

        colorChoices.put("tan", Color.TAN);
        colorChoices.put("teal", Color.TEAL);

        colorChoices.put("violet", Color.VIOLET);

        colorChoices.put("white", Color.WHITE);

        colorChoices.put("yellow", Color.YELLOW);
    }

    /**
     * @param s String of the color wanted. Not case sensitive. Ex: "red"
     * @return {@code Color} that the string represents. Ex: "red" returns Color.RED
     */
    private Color getColorFromChoices(String s) {
        return colorChoices.get(s.trim().toLowerCase());
    }

    @Override
    public void render(float dt) {
        super.render(dt);
        
        //alternate color number
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            altNumber = 1;
        else
            altNumber = 0;

        //back highscores screen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            GRID_WARS.setScreen(new SurvivalModeOptions(GRID_WARS));
        }

    }
}
