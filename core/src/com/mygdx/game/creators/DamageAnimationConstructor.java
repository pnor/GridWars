package com.mygdx.game.creators;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.*;
import com.mygdx.game.misc.ColorUtils;
import com.mygdx.game.misc.EventCompUtil;
import com.mygdx.game.misc.GameEvent;
import com.mygdx.game.move_related.VisualEffect;
import com.mygdx.game.move_related.VisualEvent;
import com.mygdx.game.move_related.Visuals;
import com.mygdx.game.screens.BattleScreen;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.atlas;

/**
 * CLass containing builder methods for animations played when getting hurt or dying.
 *
 * @author Phillip O'Reggio
 */
public class DamageAnimationConstructor {
    private static boolean ready;

    private static BoardManager boards;
    private static Engine engine;
    private static float scale;

    /**
     * Readies the {@link MoveConstructor} for use.
     * @param scaleFactor scale of board
     * @param manager BoardManager
     * @param eng Engine
     */
    public static void initialize(float scaleFactor, BoardManager manager, Engine eng) {
        scale = scaleFactor;
        boards = manager;
        engine = eng;
        ready = true;
    }

    /**
     * Clears static fields in {@link MoveConstructor}
     */
    public static void clear() {
        scale = 1;
        boards = null;
        engine = null;
        ready = false;
    }

    public static boolean isReady() {
        return ready;
    }


    /**
     * Creates highscores generic damage animation
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
     * Creates highscores shuffling animation for status effects
     * @param user Entity that is being damaged
     * @return damage animation {@code Visuals}
     */
    public static Visuals shuffleAnimation(Entity user) {
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

        return new Visuals(user, null,
                new Array<VisualEvent>(new VisualEvent[]{moveRight.copy(.001f, 1), moveLeft, moveRight, moveLeft.copy(), moveRight.copy(1)
                }));
    }

