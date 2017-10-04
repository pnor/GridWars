package com.mygdx.game.creators;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.components.PositionComponent;
import com.mygdx.game.components.SpriteComponent;
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
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
                return makeSurvivalBackChecker();
            //endregion
        }
        return null;
    }

    //region regular battle
    public static Background makeSimpleBack() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.8f, .8f, .8f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagStripeHoriz"));
        overlay.setColor(new Color(.7f, .7f, .7f, 1));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("SimpleRoundedZag"));
        overlay2.setColor(new Color(.5f, .5f, .5f, .1f));
        return new Background(
                back,
                new Sprite[] {overlay, overlay2},
                new BackType[] {BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_SLOW},
                null, null);
    }

    public static Background makeComplexBack() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.5f, .5f, .5f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagCheckerBackground"));
        overlay.setColor(new Color(.3f, .3f, .3f, 1));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("SqureStripHoriz"));
        overlay2.setColor(new Color(.2f, .2f, .2f, 1f));
        return new Background(
                back,
                new Sprite[] {overlay, overlay2},
                new BackType[] {BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_SLOW},
                null, null);
    }

    public static Background makeCompactBack() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(Color.DARK_GRAY);
        Entity backgroundEntity = new Entity();
        backgroundEntity.add(new SpriteComponent(back));
        backgroundEntity.add(new PositionComponent(new Vector2(0, 0), back.getHeight(), back.getWidth(), 0));

        Sprite overlay = new Sprite(backAtlas.findRegion("CheckerBackground"));
        overlay.setColor(Color.GRAY);
        Entity overlayEntity = new Entity();
        overlayEntity.add(new SpriteComponent(overlay));
        overlayEntity.add(new PositionComponent(new Vector2(0, 0), overlay.getHeight(), overlay.getWidth(), 0));

        Sprite overlay2 = new Sprite(backAtlas.findRegion("CheckerBackground"));
        overlay2.setColor(Color.LIGHT_GRAY);
        Entity overlayEntity2 = new Entity();
        overlayEntity2.add(new SpriteComponent(overlay2));
        overlayEntity2.add(new PositionComponent(new Vector2(-overlay2.getWidth() / 4, 0), overlay2.getHeight(), back.getWidth(), 90));

        return new Background(
                back,
                new Entity[] {overlayEntity, overlayEntity2},
                new BackType[] {BackType.SCROLL_HORIZONTAL, BackType.SCROLL_VERTICAL_FAST},
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
    //endregion

    //region survival
    public static Background makeSurvivalBackChecker() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.3f, .3f, .3f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagCheckerBackground"));
        overlay.setColor(new Color(.4f, .4f, .4f, 1));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("DiagStripeHoriz"));
        overlay2.setColor(new Color(1, 1, 1, .1f));
        return new Background(
                back,
                new Sprite[] {overlay, overlay2},
                new BackType[] {BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_SLOW},
                null, null);
    }
    //endregion
}
