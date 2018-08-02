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
import com.mygdx.game.AI.MoveInfo;
import com.mygdx.game.AI.StatusEffectInfo;
import com.mygdx.game.GridWars;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.*;
import com.mygdx.game.misc.ColorUtils;
import com.mygdx.game.misc.EventCompUtil;
import com.mygdx.game.misc.GameEvent;
import com.mygdx.game.move_related.*;
import com.mygdx.game.screens.BattleScreen;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.atlas;
import static com.mygdx.game.creators.StatusEffectConstructor.*;

/**
 * Class that creates various {@link Move}s to be used by Entities on the board.
 *
 * @author Phillip O'Reggio
 */
public class MoveConstructor {
    private static boolean ready;

    private static float scale;
    private static GridWars game;
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
    public static void initialize(float scaleFactor, BoardManager manager, Engine eng, Stage stge, GridWars gm) {
        scale = scaleFactor;
        boards = manager;
        engine = eng;
        stage = stge;
        game = gm;
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
        game = null;
        ready = false;
    }
    
    public static boolean isReady() {
        return ready;
    }

    //region Moves

    /*
    Note : the line below
        Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - (half sprite width), t.getHeight() / 2 - (half sprite height)));
    Gets the position of the center of the tile, then subtracts half the size of the sprite being drawn from it.
     */
    //TODO check all moves used out of bounds (on invisible tiles and actually out of bounds). fix ones that crash

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

