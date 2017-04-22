package com.mygdx.game.creators;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.components.*;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.screens_ui.BattleScreen;

import static com.mygdx.game.ComponentMappers.state;
import static com.mygdx.game.GridWars.atlas;

/**
 * Class containing static methods for creating certain Entities.
 * @author Phillip O'Reggio
 */
public class EntityConstructor {
    //Testing purposes
    public static Entity testerChessPiece(int team, BattleScreen screen, Engine engine, Stage stage) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));

        entity.add(new ActorComponent(new SpriteActor(atlas.createSprite("Star"), true, true)));
        entity.add(new BoardComponent());
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{MoveConstructor.Tackle(entity, engine, stage, screen)})));
        entity.add(new StatComponent(1, 999, 1, 0, 8));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(MoveConstructor.damageAnimation(entity, engine, stage, screen),
                MoveConstructor.heavyDamageAnimation(entity, engine, stage, screen),
                MoveConstructor.deathAnimation(entity, engine, stage, screen)));

        return entity;
    }

    public static Entity testerRobot(int team, BattleScreen screen, Engine engine, Stage stage) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("Bot1"),
                atlas.findRegion("Bot2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(5, 7, 2, 1, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(MoveConstructor.damageAnimation(entity, engine, stage, screen),
                MoveConstructor.heavyDamageAnimation(entity, engine, stage, screen),
                MoveConstructor.deathAnimation(entity, engine, stage, screen)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{MoveConstructor.Tackle(entity, engine, stage, screen),
                MoveConstructor.StarSpin(entity, engine, stage, screen)})));
        entity.add(new NameComponent("Robo - Beta"));

        return entity;
    }

    public static Entity testerHole(int team, BattleScreen screen, Engine engine, Stage stage) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{atlas.findRegion("Hole"),
                atlas.findRegion("Hole2"), atlas.findRegion("Hole3"), atlas.findRegion("Hole4")},
                Animation.PlayMode.LOOP_PINGPONG, 0.1f)));
        entity.add(new BoardComponent());
        entity.add(new NameComponent("Hole"));

        return entity;
    }
}
