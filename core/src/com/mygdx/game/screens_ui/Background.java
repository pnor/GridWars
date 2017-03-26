package com.mygdx.game.screens_ui;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.components.PositionComponent;
import com.mygdx.game.components.SpriteComponent;


/**
 * @author Phillip O'Reggio
 */
public class Background {
    private Entity backLayer;
    private Array<BackType> layers;

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

    public void update(float deltaTime) {
        for (BackType back : layers) {
            back.update(deltaTime);
        }
    }

    public Entity getBackLayer() {
        return backLayer;
    }

    public Array<BackType> getLayers() {
        return layers;
    }
}