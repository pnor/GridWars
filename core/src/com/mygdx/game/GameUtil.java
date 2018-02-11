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

        colors.put("blue", Color.BLUE);
        colors.put("black", Color.BLACK);
        colors.put("brown", Color.BROWN);

        colors.put("chartreuse", Color.CHARTREUSE);
        colors.put("color wars", new LerpColor(Color.CYAN, Color.GOLD, 1f));
        colors.put("cyan", Color.CYAN);

        colors.put("dark gray", Color.DARK_GRAY);
        colors.put("darkness", new LerpColor(Color.BLACK, Color.CLEAR, 5f, Interpolation.bounce));

        colors.put("firebrick", Color.FIREBRICK);
        colors.put("forest", Color.FOREST);

        colors.put("glow", new LerpColor(new Color(1, 1, .8f, 1), Color.GOLD, 5f));
        colors.put("goldenrod", Color.GOLDENROD);
        colors.put("green", Color.GREEN);
        colors.put("gray", Color.GRAY);
        colors.put("groovy", new LerpColor(new Color(.4f, .2f, .1f, 1), Color.PURPLE, .5f, Interpolation.bounceOut));
        colors.put("ghost", new Color(.6f, .6f, .6f, .65f));

        colors.put("hold my coffee", new LerpColor(new Color(.4f, .29f, .227f, 1f), Color.ORANGE, 1f));

        colors.put("invisible", new Color(1, 1, 1, 0));

        colors.put("jared", new LerpColor(new Color(.1f, .1f, .1f, .5f), Color.NAVY, 4f));
        colors.put("java", new LerpColor(new Color(.4f, .29f, .227f, 1f), new Color(.51f, .36f, .29f, 1f), 1f));

        colors.put("light gray", Color.LIGHT_GRAY);
        colors.put("lime", Color.LIME);

        colors.put("magenta", Color.MAGENTA);
        colors.put("maroon", Color.MAROON);

        colors.put("orange", Color.ORANGE);
        colors.put("olive", Color.OLIVE);

        colors.put("pink", Color.PINK);
        colors.put("psi", new LerpColor(Color.PURPLE, new Color(0, 1, 1, 0), 1f, Interpolation.bounceIn));
        colors.put("purple", Color.PURPLE);

        colors.put("random", new LerpColor(new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1), new Color(MathUtils.random(0, 1), MathUtils.random(0, 1), MathUtils.random(0, 1), 1), MathUtils.random(.3f, 3)));
        colors.put("red", Color.RED);
        colors.put("royal", Color.ROYAL);

        colors.put("salmon", Color.SALMON);
        colors.put("scarlet", Color.SCARLET);
        colors.put("sea", new LerpColor(Color.BLUE, Color.CYAN, 6f));
        colors.put("shiny", new LerpColor(new Color(.9f, .9f, .9f, 1), new Color(.3f, .6f, 1, 1), 5f, Interpolation.exp10In));
        colors.put("sky", Color.SKY);
        colors.put("slate", Color.SLATE);
        colors.put("smokey", new LerpColor(new Color(1, 0, 0, 1), new Color(.1f, .01f, 0, 1), 5f));

        colors.put("tan", Color.TAN);
        colors.put("teal", Color.TEAL);

        colors.put("violet", Color.VIOLET);

        colors.put("white", Color.WHITE);
        colors.put("wild", new LerpColor(Color.GOLDENROD, Color.PURPLE, .4f, Interpolation.bounceIn));

        colors.put("yellow", Color.YELLOW);

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
