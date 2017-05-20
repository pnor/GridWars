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

import static com.mygdx.game.GridWars.*;

/**
 * @author Phillip O'Reggio
 */
public class TeamSelectScreen extends MenuScreen implements Screen {
    private Label titleLbl;
    private Table teamCustomizeTable;
    private Label teamNameLbl, teamColorLbl;
    private TextField teamName, teamColor;
    private HoverButton okBtn, backBtn, clearBtn, lastTeamBtn;
    private Table characterBtnTable;
    /**
     * 0 : canight
     * 1 : catdroid
     * 2 : firebull
     * 3 : icebird
     * 4 : fish
     * 5 : turtle
     * 6 : fox
     * 7 : thunderdog
     * 8 : mummy
     * 9 : squid
     * 10 : steamdragon
     * 11 : jellygirl
     * 12 : mirrorman
     * 13 : random
     */
    private Array<ImageButton> characterBtns;
    private Table portraitTable;
    private Array<Image> characterPortraits;
    private Table menuBtnTable;
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
            teams.add(new Team(Integer.toString(i), new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f), false));
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
        okBtn = new HoverButton("OK", skin, Color.GREEN, Color.DARK_GRAY);
        backBtn = new HoverButton("Back", skin, Color.YELLOW, Color.DARK_GRAY);
        clearBtn = new HoverButton("Clear", skin, Color.RED, Color.DARK_GRAY);
        lastTeamBtn = new HoverButton("Last Team", skin, Color.BLUE, Color.DARK_GRAY);

        characterBtns = new Array<ImageButton>(new ImageButton[]{
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("Canight"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("catdroid"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("firebull"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("icebird"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("fish"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("turtle"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("fox"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("thunderdog"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("mummy"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("squid"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("steamdragon"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("jellygirl"))),
                new ImageButton(new TextureRegionDrawable(atlas.findRegion("mirrorman"))), new ImageButton(new TextureRegionDrawable(atlas.findRegion("mystery")))
        });
        characterPortraits = new Array<Image>(new Image[]{new Image(
                new TextureRegionDrawable(atlas.findRegion("cube"))), new Image(new TextureRegionDrawable(atlas.findRegion("cube"))),
                new Image(new TextureRegionDrawable(atlas.findRegion("cube"))), new Image(new TextureRegionDrawable(atlas.findRegion("cube")))});

        //listeners----------------
        ChangeListener listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (((Button) actor).isPressed()) {
                    if (actor == okBtn) {
                        if (teams.get(curTeam).getEntities().size <= 0) //empty team
                            return;
                        else if (curTeam >= maxTeams - 1) {//last team selected
                            GRID_WARS.setScreen(new BoardSelectScreen(maxTeams, zones, teams, GRID_WARS));
                        } else { //else -> move to next team
                            teamName.setText("");
                            teamColor.setText("");
                            currentEntity = 0;
                            curTeam++;
                            for (int i = 0; i < characterPortraits.size; i++)
                                characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
                        }
                    } else if (actor == backBtn)
                        removeLastSelection();
                    else if (actor == clearBtn) {
                        teams.get(curTeam).getEntities().clear();
                        for (int i = 0; i < characterPortraits.size; i++)
                            characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
                        currentEntity = 0;
                    } else if (actor == lastTeamBtn) {
                        if (curTeam <= 0)
                            return;
                        teamName.setText("");
                        teamColor.setText("");
                        teams.get(curTeam).getEntities().clear();
                        teams.get(curTeam).setTeamName("" + curTeam);
                        teams.get(curTeam).setTeamColor(new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
                        teams.get(curTeam - 1).getEntities().clear();
                        teams.get(curTeam - 1).setTeamName("" + (curTeam - 1));
                        teams.get(curTeam - 1).setTeamColor(new Color(.0001f + (float)(Math.random()), .0001f + (float)(Math.random()), .0001f + (float)(Math.random()), 1f));
                        for (int i = 0; i < characterPortraits.size; i++)
                            characterPortraits.get(i).setDrawable(new TextureRegionDrawable(atlas.findRegion("cube")));
                        curTeam--;
                        currentEntity = 0;
                    }

                    //character buttons
                    else if (currentEntity <= 3) {
                        if (actor == characterBtns.get(0)) {
                            chooseEntity(new TextureRegionDrawable(atlas.findRegion("Canight")), "Canight");
                        }
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
                Color color = generateColor(input);
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
        backgroundLay.setColor(Color.BLACK);
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeOverlay")));
        topLayer.setColor(Color.FOREST);
        background = new Background(backgroundLay,
                new Sprite[]{topLayer},
                new BackType[]{BackType.SCROLL_HORIZONTAL},
                null, null);

        okBtn.addListener(listener);
        backBtn.addListener(listener);
        clearBtn.addListener(listener);
        lastTeamBtn.addListener(listener);
        for (int i = 0; i < characterBtns.size; i++)
            characterBtns.get(i).addListener(listener);
        teamName.setTextFieldListener(nameListener);
        teamColor.setTextFieldListener(colorListener);


        table.add(titleLbl).colspan(2).padBottom(40).row();
        teamCustomizeTable.add(teamNameLbl);
        teamCustomizeTable.add(teamName);
        teamCustomizeTable.add(teamColorLbl);
        teamCustomizeTable.add(teamColor);
        table.add(teamCustomizeTable).row();
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j <= characterBtns.size / 2; j++) {
                if (j == characterBtns.size / 2)
                    characterBtnTable.add(characterBtns.get( ((i*7) + j) - 1 ) ).row();
                else
                    characterBtnTable.add(characterBtns.get( ((i*7) + j) - 1 ) );
            }
        }
        table.add(characterBtnTable).colspan(2).padBottom(20f).row();
        for (int i = 0; i < characterPortraits.size; i++) {
            portraitTable.add(characterPortraits.get(i)).padRight(10f);
        }
        table.add(portraitTable).padBottom(30f).colspan(2).row();
        menuBtnTable.add(okBtn).size(150, 50);
        menuBtnTable.add(backBtn).size(150, 50);
        menuBtnTable.add(clearBtn).size(150, 50);
        menuBtnTable.add(lastTeamBtn).size(150, 50);
        table.add(menuBtnTable).colspan(2);

        table.setBackground(tableBackground);

        table.debug();
        characterBtnTable.debug();
        portraitTable.debug();
    }

    private void chooseEntity(TextureRegionDrawable teamIcon, String string) {
        characterPortraits.get(currentEntity).setDrawable(teamIcon);
        teams.get(curTeam).getEntities().add(EntityConstructor.testerRobot(curTeam, engine, stage));
        currentEntity++;
    }

    private void removeLastSelection() {
        if (currentEntity <= 0 || teams.get(curTeam).getEntities().size <= 0) {
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
     * @param s String of the color wanted. Not case sensitive. Ex: "red"
     * @return {@code Color} that the string represents. Ex: "red" returns Color.RED
     */
    private Color generateColor(String s) {
        s = s.toLowerCase();
        switch (s.charAt(0)) {
            case 'b' :
               if (s.equals("blue"))
                   return Color.BLUE;
               else if (s.equals("black"))
                   return Color.BLACK;
               else if (s.equals("brown"))
                   return Color.BROWN;
               break;
            case 'c' :
               if (s.equals("chartreuse"))
                    return Color.CHARTREUSE;
               else if (s.equals("coral"))
                    return Color.CORAL;
               else if (s.equals("cyan"))
                    return Color.CYAN;
               break;
            case 'd' :
                if (s.equals("chartreuse"))
                    return Color.DARK_GRAY;
                break;
            case 'f' :
                if (s.equals("firebrick"))
                    return Color.FIREBRICK;
                else if (s.equals("lightgray"))
                    return Color.FOREST;
                break;
            case 'g' :
                if (s.equals("gold"))
                    return Color.GOLD;
                else if (s.equals("goldenrod"))
                    return Color.GOLDENROD;
                else if (s.equals("green"))
                    return Color.GREEN;
                else if (s.equals("gray"))
                    return Color.GRAY;
                break;
            case 'l' :
                if (s.equals("lightgray"))
                    return Color.LIGHT_GRAY;
                else if (s.equals("lime"))
                    return Color.LIME;
                break;
            case 'm' :
                if (s.equals("magenta"))
                    return Color.MAGENTA;
                else if (s.equals("maroon"))
                    return Color.MAROON;
                break;
            case 'n' :
                if (s.equals("navy"))
                    return Color.NAVY;
                break;
            case 'o' :
                if (s.equals("orange"))
                    return Color.ORANGE;
                else if (s.equals("olive"))
                    return Color.OLIVE;
                break;
            case 'p' :
                if (s.equals("pink"))
                    return Color.PINK;
                else if (s.equals("purple"))
                    return Color.PURPLE;
                break;
            case 'r' :
                if (s.equals("red"))
                    return Color.RED;
                else if (s.equals("royal"))
                    return Color.ROYAL;
                break;
            case 's' :
                if (s.equals("salmon"))
                    return Color.SALMON;
                else if (s.equals("scarlet"))
                    return Color.SCARLET;
                else if (s.equals("sky"))
                    return Color.SKY;
                else if (s.equals("slate"))
                    return Color.SLATE;
                break;
            case 't' :
                if (s.equals("tan"))
                    return Color.TAN;
                else if (s.equals("teal"))
                    return Color.TEAL;
                break;
            case 'v' :
                if (s.equals("violet"))
                    return Color.VIOLET;
                break;
            case 'w' :
                if (s.equals("white"))
                    return Color.WHITE;
                break;
            case 'y' :
                if (s.equals("yellow"))
                    return Color.YELLOW;
                break;
        }
        //defualt
        return null;
    }

    @Override
    public void render(float dt) {
        super.render(dt); //DEBUG!
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            System.out.println("----");
            for (int i = 0; i < teams.size; i++) {
                System.out.println("Team " + teams.get(i).getTeamName() + " Color: " + teams.get(i).getTeamColor() + "  " + teams.get(i).getEntities());
            }
        }
    }
}
