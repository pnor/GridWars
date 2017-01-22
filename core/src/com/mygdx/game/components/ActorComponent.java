package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.actors.UIActor;

/**
 * Component for whether the Entity has an Actor
 * @author pnore_000
 */
public class ActorComponent implements Component{
    public UIActor actor;

    public ActorComponent(UIActor a) {
        actor = a;
        actor.setBounds(actor.getX(), actor.getY(), actor.getWidth(), actor.getHeight());
    }
}
