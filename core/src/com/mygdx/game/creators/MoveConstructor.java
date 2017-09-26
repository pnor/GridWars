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
import com.mygdx.game.screens_ui.screens.BattleScreen;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.atlas;
import static com.mygdx.game.creators.StatusEffectConstructor.*;
import static com.mygdx.game.misc.EventCompUtil.*;

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

        return new Move("Tackle", null, user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Star Spin", "Something spun around!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
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

        return new Move("Slice", nm.get(user).name + " sliced its blade!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
        return new Move("Piercing Slice", nm.get(user).name + " delivered a piercing blow!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
        return new Move("Breaking Slice", nm.get(user).name + " delivered a crippling blow!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{circles.copy(.1f, 5), circles ,sliceVis, crossSliceVis})), new MoveInfo(true, 1, defenseless().createStatusEffectInfo()));
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
        return new Move("Poison Blade", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{circles.copy(.1f, 5), circles, sliceVis, crossSliceVis})), new MoveInfo(false, 1, poison().createStatusEffectInfo()));
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

        return new Move("Blade Flurry", nm.get(user).name + " let loose with a flurry of attacks!", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
        return new Move("Bark", nm.get(user).name + " barked intimidatingly!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{bark.copy(.1f), bark2.copy(.1f), bark.copy(.1f), bark2.copy(.1f) ,bark, bark2})), new MoveInfo(false, 0, offenseless().createStatusEffectInfo()));
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

        return new Move("Metal Claw", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Laser Beam", nm.get(user).name + " shot a laser beam!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0)}),
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

        return new Move("Electrical Fire", null, user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(burn(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explosions, fire})), new MoveInfo(false, 1, burn().createStatusEffectInfo()));
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

        return new Move("Laser Spread", nm.get(user).name + " sprayed laser beams!", user, 4,
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

        return new Move("Body Slam", nm.get(user).name + " charged forward!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Sear", null, user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(burn(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{fire})), new MoveInfo(false, 0, burn().createStatusEffectInfo()));
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

        return new Move("Wild Fire", user, 4, new Array<BoardPosition>(new BoardPosition[]{
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
                            status.get(enemy).addStatusEffect(burn(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{}),
                new Array<VisualEvent>(new VisualEvent[]{explosions})),
                new MoveInfo(false, .5f, (entity) -> {
                    if (entity.acceptsStatusEffects && MathUtils.randomBoolean(.7f))
                        entity.statusEffectInfos.add(burn().createStatusEffectInfo());
                }));
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

        return new Move("Chill", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(shivers(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{freeze, sparkle})), new MoveInfo(false, 1, shivers().createStatusEffectInfo()));
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

        return new Move("Tailwind", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(speedUp(), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{largeSparkle, sparkle, largeSparkle.copy(), sparkle.copy()})), new MoveInfo(false, 0, speedUp().createStatusEffectInfo()));
    }

    public static Move twister(Entity user) {
        VisualEvent shuriken = new VisualEvent(new VisualEffect() {
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
                    Vector2 entitySize = new Vector2(70 * scale, 70 * scale);
                    Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                    tilePosition.add(boards.getTileWidth() / 2 - entitySize.x / 2f,
                            boards.getTileHeight() / 2 - entitySize.y / 2f);

                    Entity shuriken = new Entity();
                    shuriken.add(new PositionComponent(tilePosition, entitySize.x, entitySize.y, 0));
                    shuriken.add(new LifetimeComponent(0, 1.5f));
                    shuriken.add(new AnimationComponent(.1f,
                            new TextureRegion[]{atlas.findRegion("shuriken"),
                                    atlas.findRegion("shuriken2")},
                            new Color(.2f, 1f, .5f, 1),
                            Animation.PlayMode.LOOP));
                    shuriken.add(new EventComponent(.005f, true, EventCompUtil.rotate(20)));

                    engine.addEntity(shuriken);
                }
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
                    sprite.setColor(Color.RED);
                    sparkle.add(new SpriteComponent(sprite));
                    sparkle.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(5)));

                    engine.addEntity(sparkle);
                }
            }
        }, .08f, 9);

        return new Move("Twister", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1), new BoardPosition(-1, 0), new BoardPosition(-1, 1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1), new BoardPosition(-1, 0), new BoardPosition(-1, 1)}),
                new Array<VisualEvent>(new VisualEvent[]{shuriken, sparkle})), new MoveInfo(false, 1));
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

        return new Move("Freeze", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(freeze(), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{freeze, sparkle})), new MoveInfo(false, 0, freeze().createStatusEffectInfo()));
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        return new Move("Assist", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp + 2, 0, stm.get(enemy).getModMaxHp(enemy));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToGreen, sparkle, explode, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.hp += 2;
                }));
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        return new Move("Clear", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy))
                            status.get(enemy).removeAll(enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlue, sparkle, explode, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {if (entity.acceptsStatusEffects) entity.statusEffectInfos.clear();}));
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

        return new Move("Recover", nm.get(user).name + " began to recover.", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 3, 0, stm.get(e).getModMaxHp(e));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToGreen, sparkle, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.hp += 2;
                }));
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

        return new Move("Submerge", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-3, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp - 3, 0, stm.get(enemy).getModMaxHp(enemy));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-3, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{ripples, waterBall, changeToBlue, largeSparkle, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.hp -= 3;
                }));
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

        return new Move("Rest Mind", nm.get(user).name + " began to rest its mind.", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        stm.get(e).sp = MathUtils.clamp(stm.get(e).sp + 1, 0, stm.get(e).getModMaxSp(e));

                        if (status.has(e))
                            status.get(e).addStatusEffect(defenseless(), e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{sparkle, explode,  returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.sp += 1;
                    if (entity.acceptsStatusEffects)
                        entity.statusEffectInfos.add(defenseless().createStatusEffectInfo());

                }));
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

        return new Move("Drench", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
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
                circles.add(new LifetimeComponent(0, 3f));
                circles.add(new AnimationComponent(1f,
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
        }, 3f, 1);

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

        return new Move("Electrocute", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) / 2 - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(paralyze(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{spinning, ripple, shock, largeSparkle})), new MoveInfo(false, .5f, paralyze().createStatusEffectInfo()));
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

        return new Move("Comet Shower", user, 6, new Array<BoardPosition>(new BoardPosition[]{
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


        return new Move("Shock Claw", nm.get(user).name + " scraped the opponent!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy)) {
                            if (!status.get(enemy).statusEffects.containsKey("Paralyze")) { //Not paralyzed
                                if (MathUtils.randomBoolean(.5f))
                                    status.get(enemy).addStatusEffect(paralyze(), enemy);
                            } else { //is paralyzed
                                status.get(enemy).removeStatusEffect(enemy, "Paralyze"); //cure their paralysis
                                stm.get(e).sp = MathUtils.clamp(stm.get(e).sp + 1, 0, stm.get(e).getModMaxSp(e)); //recover sp
                            }
                        }

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{shock, claw})), new MoveInfo(false, 1, (entity) -> {
                    if (entity.acceptsStatusEffects && MathUtils.randomBoolean(.5f))
                        entity.statusEffectInfos.add(paralyze().createStatusEffectInfo());
                }));
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

        return new Move("Charge", nm.get(user).name + " gained electric energy.", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).removeAll(e);
                            status.get(e).addStatusEffect(charged(), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, charges, largeSparkle, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.statusEffectInfos.clear();
                    if (entity.acceptsStatusEffects)
                        entity.statusEffectInfos.add(charged().createStatusEffectInfo());
                }));
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
        }, .02f, 120);

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

        return new Move("Supercharge", nm.get(user).name + " gained a large amount of electric energy!", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).removeAll(e);
                            status.get(e).addStatusEffect(supercharged(), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, charges, largeSparkle, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.statusEffectInfos.clear();
                    if (entity.acceptsStatusEffects)
                        entity.statusEffectInfos.add(supercharged().createStatusEffectInfo());
                })
        );
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

        return new Move("Volt Deluge", user, 5, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 1), new BoardPosition(-1, -1),  new BoardPosition(-2, 0),  new BoardPosition(-3, 1), new BoardPosition(-3, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.75f))
                            status.get(enemy).addStatusEffect(paralyze(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 1), new BoardPosition(-1, -1),  new BoardPosition(-2, 0),  new BoardPosition(-3, 1), new BoardPosition(-3, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        ions, doNothing, rippleOut.copy(), shocking.copy(), rippleOut, shocking})),
                new MoveInfo(false, 1, (entity) -> {
                    if (MathUtils.randomBoolean(.75f) && entity.acceptsStatusEffects)
                        entity.statusEffectInfos.add(paralyze().createStatusEffectInfo());
                })
        );
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

        return new Move("Barrage", null, user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Feint", nm.get(user).name + " struck where the enemy was vulnerable!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        int numStatuses = 0;

                        if (status.has(enemy))
                            numStatuses = status.get(enemy).statusEffects.values().toArray().size;

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * numStatuses - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode.copy(), barrage.copy(), explode.copy(), barrage.copy(), explode, barrage})),
                new MoveInfo(false, 0, (entity) -> {
                    if (entity.acceptsStatusEffects)
                        entity.hp -= stm.get(user).getModAtk(user) * entity.statusEffectInfos.size;
                })
        );
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

        return new Move("Basilisk Strike", "The target was petrified!", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(petrify(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{barrage, doNothing, sparkle.copy(), bubble.copy(), sparkle, bubble})), new MoveInfo(false, 1, petrify().createStatusEffectInfo()));
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

        return new Move("Curse", nm.get(user).name + " placed a curse.", user, 5, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(curse(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{fire, spinningDiamond.copy(), sparkle.copy(), spinningDiamond, sparkle})), new MoveInfo(false, 0, curse().createStatusEffectInfo()));
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

        return new Move("Rest Body", nm.get(user).name + " began to rest its body.", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 1, 0, stm.get(e).getModMaxHp(e));

                        if (status.has(e))
                            status.get(e).removeAll(e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{sparkle, explode,  returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.hp = MathUtils.clamp(entity.hp + 1, 0, entity.maxHp);
                    if (entity.acceptsStatusEffects)
                        entity.statusEffectInfos.clear();
                })
        );
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

        return new Move("Ignite", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.2f))
                            status.get(enemy).addStatusEffect(burn(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{redSparkleOut, explode, smallBooms, explodeBig, largerRadiusBooms})), new MoveInfo(false, 1, (entity) -> {
            if (entity.acceptsStatusEffects && MathUtils.randomBoolean(.2f)) {
                entity.statusEffectInfos.add(burn().createStatusEffectInfo());
            }}));
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

        return new Move("Drench", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
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

        return new Move("Meteor Shower", user, 6, new Array<BoardPosition>(new BoardPosition[]{
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

        return new Move("Dragon Breath", nm.get(user).name + " spewed dragon breath!", user, 1,
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

        return new Move("Toxic Breath", nm.get(user).name + " spewed a poisonous breath!", user, 2,
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)
                }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(poison(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, ripples})), new MoveInfo(false, 0, poison().createStatusEffectInfo()));
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

        return new Move("Fresh Breath", nm.get(user).name + " breathed refreshing air!", user, 1,
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
                new MoveInfo(false, 0, (entity -> {
                    if (entity.acceptsStatusEffects)
                        entity.statusEffectInfos.clear();
                }))
        );
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

        return new Move("Spa Breath", nm.get(user).name + " breathed a soothing air!", user, 2,
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
                new MoveInfo(false, 0, (entity) -> {
                    entity.hp = MathUtils.clamp(entity.hp + 3, 0, entity.maxHp);
                })
        );
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

        return new Move("Restore", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp += stm.get(enemy).getModMaxHp(enemy) / 2, 0, stm.get(enemy).getModMaxHp(enemy));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlue, largeSparkle, circles, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.hp = MathUtils.clamp(entity.hp + entity.maxHp / 2, 0, entity.maxHp);
                })
        );
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

        return new Move("Regen", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(regeneration(), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToGreen, regenParticles1, regenParticles2, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                if (entity.acceptsStatusEffects)
                    entity.statusEffectInfos.add(regeneration().createStatusEffectInfo());
                })
        );
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        return new Move("Boost", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(regeneration(), enemy);
                            status.get(enemy).addStatusEffect(speedUp(), enemy);
                            status.get(enemy).addStatusEffect(attackUp(), enemy);
                            status.get(enemy).addStatusEffect(guardUp(), enemy);

                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, ripple.copy(), regenParticles1, ripple.copy(), regenParticles2, ripple.copy(),
                        returnToNormalGradual, returnToNormal, rippleGold})),
                new MoveInfo(false, 0, (entity) -> {
                    if (entity.acceptsStatusEffects) {
                        entity.statusEffectInfos.add(regeneration().createStatusEffectInfo());
                        entity.statusEffectInfos.add(speedUp().createStatusEffectInfo());
                        entity.statusEffectInfos.add(attackUp().createStatusEffectInfo());
                        entity.statusEffectInfos.add(guardUp().createStatusEffectInfo());
                    }
                })
        );
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

        return new Move("Transfer", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                new MoveInfo(false, 0, (entity) -> {
                    entity.sp += 2;
                }));
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

        return new Move("Reflect Move", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (mvm.has(enemy)) {
                            if (mvm.get(e).moveList.size >= 4)
                                mvm.get(e).moveList.set(3, mvm.get(enemy).moveList.get(0).createCopy(e));
                            else
                                mvm.get(e).moveList.add(mvm.get(enemy).moveList.get(0).createCopy(e));

                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{mirror, largeSparkle})), new MoveInfo(false, 0, (entity) -> entity.arbitraryValue += 50));
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

        return new Move("Mirror Move", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (mvm.has(enemy)) {
                            if (mvm.get(e).moveList.size >= 4)
                                mvm.get(e).moveList.set(3, mvm.get(enemy).moveList.get(mvm.get(enemy).moveList.size - 1).createCopy(e));
                            else
                                mvm.get(e).moveList.add(mvm.get(enemy).moveList.get(mvm.get(enemy).moveList.size - 1).createCopy(e));
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{mirror, largeSparkle})), new MoveInfo(false, 0, (entity) -> entity.arbitraryValue += 50));
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
        }, 3f, 1);

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

        return new Move("Roulette Reflect", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (mvm.has(enemy)) {
                            mvm.get(e).moveList.set(3, mvm.get(enemy).moveList.get(MathUtils.random(0, mvm.get(enemy).moveList.size - 1)).createCopy(e));
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{mirror, spinning})), new MoveInfo(false, 0, (entity) -> entity.arbitraryValue += 50));
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

        return new Move("Slam", nm.get(user).name + " slammed into the opponent", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Heavy Slam", nm.get(user).name + " slammed into the opponent", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Claw", nm.get(user).name + " slashed its claws!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Claw", nm.get(user).name + " slashed its claws!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
        return new Move("Monoplode+", nm.get(user).name + " uses a defense piercing spell!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);

                        if (status.has(e))
                            status.get(e).addStatusEffect(defenseless(), e);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles, explode, explodeBig})), new MoveInfo(true, 1));
    }
    //---Tower Waves
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
        
        return new Move("Power Wave", "The " + nm.get(user).name + " emitted a strengthening wave!", user, 0,
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
                            status.get(enemy).addStatusEffect(attackUp2(), enemy);
                            status.get(enemy).addStatusEffect(speedUp2(), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                        new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                        new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
                }),
                new Array<VisualEvent>(new VisualEvent[]{wave})), new MoveInfo(false, 0, new StatusEffectInfo[] {attackUp2().createStatusEffectInfo(), speedUp2().createStatusEffectInfo()},
                (entity) -> {
                    //should always use it
                    if (entity.team == 1)
                        entity.arbitraryValue += 1000;
                    else
                        entity.arbitraryValue += 100;
                }));
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

        return new Move("Weaken Wave", "The " + nm.get(user).name + " emitted a weakening wave!", user, 0,
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
                            status.get(enemy).addStatusEffect(offenseless2(), enemy);
                            status.get(enemy).addStatusEffect(paralyze(), enemy);
                            status.get(enemy).addStatusEffect(defenseless2(), enemy);

                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
        }),
                new Array<VisualEvent>(new VisualEvent[]{wave})), new MoveInfo(false, 0, new StatusEffectInfo[] {
                        offenseless2().createStatusEffectInfo(), paralyze().createStatusEffectInfo(), defenseless2().createStatusEffectInfo()},
                (entity) -> {
                    //should always use it
                    if (entity.team == 1)
                        entity.arbitraryValue += 1000;
                    else
                        entity.arbitraryValue += 100;
                }));
    }

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

        return new Move("War Wave", "The " + nm.get(user).name + " emitted a dangerous wave!", user, 1,
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
                            status.get(enemy).addStatusEffect(berserk(), enemy);
                        }

                        if (status.has(e) && !status.get(e).statusEffects.containsKey("Defenseless"))
                            status.get(e).addStatusEffect(defenseless(), e);

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
        }),
                new Array<VisualEvent>(new VisualEvent[]{wave})), new MoveInfo(false, 0, new StatusEffectInfo[] {berserk().createStatusEffectInfo(), speedUp2().createStatusEffectInfo()},
                (entity) -> {

                }));
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
        }, .06f, 8);

        return new Move("Slash", nm.get(user).name + " slashed the opponent", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                        return;
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

        return new Move("Chain Fire", user, 2, new Array<BoardPosition>(
                new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0), new BoardPosition(-4, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * .5f - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(burn(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0), new BoardPosition(-4, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{fire, explode, sparkles})), new MoveInfo(false, .5f, burn().createStatusEffectInfo()));
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
                        return;
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
                        return;
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

        return new Move("Flame Charge", nm.get(user).name + " began heating up!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).addStatusEffect(speedUp2(), e);
                            status.get(e).addStatusEffect(attackUp2(), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        changeToBlack, fire, smoke, fire.copy(), smoke.copy(), fire.copy(), smoke.copy(), fire.copy(), smoke.copy(.01f, 15),
                        explode, smokeOut, returnToNormal
                })),
                new MoveInfo(false, 0, speedUp2().createStatusEffectInfo(), attackUp2().createStatusEffectInfo()));
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
                        return;
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
        return new Move("Blue Flame", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
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

        return new Move("Hammer Strike", nm.get(user).name + " slammed the opponent with its hammer!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless2(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, bam, sparkle})), new MoveInfo(false, 1, defenseless2().createStatusEffectInfo()));
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

        return new Move("Gather", nm.get(user).name + " began gathering water molecules to regenerate itself!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (stm.has(e))
                            stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 5, 0, stm.get(e).getModMaxHp(e));
                        if (status.has(e)) {
                            status.get(e).addStatusEffect(regeneration(), e);
                            status.get(e).addStatusEffect(guardUpAmp(), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        changeToBlack, waterBall, particles, innerBubble, returnToNormalGradual, returnToNormal
                })),
                new MoveInfo(false, 0, new StatusEffectInfo[]{regeneration().createStatusEffectInfo(), guardUpAmp().createStatusEffectInfo()}, (entity) -> {
                    entity.hp = MathUtils.clamp(entity.hp + 5, 0, entity.maxHp);
                    if (entity.hp < 6) //encourage use of this move if low on health
                        entity.arbitraryValue += 300;
                }));
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
                        return;
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

        return new Move("Strengthen", nm.get(user).name + " increased its attack power using water molecules!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (stm.has(e))
                            stm.get(e).atk++;
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        changeToBlack, waterBall, particles, smoke, explode, smoke.copy(), returnToNormalGradual, returnToNormal
                })),
                new MoveInfo(false, 0, (entity) -> {
                            //Uses it less when attack is higher
                            entity.attack++;
                            if (entity.attack < 10) {
                                if (MathUtils.randomBoolean())
                                    entity.arbitraryValue += 100;
                                else
                                    entity.arbitraryValue += 15;
                            } else {
                                    entity.arbitraryValue += 15;
                            }

                        }));
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
                        return;
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

        return new Move("KO Strike", nm.get(user).name + " knocked the opponent out!", user, 7, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Spear Stab", nm.get(user).name + " pierced defenses!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                        return;
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

        return new Move("Flux Wave", user, 1, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(0, -1), new BoardPosition(0, -2), new BoardPosition(-1, -2), new BoardPosition(1, -2), new BoardPosition(0, -3)
            }),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(paralyze(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(0, -1), new BoardPosition(0, -2), new BoardPosition(-1, -2), new BoardPosition(1, -2), new BoardPosition(0, -3)}),
                new Array<VisualEvent>(new VisualEvent[]{projectile, rippleOut.copy(), shocking.copy(), rippleOut, shocking, zags})),
                new MoveInfo(false, 1, (paralyze().createStatusEffectInfo())
        ));
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
                        return;
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

        return new Move("Disrupt", nm.get(user).name + " emitted a disruptive wave!", user, 1,
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
                            if (status.get(enemy).statusEffects.size > 0)
                                status.get(enemy).removeAll(enemy);
                            status.get(enemy).addStatusEffect(offenseless2(), enemy);
                            status.get(enemy).addStatusEffect(defenseless2(), enemy);
                            status.get(enemy).addStatusEffect(paralyze(), enemy);
                            status.get(enemy).addStatusEffect(inept(), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1), new BoardPosition(0, -1), new BoardPosition(1, -1),
                new BoardPosition(1, 0), new BoardPosition(1, 1), new BoardPosition(0, 1), new BoardPosition(-1, 1),
                new BoardPosition(-2, 0), new BoardPosition(0, -2), new BoardPosition(2, 0), new BoardPosition(0, 2)
        }),
                new Array<VisualEvent>(new VisualEvent[]{wave, zags})), new MoveInfo(false, 0, (entity) -> {
                    if (entity.acceptsStatusEffects) {
                        if (entity.statusEffectInfos.size >= 1)
                            entity.statusEffectInfos.clear();
                        entity.statusEffectInfos.add(offenseless2().createStatusEffectInfo());
                        entity.statusEffectInfos.add(defenseless2().createStatusEffectInfo());
                        entity.statusEffectInfos.add(paralyze().createStatusEffectInfo());
                        entity.statusEffectInfos.add(inept().createStatusEffectInfo());

                        //encourage use if hits multiple entities
                        entity.arbitraryValue -= 30;
                    }
        }
        ));
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
                        return;
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

        return new Move("Polarize", nm.get(user).name + "'s charges become polarized!", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).removeAll(e);
                            status.get(e).addStatusEffect(charged(), e);
                            status.get(e).addStatusEffect(regenerationPlus(), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, rippleOut, charges, rippleOut.copy(), shocking, zags, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.statusEffectInfos.clear();
                    if (entity.acceptsStatusEffects) {
                        entity.statusEffectInfos.add(charged().createStatusEffectInfo());
                        entity.statusEffectInfos.add(regenerationPlus().createStatusEffectInfo());
                    }
                })
        );
    }
    //dragon
    public static Move dragonBreath2(Entity user) {
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
                                    MathUtils.clamp(color.a - 1f / 8, 0, 1));
                            animm.get(e).shadeColor = color;
                        }
                        pm.get(e).rotation += 9;
                    }
                }));
                engine.addEntity(clouds);
            }
        }, .03f, 36);

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
        }, .1f, 7);

        return new Move("Dragon Breath", nm.get(user).name + " spewed dragon breath!", user, 0,
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
                        return;
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
                        return;
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

        return new Move("Roar", nm.get(user).name + "'s roar scared the enemy!", user, 3,
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
                            status.get(enemy).addStatusEffect(offenseless(), enemy);
                            status.get(enemy).addStatusEffect(paralyze(), enemy);
                            status.get(enemy).addStatusEffect(shivers(), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, explode, explodeBig, ripples, sparks})),
                new MoveInfo(false, 0, shivers().createStatusEffectInfo(), paralyze().createStatusEffectInfo(), offenseless().createStatusEffectInfo()));
    }

    public static Move flash(Entity user) {
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
        }, .01f, 70);

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
        }, .004f, 60);

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
        }, .09f, 15);

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

        return new Move("Flash", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 1.5f, 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{sparkleOut, explode, smallBooms, flash, explodeBig, largerRadiusBooms, sparkle, floatUpDiamonds})), new MoveInfo(true, 1.5f));
    }

    //TODO redo
    public static Move raze(Entity user) {
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
                        return;
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
        }, .01f, 4);
        
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

        return new Move("Raze", nm.get(user).name + " unleashed a surge of energy", user, 0,
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
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 5, 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)}),
                new Array<VisualEvent>(new VisualEvent[]{firebreath, explosions, firebreath.copy(), fires, firebreath.copy(),
                        explosions.copy(), explosions.copy(), firebreath.copy(), fires.copy(), sparks})),
                new MoveInfo(true, 5));
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

        return new Move("Stone Glare", nm.get(user).name + " petrified the opponent with a glare!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(petrify(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, sparkle})), new MoveInfo(false, 0, petrify().createStatusEffectInfo()));
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
                        return;
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
                        return;
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

        return new Move("Roar", nm.get(user).name + " roared!", user, 1,
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
                            status.get(enemy).addStatusEffect(offenseless2(), enemy);
                            status.get(enemy).addStatusEffect(inept(), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{  new BoardPosition(-1, 0),
                new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, explode, explodeBig, ripples})), new MoveInfo(false, 0, offenseless2().createStatusEffectInfo(), inept().createStatusEffectInfo()));
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

        return new Move("Prepare", nm.get(user).name + " readied its body to pounce!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {

                        if (status.has(e))
                            status.get(e).addStatusEffect(speedUp2(), e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, shuffleBackForth, explode,  returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, speedUp2().createStatusEffectInfo()));
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

        return new Move("Ready", nm.get(user).name + " readied its body to attack!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {

                        if (status.has(e)) {
                            status.get(e).addStatusEffect(speedUp2(), e);
                            status.get(e).addStatusEffect(attackUp2(), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, shuffleBackForth, shuffleBackForth.copy(.01f, 30), explode,  returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, speedUp2().createStatusEffectInfo(), attackUp2().createStatusEffectInfo()));
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
                        return;
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
                        return;
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

        return new Move("Neo-Roar", nm.get(user).name + "'s roar electrified the air!", user, 1,
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
                            status.get(enemy).addStatusEffect(offenseless(), enemy);
                            status.get(enemy).addStatusEffect(paralyze(), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).shuffleAnimation != null)
                            vm.get(enemy).shuffleAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, 0),
                        new BoardPosition(-2, 1), new BoardPosition(-2, 0), new BoardPosition(-2, -1),
                        new BoardPosition(-3, 2), new BoardPosition(-3, 1), new BoardPosition(-3, 0), new BoardPosition(-3, -1), new BoardPosition(-3, -2)}),
                new Array<VisualEvent>(new VisualEvent[]{breath, explode, explodeBig, ripples, sparks})),
                new MoveInfo(false, 0, offenseless().createStatusEffectInfo(), paralyze().createStatusEffectInfo()));
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

        return new Move("Reflect Beam", nm.get(user).name + " shot off a beam of reflected light!", user, 2, new Array<BoardPosition>(
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

        return new Move("Reflect Beam", nm.get(user).name + " shot off a beam of reflected light!", user, 0, new Array<BoardPosition>(
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

        return new Move("Refract Beam", nm.get(user).name + " shot off a beam of refracted light!", user, 0, new Array<BoardPosition>(
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

        return new Move("Poison Punch", nm.get(user).name + " attacks!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean())
                            status.get(enemy).addStatusEffect(poison(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explosions})), new MoveInfo(false, 1, (entity) -> {
                    if (entity.acceptsStatusEffects && MathUtils.randomBoolean())
                        entity.statusEffectInfos.add(poison().createStatusEffectInfo());
        }));
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

        return new Move("Immobilize", "The target was drenched in immobilizing goo!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(paralyze(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{zag, sparkle, bubble, doNothing, explode})), new MoveInfo(false, 0, paralyze().createStatusEffectInfo()));
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

        return new Move("Stun Punch", nm.get(user).name + " attacks!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean())
                            status.get(enemy).addStatusEffect(paralyze(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explosions})), new MoveInfo(false, 1, (entity) -> {
            if (entity.acceptsStatusEffects && MathUtils.randomBoolean())
                entity.statusEffectInfos.add(paralyze().createStatusEffectInfo());
        }));
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

        return new Move("Recover", nm.get(user).name + " began to regenerate.", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 3, 0, stm.get(e).getModMaxHp(e));

                        if (status.has(e))
                            status.get(e).addStatusEffect(regeneration(), e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, sparkles, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, regeneration().createStatusEffectInfo(), (entity) -> {
                    entity.hp += 3;
                }));
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
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-140 * scale, 140 * scale), MathUtils.random(-140 * scale, 140 * scale))
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

        return new Move("Mystery Strike", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.4f)) {
                            if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(paralyze(), enemy);
                            else if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(burn(), enemy);
                            else if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(poison(), enemy);
                            else if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(toxic(), enemy);
                            else if (MathUtils.randomBoolean(33f))
                                status.get(enemy).addStatusEffect(curse(), enemy);
                            else
                                status.get(enemy).addStatusEffect(inept(), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{punch, explosions, explosionsLargeRad})), new MoveInfo(false, 1, (entity) -> {
                    if (entity.acceptsStatusEffects) {
                        if (MathUtils.randomBoolean(33f))
                            entity.statusEffectInfos.add(paralyze().createStatusEffectInfo());
                        else if (MathUtils.randomBoolean(33f))
                            entity.statusEffectInfos.add(burn().createStatusEffectInfo());
                        else if (MathUtils.randomBoolean(33f))
                            entity.statusEffectInfos.add(poison().createStatusEffectInfo());
                        else if (MathUtils.randomBoolean(33f))
                            entity.statusEffectInfos.add(toxic().createStatusEffectInfo());
                        else if (MathUtils.randomBoolean(33f))
                            entity.statusEffectInfos.add(curse().createStatusEffectInfo());
                        else
                            entity.statusEffectInfos.add(inept().createStatusEffectInfo());
                    }
        }));
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
        return new Move("Accursed Sludge", "The target was cursed!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(curse(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sludge, explode})), new MoveInfo(false, 0, curse().createStatusEffectInfo()));
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

        return new Move("Sludge Throw", nm.get(user).name + " threw sludge!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.333f))
                            status.get(enemy).addStatusEffect(poison(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{booms, bubble, sludge})), new MoveInfo(false, 1, (entity) -> {
                    if (entity.acceptsStatusEffects && MathUtils.randomBoolean(.333f))
                        entity.statusEffectInfos.add(poison().createStatusEffectInfo());
                }));
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

        return new Move("Suppress", "The target's attack was lowered!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(offenseless(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sludge, ripple})), new MoveInfo(false, 0, offenseless().createStatusEffectInfo()));
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

        return new Move("Suppress", "The target's defense was lowered!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless2(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sludge, ripple})), new MoveInfo(false, 0, defenseless2().createStatusEffectInfo()));
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

        return new Move("Sludge Throw", nm.get(user).name + " threw sludge!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.333f)) {
                            if (MathUtils.randomBoolean())
                                status.get(enemy).addStatusEffect(poison(), enemy);
                            else
                                status.get(enemy).addStatusEffect(paralyze(), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{booms, bubble, sludge})), new MoveInfo(false, 1, (entity) -> {
            if (entity.acceptsStatusEffects && MathUtils.randomBoolean(.333f)) {
                if (MathUtils.randomBoolean())
                    entity.statusEffectInfos.add(poison().createStatusEffectInfo());
                else
                    entity.statusEffectInfos.add(paralyze().createStatusEffectInfo());
            }
        }));
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

        return new Move("Toxic Throw", nm.get(user).name + " threw toxic waste!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy) && MathUtils.randomBoolean(.666f)) {
                            if (MathUtils.randomBoolean(.4f))
                                status.get(enemy).addStatusEffect(toxic(), enemy);
                            else
                                status.get(enemy).addStatusEffect(poison(), enemy);
                        }

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{booms, bubble, sludge})), new MoveInfo(false, 1, (entity) -> {
            if (entity.acceptsStatusEffects && MathUtils.randomBoolean(.666f)) {
                if (MathUtils.randomBoolean(.4f))
                    entity.statusEffectInfos.add(toxic().createStatusEffectInfo());
                else
                    entity.statusEffectInfos.add(poison().createStatusEffectInfo());
            }
        }));
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

        return new Move("Suppress", "A move is now unusable!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
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
                new Array<VisualEvent>(new VisualEvent[]{explode, bubble, sludge, explodeBig})), new MoveInfo(false, 0, (entity) -> entity.arbitraryValue -= 50));
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

        return new Move("Medical Throw", "The target was hit with medical substances!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp += 3, 0, stm.get(enemy).getModMaxHp(enemy));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{bubble, sludge, shine, ripple})), new MoveInfo(false, 0, (entity) -> {entity.hp += 3;}));
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

        return new Move("Slam", nm.get(user).name + " slammed into the opponent", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Slam", nm.get(user).name + " slammed into the opponent", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Heavy Slam", nm.get(user).name + " slammed into the opponent", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Guard", nm.get(user).name + " raised its guard.", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e))
                            status.get(e).addStatusEffect(guardUp(), e);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{moveRight, moveLeft, moveRight.copy(), moveLeft.copy()})),
                new MoveInfo(false, 0, guardUp().createStatusEffectInfo()));
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

        return new Move("Super Guard", nm.get(user).name + " assumed a defensive stance.", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).addStatusEffect(guardUp(), e);
                            status.get(e).addStatusEffect(regeneration(), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, moveRight, moveLeft, moveRight.copy(), moveLeft.copy()})),
                new MoveInfo(false, 0, guardUp().createStatusEffectInfo(), regeneration().createStatusEffectInfo()));
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

        return new Move("Ultimate Guard", nm.get(user).name + " assumed a perfect defensive stance.", user, 6, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        if (status.has(e)) {
                            status.get(e).addStatusEffect(guardUp2(), e);
                            status.get(e).addStatusEffect(regeneration(), e);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, moveRight, moveLeft, moveRight.copy(), moveLeft.copy()})),
                new MoveInfo(false, 0, guardUp2().createStatusEffectInfo(), regeneration().createStatusEffectInfo()));
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

        return new Move("Laser Beam", nm.get(user).name + " shot a laser beam!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0)}),
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

        return new Move("Slash", nm.get(user).name + " attacks!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                slashes.copy(), moveLeft.copy(), slashes.copy(), moveRight.copy(), slashes.copy(), moveLeft.copy()})), new MoveInfo(false, 1));
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

        return new Move("Toxic Slash", nm.get(user).name + " attacks with a poison!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(poison(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{slashes, moveRight, slashes.copy(), moveLeft, slashes.copy(), moveRight.copy(),
                        slashes.copy(), moveLeft.copy(), slashes.copy(), moveRight.copy(), slashes.copy(), moveLeft.copy()})), new MoveInfo(false, 1, poison().createStatusEffectInfo()));
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

        return new Move("Immobite", "The target's attack paralyzed the opponent!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(paralyze(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{slashes, zag, doNothing, explode})), new MoveInfo(false, 1, paralyze().createStatusEffectInfo()));
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

        return new Move("Steal Skill", nm.get(user).name + " tried to steal skill points!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                        moveRight.copy(), shine, slashes.copy(), shine.copy(), moveLeft.copy(), shine.copy()
                })),
                new MoveInfo(false, .5f, (entity) -> {entity.sp -= MathUtils.random(0, 3);}));
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

        return new Move("Steal Health", nm.get(user).name + " tried to steal health!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                        moveRight.copy(), shine, slashes.copy(), shine.copy(), moveLeft.copy(), shine.copy()
                })),
                new MoveInfo(false, 1));
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
        return new Move("Demoralizing Blow", nm.get(user).name + " delivered a demoralizing blow!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 1.5f, 0, 999);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(inept(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{glow, glow2, sliceVis, crossSliceVis})), new MoveInfo(true, 1.5f, inept().createStatusEffectInfo()));
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

        return new Move("Slash", nm.get(user).name + " attacks!", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                        slashes.copy(), moveLeft.copy(), slashes.copy(), moveRight.copy(), slashes.copy(), moveLeft.copy()})), new MoveInfo(false, 1));
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

        return new Move("Crush Claw", nm.get(user).name + " crushed the opponent!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(defenseless2(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, sphereOut, claw})), new MoveInfo(false, 1, defenseless2().createStatusEffectInfo()));
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


        return new Move("Penetrate", nm.get(user).name + " attacked through defenses!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Judging Glare", nm.get(user).name + " gave the opponent a judging glare!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy))
                            status.get(enemy).addStatusEffect(inept(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{explode, sparkle})), new MoveInfo(false, 0, inept().createStatusEffectInfo()));
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

        return new Move("Beam", nm.get(user).name + " shot off a beam!", user, 1, new Array<BoardPosition>(
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
                            status.get(enemy).addStatusEffect(paralyze(), enemy);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0), new BoardPosition(-2, 0), new BoardPosition(-3, 0), new BoardPosition(-4, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{preBoom, explode1, sparkle1, explode2, sparkle2, explode3, sparkle3, explode4, sparkle4})), new MoveInfo(false, 1,
                (entity) -> {
                    if (entity.acceptsStatusEffects && MathUtils.randomBoolean())
                        entity.statusEffectInfos.add(paralyze().createStatusEffectInfo());
                }
        ));
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
        return new Move("Monoplode", nm.get(user).name + " uses a spell!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
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
        return new Move("Monoplode", nm.get(user).name + " uses a spell!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
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
        return new Move("Monoplode", nm.get(user).name + " uses a spell!", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
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


        return new Move("Monopierce", nm.get(user).name + " uses a mystifying spell!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, ripples, doNothing, returnToNormalGradual, returnToNormal})), new MoveInfo(true, 1));
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
                //Vector2 tileCenter = t.localToStageCoordinates(new Vector2(t.getWidth() / 2f, t.getHeight() / 2f));
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


        return new Move("Monoflash", nm.get(user).name + " uses a enigmatic spell!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
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
                        return;
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

        return new Move("Combobulate", nm.get(user).name + " uses enigmatic wizardry!", user, 4,
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
                            status.get(enemy).addStatusEffect(defenseless2(), enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(-1, -1), new BoardPosition(-2, -2), new BoardPosition(-3, -3),
                        new BoardPosition(-1, 1), new BoardPosition(-2, 2), new BoardPosition(-3, 3)
                }),
                new Array<VisualEvent>(new VisualEvent[]{
                        explode, nothing, flash, setInvert, floatUpDiamonds, sparkle, ripples, floatDiamonds,
                        flashBack, revertShading, nothing.copy()
                })), new MoveInfo(true, 3, defenseless2().createStatusEffectInfo()));
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
        return new Move("Enchant", nm.get(user).name + " used a supportive spell!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(regeneration(), enemy);
                            status.get(enemy).addStatusEffect(attackUp3(), enemy);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, sparkles, nothing, returnToNormalGradual, returnToNormal})), new MoveInfo(false, 0, regeneration().createStatusEffectInfo(), attackUp3().createStatusEffectInfo()));
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
        return new Move("Ward", nm.get(user).name + " used a defensive spell!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(speedUp2(), enemy);
                            status.get(enemy).addStatusEffect(guardUp2(), enemy);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToBlack, sparkles, nothing, returnToNormalGradual, returnToNormal})), new MoveInfo(false, 0, guardUp2().createStatusEffectInfo(), speedUp2().createStatusEffectInfo()));
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


        return new Move("Disarm", nm.get(user).name + " uses a weakening spell!", user, 1, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy)) {
                            status.get(enemy).addStatusEffect(offenseless(), enemy);
                            status.get(enemy).addStatusEffect(defenseless2(), enemy);
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{flash, changeToBlack, returnToNormalGradual, returnToNormal})), new MoveInfo(false, 0, offenseless().createStatusEffectInfo(), defenseless2().createStatusEffectInfo()));
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
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

                if (!boards.getBoard().getTile(bp.r, bp.c).isOccupied())
                    return;

                Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                am.get(enemy).actor.shade(BattleScreen.getShadeColorBasedOnState(enemy));
            }
        }, .05f, 1);

        return new Move("Full Restore", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (status.has(enemy) && status.get(enemy).statusEffects.size > 0)
                            status.get(enemy).removeAll(enemy);

                        if (stm.has(enemy))
                            stm.get(enemy).hp = stm.get(enemy).getModMaxHp(enemy);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{changeToGreen, sparkle, explode, returnToNormalGradual, returnToNormal})),
                new MoveInfo(false, 0, (entity) -> {
                    entity.hp = entity.maxHp;
                    if (entity.acceptsStatusEffects)
                        entity.statusEffectInfos.clear();
                }));
    }
    //endregion
}