    /**
     * Creates highscores generic damage animation
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

    /**
     * Creates the explosive death animation
     * @param user Entity that is being killed
     * @param color of the explosions
     * @return death animation {@code Visuals}
     */
    public static Visuals deathAnimationExplosive(Entity user, Color color) {
        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(Color.RED);
            }
        }, .2f, 1);

        VisualEvent explosions = new VisualEvent(new VisualEffect() {
            boolean right;

            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .11f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        color,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);

                if (right)
                    am.get(user).actor.moveBy(4 * scale, 0);
                else
                    am.get(user).actor.moveBy(-4 * scale, 0);
                right = !right;

            }
        }, .2f, 6);

        VisualEvent explosionsFast = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .11f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        color,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .05f, 18);

        VisualEvent endSparkles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-90 * scale, 90 * scale), MathUtils.random(-90 * scale, 90 * scale)),
                        entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("sparkle");
                sprite.setOriginCenter();
                sprite.setColor(color);
                boom.add(new SpriteComponent(sprite));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(boom);
            }
        }, .005f, 30);

        VisualEvent fadeAndBlacken = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(
                        new Color(am.get(user).actor.getColor().r - .01f, am.get(user).actor.getColor().g - .1f,
                                am.get(user).actor.getColor().b - .1f, am.get(user).actor.getColor().a - .1f));
            }
        }, .1f, 9);

        return new Visuals(user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed, explosions, explosionsFast, endSparkles, fadeAndBlacken.copy(.275f, 1), fadeAndBlacken}));
    }

    /**
     * Creates the final boss death animation
     * @param user Entity that is being killed
     * @return death animation {@code Visuals}
     */
    public static Visuals deathAnimationFinal(Entity user) {
        VisualEvent initialBlack = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(Color.BLACK);
            }
        }, .2f, 1);

        VisualEvent explosionsSlow = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .22f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("diamondBoom"),
                                atlas.findRegion("diamondBoom2"),
                                atlas.findRegion("diamondBoom3"),
                                atlas.findRegion("diamondBoom4"),
                                atlas.findRegion("diamondBoom5"),
                                atlas.findRegion("diamondBoom6")},
                        ColorUtils.HSV_to_RGB(MathUtils.random(0, 100), 100, 100),
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(10)));
                engine.addEntity(boom);
            }
        }, .15f, 20);

        VisualEvent explosionsFast = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-140 * scale, 140 * scale), MathUtils.random(-140 * scale, 140 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .11f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .03f, 40);

        VisualEvent singleLargeExplosion = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
                boom.add(new LifetimeComponent(0, .32f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("BWexplode"),
                                atlas.findRegion("BWexplode2"),
                                atlas.findRegion("BWexplode3"),
                                atlas.findRegion("BWexplode4"),
                                atlas.findRegion("BWexplode5"),
                                atlas.findRegion("BWexplode6")},
                        Color.BLACK,
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.03f, true, EventCompUtil.fadeOut(10)));
                engine.addEntity(boom);
            }
        }, .03f, 1);

        VisualEvent endSparklesSmall = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-90 * scale, 90 * scale), MathUtils.random(-90 * scale, 90 * scale)),
                        entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("sparkle");
                sprite.setOriginCenter();
                sprite.setColor(Color.RED);
                boom.add(new SpriteComponent(sprite));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(boom);
            }
        }, .005f, 30);

        VisualEvent endSparklesLarge = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-300 * scale, 300 * scale), MathUtils.random(-300 * scale, 300 * scale)),
                        entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .3f));
                Sprite sprite = atlas.createSprite("sparkle");
                sprite.setOriginCenter();
                sprite.setColor(ColorUtils.HSV_to_RGB(MathUtils.random(0, 100), 100, 100));
                boom.add(new SpriteComponent(sprite));
                boom.add(new EventComponent(.1f, true, EventCompUtil.fadeOut(3)));

                engine.addEntity(boom);
            }
        }, .01f, 30);

        VisualEvent fadeAndBlacken = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(
                        new Color(am.get(user).actor.getColor().r - .01f, am.get(user).actor.getColor().g - .1f,
                                am.get(user).actor.getColor().b - .1f, am.get(user).actor.getColor().a - .1f));
            }
        }, .1f, 1);

        return new Visuals(user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialBlack, explosionsSlow, explosionsFast, singleLargeExplosion,
                        endSparklesSmall, endSparklesLarge, fadeAndBlacken.copy(.275f, 1), fadeAndBlacken}));
    }

    /**
     * Creates the death animation with steam coming out
     * @param user Entity that is being killed
     * @return death animation {@code Visuals}
     */
    public static Visuals deathAnimationSteamy(Entity user) {
        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(Color.RED);
            }
        }, .2f, 1);
        
        VisualEvent explosions = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
                boom.add(new PositionComponent(tilePosition.cpy().add(MathUtils.random(-50 * scale, 50 * scale), MathUtils.random(-50 * scale, 50 * scale))
                        , entitySize.x, entitySize.y, 0));
                boom.add(new LifetimeComponent(0, .11f));
                boom.add(new AnimationComponent(.02f,
                        new TextureRegion[]{atlas.findRegion("explode"),
                                atlas.findRegion("explode2"),
                                atlas.findRegion("explode3"),
                                atlas.findRegion("explode4"),
                                atlas.findRegion("explode5"),
                                atlas.findRegion("explode6")},
                        Animation.PlayMode.NORMAL));
                boom.add(new EventComponent(.02f, true, EventCompUtil.fadeOut(5)));
                engine.addEntity(boom);
            }
        }, .05f, 3);

        VisualEvent smoke = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                BoardPosition bp = bm.get(user).pos.copy();
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
        }, .03f, 3);

        VisualEvent fadeAndBlacken = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions) {
                am.get(user).actor.shade(
                        new Color(am.get(user).actor.getColor().r - .01f, am.get(user).actor.getColor().g - .1f,
                                am.get(user).actor.getColor().b - .1f, am.get(user).actor.getColor().a - .1f));
            }
        }, .1f, 10);

        return new Visuals(user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed, explosions, smoke, explosions.copy(), smoke.copy(),
                        explosions.copy(), smoke.copy(), explosions.copy(), smoke.copy(), fadeAndBlacken}));
    }
}
