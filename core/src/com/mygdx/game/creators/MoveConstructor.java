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
     * Creates a shuffling animation for status effects
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
        return new StatusEffect("Poison", 3, new LerpColor(Color.GREEN, new Color(201f / 255f, 1f, 0f, 1f)),
                (e) -> {
            stm.get(e).hp -= 1;
            if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                vm.get(e).heavyDamageAnimation.setPlaying(true, true);
        }, (entity) -> entity.hp -= 1);
    }

    public static StatusEffect burn() {
        StatusEffect effect = new StatusEffect("Burn", 3, new LerpColor(Color.RED, new Color(1, 125f / 255f, 0f, 1f), .3f, Interpolation.sineOut),
                (e) -> {
            if (stm.has(e) && MathUtils.randomBoolean()) {
                stm.get(e).hp -= 1;
                if (vm.has(e) && !vm.get(e).heavyDamageAnimation.getIsPlaying())
                    vm.get(e).heavyDamageAnimation.setPlaying(true, true);
            }
        }, (entity) -> {
            if (MathUtils.randomBoolean())
                entity.hp -= 1;
        });

        effect.setStatChanges(1, 1, 1, .5f, 1, 1);
        return effect;
    }

    public static StatusEffect paralyze() {
        StatusEffect effect = new StatusEffect("Paralyze", 3, new LerpColor(Color.GRAY, Color.YELLOW, .4f, Interpolation.exp5In), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, 1, .5f);
        return effect;
    }

    public static StatusEffect freeze() {
        StatusEffect effect = new StatusEffect("Freeze", 2, new LerpColor(new Color(.8f, .4f, 1, 1), Color.CYAN, .5f), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, .5f, 0);
        effect.setStopsAnimation(true);
        return effect;
    }

    public static StatusEffect shivers() {
        StatusEffect effect = new StatusEffect("Shivers", 2, new LerpColor(new Color(.8f, .8f, 1, 1), Color.WHITE), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, .5f, 1, 1, 1);
        return effect;
    }

    public static StatusEffect petrify() {
        StatusEffect effect = new StatusEffect("Petrify", 2, new Color(214f / 255f, 82f / 255f, 0, 1), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 2, 1, 0);
        effect.setStopsAnimation(true);
        return effect;
    }

    public static StatusEffect stillness() {
        StatusEffect effect = new StatusEffect("Stillness", 2, new LerpColor(Color.WHITE, new Color(0, 140f / 255f, 1f, 1f), .7f,  Interpolation.sine), (e) -> {/*nothing*/}, null);
        effect.setStopsAnimation(true);
        return effect;
    }

    public static StatusEffect curse() {
        StatusEffect effect = new StatusEffect("Curse", 4, new LerpColor(Color.GRAY, Color.BLACK, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, .5f, .5f, .5f);
        return effect;
    }

    public static StatusEffect defenseless() {
        StatusEffect effect =  new StatusEffect("Defenseless", 1, new LerpColor(Color.WHITE, Color.NAVY, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, 0, 1);
        return effect;
    }

    public static StatusEffect offenseless() {
        StatusEffect effect = new StatusEffect("Offenseless", 2, new LerpColor(Color.WHITE, Color.RED, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, .5f, 1, 1);
        return effect;
    }

    public static StatusEffect speedUp() {
        StatusEffect effect = new StatusEffect("Quick", 1, new LerpColor(Color.WHITE, Color.CYAN, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, 1, 2);
        return effect;
    }

    public static StatusEffect attackUp() {
        StatusEffect effect = new StatusEffect("Power", 1, new LerpColor(Color.WHITE, Color.ORANGE, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1.5f, 1, 1);
        return effect;
    }

    public static StatusEffect guardUp() {
        StatusEffect effect = new StatusEffect("Guard", 1, new LerpColor(Color.WHITE, Color.BLUE, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(1, 1, 1, 1, 2, 1);
        return effect;
    }

    public static StatusEffect healthUp() {
        StatusEffect effect = new StatusEffect("Durability", 3, new LerpColor(Color.WHITE, Color.GREEN, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(2, 1, 1, 1, 2, 1);
        return effect;
    }

    public static StatusEffect charged() {
        StatusEffect effect = new StatusEffect("Charged", 3, new LerpColor(Color.ORANGE, Color.YELLOW, .5f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(2, 1, 2, 2, 1, 2);
        return effect;
    }

    public static StatusEffect supercharged() {
        StatusEffect effect = new StatusEffect("Supercharged", 3, new LerpColor(Color.ORANGE, Color.CYAN, .3f, Interpolation.fade), (e) -> {/*nothing*/}, null);
        effect.setStatChanges(3, 1, 2, 5, 1, 3);
        return effect;
    }

    public static StatusEffect regeneration() {
        StatusEffect effect = new StatusEffect("Regeneration", 3, new LerpColor(Color.PINK, new Color(.8f, 1, .8f, 1f), .5f, Interpolation.sineOut), (e) -> {
            if (stm.has(e)) {
                stm.get(e).hp = MathUtils.clamp(stm.get(e).hp + 1, 0, stm.get(e).getModMaxHp(e));
            }
            if (vm.has(e) && !vm.get(e).shuffleAnimation.getIsPlaying())
                vm.get(e).shuffleAnimation.setPlaying(true, true);
        }, (entity -> {
            entity.hp += 1;
        }));
        effect.setStatChanges(1, 1, 1, 1, 1, 1);
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
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 2 - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{curveSliceVis, crossSliceVis.copy(), sliceVis, crossSliceVis.copy(), curveSliceVis.copy(), sliceVis.copy()})),
                new MoveInfo(false, 2));
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
                new Array<VisualEvent>(new VisualEvent[]{laser})), new MoveInfo(false, 1));
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
                beamSprite.setColor(Color.ORANGE);
                beam.add(new SpriteComponent(beamSprite));
                beam.add(new PositionComponent(startTilePosition, entitySize.x, entitySize.y, direction - 90 + offset));
                beam.add(new MovementComponent(new Vector2(1300 * scale, 0).setAngle(direction + offset)));
                beam.add(new LifetimeComponent(0, .26f));
                beam.add(new EventComponent(0.02f, true, EventCompUtil.fadeOutAfter(7, 6)));
                engine.addEntity(beam);
            }
        }, .03f, 28);

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
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{laser})), new MoveInfo(false, 1));
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
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boards.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 entitySize = new Vector2(90 * scale, 90 * scale);
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
        }, .08f, 9);

        return new Move("Twister", user, 4, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);

                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 2 - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{shuriken, sparkle})), new MoveInfo(false, 2));
    }

    public static Move freeze(Entity user) {
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

        return new Move("Freeze", user, 5, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
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

        return new Move("Submerge", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp = MathUtils.clamp(stm.get(enemy).hp - 3, 0, stm.get(enemy).getModMaxHp(enemy));
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-2, 0)}),
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
        }, .02f, 7);

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
                    new BoardPosition(-4, 1),  new BoardPosition(1, -4),  new BoardPosition(3, -1), new BoardPosition(1, 3)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e) * 1.5f - stm.get(enemy).getModDef(enemy), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                    new BoardPosition(-2, 0), new BoardPosition(-1, -1),  new BoardPosition(2, 0),  new BoardPosition(1, 1),
                    new BoardPosition(-2, 1),  new BoardPosition(0, -3),  new BoardPosition(2, -1), new BoardPosition(0, 3),
                    new BoardPosition(-4, 1),  new BoardPosition(1, -4),  new BoardPosition(3, -1), new BoardPosition(1, 3)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        preSparkles.copy(), preBooms.copy(), preSparkles.copy(), preBooms.copy(),
                        comets.copy(), ripple.copy(), comets.copy(), ripple.copy(), comets.copy(), ripple, comets, preBooms.copy(), preBooms})), new MoveInfo(false, 1.5f));
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

        return new Move("Basilisk Strike", "The target was petrified!", user, 2, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

    public static Move curse(Entity user) {
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

        return new Move("Curse", nm.get(user).name + " placed a curse.", user, 3, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).getModAtk(e), 0, 999);

                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(-1, 0), new BoardPosition(-1, -1),  new BoardPosition(-1, 1),
                new BoardPosition(0, 1),  new BoardPosition(0, -1),
                new BoardPosition(1, 0),  new BoardPosition(1, -1),  new BoardPosition(1, 1)}),
                new Array<VisualEvent>(new VisualEvent[]{
                        preSparkles.copy(), preBooms.copy(), preSparkles.copy(), preBooms.copy(),
                        comets.copy(), ripple.copy(), comets.copy(), ripple.copy(), comets.copy(), ripple, comets, preBooms.copy(), preBooms})), new MoveInfo(true, 1));
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
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
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

        return new Move("Fresh Breath", nm.get(user).name + " breathed refreshing air!", user, 2,
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

        return new Move("Spa Breath", nm.get(user).name + " breathed a soothing air!", user, 4,
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

        return new Move("Reflect Move", user, 0, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp) {
                        Entity enemy = BoardComponent.boards.getCodeBoard().get(bp.r, bp.c);
                        if (mvm.has(enemy)) {
                            mvm.get(e).moveList.set(1, mvm.get(enemy).moveList.get(0));
                        }
                    }
                }, new Visuals(user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{mirror, largeSparkle})), null);
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
                    animm.get(entity).shadeColor = color;
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
                    animm.get(entity).shadeColor = color;
                }
            };
        }

        /**
         * @return {@link GameEvent} that will become more transparent after a set amount of time
         * @param timesUntilFade Amount of times this method will be called until it begins fading
         * @param amount of times this needs to be called until it is fully transparent. (note: will not work as well
         *               colors that have a transparency of <1
         */
        public static GameEvent fadeOutAfter(int timesUntilFade, int amount) {
            return new GameEvent() {
                private int timesCalled;
                @Override
                public void event(Entity e, Engine engine) {
                    timesCalled++;
                    if (timesCalled >= timesUntilFade) {
                        if (sm.has(e)) { //sprite
                            Sprite sprite = sm.get(e).sprite;
                            sprite.setColor(
                                    sprite.getColor().r,
                                    sprite.getColor().g,
                                    sprite.getColor().b,
                                    MathUtils.clamp(sprite.getColor().a - 1f / amount, 0, 1));
                        } else { //animation
                            Color color = animm.get(e).shadeColor;
                            color = new Color(
                                    color.r,
                                    color.g,
                                    color.b,
                                    MathUtils.clamp(color.a - 1f / amount, 0, 1));
                            animm.get(e).shadeColor = color;

                        }
                    }
                }
            };
        }

        public static GameEvent fadeInThenOut(int amountIn, int space, int amountOut) {
            return new GameEvent() {
                private int timesRun;
                @Override
                public void event(Entity e, Engine engine) {
                    timesRun++;

                    if (timesRun < amountIn) {
                        if (sm.has(e)) { //sprite
                            Sprite sprite = sm.get(e).sprite;
                            sprite.setColor(
                                    sprite.getColor().r,
                                    sprite.getColor().g,
                                    sprite.getColor().b,
                                    MathUtils.clamp(sprite.getColor().a + 1f / amountIn, 0, 1));
                        } else { //animation
                            Color color = animm.get(e).shadeColor;
                            color = new Color(
                                    color.r,
                                    color.g,
                                    color.b,
                                    MathUtils.clamp(color.a + 1f / amountIn, 0, 1));
                            animm.get(e).shadeColor = color;
                        }
                    } else if (timesRun >= amountIn && timesRun < space) {
                        //nothing
                    } else {
                        if (sm.has(e)) { //sprite
                            Sprite sprite = sm.get(e).sprite;
                            sprite.setColor(
                                    sprite.getColor().r,
                                    sprite.getColor().g,
                                    sprite.getColor().b,
                                    MathUtils.clamp(sprite.getColor().a - 1f / amountOut, 0, 1));
                        } else { //animation
                            Color color = animm.get(e).shadeColor;
                            color = new Color(
                                    color.r,
                                    color.g,
                                    color.b,
                                    MathUtils.clamp(color.a - 1f / amountOut, 0, 1));
                            animm.get(e).shadeColor = color;
                        }
                    }
                }
            };
        }


        /**
         * @param amount degrees rotated per method call
         * @return {@link GameEvent} that rotates the entity
         */
        public static GameEvent rotate(float amount) {
            return (entity, engine) -> {
                pm.get(entity).rotation += amount;
            };
        }



    }
}
