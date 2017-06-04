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
    //Damage related animations ------------
    /**
     * Creates a generic damage animation
     * @param user Entity that is being damaged
     * @param engine {@code Engine}
     * @param stage {@code Stage}
     * @return damage animation {@code Visuals}
     */
    public static Visuals damageAnimation(Entity user, Engine engine, Stage stage) {

        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                am.get(user).actor.shade(new Color(.9f, .1f, .1f, 1));
            }
        }, .001f, 1);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .05f, 8);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                am.get(user).actor.shade(BattleScreen.getShadeColorBasedOnState(user));
            }
        }, .05f, 1);

        return new Visuals(user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed, returnToNormalGradual.copy(.15f, 1), returnToNormalGradual, returnToNormal}));
    }

    /**
     * Creates a generic damage animation
     * @param user Entity that is being damaged
     * @param engine {@code Engine}
     * @param stage {@code Stage}
     * @return damage animation {@code Visuals}
     */
    public static Visuals heavyDamageAnimation(Entity user, Engine engine, Stage stage) {
        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                    am.get(user).actor.shade(new Color(.8f, 0, 0, 1));
            }
        }, .001f, 1);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                am.get(user).actor.moveBy(3, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                am.get(user).actor.moveBy(-3, 0);
            }
        }, .05f, 2);

        VisualEvent returnToNormalGradual = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(BattleScreen.getShadeColorBasedOnState(user), .1f));
            }
        }, .02f, 9);

        VisualEvent returnToNormal = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
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
     * @param engine {@code Engine}
     * @param stage {@code Stage}
     * @return death animation {@code Visuals}
     */
    public static Visuals deathAnimation(Entity user, Engine engine, Stage stage) {
        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                am.get(user).actor.shade(Color.RED);
            }
        }, .001f, 1);

        VisualEvent fadeAndBlacken = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                am.get(user).actor.shade(
                        new Color(am.get(user).actor.getColor().r - .01f, am.get(user).actor.getColor().g - .1f,
                                am.get(user).actor.getColor().b - .1f, am.get(user).actor.getColor().a - .1f));
            }
        }, .1f, 9);

        return new Visuals(user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed, fadeAndBlacken.copy(.275f, 1), fadeAndBlacken}));
    }

    //Status Effects ------------
    public static StatusEffect poison() {
        return new StatusEffect("Poison", 3, new LerpColor(Color.GREEN, new Color(201f / 255f, 1f, 0f, 1f)), (e) -> {
            stm.get(e).hp -= 1;
            if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                vm.get(e).heavyDamageAnimation.setPlaying(true, true);
        });
    }

    public static StatusEffect burn() {
        return new StatusEffect("Burn", 3, new LerpColor(Color.RED, new Color(1, 125f / 255f, 0f, 1f), .3f, Interpolation.sineOut), (e) -> {
            if (stm.has(e) && MathUtils.randomBoolean()) {
                stm.get(e).hp -= 1;
                if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                    vm.get(e).heavyDamageAnimation.setPlaying(true, true);
            }
        });
    }

    public static StatusEffect paralyze() {
        return new StatusEffect("Paralyze", 3, new LerpColor(Color.RED, new LerpColor(Color.GRAY, Color.YELLOW, .4f, Interpolation.exp5In)), (e) -> {/*nothing*/});
    }

    public static StatusEffect petrify() {
        return new StatusEffect("Petrify", 2, new Color(214f / 255f, 82f / 255f, 0, 1), (e) -> {/*nothing*/});
    }

    public static StatusEffect stillness() {
        return new StatusEffect("Stillness", 2, new LerpColor(Color.WHITE, new Color(0, 140f / 255f, 1f, 1f), .7f,  Interpolation.sine), (e) -> {/*nothing*/});
    }

    public static StatusEffect curse() {
        return new StatusEffect("Curse", 2, new LerpColor(Color.GRAY, Color.BLACK, .5f, Interpolation.fade), (e) -> {/*nothing*/});
    }

    public static StatusEffect defenseless() {
        return new StatusEffect("Defenseless", 1, new LerpColor(Color.WHITE, Color.NAVY, .5f, Interpolation.fade), (e) -> {/*nothing*/});
    }

    //Moves ------------

    /*
    Note : the line below
        Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - (half sprite width), t.getHeight() / 2 - (half sprite height)));
    Gets the position of the center of the tile, then subtracts half the size of the sprite being drawn from it.
     */

    public static Move Tackle(Entity user, Engine engine, Stage stage) {
        VisualEvent TackleVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * BoardComponent.boards.getBoard().getScale(), 60 * BoardComponent.boards.getBoard().getScale());
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

    public static Move StarSpin(Entity user, Engine engine, Stage stage) {
        VisualEvent spin = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2, BoardComponent.boards.getTileHeight() / 2);
                Entity star = new Entity();
                star.add(new PositionComponent(new Vector2(tilePosition.cpy().x,
                        tilePosition.cpy().y),
                        45 * BoardComponent.boards.getBoard().getScale(),
                        45 * BoardComponent.boards.getBoard().getScale(),
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

    public static Move swordSlice(Entity user, Engine engine, Stage stage) {
        VisualEvent sliceVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(0, 0));
                tilePosition.add(BoardComponent.boards.getTileWidth() / 2 - (22.5f * BoardComponent.boards.getBoard().getScale()),
                        BoardComponent.boards.getTileHeight() / 2 - (22.5f * BoardComponent.boards.getBoard().getScale()));
                Entity slash = new Entity();
                slash.add(new PositionComponent(tilePosition, 45 * BoardComponent.boards.getBoard().getScale(), 45 * BoardComponent.boards.getBoard().getScale(), 0));
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
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - 22.5f, t.getHeight() / 2 - 22.5f));
                tilePosition.x += t.getWidth() / 4; //Move rotated sprite to be aligned
                Entity crossSlash = new Entity();
                crossSlash.add(new PositionComponent(tilePosition, 45 * BoardComponent.boards.getBoard().getScale(), 45 * BoardComponent.boards.getBoard().getScale(), 90));
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

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{sliceVis, crossSliceVis})));
    }

    public static Move pierceSwordSlice(Entity user, Engine engine, Stage stage) {
        //Visuals---
        VisualEvent glow = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {

                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * BoardComponent.boards.getBoard().getScale(), 60 * BoardComponent.boards.getBoard().getScale());
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
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * BoardComponent.boards.getBoard().getScale(), 60 * BoardComponent.boards.getBoard().getScale());
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
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * BoardComponent.boards.getBoard().getScale(), 60 * BoardComponent.boards.getBoard().getScale());
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
        return new Move("Piercing Slice", nm.get(user).name + " delivered a piercing blow!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{glow ,sliceVis, crossSliceVis})));
    }

    public static Move guardPiercer(Entity user, Engine engine, Stage stage) {
        //Visuals---
        VisualEvent circles = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(70 * BoardComponent.boards.getBoard().getScale(), 70 * BoardComponent.boards.getBoard().getScale());
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
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * BoardComponent.boards.getBoard().getScale(), 60 * BoardComponent.boards.getBoard().getScale());
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
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = BoardComponent.boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(60 * BoardComponent.boards.getBoard().getScale(), 60 * BoardComponent.boards.getBoard().getScale());
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

                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{circles.copy(.1f, 5), circles ,sliceVis, crossSliceVis})));
    }



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
