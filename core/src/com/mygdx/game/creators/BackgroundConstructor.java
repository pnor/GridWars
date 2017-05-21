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
            case 1 :
            case 2 :
            case 3 :
                return makeSimpleBack();
        }
        return null;
    }

    public static Background makeSimpleBack() {
        Sprite back = new Sprite(backAtlas.findRegion("BlankBackground"));
        back.setColor(new Color(.5f, .5f, .5f, 1));
        Sprite overlay = new Sprite(backAtlas.findRegion("DiagStripeOverlay"));
        back.setColor(new Color(.7f, .7f, .7f, 1));
        return new Background(
                back,
                new Sprite[] {overlay},
                new BackType[] {BackType.SCROLL_HORIZONTAL},
                null, null);
    }

    public static Background makeDesertBack() {
        Sprite backgroundLay = new Sprite(backAtlas.findRegion("BlankBackground"));
        backgroundLay.setColor(new Color(121f / 255, 121f / 255f, 19f / 255f, 1));
        Sprite topLayer = new Sprite(new Sprite(backAtlas.findRegion("DiagStripeOverlay")));
        topLayer.setColor(new Color(181f / 255, 181f / 255f, 79f / 255f, 1));
        return new Background(backgroundLay,
                new Sprite[]{new Sprite(backAtlas.findRegion("DiagStripeOverlay")), topLayer},
                new BackType[]{BackType.FADE_COLOR, BackType.SCROLL_HORIZONTAL},
                Color.DARK_GRAY, Color.WHITE);
    }
}
