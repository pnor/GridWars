package com.mygdx.game.creators;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.actors.AnimationActor;
import com.mygdx.game.actors.SpriteActor;
import com.mygdx.game.components.*;
import com.mygdx.game.misc.Phase;
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

    //region Testing purposes
    public static Entity testerChessPiece(int team, int altColor) {
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
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));

        return entity;
    }

    public static Entity testerRobot(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("robot"),
                atlas.findRegion("robot2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(5, 5, 1, 1, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{MoveConstructor.Tackle(entity),
                MoveConstructor.bodySlam(entity)})));
        entity.add(new NameComponent("Robo - Beta"));

        return entity;
    }

    public static Entity testerPlaceHolder(int team, int altColor) {
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
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.Tackle(entity),
                MoveConstructor.submerge(entity),
                MoveConstructor.assist(entity),
                MoveConstructor.wildFire(entity)})));
        entity.add(new NameComponent("TESTER"));

        return entity;
    }

    public static Entity AITester(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("AI tester"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("floorSpike"),
                atlas.findRegion("iceSpike")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(5, 5, 1, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.swordSlice(entity)
                //--,
        })));

        return entity;
    }

    public static Entity testerHole(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));

        entity.add(new ActorComponent(new SpriteActor(new Sprite(atlas.findRegion("hole")))));
        entity.add(new BoardComponent());
        entity.add(new NameComponent("Hole"));

        return entity;
    }
    //endregion

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
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));

        return entity;
    }

    public static Entity durableCube() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(10, 0, 0, 0, 0));
        entity.add(new ActorComponent(new SpriteActor((atlas.createSprite("cubelight")), true, true)));
        entity.add(new NameComponent("Durable Cube"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));

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
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));

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
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)
                ));

        return entity;
    }

    public static Entity tree() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(2, 0, 0, 1, 0));
        entity.add(new ActorComponent(new SpriteActor((atlas.createSprite("tree")), true, true)));
        entity.add(new NameComponent("Tree"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));

        return entity;
    }

    public static Entity torch() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(2, 0, 0, 0, 0));
        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("torch"),
                atlas.findRegion("torch2")
        }, Animation.PlayMode.LOOP, 0.3f)));
        entity.add(new NameComponent("Torch"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));

        return entity;
    }

    public static Entity stoneTorch() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 0, 0, 0, 0));
        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("stoneTorch"),
                atlas.findRegion("stoneTorch2")
        }, Animation.PlayMode.LOOP, 0.3f)));
        entity.add(new NameComponent("Stone Torch"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));

        return entity;
    }

    public static Entity gargoyleStatue() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(7, 0, 0, 2, 0));
        entity.add(new ActorComponent(new SpriteActor(atlas.findRegion("gargoyleStatue"))));
        entity.add(new NameComponent("Gargoyle Statue"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));

        return entity;
    }

    public static Entity pillar() {
        Entity entity = new Entity();
        entity.add(new BoardComponent());
        entity.add(new StatComponent(6, 0, 0, 0, 0));
        entity.add(new ActorComponent(new SpriteActor((atlas.createSprite("pillar")), true, true)));
        entity.add(new NameComponent("Pillar"));

        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));

        return entity;
    }
    //endregion

    //Game Piece Entity ------------
    //region player entities
    public static Entity canight(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Canight"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("Canight"),
                    atlas.findRegion("Canight2")
            }, Animation.PlayMode.LOOP, 0.5f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("CanightAlt"),
                    atlas.findRegion("Canight2Alt")
            }, Animation.PlayMode.LOOP, 0.5f)));
        
        entity.add(new BoardComponent());
        entity.add(new StatComponent(2, 4, 2, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.swordSlice(entity),
                MoveConstructor.Bark(entity),
                MoveConstructor.chargedSlice(entity)
        })));

        return entity;
    }

    public static Entity catdroid(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Catdroid"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("catdroid"),
                    atlas.findRegion("catdroid2")
            }, Animation.PlayMode.LOOP, 0.5f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("catdroidAlt"),
                    atlas.findRegion("catdroid2Alt")
            }, Animation.PlayMode.LOOP, 0.5f)));
        
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 5, 2, 2, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<>(new Move[]{
                MoveConstructor.metalClaw(entity),
                MoveConstructor.laserBeam(entity),
                MoveConstructor.electricalFire(entity),
                MoveConstructor.laserSpray(entity)
        })));

        return entity;
    }

    public static Entity pyrobull(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Pyrobull"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("firebull"),
                    atlas.findRegion("firebull2")
            }, Animation.PlayMode.LOOP, 0.2f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("firebullAlt"),
                    atlas.findRegion("firebull2Alt")
            }, Animation.PlayMode.LOOP, 0.2f)));
       
        entity.add(new BoardComponent());
        entity.add(new StatComponent(6, 4, 5, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.bodySlam(entity),
                MoveConstructor.sear(entity),
                MoveConstructor.wildFire(entity)
        })));

        return entity;
    }

    public static Entity freezird(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Freezird"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("icebird"),
                    atlas.findRegion("icebird2")
            }, Animation.PlayMode.LOOP, 0.5f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("icebirdAlt"),
                    atlas.findRegion("icebird2Alt")
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
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.chill(entity),
                MoveConstructor.tailwind(entity),
                MoveConstructor.twister(entity),
                MoveConstructor.freeze(entity)
        })));

        return entity;
    }

    public static Entity medicarp(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Medicarp"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("fish"),
                    atlas.findRegion("fish2")
            }, Animation.PlayMode.LOOP, 0.5f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("fishAlt"),
                    atlas.findRegion("fish2Alt")
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
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.assist(entity),
                MoveConstructor.clear(entity),
                MoveConstructor.recover(entity),
                MoveConstructor.submerge(entity)
        })));

        return entity;
    }

    public static Entity thoughtoise(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Thoughtoise"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("turtle"),
                    atlas.findRegion("turtle2")
            }, Animation.PlayMode.LOOP, 0.7f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("turtleAlt"),
                    atlas.findRegion("turtle2Alt")
            }, Animation.PlayMode.LOOP, 0.7f)));

        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 8, 3, 2, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.restMind(entity),
                MoveConstructor.drench(entity),
                MoveConstructor.electrocute(entity),
                MoveConstructor.cometShower(entity)
        })));

        return entity;
    }

    public static Entity vulpedge(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Vulpedge"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("fox"),
                    atlas.findRegion("fox2")
            }, Animation.PlayMode.LOOP, 0.5f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("foxAlt"),
                    atlas.findRegion("fox2Alt")
            }, Animation.PlayMode.LOOP, 0.5f)));

        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 3, 2, 1, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.swordSlice(entity),
                MoveConstructor.guardPiercer(entity),
                MoveConstructor.pierceSwordSlice(entity),
                MoveConstructor.poisonBlade(entity)
        })));

        return entity;
    }

    public static Entity thundog(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Thundog"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("thunderdog"),
                    atlas.findRegion("thunderdog2")
            }, Animation.PlayMode.LOOP, 0.2f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("thunderdogAlt"),
                    atlas.findRegion("thunderdog2Alt")
            }, Animation.PlayMode.LOOP, 0.2f)));

        entity.add(new BoardComponent());
        entity.add(new StatComponent(7, 5, 1, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.shockClaw(entity),
                MoveConstructor.charge(entity),
                MoveConstructor.superCharge(entity),
                MoveConstructor.voltDeluge(entity)
        })));

        return entity;
    }

    public static Entity mummy(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Mummy"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("mummy"),
                    atlas.findRegion("mummy2")
            }, Animation.PlayMode.LOOP, 0.3f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("mummyAlt"),
                    atlas.findRegion("mummy2Alt")
            }, Animation.PlayMode.LOOP, 0.3f)));

        entity.add(new BoardComponent());
        entity.add(new StatComponent(5, 5, 2, 1, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.barrage(entity),
                MoveConstructor.feint(entity),
                MoveConstructor.basiliskPunch(entity),
                MoveConstructor.curse(entity)
        })));

        return entity;
    }

    public static Entity squizerd(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Squizerd"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("squid"),
                    atlas.findRegion("squid2")
            }, Animation.PlayMode.LOOP, 0.7f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("squidAlt"),
                    atlas.findRegion("squid2Alt")
            }, Animation.PlayMode.LOOP, 0.7f)));

        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 9, 3, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.restBody(entity),
                MoveConstructor.ignite(entity),
                MoveConstructor.drench2(entity),
                MoveConstructor.cometShowerClose(entity)
        })));

        return entity;
    }

    public static Entity wyvrapor(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Wyvrapor"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("steamdragon"),
                    atlas.findRegion("steamdragon2")
            }, Animation.PlayMode.LOOP, 0.5f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("steamdragonAlt"),
                    atlas.findRegion("steamdragon2Alt")
            }, Animation.PlayMode.LOOP, 0.5f)));

        entity.add(new BoardComponent());
        entity.add(new StatComponent(6, 6, 1, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.dragonBreath(entity),
                MoveConstructor.toxicBreath(entity),
                MoveConstructor.freshBreath(entity),
                MoveConstructor.spaBreath(entity)
        })));

        return entity;
    }

    public static Entity jellymiss(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Jellymiss"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("jellygirl"),
                    atlas.findRegion("jellygirl2")
            }, Animation.PlayMode.LOOP, 0.7f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("jellygirlAlt"),
                    atlas.findRegion("jellygirl2Alt")
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
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.restore(entity),
                MoveConstructor.regen(entity),
                MoveConstructor.boost(entity),
                MoveConstructor.transfer(entity)
        })));

        return entity;
    }

    public static Entity mirrorman(int team, int altColor) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Mirror Man"));

        if (altColor == 0)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("mirrorman"),
                    atlas.findRegion("mirrorman2")
            }, Animation.PlayMode.LOOP, 0.3f)));
        else if (altColor >= 1)
            entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                    atlas.findRegion("mirrormanAlt"),
                    atlas.findRegion("mirrorman2Alt")
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
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.reflectMove(entity),
                MoveConstructor.mirrorMove(entity),
                MoveConstructor.rouletteReflect(entity),
                MoveConstructor.clear(entity)
        })));

        return entity;
    }
    //endregion

    //region Survival enemy entities

    //region bosses
    public static Entity blazePneuma(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Blaze Pneuma"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("fireSpirit"),
                atlas.findRegion("fireSpirit2")
        }, Animation.PlayMode.LOOP, 0.4f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(20, 6, 3, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimationExplosive(entity, Color.RED),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.fireSlash(entity),
                MoveConstructor.chainFire(entity),
                MoveConstructor.flameCharge(entity),
                MoveConstructor.blueFlame(entity)
        })));

        return entity;
    }

    public static Entity aquaPneuma(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Aqua Pneuma"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("waterSpirit"),
                atlas.findRegion("waterSpirit2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(25, 5, 4, 1, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimationExplosive(entity, Color.RED),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.hammerStrike(entity),
                MoveConstructor.drench(entity),
                MoveConstructor.gather(entity),
                MoveConstructor.superGuard(entity)
        })));

        return entity;
    }
    //endregion

    //region small lion
    public static Entity stoneLeo(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Stone Leo"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("StoneLion"),
                atlas.findRegion("StoneLion2")
        }, Animation.PlayMode.LOOP, 0.8f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(2, 2, 1, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.slam(entity),
                MoveConstructor.stoneGlare(entity)
        })));

        return entity;
    }
    //endregion

    //region big lion
    public static Entity stoneLion(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Stone Lion"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("lion"),
                atlas.findRegion("lion2")
        }, Animation.PlayMode.LOOP, 0.7f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(6, 5, 3, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.claw(entity),
                MoveConstructor.heavySlam(entity),
                MoveConstructor.stoneGlare(entity)
        })));

        return entity;
    }

    public static Entity yellowLion(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Yellow Lion"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("yellowLion"),
                atlas.findRegion("yellowLion2")
        }, Animation.PlayMode.LOOP, 0.7f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(7, 8, 4, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.claw(entity),
                MoveConstructor.prepare(entity),
                MoveConstructor.roar(entity),
                MoveConstructor.judgingGlare(entity)
        })));

        return entity;
    }

    public static Entity blueLion(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Blue Lion"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("blueLion"),
                atlas.findRegion("blueLion2")
        }, Animation.PlayMode.LOOP, 0.7f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(12, 4, 5, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.blueClaw(entity),
                MoveConstructor.ready(entity),
                MoveConstructor.neoRoar(entity),
                MoveConstructor.reflectionBeamLion(entity)
        })));

        return entity;
    }
    //endregion

    //region gargoyle
    public static Entity gargoyle(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Gargoyle"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("gargoyle"),
                atlas.findRegion("gargoyle2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(6, 6, 2, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.claw(entity),
                MoveConstructor.stoneGlare(entity),
                MoveConstructor.penetrate(entity)
        })));

        return entity;
    }

    public static Entity archgargoyle(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Archgargoyle"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("whiteGargoyle"),
                atlas.findRegion("whiteGargoyle2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(8, 8, 2, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.crushClaw(entity),
                MoveConstructor.beam(entity),
                MoveConstructor.stoneGlare(entity),
                MoveConstructor.judgingGlare(entity)
        })));

        return entity;
    }
    //endregion

    //region scaleman
    public static Entity scaleman(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Scaleman"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("scaleman"),
                atlas.findRegion("scaleman2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(10, 5, 1, 1, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.slam(entity),
                MoveConstructor.heavySlam(entity),
                MoveConstructor.guard(entity)
        })));
        entity.add(new PhaseComponent(
                new Phase(3, new AnimationActor(new TextureRegion[]{
                        atlas.findRegion("scalemanHigh"),
                        atlas.findRegion("scalemanHigh2")},
                        Animation.PlayMode.LOOP, 0.1f),
                        new StatComponent(10, 5, 4, 0, 3)),
                new Phase(6, new AnimationActor(new TextureRegion[]{
                        atlas.findRegion("scalemanMid"),
                        atlas.findRegion("scalemanMid2")},
                        Animation.PlayMode.LOOP, 0.2f),
                        new StatComponent(10, 5, 2, 0, 2)),
                new Phase(10, new AnimationActor(new TextureRegion[]{
                        atlas.findRegion("scaleman"),
                        atlas.findRegion("scaleman2")},
                        Animation.PlayMode.LOOP, 0.5f),
                        new StatComponent(10, 5, 1, 1, 1))
        ));
        return entity;
    }

    public static Entity chromeMan(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Chrome Man"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("brightScaleman"),
                atlas.findRegion("brightScaleman2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(10, 7, 1, 1, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.reflectionBeam(entity),
                MoveConstructor.refractionBeam(entity),
                MoveConstructor.recover(entity),
                MoveConstructor.superGuard(entity)
        })));
        entity.add(new PhaseComponent(
                new Phase(3, new AnimationActor(new TextureRegion[]{
                        atlas.findRegion("brightScalemanHigh"),
                        atlas.findRegion("brightScalemanHigh2")},
                        Animation.PlayMode.LOOP, 0.1f),
                        new StatComponent(10, 5, 3, 0, 3)),
                new Phase(6, new AnimationActor(new TextureRegion[]{
                        atlas.findRegion("brightScalemanMid"),
                        atlas.findRegion("brightScalemanMid2")},
                        Animation.PlayMode.LOOP, 0.2f),
                        new StatComponent(10, 7, 2, 1, 2)),
                new Phase(10, new AnimationActor(new TextureRegion[]{
                        atlas.findRegion("brightScaleman"),
                        atlas.findRegion("brightScaleman2")},
                        Animation.PlayMode.LOOP, 0.5f),
                        new StatComponent(10, 7, 1, 1, 1))
        ));
        return entity;
    }
    //endregion

    //region slimemen
    public static Entity slimeman(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Slimeman"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("slimeman"),
                atlas.findRegion("slimeman2")
        }, Animation.PlayMode.LOOP, .5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 2, 1, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.poisonPunch(entity),
                MoveConstructor.immobilize(entity)
        })));

        return entity;
    }

    public static Entity chemMan(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Chemistry Man"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("slimemanNeo"),
                atlas.findRegion("slimemanNeo2")
        }, Animation.PlayMode.LOOP, .5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 4, 3, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.stunPunch(entity),
                MoveConstructor.regenerate(entity)
        })));

        return entity;
    }

    public static Entity alkaliMan(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Alkali Man"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("slimemanDark"),
                atlas.findRegion("slimemanDark2")
        }, Animation.PlayMode.LOOP, .5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(6, 4, 3, 0, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.mysteryStrike(entity),
                MoveConstructor.accursedSludge(entity),
                MoveConstructor.regenerate(entity)
        })));

        return entity;
    }
    //endregion

    //region can man
    public static Entity canman(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Can Man"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("toxicCanMan"),
                atlas.findRegion("toxicCanMan2")
        }, Animation.PlayMode.LOOP, .5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(2, 4, 1, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.sludgeThrow(entity),
                MoveConstructor.suppressAttack(entity)
        })));

        return entity;
    }

    public static Entity toxicCanman(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Toxic Can Man"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("acidicCanMan"),
                atlas.findRegion("acidicCanMan2")
        }, Animation.PlayMode.LOOP, .5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 5, 1, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.sludgeThrow2(entity),
                MoveConstructor.toxicThrow(entity),
                MoveConstructor.suppressDefense(entity)
        })));

        return entity;
    }

    public static Entity medicanMan(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Medican Man"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("healCanMan"),
                atlas.findRegion("healCanMan2")
        }, Animation.PlayMode.LOOP, .5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(2, 7, 1, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.suppressAttack(entity),
                MoveConstructor.suppressDefense(entity),
                MoveConstructor.medicalThrow(entity)
        })));

        return entity;
    }

    //endregion

    //region golem
    public static Entity golem(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Golem"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("StoneGolem"),
                atlas.findRegion("StoneGolem2")
        }, Animation.PlayMode.LOOP, 1f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(7, 2, 2, 1, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.slam(entity),
                MoveConstructor.heavySlam(entity),
                MoveConstructor.guard(entity)
        })));

        return entity;
    }

    public static Entity redGolem(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Red Golem"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("RedGolem"),
                atlas.findRegion("RedGolem2")
        }, Animation.PlayMode.LOOP, 1f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(8, 3, 2, 1, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.slamRed(entity),
                MoveConstructor.heavySlamRed(entity),
                MoveConstructor.guard(entity)
        })));

        return entity;
    }

    public static Entity golemMK2(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Golem MK 2"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("betaGolem"),
                atlas.findRegion("betaGolem2"),
                atlas.findRegion("betaGolem3")
        }, Animation.PlayMode.LOOP_PINGPONG, .6f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(6, 5, 2, 1, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.slamRed(entity),
                MoveConstructor.heavySlamRed(entity),
                MoveConstructor.laserBeam(entity),
                MoveConstructor.superGuard(entity)
        })));

        return entity;
    }

    public static Entity golemMK3(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Golem MK 3"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("neoGolem"),
                atlas.findRegion("neoGolem2")
        }, Animation.PlayMode.LOOP_PINGPONG, .3f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(7, 7, 2, 1, 1));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.slamBlue(entity),
                MoveConstructor.laserBeamBlue(entity),
                MoveConstructor.ultimateGuard(entity)
        })));

        return entity;
    }
    //endregion

    //region spiders
    public static Entity spider(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Spider"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("spider"),
                atlas.findRegion("spider2")
        }, Animation.PlayMode.LOOP, 0.3f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 3, 1, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.slash(entity),
                MoveConstructor.toxicSlash(entity),
                MoveConstructor.immobite(entity)
        })));

        return entity;
    }

    public static Entity immoralSpider(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Immoral Spider"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("toughspider"),
                atlas.findRegion("toughspider2")
        }, Animation.PlayMode.LOOP, 0.3f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 4, 2, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.stealSkill(entity),
                MoveConstructor.stealHealth(entity),
                MoveConstructor.demoralizeBlow(entity)
        })));

        return entity;
    }

    public static Entity lethalSpider(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Lethal Spider"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("redSpider"),
                atlas.findRegion("redSpider2")
        }, Animation.PlayMode.LOOP, 0.15f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 3, 6, 0, 3));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.slash2(entity)
        })));

        return entity;
    }

    //endregion

    //region spirit
    //swords
    public static Entity stoneSword(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Possessed Sword"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("stoneSword"),
                atlas.findRegion("stoneSword2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(2, 0, 2, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.swordSlice(entity)
        })));

        return entity;
    }

    public static Entity blueSword(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Possessed Sword"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("possesedSwordBlue"),
                atlas.findRegion("possesedSwordBlue2")
        }, Animation.PlayMode.LOOP, 0.5f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 4, 5, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.swordSlice(entity),
                MoveConstructor.guardPiercer(entity),
                MoveConstructor.chargedSlice(entity)
        })));

        return entity;
    }
    //books
    public static Entity book(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Haunted Book"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("possesedBook"),
                atlas.findRegion("possesedBook2")
        }, Animation.PlayMode.LOOP, 0.4f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 5, 2, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.monoplode(entity),
                MoveConstructor.monopierce(entity),
                MoveConstructor.curse(entity)
        })));

        return entity;
    }

    public static Entity advancedBook(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Ghastly Textbook"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("advancedBook"),
                atlas.findRegion("advancedBook2")
        }, Animation.PlayMode.LOOP, 0.4f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(4, 7, 3, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.monoplode2(entity),
                MoveConstructor.disarm(entity),
                MoveConstructor.monoflash(entity)
        })));

        return entity;
    }

    public static Entity romanceBook(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Ghastly Romance"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("loveBook"),
                atlas.findRegion("loveBook2")
        }, Animation.PlayMode.LOOP, 0.4f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(3, 7, 1, 0, 2));
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.monopierce(entity),
                MoveConstructor.enchant(entity),
                MoveConstructor.ward(entity),
                MoveConstructor.fullRestore(entity)
        })));

        return entity;
    }

    public static Entity fancyBook(int team) {
        Entity entity = new Entity();
        if (team > -1)
            entity.add(new TeamComponent(team));
        entity.add(new NameComponent("Haunted Novel"));

        entity.add(new ActorComponent(new AnimationActor(new TextureRegion[]{
                atlas.findRegion("fancyBook"),
                atlas.findRegion("fancyBook2")
        }, Animation.PlayMode.LOOP, 0.4f)));
        entity.add(new BoardComponent());
        entity.add(new StatComponent(6, 12, 3, 0, 2));
        stm.get(entity).obscureStatInfo = true;
        entity.add(new StatusEffectComponent());
        entity.add(new StateComponent());
        state.get(entity).canAttack = true;
        state.get(entity).canMove = true;
        entity.add(new VisualsComponent(
                MoveConstructor.damageAnimation(entity),
                MoveConstructor.heavyDamageAnimation(entity),
                MoveConstructor.deathAnimation(entity),
                MoveConstructor.shuffleAnimation(entity)));
        entity.add(new MovesetComponent(new Array<Move>(new Move[]{
                MoveConstructor.monoplode3(entity),
                MoveConstructor.enchant(entity),
                MoveConstructor.transfer(entity),
                MoveConstructor.combubulate(entity)
        })));

        return entity;
    }
    //endregion

    //region other
    //endregion
}
