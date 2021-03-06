package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.creators.EntityConstructor;
import com.mygdx.game.ui.LerpColor;

import java.util.HashMap;

import static com.mygdx.game.GridWars.atlas;
import static com.mygdx.game.misc.ColorUtils.HSV_to_RGB;

/**
 * Class containing static methods for things that don't belong to any one class
 * @author Phillip O'Reggio
 */
public class GameUtil {
    /**
     * Creates the list of colors you can choose from.
     */
    public static HashMap<String, Color> setUpColorChoices() {
        HashMap<String, Color> colors = new HashMap<String, Color>();

        colors.put("acid", new Color(247f / 255f, 234f / 255f, 93f / 255f, 1));
        colors.put("acidic", new LerpColor(new Color(247f / 255f, 234f / 255f, 93f / 255f, 1), Color.LIME));

        colors.put("bad", new Color(181f / 255f, 0, 0, 1));
        colors.put("bad guys", new LerpColor(Color.RED, Color.BLACK, 0.4f));
        colors.put("blue", Color.BLUE);
        colors.put("black", Color.BLACK);
        colors.put("brave", new LerpColor(new Color(168f / 255f, 0, 0, 1), new Color(10f / 255f, 125f / 255f, 189f / 255f, 1)));
        colors.put("brown", Color.BROWN);

        colors.put("canight", new LerpColor(Color.WHITE, Color.CYAN, 1f));
        colors.put("chartreuse", Color.CHARTREUSE);
        colors.put("clean", new LerpColor(Color.WHITE, new Color(0, 0.7f, 1, 1), Interpolation.bounceIn));
        colors.put("color wars", new LerpColor(Color.CYAN, Color.GOLD, 1f));
        colors.put("cool", new Color(10f / 255f, 1, 210f / 255f, 1f));
        colors.put("cornell", new LerpColor(Color.RED, Color.WHITE, 0.7f));
        colors.put("crazy", new LerpColor(Color.RED, Color.YELLOW, 0.4f, Interpolation.elastic));
        colors.put("cyan", Color.CYAN);

        colors.put("dark gray", Color.DARK_GRAY);
        colors.put("darkness", new LerpColor(Color.BLACK, Color.CLEAR, 5f, Interpolation.bounce));
        colors.put("day", new LerpColor(new Color(1, 238f / 255f, 0, 1), new Color(1, 212f / 255f, 0, 1), 1f, Interpolation.exp10In));
        colors.put("diatomic", new LerpColor(Color.RED, Color.BLUE, 5f));
        colors.put("dog", new LerpColor(Color.BROWN, Color.GOLDENROD, 5f, Interpolation.pow2InInverse));
        colors.put("dream", new LerpColor(Color.CYAN, Color.PURPLE, 5f, Interpolation.pow5));
        colors.put("dreamy", 
            new LerpColor(
                new LerpColor(Color.NAVY, Color.BLACK, 4f, Interpolation.elastic), 
                new LerpColor(Color.PURPLE, Color.CYAN, 3f)
            )
        );

        colors.put("emma", new LerpColor(Color.RED, Color.LIGHT_GRAY, .4f, Interpolation.circleOut));
        colors.put("epic", new LerpColor(Color.RED, Color.BLACK, .1f, Interpolation.bounce));
        colors.put("electrical", new LerpColor(Color.YELLOW, Color.BLACK, .8f, Interpolation.pow3In));

        colors.put("fire", new LerpColor(Color.RED, Color.ORANGE, .35f));
        colors.put("firebrick", Color.FIREBRICK);
        colors.put("forest", Color.FOREST);

        colors.put("glimmer", new LerpColor(
            Color.WHITE,
            new LerpColor(new Color(0.7f, 0.85f, 1, 1), Color.GOLD, 4f, Interpolation.circleOut),
            1.5f, Interpolation.bounceOut
        ));
        colors.put("glow", new LerpColor(new Color(1, 1, .8f, 1), Color.GOLD, 5f));
        colors.put("goldenrod", Color.GOLDENROD);
        colors.put("good", new Color(1f, 131f / 255f, 0, 1));
        colors.put("gray", Color.GRAY);
        colors.put("green", Color.GREEN);
        colors.put("grid wars", new LerpColor(Color.BROWN, Color.GOLD, 1f));
        colors.put("grid warriors", new LerpColor(
            new LerpColor(Color.BROWN, Color.CYAN, 2f),
            new LerpColor(Color.GOLD, Color.WHITE, 1.5f)
        ));
        colors.put("groovy", new LerpColor(new Color(.4f, .2f, .1f, 1), Color.PURPLE, .5f, Interpolation.bounceOut));
        colors.put("ghost", new Color(.6f, .6f, .6f, .65f));

        colors.put("hazard", new LerpColor(Color.YELLOW, Color.BLACK, 1f));
        colors.put("hold my coffee", new LerpColor(new Color(.4f, .29f, .227f, 1f), Color.ORANGE, 1f));

        colors.put("invisible", new Color(1, 1, 1, 0));
        colors.put("almost invisible", 
            new LerpColor(new Color(1, 1, 1, 0), 
                new LerpColor(Color.WHITE, new Color(0, 0, 0, 0), 5f, Interpolation.bounceOut)
            )
        );

        colors.put("jared", new LerpColor(new Color(.1f, .1f, .1f, .5f), Color.NAVY, 4f));
        colors.put("java", new LerpColor(new Color(.4f, .29f, .227f, 1f), new Color(.51f, .36f, .29f, 1f), 1f));
        colors.put("jazzy", new LerpColor(new Color(.5f, 1, 1, 1), Color.GREEN, .5f, Interpolation.bounceOut));
        colors.put("jj", new LerpColor(Color.PURPLE, Color.NAVY, 4f));

        colors.put("lazy", 
            new LerpColor(
                new LerpColor(Color.WHITE, new Color(0, 0.9f, 0.7f, 1), 7.2f), 
                new LerpColor(new Color(0, 0.9f, 1, 1), new Color(1, 0, 0, 1), 5.2f)
            )
        );
        colors.put("legume", new LerpColor(Color.GREEN, Color.LIME));
        colors.put("light", new LerpColor(Color.WHITE, new Color(.8f, .8f, 1, 1)));
        colors.put("light gray", Color.LIGHT_GRAY);
        colors.put("lime", Color.LIME);

        colors.put("magenta", Color.MAGENTA);
        colors.put("maroon", Color.MAROON);
        colors.put("metal", new LerpColor(Color.GRAY, Color.BROWN, 1f, Interpolation.pow3));
        colors.put("miracle", 
            new LerpColor(
                new LerpColor(Color.WHITE, Color.GOLD, 5f), 
                new LerpColor(Color.CYAN, Color.LIME, 3.5f)
            )
        );
        colors.put("misty", 
            new LerpColor(
                new LerpColor(Color.WHITE, new Color(1, 0.6f, 0.6f, 0.8f), 5f), 
                new LerpColor(Color.CYAN, new Color(1, 1, 1, 0.5f), 3.5f)
            )
        );
        colors.put("monochrome", new LerpColor(Color.BLACK, Color.WHITE, .7f));
        colors.put("multicolor", new LerpColor(
            new LerpColor(Color.RED, Color.BLUE, 1.2f, Interpolation.pow2InInverse),
            new LerpColor(Color.YELLOW, new LerpColor(
                Color.CYAN,
                new LerpColor(new Color(1, 0.6f, 0.2f, 0.7f), new Color(0, 0.6f, 1f, 1), 1.5f, Interpolation.pow3In)
            ))
        ));
        colors.put("mystical cyan", new LerpColor(Color.CYAN, Color.ROYAL, 5f));

        colors.put("neat", new Color(1, 243f / 255f, 232f / 255f, 1f));
        colors.put("neon", new Color(5f / 255f, 239f / 255f, 159f / 255f, 1f));
        colors.put("night", new LerpColor(new Color(0, 4f / 255f, 96f / 255f, 1), Color.BLACK, 1f, Interpolation.exp10In));
        colors.put("noble", new LerpColor(new Color(1, 243f / 255f, 232f / 255, 1), Color.WHITE, 3f));

        colors.put("orange", Color.ORANGE);
        colors.put("olive", Color.OLIVE);
        colors.put("ominous", 
            new LerpColor(
                new LerpColor(Color.DARK_GRAY, Color.BLACK, 0.3f), 
                new LerpColor(
                    new LerpColor(new Color(0, 0, 0.3f, 0.8f), Color.VIOLET, 0.2f), 
                    new LerpColor(new Color(0.5f, 0, 0, 0.9f), Color.NAVY, 0.1f)
                )
            )
        );

        colors.put("party", 
            new LerpColor(
                new LerpColor(Color.RED, Color.GREEN, 0.3f), 
                new LerpColor(
                    new LerpColor(Color.CYAN, Color.VIOLET, 0.2f), 
                    new LerpColor(Color.ORANGE, Color.YELLOW, 0.1f)
                )
            )
        );
        colors.put("pink", Color.PINK);
        colors.put("prism", new LerpColor(
            new LerpColor(Color.RED, Color.BLUE, 1), 
            new LerpColor(Color.YELLOW, Color.GREEN, 1), 1.3f)
        );
        colors.put("prismatic", 
            new LerpColor(
                new LerpColor(Color.CYAN, Color.ORANGE, 1), 
                new LerpColor(Color.PURPLE, Color.GOLDENROD, 1.3f)
            )
        );
        colors.put("procrastination", 
            new LerpColor(
                new LerpColor(Color.RED, Color.ORANGE, 1), 
                new LerpColor(Color.LIME, Color.SKY, 0.3f)
            )
        );
        colors.put("psi", new LerpColor(Color.PURPLE, new Color(0, 1, 1, 0), 1f, Interpolation.bounceIn));
        colors.put("purple", Color.PURPLE);
        colors.put("python", new LerpColor(new Color(54f / 255f, 105f / 255f, 148f / 255f, 1f), new Color(1, 195f / 255f, 49f / 255f, 1f), 1f));

        colors.put("radiant", new LerpColor(Color.YELLOW, Color.ORANGE, .2f));
        colors.put("rainbow", 
            new LerpColor(
                new LerpColor(Color.RED, Color.GREEN, 1.5f), 
                new LerpColor(
                    new LerpColor(Color.BLUE, Color.VIOLET, 2.3f), 
                    new LerpColor(Color.ORANGE, Color.YELLOW, 3.2f)
                )
            )
        );
        colors.put("random", new LerpColor(new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1), new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1), MathUtils.random(.3f, 3)));
        colors.put("random2", new LerpColor(new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1), new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1), MathUtils.random(.3f, 3)));
        colors.put("random3", new LerpColor(new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1), new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1), MathUtils.random(.3f, 3)));
        colors.put("random4",
            new LerpColor(
                new LerpColor(
                    new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1),
                    new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1),
                    MathUtils.random(.3f, 3)
                ),
                new LerpColor(
                    new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1),
                    new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1),
                    MathUtils.random(.3f, 3)
                )
            )
        );
        colors.put("random5",
            new LerpColor(
                new LerpColor(
                    new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1),
                    new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1),
                    MathUtils.random(.3f, 5)
                ),
                new LerpColor(
                    new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1),
                    new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1),
                    MathUtils.random(.3f, 5)
                )
            )
        );
        colors.put("randomhsv", new LerpColor(HSV_to_RGB(MathUtils.random(0, 100), 100, 100), HSV_to_RGB(MathUtils.random(0, 100), 100, 100), MathUtils.random(.3f, 3)));
        colors.put("randomhsv2", new LerpColor(HSV_to_RGB(MathUtils.random(0, 100), 100, 100), HSV_to_RGB(MathUtils.random(0, 100), 100, 100), MathUtils.random(.3f, 3)));
        colors.put("randomhsv3", new LerpColor(HSV_to_RGB(MathUtils.random(0, 100), 100, 100), HSV_to_RGB(MathUtils.random(0, 100), 100, 100), MathUtils.random(.3f, 3)));
        colors.put("randomhsv4",
            new LerpColor(
                new LerpColor(
                    HSV_to_RGB(MathUtils.random(0, 100), 100, 100),
                    HSV_to_RGB(MathUtils.random(0, 100), 100, 100),
                    MathUtils.random(.3f, 5)
                ),
                new LerpColor(
                    HSV_to_RGB(MathUtils.random(0, 100), 100, 100),
                    HSV_to_RGB(MathUtils.random(0, 100), 100, 100),
                    MathUtils.random(.3f, 5)
                )
            )
        );

        colors.put("rapid", new LerpColor(Color.CHARTREUSE, Color.LIME, .1f));
        colors.put("red", Color.RED);
        colors.put("royal", Color.ROYAL);

        colors.put("sadness", new LerpColor(new Color(0f, 71f / 255f, 186f / 255f, 1), new Color(39f / 255f, 1f / 255f, 117f / 255f, 1), 8f));
        colors.put("salmon", Color.SALMON);
        colors.put("scarlet", Color.SCARLET);
        colors.put("sea", new LerpColor(Color.BLUE, Color.CYAN, 6f));
        colors.put("shine", new LerpColor(Color.WHITE, new Color(.9f, .9f, 1, 1), .3f, Interpolation.bounceOut));
        colors.put("shiny", new LerpColor(new Color(.9f, .9f, .9f, 1), new Color(.3f, .6f, 1, 1), 5f, Interpolation.bounceOut));
        colors.put("simple", new Color(247f / 255f, 1, 243f / 255f, 1));
        colors.put("sky", Color.SKY);
        colors.put("slate", Color.SLATE);
        colors.put("smokey", new LerpColor(new Color(1, 0, 0, 1), new Color(.1f, .01f, 0, 1), 5f));
        colors.put("steamy", 
            new LerpColor(
                new LerpColor(Color.WHITE, new Color(1, 0.3f, 0.2f, 0.7f), 4.2f), 
                new LerpColor(new Color(0.9f, 0.9f, 1, 1), new Color(1, 1, 1, 0.6f), 2.2f)
            )
        );
        colors.put("strange", 
            new LerpColor(
                new Color(0.5f, 0, 0, 1), 
                new LerpColor(Color.WHITE, Color.BLUE, 5, Interpolation.bounceOut)
            )
        );
        colors.put("success", new LerpColor(new Color(0, 157f / 255f, 1f, 0), new Color(1, 212f / 255f, 0, 1), 4f));
        colors.put("sunset", new LerpColor(new Color(194f / 255f, 91f / 255f, 35f / 255f, 1), new Color(48f / 255f, 11f / 255f, 8f / 255f, 1), 1f, Interpolation.exp10In));
        colors.put("swift", new LerpColor(new Color(253f / 255f, 123f / 255f, 27f / 255f, 1f), new Color(1, 1, 241f / 255f, 1f), 1f));

        colors.put("tan", Color.TAN);
        colors.put("teal", Color.TEAL);
        colors.put("thunder", new LerpColor(Color.YELLOW, new Color(.4f, .4f, 0.4f, 1), 5f));

        colors.put("ultimate", new LerpColor(new Color(1, .2f, .3f, 1), Color.GOLDENROD, .5f));
        colors.put("ultimate2", 
            new LerpColor(
                new LerpColor(new Color(1, .2f, .3f, 1), Color.GOLDENROD, .5f), 
                new LerpColor(Color.ORANGE, Color.GOLD, 5f, Interpolation.exp10In)
            )
        );

        colors.put("victory", new LerpColor(Color.GOLD, Color.BLACK, .5f));
        colors.put("violet", Color.VIOLET);
        colors.put("vulpedge", new LerpColor(Color.WHITE, Color.RED, 1f));

        colors.put("white", Color.WHITE);
        colors.put("wierd", 
            new LerpColor(
                new LerpColor(Color.RED, new Color(0.4f, 1f, 1f, 0.7f), 1.3f, Interpolation.swingOut), 
                new LerpColor(Color.CYAN, Color.BLACK, 3f)
            )
        );
        colors.put("wild", new LerpColor(Color.GOLDENROD, Color.PURPLE, .4f, Interpolation.bounceIn));

        colors.put("yeet", new LerpColor(Color.RED, Color.ORANGE, 0.25f, Interpolation.bounceIn));
        colors.put("yellow", Color.YELLOW);
        colors.put("yo", new LerpColor(Color.BLACK, Color.YELLOW));

        colors.put("zoo", new LerpColor(Color.BROWN, new Color(0.2f, 0.75f, 1, 1), 1.1f, Interpolation.swingOut));
        colors.put("zep", 
            new LerpColor(
                new LerpColor(Color.BLACK, Color.LIGHT_GRAY, 0.5f, Interpolation.bounceOut), 
                new LerpColor(Color.WHITE, Color.DARK_GRAY, 3f, Interpolation.bounceOut),
                1.5f, Interpolation.bounceOut
            )
        );

        return colors;
    }

    /**
     * @return entity that as the same serializeID as the parameter
     */
    public static Entity getEntityFromID(int ID, int altColor) {
        switch (ID) {
            //region all cases
            case -2 : //sprite that player cannot choose normally
                return EntityConstructor.testerRobot(0, altColor);
            case -1 : //no team member
                return EntityConstructor.testerPlaceHolder(0, altColor);
            case 0 :
                return EntityConstructor.canight(0, altColor);
            case 1 :
                return EntityConstructor.catdroid(0, altColor);
            case 2 :
                return EntityConstructor.pyrobull(0, altColor);
            case 3 :
                return EntityConstructor.freezird(0, altColor);
            case 4 :
                return EntityConstructor.medicarp(0, altColor);
            case 5 :
                return EntityConstructor.thoughtoise(0, altColor);
            case 6 :
                return EntityConstructor.vulpedge(0, altColor);
            case 7 :
                return EntityConstructor.thundog(0, altColor);
            case 8 :
                return EntityConstructor.mummy(0, altColor);
            case 9 :
                return EntityConstructor.squizerd(0, altColor);
            case 10 :
                return EntityConstructor.wyvrapor(0, altColor);
            case 11 :
                return EntityConstructor.jellymiss(0, altColor);
            case 12 :
                return EntityConstructor.mirrorman(0, altColor);
            case 13 :
                return EntityConstructor.pheonix(0, altColor);
            case 14 :
                return EntityConstructor.acidsnake(0, altColor);
            case 15 :
                return EntityConstructor.dragonPneumaPlayer(0, altColor);
            default:
                return EntityConstructor.testerChessPiece(0, altColor);
            //endregion
        }
    }

    /**
     * Static method that returns the sprite that is represented by the byte value passed in. Since Sprites cannot be stored in
     * the Json file, this is how the team images displayed on the High score are found.
     * @param index number corresponding to a sprite
     * @return Current sprite representing an entity's image
     */
    public static Sprite getSpriteFromID(int index) {
        switch (index) {
            //region all cases
            case -2 : //sprite that player cannot choose normally
                return atlas.createSprite("mystery");
            case -1 : //no team member
                return atlas.createSprite("cube");
            case 0 :
                return atlas.createSprite("Canight");
            case 1 :
                return atlas.createSprite("catdroid");
            case 2 :
                return atlas.createSprite("firebull");
            case 3 :
                return atlas.createSprite("icebird");
            case 4 :
                return atlas.createSprite("fish");
            case 5 :
                return atlas.createSprite("turtle");
            case 6 :
                return atlas.createSprite("fox");
            case 7 :
                return atlas.createSprite("thunderdog");
            case 8 :
                return atlas.createSprite("mummy");
            case 9 :
                return atlas.createSprite("squid");
            case 10 :
                return atlas.createSprite("steamdragon");
            case 11 :
                return atlas.createSprite("jellygirl");
            case 12 :
                return atlas.createSprite("mirrorman");
            case 13 :
                return atlas.createSprite("pheonix");
            case 14 :
                return atlas.createSprite("acidsnake");
            case 15 :
                return atlas.createSprite("dragonAlt");
            default:
                return atlas.createSprite("robot");
            //endregion
        }
    }
}
