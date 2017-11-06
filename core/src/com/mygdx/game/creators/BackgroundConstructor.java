package com.mygdx.game.creators;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.mygdx.game.ui.BackType;
import com.mygdx.game.ui.Background;

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
                return makeSurvivalBackChecker();
            case 22: //10
                return makeSurvivalBackBlazePneuma();
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
                return makeSurvivalBackBlueZags();
            case 32: //20
                return makeSurvivalBackAquaPneuma();
            case 33:
            case 34:
                return makeSurvivalBackYellow();
            case 35:
                return makeSurvivalBackYellowDark();
            case 36:
            case 37: //25
            case 38:
                return makeSurvivalBackYellow();
            case 39:
                return makeSurvivalBackYellowDark();
            case 40:
            case 41:
                return makeSurvivalBackYellow();
            case 42: // 30
                return makeSurvivalElectroPneuma();
            case 43:
                return makeSurvivalBackGlow(new Color(0, 1, 0, .2f), new Color(1, 0, 1, .2f));
            case 44:
                return makeSurvivalBackGlow(new Color(1, .4f, 0, .2f), new Color(0, 0, .8f, .2f));
            case 45:
                return makeSurvivalBackGlow(new Color(1, 0, 0, .4f), new Color(0, .1f, 0, .2f));
            case 46:
                return makeSurvivalBackGlow(new Color(0, 0, .5f, .5f), new Color(.5f, .5f, 0, .5f));
            case 47: //35
                return makeSurvivalBackGlowBright();
            case 48:
                return makeSurvivalBackGlowAlt(new Color(.6f, 1, .6f, .6f), new Color(.6f, .6f, 1, .6f));
            case 49:
                return makeSurvivalBackGlowAlt(new Color(.8f, .8f, .8f, .6f), new Color(.2f, .2f, .2f, .6f));
            case 50:
                return makeSurvivalBackGlowAlt(new Color(0.1f, 0.1f, 0.1f, .2f), new Color(0.6f, 0.1f, 0.1f, .4f));
            case 51:
                return makeSurvivalBackGlowBright();
            case 52: //40
                return makeSurvivalBackBlazeMemory();
            case 53:
            case 54:
                return makeSurvivalBackShapeGlow();
            case 55:
                return makeSurvivalBackAquaMemory();
            case 56:
                return makeSurvivalBackShapeGlow();
            case 57: //45
            case 58:
            case 59:

            default:
                return makeSurvivalBackChecker();
            //endregion
        }
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
        Sprite overlay2 = new Sprite(backAtlas.findRegion("SquareStripHoriz"));
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

        Sprite overlay = new Sprite(backAtlas.findRegion("CheckerBackground"));
        overlay.setColor(Color.GRAY);

        Sprite overlay2 = new Sprite(backAtlas.findRegion("CheckerVert"));
        overlay2.setColor(new Color(.2f, .2f, .2f, .8f));

        return new Background(
                back,
                new Sprite[] {overlay, overlay2},
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
    //region regular
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

    public static Background makeSurvivalBackBlueZags() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.05f, .05f, .1f, 1));
        Sprite glower = new Sprite(backAtlas.findRegion("CheckerBackground"));
        glower.setColor(new Color(0, 0, .3f, .4f));
        Sprite overlay = new Sprite(backAtlas.findRegion("SquareStripHoriz"));
        overlay.setColor(new Color(.2f, .2f, .2f, .5f));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("SquareStripVert"));
        overlay2.setColor(new Color(.2f, .2f, .2f, .5f));
        return new Background(
                back,
                new Sprite[] {glower, overlay, overlay2},
                new BackType[] {BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL_SLOW, BackType.SCROLL_VERTICAL_SLOW},
                Color.DARK_GRAY, new Color(0, 0, .6f, 1));
    }

    public static Background makeSurvivalBackYellow() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.1f, .1f, 0, 1));
        Sprite glower = new Sprite(backAtlas.findRegion("DiagStripeOverlay"));
        glower.setColor(new Color(.3f, .3f, 0, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("SimpleRoundedZag"));
        overlay.setColor(new Color(.6f, .6f, 0, .3f));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("SpeedBackground"));
        overlay2.setColor(new Color(.9f, .9f, 0, .2f));
        Sprite overlay3 = new Sprite(backAtlas.findRegion("FadeBackground"));
        overlay3.setColor(new Color(0, 0, 0, .5f));
        return new Background(
                back,
                new Sprite[] {glower, overlay, overlay2, overlay3},
                new BackType[] {BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_SLOW ,BackType.NO_MOVE},
                new Color(.3f, .3f, 0, 1), new Color(.7f, .7f, 0, .1f));
    }

    public static Background makeSurvivalBackYellowDark() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.05f, .05f, 0, 1));
        Sprite glower = new Sprite(backAtlas.findRegion("DiagStripeOverlay"));
        glower.setColor(new Color(0, 0, 0, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagCheckerBackground"));
        overlay.setColor(new Color(.3f, .3f, 0, .3f));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("SpeedBackground"));
        overlay2.setColor(new Color(.4f, .4f, 0, .2f));
        Sprite overlay3 = new Sprite(backAtlas.findRegion("FadeBackground"));
        overlay3.setColor(new Color(1, 1, 1, .3f));
        return new Background(
                back,
                new Sprite[] {glower, overlay, overlay2, overlay3},
                new BackType[] {BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_SLOW ,BackType.NO_MOVE},
                new Color(0, 0, 0, 1), new Color(.4f, .4f, 0, .4f));
    }

    public static Background makeSurvivalBackGlow(Color start, Color end) {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(0, 0, 0, 1));
        Sprite glower = new Sprite(backAtlas.findRegion("FadeBackground"));
        glower.setColor(start);
        Sprite overlay = new Sprite(backAtlas.findRegion("CubeBackground"));
        overlay.setColor(new Color(1, 1, 1, .02f));
        return new Background(
                back,
                new Sprite[] {glower, overlay},
                new BackType[] {BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL_SLOW},
                start, end);
    }

    public static Background makeSurvivalBackGlowBright() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.88f, .88f, .9f, 1));
        Sprite glower = new Sprite(backAtlas.findRegion("FadeBackground"));
        glower.setColor(new Color(0, 0, 0, .7f));
        Sprite overlay = new Sprite(backAtlas.findRegion("CubeBackground"));
        overlay.setColor(new Color(0, 0, 0, .04f));
        return new Background(
                back,
                new Sprite[] {glower, overlay},
                new BackType[] {BackType.NO_MOVE, BackType.SCROLL_HORIZONTAL_SLOW},
                null, null);
    }

    public static Background makeSurvivalBackGlowAlt(Color start, Color end) {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(0, 0, 0, 1));
        Sprite glower = new Sprite(backAtlas.findRegion("DiagStripeHoriz"));
        glower.setColor(start);
        Sprite overlay = new Sprite(backAtlas.findRegion("HexagonBackground"));
        overlay.setColor(new Color(1, 1, 1, .06f));
        return new Background(
                back,
                new Sprite[] {glower, overlay},
                new BackType[] {BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL},
                start, end);
    }

    public static Background makeSurvivalBackShapeGlow() {
        Sprite back = new Sprite(backAtlas.findRegion("geometric"));
        back.setColor(new Color(.05f, .05f, .1f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("RadiusFade"));
        overlay.setColor(new Color(1, 1, 1, .13f));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("WavesHoriz"));
        overlay2.setColor(new Color(0, 0, 0, .1f));
        return new Background(
                back,
                new Sprite[] { overlay, overlay2},
                new BackType[] {BackType.NO_MOVE, BackType.SCROLL_HORIZONTAL_FASTER},
                null, null);
    }
    //endregion
    //region boss stages
    public static Background makeSurvivalBackBlazePneuma() {
        Sprite back = new Sprite(backAtlas.findRegion("SmudgeBackground"));
        back.setColor(new Color(.2f, .1f, .1f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagCheckerBackground"));
        overlay.setColor(new Color(.4f, .3f, .3f, .4f));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("CheckerBackground"));
        overlay2.setColor(new Color(.2f, .1f, .1f, .6f));
        Sprite overlay3 = new Sprite(backAtlas.findRegion("SharpDiagBackground"));
        overlay3.setColor(new Color(1, 0, 0, .1f));
        return new Background(
                back,
                new Sprite[] {overlay, overlay2, overlay3},
                new BackType[] {BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_FAST, BackType.SCROLL_HORIZONTAL_FASTER},
                null, null);
    }

    public static Background makeSurvivalBackAquaPneuma() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(0, 0, .1f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("SimpleRoundedZag"));
        overlay.setColor(new Color(0, 0, .1f, 1));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("CheckerVert"));
        overlay2.setColor(new Color(0, 0, 1, .1f));
        Sprite overlay3 = new Sprite(backAtlas.findRegion("CheckerBackground"));
        overlay3.setColor(new Color(0, 0, 1, .1f));
        return new Background(
                back,
                new Sprite[] {overlay, overlay2, overlay3},
                new BackType[] {BackType.FADE_COLOR, BackType.SCROLL_VERTICAL_FAST, BackType.SCROLL_HORIZONTAL_FAST},
                new Color(0, 0, .1f, 1), new Color(0, 0, .2f, 1));
    }

    public static Background makeSurvivalElectroPneuma() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.05f, .05f, 0, 1));
        Sprite glower = new Sprite(backAtlas.findRegion("DiagStripeOverlay"));
        glower.setColor(new Color(0, 0, 0, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("WavesHoriz"));
        overlay.setColor(new Color(.3f, .3f, 0, .3f));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("HexagonBackground"));
        overlay2.setColor(new Color(.4f, .4f, 0, .2f));
        Sprite overlay3 = new Sprite(backAtlas.findRegion("FadeBackground"));
        overlay3.setColor(new Color(1, 1, 0, .1f));
        return new Background(
                back,
                new Sprite[] {glower, overlay, overlay2, overlay3},
                new BackType[] {BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_SLOW ,BackType.NO_MOVE},
                new Color(0, 0, 0, 1), new Color(.2f, .2f, 0, .7f));
    }

    public static Background makeSurvivalBackBlazeMemory() {
        Sprite back = new Sprite(backAtlas.findRegion("SmudgeBackground"));
        back.setColor(new Color(.05f, .1f, .2f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagCheckerBackground"));
        overlay.setColor(new Color(.2f, .4f, .5f, .4f));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("CheckerBackground"));
        overlay2.setColor(new Color(.1f, .2f, .3f, .6f));
        Sprite overlay3 = new Sprite(backAtlas.findRegion("SharpDiagBackground"));
        overlay3.setColor(new Color(0.3f, .5f, .7f, .1f));
        return new Background(
                back,
                new Sprite[] {overlay, overlay2, overlay3},
                new BackType[] {BackType.SCROLL_HORIZONTAL_SLOW, BackType.SCROLL_HORIZONTAL, BackType.SCROLL_HORIZONTAL_FAST},
                null, null);
    }

    public static Background makeSurvivalBackAquaMemory() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(0, 0.05f, .1f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("SimpleRoundedZag"));
        overlay.setColor(new Color(0, .2f, .5f, 1));
        Sprite overlay2 = new Sprite(backAtlas.findRegion("CheckerVert"));
        overlay2.setColor(new Color(0, .4f, 1, .1f));
        Sprite overlay3 = new Sprite(backAtlas.findRegion("CheckerBackground"));
        overlay3.setColor(new Color(0, .4f, 1, .1f));
        return new Background(
                back,
                new Sprite[] {overlay, overlay2, overlay3},
                new BackType[] {BackType.FADE_COLOR, BackType.SCROLL_VERTICAL_FAST, BackType.SCROLL_HORIZONTAL_FAST},
                new Color(0, 0, .1f, 1), new Color(0, 0, .2f, 1));
    }
    //endregion
    //endregion
}
