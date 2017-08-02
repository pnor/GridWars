package com.mygdx.game.creators;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.screens_ui.BackType;
import com.mygdx.game.screens_ui.Background;

import static com.mygdx.game.GridWars.backAtlas;

/**
 * Contains static methods for creating each board's background
 *
 * @author Phillip O'Reggio
 */
public class BackgroundConstructor {

    public static Background getBackground(int boardIndex) {
        switch (boardIndex) {
            //region regular boards
            case 1 :
            case 2 :
                return makeSimpleBack();
            case 3 :
            case 4 :
                return makeComplexBack();
            case 5 :
            case 6 :
                return makeCompactBack();
            case 7 :
            case 8 :
                return makeDesertBack();
            case 9 :
            case 10 :
                return makeForestBack();
            case 11:
            case 12:
                return makeIslandBack();
            //endregion
            //region survival boards

            //endregion
        }
        return null;
    }

    public static Background makeSimpleBack() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.7f, .7f, .7f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagStripeHoriz"));
        overlay.setColor(new Color(.5f, .5f, .5f, 1));
        return new Background(
                back,
                new Sprite[] {overlay},
                new BackType[] {BackType.SCROLL_HORIZONTAL},
                null, null);
    }

    public static Background makeComplexBack() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.5f, .5f, .5f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagCheckerBackground"));
        overlay.setColor(new Color(.3f, .3f, .3f, 1));
        return new Background(
                back,
                new Sprite[] {overlay},
                new BackType[] {BackType.SCROLL_HORIZONTAL},
                null, null);
    }

    public static Background makeCompactBack() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(Color.LIGHT_GRAY);
        Sprite overlay = new Sprite(backAtlas.findRegion("CheckerBackground"));
        overlay.setColor(Color.WHITE);
        return new Background(
                back,
                new Sprite[] {overlay},
                new BackType[] {BackType.SCROLL_HORIZONTAL},
                null, null);
    }

    public static Background makeDesertBack() {
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(new Color(121f / 255, 121f / 255f, 19f / 255f, 1));
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("SpeedBackground")));
        topLayer.setColor(new Color(181f / 255, 181f / 255f, 79f / 255f, 1));
        return new Background(backgroundLay,
                new Sprite[]{new Sprite(backAtlas.findRegion("DiagStripeOverlay")), topLayer},
                new BackType[]{BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL},
                Color.DARK_GRAY, Color.WHITE);
    }

    public static Background makeForestBack() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.1f, .1f, 0, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagStripeHoriz"));
        overlay.setColor(new Color(.05f, .3f, 0, .6f));
        return new Background(
                back,
                new Sprite[] {overlay},
                new BackType[] {BackType.SCROLL_HORIZONTAL},
                null, null);
    }

    public static Background makeIslandBack() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.3f, .3f, 1, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("CloudBackground"));
        overlay.setColor(new Color(.9f, .9f, .9f, .6f));
        return new Background(
                back,
                new Sprite[] {overlay},
                new BackType[] {BackType.SCROLL_HORIZONTAL},
                null, null);
    }
}
