package com.mygdx.game.screens_ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.components.PositionComponent;
import com.mygdx.game.components.SpriteComponent;


/**
 * Background class that has multiple animated layers.
 * @author Phillip O'Reggio
 */
public class Background {
    private Entity backLayer;
    private Array<BackType> layers;
    private static boolean animateBackground = true;

    /**
     * backTypes and frontLayer must be equal size.
     * @param staticLayer non moving back sprite
     * @param frontLayer sprites for top layers
     * @param backTypes type of movement for each top layer (frontLayer[0] -> backTypes[0])
     * @param startColor Start interpolation color for all fade color backTypes. Use {@code null} if there is none
     * @param endColor End interpolation color for all fade color backTypes. Use {@code null} if there is none
     */
    public Background(Sprite staticLayer, Sprite[] frontLayer, BackType[] backTypes, Color startColor, Color endColor) {
        if (frontLayer.length != backTypes.length)
            throw new ExceptionInInitializerError("Layers and BackTypes must be equal; they correspond to each other");

        backLayer = new Entity();
        backLayer.add(new SpriteComponent(staticLayer));
        backLayer.add(new PositionComponent(new Vector2(0,0), staticLayer.getRegionHeight(), staticLayer.getRegionWidth(), 0));

        layers = new Array<BackType>();
        for (int i = 0; i < backTypes.length; i++) {
            backTypes[i].setImage(frontLayer[i]);
            if (backTypes[i] == BackType.FADE_COLOR)
                backTypes[i].setColors(startColor, endColor);
            layers.add(backTypes[i]);
        }
    }

    /**
     * backTypes and frontLayer must be equal size.
     * @param staticLayer non moving back sprite
     * @param frontLayer Entities for top layers
     * @param backTypes type of movement for each top layer (frontLayer[0] -> backTypes[0])
     * @param startColor Start interpolation color for all fade color backTypes. Use {@code null} if there is none
     * @param endColor End interpolation color for all fade color backTypes. Use {@code null} if there is none
     */
    public Background(Sprite staticLayer, Entity[] frontLayer, BackType[] backTypes, Color startColor, Color endColor) {
        if (frontLayer.length != backTypes.length)
            throw new ExceptionInInitializerError("Layers and BackTypes must be equal; they correspond to each other");

        backLayer = new Entity();
        backLayer.add(new SpriteComponent(staticLayer));
        backLayer.add(new PositionComponent(new Vector2(0,0), staticLayer.getRegionHeight(), staticLayer.getRegionWidth(), 0));

        layers = new Array<BackType>();
        for (int i = 0; i < backTypes.length; i++) {
            backTypes[i].setEntity(frontLayer[i]);
            if (backTypes[i] == BackType.FADE_COLOR)
                backTypes[i].setColors(startColor, endColor);
            layers.add(backTypes[i]);
        }
    }

    public void update(float deltaTime) {
        if (animateBackground)
            for (BackType back : layers)
                back.update(deltaTime);
    }

    public static void setAnimateBackground(boolean b) {
        animateBackground = b;
    }

    public Entity getBackLayer() {
        return backLayer;
    }

    public Array<BackType> getLayers() {
        return layers;
    }
}