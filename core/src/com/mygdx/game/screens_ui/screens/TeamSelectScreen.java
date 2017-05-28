package com.mygdx.game.screens_ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GridWars;
import com.mygdx.game.creators.EntityConstructor;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.screens_ui.BackType;
import com.mygdx.game.screens_ui.Background;
import com.mygdx.game.screens_ui.HoverButton;
import com.mygdx.game.screens_ui.LerpColor;

import java.util.HashMap;

import static com.mygdx.game.GridWars.*;

/**
 * @author Phillip O'Reggio
 */
public class TeamSelectScreen extends MenuScreen implements Screen {
    private Label titleLbl;

    private Table teamCustomizeTable;
    private Label teamNameLbl, teamColorLbl;
    private TextField teamName, teamColor;
    private HashMap<String, Color> colorChoices;

    private Table selectedTeamsIconsTable;
    private Array<Image> teamImages;

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

    private Table portraitTable;
    private Array<Image> characterPortraits;

    private Table menuBtnTable;
    private HoverButton okBtn, backBtn, clearBtn, lastTeamBtn, nextBtn;

    private int maxTeams, curTeam, currentEntity;
    private final int MAX_ENTITY_PER_TEAM = 4;
    private boolean zones;
    private Array<Team> teams;

    /**
     * Creates a team selection screen
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
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("arial.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        NinePatch tableBack = new NinePatch(new Texture(Gdx.files.internal("TableBackground.png")), 33, 33, 33, 33);
        NinePatchDrawable tableBackground = new NinePatchDrawable(tableBack);

        teams = new Array<Team>();
        for (int i = 0; i < maxTeams; i++)
            teams.add(new Team("", new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f), false));
        switch (maxTeams) { //Give default names
            case 4 :
                teams.get(3).setTeamName("Delta");
            case 3 :
                teams.get(2).setTeamName("Gamma");
            case 2 :
                teams.get(1).setTeamName("Beta");
            case 1 :
                teams.get(0).setTeamName("Alpha");
        }
        //UI stuff
        selectedTeamsIconsTable = new Table();
        characterBtnTable = new Table();
        portraitTable = new Table();
        menuBtnTable = new Table();
        teamCustomizeTable = new Table();

        param.size = 65;
        titleLbl = new Label("Choose Your Team", new Label.LabelStyle(fontGenerator.generateFont(param), Color.WHITE));
        titleLbl.setColor(Color.GREEN);
        teamNameLbl = new Label("Team", skin);
        teamName = new TextField("", skin);
        teamColorLbl = new Label("Color", skin);
        teamColor = new TextField("", skin);
        okBtn = new HoverButton("OK", skin, Color.PINK, Color.GRAY);
        backBtn = new HoverButton("Back", skin, Color.YELLOW, Color.GRAY);
        clearBtn = new HoverButton("Clear", skin, Color.RED, Color.GRAY);
        lastTeamBtn = new HoverButton("Last Team", skin, Color.BLUE, Color.GRAY);
        nextBtn = new HoverButton("Next", skin, Color.GREEN, Color.GRAY);

        //set up character buttons
        characterBtns = new Array<ImageButton>(new ImageButton[]{
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("Canight"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("catdroid"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("firebull"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("icebird"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("fish"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("turtle"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("fox"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("thunderdog"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("mummy"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("squid"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("steamdragon"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("jellygirl"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("mirrorman"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("mystery")))
        });
        //set up character portraits
        characterPortraits = new Array<Image>(new Image[]{new Image(
                new TextureRegionDrawable(atlas.findRegion("cube"))), new Image(new TextureRegionDrawable(atlas.findRegion("cube"))),
                new Image(new TextureRegionDrawable(atlas.findRegion("cube"))), new Image(new TextureRegionDrawable(atlas.findRegion("cube")))});

        //listeners----------------
        ChangeListener listener = new ChangeListener() {
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
                    //character buttons
                    else if (currentEntity <= 3) {
                        if (actor == characterBtns.get(0))
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("Canight")), "Canight");
                        else if (actor == characterBtns.get(1))
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("catdroid")), "Catdroid");
                        else if (actor == characterBtns.get(2))
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("firebull")), "Pyrobull");
                        else if (actor == characterBtns.get(3))
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("icebird")), "Freezird");
                        else if (actor == characterBtns.get(4))
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("fish")), "Medicarp");
                        else if (actor == characterBtns.get(5))
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("turtle")), "Thoughtoise");
                        else if (actor == characterBtns.get(6))
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("fox")), "Vulpedge");
                        else if (actor == characterBtns.get(7))
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("thunderdog")), "Thundog");
                        else
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("mystery")), "doesnt matter, default case");
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
                    textField.setColor(color);
                    teams.get(curTeam).setTeamColor(color);
                } else {
                    teams.get(curTeam).setTeamColor(new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
                    textField.setColor(Color.WHITE);
                }
            }
        };

        //backgrounds----------------
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(Color.DARK_GRAY);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeOverlay")));
        topLayer.setColor(Color.GRAY);
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.SCROLL_HORIZONTAL},
                null, null);

        //listeners
        okBtn.addListener(listener);
        backBtn.addListener(listener);
        clearBtn.addListener(listener);
        lastTeamBtn.addListener(listener);
        nextBtn.addListener(listener);
        for (int i = 0; i < characterBtns.size; i++)
            characterBtns.get(i).addListener(listener);
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
        table.add(teamCustomizeTable).padBottom(10f).row();

        //team character buttons table
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j <= characterBtns.size / 2; j++) {
                if (j == characterBtns.size / 2)
                    characterBtnTable.add(characterBtns.get( ((i*7) + j) - 1 ) ).row();
                else
                    characterBtnTable.add(characterBtns.get( ((i*7) + j) - 1 ) );
            }
        }
        table.add(characterBtnTable).colspan(2).padBottom(20f).row();

        //team portratis
        for (int i = 0; i < characterPortraits.size; i++) {
            portraitTable.add(characterPortraits.get(i)).padRight(10f);
        }
        table.add(portraitTable).padBottom(30f).colspan(2).row();
        //menu buttons table
        menuBtnTable.add(okBtn).size(150, 50);
        menuBtnTable.add(backBtn).size(150, 50);
        menuBtnTable.add(clearBtn).size(150, 50);
        menuBtnTable.add(lastTeamBtn).size(150, 50);
        menuBtnTable.add(nextBtn).size(150, 50);
        table.add(menuBtnTable).colspan(2);

        //table.setBackground(tableBackground);

        table.debug();
        characterBtnTable.debug();
        //portraitTable.debug();
    }

    /**
     * Choses the entity by placing its image into one of the Images, and adding the entitiy to the team.
     * @param teamIcon {@code Image} that is being set
     * @param string of the entity that is being chosen
     */
    private void chooseEntity(TextureRegionDrawable teamIcon, String string) {
        characterPortraits.get(currentEntity).setDrawable(teamIcon);
        //teams.get(curTeam).getEntities().add(EntityConstructor.testerRobot(curTeam, engine, stage));
        if (string.equals( "Canight"))
            teams.get(curTeam).getEntities().add(EntityConstructor.canight(curTeam, engine, stage));
        else if (string.equals("Catdroid"))
            teams.get(curTeam).getEntities().add(EntityConstructor.catdroid(curTeam, engine, stage));
        else if (string.equals("Pyrobull"))
            teams.get(curTeam).getEntities().add(EntityConstructor.pyrobull(curTeam, engine, stage));
        else if (string.equals("Freezird"))
            teams.get(curTeam).getEntities().add(EntityConstructor.freezird(curTeam, engine, stage));
        else if (string.equals("Medicarp"))
            teams.get(curTeam).getEntities().add(EntityConstructor.medicarp(curTeam, engine, stage));
        else if (string.equals("Thoughtoise"))
            teams.get(curTeam).getEntities().add(EntityConstructor.thoughtoise(curTeam, engine, stage));
        else if (string.equals("Vulpedge"))
            teams.get(curTeam).getEntities().add(EntityConstructor.vulpedge(curTeam, engine, stage));
        else if (string.equals("Thundog"))
            teams.get(curTeam).getEntities().add(EntityConstructor.thundog(curTeam, engine, stage));
        else
            teams.get(curTeam).getEntities().add(EntityConstructor.testerRobot(curTeam, engine, stage));
        currentEntity++;
    }

