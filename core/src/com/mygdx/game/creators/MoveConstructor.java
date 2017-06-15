package com.mygdx.game.creators;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.*;
import com.mygdx.game.misc.GameEvent;
import com.mygdx.game.move_related.*;
import com.mygdx.game.screens_ui.LerpColor;
import com.mygdx.game.screens_ui.screens.BattleScreen;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.atlas;

/**
 * Class that creates various moves to be used by Entities on the board. Also contains methods for certain
 * animations ({@link Visuals} for dying and taking damage) and {@link StatusEffect}s.
 * @author Phillip O'Reggio
 */
public class MoveConstructor {
    private static boolean ready;

    private static float scale;
    private static BoardManager boards;
    private static Engine engine;
    private static Stage stage;

    /**
     * Readies the {@link MoveConstructor} for use.
     * @param scaleFactor scale of board
     * @param manager BoardManager
     * @param eng Engine
     * @param stge Stage
     */
    public static void initialize(float scaleFactor, BoardManager manager, Engine eng, Stage stge) {
        scale = scaleFactor;
        boards = manager;
        engine = eng;
        stage = stge;
        ready = true;
    }

    /**
     * Clears static fields in {@link MoveConstructor}
     */
    public static void clear() {
        scale = 1;
        boards = null;
        engine = null;
        stage = null;
        ready = false;
    }
    
    public static boolean isReady() {
        return ready;
    }


