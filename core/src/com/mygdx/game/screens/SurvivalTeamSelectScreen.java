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
public class SurvivalTeamSelectScreen extends MenuScreen implements Screen {

    private Label titleLbl;

    //team info
    private Table teamCustomizeTable;
    private Label teamNameLbl, teamColorLbl;
    private TextField teamName, teamColor;
    private HashMap<String, Color> colorChoices;

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
    
    //menu control buttons
    private Table menuBtnTable;
    private HoverButton okBtn, backBtn, clearBtn;

    //team info
    private int currentEntity;
    private final int MAX_ENTITY_PER_TEAM = 4;
    private Team team;

    //misc
    /** number representing alternate color choices for players */
    private int altNumber;

    /**
     * Creates a team selection screen for a survival mode. Only one team is selectable
     */
    public SurvivalTeamSelectScreen(GridWars gridWars) {
        super(gridWars);
        setUpColorChoices();
    }

    @Override
    public void show() {
        super.show();
        EntityConstructor.initialize(engine, stage);

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("Rubik-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        NinePatch tableBack = new NinePatch(new Texture(Gdx.files.internal("TableBackground.png")), 33, 33, 33, 33);
        NinePatchDrawable tableBackground = new NinePatchDrawable(tableBack);
        
        team = new Team("Grid Warriors", new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
        //UI stuff
        characterBtnTable = new Table();
        portraitTable = new Table();
        menuBtnTable = new Table();
        teamCustomizeTable = new Table();

        param.size = 65;
        titleLbl = new Label("Choose Your Team", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        teamNameLbl = new Label("Team", skin);
        teamName = new TextField("", skin);
        teamColorLbl = new Label("Color", skin);
        teamColor = new TextField("", skin);
        okBtn = new HoverButton("OK", skin, Color.GRAY, Color.GREEN);
        backBtn = new HoverButton("Back", skin, Color.GRAY, Color.BLUE);
        clearBtn = new HoverButton("Clear", skin, Color.GRAY, Color.RED);

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
                            team.getEntities().add(EntityConstructor.canight(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(1)) {
                            team.getEntities().add(EntityConstructor.catdroid(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(2)) {
                            team.getEntities().add(EntityConstructor.pyrobull(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(3)) {
                            team.getEntities().add(EntityConstructor.freezird(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(4)) {
                            team.getEntities().add(EntityConstructor.medicarp(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(5)) {
                            team.getEntities().add(EntityConstructor.thoughtoise(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(6)) {
                            team.getEntities().add(EntityConstructor.vulpedge(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(7)) {
                            team.getEntities().add(EntityConstructor.thundog(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(8)) {
                            team.getEntities().add(EntityConstructor.mummy(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(9)) {
                            team.getEntities().add(EntityConstructor.squizerd(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(10)) {
                            team.getEntities().add(EntityConstructor.wyvrapor(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(11)) {
                            team.getEntities().add(EntityConstructor.jellymiss(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(12)) {
                            team.getEntities().add(EntityConstructor.mirrorman(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(13)) {
                            team.getEntities().add(EntityConstructor.pheonix(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else if (actor == characterBtns.get(14)) {
                            team.getEntities().add(EntityConstructor.acidsnake(0, altNumber));
                            characterPortraits.get(currentEntity).setDrawable(
                                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
                        } else { //random
                            characterPortraits.get(currentEntity).setDrawable(new TextureRegionDrawable(atlas.findRegion("mystery")));
                            int randomIndex = MathUtils.random(0, 14);
                            int randomColor = (MathUtils.randomBoolean(.05f))? 1 : 0;
                            switch (randomIndex) {
                                case 0 :
                                    team.getEntities().add(EntityConstructor.canight(0, randomColor));
                                    break;
                                case 1 :
                                    team.getEntities().add(EntityConstructor.catdroid(0, randomColor));
                                    break;
                                case 2 :
                                    team.getEntities().add(EntityConstructor.pyrobull(0, randomColor));
                                    break;
                                case 3 :
                                    team.getEntities().add(EntityConstructor.freezird(0, randomColor));
                                    break;
                                case 4 :
                                    team.getEntities().add(EntityConstructor.medicarp(0, randomColor));
                                    break;
                                case 5 :
                                    team.getEntities().add(EntityConstructor.thoughtoise(0, randomColor));
                                    break;
                                case 6 :
                                    team.getEntities().add(EntityConstructor.vulpedge(0, randomColor));
                                    break;
                                case 7 :
                                    team.getEntities().add(EntityConstructor.thundog(0, randomColor));
                                    break;
                                case 8 :
                                    team.getEntities().add(EntityConstructor.mummy(0, randomColor));
                                    break;
                                case 9 :
                                    team.getEntities().add(EntityConstructor.squizerd(0, randomColor));
                                    break;
                                case 10 :
                                    team.getEntities().add(EntityConstructor.wyvrapor(0, randomColor));
                                    break;
                                case 11 :
                                    team.getEntities().add(EntityConstructor.jellymiss(0, randomColor));
                                    break;
                                case 12 :
                                    team.getEntities().add(EntityConstructor.mirrorman(0, randomColor));
                                    break;
                                case 13 :
                                    team.getEntities().add(EntityConstructor.pheonix(0, randomColor));
                                    break;
                                case 14 :
                                    team.getEntities().add(EntityConstructor.acidsnake(0, randomColor));
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
               team.setTeamName(textField.getText());
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
                        team.setTeamColor(color);
                    } else {
                        textField.setColor(color);
                        team.setTeamColor(color);
                    }
                } else { //random color
                    team.setTeamColor(new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
                    textField.setColor(Color.WHITE);
                }
            }
        };

        //backgrounds----------------
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.BLACK);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeHoriz")));
        topLayer.setColor(Color.NAVY);
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.SCROLL_HORIZONTAL},
                null, null);

        //listeners
        okBtn.addListener(teamSelectionListener);
        backBtn.addListener(teamSelectionListener);
        clearBtn.addListener(teamSelectionListener);
        for (int i = 0; i < characterBtns.size; i++)
            characterBtns.get(i).addListener(characterListener);
        teamName.setTextFieldListener(nameListener);
        teamColor.setTextFieldListener(colorListener);

        //Table set ups and arranging
        table.add(titleLbl).colspan(2).padBottom(40).row();

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
        characterBtnTable.setBackground(tableBackground);
        table.add(characterBtnTable).colspan(2).padBottom(20f).row();

        //team portraits
        for (int i = 0; i < characterPortraits.size; i++) {
            portraitTable.add(characterPortraits.get(i)).padRight(10f);
        }
        portraitTable.setBackground(tableBackground);
        table.add(portraitTable).padBottom(30f).colspan(2).row();

        //menu buttons table
        menuBtnTable.add(okBtn).size(150, 50);
        menuBtnTable.add(backBtn).size(150, 50);
        menuBtnTable.add(clearBtn).size(150, 50);
        table.add(menuBtnTable).colspan(2);

        //table.setBackground(tableBackground);

        //table.debug();
        //characterBtnTable.debug();
        //portraitTable.debug();
    }

    /**
     * Confirms the selection of a team. If the team is empty, it does nothing. If there is still teams to be chosen, it will change
     * the current team and entity variables and clear the {@code Image}s, to allow the next team to be chosen. If the there
     * is no more teams, it will move on to the {@code BoardSelectScreen}.
     */
    private void confirmSelection() {
        if (team.getEntities().size <= 0) //empty team
            return;
        else { //else -> move to next screen
            teamName.setText("");
            teamColor.setText("");
            teamColor.setColor(Color.WHITE);
            currentEntity = 0;
            //color team icon
            for (int i = 0; i < characterPortraits.size; i++)
                characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
            GRID_WARS.setScreen(new SurvivalTowerScreen(team, 1, 3, 3, GRID_WARS));
        }
    }

    /**
     * Removes the last entity selection.
     */
    private void removeLastSelection() {
        if (currentEntity <= 0 || team.getEntities().size <= 0) {
            return;
        }
        characterPortraits.get(currentEntity - 1).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
        team.getEntities().pop();
        currentEntity--;
    }

    /**
     * Clears all the selected entities on a team.
     */
    private void clearSelection() {
        team.getEntities().clear();
        for (int i = 0; i < characterPortraits.size; i++)
            characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
        currentEntity = 0;
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

    private boolean checkSecretCombo() {
        //SHIFT left + SHIFT right + TAB
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) && Gdx.input.isKeyJustPressed(Input.Keys.TAB);
    }

    @Override
    public void render(float dt) {
        super.render(dt);

        //alternate color number
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            altNumber = 1;
        else
            altNumber = 0;

        //bonus survival character (only if game has been beaten) //TODO add in if game is beaten
        if (checkSecretCombo() && team.getEntities().size <= 3) {
            if (Gdx.input.isKeyPressed(Input.Keys.BACKSLASH))
                team.getEntities().add(EntityConstructor.dragonPneumaPlayer(0, 1));
            else
                team.getEntities().add(EntityConstructor.dragonPneumaPlayer(0, 0));
            characterPortraits.get(currentEntity).setDrawable(
                    new TextureRegionDrawable(am.get(team.getEntities().peek()).actor.getSprite()));
            currentEntity++;
        }
    }
}
