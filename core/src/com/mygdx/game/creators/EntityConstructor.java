package com.mygdx.game.creators;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.components.*;
import com.mygdx.game.move_related.Move;

import static com.mygdx.game.ComponentMappers.state;
import static com.mygdx.game.ComponentMappers.stm;
import static com.mygdx.game.GridWars.atlas;

/**
 * Class containing static methods for creating certain Entities.
 * @author Phillip O'Reggio
 */
public class EntityConstructor {
    private static boolean ready;
    
    private static Engine engine;
    private static Stage stage;

    /**
     * Readies the EntityConstructor for use
     * @param eng {@link Engine}
     * @param stge {@link Stage}
     */
    public static void initialize(Engine eng, Stage stge) {
        engine = eng;
        stage = stge;
        ready = true;
    }

    /**
     * Clears static fields in EntityConstructor
     */
    public static void clear() {
        engine = null;
        stage = null;
        ready = false;
    }
    
    public static boolean isReady() {
        return ready;
    }

    //Testing purposes ----
    public static Entity testerChessPiece(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));

        entity.add(new ActorComponent(new SpriteActor(atlas.createSprite("tester"), true, true)));
        entity.add(new BoardComponent());
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{MoveConstructor.Tackle(entity)})));
        entity.add(new StatComponent(1, 999, 1, 0, 8));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));

        return entity;
    }

    public static Entity testerRobot(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("robot"),
                atlas.findRegion("robot2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(5, 7, 2, 1, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{MoveConstructor.Tackle(entity),
                MoveConstructor.StarSpin(entity)})));
        entity.add(new NameComponent("Robo - Beta"));

        return entity;
    }

    public static Entity testerPlaceHolder(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("robot"),
                atlas.findRegion("robot2")
        }, Animation.PlayMode.LOOP, 0.5f)));        entity.add(new BoardComponent());
        entity.add(new StatComponent(1, 20, 20, 0, 14));
        stm.get(entity).sp  = 20;
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.Tackle(entity),
                MoveConstructor.submerge(entity),
                MoveConstructor.assist(entity),
                MoveConstructor.wildFire(entity)})));
        entity.add(new NameComponent("TESTER"));

        return entity;
    }

    public static Entity testerHole(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));

        entity.add(new ActorComponent(new SpriteActor(new Sprite(atlas.findRegion("hole")))));
        entity.add(new BoardComponent());
        entity.add(new NameComponent("Hole"));

        return entity;
    }

    //region Blockade Type Entity
    public static Entity cube() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(5, 0, 0, 0, 0));
        entity.add(new ActorComponent(new SpriteActor((atlas.createSprite("cube")), true, true)));
        entity.add(new NameComponent("Cube"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));

        return entity;
    }

    public static Entity durableCube() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(20, 0, 0, 0, 0));
        entity.add(new ActorComponent(new SpriteActor((atlas.createSprite("cubelight")), true, true)));
        entity.add(new NameComponent("Durable Cube"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));

        return entity;
    }

    public static Entity cactus() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 0, 0, 1, 0));
        entity.add(new ActorComponent(new SpriteActor((atlas.createSprite("Cactus")), true, true)));
        entity.add(new NameComponent("Cactus"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));

        return entity;
    }

    public static Entity flowerCactus() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(5, 0, 0, 0, 0));
        entity.add(new ActorComponent(new SpriteActor((atlas.createSprite("flowercactus")), true, true)));
        entity.add(new NameComponent("Flower Cactus"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));

        return entity;
    }
    //endregion

    //Game Piece Entity ------------
    public static Entity canight(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Canight"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("Canight"),
                atlas.findRegion("Canight2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 4, 2, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.swordSlice(entity),
                MoveConstructor.Bark(entity),
                MoveConstructor.chargedSlice(entity)
        })));

        return entity;
    }

    public static Entity catdroid(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Catdroid"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("catdroid"),
                atlas.findRegion("catdroid2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 5, 2, 2, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<>(new Move[]{
                MoveConstructor.metalClaw(entity),
                MoveConstructor.laserBeam(entity),
                MoveConstructor.electricalFire(entity),
                MoveConstructor.laserSpray(entity)
        })));

        return entity;
    }

    public static Entity pyrobull(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Pyrobull"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("firebull"),
                atlas.findRegion("firebull2")
        }, Animation.PlayMode.LOOP, 0.2f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(5, 4, 5, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.bodySlam(entity),
                MoveConstructor.sear(entity),
                MoveConstructor.wildFire(entity)
        })));

        return entity;
    }

    public static Entity freezird(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Freezird"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("icebird"),
                atlas.findRegion("icebird2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 5, 2, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.chill(entity),
                MoveConstructor.tailwind(entity),
                MoveConstructor.twister(entity),
                MoveConstructor.freeze(entity)
        })));

        return entity;
    }

    public static Entity medicarp(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Medicarp"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("fish"),
                atlas.findRegion("fish2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(8, 4, 1, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.assist(entity),
                MoveConstructor.clear(entity),
                MoveConstructor.recover(entity),
                MoveConstructor.submerge(entity)
        })));

        return entity;
    }

    public static Entity thoughtoise(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Thoughtoise"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("turtle"),
                atlas.findRegion("turtle2")
        }, Animation.PlayMode.LOOP, 0.7f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(2, 8, 3, 4, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.restMind(entity),
                MoveConstructor.drench(entity),
                MoveConstructor.electrocute(entity),
                MoveConstructor.cometShower(entity)
        })));

        return entity;
    }

    public static Entity vulpedge(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Vulpedge"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("fox"),
                atlas.findRegion("fox2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 3, 2, 1, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.swordSlice(entity),
                MoveConstructor.guardPiercer(entity),
                MoveConstructor.pierceSwordSlice(entity),
                MoveConstructor.poisonBlade(entity)
        })));

        return entity;
    }

    public static Entity thundog(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Thundog"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("thunderdog"),
                atlas.findRegion("thunderdog2")
        }, Animation.PlayMode.LOOP, 0.2f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 4, 1, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.shockClaw(entity),
                MoveConstructor.charge(entity),
                MoveConstructor.superCharge(entity),
                MoveConstructor.voltDeluge(entity)
        })));

        return entity;
    }

    public static Entity mummy(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Mummy"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("mummy"),
                atlas.findRegion("mummy2")
        }, Animation.PlayMode.LOOP, 0.3f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 3, 2, 1, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.barrage(entity),
                MoveConstructor.feint(entity),
                MoveConstructor.basiliskPunch(entity),
                MoveConstructor.curse(entity)
        })));

        return entity;
    }

    public static Entity squizerd(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Squizerd"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("squid"),
                atlas.findRegion("squid2")
        }, Animation.PlayMode.LOOP, 0.7f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 9, 3, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.restBody(entity),
                MoveConstructor.ignite(entity),
                MoveConstructor.drench(entity), //TODO change probably
                MoveConstructor.cometShowerClose(entity)
        })));

        return entity;
    }

    public static Entity wyvrapor(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Wyvrapor"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("steamdragon"),
                atlas.findRegion("steamdragon2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 4, 1, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.dragonBreath(entity),
                MoveConstructor.toxicBreath(entity),
                MoveConstructor.freshBreath(entity),
                MoveConstructor.spaBreath(entity)
        })));

        return entity;
    }

    public static Entity jellymiss(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Jellymiss"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("jellygirl"),
                atlas.findRegion("jellygirl2")
        }, Animation.PlayMode.LOOP, 0.7f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 10, 1, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.restore(entity),
                MoveConstructor.regen(entity)
        })));

        return entity;
    }

    public static Entity mirrorman(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Mirror Man"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("mirrorman"),
                atlas.findRegion("mirrorman2")
        }, Animation.PlayMode.LOOP, 0.3f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(5, 12, 1, 1, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.Tackle(entity),
                MoveConstructor.StarSpin(entity)
        })));

        return entity;
    }
}