    //region Damage related animations
    /**
     * Creates a generic damage animation
     * @param user Entity that is being damaged
     * @return damage animation {@code Visuals}
     */
    public static Visuals damageAnimation(Entity user) {

        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(new Color(.9f, .1f, .1f, 1));
            }
        }, .001f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        return new Visuals(user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed, returnToNormalGradual.copy(.15f, 1), returnToNormalGradual, returnToNormal}));
    }

    /**
     * Creates a generic damage animation
     * @param user Entity that is being damaged
     * @return damage animation {@code Visuals}
     */
    public static Visuals heavyDamageAnimation(Entity user) {
        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                    am.get(user).actor.shade(new Color(.8f, 0, 0, 1));
            }
        }, .001f, 1);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.moveBy(3, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.moveBy(-3, 0);
            }
        }, .05f, 2);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .02f, 9);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .02f, 1);

        return new Visuals(user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed,
                        moveRight.copy(.001f, 1), moveLeft, moveRight, moveLeft.copy(), moveRight.copy(1),
                        returnToNormalGradual, returnToNormal
                }));
    }

    /**
     * Creates the generic death animation
     * @param user Entity that is being killed
     * @return death animation {@code Visuals}
     */
    public static Visuals deathAnimation(Entity user) {
        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(Color.RED);
            }
        }, .001f, 1);

        VisualEvent fadeAndBlacken = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(
                        new Color(am.get(user).actor.getColor().r - .01f, am.get(user).actor.getColor().g - .1f,
                                am.get(user).actor.getColor().b - .1f, am.get(user).actor.getColor().a - .1f));
            }
        }, .1f, 9);

        return new Visuals(user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed, fadeAndBlacken.copy(.275f, 1), fadeAndBlacken}));
    }
    //endregion

    //region Status Effects
    public static StatusEffect poison() {
        return new StatusEffect("Poison", 3, new LerpColor(Color.GREEN, new Color(201f / 255f, 1f, 0f, 1f)), (e) -> {
            stm.get(e).hp -= 1;
            if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                vm.get(e).heavyDamageAnimation.setPlaying(true, true);
        });
    }

    public static StatusEffect burn() {
        StatusEffect effect = new StatusEffect("Burn", 3, new LerpColor(Color.RED, new Color(1, 125f / 255f, 0f, 1f), .3f, Interpolation.sineOut), (e) -> {
            if (stm.has(e) && MathUtils.randomBoolean()) {
                stm.get(e).hp -= 1;
                if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                    vm.get(e).heavyDamageAnimation.setPlaying(true, true);
            }
        });
        effect.setStatChanges(1, 1, 1, .5f, 1, 1);
        return effect;
    }

    public static StatusEffect paralyze() {
        StatusEffect effect = new StatusEffect("Paralyze", 3, new LerpColor(Color.RED, new LerpColor(Color.GRAY, Color.YELLOW, .4f, Interpolation.exp5In)), (e) -> {/*nothing*/});
        effect.setStatChanges(1, 1, 1, 1, 1, .5f);
        return effect;
    }

    public static StatusEffect petrify() {
        StatusEffect effect = new StatusEffect("Petrify", 2, new Color(214f / 255f, 82f / 255f, 0, 1), (e) -> {/*nothing*/});
        effect.setStatChanges(1, 1, 1, 2, 1, 0);
        return effect;
    }

    public static StatusEffect stillness() {
        return new StatusEffect("Stillness", 2, new LerpColor(Color.WHITE, new Color(0, 140f / 255f, 1f, 1f), .7f,  Interpolation.sine), (e) -> {/*nothing*/});
    }

    public static StatusEffect curse() {
        StatusEffect effect = new StatusEffect("Curse", 2, new LerpColor(Color.GRAY, Color.BLACK, .5f, Interpolation.fade), (e) -> {/*nothing*/});
        effect.setStatChanges(1, 1, 1, .5f, .5f, .5f);
        return effect;
    }

    public static StatusEffect defenseless() {
        StatusEffect effect =  new StatusEffect("Defenseless", 1, new LerpColor(Color.WHITE, Color.NAVY, .5f, Interpolation.fade), (e) -> {/*nothing*/});
        effect.setStatChanges(1, 1, 1, 1, 0, 1);
        return effect;
    }

    public static StatusEffect offenseless() {
        StatusEffect effect = new StatusEffect("Offenseless", 2, new LerpColor(Color.WHITE, Color.RED, .5f, Interpolation.fade), (e) -> {/*nothing*/});
        effect.setStatChanges(1, 1, 1, .5f, 1, 1);
        return effect;
    }
    //endregion

    //region Moves

    /*
    Note : the line below
        Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - (half sprite width), t.getHeight() / 2 - (half sprite height)));
    Gets the position of the center of the tile, then subtracts half the size of the sprite being drawn from it.
     */

    public static Move Tackle(Entity user) {
        VisualEvent TackleVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity star = new Entity();
                star.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 70) - 35, (float) (Math.random() * 70) - 35)
                        , entitySize.x, entitySize.y, (float) (Math.random() * 360)));
                star.add(new LifetimeComponent(0, .6f));
                star.add(new AnimationComponent(.2f, new TextureRegion[]{atlas.findRegion("boom"),
                        atlas.findRegion("cloud")}, Animation.PlayMode.LOOP));
                engine.addEntity(star);
            }
        }, .2f, 4);

        return new Move("Tackle", null, user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                           stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{TackleVis.copy(0.1f, 1), TackleVis})));
    }

    public static Move StarSpin(Entity user) {
        VisualEvent spin = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2, BoardComponent.boards.getTileHeight() / 2);
                Entity star = new Entity();
                star.add(new PositionComponent(new Vector2(tilePosition.cpy().x,
                        tilePosition.cpy().y),
                        45 * scale,
                        45 * scale,
                        0));
                star.add(new LifetimeComponent(0, 1.2f));
                star.add(new AnimationComponent(.3f, new TextureRegion[]{atlas.findRegion("boom"),
                        atlas.findRegion("cloud")}, Animation.PlayMode.LOOP));
                star.add(new EventComponent(.1f, 0f, true, true, new GameEvent() {
                    @Override
                    public void event(Entity e, Engine engine) {
                        pm.get(e).rotation += 40;
                    }
                }));
                engine.addEntity(star);
            }
        }, 0f, 1);

        return new Move("Star Spin", "Something spun around!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{spin})));
    }

    //Vulpedge
    public static Move swordSlice(Entity user) {
        VisualEvent sliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                Vector2 entitySize = new Vector2(45 * scale, 45 * scale);

                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.x / 2f);
                Entity slash = new Entity();
                slash.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                slash.add(new LifetimeComponent(0, .21f));
                slash.add(new AnimationComponent(.05f,new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        Animation.PlayMode.NORMAL));
                engine.addEntity(slash);
            }
        }, .21f, 1);

        VisualEvent crossSliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(45 * scale, 45 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));
                tilePosition.x += t.getWidth() / 4; //Move rotated sprite to be aligned

                Entity crossSlash = new Entity();
                crossSlash.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 90));
                crossSlash.add(new LifetimeComponent(0, .21f));
                crossSlash.add(new AnimationComponent(.05f,new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        Animation.PlayMode.LOOP));
                engine.addEntity(crossSlash);
            }
        }, .21f, 1);

        return new Move("Slice", nm.get(user).name + " sliced its blade!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{sliceVis, crossSliceVis})));
    }

    public static Move pierceSwordSlice(Entity user) {
        //Visuals---
        VisualEvent glow = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {

                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity glow = new Entity();
                glow.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 0));
                glow.add(new LifetimeComponent(0, .5f));
                Sprite glowSprite = atlas.createSprite("circle");
                glowSprite.setColor(new Color(1, 0, 0, 0));
                glow.add(new SpriteComponent(glowSprite));
                glow.add(new EventComponent(.1f, true, EventCompUtil.fadeIn(6)));
                engine.addEntity(glow);
            }

        }, .6f, 1);

        VisualEvent sliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity slash = new Entity();
                slash.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 0));
                slash.add(new LifetimeComponent(0, .49f));
                slash.add(new AnimationComponent(.1f,new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        Color.RED,
                        Animation.PlayMode.NORMAL));
                engine.addEntity(slash);
            }
        }, .01f, 1);

        VisualEvent crossSliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);

                tileCenter.x += t.getWidth() / 4; //Move rotated sprite to be aligned
                Entity crossSlash = new Entity();
                crossSlash.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 90));
                crossSlash.add(new LifetimeComponent(0, .49f));
                crossSlash.add(new AnimationComponent(.1f, new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        Color.RED,
                        Animation.PlayMode.LOOP));
                engine.addEntity(crossSlash);
            }
        }, .5f, 1);

        //Move
        return new Move("Piercing Slice", nm.get(user).name + " delivered a piercing blow!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 1.5, 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{glow ,sliceVis, crossSliceVis})));
    }

    public static Move guardPiercer(Entity user) {
        //Visuals---
        VisualEvent circles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(70 * scale, 70 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 70) - 35, (float) (Math.random() * 70) - 35),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(2, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                System.out.println(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .5f));

                Sprite glowSprite = atlas.createSprite("circle");
                glowSprite.setColor(new Color(.3f, .3f, 1, 0f));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.1f, true, EventCompUtil.fadeIn(6)));

                engine.addEntity(glow);
            }

        }, .5f, 1);

        VisualEvent sliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity slash = new Entity();
                slash.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 0));
                slash.add(new LifetimeComponent(0, .49f));
                slash.add(new AnimationComponent(.1f,new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        Color.BLUE,
                        Animation.PlayMode.NORMAL));
                engine.addEntity(slash);
            }
        }, .01f, 1);

        VisualEvent crossSliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);
                tileCenter.x += t.getWidth() / 4; //Move rotated sprite to be aligned

                Entity crossSlash = new Entity();
                crossSlash.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 90));
                crossSlash.add(new LifetimeComponent(0, .49f));
                crossSlash.add(new AnimationComponent(.1f, new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        Color.BLUE,
                        Animation.PlayMode.LOOP));
                engine.addEntity(crossSlash);
            }
        }, .5f, 1);

        //Move
        return new Move("Breaking Slice", nm.get(user).name + " delivered a crippling blow!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles.copy(.1f, 5), circles ,sliceVis, crossSliceVis})));
    }

    public static Move poisonBlade(Entity user) {
        //Visuals---
        VisualEvent circles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(70 * scale, 70 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * entitySize.x) - entitySize.x, (float) (Math.random() * entitySize.y) - entitySize.y),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(1f, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                System.out.println(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .5f));

                Sprite glowSprite = atlas.createSprite("circle");
                glowSprite.setColor(Color.GREEN);
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(5)));

                engine.addEntity(glow);
            }

        }, .55f, 1);

        VisualEvent sliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }

                Vector2 entitySize = new Vector2(70 * scale, 70 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2f - (entitySize.x / 2f),
                        BoardComponent.boards.getTileHeight() / 2f - (entitySize.y / 2f));
                Entity slash = new Entity();
                slash.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                slash.add(new LifetimeComponent(0, .21f));
                slash.add(new AnimationComponent(.05f,new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        new Color(202f / 255f, 1, 0, 1),
                        Animation.PlayMode.NORMAL));
                engine.addEntity(slash);
            }
        }, .21f, 1);

        VisualEvent crossSliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - 22.5f, t.getHeight() / 2 - 22.5f));
                tilePosition.x += t.getWidth() / 4; //Move rotated sprite to be aligned
                Entity crossSlash = new Entity();
                crossSlash.add(new PositionComponent(tilePosition, 45 * scale, 45 * scale, 90));
                crossSlash.add(new LifetimeComponent(0, .21f));
                crossSlash.add(new AnimationComponent(.05f, new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        new Color(202f / 255f, 1, 0, 1),
                        Animation.PlayMode.LOOP));
                engine.addEntity(crossSlash);
            }
        }, .21f, 1);

        //Move
        return new Move("Poison Blade", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(e), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(poison(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles.copy(.1f, 5), circles, sliceVis, crossSliceVis})));
    }

    //Canight
    public static Move chargedSlice(Entity user) {
        VisualEvent curveSliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);

                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.x / 2f);
                Entity slash = new Entity();
                slash.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                slash.add(new LifetimeComponent(0, .11f));
                slash.add(new AnimationComponent(.025f,new TextureRegion[] {
                        atlas.findRegion("blueslash1"),
                        atlas.findRegion("blueslash2"),
                        atlas.findRegion("blueslash3"),
                        atlas.findRegion("blueslash4")},
                        Animation.PlayMode.NORMAL));
                engine.addEntity(slash);
            }
        }, .11f, 1);

        VisualEvent sliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);

                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.x / 2f);
                Entity slash = new Entity();
                slash.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                slash.add(new LifetimeComponent(0, .11f));
                slash.add(new AnimationComponent(.025f,new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        new Color(.97f, .97f, 1, 1),
                        Animation.PlayMode.NORMAL));
                engine.addEntity(slash);
            }
        }, .11f, 1);

        VisualEvent crossSliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(45 * scale, 45 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 - entitySize.x / 2f));

                tilePosition.x += t.getWidth() / 4; //Move rotated sprite to be aligned
                Entity crossSlash = new Entity();
                crossSlash.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 90));
                crossSlash.add(new LifetimeComponent(0, .11f));
                crossSlash.add(new AnimationComponent(.025f,new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        new Color(.97f, .97f, 1, 1),
                        Animation.PlayMode.LOOP));
                engine.addEntity(crossSlash);
            }
        }, .11f, 1);

        return new Move("Blade Flurry", nm.get(user).name + " let loose with a flurry of attacks!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 2 - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{curveSliceVis, crossSliceVis.copy(), sliceVis, crossSliceVis.copy(), curveSliceVis.copy(), sliceVis.copy()})));
    }

    public static Move Bark(Entity user) {
        //Visuals---
        VisualEvent bark = new VisualEvent(new VisualEffect() {
            BoardPosition bp;
            Tile t;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                System.out.println("Scale: " + scale);
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity zigzag = new Entity();
                Sprite zig = new Sprite(atlas.findRegion("zigzag"));
                zig.setColor(Color.ORANGE);
                zigzag.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 0));
                zigzag.add(new LifetimeComponent(0, .49f));
                zigzag.add(new SpriteComponent(zig));
                zigzag.add(new EventComponent(.1f, true, (Entity e, Engine eng) -> {
                    pm.get(e).position.add(new Vector2(MathUtils.random(-5 * scale, 5 * scale), MathUtils.random(-5 * scale, 5 * scale)));
                }));
                engine.addEntity(zigzag);
            }
        }, .01f, 1);

        VisualEvent bark2 = new VisualEvent(new VisualEffect() {
            BoardPosition bp;
            Tile t;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);
                tileCenter.x += t.getWidth() / 4; //Move rotated sprite to be aligned

                Entity zigzag = new Entity();
                Sprite zig = new Sprite(atlas.findRegion("zigzag"));
                zig.setColor(Color.BLUE);
                zigzag.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 90));
                zigzag.add(new LifetimeComponent(0, .49f));
                zigzag.add(new SpriteComponent(zig));
                zigzag.add(new EventComponent(.1f, true, (Entity e, Engine eng) -> {
                    pm.get(e).position.add(new Vector2(MathUtils.random(-5 * scale, 5 * scale), MathUtils.random(-5 * scale, 5 * scale)));
                }));
                engine.addEntity(zigzag);
            }
        }, .5f, 1);

        //Move
        return new Move("Bark", nm.get(user).name + " barked intimidatingly!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(offenseless(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);

                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bark.copy(.1f), bark2.copy(.1f), bark.copy(.1f), bark2.copy(.1f) ,bark, bark2})));
    }

    //Catdroid
    public static Move metalClaw(Entity user) {
        VisualEvent claw = new VisualEvent(new VisualEffect() {
            BoardPosition bp;
            Tile t;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                 bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                Vector2 entitySize = new Vector2(64 * scale, 64 * scale);

                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.x / 2f);

                Entity claw = new Entity();
                claw.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                claw.add(new LifetimeComponent(0, .21f));
                claw.add(new AnimationComponent(.05f,new TextureRegion[] {
                        atlas.findRegion("claw"),
                        atlas.findRegion("claw2"),
                        atlas.findRegion("claw3"),
                        atlas.findRegion("claw4"),
                        atlas.findRegion("claw5")},
                        Animation.PlayMode.NORMAL));
                engine.addEntity(claw);
            }
        }, .21f, 1);

        return new Move("Metal Claw", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{claw})));
    }

    //endregion


    /**
     * Class containing convenience methods for the {@link GameEvent} in {@link EventComponent}.
     *
     * @author Phillip O'Reggio
     */
    private static class EventCompUtil {
        /**
         * @return {@link GameEvent} that will become more transparent.
         * @param amount of times this needs to be called until it is fully transparent. (note: will not work as well
         *               colors that have a transparency of <1
         */
        public static GameEvent fadeOut(int amount) {
            return (entity, engine) -> {
                if (sm.has(entity)) { //sprite
                  Sprite sprite = sm.get(entity).sprite;
                  sprite.setColor(
                          sprite.getColor().r,
                          sprite.getColor().g,
                          sprite.getColor().b,
                          MathUtils.clamp(sprite.getColor().a - 1f / amount, 0, 1));
                } else { //animation
                    Color color = animm.get(entity).shadeColor;
                    color = new Color(
                            color.r,
                            color.g,
                            color.b,
                            MathUtils.clamp(color.a - 1f / amount, 0, 1));
                }
            };
        }


        /**
         * @return {@link GameEvent} that will become more opaque.
         * @param amount of times this needs to be called until it is fully opaque. (note: will not work as well
         *               colors that have a transparency of <1
         */
        public static GameEvent fadeIn(int amount) {
            return (entity, engine) -> {
                if (sm.has(entity)) { //sprite
                    Sprite sprite = sm.get(entity).sprite;
                    sprite.setColor(
                            sprite.getColor().r,
                            sprite.getColor().g,
                            sprite.getColor().b,
                            MathUtils.clamp(sprite.getColor().a + 1f / amount, 0, 1));
                } else { //animation
                    Color color = animm.get(entity).shadeColor;
                    color = new Color(
                            color.r,
                            color.g,
                            color.b,
                            MathUtils.clamp(color.a + 1f / amount, 0, 1));
                }
            };
        }



    }
}