        Move move = new Move("Tackle", null, user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{TackleVis.copy(0.1f, 1), TackleVis})), new MoveInfo(false, 1));
        
        move.setAttackDescription("Rams into the opponent in a comical way. DEBUG (this is the very first attack created)");
        return move;
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

        Move move = new Move("Star Spin", "Something spun around!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
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
                new Array<VisualEvent>(new VisualEvent[]{spin})), new MoveInfo(false, 5));
        move.setAttackDescription("Attacks the opponent with a spinning star several times in rapid succession. Deals half the user's attack power 5 times. DEBUG");
        return move;
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
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);
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

        Move move = new Move("Slice", nm.get(user).name + " sliced its blade!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{sliceVis, crossSliceVis})), new MoveInfo(false, 1));
        move.setAttackDescription("Slices the opponent with a sharp edge. Deals regular damage.");
        return move;
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
                slash.add(new LifetimeComponent(0, .21f));
                slash.add(new AnimationComponent(.062f,new TextureRegion[] {
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

                Entity crossSlash = new Entity();
                crossSlash.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 90));
                crossSlash.add(new LifetimeComponent(0, .21f));
                crossSlash.add(new AnimationComponent(.062f, new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        Color.RED,
                        Animation.PlayMode.LOOP));
                engine.addEntity(crossSlash);
            }
        }, .2f, 1);

        //Move
        Move move = new Move("Piercing Slice", nm.get(user).name + " delivered a piercing blow!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{glow ,sliceVis, crossSliceVis})), new MoveInfo(true, 1.5f));
        move.setAttackDescription("Pierces the opponent's defenses with a sharp edge. Ignores the opponent's defense and inflicts 1.5x damage.");
        return move;
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
                Vector2 movementToCenter = new Vector2(50, 0);
                movementToCenter.setAngle(directionTowardsCenter);
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
                slash.add(new LifetimeComponent(0, .21f));
                slash.add(new AnimationComponent(.062f,new TextureRegion[] {
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

                Entity crossSlash = new Entity();
                crossSlash.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 90));
                crossSlash.add(new LifetimeComponent(0, .21f));
                crossSlash.add(new AnimationComponent(.063f, new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        Color.BLUE,
                        Animation.PlayMode.NORMAL));
                engine.addEntity(crossSlash);
            }
        }, .2f, 1);

        //Move
        Move move = new Move("Breaking Slice", nm.get(user).name + " delivered a crippling blow!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless(1), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles.copy(.1f, 5), circles ,sliceVis, crossSliceVis})), new MoveInfo(true, 1, defenseless(1).createStatusEffectInfo()));
        move.setAttackDescription("Slices the opponent while exposing their weak points. Deals piercing damage and makes the target's defense 0 for 1 turn.");
        return move;
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
                Vector2 movementToCenter = new Vector2(50f, 0);
                movementToCenter.setAngle(directionTowardsCenter);
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
        Move move = new Move("Poison Blade", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(e), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(poison(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles.copy(.1f, 5), circles, sliceVis, crossSliceVis})), new MoveInfo(false, 1, poison(2).createStatusEffectInfo()));
        move.setAttackDescription("Slices the opponent with a poison-tipped edge. Deals regular damage and inflicts the target with Poison for 2 turns.");
        return move;
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
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);
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
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);
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
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 - entitySize.y / 2f));

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

        Move move = new Move("Blade Flurry", nm.get(user).name + " let loose with a flurry of attacks!", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 3 - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{curveSliceVis, crossSliceVis.copy(), sliceVis, crossSliceVis.copy(), curveSliceVis.copy(), sliceVis.copy()})),
                new MoveInfo(false, 3));
        move.setAttackDescription("Attacks the opponent with a flurry of blows. Deals 3x damage.");
        return move;
    }

    public static Move bark(Entity user) {
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
                Vector2 entitySize = new Vector2(60 * scale, 60 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity zigzag = new Entity();
                Sprite zig = new Sprite(atlas.findRegion("zigzag"));
                zig.setColor(Color.RED);
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

                Entity zigzag = new Entity();
                Sprite zig = new Sprite(atlas.findRegion("zigzag"));
                zig.setColor(Color.GREEN);
                zig.setOrigin(30 * scale, 30 * scale);
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
        Move move = new Move("Bark", nm.get(user).name + " barked intimidatingly!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(offenseless(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);

                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bark.copy(.1f), bark2.copy(.1f), bark.copy(.1f), bark2.copy(.1f) ,bark, bark2})), new MoveInfo(false, 0, offenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Barks intimidatingly at the opponent. Halves the attack of the target for 2 turns.");
        return move;
    }

    public static Move yelp(Entity user) {
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

                Entity zigzag = new Entity();
                Sprite zig = new Sprite(atlas.findRegion("zigzag"));
                zig.setColor(Color.BLUE);
                zig.setOrigin(30 * scale, 30 * scale);
                zigzag.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 0));
                zigzag.add(new LifetimeComponent(0, .49f));
                zigzag.add(new SpriteComponent(zig));
                zigzag.add(new EventComponent(.1f, true, (Entity e, Engine eng) -> {
                    pm.get(e).position.add(new Vector2(MathUtils.random(-5 * scale, 5 * scale), MathUtils.random(-5 * scale, 5 * scale)));
                }));
                engine.addEntity(zigzag);
            }
        }, .5f, 1);

        //Move
        Move move = new Move("Yelp", nm.get(user).name + " yelped cutely!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);

                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bark.copy(.1f), bark2.copy(.1f), bark.copy(.1f), bark2.copy(.1f) ,bark, bark2})), new MoveInfo(false, 0, defenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Yelps in order to lower the opponents guard. Brings the target's defense to 0 for 2 turns.");
        return move;
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
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

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

        Move move = new Move("Metal Claw", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{claw})), new MoveInfo(false, 1));
        move.setAttackDescription("Claws at the opponent with metallic claws or talons. Deals regular damage.");
        return move;
    }

    public static Move laserBeam(Entity user) {
        VisualEvent laser = new VisualEvent(new VisualEffect() {
            Tile startTile;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(57 * scale, 17 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                if (direction == 90)
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else if (direction == 180)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.y / 2f,
                            boards.getTileHeight() / 2);
                else if (direction == 270)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y * 1.5f);

                Entity beam = new Entity();
                Sprite beamSprite = new Sprite(atlas.findRegion("beam"));
                beamSprite.setOriginCenter();
                beamSprite.setColor(Color.RED);
                beam.add(new SpriteComponent(beamSprite));
                beam.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90));
                beam.add(new MovementComponent(new Vector2(1300 * scale, 0).setAngle(direction)));
                beam.add(new LifetimeComponent(0, .26f));
                beam.add(new EventComponent(0.02f, true, EventCompUtil.fadeOutAfter(7, 6)));
                engine.addEntity(beam);
            }
        }, .03f, 12);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("explode"),
                                    atlas.findRegion("explode2"),
                                    atlas.findRegion("explode3"),
                                    atlas.findRegion("explode4"),
                                    atlas.findRegion("explode5"),
                                    atlas.findRegion("explode6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 1);

        Move move = new Move("Laser Beam", nm.get(user).name + " shot a laser beam!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{laser, explode})), new MoveInfo(false, 1));
        move.setAttackDescription("Fires a laser beam that attacks all in its way. Deals regular damage.");
        return move;
    }

    public static Move electricalFire(Entity user) {
        VisualEvent explosions = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(90 * scale, 90 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                        atlas.findRegion("explode2"),
                        atlas.findRegion("explode3"),
                        atlas.findRegion("explode4"),
                        atlas.findRegion("explode5"),
                        atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .1f, 6);

        VisualEvent fire = new VisualEvent(new VisualEffect() {
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

                Entity flame = new Entity();
                flame.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 0));
                flame.add(new LifetimeComponent(0, 1.2f));
                flame.add(new AnimationComponent(.05f,
                    new TextureRegion[]{atlas.findRegion("flame"),
                            atlas.findRegion("flame2"),
                            atlas.findRegion("flame3")},
                    Animation.PlayMode.LOOP));
                flame.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(6, 6)));

                engine.addEntity(flame);
            }
        }, .1f, 1);

        Move move = new Move("Electrical Fire", null, user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);
                        if (status.has(enemy)) {
                            if (MathUtils.randomBoolean(.6f)) {
                                status.get(enemy).addStatusEffect(burn(MathUtils.random(2, 4)), enemy);
                            }
                            if (MathUtils.randomBoolean(.1f)) {
                                status.get(enemy).addStatusEffect(paralyze(1), enemy);
                            }
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explosions, fire})), new MoveInfo(false, 1, (entity, userEntity) ->
                {
                    if (entity.acceptsStatusEffects) { // add only guaranteed boost
                        if (MathUtils.randomBoolean(.6f)) {
                            entity.statusEffectInfos.add(burn(MathUtils.random(2, 4)).createStatusEffectInfo());
                        }
                        if (MathUtils.randomBoolean(.1f)) {
                            entity.statusEffectInfos.add(paralyze(1).createStatusEffectInfo());                        }
                    }
                }));
        move.setAttackDescription("Starts a fire using live wires and electricity. Deals regular damage, and has a 60% chance to burn the opponent for 2-4 turns. Also" +
                " has a 10% chance to paralyze the opponent for 1 turn.");
        return move;
    }

    public static Move laserSpray(Entity user) {
        VisualEvent laser = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset = -45;
            boolean sprayingBack;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                if (offset >= 45)
                    sprayingBack = true;
                if (!sprayingBack)
                    offset += 7.5f;
                else
                    offset -= 7.5;
                if (offset < -45) { //reset
                    offset = -45;
                    sprayingBack = false;
                }

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(57 * scale, 17 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                if (direction == 90)
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else if (direction == 180)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.y / 2f,
                            boards.getTileHeight() / 2);
                else if (direction == 270)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y * 1.5f);

                Entity beam = new Entity();
                Sprite beamSprite = new Sprite(atlas.findRegion("beam"));
                beamSprite.setOriginCenter();
                beamSprite.setColor(Color.RED);
                beam.add(new SpriteComponent(beamSprite));
                beam.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                beam.add(new MovementComponent(new Vector2(1300 * scale, 0).setAngle(direction + offset)));
                beam.add(new LifetimeComponent(0, .26f));
                beam.add(new EventComponent(0.02f, true, EventCompUtil.fadeOutAfter(7, 6)));
                engine.addEntity(beam);
            }
        }, .02f, 28);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("explode"),
                                    atlas.findRegion("explode2"),
                                    atlas.findRegion("explode3"),
                                    atlas.findRegion("explode4"),
                                    atlas.findRegion("explode5"),
                                    atlas.findRegion("explode6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 1);

        Move move = new Move("Laser Spread", nm.get(user).name + " sprayed laser beams!", user, 4,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                        new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)}),
                new Array<VisualEvent>(new VisualEvent[]{laser, explode})), new MoveInfo(false, 1));
        move.setAttackDescription("Fires a laser that sweeps across the battlefield. Has a large, wide range. Deals regular damage.");
        return move;
    }

    //Pyrobull
    public static Move bodySlam(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent bam = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("boom"));
                bamSprite.setOriginCenter();
                bamSprite.setColor(Color.RED);
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .2f, 1);

        Move move = new Move("Body Slam", nm.get(user).name + " charged forward!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{explode, bam})), new MoveInfo(false, 1));
        move.setAttackDescription("Slams into the opponent with full force. Deals regular damage.");
        return move;
    }

    public static Move sear(Entity user) {
        VisualEvent fire = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity flame = new Entity();
                flame.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-20 * scale, 20 * scale), MathUtils.random(-20 * scale, 20 * scale)),
                        entitySize.x, entitySize.y, 0));
                flame.add(new LifetimeComponent(0, 1.2f));
                flame.add(new AnimationComponent(.05f, new TextureRegion[]{
                        atlas.findRegion("flame"),
                        atlas.findRegion("flame2"),
                        atlas.findRegion("flame3")},
                        Animation.PlayMode.LOOP));
                flame.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(6, 6)));

                engine.addEntity(flame);
            }
        }, .3f, 8);

        Move move = new Move("Sear", null, user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(burn(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{fire})), new MoveInfo(false, 0, burn(3).createStatusEffectInfo()));
        move.setAttackDescription("Creates intensely hot flames. Burns the opponent for 3 turns.");
        return move;
    }

    public static Move wildFire(Entity user) {
        VisualEvent explosions = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy(); //centered around self
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-140 * scale, 140 * scale), MathUtils.random(-140 * scale, 140 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .03f, 30);

        Move move = new Move("Wild Fire", user, 4, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, -1), new BoardPosition(-1, 0),
                new BoardPosition(-1, 1), new BoardPosition(0, 1),
                new BoardPosition(1, 1), new BoardPosition(0, -1),
                new BoardPosition(1, 0), new BoardPosition(1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) / 2 - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.7f))
                            status.get(enemy).addStatusEffect(burn(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{}),
                new Array<VisualEvent>(new VisualEvent[]{explosions})),
                new MoveInfo(false, .5f, (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects && MathUtils.randomBoolean(.7f))
                        enemy.statusEffectInfos.add(burn(3).createStatusEffectInfo());
                }));
        move.setAttackDescription("Causes a violent explosion around itself. Deals 1/2x damage. Has a 70% chance to Burn targets for 3 turns.");
        return move;
    }

    //Freezird
    public static Move chill(Entity user) {
        VisualEvent freeze = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity freeze = new Entity();
                freeze.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                freeze.add(new LifetimeComponent(0, .75f));
                freeze.add(new AnimationComponent(.15f,
                        new TextureRegion[]{atlas.findRegion("freeze1"),
                                atlas.findRegion("freeze2"),
                                atlas.findRegion("freeze3")},
                        Animation.PlayMode.LOOP));
                freeze.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(4, 3)));
                engine.addEntity(freeze);
            }
        }, .5f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-40 * scale, 40 * scale), MathUtils.random(-40 * scale, 40 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, -20 * scale)));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("sparkle"));
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(6, 6)));

                engine.addEntity(sparkle);
            }
        }, .1f, 8);

        Move move = new Move("Chill", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(shivers(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{freeze, sparkle})), new MoveInfo(false, 1, shivers(2).createStatusEffectInfo()));
        move.setAttackDescription("Creates a blast of cold air. Deals regular damage and gives the opponent the Shivers for 2 turns.");
        return move;
    }

    public static Move tailwind(Entity user) {
        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, -20 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(20 * scale, 0)));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, (entity, engine) -> {
                    sprite.setColor(
                            sprite.getColor().r,
                            sprite.getColor().g,
                            MathUtils.clamp(sprite.getColor().b + 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().a - 1f / 12, 0, 1));

                    mm.get(entity).movement.add(15 * scale, 0);
                }));

                engine.addEntity(sparkle);
            }
        }, .2f, 2);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-20 * scale, 20 * scale), MathUtils.random(-20 * scale, 20 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, .8f));
                Sprite sprite = new Sprite(atlas.findRegion("sparkle"));
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(8)));

                engine.addEntity(sparkle);
            }
        }, .2f, 2);

        Move move = new Move("Tailwind", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(speedUp(1), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{largeSparkle, sparkle, largeSparkle.copy(), sparkle.copy()})), new MoveInfo(false, 0, speedUp(1).createStatusEffectInfo()));
        move.setAttackDescription("Summons a supportive tailwind to help the target's mobility. Increases the target's speed for 1 turn.");
        return move;
    }

    public static Move twister(Entity user) {
        VisualEvent shuriken = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.first().copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity shuriken = new Entity();
                shuriken.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 1));
                shuriken.add(new LifetimeComponent(0, 1.5f));
                shuriken.add(new AnimationComponent(.1f,
                        new TextureRegion[]{atlas.findRegion("shuriken"),
                                atlas.findRegion("shuriken2")},
                        new Color(.2f, 1f, .5f, 1),
                        Animation.PlayMode.LOOP));
                shuriken.add(new EventComponent(.005f, true, new GameEvent() {
                    @Override
                    public void event(Entity e, Engine engine) {
                        pm.get(e).rotation += 10 + pm.get(e).rotation / 12;
                        if (em.get(e).currentTime >= 1.3f)
                            animm.get(e).shadeColor.a = animm.get(e).shadeColor.a - .01f;
                    }
                }));

                engine.addEntity(shuriken);
            }
        }, .2f, 2);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition position : targetPositions) {
                    BoardPosition bp = position.add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        return;
                    }
                    Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity sparkle = new Entity();
                    sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                            entitySize.x, entitySize.y, 0));
                    sparkle.add(new LifetimeComponent(0, .5f));
                    Sprite sprite = new Sprite(atlas.findRegion("boom"));
                    sprite.setOriginCenter();
                    sprite.setColor(Color.CYAN);
                    sparkle.add(new SpriteComponent(sprite));
                    sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(5)));

                    engine.addEntity(sparkle);
                }
            }
        }, .05f, 15);

        VisualEvent gathering = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        return;
                    }
                    Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                    Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                    Entity glow = new Entity();

                    glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                            entitySize.x, entitySize.y, 0));

                    float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                            tileCenter.y - (pm.get(glow).getCenter().y),
                            tileCenter.x - (pm.get(glow).getCenter().x));
                    Vector2 movementToCenter = new Vector2(50 * scale, 0);
                    movementToCenter.setAngle(directionTowardsCenter);
                    glow.add(new MovementComponent(movementToCenter));
                    glow.add(new LifetimeComponent(0, .3f));
                    Sprite glowSprite = atlas.createSprite("openCircle");
                    if (MathUtils.randomBoolean())
                        glowSprite.setColor(Color.GREEN);
                    else
                        glowSprite.setColor(Color.CYAN);
                    glow.add(new SpriteComponent(glowSprite));
                    glow.add(new EventComponent(.05f, true, (entity, engine) -> {
                        mm.get(entity).movement.scl(1.3f);
                        sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().lerp(1, .2f, .2f, .1f, .1f));
                    }));

                    engine.addEntity(glow);
                }
            }

        }, .04f, 40);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(240 * scale, 240 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .32f));
                boom.add(new AnimationComponent(.06f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .3f, 3);

        VisualEvent flash = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(stage.getWidth(), stage.getHeight());
                Entity flash = new Entity();
                flash.add(new PositionComponent(new Vector2(0, 0), entitySize.x, entitySize.y, 0));
                flash.add(new LifetimeComponent(0, .25f));
                flash.add(new SpriteComponent(atlas.createSprite("LightTile")));
                sm.get(flash).sprite.setColor(new Color(.8f, 1, .9f, 1));
                flash.add(new EventComponent(.025f, true, EventCompUtil.fadeOutAfter(5, 5)));
                engine.addEntity(flash);
            }
        }, .01f, 1);


        Move move = new Move("Twister", user, 3, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-3, 0),
                new BoardPosition(-3, 1), new BoardPosition(-3, -1),
                new BoardPosition(-2, 0), new BoardPosition(-4, -0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-3, 0),
                new BoardPosition(-3, 1), new BoardPosition(-3, -1),
                new BoardPosition(-2, 0), new BoardPosition(-4, -0)}),
                new Array<VisualEvent>(new VisualEvent[]{flash, shuriken, sparkle, explode})), new MoveInfo(false, 1));
        move.setAttackDescription("Summons a vicious twister. Deals regular damage.");
        return move;
    }

    public static Move freezeAttack(Entity user) {
        VisualEvent freeze = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(95 * scale, 95 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity freeze = new Entity();
                freeze.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                freeze.add(new LifetimeComponent(0, 1.5f));
                Sprite iceSprite = new Sprite(atlas.findRegion("ice"));
                iceSprite.setOriginCenter();
                freeze.add(new SpriteComponent(iceSprite));
                freeze.add(new EventComponent(.1f, true, EventCompUtil.fadeInThenOut(3, 5, 7)));

                engine.addEntity(freeze);
            }
        }, .7f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-20 * scale, 20 * scale), MathUtils.random(-20 * scale, 20 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("sparkle"));
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(8, 4)));

                engine.addEntity(sparkle);
            }
        }, .2f, 9);

        Move move = new Move("Freeze", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(freeze(3), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{freeze, sparkle})), new MoveInfo(false, 0, freeze(3).createStatusEffectInfo()));
        move.setAttackDescription("Freezes an opponent in ice, stopping them from moving and lowering their defense. Inflicts Freeze for 3 turns.");
        return move;
    }

    //Medicarp
    public static Move assist(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explodeGreen"),
                                atlas.findRegion("explodeGreen2"),
                                atlas.findRegion("explodeGreen3"),
                                atlas.findRegion("explodeGreen4"),
                                atlas.findRegion("explodeGreen5"),
                                atlas.findRegion("explodeGreen6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.GREEN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 3)));

                engine.addEntity(sparkle);
            }
        }, .19f, 9);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToGreen = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.GREEN, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Assist", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp + 2, 0, stm.get(enemy).getModMaxHp(enemy));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToGreen, sparkle, explode, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    enemy.hp += 2;
                }));
        move.setAttackDescription("Uses supportive powers to heal the target. Always heals the target by 2 points.");
        return move;
    }

    public static Move clear(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.WHITE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 3)));

                engine.addEntity(sparkle);
            }
        }, .19f, 9);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlue = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(new Color(.7f, .7f, 1, 1), progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Clear", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy))
                            status.get(enemy).removeAll(enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlue, sparkle, explode, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {if (enemy.acceptsStatusEffects) enemy.statusEffectInfos.clear();}));
        move.setAttackDescription("Uses mystic powers to remove all status effects from the target. Clears the target of all status effects.");
        return move;
    }

    public static Move recover(Entity user) {

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.GREEN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .19f, 9);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToGreen = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                progress = MathUtils.clamp(progress + .1f, 0, 1);
                am.get(user).actor.shade(am.get(user).actor.getColor().cpy().lerp(Color.GREEN, progress));
            }
        }, .025f, 12);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        Move move = new Move("Recover", nm.get(user).name + " began to recover.", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 3, 0, stm.get(e).getModMaxHp(e));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToGreen, sparkle, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    enemy.hp += 2;
                }));
        move.setAttackDescription("Focuses its energy to recover. Heals 3 points to itself.");
        return move;
    }

    public static Move submerge(Entity user) {

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(50 * scale, 50 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity ripple = new Entity();
                ripple.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                ripple.add(new LifetimeComponent(0, .49f));
                ripple.add(new AnimationComponent(.1f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")
                        },
                        Color.BLUE,
                        Animation.PlayMode.REVERSED));
                ripple.add(new EventComponent(.05f, true, EventCompUtil.fadeOutAfter(18, 6)));

                engine.addEntity(ripple);
            }
        }, .19f, 4);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlue = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLUE, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        VisualEvent waterBall = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(64 * scale, 64 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity waterBall = new Entity();
                waterBall.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                waterBall.add(new LifetimeComponent(0, 2f));
                Sprite water = new Sprite(atlas.findRegion("water"));
                water.setOriginCenter();
                water.setColor(1, 1, 1, 0);
                waterBall.add(new SpriteComponent(water));
                waterBall.add(new EventComponent(.1f, true, EventCompUtil.fadeInThenOut(5, 10, 5)));

                engine.addEntity(waterBall);
            }
        }, .2f, 1);

        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(40 * scale, 0)));
                sparkle.add(new LifetimeComponent(0, 2f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    float prevAngle = mm.get(entity).movement.angle();
                    mm.get(entity).movement.add(new Vector2(mm.get(entity).movement.len() / 6, 0).setAngle(prevAngle + 85));

                    if (sm.has(entity)) { //sprite
                        Sprite spr = sm.get(entity).sprite;
                        spr.setColor(
                                spr.getColor().r,
                                spr.getColor().g,
                                spr.getColor().b,
                                MathUtils.clamp(spr.getColor().a - 1f / 200f, 0, 1));
                    } else { //animation
                        Color color = animm.get(entity).shadeColor;
                        color = new Color(
                                color.r,
                                color.g,
                                color.b,
                                MathUtils.clamp(color.a - 1f / 200f, 0, 1));
                        animm.get(entity).shadeColor = color;

                    }
                }));

                engine.addEntity(sparkle);
            }
        }, .15f, 6);

        Move move = new Move("Submerge", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-3, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp - 3, 0, stm.get(enemy).getModMaxHp(enemy));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-3, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{ripples, waterBall, changeToBlue, largeSparkle, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    enemy.hp -= 3;
                }));
        move.setAttackDescription("Uses mysterious powers to submerge the target in water. This" +
                " always inflicts 3 points of damage regardless of the target's stats or condition.");
        return move;
    }

    //Thoughtoise
    public static Move restMind(Entity user) {

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            private boolean open;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite;
                if (open)
                    sprite = new Sprite(atlas.findRegion("openDiamonds"));
                else
                    sprite = new Sprite(atlas.findRegion("diamonds"));
                open = !open;
                sprite.setOriginCenter();
                sprite.setColor(new Color(1, .3f, 0, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .14f, 9);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(130 * scale, 130 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .26f));
                boom.add(new AnimationComponent(.05f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        new Color(1, .3f, 0, 1),
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeColor = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                progress = MathUtils.clamp(progress + .1f, 0, 1);
                am.get(user).actor.shade(am.get(user).actor.getColor().cpy().lerp(new Color(1, .3f, 0, 1), progress));
            }
        }, .025f, 12);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        Move move = new Move("Rest Mind", nm.get(user).name + " began to rest its mind.", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        stm.get(e).sp = MathUtils.clamp(stm.get(e).sp + 1, 0, stm.get(e).getModMaxSp(e));

                        if (status.has(e)) {
                            status.get(e).addStatusEffect(defenseless(2), e);
                            status.get(e).addStatusEffect(slowness(2), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{sparkle, explode,  returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    userEntity.sp += 1;
                    if (userEntity.acceptsStatusEffects) {
                        userEntity.statusEffectInfos.add(defenseless(2).createStatusEffectInfo());
                        userEntity.statusEffectInfos.add(slowness(2).createStatusEffectInfo());
                    }
                }));
        move.setAttackDescription("Focuses its mind in order to prepare its next move. The user gains one SP point, but lowers the user's " +
        "defense and speed to 0 for 2 turn.");
        return move;
    }

    public static Move drench(Entity user) {
        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("openDiamonds"));
                sprite.setOriginCenter();
                sprite.setColor(Color.BLUE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .19f, 9);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(64 * scale, 64 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bubble.add(new LifetimeComponent(0, 2f));
                Sprite water = new Sprite(atlas.findRegion("bubble"));
                water.setOriginCenter();
                water.setColor(.3f, 1f, 1f, 0);
                bubble.add(new SpriteComponent(water));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeIn(5)));

                engine.addEntity(bubble);
            }
        }, .01f, 1);

        VisualEvent ripple = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .25f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.CYAN,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(600 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, .3f));
                Sprite sprite = new Sprite(atlas.findRegion("diamonds"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(.2f, .9f, .9f, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            spr.getColor().r,
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - 1f / 30f, 0, 1));
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 7);

        Move move = new Move("Drench", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-3, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-3, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sparkle, ripple, largeSparkle})), new MoveInfo(false, 1));
        move.setAttackDescription("Summons a rush of water to drench the opponent. Deals regular damage.");
        return move;
    }

    public static Move electrocute(Entity user) {

        VisualEvent spinning = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity circles = new Entity();
                circles.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                circles.add(new LifetimeComponent(0, 2f));
                circles.add(new AnimationComponent(.6666f,
                        new TextureRegion[]{atlas.findRegion("fourCircles"),
                                atlas.findRegion("sixCircles"),
                                atlas.findRegion("eightCircles")},
                        Color.CYAN,
                        Animation.PlayMode.NORMAL));
                circles.add(new EventComponent(.1f, true, (entity, engine) -> {
                    pm.get(entity).rotation += 10 + pm.get(entity).rotation / 8;
                    Color color = animm.get(entity).shadeColor;
                    color = new Color(
                            MathUtils.clamp(color.r + 1f / 30f, 0, 1),
                            MathUtils.clamp(color.g + 1f / 30f, 0, 1),
                            MathUtils.clamp(color.b - 1f / 30f, 0, 1),
                            color.a);
                    animm.get(entity).shadeColor = color;
                }));

                engine.addEntity(circles);
            }
        }, 2f, 1);

        VisualEvent ripple = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .25f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.CYAN,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .25f, 2);

        VisualEvent shock = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity shock = new Entity();
                shock.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                shock.add(new LifetimeComponent(0, 1f));
                shock.add(new AnimationComponent(.1f,
                        new TextureRegion[]{atlas.findRegion("shock1"),
                                atlas.findRegion("shock2")},
                        Color.YELLOW,
                        Animation.PlayMode.LOOP));

                engine.addEntity(shock);
            }
        }, .01f, 1);

        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(600 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, .3f));
                Sprite sprite = new Sprite(atlas.findRegion("diamonds"));
                sprite.setOriginCenter();
                sprite.setColor(Color.YELLOW);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            spr.getColor().r,
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - 1f / 30f, 0, 1));
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 7);

        Move move = new Move("Electrocute", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-4, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-4, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{spinning, ripple, shock, largeSparkle})), new MoveInfo(false, .5f, paralyze(3).createStatusEffectInfo()));
        move.setAttackDescription("Uses arcane powers to induce a current in the opponent. Deals regular damage and inflicts the" +
        " user with paralysis.");
        return move;
    }

    public static Move cometShower(Entity user) {

        VisualEvent preSparkles = new VisualEvent(new VisualEffect() {
            private boolean open;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 position = new Vector2(MathUtils.random(stage.getWidth() / 6f, stage.getWidth() - stage.getWidth() / 6f), MathUtils.random(stage.getHeight() / 6f, stage.getHeight() - stage.getHeight() / 6f));
                position.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(position, entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, .5f));
                Sprite sprite;
                if (open)
                    sprite = new Sprite(atlas.findRegion("openDiamonds"));
                else
                    sprite = new Sprite(atlas.findRegion("diamonds"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(.4f, .4f, 1, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(5)));

                engine.addEntity(sparkle);
            }
        }, .04f, 5);

        VisualEvent preBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 position = new Vector2(MathUtils.random(stage.getWidth() / 6f, stage.getWidth() - stage.getWidth() / 6f), MathUtils.random(stage.getHeight() / 6f, stage.getHeight() - stage.getHeight() / 6f));
                position.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity diamondBoom = new Entity();
                diamondBoom.add(new PositionComponent(position, entitySize.x, entitySize.y, 0));
                diamondBoom.add(new LifetimeComponent(0, .5f));
                diamondBoom.add(new AnimationComponent(.05f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        new Color(1, .3f, 0, 1),
                        Animation.PlayMode.NORMAL));
                diamondBoom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(5)));

                engine.addEntity(diamondBoom);
            }
        }, .02f, 10);

        VisualEvent comets = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 position = new Vector2(MathUtils.random(0, stage.getWidth()) - stage.getWidth() / 8f, 1000);

                Entity comet = new Entity();
                comet.add(new PositionComponent(position, entitySize.x, entitySize.y, 280));
                comet.add(new MovementComponent(new Vector2(1600, 0).rotate(280)));
                comet.add(new LifetimeComponent(0, 2f));
                comet.add(new AnimationComponent(.06f, new TextureRegion[] {
                        atlas.findRegion("comet"),
                        atlas.findRegion("comet2")},
                        new Color(.4f, .4f, 1, 1),
                        Animation.PlayMode.LOOP
                        ));
                comet.add(new EventComponent(.1f, true, EventCompUtil.fadeIn(5)));

                engine.addEntity(comet);
            }
        }, .05f, 8);

        VisualEvent ripple = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .28f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                    atlas.findRegion("explodeBlue2"),
                                    atlas.findRegion("explodeBlue3"),
                                    atlas.findRegion("explodeBlue4"),
                                    atlas.findRegion("explodeBlue5"),
                                    atlas.findRegion("explodeBlue6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .2f, 2);

        Move move = new Move("Comet Shower", user, 6, new Array<BoardPosition>(new BoardPosition[]{
                    new BoardPosition(-2, 0), new BoardPosition(-1, -1),  new BoardPosition(2, 0),  new BoardPosition(1, 1),
                    new BoardPosition(-2, 1),  new BoardPosition(0, -3),  new BoardPosition(2, -1), new BoardPosition(0, 3),
                    new BoardPosition(-4, 1),  new BoardPosition(1, -4),  new BoardPosition(3, -1), new BoardPosition(1, 3),
                    new BoardPosition(-4, 2), new BoardPosition(4, -2), new BoardPosition(3, -4), new BoardPosition(3, -3)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 2f - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                    new BoardPosition(-2, 0), new BoardPosition(-1, -1),  new BoardPosition(2, 0),  new BoardPosition(1, 1),
                    new BoardPosition(-2, 1),  new BoardPosition(0, -3),  new BoardPosition(2, -1), new BoardPosition(0, 3),
                    new BoardPosition(-4, 1),  new BoardPosition(1, -4),  new BoardPosition(3, -1), new BoardPosition(1, 3),
                    new BoardPosition(-4, 2), new BoardPosition(4, -2), new BoardPosition(3, -4), new BoardPosition(3, -3)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        preSparkles.copy(), preBooms.copy(), preSparkles.copy(), preBooms.copy(),
                        comets.copy(), ripple.copy(), comets.copy(), ripple.copy(), comets.copy(), ripple, comets, preBooms.copy(), preBooms})), new MoveInfo(false, 2f));
        move.setAttackDescription("Uses most of its energy to summon a flurry of comets onto the field. Deals 2x damage.");
        return move;
    }

    //Thundog
    public static Move shockClaw(Entity user) {
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
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity claw = new Entity();
                claw.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                claw.add(new LifetimeComponent(0, .21f));
                claw.add(new AnimationComponent(.05f,new TextureRegion[] {
                        atlas.findRegion("claw"),
                        atlas.findRegion("claw2"),
                        atlas.findRegion("claw3"),
                        atlas.findRegion("claw4"),
                        atlas.findRegion("claw5")},
                        Color.CYAN,
                        Animation.PlayMode.NORMAL));
                engine.addEntity(claw);
            }
        }, .21f, 1);

        VisualEvent shock = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity shock = new Entity();
                shock.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                shock.add(new LifetimeComponent(0, .2f));
                shock.add(new AnimationComponent(.1f,
                        new TextureRegion[]{atlas.findRegion("shock1"),
                                atlas.findRegion("shock2")},
                        Color.YELLOW,
                        Animation.PlayMode.LOOP));

                engine.addEntity(shock);
            }
        }, .01f, 1);


        Move move = new Move("Shock Claw", nm.get(user).name + " scraped the opponent!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy)) {
                            if (!status.get(enemy).contains("Paralyze")) { //Not paralyzed
                                if (MathUtils.randomBoolean(.5f))
                                    status.get(enemy).addStatusEffect(paralyze(3), enemy);
                            } else { //is paralyzed
                                status.get(enemy).removeStatusEffect(enemy, "Paralyze"); //cure their paralysis
                                stm.get(e).sp = MathUtils.clamp(stm.get(e).sp + 2, 0, stm.get(e).getModMaxSp(e)); //recover sp
                            }
                        }

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{shock, claw})), new MoveInfo(false, 1, (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects) {
                        if (enemy.statusEffectInfos.contains(paralyze(3).createStatusEffectInfo(), false)) { //already paralyzed->cure and heal self
                            enemy.statusEffectInfos.removeValue(paralyze(3).createStatusEffectInfo(), false);
                            userEntity.sp+=2;
                        } else { // chance to paralyze
                            if (MathUtils.randomBoolean(.5f))
                                enemy.statusEffectInfos.add(paralyze(3).createStatusEffectInfo());
                        }
                    }
                }));
        move.setAttackDescription("Slashes the target with electrically charged claws. Deals regular damage. Has a 50% chance to paralyze the target for 3 turns. If this move is used" +
        " on a paralyzed target, it will cure their paralysis and increase the user's SP by 2 points.");
        return move;
    }

    public static Move charge(Entity user) {

        VisualEvent charges = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, .2f));
                Sprite sprite = new Sprite(atlas.findRegion("eightCircles"));
                sprite.setOriginCenter();
                sprite.setColor(Color.GREEN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(10)));

                engine.addEntity(sparkle);
            }
        }, .03f, 30);

        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(600 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, .3f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.YELLOW);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            spr.getColor().r,
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - 1f / 30f, 0, 1));
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 6);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                progress = MathUtils.clamp(progress + .1f, 0, 1);
                am.get(user).actor.shade(am.get(user).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .025f, 12);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        Move move = new Move("Charge", nm.get(user).name + " gained electric energy.", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).removeAll(e);
                            status.get(e).addStatusEffect(charged(3), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, charges, largeSparkle, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    userEntity.statusEffectInfos.clear();
                    if (userEntity.acceptsStatusEffects)
                        userEntity.statusEffectInfos.add(charged(3).createStatusEffectInfo());
                }));
        move.setAttackDescription("Gathers electric energy to increase its Max SP, Attack, and Speed for 3 turns. Clears any status effects beforehand.");
        return move;
    }

    public static Move superCharge(Entity user) {

        VisualEvent charges = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, .2f));
                Sprite sprite = new Sprite(atlas.findRegion("eightCircles"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(MathUtils.random(.5f, 1), MathUtils.random(.5f, 1), MathUtils.random(.5f, 1), 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(10)));

                engine.addEntity(sparkle);
            }
        }, .02f, 90);

        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(50 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, 3f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(MathUtils.random(.6f, 1), MathUtils.random(.6f, 1), MathUtils.random(.6f, 1), 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    mm.get(entity).movement.rotate(10);
                    mm.get(entity).movement.add(new Vector2(mm.get(entity).movement.len() / 5f, 0).rotate(mm.get(entity).movement.angle()));
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 6);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                progress = MathUtils.clamp(progress + .1f, 0, 1);
                am.get(user).actor.shade(am.get(user).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .075f, 12);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        Move move = new Move("Supercharge", nm.get(user).name + " gained a large amount of electric energy!", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).removeAll(e);
                            status.get(e).addStatusEffect(supercharged(4), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, charges, largeSparkle, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    userEntity.statusEffectInfos.clear();
                    if (userEntity.acceptsStatusEffects)
                        userEntity.statusEffectInfos.add(supercharged(4).createStatusEffectInfo());
                })
        );
        move.setAttackDescription("Gathers a large amount of electric energy to greatly increase its Max SP, Attack, and Speed for 4 turns. Clears" +
        " any status effect beforehand.");
        return move;
    }

    public static Move voltDeluge(Entity user) {

        VisualEvent ions = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(50 * scale, 50 * scale);
                Vector2 position = new Vector2(MathUtils.random(0, stage.getWidth()) - stage.getWidth() / 8f, 1000);

                Entity ion = new Entity();
                ion.add(new PositionComponent(position, entitySize.x, entitySize.y, 280));
                ion.add(new MovementComponent(new Vector2(1200, 0).rotate(280)));
                ion.add(new LifetimeComponent(0, 2f));
                ion.add(new AnimationComponent(.06f, new TextureRegion[] {
                        atlas.findRegion("fourCircles"),
                        atlas.findRegion("eightCircles")},
                        Animation.PlayMode.LOOP
                ));
                if (MathUtils.randomBoolean())
                    animm.get(ion).shadeColor = Color.CYAN;
                else
                    animm.get(ion).shadeColor = Color.YELLOW;

                ion.add(new EventComponent(.1f, true, (e, engine) -> {
                    pm.get(e).rotation += 20;
                    pm.get(e).position.add(new Vector2(MathUtils.random(0, 10 * scale), (MathUtils.random(0, 10 * scale))));
                }));

                engine.addEntity(ion);
            }
        }, .02f, 100);

        VisualEvent doNothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {/* nothing */}
        }, .35f, 1);

        VisualEvent rippleOut = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .28f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.RED,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .1f, 2);

        VisualEvent shocking = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .28f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("shock1"),
                                    atlas.findRegion("shock2"),
                                    atlas.findRegion("shock1"),
                                    atlas.findRegion("shock2"),
                                    atlas.findRegion("shock1"),
                                    atlas.findRegion("shockFinal")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .1f, 2);

        Move move = new Move("Volt Deluge", user, 5, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 1), new BoardPosition(-1, -1),  new BoardPosition(-2, 0),  new BoardPosition(-3, 1), new BoardPosition(-3, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.8f)) {
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);
                            status.get(enemy).addStatusEffect(defenseless(3), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 1), new BoardPosition(-1, -1),  new BoardPosition(-2, 0),  new BoardPosition(-3, 1), new BoardPosition(-3, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        ions, doNothing, rippleOut.copy(), shocking.copy(), rippleOut, shocking})),
                new MoveInfo(false, 1, (enemy, userEntity) -> {
                    if (MathUtils.randomBoolean(.8f) && enemy.acceptsStatusEffects) {
                        enemy.statusEffectInfos.add(paralyze(3).createStatusEffectInfo());
                        enemy.statusEffectInfos.add(defenseless(3).createStatusEffectInfo());
                    }
                })
        );
        move.setAttackDescription("Causes a shower of electricity to rain near the user. Deals regular damage, and has an 80% chance to inflict targets with" +
        " paralysis and defenseless.");
        return move;
    }

    //Mummy
    public static Move barrage(Entity user) {
        VisualEvent barrage = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-20 * scale, 20 * scale), MathUtils.random(-20 * scale, 20 * scale)),
                        entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("fist");
                sprite.setOriginCenter();
                sprite.setColor(Color.PINK);
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(fist);
            }
        }, .1f, 5);

        Move move = new Move("Barrage", null, user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{barrage})), new MoveInfo(false, 1));
        move.setAttackDescription("Attacks the target with a flurry of fists. Deals regular damage.");
        return move;
    }

    public static Move feint(Entity user) {
        VisualEvent barrage = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-20 * scale, 20 * scale), MathUtils.random(-20 * scale, 20 * scale)),
                        entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("fist");
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(fist);
            }
        }, .1f, 4);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Color.DARK_GRAY,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        Move move = new Move("Feint", nm.get(user).name + " struck where the enemy was vulnerable!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        int numStatuses = 0;

                        if (status.has(enemy))
                            numStatuses = status.get(enemy).getTotalStatusEffects();

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * numStatuses - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode.copy(), barrage.copy(), explode.copy(), barrage.copy(), explode, barrage})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects)
                        enemy.hp -= userEntity.attack * enemy.statusEffectInfos.size;
                })
        );
        move.setAttackDescription("Tricks the target with a fake strike before going in with a barrage of punches. Deals damage equal to the " +
        "user's attack multiplied by the number of status effects the target has.");
        return move;
    }

    public static Move basiliskPunch(Entity user) {
        VisualEvent barrage = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(35 * scale, 35 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .6f));
                Sprite sprite = atlas.createSprite("fist");
                sprite.setOriginCenter();
                sprite.setColor(Color.BROWN);
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 3)));

                engine.addEntity(fist);
            }
        }, .1f, 1);

        VisualEvent doNothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
            }
        }, .2f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.ORANGE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 3)));

                engine.addEntity(sparkle);
            }
        }, .19f, 2);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 15 * scale)));
                bubble.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(Color.GREEN);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 3)));

                engine.addEntity(bubble);
            }
        }, .19f, 2);

        Move move = new Move("Basilisk Strike", "The target was petrified!", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(petrify(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{barrage, doNothing, sparkle.copy(), bubble.copy(), sparkle, bubble})), new MoveInfo(false, 1, petrify(2).createStatusEffectInfo()));
        move.setAttackDescription("Strikes the target with a fist imbued in a mysterious poison. Deals regular damage and inflicts the target with Petrify for 2 turns.");
        return move;
    }

    public static Move curseAttack(Entity user) {
        VisualEvent fire = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(64 * scale, 64 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fire = new Entity();
                fire.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                fire.add(new MovementComponent(new Vector2(50 * scale, 0)));
                mm.get(fire).movement.setAngle(direction);
                fire.add(new LifetimeComponent(0, 1.2f));
                fire.add(new AnimationComponent(.05f, new TextureRegion[]{
                        atlas.findRegion("flame"),
                        atlas.findRegion("flame2"),
                        atlas.findRegion("flame3")},
                        new Color(.1f, .1f, .1f, 1),
                        Animation.PlayMode.LOOP));
                fire.add(new EventComponent(.01f, true, (entity, engine) -> {
                    mm.get(entity).movement.rotate(5);
                    mm.get(entity).movement.add(new Vector2(mm.get(entity).movement.len() / 15f, 0).rotate(mm.get(entity).movement.angle()));
                    Color color = animm.get(entity).shadeColor;
                    color = new Color(
                            color.r,
                            color.g,
                            color.b,
                            MathUtils.clamp(color.a - 1f / 120, 0, 1)
                    );
                    animm.get(entity).shadeColor = color;
                }));

                engine.addEntity(fire);
            }
        }, .01f, 6);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.BLACK);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 3)));

                engine.addEntity(sparkle);
            }
        }, .19f, 2);

        VisualEvent spinningDiamond = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 30 * scale), MathUtils.random(-50 * scale, 20 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 15 * scale)));
                bubble.add(new LifetimeComponent(0, 1.5f));
                Sprite sprite = new Sprite(atlas.findRegion("diamondBoom5"));
                sprite.setOriginCenter();
                sprite.setColor(Color.GREEN);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            spr.getColor().r,
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - 1f / 15f, 0, 1));
                    pm.get(entity).rotation += 10;
                }));

                engine.addEntity(bubble);
            }
        }, .19f, 2);

        Move move = new Move("Curse", nm.get(user).name + " placed a curse.", user, 5, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(curse(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{fire, spinningDiamond.copy(), sparkle.copy(), spinningDiamond, sparkle})), new MoveInfo(false, 0, curse(3).createStatusEffectInfo()));
        move.setAttackDescription("Uses arcane rituals to cast a curse on the target. Inflicts the target with Curse for 3 turns, lowering all " +
        "of their stats.");
        return move;
    }

    //squizzerd
    public static Move restBody(Entity user) {

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            private boolean open;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite;
                if (open)
                    sprite = new Sprite(atlas.findRegion("openDiamonds"));
                else
                    sprite = new Sprite(atlas.findRegion("diamonds"));
                open = !open;
                sprite.setOriginCenter();
                sprite.setColor(new Color(.3f, .3f, 1, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .14f, 9);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(130 * scale, 130 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .26f));
                boom.add(new AnimationComponent(.05f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        new Color(.3f, .3f, 1, 1),
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeColor = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                progress = MathUtils.clamp(progress + .1f, 0, 1);
                am.get(user).actor.shade(am.get(user).actor.getColor().cpy().lerp(new Color(.3f, .3f, 1, 1), progress));
            }
        }, .025f, 12);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        Move move = new Move("Rest Body", nm.get(user).name + " began to rest its body.", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 1, 0, stm.get(e).getModMaxHp(e));

                        if (status.has(e))
                            status.get(e).removeAll(e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{sparkle, explode,  returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    userEntity.hp = MathUtils.clamp(userEntity.hp + 1, 0, userEntity.maxHp);
                    if (userEntity.acceptsStatusEffects)
                        userEntity.statusEffectInfos.clear();
                })
        );
        move.setAttackDescription("Relaxes tension in its body. Recovers 1 point of health and cures all of the user's status effects.");
        return move;
    }

    public static Move ignite(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .14f, 2);

        VisualEvent smallBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                        entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("boom");
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(fist);
            }
        }, .03f, 15);

        VisualEvent largerRadiusBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-150 * scale, 150 * scale), MathUtils.random(-150 * scale, 150 * scale)),
                        entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("boom");
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                boom.add(new SpriteComponent(sprite));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(boom);
            }
        }, .01f, 30);

        VisualEvent redSparkleOut = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(900 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, .3f));
                Sprite sprite = new Sprite(atlas.findRegion("diamonds"));
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            spr.getColor().r,
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - 1f / 30f, 0, 1));
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 6);

        Move move = new Move("Ignite", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.5f))
                            status.get(enemy).addStatusEffect(burn(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{redSparkleOut, explode, smallBooms, explodeBig, largerRadiusBooms})), new MoveInfo(false, 1, (enemy, userEntity) -> {
            if (enemy.acceptsStatusEffects && MathUtils.randomBoolean(.5f)) {
                enemy.statusEffectInfos.add(burn(3).createStatusEffectInfo());
            }}));
        move.setAttackDescription("Sets the target on fire using mystic powers. Deals regular damage and has a 50% chance to Burn the target for 3 turns.");
        return move;
    }

    public static Move drench2(Entity user) {
        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("openDiamonds"));
                sprite.setOriginCenter();
                sprite.setColor(Color.PURPLE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .19f, 9);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(64 * scale, 64 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bubble.add(new LifetimeComponent(0, 2f));
                Sprite water = new Sprite(atlas.findRegion("bubble"));
                water.setOriginCenter();
                water.setColor(1f, .35f, 1f, 0);
                bubble.add(new SpriteComponent(water));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeIn(5)));

                engine.addEntity(bubble);
            }
        }, .01f, 1);

        VisualEvent ripple = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .25f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.PURPLE,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(600 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, .3f));
                Sprite sprite = new Sprite(atlas.findRegion("diamonds"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(.9f, .2f, .9f, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            spr.getColor().r,
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - 1f / 30f, 0, 1));
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 7);

        Move move = new Move("Drench", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sparkle, ripple, largeSparkle})), new MoveInfo(false, 1));
        move.setAttackDescription("Summons a rush of water to drench the opponent. Deals regular damage.");
        return move;
    }

    public static Move cometShowerClose(Entity user) {

        VisualEvent preSparkles = new VisualEvent(new VisualEffect() {
            private boolean open;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 position = new Vector2(MathUtils.random(stage.getWidth() / 6f, stage.getWidth() - stage.getWidth() / 6f), MathUtils.random(stage.getHeight() / 6f, stage.getHeight() - stage.getHeight() / 6f));
                position.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(position, entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, .5f));
                Sprite sprite;
                if (open)
                    sprite = new Sprite(atlas.findRegion("openDiamonds"));
                else
                    sprite = new Sprite(atlas.findRegion("diamonds"));
                sprite.setOriginCenter();
                sprite.setColor(Color.BLACK);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(5)));

                engine.addEntity(sparkle);
            }
        }, .04f, 5);

        VisualEvent preBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 position = new Vector2(MathUtils.random(stage.getWidth() / 6f, stage.getWidth() - stage.getWidth() / 6f), MathUtils.random(stage.getHeight() / 6f, stage.getHeight() - stage.getHeight() / 6f));
                position.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity diamondBoom = new Entity();
                diamondBoom.add(new PositionComponent(position, entitySize.x, entitySize.y, 0));
                diamondBoom.add(new LifetimeComponent(0, .5f));
                diamondBoom.add(new AnimationComponent(.05f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        new Color(1, 0, 0, 1),
                        Animation.PlayMode.NORMAL));
                diamondBoom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(5)));

                engine.addEntity(diamondBoom);
            }
        }, .04f, 5);

        VisualEvent comets = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(140 * scale, 140 * scale);
                Vector2 position = new Vector2(MathUtils.random(0, stage.getWidth()) - stage.getWidth() / 8f, 1000);

                Entity comet = new Entity();
                comet.add(new PositionComponent(position, entitySize.x, entitySize.y, 280));
                comet.add(new MovementComponent(new Vector2(1900, 0).rotate(280)));
                comet.add(new LifetimeComponent(0, 2f));
                comet.add(new AnimationComponent(.06f, new TextureRegion[] {
                        atlas.findRegion("comet"),
                        atlas.findRegion("comet2")},
                        new Color(1f, .3f, .3f, 1),
                        Animation.PlayMode.LOOP
                ));
                comet.add(new EventComponent(.1f, true, EventCompUtil.fadeIn(5)));

                engine.addEntity(comet);
            }
        }, .035f, 12);

        VisualEvent ripple = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .28f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("explode"),
                                    atlas.findRegion("explode2"),
                                    atlas.findRegion("explode3"),
                                    atlas.findRegion("explode4"),
                                    atlas.findRegion("explode5"),
                                    atlas.findRegion("explode6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .2f, 2);

        Move move = new Move("Meteor Shower", user, 6, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1),  new BoardPosition(-1, 1),
                new BoardPosition(0, 1),  new BoardPosition(0, -1),
                new BoardPosition(1, 0),  new BoardPosition(1, -1),  new BoardPosition(1, 1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 2, 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1),  new BoardPosition(-1, 1),
                new BoardPosition(0, 1),  new BoardPosition(0, -1),
                new BoardPosition(1, 0),  new BoardPosition(1, -1),  new BoardPosition(1, 1)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        preSparkles.copy(), preBooms.copy(), preSparkles.copy(), preBooms.copy(),
                        comets.copy(), ripple.copy(), comets.copy(), ripple.copy(), comets.copy(), ripple, comets, preBooms.copy(), preBooms})), new MoveInfo(true, 2));
        move.setAttackDescription("Uses most of its energy to summon a flurry of comets near itself. Ignores the opponents defense and deals 2x damage.");
        return move;
    }

    //wyvrapor
    public static Move dragonBreath(Entity user) {
        VisualEvent breath = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset = -45;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                offset = MathUtils.random(-45, 45);
                /*
                if (offset >= 45)
                    sprayingBack = true;
                if (!sprayingBack)
                    offset += 7.5f + MathUtils.random(0, 3f);
                else
                    offset -= 7.5 - MathUtils.random(0, 3f);
                if (offset < -45) { //reset
                    offset = -45;
                    sprayingBack = false;
                }
                */

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(60 * scale, 60 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                if (direction == 90)
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else if (direction == 180)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.y / 2f,
                            boards.getTileHeight() / 2);
                else if (direction == 270)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2);

                Entity clouds = new Entity();
                clouds.add(new AnimationComponent(.1f, new TextureRegion[]{
                        atlas.findRegion("cloud"), atlas.findRegion("cloud2")
                }, Animation.PlayMode.LOOP));
                if (MathUtils.randomBoolean(.3f))
                    animm.get(clouds).shadeColor = new Color(.2f, .2f, .6f, 1);
                else if (MathUtils.randomBoolean(.3f))
                    animm.get(clouds).shadeColor = new Color(.1f, .1f, 1, 1);
                else
                    animm.get(clouds).shadeColor = new Color(0, 0, .2f, 1);

                clouds.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                clouds.add(new MovementComponent(new Vector2(MathUtils.random(350, 750) * scale, 0).setAngle(direction + offset)));
                clouds.add(new LifetimeComponent(0, .52f));
                clouds.add(new EventComponent(0.02f, true, new GameEvent() {
                    private int timesCalled;
                    @Override
                    public void event(Entity e, Engine engine) {
                        timesCalled++;
                        if (timesCalled >= 18) {
                            Color color = animm.get(e).shadeColor;
                            color = new Color(
                                    color.r,
                                    color.g,
                                    color.b,
                                    MathUtils.clamp(color.a - 1f / 8, 0, 1));
                            animm.get(e).shadeColor = color;
                        }
                        pm.get(e).rotation += 9;
                    }
                }));
                engine.addEntity(clouds);
            }
        }, .03f, 28);

        VisualEvent fires = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(32 * scale, 32 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity flame = new Entity();
                    flame.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-40, 40), MathUtils.random(-40, 40)), entitySize.x, entitySize.y, 0));
                    flame.add(new LifetimeComponent(0, .4f));
                    flame.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("flame"),
                                    atlas.findRegion("flame2"),
                                    atlas.findRegion("flame3")},
                            Color.BLUE,
                            Animation.PlayMode.NORMAL));
                    flame.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(flame);
                }
            }
        }, .2f, 3);

        Move move = new Move("Dragon Breath", nm.get(user).name + " spewed dragon breath!", user, 1,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, fires})), new MoveInfo(false, 1));
        move.setAttackDescription("Breathes a gaseous flame in a wide arc in front of itself. Deals regular damage.");
        return move;
    }

    public static Move toxicBreath(Entity user) {
        VisualEvent breath = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset = -45;
            boolean sprayingBack;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                offset = MathUtils.random(-45, 45);

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(60 * scale, 60 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                if (direction == 90)
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else if (direction == 180)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.y / 2f,
                            boards.getTileHeight() / 2);
                else if (direction == 270)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2);

                Entity clouds = new Entity();
                clouds.add(new AnimationComponent(.1f, new TextureRegion[]{
                        atlas.findRegion("cloud"), atlas.findRegion("cloud2")
                }, Animation.PlayMode.LOOP));
                if (MathUtils.randomBoolean(.3f))
                    animm.get(clouds).shadeColor = new Color(.1f, .3f, 0, 1);
                else if (MathUtils.randomBoolean(.3f))
                    animm.get(clouds).shadeColor = new Color(0, .3f, 0, 1);
                else
                    animm.get(clouds).shadeColor = new Color(0, .8f, 0, 1);

                clouds.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                clouds.add(new MovementComponent(new Vector2(MathUtils.random(150, 550) * scale, 0).setAngle(direction + offset)));
                clouds.add(new LifetimeComponent(0, .62f));
                clouds.add(new EventComponent(0.02f, true, new GameEvent() {
                    private int timesCalled;
                    @Override
                    public void event(Entity e, Engine engine) {
                        timesCalled++;
                        if (timesCalled >= 21) {
                            Color color = animm.get(e).shadeColor;
                            color = new Color(
                                    color.r,
                                    color.g,
                                    color.b,
                                    MathUtils.clamp(color.a - 1f / 10, 0, 1));
                            animm.get(e).shadeColor = color;
                        }
                        pm.get(e).rotation += 6;
                    }
                }));
                engine.addEntity(clouds);
            }
        }, .03f, 28);

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(64 * scale, 64 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.GREEN,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .2f, 2);

        Move move = new Move("Toxic Breath", nm.get(user).name + " spewed a poisonous breath!", user, 2,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(poison(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, ripples})), new MoveInfo(false, 0, poison(2).createStatusEffectInfo()));
        move.setAttackDescription("Breathes a poisonous gas in front of itself. Inflicts poison for 2 turns.");
        return move;
    }

    public static Move freshBreath(Entity user) {
        VisualEvent breath = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset = -45;
            boolean sprayingBack;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                offset = MathUtils.random(-45, 45);

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(60 * scale, 60 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                if (direction == 90)
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else if (direction == 180)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.y / 2f,
                            boards.getTileHeight() / 2);
                else if (direction == 270)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2);

                Entity clouds = new Entity();
                clouds.add(new AnimationComponent(.1f, new TextureRegion[]{
                        atlas.findRegion("cloud"), atlas.findRegion("cloud2")
                }, Animation.PlayMode.LOOP));
                /*
                if (MathUtils.randomBoolean(.3f))
                    animm.get(clouds).shadeColor = new Color(.1f, .3f, 0, 1);
                else if (MathUtils.randomBoolean(.3f))
                    animm.get(clouds).shadeColor = new Color(0, .3f, 0, 1);
                else
                    animm.get(clouds).shadeColor = new Color(0, .8f, 0, 1);
                    */

                clouds.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                clouds.add(new MovementComponent(new Vector2(MathUtils.random(250, 650) * scale, 0).setAngle(direction + offset)));
                clouds.add(new LifetimeComponent(0, .62f));
                clouds.add(new EventComponent(0.02f, true, new GameEvent() {
                    private int timesCalled;
                    @Override
                    public void event(Entity e, Engine engine) {
                        timesCalled++;
                        if (timesCalled >= 16) {
                            Color color = animm.get(e).shadeColor;
                            color = new Color(
                                    color.r,
                                    color.g,
                                    color.b,
                                    MathUtils.clamp(color.a - 1f / 15, 0, 1));
                            animm.get(e).shadeColor = color;
                        }
                        pm.get(e).rotation += 8;
                    }
                }));
                engine.addEntity(clouds);
            }
        }, .03f, 28);

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(64 * scale, 64 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.WHITE,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .24f, 4);

        VisualEvent ripplesLarge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.WHITE,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .2f, 2);

        Move move = new Move("Fresh Breath", nm.get(user).name + " breathed refreshing air!", user, 2,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).removeAll(enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, ripples, ripplesLarge})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects)
                        enemy.statusEffectInfos.clear();
                })
        );
        move.setAttackDescription("Breathes a refreshing wind in a wide arc in front of itself. Removes the status effects of the targets.");
        return move;
    }

    public static Move spaBreath(Entity user) {
        VisualEvent breath = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset = -45;
            boolean sprayingBack;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                offset = MathUtils.random(-45, 45);

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(60 * scale, 60 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                if (direction == 90)
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else if (direction == 180)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.y / 2f,
                            boards.getTileHeight() / 2);
                else if (direction == 270)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2);

                Entity clouds = new Entity();
                clouds.add(new AnimationComponent(.1f, new TextureRegion[]{
                        atlas.findRegion("cloud"), atlas.findRegion("cloud2")
                }, Animation.PlayMode.LOOP));
                if (MathUtils.randomBoolean(.3f))
                    animm.get(clouds).shadeColor = Color.WHITE;
                else
                    animm.get(clouds).shadeColor = new Color(.7f, 1, .7f, 1);

                clouds.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                clouds.add(new MovementComponent(new Vector2(MathUtils.random(250, 650) * scale, 0).setAngle(direction + offset)));
                clouds.add(new LifetimeComponent(0, .62f));
                clouds.add(new EventComponent(0.02f, true, new GameEvent() {
                    private int timesCalled;
                    @Override
                    public void event(Entity e, Engine engine) {
                        timesCalled++;
                        if (timesCalled >= 16) {
                            Color color = animm.get(e).shadeColor;
                            color = new Color(
                                    color.r,
                                    color.g,
                                    color.b,
                                    MathUtils.clamp(color.a - 1f / 15, 0, 1));
                            animm.get(e).shadeColor = color;
                        }
                        pm.get(e).rotation += 8;
                    }
                }));
                engine.addEntity(clouds);
            }
        }, .03f, 28);

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(64 * scale, 64 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.GREEN,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .24f, 4);

        VisualEvent ripplesLarge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .28f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("explodeGreen"),
                                    atlas.findRegion("explodeGreen2"),
                                    atlas.findRegion("explodeGreen3"),
                                    atlas.findRegion("explodeGreen4"),
                                    atlas.findRegion("explodeGreen5"),
                                    atlas.findRegion("explodeGreen6"),
                            },
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .2f, 2);

        Move move = new Move("Spa Breath", nm.get(user).name + " breathed a soothing air!", user, 3,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp + 3, 0, stm.get(enemy).getModMaxHp(enemy));

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, ripples, ripplesLarge})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    enemy.hp = MathUtils.clamp(enemy.hp + 3, 0, enemy.maxHp);
                })
        );
        move.setAttackDescription("Breathes a soothing wind in a wide arc in front of itself. Heals the targets' health by 3 points.");
        return move;
    }

    //jellymiss
    public static Move restore(Entity user) {
        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 10 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 20 * scale)));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(.7f, .4f, 1, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, (entity, engine) -> {
                    sprite.setColor(
                            sprite.getColor().r,
                            sprite.getColor().g,
                            MathUtils.clamp(sprite.getColor().b + 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().a - 1f / 12, 0, 1));

                    mm.get(entity).movement.add(0, 15 * scale);
                }));

                engine.addEntity(sparkle);
            }
        }, .06f, 4);

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
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, .8f));
                sparkle.add(new MovementComponent(new Vector2(0, 25 * scale)));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(MathUtils.random(.6f, 1), MathUtils.random(.6f, 1), MathUtils.random(.6f, 1), 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(8)));

                engine.addEntity(sparkle);
            }
        }, .035f, 9);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlue = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLUE, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Restore", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp += stm.get(enemy).getModMaxHp(enemy) / 2, 0, stm.get(enemy).getModMaxHp(enemy));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlue, largeSparkle, circles, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    enemy.hp = MathUtils.clamp(enemy.hp + enemy.maxHp / 2, 0, enemy.maxHp);
                })
        );
        move.setAttackDescription("Restores wounds using warm water. Restores the health of the target by 1/2 of their max health.");
        return move;
    }

    public static Move regen(Entity user) {
        VisualEvent regenParticles1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 10 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 20 * scale)));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("pierce2"));
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, (entity, engine) -> {
                    sprite.setColor(
                            sprite.getColor().r,
                            sprite.getColor().g,
                            MathUtils.clamp(sprite.getColor().b + 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().a - 1f / 12, 0, 1));

                    mm.get(entity).movement.add(0, 15 * scale);
                }));

                engine.addEntity(sparkle);
            }
        }, .1f, 4);

        VisualEvent regenParticles2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(40 * scale, 40 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 10 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 20 * scale)));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("pierce2"));
                sprite.setOriginCenter();
                sprite.setColor(Color.PINK);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, (entity, engine) -> {
                    sprite.setColor(
                            sprite.getColor().r,
                            sprite.getColor().g,
                            MathUtils.clamp(sprite.getColor().b + 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().a - 1f / 12, 0, 1));

                    mm.get(entity).movement.add(0, 15 * scale);
                }));

                engine.addEntity(sparkle);
            }
        }, .1f, 4);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToGreen = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.GREEN, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Regen", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(regeneration(3), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToGreen, regenParticles1, regenParticles2, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                if (enemy.acceptsStatusEffects)
                    enemy.statusEffectInfos.add(regeneration(3).createStatusEffectInfo());
                })
        );
        move.setAttackDescription("Uses heated water to slowly heal wounds over time. Gives the target Regeneration for 3 turns.");
        return move;
    }

    public static Move boost(Entity user) {
        VisualEvent regenParticles1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 10 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 20 * scale)));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("pierce2"));
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, (entity, engine) -> {
                    sprite.setColor(
                            sprite.getColor().r,
                            sprite.getColor().g,
                            MathUtils.clamp(sprite.getColor().b + 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().a - 1f / 12, 0, 1));

                    mm.get(entity).movement.add(0, 15 * scale);
                }));

                engine.addEntity(sparkle);
            }
        }, .1f, 4);

        VisualEvent regenParticles2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(40 * scale, 40 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 10 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 20 * scale)));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("pierce2"));
                sprite.setOriginCenter();
                sprite.setColor(Color.PINK);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, (entity, engine) -> {
                    sprite.setColor(
                            sprite.getColor().r,
                            sprite.getColor().g,
                            MathUtils.clamp(sprite.getColor().b + 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().a - 1f / 12, 0, 1));

                    mm.get(entity).movement.add(0, 15 * scale);
                }));

                engine.addEntity(sparkle);
            }
        }, .1f, 4);

        VisualEvent ripple = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .25f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.PINK,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .25f, 2);

        VisualEvent rippleGold = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(180 * scale, 180 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .25f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.GOLD,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .3f, 2);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);

                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Boost", user, 5, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(speedUp(3), enemy);
                            status.get(enemy).addStatusEffect(attackUp(3), enemy);
                            status.get(enemy).addStatusEffect(guardUp(3), enemy);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, ripple.copy(), regenParticles1, ripple.copy(), regenParticles2,
                        returnToNormalGradual, returnToNormal, rippleGold})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects) {
                        enemy.statusEffectInfos.add(regeneration(2).createStatusEffectInfo());
                        enemy.statusEffectInfos.add(speedUp(2).createStatusEffectInfo());
                        enemy.statusEffectInfos.add(attackUp(2).createStatusEffectInfo());
                        enemy.statusEffectInfos.add(guardUp(2).createStatusEffectInfo());
                    }
                })
        );
        move.setAttackDescription("Invigorates the energy of the target. Gives the target a boost to Attack, Defense, and Speed for 3 turns.");
        return move;
    }

    public static Move transfer(Entity user) {

        VisualEvent rippleOther = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .25f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.WHITE,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .25f, 2);

        VisualEvent rippleSelf = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos;
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .25f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.WHITE,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .25f, 2);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Transfer", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy)) {
                            stm.get(enemy).sp = MathUtils.clamp(stm.get(enemy).sp + 2, 0, stm.get(enemy).getModMaxSp(enemy));
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, rippleSelf, rippleOther, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    enemy.sp += 2;
                }));
        move.setAttackDescription("Transfers the user's energy to any object or character. Increases the target's SP by 2 points.");
        return move;
    }

    //mirrorman
    public static Move reflectMove(Entity user) {
        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 10 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 20 * scale)));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("sparkle"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(.7f, .4f, 1, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, (entity, engine) -> {
                    sprite.setColor(
                            sprite.getColor().r,
                            sprite.getColor().g,
                            MathUtils.clamp(sprite.getColor().b + 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().a - 1f / 12, 0, 1));

                    mm.get(entity).movement.add(0, 15 * scale);
                }));

                engine.addEntity(sparkle);
            }
        }, .03f, 8);

        VisualEvent mirror = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity mirror = new Entity();
                mirror.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                mirror.add(new LifetimeComponent(0, 1.5f));

                mirror.add(new AnimationComponent(.1f, new TextureRegion[]{
                        atlas.findRegion("mirror"),
                        atlas.findRegion("mirror2"),
                        atlas.findRegion("mirror3")},
                        Animation.PlayMode.LOOP));
                mirror.add(new EventComponent(.05f, true, EventCompUtil.fadeOutAfter(20, 10)));

                engine.addEntity(mirror);
            }
        }, .2f, 1);

        Move move = new Move("Reflect Move", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (mvm.has(enemy)) {
                            Move copiedMove = mvm.get(enemy).moveList.get(0).createCopy(e);
                            if (copiedMove.getName().equals("Reflect Move") || copiedMove.getName().equals("Mirror Move") ||
                                    copiedMove.getName().equals("Roulette Move")) { //fail if its copying a move that copies other moves
                                return;
                            }

                            if (mvm.get(e).moveList.size >= 4)
                                mvm.get(e).moveList.set(3, copiedMove);
                            else
                                mvm.get(e).moveList.add(copiedMove);

                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{mirror, largeSparkle})),
                new MoveInfo(false, 0, (enemy, userEntity) -> userEntity.arbitraryValue = (MathUtils.randomBoolean())? userEntity.arbitraryValue + 50 : userEntity.arbitraryValue));
        move.setAttackDescription("Uses reflection to copy the target's fighting tactics. Copies the target's first move and replaces the user's last move with it.");
        return move;
    }

    public static Move mirrorMove(Entity user) {
        VisualEvent largeSparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 10 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 20 * scale)));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite = new Sprite(atlas.findRegion("sparkle"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(.4f, .7f, 1, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, (entity, engine) -> {
                    sprite.setColor(
                            MathUtils.clamp(sprite.getColor().r - 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().g - 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().b - 1f / 24, 0, 1),
                            MathUtils.clamp(sprite.getColor().a - 1f / 12, 0, 1));

                    mm.get(entity).movement.add(0, 15 * scale);
                }));

                engine.addEntity(sparkle);
            }
        }, .03f, 8);

        VisualEvent mirror = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity mirror = new Entity();
                mirror.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                mirror.add(new LifetimeComponent(0, 1.5f));

                mirror.add(new AnimationComponent(.1f, new TextureRegion[]{
                        atlas.findRegion("mirror"),
                        atlas.findRegion("mirror2"),
                        atlas.findRegion("mirror3")},
                        Animation.PlayMode.LOOP));
                mirror.add(new EventComponent(.05f, true, EventCompUtil.fadeOutAfter(20, 10)));

                engine.addEntity(mirror);
            }
        }, .2f, 1);

        Move move = new Move("Mirror Move", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (mvm.has(enemy)) {
                            Move copiedMove = mvm.get(enemy).moveList.get(mvm.get(enemy).moveList.size - 1).createCopy(e);
                            if (copiedMove.getName().equals("Reflect Move") || copiedMove.getName().equals("Mirror Move") ||
                                    copiedMove.getName().equals("Roulette Move")) { //fail if its copying a move that copies other moves
                                return;
                            }

                            if (mvm.get(e).moveList.size >= 4)
                                mvm.get(e).moveList.set(3, copiedMove);
                            else
                                mvm.get(e).moveList.add(copiedMove);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{mirror, largeSparkle})),
                new MoveInfo(false, 0, (enemy, userEntity) -> userEntity.arbitraryValue = (MathUtils.randomBoolean())? userEntity.arbitraryValue + 50 : userEntity.arbitraryValue));
        move.setAttackDescription("Uses mirrors to copy the target's alternate fighting tactics. Copies the target's last move and replaces the user's last move with it.");
        return move;
    }

    public static Move rouletteReflect(Entity user) {
        VisualEvent spinning = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(40 * scale, 40 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity circles = new Entity();
                circles.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                circles.add(new LifetimeComponent(0, 1.5f));
                circles.add(new AnimationComponent(.5f,
                        new TextureRegion[]{atlas.findRegion("fourCircles"),
                                atlas.findRegion("sixCircles"),
                                atlas.findRegion("eightCircles")},
                        Color.ORANGE,
                        Animation.PlayMode.NORMAL));
                circles.add(new EventComponent(.05f, true, (entity, engine) -> {
                    pm.get(entity).rotation += 8 + pm.get(entity).rotation / 1.5f;
                    Color color = animm.get(entity).shadeColor;
                    color = new Color(
                            MathUtils.clamp(color.r + 1f / 30f, 0, 1),
                            MathUtils.clamp(color.g - 1f / 30f, 0, 1),
                            MathUtils.clamp(color.b + 1f / 30f, 0, 1),
                            MathUtils.clamp(color.a - 1f / 30f, 0, 1));
                    animm.get(entity).shadeColor = color;
                }));

                engine.addEntity(circles);
            }
        }, 1.5f, 1);

        VisualEvent mirror = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity mirror = new Entity();
                mirror.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                mirror.add(new LifetimeComponent(0, 1.5f));

                mirror.add(new AnimationComponent(.1f, new TextureRegion[]{
                        atlas.findRegion("mirror"),
                        atlas.findRegion("mirror2"),
                        atlas.findRegion("mirror3")},
                        Animation.PlayMode.LOOP));
                mirror.add(new EventComponent(.05f, true, EventCompUtil.fadeOutAfter(20, 10)));

                engine.addEntity(mirror);
            }
        }, .2f, 1);

        Move move = new Move("Roulette Move", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (mvm.has(enemy)) {
                            Move copiedMove = mvm.get(enemy).moveList.get(MathUtils.random(0, mvm.get(enemy).moveList.size - 1)).createCopy(e);
                            if (copiedMove.getName().equals("Reflect Move") || copiedMove.getName().equals("Mirror Move") ||
                                    copiedMove.getName().equals("Roulette Move")) { //fail if its copying a move that copies other moves
                                return;
                            }
                            if (mvm.get(e).moveList.size >= 4)
                                mvm.get(e).moveList.set(3, copiedMove);
                            else
                                mvm.get(e).moveList.add(copiedMove);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{mirror, spinning})), new MoveInfo(false, 0,
                (enemy, userEntity) -> userEntity.arbitraryValue = (MathUtils.randomBoolean())? userEntity.arbitraryValue + 50 : userEntity.arbitraryValue));
        move.setAttackDescription("Uses reflection, mirrors, and a bit of luck to copy the target's fighting actions. Copies one of the target's moves at random and " +
                "replaces the user's last move with it.");
        return move;
    }

    //pheonix
    public static Move peck(Entity user) {
        VisualEvent bash = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 offset = new Vector2(MathUtils.random(-40, 40), MathUtils.random(-40, 40));

                Vector2 entitySize = new Vector2(70 * scale, 70 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);
                tilePosition.add(offset);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);


                Vector2 entitySize2 = new Vector2(40 * scale, 40 * scale);
                Vector2 tilePosition2 = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize2.x / 2f, t.getHeight() / 2 -entitySize2.y / 2f));
                tilePosition2.add(offset);
                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition2, entitySize2.x, entitySize2.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("boom"));
                bamSprite.setOriginCenter();
                bamSprite.setColor(Color.RED);
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .08f, 6);

        Move move = new Move("Peck", nm.get(user).name + " attacked!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{bash})), new MoveInfo(false, 1));
        move.setAttackDescription("Pecks with a fiery beak. Deals regular damage.");
        return move;
    }

    public static Move vigorate(Entity user) {
        //Visuals---
        VisualEvent fire = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                            , entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .5f));
                    boom.add(new AnimationComponent(.02f,
                            new TextureRegion[]{atlas.findRegion("flame"),
                                    atlas.findRegion("flame2"),
                                    atlas.findRegion("flame3")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(10)));
                    engine.addEntity(boom);
                }
            }
        }, .06f, 15);

        VisualEvent sparkles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(5 * scale, 5 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity fist = new Entity();
                    fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                            entitySize.x, entitySize.y, 0));
                    fist.add(new LifetimeComponent(0, .3f));
                    Sprite sprite = atlas.createSprite("sparkle");
                    sprite.setOriginCenter();
                    sprite.setColor(Color.ORANGE);
                    fist.add(new SpriteComponent(sprite));
                    fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                    engine.addEntity(fist);
                }
            }
        }, .01f, 40);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .14f, 2);

        VisualEvent smallBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                        entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("boom");
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(fist);
            }
        }, .01f, 15);

        VisualEvent largerRadiusBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-100 * scale, 100 * scale), MathUtils.random(-100 * scale, 100 * scale)),
                        entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("sparkle");
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                boom.add(new SpriteComponent(sprite));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(boom);
            }
        }, .005f, 30);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.RED, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        //Move
        Move move = new Move("Vigorate", nm.get(user).name + " gave the target a powerful energy.", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(attackUp(2), enemy);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, fire, sparkles, smallBooms, explodeBig,
                        largerRadiusBooms, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, attackUp(2).createStatusEffectInfo()));
        move.setAttackDescription("Uses spare energy to grant the target a powerful energy. Increases the target's attack for 2 turns.");
        return move;
    }

    public static Move rejunevate(Entity user) {
        //Visuals---
        VisualEvent fire = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                            , entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .5f));
                    boom.add(new AnimationComponent(.02f,
                            new TextureRegion[]{atlas.findRegion("flame"),
                                    atlas.findRegion("flame2"),
                                    atlas.findRegion("flame3")},
                            new Color(.4f, .4f, 1, 1),
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(10)));
                    engine.addEntity(boom);
                }
            }
        }, .06f, 15);

        VisualEvent sparkles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(5 * scale, 5 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity fist = new Entity();
                    fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                            entitySize.x, entitySize.y, 0));
                    fist.add(new LifetimeComponent(0, .3f));
                    Sprite sprite = atlas.createSprite("sparkle");
                    sprite.setOriginCenter();
                    sprite.setColor(ColorUtils.HSV_to_RGB(MathUtils.random(0, 100), 100, 100));
                    fist.add(new SpriteComponent(sprite));
                    fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                    engine.addEntity(fist);
                }
            }
        }, .01f, 40);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .14f, 2);

        VisualEvent smallBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                        entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("openCircle");
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(fist);
            }
        }, .01f, 15);

        VisualEvent sparkleUp = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(0, 1, .4f, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.03f, true, EventCompUtil.fadeOutAfter(10, 10)));

                engine.addEntity(sparkle);
            }
        }, .1f, 14);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.GREEN, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        //Move
        Move move = new Move("Rejuvenate", nm.get(user).name + " gave the target a powerful rejuvenating energy.", user, 6, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        stm.get(enemy).hp = stm.get(enemy).maxHP;
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, fire, sparkles, smallBooms, explodeBig,
                        sparkleUp, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity, userEntity) -> userEntity.hp = userEntity.maxHp));
        move.setAttackDescription("Uses a large amount of spare energy to grant the target life energy. Heals all of the target's health points.");
        return move;
    }

    public static Move selfPurge(Entity user) {

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            private boolean open;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, 1.2f));
                Sprite sprite;
                if (open)
                    sprite = new Sprite(atlas.findRegion("openDiamonds"));
                else
                    sprite = new Sprite(atlas.findRegion("diamonds"));
                open = !open;
                sprite.setOriginCenter();
                sprite.setColor(Color.WHITE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(12)));

                engine.addEntity(sparkle);
            }
        }, .08f, 16);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(190 * scale, 190 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .26f));
                boom.add(new AnimationComponent(.05f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        new Color(1, 1, 1, 1),
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeColor = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                progress = MathUtils.clamp(progress + .1f, 0, 1);
                am.get(user).actor.shade(am.get(user).actor.getColor().cpy().lerp(new Color(0, 0, 1, .4f), progress));
            }
        }, .025f, 12);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        Move move = new Move("Status-Purge", nm.get(user).name + " removed itself of any status effects.", user, 6, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e))
                            status.get(e).removeAll(e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeColor, sparkle, explode,  returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    if (userEntity.acceptsStatusEffects)
                        userEntity.statusEffectInfos.clear();
                })
        );
        move.setAttackDescription("Forces its body to burn hotter, removing any negative status effects.");
        return move;
    }

    //acidsnake
    public static Move bite(Entity user) {
        VisualEvent booms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity img = new Entity();
                img.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                img.add(new LifetimeComponent(0, .07f));
                img.add(new SpriteComponent(atlas.createSprite("boom")));
                sm.get(img).sprite.setColor(Color.GREEN);
                engine.addEntity(img);
            }
        }, .02f, 15);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(MathUtils.random(-18, 18) * scale, MathUtils.random(-18, 18) * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("spiral"));
                sprite.setOriginCenter();
                sprite.setColor(Color.PURPLE);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .12f, 6);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(.2f, .8f, 0, .7f));
                else
                    sprite.setColor(new Color(.7f, .5f, .1f, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        Move move = new Move("Bite", nm.get(user).name + " bit the opponent!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy) && status.has(enemy)) {
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);
                            status.get(enemy).addStatusEffect(poison(2), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{booms, bubble, sludge})), new MoveInfo(false, 1, poison(2).createStatusEffectInfo()));
        move.setAttackDescription("Bites with a poison drenched mouth. Deals regular damage and has a chance to inflict Poison for 2 turns.");
        return move;
    }

    public static Move toxicBite(Entity user) {
        VisualEvent booms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity img = new Entity();
                img.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                img.add(new LifetimeComponent(0, .07f));
                img.add(new SpriteComponent(atlas.createSprite("boom")));
                sm.get(img).sprite.setColor(Color.PURPLE);
                engine.addEntity(img);
            }
        }, .02f, 4);

        VisualEvent sparkOut = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);
                //random displacement
                tilePosition.add(MathUtils.random(-30, 30), MathUtils.random(-30, 30));

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(600 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, .3f));
                Sprite sprite = new Sprite(atlas.findRegion("pierce"));
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            spr.getColor().r,
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - 1f / 30f, 0, 1));
                    pm.get(entity).rotation += 5;
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 6);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(0, .2f, 0, .7f));
                else
                    sprite.setColor(new Color(.2f, 0, 0, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        Move move = new Move("Toxic Bite", nm.get(user).name + " bit the opponent with a deadly toxin!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(toxic(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        booms, sparkOut, booms.copy(), sparkOut.copy(), booms.copy(), sparkOut.copy(), booms.copy(), sparkOut.copy(), sludge
                })), new MoveInfo(false, 1, toxic(2).createStatusEffectInfo()));
        move.setAttackDescription("Mixes existing poison to make an even more lethal toxin. Deals regular damage and has a chance to inflict Toxic for 2 turns.");
        return move;
    }

    public static Move boostToxin(Entity user) {
        VisualEvent booms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity img = new Entity();
                img.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                img.add(new LifetimeComponent(0, .07f));
                img.add(new SpriteComponent(atlas.createSprite("boom")));
                sm.get(img).sprite.setColor(Color.WHITE);
                engine.addEntity(img);
            }
        }, .02f, 4);

        VisualEvent sparkOut = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);
                //random displacement
                tilePosition.add(MathUtils.random(-30, 30), MathUtils.random(-30, 30));

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(600 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, .3f));
                Sprite sprite = new Sprite(atlas.findRegion("pierce"));
                sprite.setOriginCenter();
                sprite.setColor(Color.WHITE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            MathUtils.clamp(spr.getColor().r - 1f / 20f, 0, 1),
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - 1f / 30f, 0, 1));
                    pm.get(entity).rotation += 9;
                    mm.get(entity).movement.rotate(9);
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 6);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("openDiamonds"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("diamonds"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(Color.WHITE);
                else
                    sprite.setColor(Color.GOLD);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        Move move = new Move("Boost Toxin", nm.get(user).name + " bit the opponent with an unpredictable, dangerous toxin!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(poison(2), enemy);
                            float chance = (float) Math.random();
                            if (MathUtils.randomBoolean()) { //1 tree of possible effects (all stat boosters)
                                if (chance <= .25f) { // 25%
                                    status.get(enemy).addStatusEffect(attackUp(2), enemy);
                                } else if (chance > .25f && chance <= .5f) { // 25%
                                    status.get(enemy).addStatusEffect(speedUp(2), enemy);
                                } else if (chance > .5f && chance <= .75f) { // 25%
                                    status.get(enemy).addStatusEffect(guardUp(2), enemy);
                                } else { //remaining 25%: splits into more trees:
                                    chance = (float) Math.random();
                                    if (chance <= .33f) { // 33%
                                        status.get(enemy).addStatusEffect(attackUp3(2), enemy);
                                    } else if (chance > .33f && chance <= .66f) { // 33%
                                        status.get(enemy).addStatusEffect(attackUp2(2), enemy);
                                        status.get(enemy).addStatusEffect(guardUp2(2), enemy);
                                    } else if (chance > .66f && chance <= .86f) { // 20%
                                        status.get(enemy).addStatusEffect(attackUp(2), enemy);
                                        status.get(enemy).addStatusEffect(guardUp(2), enemy);
                                        status.get(enemy).addStatusEffect(speedUp(2), enemy);
                                    } else if (chance > .86f && chance <= .9f) { // 4%
                                        status.get(enemy).addStatusEffect(spUp(2), enemy);
                                        status.get(enemy).addStatusEffect(speedUp2(2), enemy);
                                    } else if (chance > .9f && chance < .95f){ // 5%
                                        status.get(enemy).addStatusEffect(supercharged(2), enemy);
                                    } else {
                                        status.get(enemy).removeAll(enemy);
                                    }
                                }
                            } else { //another tree of possible effects (more harmful/unpredictable)
                                if (chance <= .15f) { // 15%
                                    status.get(enemy).addStatusEffect(burn(2), enemy);
                                } else if (chance > .15f && chance <= .3f) { // 15%
                                    status.get(enemy).addStatusEffect(petrify(2), enemy);
                                } else if (chance > .3f && chance <= .45f) { // 15%
                                    status.get(enemy).addStatusEffect(freeze(2), enemy);
                                } else { //remaining 25%: splits into more trees:
                                    chance = (float) Math.random();
                                    if (chance <= .33f) { // 33%
                                        status.get(enemy).addStatusEffect(attackUp3(2), enemy);
                                        status.get(enemy).addStatusEffect(guardUp2(2), enemy);
                                        status.get(enemy).addStatusEffect(speedUp2(2), enemy);
                                        status.get(enemy).addStatusEffect(toxic(2), enemy);
                                    } else if (chance > .33f && chance <= .66f) { // 33%
                                        status.get(enemy).addStatusEffect(unstable(2), enemy);
                                    } else if (chance > .66f && chance <= .72f) { // 6%
                                        status.get(enemy).addStatusEffect(exhausted(2), enemy);
                                    } else if (chance > .72f && chance <= .86f) { // 14%
                                        status.get(enemy).addStatusEffect(berserk(2), enemy);
                                    } else if (chance > .86f && chance <= .88f) { // 3%
                                        status.get(enemy).addStatusEffect(unstable(2), enemy);
                                        status.get(enemy).addStatusEffect(supercharged(2), enemy);
                                    } else if (chance > .88f && chance < .9f) { // 1%
                                        status.get(enemy).addStatusEffect(supercharged(2), enemy);
                                        status.get(enemy).addStatusEffect(berserk(2), enemy);
                                        status.get(enemy).addStatusEffect(attackUp3(2), enemy);
                                        status.get(enemy).addStatusEffect(speedUp(2), enemy);
                                        status.get(enemy).addStatusEffect(unstable(2), enemy);
                                    } else { //10%
                                        status.get(enemy).addStatusEffect(burn(2), enemy);
                                        status.get(enemy).addStatusEffect(curse(2), enemy);
                                    }
                                }
                            }
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        booms, sparkOut, booms.copy(), sparkOut.copy(), booms.copy(), sparkOut.copy(), booms.copy(), sparkOut.copy(), sludge
                })), new MoveInfo(false, 1,
                (entity, userEntity) -> {
                    if (entity.acceptsStatusEffects) { //add only guaranteed boost
                        entity.statusEffectInfos.add(poison(2).createStatusEffectInfo());
                    }
                    entity.arbitraryValue += MathUtils.random(-200, 200); //random status effect chance
            }
        ));
        move.setAttackDescription("Mixes toxins to create a highly unpredictable outcome. Deals regular damage and has a chance to inflict " +
        "Poison and a random combination of status effects on the target for 2 turns.");
        return move;
    }

    public static Move berserkBite(Entity user) {
        VisualEvent booms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity img = new Entity();
                img.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                img.add(new LifetimeComponent(0, .07f));
                img.add(new SpriteComponent(atlas.createSprite("boom")));
                if (MathUtils.randomBoolean())
                    sm.get(img).sprite.setColor(Color.RED);
                else sm.get(img).sprite.setColor(Color.BLUE);
                engine.addEntity(img);
            }
        }, .02f, 30);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(MathUtils.random(-18, 18) * scale, MathUtils.random(-18, 18) * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("zigzag"));
                sprite.setOriginCenter();
                sprite.setColor(Color.BLACK);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .07f, 13);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(Color.RED);
                else
                    sprite.setColor(Color.BLUE);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        Move move = new Move("Berserk Bite", nm.get(user).name + " bit the opponent with an aggravating toxin!", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);
                        status.get(enemy).addStatusEffect(berserk(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{booms, bubble, sludge})), new MoveInfo(false, 1, berserk(2).createStatusEffectInfo()));
        move.setAttackDescription("Mixes toxins to create a virulent acid. Deals regular damage and has a chance to inflict Berserk for 2 turns, which " +
                "drastically raises attack but halves health and causes gradual damage.");
        return move;
    }
    //endregion

    //region survival Moves
    //misc
    public static Move slam(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent bam = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("boom"));
                bamSprite.setOriginCenter();
                bamSprite.setColor(Color.GRAY);
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .2f, 1);

        Move move = new Move("Slam", nm.get(user).name + " slammed into the opponent", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{explode, bam})), new MoveInfo(false, 1));
        move.setAttackDescription("Slams into the opponent dealing regular damage.");
        return move;
    }

    public static Move heavySlam(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent bam = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .4f));
                Sprite bamSprite = new Sprite(atlas.findRegion("boom"));
                bamSprite.setOriginCenter();
                bamSprite.setColor(Color.GRAY);
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(8)));

                engine.addEntity(bam);
            }
        }, .2f, 1);

        VisualEvent sphereOut = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);
                Vector2 tileCenter = tilePosition.cpy().add(new Vector2(50 * scale, 50 * scale));

                Entity circ = new Entity();
                tilePosition.add(MathUtils.random(-30, 30) * scale, (MathUtils.random(-30, 30) * scale));
                circ.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(circ).getCenter().y),
                        tileCenter.x - (pm.get(circ).getCenter().x));

                circ.add(new MovementComponent(new Vector2(50 * scale, 0)));
                mm.get(circ).movement.setAngle(directionTowardsCenter + 180);
                circ.add(new LifetimeComponent(0, .5f));
                Sprite sprite = new Sprite(atlas.findRegion("circle"));
                sprite.setOriginCenter();
                circ.add(new SpriteComponent(sprite));

                circ.add(new EventComponent(.01f, true, EventCompUtil.fadeOut(50)));

                engine.addEntity(circ);
            }
        }, .01f, 6);

        Move move = new Move("Heavy Slam", nm.get(user).name + " slammed into the opponent", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{sphereOut, explode, bam})), new MoveInfo(false, 2));
        move.setAttackDescription("Slams into the opponent with great force. Deals 2x damage.");
        return move;
    }

    public static Move claw(Entity user) {
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
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);

                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

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

        Move move = new Move("Claw", nm.get(user).name + " slashed its claws!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{claw})), new MoveInfo(false, 1));
        move.setAttackDescription("Slashes at the opponent dealing regular damage.");
        return move;
    }

    public static Move blueClaw(Entity user) {
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
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);

                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity claw = new Entity();
                claw.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                claw.add(new LifetimeComponent(0, .21f));
                claw.add(new AnimationComponent(.05f,new TextureRegion[] {
                        atlas.findRegion("claw"),
                        atlas.findRegion("claw2"),
                        atlas.findRegion("claw3"),
                        atlas.findRegion("claw4"),
                        atlas.findRegion("claw5")},
                        Color.CYAN,
                        Animation.PlayMode.NORMAL));
                engine.addEntity(claw);
            }
        }, .21f, 1);

        Move move = new Move("Claw", nm.get(user).name + " slashed its claws!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{claw})), new MoveInfo(false, 1));
        move.setAttackDescription("Slashes at the opponent dealing regular damage.");
        return move;
    }

    public static Move monoplodeOrb(Entity user) {
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
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(350 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                Sprite glowSprite = atlas.createSprite("circle");
                glowSprite.setColor(new Color(.1f, .1f, .1f, 0f));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.05f, true, EventCompUtil.fadeIn(6)));

                engine.addEntity(glow);
            }

        }, .04f, 25);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Color.BLACK,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        //Move
        Move move = new Move("Monoplode+", nm.get(user).name + " uses a defense piercing spell!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);

                        if (status.has(e))
                            status.get(e).addStatusEffect(defenseless(1), e);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles, explode, explodeBig})), new MoveInfo(true, 1));
        move.setAttackDescription("Attacks the target with dangerous magic orbs. Ignores the opponents defense and deals regular damage. Reduces the user's" +
        " defense to 0 for 1 turn.");
        return move;
    }
    //---Tower Waves (from top of entity)
    public static Move powerWave(Entity user) {
        VisualEvent wave = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f + 25 * scale);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .27f));
                boom.add(new AnimationComponent(.06f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.ORANGE,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.03f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().lerp(.4f, .9f, 1, 1, .1f);
                }));
                engine.addEntity(boom);
            }
        }, .4f, 3);
        
        Move move = new Move("Power Wave", "The " + nm.get(user).name + " emitted a strengthening wave!", user, 2,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                        new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                        new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(attackUp(2), enemy);
                            status.get(enemy).addStatusEffect(speedUp(2), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                        new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                        new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
                }),
                new Array<VisualEvent>(new VisualEvent[]{wave})),
                new MoveInfo(false, 0, new StatusEffectInfo[]{attackUp(2).createStatusEffectInfo(), speedUp(2).createStatusEffectInfo()}, (entity, userEntity) -> entity.arbitraryValue += 30));
        move.setAttackDescription("Emits an invigorating energy wave. Increases the target's attack and speed for 2 turns.");
        return move;
    }

    public static Move weakenWave(Entity user) {
        VisualEvent wave = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f + 25 * scale);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .27f));
                boom.add(new AnimationComponent(.06f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.CYAN,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.03f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().lerp(1, 0, 0, 1, .1f);
                }));
                engine.addEntity(boom);
            }
        }, .4f, 3);

        Move move = new Move("Weaken Wave", "The " + nm.get(user).name + " emitted a weakening wave!", user, 2,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                        new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                        new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(offenseless(3), enemy);
                            status.get(enemy).addStatusEffect(paralyze(1), enemy);
                            status.get(enemy).addStatusEffect(defenseless(2), enemy);

                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
        }),
                new Array<VisualEvent>(new VisualEvent[]{wave})), new MoveInfo(false, 0,
                        offenseless(3).createStatusEffectInfo(), paralyze(3).createStatusEffectInfo(), defenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Emits a weakening wave. Reduces the target's attack for 3 turns, speed for 1 turn, and defense for 2 turns.");
        return move;
    }

    public static Move shieldWave(Entity user) {
        VisualEvent wave = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f + 25 * scale);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .27f));
                boom.add(new AnimationComponent(.06f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.BLUE,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.03f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().lerp(1, 1, 1, 1, .1f);
                }));
                engine.addEntity(boom);
            }
        }, .4f, 3);

        Move move = new Move("Shield Wave", "The " + nm.get(user).name + " emitted a defense boosting wave!", user, 2,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                        new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                        new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(guardUp(2), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
        }),
                new Array<VisualEvent>(new VisualEvent[]{wave})), new MoveInfo(false, 0, guardUp(2).createStatusEffectInfo()));
        move.setAttackDescription("Emits a protective wave, increasing the target's defense for 2 turns.");
        return move;
    }
    //waves coming out of center of entity --
    public static Move warWave(Entity user) {
        VisualEvent wave = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .27f));
                boom.add(new AnimationComponent(.06f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.RED,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.03f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().lerp(0, 0, 0, 1, .1f);
                }));
                engine.addEntity(boom);
            }
        }, .4f, 3);

        VisualEvent smoke = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity e = new Entity();
                    e.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                            , entitySize.x, entitySize.y, 0));
                    e.add(new LifetimeComponent(0, 1.4f));
                    e.add(new MovementComponent(new Vector2(0, MathUtils.random(10, 50) * scale)));
                    Sprite s;
                    if (MathUtils.randomBoolean())
                        s = atlas.createSprite("cloud");
                    else
                        s = atlas.createSprite("cloud2");
                    s.setOriginCenter();
                    s.setColor(new Color(.1f, .1f, .1f, .5f));
                    e.add(new SpriteComponent(s));
                    e.add(new EventComponent(.05f, true, new GameEvent() {
                        boolean direction = MathUtils.randomBoolean();
                        @Override
                        public void event(Entity e, Engine engine) {
                            if (direction)
                                pm.get(e).rotation += MathUtils.random(5, 10);
                            else
                                pm.get(e).rotation -= MathUtils.random(5, 10);
                            mm.get(e).movement.scl(1.03f);
                            sm.get(e).sprite.setColor(sm.get(e).sprite.getColor().lerp(new Color(0, 0, 0, 0), .08f));
                        }
                    }));
                    engine.addEntity(e);
                }
            }
        }, .2f, 6);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(170 * scale, 170 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .36f));
                    boom.add(new AnimationComponent(.07f,
                            new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                    atlas.findRegion("diamondBoom2"),
                                    atlas.findRegion("diamondBoom3"),
                                    atlas.findRegion("diamondBoom4"),
                                    atlas.findRegion("diamondBoom5"),
                                    atlas.findRegion("diamondBoom6")},
                            Color.RED,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.07f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 1);

        Move move = new Move("War Wave", "The " + nm.get(user).name + " emitted a maddening wave!", user, 1,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                        new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                        new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(berserk(10), enemy);
                        }

                        if (status.has(e) && !status.get(e).contains("Defenseless"))
                            status.get(e).addStatusEffect(defenseless(1), e);

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
        }),
                new Array<VisualEvent>(new VisualEvent[]{wave, explode, smoke})), new MoveInfo(false, 0, new StatusEffectInfo[] {berserk(10).createStatusEffectInfo()},
                (enemy, userEntity) -> {

                }));
        move.setAttackDescription("Emits a chaotic wave. Inflicts Berserk for 2 turns, which drastically raises attack but halves health and causes gradual damage. " +
                "Reduces the user's defense to 0 for 1 turn. ");
        return move;
    }

    public static Move peaceWave(Entity user) {
        VisualEvent wave = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .27f));
                boom.add(new AnimationComponent(.06f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.PINK,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.03f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().lerp(0, 1, 0, 1, .1f);
                }));
                engine.addEntity(boom);
            }
        }, .4f, 3);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity sparkle = new Entity();
                    sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                            entitySize.x, entitySize.y, 0));
                    sparkle.add(new LifetimeComponent(0, .3f));
                    sparkle.add(new MovementComponent(new Vector2(0, 40)));
                    Sprite sprite = atlas.createSprite("sparkle");
                    sprite.setOriginCenter();
                    sprite.setColor(Color.GREEN);
                    sparkle.add(new SpriteComponent(sprite));
                    sparkle.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(6)));

                    engine.addEntity(sparkle);
                }
            }
        }, .12f, 6);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .26f));
                    boom.add(new AnimationComponent(.05f,
                            new TextureRegion[]{atlas.findRegion("explodeGreen"),
                                    atlas.findRegion("explodeGreen2"),
                                    atlas.findRegion("explodeGreen3"),
                                    atlas.findRegion("explodeGreen4"),
                                    atlas.findRegion("explodeGreen5"),
                                    atlas.findRegion("explodeGreen6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 1);

        Move move = new Move("Peace Wave", "The " + nm.get(user).name + " emitted a peaceful wave!", user, 1,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                        new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                        new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(pacifist(3), enemy);
                        }

                        if (status.has(e) && !status.get(e).contains("Defenseless"))
                            status.get(e).addStatusEffect(defenseless(1), e);

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
        }),
                new Array<VisualEvent>(new VisualEvent[]{wave, sparkle, explode})), new MoveInfo(false, 0, pacifist(3).createStatusEffectInfo()));
        move.setAttackDescription("Emits a peaceful wave. Inflicts Pacifist for 3 turns, which reduces attack and defense to 0 and causes strong health regeneration. " +
                "Reduces the user's defense to 0 for 1 turn. ");
        return move;
    }

    public static Move worryWave(Entity user) {
        VisualEvent wave = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .27f));
                boom.add(new AnimationComponent(.06f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.CYAN,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.03f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().lerp(0, 0, 0, 1, .1f);
                }));
                engine.addEntity(boom);
            }
        }, .4f, 3);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .36f));
                    boom.add(new AnimationComponent(.07f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            Color.BLACK,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.07f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 1);

        Move move = new Move("Worry Wave", "The " + nm.get(user).name + " emitted an unsettling wave!", user, 1,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                        new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                        new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(restless(3), enemy);
                        }

                        if (status.has(e) && !status.get(e).contains("Defenseless"))
                            status.get(e).addStatusEffect(defenseless(1), e);

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
        }),
                new Array<VisualEvent>(new VisualEvent[]{wave, explode})), new MoveInfo(false, 0, new StatusEffectInfo[] {restless(3).createStatusEffectInfo()},
                (enemy, userEntity) -> {
                    enemy.arbitraryValue -= 60;
                }));
        move.setAttackDescription("Emits a unsettling wave. Inflicts Restless for 3 turns, which changes Max SP, attack, and defense to 0 but " +
                        "doubles speed. Reduces the user's defense to 0 for 1 turn. ");
        return move;
    }

    //boss
    //fire spirit
    public static Move fireSlash(Entity user) {
        VisualEvent slash = new VisualEvent(new VisualEffect() {
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
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.06f,
                        new TextureRegion[]{atlas.findRegion("blueslash1"),
                                atlas.findRegion("blueslash2"),
                                atlas.findRegion("blueslash3"),
                                atlas.findRegion("blueslash4")},
                        Color.RED,
                        Animation.PlayMode.NORMAL));
                //boom.add(new EventComponent(.07f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Color.WHITE,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent fire = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .5f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("flame"),
                                atlas.findRegion("flame2"),
                                atlas.findRegion("flame3")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(10)));
                engine.addEntity(boom);
            }
        }, .05f, 12);

        Move move = new Move("Slash", nm.get(user).name + " slashed the opponent", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{explode, slash, fire})), new MoveInfo(false, 1));
        move.setAttackDescription("Slashes the opponent with a fiery scythe. Deals regular damage.");
        return move;
    }

    public static Move chainFire(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .2f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("explode"),
                                    atlas.findRegion("explode2"),
                                    atlas.findRegion("explode3"),
                                    atlas.findRegion("explode4"),
                                    atlas.findRegion("explode5"),
                                    atlas.findRegion("explode6")},
                            Color.WHITE,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                    engine.addEntity(boom);
                }
            }
        }, .1f, 1);

        VisualEvent fire = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                            , entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .5f));
                    boom.add(new AnimationComponent(.02f,
                            new TextureRegion[]{atlas.findRegion("flame"),
                                    atlas.findRegion("flame2"),
                                    atlas.findRegion("flame3")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(10)));
                    engine.addEntity(boom);
                }
            }
        }, .06f, 15);

        VisualEvent sparkles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(5 * scale, 5 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity fist = new Entity();
                    fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                            entitySize.x, entitySize.y, 0));
                    fist.add(new LifetimeComponent(0, .3f));
                    Sprite sprite = atlas.createSprite("sparkle");
                    sprite.setOriginCenter();
                    sprite.setColor(Color.ORANGE);
                    fist.add(new SpriteComponent(sprite));
                    fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                    engine.addEntity(fist);
                }
            }
        }, .03f, 10);

        Move move = new Move("Chain Fire", user, 2, new Array<BoardPosition>(
                new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0), new BoardPosition(-4, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * .5f - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(burn(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0), new BoardPosition(-4, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{fire, explode, sparkles})), new MoveInfo(false, .5f, burn(3).createStatusEffectInfo()));
        move.setAttackDescription("Lights a fire that spreads to several targets in front of the user. Deals 1/2x damage and burns the targets for 3 turns.");
        return move;
    }

    public static Move flameCharge(Entity user) {
        VisualEvent fire = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                            , entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .5f));
                    boom.add(new AnimationComponent(.02f,
                            new TextureRegion[]{atlas.findRegion("flame"),
                                    atlas.findRegion("flame2"),
                                    atlas.findRegion("flame3")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(10)));
                    engine.addEntity(boom);
                }
            }
        }, .03f, 5);

        VisualEvent smoke = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(40 * scale, 40 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity e = new Entity();
                    e.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                            , entitySize.x, entitySize.y, 0));
                    e.add(new LifetimeComponent(0, 1.4f));
                    e.add(new MovementComponent(new Vector2(0, MathUtils.random(40, 70) * scale)));
                    Sprite s;
                    if (MathUtils.randomBoolean())
                        s = atlas.createSprite("cloud");
                    else
                        s = atlas.createSprite("cloud2");
                    s.setOriginCenter();
                    s.setColor(new Color(.3f, .3f, .3f, .5f));
                    e.add(new SpriteComponent(s));
                    e.add(new EventComponent(.05f, true, new GameEvent() {
                        boolean direction = MathUtils.randomBoolean();
                        @Override
                        public void event(Entity e, Engine engine) {
                            if (direction)
                                pm.get(e).rotation += MathUtils.random(5, 10);
                            else
                                pm.get(e).rotation -= MathUtils.random(5, 10);
                            mm.get(e).movement.scl(1.03f);
                            sm.get(e).sprite.setColor(sm.get(e).sprite.getColor().lerp(new Color(0, 0, 0, 0), .08f));
                        }
                    }));
                    engine.addEntity(e);
                }
            }
        }, .03f, 5);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent smokeOut = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(35 * scale, 35 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity e = new Entity();
                e.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-100 * scale, 100 * scale), MathUtils.random(-100 * scale, 100 * scale))
                        , entitySize.x, entitySize.y, 0));
                e.add(new LifetimeComponent(0, .5f));
                Sprite s;
                if (MathUtils.randomBoolean())
                    s = atlas.createSprite("cloud");
                else
                    s = atlas.createSprite("cloud2");
                s.setOriginCenter();
                s.setColor(new Color(.6f, .6f, .6f, .5f));
                e.add(new SpriteComponent(s));
                e.add(new EventComponent(.05f, true, (engine, entity) -> {
                    sm.get(e).sprite.setColor(sm.get(e).sprite.getColor().lerp(new Color(0, 0, 0, 0), .08f));
                    pm.get(e).rotation -= 5;
                }));
                engine.addEntity(e);
            }
        }, .01f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Flame Charge", nm.get(user).name + " began heating up!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).addStatusEffect(speedUp(2), e);
                            status.get(e).addStatusEffect(attackUp(2), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        changeToBlack, fire, smoke, fire.copy(), smoke.copy(), fire.copy(), smoke.copy(), fire.copy(), smoke.copy(.01f, 15),
                        explode, smokeOut, returnToNormal
                })),
                new MoveInfo(false, 0, speedUp(2).createStatusEffectInfo(), attackUp(2).createStatusEffectInfo()));
        move.setAttackDescription("The user stokes its inner fire, increasing the user's attack and speed for 2 turns.");
        return move;
    }

    public static Move blueFlame(Entity user) {
        //Visuals---
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Color.WHITE,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .15f, 1);

        VisualEvent explodeLargest = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(400 * scale, 400 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .15f, 1);

        VisualEvent fire = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .5f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("flame"),
                                atlas.findRegion("flame2"),
                                atlas.findRegion("flame3")},
                        new Color(.3f, .3f, 1, 1),
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(10)));
                engine.addEntity(boom);
            }
        }, .04f, 14);

        VisualEvent smallBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                        entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("boom");
                sprite.setOriginCenter();
                sprite.setColor(Color.BLUE);
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(fist);
            }
        }, .03f, 20);

        VisualEvent largerRadiusBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-150 * scale, 150 * scale), MathUtils.random(-150 * scale, 150 * scale)),
                        entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("boom");
                sprite.setOriginCenter();
                sprite.setColor(Color.BLUE);
                boom.add(new SpriteComponent(sprite));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(boom);
            }
        }, .005f, 40);

        VisualEvent smoke = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(40 * scale, 40 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity e = new Entity();
                    e.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                            , entitySize.x, entitySize.y, 0));
                    e.add(new LifetimeComponent(0, 1.4f));
                    e.add(new MovementComponent(new Vector2(0, MathUtils.random(40, 70) * scale)));
                    Sprite s;
                    if (MathUtils.randomBoolean())
                        s = atlas.createSprite("cloud");
                    else
                        s = atlas.createSprite("cloud2");
                    s.setOriginCenter();
                    s.setColor(new Color(.2f, .2f, .2f, .6f));
                    e.add(new SpriteComponent(s));
                    e.add(new EventComponent(.05f, true, new GameEvent() {
                        boolean direction = MathUtils.randomBoolean();
                        @Override
                        public void event(Entity e, Engine engine) {
                            if (direction)
                                pm.get(e).rotation += MathUtils.random(5, 10);
                            else
                                pm.get(e).rotation -= MathUtils.random(5, 10);
                            mm.get(e).movement.scl(1.03f);
                            sm.get(e).sprite.setColor(sm.get(e).sprite.getColor().lerp(new Color(0, 0, 0, 0), .08f));
                        }
                    }));
                    engine.addEntity(e);
                }
            }
        }, .01f, 5);

        VisualEvent miniExplosions = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(80 * scale, 80 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-130 * scale, 130 * scale), MathUtils.random(-130 * scale, 130 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .11f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .006f, 30);

        //Move
        Move move = new Move("Blue Flame", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 2 - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        fire, smoke.copy(.1f, 20), explode, smoke, smallBooms, smoke.copy(), explodeBig, smoke.copy(), explodeLargest,
                        miniExplosions, largerRadiusBooms.copy(.005f, 10), explodeLargest.copy(), miniExplosions.copy(), largerRadiusBooms.copy(.005f, 10),
                        explodeLargest.copy(), largerRadiusBooms, smoke.copy(.2f, 15)})), new MoveInfo(false, 2));
        move.setAttackDescription("Summons a ferocious blue flare to consume the target. Deals 2x damage.");
        return move;
    }
    //water spirit
    public static Move hammerStrike(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent bam = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("boom"));
                bamSprite.setOriginCenter();
                bamSprite.setColor(Color.GRAY);
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .01f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(.3f, .7f, 1, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .19f, 9);

        Move move = new Move("Hammer Strike", nm.get(user).name + " slammed the opponent with its hammer!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, bam, sparkle})), new MoveInfo(false, 1, defenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Slams the target with a hammer. Deals regular damage and reduces the target's defense to 0.");
        return move;
    }

    public static Move gather(Entity user) {
        VisualEvent waterBall = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(130 * scale, 130 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity waterBall = new Entity();
                waterBall.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                waterBall.add(new LifetimeComponent(0, 2.5f));
                Sprite water = new Sprite(atlas.findRegion("water"));
                water.setOriginCenter();
                water.setColor(1, 1, 1, 0);
                waterBall.add(new SpriteComponent(water));
                waterBall.add(new EventComponent(.1f, true, EventCompUtil.fadeInThenOut(5, 10, 5)));

                engine.addEntity(waterBall);
            }
        }, .2f, 1);

        VisualEvent particles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(30 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));
                glow.add(new LifetimeComponent(0, .3f));
                Sprite glowSprite = (MathUtils.randomBoolean())? atlas.createSprite("diamonds") : atlas.createSprite("openDiamonds");
                glowSprite.setColor(new Color(.3f, .8f, 1, 0));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.05f, true, (entity, engine) -> {
                    mm.get(entity).movement.scl(1.3f);
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().lerp(.3f, .4f, 1, 1, .1f));
                }));

                engine.addEntity(glow);
            }

        }, .04f, 35);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explodeGreen"),
                                atlas.findRegion("explodeGreen2"),
                                atlas.findRegion("explodeGreen3"),
                                atlas.findRegion("explodeGreen4"),
                                atlas.findRegion("explodeGreen5"),
                                atlas.findRegion("explodeGreen6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent innerBubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 30 * scale)));
                sparkle.add(new LifetimeComponent(0, .5f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(Color.BLUE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.025f, true, EventCompUtil.fadeOut(20)));

                engine.addEntity(sparkle);
            }
        }, .15f, 10);


        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Gather", nm.get(user).name + " began gathering water molecules to regenerate itself!", user, 7, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (stm.has(e))
                            stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 5, 0, stm.get(e).getModMaxHp(e));
                        if (status.has(e)) {
                            status.get(e).addStatusEffect(regeneration(3), e);
                            status.get(e).addStatusEffect(guardUp2(1), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        changeToBlack, waterBall, particles, innerBubble, returnToNormalGradual, returnToNormal, explode
                })),
                new MoveInfo(false, 0, new StatusEffectInfo[]{regeneration(3).createStatusEffectInfo(), guardUp2(1).createStatusEffectInfo()}, (enemy, userEntity) -> {
                    userEntity.hp = MathUtils.clamp(userEntity.hp + 5, 0, userEntity.maxHp);
                    if (userEntity.hp < 6) //encourage use of this move if low on health
                        userEntity.arbitraryValue += 300;
                }));
        move.setAttackDescription("Gathers water molecules to regenerate its body. Causes the user to regenerate health for 3 turns and increases the user's defense " +
        "for 1 turn.");
        return move;
    }

    public static Move strengthen(Entity user) {
        VisualEvent waterBall = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(130 * scale, 130 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity waterBall = new Entity();
                waterBall.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                waterBall.add(new LifetimeComponent(0, 2.5f));
                Sprite water = new Sprite(atlas.findRegion("water"));
                water.setOriginCenter();
                water.setColor(1, .8f, .9f, 0);
                waterBall.add(new SpriteComponent(water));
                waterBall.add(new EventComponent(.1f, true, EventCompUtil.fadeInThenOut(5, 10, 5)));

                engine.addEntity(waterBall);
            }
        }, .2f, 1);

        VisualEvent particles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(35 * scale, 35 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add(MathUtils.random(-150, 150), MathUtils.random(-150, 150)),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(30 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));
                glow.add(new LifetimeComponent(0, .3f));
                Sprite glowSprite = (MathUtils.randomBoolean()) ? atlas.createSprite("diamonds") : atlas.createSprite("openDiamonds");
                glowSprite.setColor(new Color(1, .1f, .3f, 0));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.05f, true, (entity, engine) -> {
                    mm.get(entity).movement.scl(1.3f);
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().lerp(.3f, .9f, 1, 1, .1f));
                }));

                engine.addEntity(glow);
            }

        }, .04f, 35);

        VisualEvent smoke = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(40 * scale, 40 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity e = new Entity();
                    e.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                            , entitySize.x, entitySize.y, 0));
                    e.add(new LifetimeComponent(0, 1.4f));
                    e.add(new MovementComponent(new Vector2(0, MathUtils.random(40, 70) * scale)));
                    Sprite s;
                    if (MathUtils.randomBoolean())
                        s = atlas.createSprite("cloud");
                    else
                        s = atlas.createSprite("cloud2");
                    s.setOriginCenter();
                    s.setColor(new Color(.3f, .3f, .3f, .5f));
                    e.add(new SpriteComponent(s));
                    e.add(new EventComponent(.05f, true, new GameEvent() {
                        boolean direction = MathUtils.randomBoolean();

                        @Override
                        public void event(Entity e, Engine engine) {
                            if (direction)
                                pm.get(e).rotation += MathUtils.random(5, 10);
                            else
                                pm.get(e).rotation -= MathUtils.random(5, 10);
                            mm.get(e).movement.scl(1.03f);
                            sm.get(e).sprite.setColor(sm.get(e).sprite.getColor().lerp(new Color(0, 0, 0, 0), .08f));
                        }
                    }));
                    engine.addEntity(e);
                }
            }
        }, .03f, 10);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Color.RED,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Strengthen", nm.get(user).name + " increased its attack power using water molecules!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (stm.has(e))
                            stm.get(e).atk++;
                        if (am.get(e).actor.getWidth() <= 95 * scale) {
                            am.get(e).actor.setSize(am.get(e).actor.getWidth() + 3 * scale, am.get(e).actor.getHeight() + 3 * scale);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        changeToBlack, waterBall, particles, smoke, explode, smoke.copy(), returnToNormalGradual, returnToNormal
                })),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                            //Uses it less when attack is higher
                            userEntity.arbitraryValue += 12 * MathUtils.clamp(10 - userEntity.attack, 1, 100);
                            userEntity.attack++;
                        }));
        move.setAttackDescription("Gathers water molecules in order to increase the user's size. Increases the user's attack stat by 1. ");
        return move;
    }

    public static Move KOStrike(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(130 * scale, 130 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent bam = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("boom"));
                bamSprite.setOriginCenter();
                bamSprite.setColor(Color.GRAY);
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .01f, 1);

        VisualEvent particles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(100 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                Sprite glowSprite = atlas.createSprite("openCircle");
                glowSprite.setColor(new Color(.3f, .8f, 1, 0));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.05f, true, (entity, engine) -> {
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().lerp(1, 1, 1, 1, .1f));
                }));

                engine.addEntity(glow);
            }

        }, .04f, 35);

        VisualEvent sparkles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity fist = new Entity();
                    fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-70 * scale, 70 * scale), MathUtils.random(-70 * scale, 70 * scale)),
                            entitySize.x, entitySize.y, 0));
                    fist.add(new LifetimeComponent(0, .3f));
                    Sprite sprite = atlas.createSprite("shine");
                    sprite.setOriginCenter();
                    fist.add(new SpriteComponent(sprite));
                    fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                    engine.addEntity(fist);
                }
            }
        }, .03f, 10);

        Move move = new Move("KO Strike", nm.get(user).name + " knocked the opponent out!", user, 7, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 10, 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{particles, explode, bam, sparkles})), new MoveInfo(true, 10));
        move.setAttackDescription("Slams the target with a devastating hammer strike. Deals 10x damage.");
        return move;
    }
    //thunder Spirit
    public static Move stab(Entity user) {
        VisualEvent stabbing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(50 * scale, 50 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-25 * scale, 25 * scale), MathUtils.random(-25 * scale, 25 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                boom.add(new AnimationComponent(.1f,
                        new TextureRegion[]{atlas.findRegion("pierce"),
                                atlas.findRegion("pierce2")},
                        Color.CYAN,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOutAfter(10, 5)));
                engine.addEntity(boom);
            }
        }, .06f, 8);

        Move move = new Move("Spear Stab", nm.get(user).name + " pierced defenses!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{stabbing})), new MoveInfo(true, 1));
        move.setAttackDescription("Stabs the target with an electric spear. Ignores defense and deals regular damage.");
        return move;
    }

    public static Move fluxWave(Entity user) {
        VisualEvent projectile = new VisualEvent(new VisualEffect() {
            Tile startTile;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(57 * scale, 27 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                if (direction == 90)
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else if (direction == 180)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.y / 2f,
                            boards.getTileHeight() / 2);
                else if (direction == 270)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y * 1.5f);

                Entity beam = new Entity();
                Sprite beamSprite = new Sprite(atlas.findRegion("arrowhead"));
                beamSprite.setOriginCenter();
                beamSprite.setColor(Color.YELLOW);
                beam.add(new SpriteComponent(beamSprite));
                beam.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90));
                beam.add(new MovementComponent(new Vector2(1300 * scale, 0).setAngle(direction)));
                beam.add(new LifetimeComponent(0, .16f));
                beam.add(new EventComponent(0.02f, true, EventCompUtil.fadeOutAfter(4, 4)));
                engine.addEntity(beam);
            }
        }, .19f, 1);

        VisualEvent rippleOut = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .28f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.CYAN,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .12f, 2);

        VisualEvent shocking = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .28f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("shock1"),
                                    atlas.findRegion("shock2"),
                                    atlas.findRegion("shock1"),
                                    atlas.findRegion("shock2"),
                                    atlas.findRegion("shock1"),
                                    atlas.findRegion("shockFinal")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .12f, 2);

        VisualEvent zags = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity fist = new Entity();
                    fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                            entitySize.x, entitySize.y, 0));
                    fist.add(new LifetimeComponent(0, .3f));
                    Sprite sprite = atlas.createSprite("zigzag");
                    sprite.setOriginCenter();
                    sprite.setColor(Color.CYAN);
                    fist.add(new SpriteComponent(sprite));
                    fist.add(new EventComponent(.05f, true, (entity, engine) -> {
                        pm.get(entity).position.add(MathUtils.random(-5, 5), MathUtils.random(-5, 5));
                    }));

                    engine.addEntity(fist);
                }
            }
        }, .03f, 15);

        Move move = new Move("Flux Wave", user, 1, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(0, -1), new BoardPosition(0, -2), new BoardPosition(-1, -2), new BoardPosition(1, -2), new BoardPosition(0, -3)
            }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(0, -1), new BoardPosition(0, -2), new BoardPosition(-1, -2), new BoardPosition(1, -2), new BoardPosition(0, -3)}),
                new Array<VisualEvent>(new VisualEvent[]{projectile, rippleOut.copy(), shocking.copy(), rippleOut, shocking, zags})),
                new MoveInfo(false, 1, (paralyze(3).createStatusEffectInfo())
        ));
        move.setAttackDescription("Zaps the area in front of the user with a jolt of electricity. Paralyzes for 3 turns and deals regular damage.");
        return move;
    }

    public static Move disrupt(Entity user) {
        VisualEvent wave = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(250 * scale, 250 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f + 25 * scale);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .27f));
                boom.add(new AnimationComponent(.06f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.PURPLE,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.03f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().lerp(0, 1, 1, 1, .1f);
                }));
                engine.addEntity(boom);
            }
        }, .4f, 3);

        VisualEvent zags = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity fist = new Entity();
                    fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                            entitySize.x, entitySize.y, 0));
                    fist.add(new LifetimeComponent(0, .3f));
                    Sprite sprite = atlas.createSprite("zigzag");
                    sprite.setOriginCenter();
                    sprite.setColor(Color.PURPLE);
                    fist.add(new SpriteComponent(sprite));
                    fist.add(new EventComponent(.05f, true, (entity, engine) -> {
                        pm.get(entity).position.add(MathUtils.random(-5, 5), MathUtils.random(-5, 5));
                    }));

                    engine.addEntity(fist);
                }
            }
        }, .06f, 50);

        Move move = new Move("Disrupt", nm.get(user).name + " emitted a disruptive wave!", user, 4,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                        new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                        new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy)) {
                            if (status.get(enemy).getTotalStatusEffects() > 0)
                                status.get(enemy).removeAll(enemy);
                            status.get(enemy).addStatusEffect(exhausted(3), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
        }),
                new Array<VisualEvent>(new VisualEvent[]{wave, zags})), new MoveInfo(false, 0, (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects) {
                        if (enemy.statusEffectInfos.size >= 1)
                            enemy.statusEffectInfos.clear();
                        enemy.statusEffectInfos.add(exhausted(3).createStatusEffectInfo());
                        //encourage use if hits multiple entities
                        if (enemy.statusEffectInfos.size >= 1)
                            enemy.arbitraryValue -= 60;
                    }
        }
        ));
        move.setAttackDescription("Emits a disruptive wave, removing all the target's status effects. Stops the target from " +
        "regenerating SP and reduces their stats.");
        return move;
    }

    public static Move polarize(Entity user) {

        VisualEvent charges = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new LifetimeComponent(0, .2f));
                Sprite sprite = new Sprite(atlas.findRegion("diamonds"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(.6f, 1, 1, 1));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(10)));

                engine.addEntity(sparkle);
            }
        }, .02f, 120);

        VisualEvent rippleOut = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .28f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.CYAN,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .12f, 2);

        VisualEvent shocking = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .28f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("shock1"),
                                    atlas.findRegion("shock2"),
                                    atlas.findRegion("shock1"),
                                    atlas.findRegion("shock2"),
                                    atlas.findRegion("shock1"),
                                    atlas.findRegion("shockFinal")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .12f, 2);

        VisualEvent zags = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity fist = new Entity();
                    fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                            entitySize.x, entitySize.y, 0));
                    fist.add(new LifetimeComponent(0, .3f));
                    Sprite sprite = atlas.createSprite("zigzag");
                    sprite.setOriginCenter();
                    sprite.setColor(Color.YELLOW);
                    fist.add(new SpriteComponent(sprite));
                    fist.add(new EventComponent(.05f, true, (entity, engine) -> {
                        pm.get(entity).position.add(MathUtils.random(-5, 5), MathUtils.random(-5, 5));
                    }));

                    engine.addEntity(fist);
                }
            }
        }, .03f, 30);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                progress = MathUtils.clamp(progress + .1f, 0, 1);
                am.get(user).actor.shade(am.get(user).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .075f, 12);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        Move move = new Move("Polarize", nm.get(user).name + "'s charges become polarized!", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).removeAll(e);
                            status.get(e).addStatusEffect(charged(3), e);
                            status.get(e).addStatusEffect(regenerationPlus(3), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, rippleOut, charges, rippleOut.copy(), shocking, zags, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    if (userEntity.acceptsStatusEffects) {
                        userEntity.statusEffectInfos.clear();
                        userEntity.statusEffectInfos.add(charged(3).createStatusEffectInfo());
                        userEntity.statusEffectInfos.add(regenerationPlus(3).createStatusEffectInfo());
                    }
                })
        );
        move.setAttackDescription("Releases a large bolt of electricity, increasing its stats and causing the user to regenerate Health and SP for 3 turns.");
        return move;
    }
    //dragon
    public static Move dragonBreath2(Entity user) {
        VisualEvent breath = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                offset = MathUtils.random(-60, 60);

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(60 * scale, 60 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));

                startTilePosition.add(boards.getTileWidth() / 2 - 25 * scale,
                        boards.getTileHeight() / 2 - 50 * scale);

                Entity clouds = new Entity();
                clouds.add(new AnimationComponent(.1f, new TextureRegion[]{
                        atlas.findRegion("cloud"), atlas.findRegion("cloud2")
                }, Animation.PlayMode.LOOP));
                if (MathUtils.randomBoolean(.3f))
                    animm.get(clouds).shadeColor = new Color(1f, .6f, .5f, 1);
                else if (MathUtils.randomBoolean(.3f))
                    animm.get(clouds).shadeColor = new Color(1f, .2f, .1f, 1);
                else
                    animm.get(clouds).shadeColor = new Color(1f, .1f, .2f, 1);

                clouds.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                clouds.add(new MovementComponent(new Vector2(MathUtils.random(250, 550) * scale, 0).setAngle(direction + offset)));
                clouds.add(new LifetimeComponent(0, .52f));
                clouds.add(new EventComponent(0.02f, true, new GameEvent() {
                    private int timesCalled;
                    @Override
                    public void event(Entity e, Engine engine) {
                        timesCalled++;
                        if (timesCalled >= 18) {
                            Color color = animm.get(e).shadeColor;
                            color = new Color(
                                    color.r,
                                    color.g,
                                    color.b,
                                    MathUtils.clamp(color.a - 1f / 26f, 0, 1));
                            animm.get(e).shadeColor = color;
                        }
                        pm.get(e).rotation += 9;
                    }
                }));
                engine.addEntity(clouds);
            }
        }, .02f, 50);

        VisualEvent fires = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(32 * scale, 32 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity flame = new Entity();
                    flame.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-40, 40), MathUtils.random(-40, 40)), entitySize.x, entitySize.y, 0));
                    flame.add(new LifetimeComponent(0, .4f));
                    flame.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("flame"),
                                    atlas.findRegion("flame2"),
                                    atlas.findRegion("flame3")},
                            Animation.PlayMode.NORMAL));
                    flame.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(flame);
                }
            }
        }, .06f, 10);

        Move move = new Move("Dragon Breath", nm.get(user).name + " spewed dragon breath!", user, 3,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, fires})), new MoveInfo(false, 1));
        move.setAttackDescription("Aggressively breathes on the target. Deals regular damage.");
        return move;
    }

    public static Move dragonRoar(Entity user) {
        VisualEvent breath = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset = -45;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                offset = MathUtils.random(-45, 45);

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(60 * scale, 60 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                startTilePosition.add(boards.getTileWidth() / 2 - 25 * scale,
                        boards.getTileHeight() / 2 - 50 * scale);

                Entity clouds = new Entity();
                clouds.add(new AnimationComponent(.1f, new TextureRegion[]{
                        atlas.findRegion("cloud"), atlas.findRegion("cloud2")
                }, Animation.PlayMode.LOOP));
                clouds.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                clouds.add(new MovementComponent(new Vector2(MathUtils.random(250, 450) * scale, 0).setAngle(direction + offset)));
                clouds.add(new LifetimeComponent(0, .92f));
                clouds.add(new EventComponent(0.02f, true, new GameEvent() {
                    private int timesCalled;
                    @Override
                    public void event(Entity e, Engine engine) {
                        timesCalled++;
                        if (timesCalled >= 16) {
                            Color color = animm.get(e).shadeColor;
                            color = new Color(
                                    color.r,
                                    color.g,
                                    color.b,
                                    MathUtils.clamp(color.a - 1f / 46f, 0, 1));
                            animm.get(e).shadeColor = color;
                        }
                        pm.get(e).rotation += 2;
                    }
                }));
                engine.addEntity(clouds);
            }
        }, .03f, 48);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(50 * scale, 50 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            new Color(1, 1, 1, .25f),
                            Animation.PlayMode.LOOP));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(20, 5)));
                    engine.addEntity(boom);
                }
            }
        }, .25f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .4f));
                    boom.add(new AnimationComponent(.022f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            new Color(1, 1, 1, .25f),
                            Animation.PlayMode.LOOP));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(5, 5)));
                    engine.addEntity(boom);
                }
            }
        }, .22f, 1);

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(110 * scale, 110 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.WHITE,
                            Animation.PlayMode.LOOP_REVERSED));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .12f, 2);

        VisualEvent sparks = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(26 * scale, 26 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity sparks = new Entity();
                    sparks.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-40, 40), MathUtils.random(-40, 40)), entitySize.x, entitySize.y, 0));
                    sparks.add(new LifetimeComponent(0, .4f));
                    Sprite s = atlas.createSprite("openDiamonds");
                    s.setOriginCenter();
                    s.setColor(ColorUtils.HSV_to_RGB(MathUtils.random(0, 360), 100, 100));
                    sparks.add(new SpriteComponent(s));
                    sparks.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(sparks);
                }
            }
        }, .05f, 15);

        Move move = new Move("Roar", nm.get(user).name + "'s roar scared the enemy!", user, 3,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                        new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(offenseless(2), enemy);
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);
                            status.get(enemy).addStatusEffect(shivers(2), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, explode, explodeBig, ripples, sparks})),
                new MoveInfo(false, 0, shivers(2).createStatusEffectInfo(), paralyze(3).createStatusEffectInfo(), offenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Roars loud enough to paralyze those nearby in fear. Paralyzes and inflicts the Shivers for 2 turns." +
                " Paralyzes the target for 3 turns." );
        return move;
    }

    public static Move flash(Entity user) {
        VisualEvent flash = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Vector2 entitySize = new Vector2(stage.getWidth(), stage.getHeight());
                Entity flash = new Entity();
                flash.add(new PositionComponent(new Vector2(0, 0), entitySize.x, entitySize.y, 0));
                flash.add(new LifetimeComponent(0, .25f));
                flash.add(new SpriteComponent(atlas.createSprite("LightTile")));
                sm.get(flash).sprite.setColor(Color.WHITE);
                flash.add(new EventComponent(.025f, true, EventCompUtil.fadeOutAfter(5, 5)));
                engine.addEntity(flash);
            }
        }, .01f, 1);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("explodeGreen"),
                                atlas.findRegion("explodeGreen2"),
                                atlas.findRegion("explodeGreen3"),
                                atlas.findRegion("explodeGreen4"),
                                atlas.findRegion("explodeGreen5"),
                                atlas.findRegion("explodeGreen6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .14f, 3);

        VisualEvent smallBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale)),
                        entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("openDiamonds");
                sprite.setOriginCenter();
                sprite.setColor(ColorUtils.HSV_to_RGB(MathUtils.random(0, 360), 100, 100));
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(fist);
            }
        }, .01f, 50);

        VisualEvent largerRadiusBooms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-150 * scale, 150 * scale), MathUtils.random(-150 * scale, 150 * scale)),
                        entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("diamonds");
                sprite.setOriginCenter();
                sprite.setColor(ColorUtils.HSV_to_RGB(MathUtils.random(0, 360), 100, 100));
                boom.add(new SpriteComponent(sprite));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(boom);
            }
        }, .004f, 40);

        VisualEvent sparkleOut = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(900 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, .3f));
                Sprite sprite = new Sprite(atlas.findRegion("diamonds"));
                sprite.setOriginCenter();
                sprite.setColor(ColorUtils.HSV_to_RGB(MathUtils.random(0, 360), 100, 100));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            spr.getColor().r,
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - 1f / 30f, 0, 1));
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 10);

        VisualEvent floatUpDiamonds = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(40, 40);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(new Vector2(50 + MathUtils.random(stage.getWidth() - 200), 25 + MathUtils.random(stage.getHeight() - 100)), entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("openDiamonds"));
                sprite.setOriginCenter();
                sprite.setColor(ColorUtils.HSV_to_RGB(MathUtils.random(0, 360), 100, 100));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .09f, 7);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10, 10);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(new Vector2(50 + MathUtils.random(stage.getWidth() - 200), 25 + MathUtils.random(stage.getHeight() - 100)), entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("sparkle"));
                sprite.setOriginCenter();
                sprite.setColor(Color.WHITE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .03f, 15);

        Move move = new Move("Spectral Flash", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(offenseless(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{sparkleOut, explode, smallBooms, flash, explodeBig, largerRadiusBooms, sparkle, floatUpDiamonds})),
                new MoveInfo(true, 1, offenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Releases a spark of pure energy. Ignores defense and lowers the target's attack for 2 turn.");
        return move;
    }

    public static Move dragonFluxwave(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 1);

        VisualEvent nothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
            }
        }, .2f, 1);

        VisualEvent explodePause = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .5f));
                boom.add(new AnimationComponent(.05f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, new GameEvent() {
                    float time;
                    @Override
                    public void event(Entity e, Engine engine) {
                        time += .03f;
                        if (time >= .17f) {
                            animm.get(e).shadeColor = animm.get(e).shadeColor.cpy().lerp(0, 0, 0, 0, .2f);
                        }
                    }
                }));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent explodePause2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .5f));
                boom.add(new AnimationComponent(.05f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, new GameEvent() {
                    float time;
                    @Override
                    public void event(Entity e, Engine engine) {
                        time += .03f;
                        if (time >= .17f) {
                            animm.get(e).shadeColor = animm.get(e).shadeColor.cpy().lerp(0, 0, 0, 0, .2f);
                        }
                    }
                }));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
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
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, 1f));
                boom.add(new AnimationComponent(.1f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        ColorUtils.HSV_to_RGB(MathUtils.random(0, 360), 100, 100),
                        Animation.PlayMode.LOOP_PINGPONG));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(5, 5)));
                engine.addEntity(boom);
            }
        }, .05f, 10);

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
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(350 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                Sprite glowSprite = atlas.createSprite("circle");
                glowSprite.setColor(Color.BLACK);
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.05f, true, new GameEvent() {
                    float progress;
                    @Override
                    public void event(Entity e, Engine engine) {
                        progress += .05f;
                        sm.get(e).sprite.setColor(sm.get(e).sprite.getColor().r + .17f, sm.get(e).sprite.getColor().r + .17f, sm.get(e).sprite.getColor().b + .19f, 1);
                        pm.get(e).position.add(MathUtils.random(-10, 10), MathUtils.random(-10, 10));
                    }
                }));

                engine.addEntity(glow);
            }

        }, .02f, 25);

        Move move = new Move("Fluxwave", nm.get(user).name + " caused major disturbances!", user, 5,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(0, -2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(curse(2), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(0, -2)
                }),
                new Array<VisualEvent>(new VisualEvent[]{
                        explode, nothing, explodePause, explodePause2, ripples, circles
                })), new MoveInfo(true, 1, curse(2).createStatusEffectInfo()));
        move.setAttackDescription("Releases negative energy near the target, making them feel weak. Ignores defense and inflicts Curse for 2 turn.");
        return move;
    }

    public static Move raze(Entity user) {
        VisualEvent flashRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(stage.getWidth(), stage.getHeight());
                Entity flash = new Entity();
                flash.add(new PositionComponent(new Vector2(0, 0), entitySize.x, entitySize.y, 0));
                flash.add(new LifetimeComponent(0, .25f));
                flash.add(new SpriteComponent(atlas.createSprite("LightTile")));
                sm.get(flash).sprite.setColor(Color.RED);
                flash.add(new EventComponent(.025f, true, EventCompUtil.fadeOutAfter(5, 5)));
                engine.addEntity(flash);
            }
        }, .1f, 1);

        VisualEvent firebreath = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset = -45;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                offset = MathUtils.random(-45, 45);

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(80 * scale, 60 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                startTilePosition.add(boards.getTileWidth() / 2 - 25 * scale,
                        boards.getTileHeight() / 2 - 50 * scale);

                Entity clouds = new Entity();
                Sprite s = atlas.createSprite("flame");
                s.setOriginCenter();
                //s.setColor(ColorUtils.HSV_to_RGB(MathUtils.random(0, 360), 100, 100));
                clouds.add(new SpriteComponent(s));
                clouds.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset + 180));
                clouds.add(new MovementComponent(new Vector2(MathUtils.random(450, 850) * scale, 0).setAngle(direction + offset)));
                clouds.add(new LifetimeComponent(0, .5f));
                clouds.add(new EventComponent(0.02f, true, EventCompUtil.fadeOut(25)));
                engine.addEntity(clouds);
            }
        }, .03f, 30);

        VisualEvent explosions = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(130 * scale, 130 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                            , entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .16f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("explode"),
                                    atlas.findRegion("explode2"),
                                    atlas.findRegion("explode3"),
                                    atlas.findRegion("explode4"),
                                    atlas.findRegion("explode5"),
                                    atlas.findRegion("explode6")},
                            Animation.PlayMode.LOOP_PINGPONG));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 2);

        VisualEvent sparks = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(26 * scale, 26 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity sparks = new Entity();
                    sparks.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-40, 40), MathUtils.random(-40, 40)), entitySize.x, entitySize.y, 0));
                    sparks.add(new LifetimeComponent(0, .4f));
                    Sprite s = atlas.createSprite("circle");
                    s.setOriginCenter();
                    s.setColor(Color.RED);
                    sparks.add(new SpriteComponent(s));
                    sparks.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(sparks);
                }
            }
        }, .05f, 15);

        Move move = new Move("Raze", nm.get(user).name + " unleashed a surge of energy!", user, 7,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                        new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(slowness(1), enemy);
                            status.get(enemy).addStatusEffect(offenseless(2), enemy);
                            status.get(enemy).addStatusEffect(shivers(3), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)}),
                new Array<VisualEvent>(new VisualEvent[]{flashRed, firebreath, explosions, sparks})),
                new MoveInfo(true, 1, slowness(1).createStatusEffectInfo(), offenseless(2).createStatusEffectInfo(), shivers(3).createStatusEffectInfo()));
        move.setAttackDescription("Attacks a large range with a surge of power. Ignores defense. Lowers the target's speed, attack and maximum SP for " +
                "1 turn, 2 turns, and 3 turns respectively.");
        return move;
    }

    //lions
    public static Move stoneGlare(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .2f, 1);

        Move move = new Move("Stone Glare", nm.get(user).name + " petrified the opponent with a glare!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(petrify(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, sparkle})), new MoveInfo(false, 0, petrify(2).createStatusEffectInfo()));
        move.setAttackDescription("Gives a stoic glare that would even petrify a statue. Inflicts Petrification for 2 turns.");
        return move;
    }

    public static Move roar(Entity user) {
        VisualEvent breath = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset = -45;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                offset = MathUtils.random(-45, 45);

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(30 * scale, 30 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                //startTilePosition.add(25 * scale, 25 * scale);

                Entity spark = new Entity();
                Sprite s = atlas.createSprite("openCircle");
                s.setOriginCenter();
                s.setColor(Color.YELLOW);
                spark.add(new SpriteComponent(s));
                spark.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                spark.add(new MovementComponent(new Vector2(MathUtils.random(350, 750) * scale, 0).setAngle(direction + offset)));
                spark.add(new LifetimeComponent(0, .52f));
                spark.add(new EventComponent(0.02f, true, (entity, engine) -> {
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().lerp(new Color(1, 0, 0, 0), .04f));
                    mm.get(entity).movement.scl(.97f);
                }));
                engine.addEntity(spark);
            }
        }, .03f, 28);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(50 * scale, 50 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            new Color(1, 1, 1, .25f),
                            Animation.PlayMode.LOOP));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(20, 5)));
                    engine.addEntity(boom);
                }
            }
        }, .25f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .4f));
                    boom.add(new AnimationComponent(.022f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            new Color(1, 1, 1, .25f),
                            Animation.PlayMode.LOOP));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(5, 5)));
                    engine.addEntity(boom);
                }
            }
        }, .22f, 1);

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(110 * scale, 110 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.YELLOW,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .24f, 2);

        Move move = new Move("Roar", nm.get(user).name + " roared!", user, 4,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                        new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(offenseless(3), enemy);
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{  new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, explode, explodeBig, ripples})), new MoveInfo(false, 0, offenseless(3).createStatusEffectInfo(), paralyze(3).createStatusEffectInfo()));
        move.setAttackDescription("Roars loudly and proudly. Lowers the attack and Paralyzes targets for 3 turns.");
        return move;
    }

    public static Move prepare(Entity user) {

        VisualEvent shuffleBackForth = new VisualEvent(new VisualEffect() {
            boolean right;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                if (right)
                    am.get(user).actor.moveBy(7, 0);
                else
                    am.get(user).actor.moveBy(-7, 0);
                right = !right;
            }
        }, .05f, 10);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(130 * scale, 130 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .26f));
                boom.add(new AnimationComponent(.05f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Prepare", nm.get(user).name + " readied its body to pounce!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {

                        if (status.has(e))
                            status.get(e).addStatusEffect(speedUp(2), e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, shuffleBackForth, explode,  returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, speedUp(2).createStatusEffectInfo()));
        move.setAttackDescription("Prepares to pounce. Increases the user's speed for 2 turns.");
        return move;
    }

    public static Move ready(Entity user) {
        VisualEvent shuffleBackForth = new VisualEvent(new VisualEffect() {
            boolean right;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                if (right)
                    am.get(user).actor.moveBy(7, 0);
                else
                    am.get(user).actor.moveBy(-7, 0);
                right = !right;
            }
        }, .06f, 16);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(170 * scale, 170 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .26f));
                boom.add(new AnimationComponent(.05f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Ready", nm.get(user).name + " readied its body to attack!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {

                        if (status.has(e)) {
                            status.get(e).addStatusEffect(speedUp(2), e);
                            status.get(e).addStatusEffect(attackUp(2), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, shuffleBackForth, shuffleBackForth.copy(.01f, 30), explode,  returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, speedUp(2).createStatusEffectInfo(), attackUp(2).createStatusEffectInfo()));
        move.setAttackDescription("Readies itself to attack. Increases the user's speed and attack for 2 turns.");
        return move;
    }

    public static Move neoRoar(Entity user) {
        VisualEvent breath = new VisualEvent(new VisualEffect() {
            Tile startTile;
            float offset = -45;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;
                //spraying code --
                offset = MathUtils.random(-45, 45);

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(30 * scale, 30 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));

                Entity spark = new Entity();
                Sprite s = atlas.createSprite("openCircle");
                s.setOriginCenter();
                s.setColor(Color.BLUE);
                spark.add(new SpriteComponent(s));
                spark.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                spark.add(new MovementComponent(new Vector2(MathUtils.random(350, 750) * scale, 0).setAngle(direction + offset)));
                spark.add(new LifetimeComponent(0, .52f));
                spark.add(new EventComponent(0.02f, true, (entity, engine) -> {
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().lerp(new Color(1, 0, 0, 0.3f), .06f));
                    mm.get(entity).movement.scl(.97f);
                }));
                engine.addEntity(spark);
            }
        }, .03f, 28);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(50 * scale, 50 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            new Color(1, 1, 1, .25f),
                            Animation.PlayMode.LOOP));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(20, 5)));
                    engine.addEntity(boom);
                }
            }
        }, .25f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .4f));
                    boom.add(new AnimationComponent(.022f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            new Color(1, 1, 1, .25f),
                            Animation.PlayMode.LOOP));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(5, 5)));
                    engine.addEntity(boom);
                }
            }
        }, .22f, 1);

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(110 * scale, 110 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy(), entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .25f));
                    boom.add(new AnimationComponent(.03f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Color.BLUE,
                            Animation.PlayMode.LOOP_REVERSED));
                    boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .12f, 2);

        VisualEvent sparks = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 entitySize = new Vector2(26 * scale, 26 * scale);
                Vector2 tilePosition;

                for (BoardPosition bp : targetPositions) {
                    BoardPosition newPos = bp.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(newPos.r, newPos.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }

                    tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity sparks = new Entity();
                    sparks.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-40, 40), MathUtils.random(-40, 40)), entitySize.x, entitySize.y, 0));
                    sparks.add(new LifetimeComponent(0, .4f));
                    Sprite s = atlas.createSprite("diamonds");
                    s.setOriginCenter();
                    s.setColor(Color.YELLOW);
                    sparks.add(new SpriteComponent(s));
                    sparks.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(sparks);
                }
            }
        }, .1f, 7);

        Move move = new Move("Neo-Roar", nm.get(user).name + "'s roar electrified the air!", user, 4,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                        new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(defenseless(3), enemy);
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                        new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, explode, explodeBig, ripples, sparks})),
                new MoveInfo(false, 0, defenseless(2).createStatusEffectInfo(), paralyze(3).createStatusEffectInfo()));
        move.setAttackDescription("Releases an energetic roar that electrifies the air and strikes fear into those nearby." +
                " Lowers the defense and Paralyzes targets for 3 turns.");
        return move;
    }

    public static Move reflectionBeamLion(Entity user) {
        VisualEvent preBoom = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            Color.DARK_GRAY,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .2f, 1);

        VisualEvent explode1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(1).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(1).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode3 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(2).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle3 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(2).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode4 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(3).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle4 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(3).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        Move move = new Move("Reflect Beam", nm.get(user).name + " shot off a beam of reflected light!", user, 2, new Array<BoardPosition>(
                new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 0),
                        new BoardPosition(-3, 0),
                        new BoardPosition(-4, 0)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) / 2f - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0), new BoardPosition(-4, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{preBoom, explode1, sparkle1, explode2, sparkle2, explode3, sparkle3, explode4, sparkle4})), new MoveInfo(false, .5f));
        move.setAttackDescription("Uses light to attack targets far away. Deals 1/2x damage.");
        return move;
    }

    //scaleman
    public static Move reflectionBeam(Entity user) {
        VisualEvent preBoom = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            Color.DARK_GRAY,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .2f, 1);

        VisualEvent explode1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(1).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(1).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode3 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(2).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle3 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(2).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode4 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(3).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle4 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(3).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        Move move = new Move("Reflect Beam", nm.get(user).name + " shot off a beam of reflected light!", user, 0, new Array<BoardPosition>(
                new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 0),
                        new BoardPosition(-3, 0),
                        new BoardPosition(-4, 0)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0), new BoardPosition(-4, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{preBoom, explode1, sparkle1, explode2, sparkle2, explode3, sparkle3, explode4, sparkle4})), new MoveInfo(false, 1));
        move.setAttackDescription("Uses reflected light to attack targets in a straight line. Deals regular damage.");
        return move;
    }

    public static Move refractionBeam(Entity user) {
        VisualEvent preBoom = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            Color.DARK_GRAY,
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .2f, 1);

        VisualEvent explode1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setColor(Color.CYAN);
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(1).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(1).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setColor(Color.CYAN);
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode3 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(2).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeGreen"),
                                atlas.findRegion("explodeGreen2"),
                                atlas.findRegion("explodeGreen3"),
                                atlas.findRegion("explodeGreen4"),
                                atlas.findRegion("explodeGreen5"),
                                atlas.findRegion("explodeGreen6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle3 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(2).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setColor(Color.RED);
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode4 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(3).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explodeGreen"),
                                atlas.findRegion("explodeGreen2"),
                                atlas.findRegion("explodeGreen3"),
                                atlas.findRegion("explodeGreen4"),
                                atlas.findRegion("explodeGreen5"),
                                atlas.findRegion("explodeGreen6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle4 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(3).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setColor(Color.RED);
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        Move move = new Move("Refract Beam", nm.get(user).name + " shot off a beam of refracted light!", user, 0, new Array<BoardPosition>(
                new BoardPosition[]{
                        new BoardPosition(-1, -1),
                        new BoardPosition(-1, 1),
                        new BoardPosition(-2, -2),
                        new BoardPosition(-2, 2)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1), new BoardPosition(-1, 1), new BoardPosition(-2, -2), new BoardPosition(-2, 2)}),
                new Array<VisualEvent>(new VisualEvent[]{preBoom, explode1, sparkle1, explode2, sparkle2, explode3, sparkle3, explode4, sparkle4})), new MoveInfo(false, 1));
        move.setAttackDescription("Uses refracted light to attack targets in a diagonal. Deals regular damage.");
        return move;
    }

    //slimeman
    public static Move poisonPunch(Entity user) {
        VisualEvent explosions = new VisualEvent(new VisualEffect() {
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
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .11f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("explodeGreen"),
                                atlas.findRegion("explodeGreen2"),
                                atlas.findRegion("explodeGreen3"),
                                atlas.findRegion("explodeGreen4"),
                                atlas.findRegion("explodeGreen5"),
                                atlas.findRegion("explodeGreen6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .06f, 8);

        Move move = new Move("Poison Punch", nm.get(user).name + " attacks!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean())
                            status.get(enemy).addStatusEffect(poison(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explosions})), new MoveInfo(false, 1, (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects && MathUtils.randomBoolean())
                        enemy.statusEffectInfos.add(poison(2).createStatusEffectInfo());
        }));
        move.setAttackDescription("Punches the target with an unwashed hand. Deals regular damage, and has a 50% chance to poison.");
        return move;
    }

    public static Move immobilize(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Color.YELLOW,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent zag = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(35 * scale, 35 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition.add(MathUtils.random(-40, 40), MathUtils.random(-40, 40)), entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("zigzag");
                sprite.setOriginCenter();
                sprite.setColor(Color.ORANGE);
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(fist);
            }
        }, .01f, 5);

        VisualEvent doNothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
            }
        }, .2f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("shine"));
                sprite.setOriginCenter();
                sprite.setColor(Color.GREEN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 3)));

                engine.addEntity(sparkle);
            }
        }, .19f, 2);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 7 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(Color.GREEN);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .12f, 4);

        Move move = new Move("Immobilize", "The target was drenched in immobilizing goo!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{zag, sparkle, bubble, doNothing, explode})), new MoveInfo(false, 0, paralyze(3).createStatusEffectInfo()));
        move.setAttackDescription("Soaks the target in a immobilizing goo. Paralyzes for 3 turns.");
        return move;
    }

    public static Move stunPunch(Entity user) {
        VisualEvent explosions = new VisualEvent(new VisualEffect() {
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
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-35 * scale, 35 * scale), MathUtils.random(-35 * scale, 35 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .11f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                atlas.findRegion("explodeBlue2"),
                                atlas.findRegion("explodeBlue3"),
                                atlas.findRegion("explodeBlue4"),
                                atlas.findRegion("explodeBlue5"),
                                atlas.findRegion("explodeBlue6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .06f, 8);

        Move move = new Move("Stun Punch", nm.get(user).name + " attacks!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean())
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explosions})), new MoveInfo(false, 1, (enemy, userEntity) -> {
            if (enemy.acceptsStatusEffects && MathUtils.randomBoolean())
                enemy.statusEffectInfos.add(paralyze(3).createStatusEffectInfo());
        }));
        move.setAttackDescription("Punches the target with a fist seeped in an immobilizing goo. Deals regular damage and has a 50% chance to paralyze.");
        return move;
    }

    public static Move regenerate(Entity user) {
        VisualEvent sparkles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                directionTowardsCenter += 30; //offset
                Vector2 movementToCenter = new Vector2(220 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .8f));

                Sprite glowSprite = atlas.createSprite("sparkle");
                glowSprite.setColor(Color.CYAN);
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.0533f, true, (entity, engine) -> {
                    mm.get(entity).movement.rotate(-3);
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().add(-.04f, -.04f, -.04f, -6.666f));
                }));
                engine.addEntity(glow);
            }

        }, .02f, 90);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                progress = MathUtils.clamp(progress + .1f, 0, 1);
                am.get(user).actor.shade(am.get(user).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .025f, 12);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        Move move = new Move("Recover", nm.get(user).name + " began to regenerate.", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 2, 0, stm.get(e).getModMaxHp(e));

                        if (status.has(e))
                            status.get(e).addStatusEffect(regeneration(3), e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, sparkles, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, regeneration(3).createStatusEffectInfo(), (enemy, userEntity) -> {
                    enemy.hp += 2;
                }));
        move.setAttackDescription("Focuses the energy around itself to recover a large amount of health. Recovers 2 health points and " +
                "causes the user to regenerate health for 3 turns.");
        return move;
    }

    public static Move mysteryStrike(Entity user) {
        VisualEvent punch = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, 1f));
                Sprite bamSprite = new Sprite(atlas.findRegion("fist"));
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOutAfter(15, 5)));

                engine.addEntity(bam);
            }
        }, .5f, 1);

        VisualEvent explosions = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(40 * scale, 40 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .11f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        new Color(ColorUtils.HSV_to_RGB(MathUtils.random(0, 360), MathUtils.random(60, 100), 100)),
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .03f, 20);

        VisualEvent explosionsLargeRad = new VisualEvent(new VisualEffect() {
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
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-60 * scale, 60 * scale), MathUtils.random(-60 * scale, 60 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .11f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        new Color(ColorUtils.HSV_to_RGB(MathUtils.random(0, 360), MathUtils.random(60, 100), 100)),
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .02f, 20);

        Move move = new Move("Mystery Strike", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.4f)) {
                            if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(paralyze(3), enemy);
                            else if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(burn(3), enemy);
                            else if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(poison(2), enemy);
                            else if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(toxic(3), enemy);
                            else if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(curse(3), enemy);
                            else
                                status.get(enemy).addStatusEffect(petrify(3), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{punch, explosions, explosionsLargeRad})), new MoveInfo(false, 1, (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects) {
                        if (MathUtils.randomBoolean(33f))
                            enemy.statusEffectInfos.add(paralyze(3).createStatusEffectInfo());
                        else if (MathUtils.randomBoolean(33f))
                            enemy.statusEffectInfos.add(burn(3).createStatusEffectInfo());
                        else if (MathUtils.randomBoolean(33f))
                            enemy.statusEffectInfos.add(poison(2).createStatusEffectInfo());
                        else if (MathUtils.randomBoolean(33f))
                            enemy.statusEffectInfos.add(toxic(3).createStatusEffectInfo());
                        else if (MathUtils.randomBoolean(33f))
                            enemy.statusEffectInfos.add(curse(3).createStatusEffectInfo());
                        else
                            enemy.statusEffectInfos.add(petrify(3).createStatusEffectInfo());
                    }
        }));
        move.setAttackDescription("Punches the target with a hand covered in undiscovered viruses. Deals regular damage and has a chance to inflict" +
                "a random status effect.");
        return move;
    }

    public static Move accursedSludge(Entity user) {
        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 7 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("openDiamonds"));
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .06f, 4);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(0, 0, 0, .7f));
                else
                    sprite.setColor(new Color(.2f, .2f, .2f, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.first().copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        Move move = new Move("Accursed Sludge", "The target was cursed!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(curse(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sludge, explode})), new MoveInfo(false, 0, curse(3).createStatusEffectInfo()));
        move.setAttackDescription("Covers the target in a acidic taboo. Inflicts Curse for 3 turns.");
        return move;
    }

    //cam man
    public static Move sludgeThrow(Entity user) {
        VisualEvent booms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity img = new Entity();
                img.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                img.add(new LifetimeComponent(0, .07f));
                img.add(new SpriteComponent(atlas.createSprite("boom")));
                sm.get(img).sprite.setColor(Color.YELLOW);
                engine.addEntity(img);
            }
        }, .02f, 15);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 7 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(Color.GREEN);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .12f, 4);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(0, 1, 0, .7f));
                else
                    sprite.setColor(new Color(.3f, 1, .1f, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        Move move = new Move("Sludge Throw", nm.get(user).name + " threw sludge!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.333f))
                            status.get(enemy).addStatusEffect(poison(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{booms, bubble, sludge})), new MoveInfo(false, 1, (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects && MathUtils.randomBoolean(.333f))
                        enemy.statusEffectInfos.add(poison(2).createStatusEffectInfo());
                }));
        move.setAttackDescription("Throws unsafe trash at the target. Deals regular damage and has a 33% chance to Poison.");
        return move;
    }

    public static Move suppressAttack(Entity user) {
        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 7 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .06f, 4);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(1, 0, 0, .7f));
                else
                    sprite.setColor(new Color(1, .3f, .2f, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        VisualEvent ripple = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity ripple = new Entity();
                ripple.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                ripple.add(new LifetimeComponent(0, .24f));
                ripple.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.ORANGE.cpy(),
                        Animation.PlayMode.LOOP_PINGPONG));
                ripple.add(new EventComponent(.01f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().add(-.04f, 0, .04f, 0);
                }));
                engine.addEntity(ripple);
            }
        }, .01f, 1);

        Move move = new Move("Suppress", "The target's attack was lowered!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(offenseless(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sludge, ripple})), new MoveInfo(false, 0, offenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Uses smelly fumes to lower the target's will to fight. Lowers the target's attack for 2 turns.");
        return move;
    }

    public static Move suppressDefense(Entity user) {
        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 7 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(Color.BLUE);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .06f, 4);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(0, 0, 1, .7f));
                else
                    sprite.setColor(new Color(.5f, .5f, 1f, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        VisualEvent ripple = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity ripple = new Entity();
                ripple.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                ripple.add(new LifetimeComponent(0, .24f));
                ripple.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.GREEN.cpy(),
                        Animation.PlayMode.LOOP_PINGPONG));
                ripple.add(new EventComponent(.01f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().add(-.04f, 0, .04f, 0);
                }));
                engine.addEntity(ripple);
            }
        }, .01f, 1);

        Move move = new Move("Suppress", "The target's defense was lowered!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sludge, ripple})), new MoveInfo(false, 0, defenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Uses funky fumes to lower the target's will to defend itself. Lowers the target's defense for 2 turns.");
        return move;
    }

    public static Move sludgeThrow2(Entity user) {
        VisualEvent booms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity img = new Entity();
                img.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                img.add(new LifetimeComponent(0, .07f));
                img.add(new SpriteComponent(atlas.createSprite("boom")));
                sm.get(img).sprite.setColor(Color.RED);
                engine.addEntity(img);
            }
        }, .02f, 15);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 7 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(Color.BROWN);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .12f, 4);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(1, .8f, 0, .7f));
                else
                    sprite.setColor(new Color(1, .2f, .1f, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        Move move = new Move("Sludge Throw", nm.get(user).name + " threw sludge!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.333f)) {
                            if (MathUtils.randomBoolean())
                                status.get(enemy).addStatusEffect(poison(2), enemy);
                            else
                                status.get(enemy).addStatusEffect(paralyze(3), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{booms, bubble, sludge})), new MoveInfo(false, 1, (enemy, userEntity) -> {
            if (enemy.acceptsStatusEffects && MathUtils.randomBoolean(.333f)) {
                if (MathUtils.randomBoolean())
                    enemy.statusEffectInfos.add(poison(2).createStatusEffectInfo());
                else
                    enemy.statusEffectInfos.add(paralyze(3).createStatusEffectInfo());
            }
        }));
        move.setAttackDescription("Throws dangerous trash at the target. Deals regular damage and has a 33% chance to Poison or Paralyze.");
        return move;
    }

    public static Move toxicThrow(Entity user) {
        VisualEvent booms = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity img = new Entity();
                img.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                img.add(new LifetimeComponent(0, .07f));
                img.add(new SpriteComponent(atlas.createSprite("boom")));
                sm.get(img).sprite.setColor(Color.GREEN);
                engine.addEntity(img);
            }
        }, .02f, 15);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(MathUtils.random(-18, 18) * scale, MathUtils.random(-18, 18) * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("spiral"));
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .12f, 6);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(1, .8f, 0, .7f));
                else
                    sprite.setColor(new Color(1, .2f, .1f, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        Move move = new Move("Toxic Throw", nm.get(user).name + " threw toxic waste!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.666f)) {
                            if (MathUtils.randomBoolean(.4f))
                                status.get(enemy).addStatusEffect(toxic(3), enemy);
                            else
                                status.get(enemy).addStatusEffect(poison(2), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{booms, bubble, sludge})), new MoveInfo(false, 1, (enemy, userEntity) -> {
            if (enemy.acceptsStatusEffects && MathUtils.randomBoolean(.666f)) {
                if (MathUtils.randomBoolean(.4f))
                    enemy.statusEffectInfos.add(toxic(3).createStatusEffectInfo());
                else
                    enemy.statusEffectInfos.add(poison(2).createStatusEffectInfo());
            }
        }));
        move.setAttackDescription("Throws a poisonous sludge at the target. Deals regular damage and has a 66% chance to inflict Poison or Toxic.");
        return move;
    }

    public static Move suppressMove(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 7 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(Color.BLACK);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .12f, 4);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(0, 0, 0, .7f));
                else
                    sprite.setColor(new Color(.1f, .1f, .1f, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        Move move = new Move("Suppress", "A move is now unusable!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (mvm.get(enemy).moveList.size > 1)
                            mvm.get(enemy).moveList.removeIndex(MathUtils.random(0, mvm.get(enemy).moveList.size - 1));

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, bubble, sludge, explodeBig})), new MoveInfo(false, 0, (enemy, userEntity) -> enemy.arbitraryValue -= 50));
        move.setAttackDescription("Uses unforgettable fumes to block out specific memories. Removes 1 of the target's moves permanently at random.");
        return move;
    }

    public static Move medicalThrow(Entity user) {
        VisualEvent bubble = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, 7 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("bubble"));
                sprite.setOriginCenter();
                sprite.setColor(Color.PINK);
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .06f, 4);

        VisualEvent shine = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity entity = new Entity();
                entity.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                entity.add(new MovementComponent(new Vector2(0, 13 * scale)));
                entity.add(new LifetimeComponent(0, 1f));
                Sprite sprite = new Sprite(atlas.findRegion("sparkle"));
                sprite.setOriginCenter();
                sprite.setColor(Color.WHITE);
                entity.add(new SpriteComponent(sprite));
                entity.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(entity);
            }
        }, .06f, 4);

        VisualEvent sludge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity bubble = new Entity();
                bubble.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                bubble.add(new MovementComponent(new Vector2(0, -4 * scale)));
                bubble.add(new LifetimeComponent(0, 1f));
                Sprite sprite;
                if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat"));
                else if (MathUtils.randomBoolean())
                    sprite = new Sprite(atlas.findRegion("splat2"));
                else
                    sprite = new Sprite(atlas.findRegion("splat3"));
                sprite.setOriginCenter();
                if (MathUtils.randomBoolean())
                    sprite.setColor(new Color(1, 0.9f, 1, .7f));
                else
                    sprite.setColor(new Color(1, .5f, 1, .7f));
                bubble.add(new SpriteComponent(sprite));
                bubble.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 7)));

                engine.addEntity(bubble);
            }
        }, .08f, 7);

        VisualEvent ripple = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity ripple = new Entity();
                ripple.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                ripple.add(new LifetimeComponent(0, .24f));
                ripple.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Animation.PlayMode.LOOP_PINGPONG));
                ripple.add(new EventComponent(.01f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor = animm.get(entity).shadeColor.cpy().add(-.04f, 0, .04f, 0);
                }));
                engine.addEntity(ripple);
            }
        }, .01f, 1);

        Move move = new Move("Medical Throw", "The target was hit with medical substances!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp += 3, 0, stm.get(enemy).getModMaxHp(enemy));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sludge, shine, ripple})), new MoveInfo(false, 0, (enemy, userEntity) -> {enemy.hp += 3;}));
        move.setAttackDescription("Throws a medical material at the target. Heals the target's health by 3.");
        return move;
    }

    //golem
    public static Move slamRed(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Color.RED,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent bam = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("boom"));
                bamSprite.setOriginCenter();
                bamSprite.setColor(Color.RED);
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .2f, 1);

        Move move = new Move("Slam", nm.get(user).name + " slammed into the opponent", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{explode, bam})), new MoveInfo(false, 1));
        move.setAttackDescription("Slams into the target. Deals regular damage.");
        return move;
    }

    public static Move slamBlue(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Color.BLUE,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent bam = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 - entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("boom"));
                bamSprite.setOriginCenter();
                bamSprite.setColor(Color.CYAN);
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .2f, 1);

        Move move = new Move("Slam", nm.get(user).name + " slammed into the opponent", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{explode, bam})), new MoveInfo(false, 1));
        move.setAttackDescription("Slams into the target. Deals regular damage.");
        return move;
    }

    public static Move heavySlamRed(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Color.RED,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent bam = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .4f));
                Sprite bamSprite = new Sprite(atlas.findRegion("boom"));
                bamSprite.setOriginCenter();
                bamSprite.setColor(Color.ORANGE);
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(8)));

                engine.addEntity(bam);
            }
        }, .2f, 1);

        VisualEvent sphereOut = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);
                Vector2 tileCenter = tilePosition.cpy().add(new Vector2(50 * scale, 50 * scale));

                Entity circ = new Entity();
                tilePosition.add(MathUtils.random(-30, 30) * scale, (MathUtils.random(-30, 30) * scale));
                circ.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(circ).getCenter().y),
                        tileCenter.x - (pm.get(circ).getCenter().x));

                circ.add(new MovementComponent(new Vector2(50 * scale, 0)));
                mm.get(circ).movement.setAngle(directionTowardsCenter + 180);
                circ.add(new LifetimeComponent(0, .5f));
                Sprite sprite = new Sprite(atlas.findRegion("circle"));
                sprite.setColor(Color.RED);
                sprite.setOriginCenter();
                circ.add(new SpriteComponent(sprite));

                circ.add(new EventComponent(.01f, true, EventCompUtil.fadeOut(50)));

                engine.addEntity(circ);
            }
        }, .01f, 6);

        Move move = new Move("Heavy Slam", nm.get(user).name + " slammed into the opponent", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{sphereOut, explode, bam})), new MoveInfo(false, 2));
        move.setAttackDescription("Slams into the target with a great force. Deals 2x damage.");
        return move;
    }

    public static Move laserBeamRed(Entity user) {
        VisualEvent laser = new VisualEvent(new VisualEffect() {
            Tile startTile;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(57 * scale, 17 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                if (direction == 90)
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else if (direction == 180)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.y / 2f,
                            boards.getTileHeight() / 2);
                else if (direction == 270)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y * 1.5f);

                Entity beam = new Entity();
                Sprite beamSprite = new Sprite(atlas.findRegion("beam"));
                beamSprite.setOriginCenter();
                beamSprite.setColor(Color.RED);
                beam.add(new SpriteComponent(beamSprite));
                beam.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90));
                beam.add(new MovementComponent(new Vector2(1300 * scale, 0).setAngle(direction)));
                beam.add(new LifetimeComponent(0, .26f));
                beam.add(new EventComponent(0.02f, true, EventCompUtil.fadeOutAfter(7, 6)));
                engine.addEntity(beam);
            }
        }, .03f, 12);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("explode"),
                                    atlas.findRegion("explode2"),
                                    atlas.findRegion("explode3"),
                                    atlas.findRegion("explode4"),
                                    atlas.findRegion("explode5"),
                                    atlas.findRegion("explode6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 1);

        Move move = new Move("Laser Beam", nm.get(user).name + " shot a laser beam!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{laser, explode})), new MoveInfo(false, 1));
        move.setAttackDescription("Fires a laser beam in front of the user. Deals regular damage.");
        return move;
    }

    public static Move guard(Entity user) {
        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.moveBy(2, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.moveBy(-2, 0);
            }
        }, .05f, 2);

        Move move = new Move("Guard", nm.get(user).name + " raised its guard.", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e))
                            status.get(e).addStatusEffect(guardUp(1), e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{moveRight, moveLeft, moveRight.copy(), moveLeft.copy()})),
                new MoveInfo(false, 0, guardUp(1).createStatusEffectInfo()));
        move.setAttackDescription("Raises its guard. Increases the user's defense for 1 turn.");
        return move;
    }

    public static Move superGuard(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.moveBy(2, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.moveBy(-2, 0);
            }
        }, .05f, 2);

        Move move = new Move("Super Guard", nm.get(user).name + " assumed a defensive stance.", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).addStatusEffect(guardUp(1), e);
                            status.get(e).addStatusEffect(regeneration(2), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, moveRight, moveLeft, moveRight.copy(), moveLeft.copy()})),
                new MoveInfo(false, 0, guardUp(1).createStatusEffectInfo(), regeneration(2).createStatusEffectInfo()));
        move.setAttackDescription("Assumes a more defensive stance. Increases the user's defense for 1 turn and causes" +
                " the user to regenerate health for 3 turns.");
        return move;
    }

    public static Move ultimateGuard(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(200 * scale, 200 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Color.PURPLE,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 2);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.moveBy(2, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.moveBy(-2, 0);
            }
        }, .05f, 2);

        Move move = new Move("Ultimate Guard", nm.get(user).name + " assumed a perfect defensive stance.", user, 6, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).addStatusEffect(guardUp(2), e);
                            status.get(e).addStatusEffect(regeneration(3), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, moveRight, moveLeft, moveRight.copy(), moveLeft.copy()})),
                new MoveInfo(false, 0, guardUp(2).createStatusEffectInfo(), regeneration(3).createStatusEffectInfo()));
        move.setAttackDescription("Moves into the ultimate defensive stance. Increases the user's defense for 2 turn and causes" +
                " the user to regenerate health for 3 turns.");
        return move;
    }

    public static Move laserBeamBlue(Entity user) {
        VisualEvent laser = new VisualEvent(new VisualEffect() {
            Tile startTile;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                Vector2 startTilePosition;
                Vector2 entitySize;
                float direction = 0;

                BoardPosition tempBp = targetPositions.first();
                if (tempBp.r < 0)
                    direction = 90;
                else if (tempBp.c < 0)
                    direction = 180;
                else if (tempBp.r > 0)
                    direction = 270;

                entitySize = new Vector2(57 * scale, 17 * scale);

                try { //check first tile
                    startTile = boards.getBoard().getTile(bm.get(user).pos.r, bm.get(user).pos.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                startTilePosition = startTile.localToStageCoordinates(new Vector2(0, 0));
                if (direction == 90)
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else if (direction == 180)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.y / 2f,
                            boards.getTileHeight() / 2);
                else if (direction == 270)
                    startTilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);
                else
                    startTilePosition.add(boards.getTileWidth() / 2,
                            boards.getTileHeight() / 2 - entitySize.y * 1.5f);

                Entity beam = new Entity();
                Sprite beamSprite = new Sprite(atlas.findRegion("beam"));
                beamSprite.setOriginCenter();
                beamSprite.setColor(Color.BLUE);
                beam.add(new SpriteComponent(beamSprite));
                beam.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90));
                beam.add(new MovementComponent(new Vector2(1300 * scale, 0).setAngle(direction)));
                beam.add(new LifetimeComponent(0, .26f));
                beam.add(new EventComponent(0.02f, true, EventCompUtil.fadeOutAfter(7, 6)));
                engine.addEntity(beam);
            }
        }, .03f, 12);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("explodeBlue"),
                                    atlas.findRegion("explodeBlue2"),
                                    atlas.findRegion("explodeBlue3"),
                                    atlas.findRegion("explodeBlue4"),
                                    atlas.findRegion("explodeBlue5"),
                                    atlas.findRegion("explodeBlue6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 1);

        Move move = new Move("Laser Beam", nm.get(user).name + " shot a laser beam!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{laser, explode})), new MoveInfo(false, 1));
        move.setAttackDescription("Fires a laser beam in front of the user. Deals regular damage.");
        return move;
    }

    //spider
    public static Move slash(Entity user) {
        VisualEvent slashes = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .07f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("vertslash1"),
                                atlas.findRegion("vertslash2"),
                                atlas.findRegion("vertslash3"),
                                atlas.findRegion("vertslash4")},
                        Animation.PlayMode.NORMAL));
                engine.addEntity(boom);
            }
        }, .01f, 2);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(4, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(-4, 0);
            }
        }, .05f, 2);

        VisualEvent fixPositioning = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                Vector2 tilePosition = new Vector2(BoardComponent.boards.getTileWidth() / 2 - am.get(enemy).actor.getWidth() / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - am.get(enemy).actor.getHeight() / 2f);
                am.get(enemy).actor.setPosition(tilePosition.x, tilePosition.y);
            }
        }, .05f, 1);

        Move move = new Move("Slash", nm.get(user).name + " attacks!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{slashes, moveRight, slashes.copy(), moveLeft, slashes.copy(), moveRight.copy(),
                slashes.copy(), moveLeft.copy(), slashes.copy(), moveRight.copy(), slashes.copy(), moveLeft.copy(), fixPositioning})), new MoveInfo(false, 1));
        move.setAttackDescription("Slashes at the target. Deals regular damage.");
        return move;
    }

    public static Move toxicSlash(Entity user) {
        VisualEvent slashes = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .13f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("vertslash1"),
                                atlas.findRegion("vertslash2"),
                                atlas.findRegion("vertslash3"),
                                atlas.findRegion("vertslash4")},
                        Color.GREEN,
                        Animation.PlayMode.NORMAL));
                engine.addEntity(boom);
            }
        }, .01f, 2);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(4, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(-4, 0);
            }
        }, .05f, 2);

        VisualEvent fixPositioning = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                Vector2 tilePosition = new Vector2(BoardComponent.boards.getTileWidth() / 2 - am.get(enemy).actor.getWidth() / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - am.get(enemy).actor.getHeight() / 2f);
                am.get(enemy).actor.setPosition(tilePosition.x, tilePosition.y);
            }
        }, .05f, 1);

        Move move = new Move("Toxic Slash", nm.get(user).name + " attacks with a deadly poison!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(poison(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{slashes, moveRight, slashes.copy(), moveLeft, slashes.copy(), moveRight.copy(),
                        slashes.copy(), moveLeft.copy(), slashes.copy(), moveRight.copy(), slashes.copy(), moveLeft.copy(), fixPositioning})), new MoveInfo(false, 1, poison(2).createStatusEffectInfo()));
        move.setAttackDescription("Slashes at the target with an edge seeped in poison. Deals regular damage and poisons the target for 2 turns.");
        return move;
    }

    public static Move immobite(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Color.YELLOW,
                        Animation.PlayMode.NORMAL));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent zag = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(35 * scale, 35 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity fist = new Entity();
                fist.add(new PositionComponent(tilePosition.add(MathUtils.random(-40, 40), MathUtils.random(-40, 40)), entitySize.x, entitySize.y, 0));
                fist.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("zigzag");
                sprite.setOriginCenter();
                sprite.setColor(Color.ORANGE);
                fist.add(new SpriteComponent(sprite));
                fist.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(fist);
            }
        }, .01f, 5);

        VisualEvent doNothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
            }
        }, .3f, 1);

        VisualEvent slashes = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .07f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("vertslash1"),
                                atlas.findRegion("vertslash2"),
                                atlas.findRegion("vertslash3"),
                                atlas.findRegion("vertslash4")},
                        Animation.PlayMode.NORMAL));
                engine.addEntity(boom);
            }
        }, .01f, 25);

        Move move = new Move("Immobite", "The target's attack paralyzed the opponent!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{slashes, zag, doNothing, explode})), new MoveInfo(false, 1, paralyze(3).createStatusEffectInfo()));
        move.setAttackDescription("Bites the target. Paralyzes the target for 3 turns.");
        return move;
    }

    public static Move stealSkill(Entity user) {
        VisualEvent slashes = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .07f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("vertslash1"),
                                atlas.findRegion("vertslash2"),
                                atlas.findRegion("vertslash3"),
                                atlas.findRegion("vertslash4")},
                        Animation.PlayMode.NORMAL));
                engine.addEntity(boom);
            }
        }, .01f, 3);

        VisualEvent shine = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .8f));
                boom.add(new MovementComponent(new Vector2(0, 20 * scale)));
                boom.add(new SpriteComponent(atlas.createSprite("sparkle")));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(8)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(4, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(-4, 0);
            }
        }, .05f, 2);

        VisualEvent fixPositioning = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                Vector2 tilePosition = new Vector2(BoardComponent.boards.getTileWidth() / 2 - am.get(enemy).actor.getWidth() / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - am.get(enemy).actor.getHeight() / 2f);
                am.get(enemy).actor.setPosition(tilePosition.x, tilePosition.y);
            }
        }, .05f, 1);

        Move move = new Move("Steal Skill", nm.get(user).name + " tried to steal skill points!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        int success = MathUtils.random(0, 3);

                        if (stm.has(enemy)) {
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) / 2 - stm.get(enemy).getModDef(enemy), 0, 999);
                            stm.get(enemy).sp = MathUtils.clamp(stm.get(enemy).sp - success, 0, 999);
                        }

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        slashes, moveRight, slashes.copy(), moveLeft, slashes.copy(),
                        moveRight.copy(), slashes.copy(), moveLeft.copy(), slashes.copy(),
                        moveRight.copy(), shine, slashes.copy(), shine.copy(), moveLeft.copy(), shine.copy(), fixPositioning
                })),
                new MoveInfo(false, .5f, (enemy, userEntity) -> {
                    int stolenAmount = MathUtils.random(0, 3);
                    enemy.sp -= stolenAmount;
                    userEntity.sp += stolenAmount;
                }));
        move.setAttackDescription("Attempts to steal the target's SP. Deals 1/2x damage and steals 0-3 skill points.");
        return move;
    }

    public static Move stealHealth(Entity user) {
        VisualEvent slashes = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .07f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("vertslash1"),
                                atlas.findRegion("vertslash2"),
                                atlas.findRegion("vertslash3"),
                                atlas.findRegion("vertslash4")},
                        Color.GREEN,
                        Animation.PlayMode.NORMAL));
                engine.addEntity(boom);
            }
        }, .01f, 3);

        VisualEvent shine = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(15 * scale, 15 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .8f));
                boom.add(new MovementComponent(new Vector2(0, 20 * scale)));
                Sprite spr = atlas.createSprite("shine");
                spr.setColor(Color.GREEN);
                boom.add(new SpriteComponent(spr));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(8)));
                engine.addEntity(boom);
            }
        }, .01f, 2);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(4, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(-4, 0);
            }
        }, .05f, 2);

        VisualEvent fixPositioning = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                Vector2 tilePosition = new Vector2(BoardComponent.boards.getTileWidth() / 2 - am.get(enemy).actor.getWidth() / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - am.get(enemy).actor.getHeight() / 2f);
                am.get(enemy).actor.setPosition(tilePosition.x, tilePosition.y);
            }
        }, .05f, 1);

        Move move = new Move("Steal Health", nm.get(user).name + " tried to steal health!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy)) {
                            int inflictedDamage = MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);
                            stm.get(enemy).hp -= inflictedDamage;
                            stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + inflictedDamage, 0, stm.get(e).getModMaxHp(e));
                        }

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        slashes, moveRight, slashes.copy(), moveLeft, slashes.copy(),
                        moveRight.copy(), slashes.copy(), moveLeft.copy(), slashes.copy(),
                        moveRight.copy(), shine, slashes.copy(), shine.copy(), moveLeft.copy(), shine.copy(), fixPositioning
                })),
                new MoveInfo(false, 1, (enemy, userEntity) -> {
                    int damage = userEntity.attack - enemy.defense;
                    enemy.hp -= damage;
                    userEntity.hp += damage;
                }
        ));
        move.setAttackDescription("Attempts to steal the target's health. Deals regular damage and recovers the damage dealt");
        return move;
    }

    public static Move demoralizeBlow(Entity user) {
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
                Sprite glowSprite = atlas.createSprite("openCircle");
                glowSprite.setColor(new Color(.6f, .6f, 1, 0));
                glow.add(new SpriteComponent(glowSprite));
                glow.add(new EventComponent(.1f, true, EventCompUtil.fadeIn(6)));
                engine.addEntity(glow);
            }

        }, .01f, 1);

        VisualEvent glow2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {

                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity glow = new Entity();
                glow.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 0));
                glow.add(new LifetimeComponent(0, .5f));
                Sprite glowSprite = atlas.createSprite("openCircle");
                glowSprite.setColor(new Color(.6f, .6f, 1, 0));
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
                Vector2 entitySize = new Vector2(40 * scale, 40 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity slash = new Entity();
                slash.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 0));
                slash.add(new LifetimeComponent(0, .39f));
                slash.add(new AnimationComponent(.08f,new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        new Color(.3f, .3f, 1, 1),
                        Animation.PlayMode.NORMAL));
                engine.addEntity(slash);
            }
        }, .2f, 1);

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
                Vector2 entitySize = new Vector2(40 * scale, 40 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                tileCenter.add(-entitySize.x / 2f, -entitySize.y / 2f);

                Entity crossSlash = new Entity();
                crossSlash.add(new PositionComponent(tileCenter, entitySize.x, entitySize.y, 90));
                crossSlash.add(new LifetimeComponent(0, .39f));
                crossSlash.add(new AnimationComponent(.08f, new TextureRegion[] {
                        atlas.findRegion("vertslash1"),
                        atlas.findRegion("vertslash2"),
                        atlas.findRegion("vertslash3"),
                        atlas.findRegion("vertslash4")},
                        new Color(.3f, .3f, 1, 1),
                        Animation.PlayMode.NORMAL));
                engine.addEntity(crossSlash);
            }
        }, .3f, 1);

        //Move
        Move move = new Move("Demoralize Blow", nm.get(user).name + " delivered a demoralizing blow!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 1.5f, 0, 999);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(petrify(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{glow, glow2, sliceVis, crossSliceVis})), new MoveInfo(true, 1.5f, petrify(3).createStatusEffectInfo()));
        move.setAttackDescription("Tricks the target into a full sense of self confidence before going in for the kill." +
                " Deals 1.5x damage and Petrifies for 3 turns.");
        return move;
    }

    public static Move slash2(Entity user) {
        VisualEvent slashes = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .07f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("vertslash1"),
                                atlas.findRegion("vertslash2"),
                                atlas.findRegion("vertslash3"),
                                atlas.findRegion("vertslash4")},
                        new Color(1, .4f, .4f, 1),
                        Animation.PlayMode.NORMAL));
                engine.addEntity(boom);
            }
        }, .01f, 5);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(4, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.moveBy(-4, 0);
            }
        }, .05f, 2);

        VisualEvent fixPositioning = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (!t.isOccupied())
                    return;

                Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                Vector2 tilePosition = new Vector2(BoardComponent.boards.getTileWidth() / 2 - am.get(enemy).actor.getWidth() / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - am.get(enemy).actor.getHeight() / 2f);
                am.get(enemy).actor.setPosition(tilePosition.x, tilePosition.y);
            }
        }, .05f, 1);

        Move move = new Move("Slash", nm.get(user).name + " attacks!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{slashes, moveRight, slashes.copy(), moveLeft, slashes.copy(), moveRight.copy(),
                        slashes.copy(), moveLeft.copy(), slashes.copy(), moveRight.copy(), slashes.copy(), moveLeft.copy(), fixPositioning})), new MoveInfo(false, 1));
        move.setAttackDescription("Viciously slashes at the target. Deals regular damage.");
        return move;
    }

    //gargoyle
    public static Move crushClaw(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Color.RED,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

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
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);

                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity claw = new Entity();
                claw.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                claw.add(new LifetimeComponent(0, .21f));
                claw.add(new AnimationComponent(.05f,new TextureRegion[] {
                        atlas.findRegion("claw"),
                        atlas.findRegion("claw2"),
                        atlas.findRegion("claw3"),
                        atlas.findRegion("claw4"),
                        atlas.findRegion("claw5")},
                        Color.RED,
                        Animation.PlayMode.NORMAL));
                engine.addEntity(claw);
            }
        }, .21f, 1);

        VisualEvent sphereOut = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);
                Vector2 tileCenter = tilePosition.cpy().add(new Vector2(50 * scale, 50 * scale));

                Entity circ = new Entity();
                tilePosition.add(MathUtils.random(-30, 30) * scale, (MathUtils.random(-30, 30) * scale));
                circ.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(circ).getCenter().y),
                        tileCenter.x - (pm.get(circ).getCenter().x));

                circ.add(new MovementComponent(new Vector2(50 * scale, 0)));
                mm.get(circ).movement.setAngle(directionTowardsCenter + 180);
                circ.add(new LifetimeComponent(0, .5f));
                Sprite spr = atlas.createSprite("sparkle");
                spr.setColor(Color.BLUE);
                spr.setOriginCenter();
                circ.add(new SpriteComponent(spr));

                circ.add(new EventComponent(.01f, true, EventCompUtil.fadeOut(50)));

                engine.addEntity(circ);
            }
        }, .01f, 6);

        Move move = new Move("Crush Claw", nm.get(user).name + " crushed the opponent!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless(2), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, sphereOut, claw})), new MoveInfo(false, 1, defenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Claws at the target with a force strong enough to bend iron. Deals regular damage and lowers the target's defense for 2 turns.");
        return move;
    }

    public static Move penetrate(Entity user) {
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
                Vector2 entitySize = new Vector2(75 * scale, 75 * scale);

                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

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

        VisualEvent sphereOut = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);
                Vector2 tileCenter = tilePosition.cpy().add(new Vector2(50 * scale, 50 * scale));

                Entity circ = new Entity();
                tilePosition.add(MathUtils.random(-30, 30) * scale, (MathUtils.random(-30, 30) * scale));
                circ.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(circ).getCenter().y),
                        tileCenter.x - (pm.get(circ).getCenter().x));

                circ.add(new MovementComponent(new Vector2(50 * scale, 0)));
                mm.get(circ).movement.setAngle(directionTowardsCenter + 180);
                circ.add(new LifetimeComponent(0, .5f));
                Sprite spr = atlas.createSprite("sparkle");
                spr.setOriginCenter();
                circ.add(new SpriteComponent(spr));

                circ.add(new EventComponent(.01f, true, EventCompUtil.fadeOut(50)));

                engine.addEntity(circ);
            }
        }, .01f, 6);

        VisualEvent sphereIn = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(350 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                Sprite glowSprite = atlas.createSprite("sparkle");
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.05f, true, EventCompUtil.fadeIn(6)));

                engine.addEntity(glow);
            }

        }, .06f, 5);


        Move move = new Move("Penetrate", nm.get(user).name + " attacked through defenses!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{sphereIn, claw, sphereOut})), new MoveInfo(true, 1));
        move.setAttackDescription("Strikes quickly to bypass the target's guard. Ignores defense and deals regular damage.");
        return move;
    }

    public static Move judgingGlare(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Color.BLUE,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setColor(Color.ROYAL);
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .2f, 1);

        Move move = new Move("Judging Glare", nm.get(user).name + " gave the opponent a judging glare!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(petrify(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, sparkle})), new MoveInfo(false, 0, petrify(3).createStatusEffectInfo()));
        move.setAttackDescription("Stares at the target in a way that makes them feel pure fear. Inflicts Petrification for 3 turns.");
        return move;
    }

    public static Move beam(Entity user) {
        VisualEvent preBoom = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .2f, 1);

        VisualEvent explode1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle1 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setColor(Color.YELLOW);
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(1).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle2 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(1).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setColor(Color.YELLOW);
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode3 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(2).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle3 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(2).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setColor(Color.YELLOW);
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        VisualEvent explode4 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(3).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(150 * scale, 150 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .21f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);
        VisualEvent sparkle4 = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(3).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(30 * scale, 30 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - entitySize.x / 2f, t.getHeight() / 2 -entitySize.y / 2f));

                Entity bam = new Entity();
                bam.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                bam.add(new LifetimeComponent(0, .2f));
                Sprite bamSprite = new Sprite(atlas.findRegion("sparkle"));
                bamSprite.setColor(Color.YELLOW);
                bamSprite.setOriginCenter();
                bam.add(new SpriteComponent(bamSprite));
                bam.add(new EventComponent(.05f, true, EventCompUtil.fadeOut(4)));

                engine.addEntity(bam);
            }
        }, .1f, 1);

        Move move = new Move("Beam", nm.get(user).name + " shot off a beam!", user, 1, new Array<BoardPosition>(
                new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 0),
                        new BoardPosition(-3, 0),
                        new BoardPosition(-4, 0)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean())
                            status.get(enemy).addStatusEffect(paralyze(3), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0), new BoardPosition(-4, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{preBoom, explode1, sparkle1, explode2, sparkle2, explode3, sparkle3, explode4, sparkle4})), new MoveInfo(false, 1,
                (enemy, userEntity) -> {
                    if (enemy.acceptsStatusEffects && MathUtils.randomBoolean())
                        enemy.statusEffectInfos.add(paralyze(3).createStatusEffectInfo());
                }
        ));
        move.setAttackDescription("Fires off a beam that sweeps the area in front of the user. Deals regular damage and has a 50%" +
                " chance to paralyze for 3 turns.");
        return move;
    }


    //possesed book
    public static Move monoplode(Entity user) {
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
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(350 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                Sprite glowSprite = atlas.createSprite("circle");
                glowSprite.setColor(new Color(.1f, .1f, .1f, 0f));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.05f, true, EventCompUtil.fadeIn(6)));

                engine.addEntity(glow);
            }

        }, .04f, 25);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Color.BLACK,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        //Move
        Move move = new Move("Monoplode", nm.get(user).name + " uses a spell!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles, explode, explodeBig})), new MoveInfo(false, 1));
        move.setAttackDescription("Uses mysterious magic to attack the target with orbs. Deals regular damage.");
        return move;
    }

    public static Move monoplode2(Entity user) {
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
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(350 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                Sprite glowSprite = atlas.createSprite("triangle");
                glowSprite.setColor(new Color(.1f, .1f, .1f, 0f));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.02f, true, (entity, engine) -> {
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().add(.01f, .03f, .08f, .03f));
                    sm.get(entity).sprite.setSize(sm.get(entity).sprite.getWidth() + .5f, sm.get(entity).sprite.getHeight() + .5f);
                    mm.get(entity).movement.scl(.9f);
                    pm.get(entity).rotation += 10;
                }));

                engine.addEntity(glow);
            }

        }, .04f, 25);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.BLACK,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Color.CYAN,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        //Move
        Move move = new Move("Monoplode", nm.get(user).name + " uses a spell!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles, explode, explodeBig})), new MoveInfo(false, 1));
        move.setAttackDescription("Uses mathematical magic to attack the target with 3 pointed objects. Deals regular damage.");
        return move;
    }

    public static Move monoplode3(Entity user) {
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
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(350 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                glow.add(new AnimationComponent(.05f,
                        new TextureRegion[]{
                        atlas.findRegion("fourCircles"),
                        atlas.findRegion("sixCircles"),
                        atlas.findRegion("eightCircles")},
                        new Color(MathUtils.random(0, .4f), MathUtils.random(0, .4f), MathUtils.random(0, .4f), 0),
                        Animation.PlayMode.LOOP
                        ));

                glow.add(new EventComponent(.02f, true, (entity, engine) -> {
                    animm.get(entity).shadeColor.add(MathUtils.random(0.01f, .1f), MathUtils.random(0.01f, .1f), MathUtils.random(0.01f, .1f), .03f);
                    animm.get(entity).addSpriteSize(1f, 1f);
                    mm.get(entity).movement.scl(.9f);
                    pm.get(entity).rotation += 30;
                }));

                engine.addEntity(glow);
            }

        }, .04f, 25);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.GREEN,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Color.CYAN,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent afterEffect = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-150 * scale, 150 * scale), MathUtils.random(-150 * scale, 150 * scale)),
                        entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("sparkle");
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                boom.add(new SpriteComponent(sprite));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(boom);
            }
        }, .01f, 10);

        //Move
        Move move = new Move("Monoplode", nm.get(user).name + " uses a spell!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles, explode, explodeBig, afterEffect})), new MoveInfo(false, 1));
        move.setAttackDescription("Uses an elite tier spell to attack the target with a large surge of magical energy. Deals regular damage.");
        return move;
    }

    public static Move monoplode4(Entity user) {
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
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(350 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                Sprite glowSprite = atlas.createSprite("triangle");
                glowSprite.setColor(new Color(.1f, .1f, .1f, 0f));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.02f, true, (entity, engine) -> {
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().add(.04f, .01f, .01f, .03f));
                    sm.get(entity).sprite.setSize(sm.get(entity).sprite.getWidth() + .5f, sm.get(entity).sprite.getHeight() + .5f);
                    mm.get(entity).movement.scl(.9f);
                    pm.get(entity).rotation += 10;
                }));

                engine.addEntity(glow);
            }

        }, .04f, 25);

        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.BLACK,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOutAfter(2, 3)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Color.RED,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent afterEffect = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-110 * scale, 110 * scale), MathUtils.random(-110 * scale, 110 * scale)),
                        entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("hexagon");
                sprite.setOriginCenter();
                sprite.setColor(new Color(MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), MathUtils.random(0f, 1f), 1));
                boom.add(new SpriteComponent(sprite));
                boom.add(new EventComponent(.025f, true, EventCompUtil.fadeOut(12)));

                engine.addEntity(boom);
            }
        }, .003f, 80);

        //Move
        Move move = new Move("Monoplode", nm.get(user).name + " uses a spell!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles, explode, explodeBig, afterEffect})), new MoveInfo(false, 1));
        move.setAttackDescription("Uses enigmatic magic to attack the target with their own mind. Deals regular damage.");
        return move;
    }

    public static Move monopierce(Entity user) {
        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, 1f));
                boom.add(new AnimationComponent(.1f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Animation.PlayMode.LOOP_PINGPONG));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(5, 5)));
                engine.addEntity(boom);
            }
        }, .01f, 10);

        VisualEvent doNothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
            }
        }, 1, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null) {
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
                }
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);


        Move move = new Move("Monopierce", nm.get(user).name + " uses a mystifying spell!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, ripples, doNothing, returnToNormalGradual, returnToNormal})), new MoveInfo(true, 1));
        move.setAttackDescription("Uses mystifying magic to unleash an attack that attacks regardless of the defense. Deals defense piercing damage.");
        return move;
    }

    public static Move monoflash(Entity user) {
        VisualEvent flash = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(stage.getWidth(), stage.getHeight());
                Entity flash = new Entity();
                flash.add(new PositionComponent(new Vector2(0, 0), entitySize.x, entitySize.y, 0));
                flash.add(new LifetimeComponent(0, .75f));
                flash.add(new SpriteComponent(atlas.createSprite("LightTile")));
                sm.get(flash).sprite.setColor(Color.WHITE);
                flash.add(new EventComponent(.05f, true, EventCompUtil.fadeInThenOut(5, 5, 5)));
                engine.addEntity(flash);
            }
        }, .3f, 1);

        VisualEvent setGrayScale = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                game.setGrayScale();
            }
        }, .01f, 1);

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
                Vector2 entitySize = new Vector2(25 * scale, 25 * scale);
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add(MathUtils.random(-400, 400), MathUtils.random(-400, 400)),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(100 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .7f));

                Sprite glowSprite = atlas.createSprite("triangle");
                glowSprite.setColor(new Color(0, 0, 0, 0f));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.02f, true, (entity, engine) -> {
                    sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().add(0, 0, 0, .012f));
                    sm.get(entity).sprite.setSize(sm.get(entity).sprite.getWidth() + .5f, sm.get(entity).sprite.getHeight() + .5f);
                    mm.get(entity).movement.scl(1.08f);
                    pm.get(entity).rotation += 13;
                }));

                engine.addEntity(glow);
            }

        }, .04f, 10);

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, 1f));
                boom.add(new AnimationComponent(.1f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.BLACK,
                        Animation.PlayMode.LOOP_PINGPONG));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(5, 5)));
                engine.addEntity(boom);
            }
        }, .1f, 1);

        VisualEvent explodeBig = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .2f));
                boom.add(new AnimationComponent(.04f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .2f, 1);

        VisualEvent nothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
            }
        }, .4f, 1);

        VisualEvent flashBack = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(stage.getWidth(), stage.getHeight());
                Entity flash = new Entity();
                flash.add(new PositionComponent(new Vector2(0, 0), entitySize.x, entitySize.y, 0));
                flash.add(new LifetimeComponent(0, .25f));
                flash.add(new SpriteComponent(atlas.createSprite("LightTile")));
                sm.get(flash).sprite.setColor(Color.WHITE);
                flash.add(new EventComponent(.025f, true, EventCompUtil.fadeOutAfter(5, 5)));
                engine.addEntity(flash);
            }
        }, .01f, 1);

        VisualEvent revertShading = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                game.removeShader();
            }
        }, .01f, 1);


        Move move = new Move("Monoflash", nm.get(user).name + " uses a enigmatic spell!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 2, 0, 999);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        flash, setGrayScale, circles, ripples, circles.copy(), ripples.copy(), explodeBig, circles.copy(), ripples.copy(),
                        circles.copy(), explodeBig.copy(), ripples.copy(), circles.copy(), explodeBig.copy(), ripples.copy(), nothing, flashBack, revertShading, nothing.copy(.3f)
                })), new MoveInfo(true, 2));
        move.setAttackDescription("Flashes a bright, disorienting light that causes physical pain. Deals 2x damage.");
        return move;
    }

    public static Move combubulate(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, .21f));
                    boom.add(new AnimationComponent(.04f,
                            new TextureRegion[]{atlas.findRegion("BWexplode"),
                                    atlas.findRegion("BWexplode2"),
                                    atlas.findRegion("BWexplode3"),
                                    atlas.findRegion("BWexplode4"),
                                    atlas.findRegion("BWexplode5"),
                                    atlas.findRegion("BWexplode6")},
                            Animation.PlayMode.NORMAL));
                    boom.add(new EventComponent(.04f, true, EventCompUtil.fadeOut(5)));
                    engine.addEntity(boom);
                }
            }
        }, .01f, 1);

        VisualEvent flash = new VisualEvent(new VisualEffect() {
            private float totalDeltaTime;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(0, stage.getWidth());
                Entity flash = new Entity();
                flash.add(new PositionComponent(new Vector2(0, stage.getHeight() / 2), entitySize.x, entitySize.y, 0));
                flash.add(new LifetimeComponent(0, 1f));
                flash.add(new SpriteComponent(atlas.createSprite("LightTile")));
                sm.get(flash).sprite.setColor(Color.WHITE);
                flash.add(new EventComponent(.01f, true, (entity, engine) -> {
                    totalDeltaTime += .01f;

                    if (totalDeltaTime <= .5f) {
                        float progress = Interpolation.exp10In.apply(totalDeltaTime / .5f);
                        //expand box
                        pm.get(entity).height = progress * stage.getHeight();
                        //put in middle
                        pm.get(entity).position.y = (stage.getHeight() / 2) - (pm.get(entity).height / 2);
                        //shade
                        sm.get(entity).sprite.setColor(new Color(1 - progress, 1 - progress, 1 - progress, 1));
                    } else { //fade out
                        sm.get(entity).sprite.setColor(sm.get(entity).sprite.getColor().add(.02f, .02f, .02f, 0));
                        if (totalDeltaTime > 0.59f) //clear at end
                            totalDeltaTime = 0;
                    }
                }));
                engine.addEntity(flash);
            }
        }, 1.01f, 1);

        VisualEvent floatUpDiamonds = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(40, 40);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(new Vector2(50 + MathUtils.random(stage.getWidth() - 200), 25 + MathUtils.random(stage.getHeight() - 100)), entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("openDiamonds"));
                sprite.setOriginCenter();
                sprite.setColor(Color.BLUE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .09f, 25);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10, 10);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(new Vector2(50 + MathUtils.random(stage.getWidth() - 200), 25 + MathUtils.random(stage.getHeight() - 100)), entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("sparkle"));
                sprite.setOriginCenter();
                sprite.setColor(Color.WHITE);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(6)));

                engine.addEntity(sparkle);
            }
        }, .03f, 25);

        VisualEvent floatDiamonds = new VisualEvent(new VisualEffect() {
            private float direction;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                direction += 60;
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60, 60);
                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(new Vector2(50 + MathUtils.random(stage.getWidth() - 200), 25 + MathUtils.random(stage.getHeight() - 100)), entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(600 * scale, 0)));
                mm.get(sparkle).movement.setAngle(direction);
                sparkle.add(new LifetimeComponent(0, .3f));
                Sprite sprite = new Sprite(atlas.findRegion("diamonds"));
                sprite.setOriginCenter();
                sprite.setColor(new Color(.2f, .9f, .2f, .5f));
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.01f, true, (entity, engine) -> {
                    Sprite spr = sm.get(entity).sprite;
                    spr.setColor(
                            spr.getColor().r,
                            spr.getColor().g,
                            spr.getColor().b,
                            MathUtils.clamp(spr.getColor().a - .5f / 30f, 0, 1));
                }));

                engine.addEntity(sparkle);
            }
        }, .01f, 40);

        VisualEvent ripples = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                for (BoardPosition pos : targetPositions) {
                    BoardPosition bp = pos.copy().add(bm.get(user).pos.r, bm.get(user).pos.c);
                    Tile t;
                    try {
                        t = boards.getBoard().getTile(bp.r, bp.c);
                    } catch (IndexOutOfBoundsException e) {
                        continue;
                    }
                    Vector2 entitySize = new Vector2(70 * scale, 70 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                            BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity boom = new Entity();
                    boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale))
                            , entitySize.x, entitySize.y, 0));
                    boom.add(new LifetimeComponent(0, 1f));
                    boom.add(new AnimationComponent(.1f,
                            new TextureRegion[]{atlas.findRegion("openCircle"),
                                    atlas.findRegion("openCircle2"),
                                    atlas.findRegion("openCircle3"),
                                    atlas.findRegion("openCircle4"),
                                    atlas.findRegion("openCircle5")},
                            Animation.PlayMode.LOOP_PINGPONG));
                    boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(5, 5)));
                    engine.addEntity(boom);
                }
            }
        }, .1f, 10);

        VisualEvent setInvert = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                game.setInvertColor();
            }
        }, .01f, 1);

        VisualEvent flashBack = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(stage.getWidth(), stage.getHeight());
                Entity flash = new Entity();
                flash.add(new PositionComponent(new Vector2(0, 0), entitySize.x, entitySize.y, 0));
                flash.add(new LifetimeComponent(0, .25f));
                flash.add(new SpriteComponent(atlas.createSprite("LightTile")));
                sm.get(flash).sprite.setColor(Color.WHITE);
                flash.add(new EventComponent(.025f, true, EventCompUtil.fadeOutAfter(5, 5)));
                engine.addEntity(flash);
            }
        }, .01f, 1);

        VisualEvent revertShading = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                game.removeShader();
            }
        }, .01f, 1);

        VisualEvent nothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
            }
        }, .3f, 1);

        Move move = new Move("Combobulate", nm.get(user).name + " uses enigmatic wizardry!", user, 4,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, -1), new BoardPosition(-2, -2), new BoardPosition(-3, -3),
                        new BoardPosition(-1, 1), new BoardPosition(-2, 2), new BoardPosition(-3, 3)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 3, 0, 999);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless(2), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, -1), new BoardPosition(-2, -2), new BoardPosition(-3, -3),
                        new BoardPosition(-1, 1), new BoardPosition(-2, 2), new BoardPosition(-3, 3)
                }),
                new Array<VisualEvent>(new VisualEvent[]{
                        explode, nothing, flash, setInvert, floatUpDiamonds, sparkle, ripples, floatDiamonds,
                        flashBack, revertShading, nothing.copy()
                })), new MoveInfo(true, 3, defenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Changes the surroundings into an inverted world, then attacks targets with multiple spells. Deals 3x damage, and lowers" +
                " the target's defense to 0.");
        return move;
    }

    public static Move enchant(Entity user) {
        //Visuals---
        VisualEvent sparkles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(100 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                Sprite glowSprite = atlas.createSprite("sparkle");
                glowSprite.setColor(new Color(MathUtils.random(.2f, 1), MathUtils.random(.2f, 1), MathUtils.random(.2f, 1), 0f));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.05f, true, EventCompUtil.fadeIn(6)));

                engine.addEntity(glow);
            }

        }, .02f, 45);

        VisualEvent nothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
            }
        }, .3f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        //Move
        Move move = new Move("Enchant", nm.get(user).name + " used a supportive spell!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(regeneration(3), enemy);
                            status.get(enemy).addStatusEffect(attackUp(3), enemy);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, sparkles, nothing, returnToNormalGradual, returnToNormal})), new MoveInfo(false, 0, regeneration(3).createStatusEffectInfo(), attackUp(3).createStatusEffectInfo()));
        move.setAttackDescription("Buffs the target's with stat-enhancing knowledge. Gives the target regeneration and increases their attack for 3 turns.");
        return move;
    }

    public static Move ward(Entity user) {
        //Visuals---
        VisualEvent sparkles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(10 * scale, 10 * scale);
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
                Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));

                Entity glow = new Entity();

                glow.add(new PositionComponent(tileCenter.cpy().add((float) (Math.random() * 150) - 75, (float) (Math.random() * 150) - 75),
                        entitySize.x, entitySize.y, 0));

                float directionTowardsCenter = MathUtils.radiansToDegrees * MathUtils.atan2(
                        tileCenter.y - (pm.get(glow).getCenter().y),
                        tileCenter.x - (pm.get(glow).getCenter().x));
                Vector2 movementToCenter = new Vector2(100 * scale, 0);
                movementToCenter.setAngle(directionTowardsCenter);
                glow.add(new MovementComponent(movementToCenter));

                glow.add(new LifetimeComponent(0, .3f));

                Sprite glowSprite = atlas.createSprite("sparkle");
                glowSprite.setColor(new Color(.7f, .7f, 1, 0));
                glow.add(new SpriteComponent(glowSprite));

                glow.add(new EventComponent(.05f, true, EventCompUtil.fadeIn(6)));

                engine.addEntity(glow);
            }

        }, .02f, 45);

        VisualEvent nothing = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
            }
        }, .3f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        //Move
        Move move = new Move("Ward", nm.get(user).name + " used a defensive spell!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(speedUp(2), enemy);
                            status.get(enemy).addStatusEffect(guardUp(2), enemy);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, sparkles, nothing, returnToNormalGradual, returnToNormal})), new MoveInfo(false, 0, guardUp(2).createStatusEffectInfo(), speedUp(2).createStatusEffectInfo()));
        move.setAttackDescription("Buffs the target with reassuring knowledge. Increases the target's speed and defense for 2 turns.");
        return move;
    }

    public static Move disarm(Entity user) {
        VisualEvent flash = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(stage.getWidth(), stage.getHeight());
                Entity flash = new Entity();
                flash.add(new PositionComponent(new Vector2(0, 0), entitySize.x, entitySize.y, 0));
                flash.add(new LifetimeComponent(0, .25f));
                flash.add(new SpriteComponent(atlas.createSprite("LightTile")));
                sm.get(flash).sprite.setColor(Color.WHITE);
                flash.add(new EventComponent(.025f, true, EventCompUtil.fadeOutAfter(5, 5)));
                engine.addEntity(flash);
            }
        }, .01f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                if (enemy != null)
                    am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);


        Move move = new Move("Disarm", nm.get(user).name + " uses a weakening spell!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(offenseless(2), enemy);
                            status.get(enemy).addStatusEffect(defenseless(2), enemy);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{flash, changeToBlack, returnToNormalGradual, returnToNormal})), new MoveInfo(false, 0, offenseless(2).createStatusEffectInfo(), defenseless(2).createStatusEffectInfo()));
        move.setAttackDescription("Creates a bright light to dazzle the target. Lower's the target's defense and attack for 2 turns.");
        return move;
    }

    public static Move fullRestore(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(100 * scale, 100 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .16f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Color.PINK,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(20 * scale, 20 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("fourCircles"));
                sprite.setOriginCenter();
                sprite.setColor(Color.PINK);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 3)));

                engine.addEntity(sparkle);
            }
        }, .08f, 14);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToGreen = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.GREEN, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Full Restore", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy) && status.get(enemy).getTotalStatusEffects() > 0)
                            status.get(enemy).removeAll(enemy);

                        if (stm.has(enemy))
                            stm.get(enemy).hp = stm.get(enemy).getModMaxHp(enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToGreen, sparkle, explode, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    enemy.hp = enemy.maxHp;
                    if (enemy.acceptsStatusEffects)
                        enemy.statusEffectInfos.clear();
                }));
        move.setAttackDescription("Uses magical energy to completely heal all wounds. Removes any of the target's status conditions, and restores their health to max.");
        return move;
    }

    public static Move superTransfer(Entity user) {

        VisualEvent rippleOther = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .25f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.GOLD,
                        Animation.PlayMode.REVERSED));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .25f, 2);

        VisualEvent rippleSelf = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos;
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(120 * scale, 120 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .25f));
                boom.add(new AnimationComponent(.03f,
                        new TextureRegion[]{atlas.findRegion("openCircle"),
                                atlas.findRegion("openCircle2"),
                                atlas.findRegion("openCircle3"),
                                atlas.findRegion("openCircle4"),
                                atlas.findRegion("openCircle5")},
                        Color.GOLD,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.06f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .25f, 2);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Transfer", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy)) {
                            stm.get(enemy).sp = MathUtils.clamp(stm.get(enemy).sp + 2, 0, stm.get(enemy).getModMaxSp(enemy));
                        }

                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(StatusEffectConstructor.spUp(2), enemy);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, rippleSelf, rippleOther, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    enemy.sp += 2;
                    if (enemy.acceptsStatusEffects)
                        enemy.statusEffectInfos.add(spUp(2).createStatusEffectInfo());
                }));
        move.setAttackDescription("Gives the target a large amount of inspirational knowledge. Increases the target's SP by 2 and causes them to recover more SP than usual each turn.");
        return move;
    }

    public static Move spiritBoost(Entity user) {
        VisualEvent explode = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(300 * scale, 300 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - entitySize.x / 2f,
                        BoardComponent.boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity boom = new Entity();
                boom.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .38f));
                boom.add(new AnimationComponent(.07f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        Color.BLUE,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.07f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .01f, 1);

        VisualEvent sparkle = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(35 * scale, 35 * scale);
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                        boards.getTileHeight() / 2 - entitySize.y / 2f);

                Entity sparkle = new Entity();
                sparkle.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-30 * scale, 30 * scale), MathUtils.random(-30 * scale, 30 * scale)),
                        entitySize.x, entitySize.y, 0));
                sparkle.add(new MovementComponent(new Vector2(0, 15 * scale)));
                sparkle.add(new LifetimeComponent(0, .6f));
                Sprite sprite = new Sprite(atlas.findRegion("fourCircles"));
                sprite.setOriginCenter();
                sprite.setColor(Color.CYAN);
                sparkle.add(new SpriteComponent(sprite));
                sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOutAfter(3, 3)));

                engine.addEntity(sparkle);
            }
        }, .08f, 14);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent changeToBlack = new VisualEvent(new VisualEffect() {
            private float progress;
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;
                progress = MathUtils.clamp(progress + .1f, 0, 1);

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(am.get(enemy).actor.getColor().cpy().lerp(Color.BLACK, progress));
            }
        }, .05f, 10);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);

                if (!boards.containsPosition(bp) || !boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        Move move = new Move("Spirit Boon", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        stm.get(enemy).sp += 1;
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(StatusEffectConstructor.spUp(3), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, sparkle, returnToNormalGradual, explode, returnToNormal})),
                new MoveInfo(false, 0, (enemy, userEntity) -> {
                    enemy.sp += 1;
                    if (enemy.acceptsStatusEffects)
                        enemy.statusEffectInfos.add(StatusEffectConstructor.spUp(4).createStatusEffectInfo());
                    if (enemy.maxHp >= 13 && enemy.sp < 7) // Encourage use if it hits the dragon (which has high max hp)
                        enemy.arbitraryValue += 150;

                }));
        move.setAttackDescription("Gives the target a spiritual boost. Restores the target's SP by 1 and allows them to regenerate SP faster for 4 turns");
        return move;
    }
    //endregion

}