    /**
     * Confirms the selection of a team. If the team is empty, it does nothing. If there is still teams to be chosen, it will change
     * the current team and entity variables and clear the {@code Image}s, to allow the next team to be chosen. If the there
     * is no more teams, it will move on to the {@code BoardSelectScreen}.
     */
    private void confirmSelection() {
        if (teams.get(curTeam).getEntities().size <= 0) //empty team
            return;
        else { //else -> move to next team
            teamName.setText("");
            teamColor.setText("");
            teamColor.setColor(Color.WHITE);
            currentEntity = 0;
            //color team icon
            for (int i = 0; i < characterPortraits.size; i++)
                characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
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
        if (currentEntity <= 0 || teams.get(curTeam).getEntities().size <= 0) { //debug
            if (currentEntity <= 0) {
                System.out.println("currentEntity <= 0  !!");
            }
            return;
        }
        characterPortraits.get(currentEntity - 1).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
        teams.get(curTeam).getEntities().pop();
        currentEntity--;
    }

    /**
     * Clears all the selected entities on a team.
     */
    private void clearSelection() {
        teams.get(curTeam).getEntities().clear();
        for (int i = 0; i < characterPortraits.size; i++)
            characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
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
        else if (curTeam == 0) genericTeamName = "Gamma";
        else if (curTeam == 0) genericTeamName = "Delta";
        teams.get(curTeam).getEntities().clear();
        teams.get(curTeam).setTeamName(genericTeamName);
        teams.get(curTeam).setTeamColor(new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
        teams.get(curTeam - 1).getEntities().clear();
        teams.get(curTeam - 1).setTeamName("" + (curTeam - 1));
        teams.get(curTeam - 1).setTeamColor(new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
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
            GRID_WARS.setScreen(new BoardSelectScreen(maxTeams, zones, teams, GRID_WARS));
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
        return colorChoices.get(s);
    }

    @Override
    public void render(float dt) {
        super.render(dt);
        //LerpColors TODO make it work
        if (teamColor.getColor() instanceof LerpColor)
            ((LerpColor) teamColor.getColor()).update(dt);
        for (Image i : characterPortraits)
            if (i.getColor() instanceof LerpColor)
                ((LerpColor) i.getColor()).update(dt);

        //DEBUG!
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            System.out.println("----");
            for (int i = 0; i < teams.size; i++) {
                System.out.println("Team " + teams.get(i).getTeamName() + " Color: " + teams.get(i).getTeamColor() + "  " + teams.get(i).getEntities());
            }
        }
    }
}
