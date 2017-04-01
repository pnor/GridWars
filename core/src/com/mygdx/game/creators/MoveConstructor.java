package com.mygdx.game.creators;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameEvent;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.*;
import com.mygdx.game.move_related.*;
import com.mygdx.game.screens_ui.BattleScreen;

import static com.mygdx.game.ComponentMappers.*;
import static com.mygdx.game.GridWars.atlas;

/**
 * Class that creates various moves to be used by Entities on the board. Also contains methods for certain
 * animations (like for dying and taking damage)
 * @author Phillip O'Reggio
 */
public class MoveConstructor {

    /**
     * Creates a generic damage animation
     * @param user Entity that is being damaged
     * @param engine {@code Engine}
     * @param stage {@code Stage}
     * @param screen {@code BattleScreen}
     * @return damage animation {@code Visuals}
     */
    public static Visuals damageAnimation(Entity user, Engine engine, Stage stage, BattleScreen screen) {
        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                am.get(user).actor.shade(new Color(.9f, .1f, .1f, 1));
            }
        }, .001f, 1);

        VisualEvent returnToWhiteFirst = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(Color.WHITE, .1f));
            }
        }, .15f, 1);

        VisualEvent returnToWhite = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(Color.WHITE, .1f));
            }
        }, .05f, 9);

        return new Visuals(screen, user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed, returnToWhiteFirst, returnToWhite}), false);
    }

    /**
     * Creates a generic damage animation
     * @param user Entity that is being damaged
     * @param engine {@code Engine}
     * @param stage {@code Stage}
     * @param screen {@code BattleScreen}
     * @return damage animation {@code Visuals}
     */
    public static Visuals heavyDamageAnimation(Entity user, Engine engine, Stage stage, BattleScreen screen) {
        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                am.get(user).actor.shade(new Color(.8f, 0, 0, 1));
            }
        }, .001f, 1);

        VisualEvent moveRight = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                am.get(user).actor.moveBy(3, 0);
            }
        }, .05f, 2);

        VisualEvent moveLeft = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                am.get(user).actor.moveBy(-3, 0);
            }
        }, .05f, 2);

        VisualEvent returnToWhite = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                am.get(user).actor.shade(am.get(user).actor.getColor().lerp(Color.WHITE, .1f));
            }
        }, .02f, 10);

        return new Visuals(screen, user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed,
                        moveRight.copy(.001f, 1), moveLeft, moveRight, moveLeft.copy(), moveRight.copy(1),
                        returnToWhite
                }), false);
    }

    /**
     * Creates the generic death animation
     * @param user Entity that is being killed
     * @param engine {@code Engine}
     * @param stage {@code Stage}
     * @return death animation {@code Visuals}
     */
    public static Visuals deathAnimation(Entity user, Engine engine, Stage stage, BattleScreen screen) {
        VisualEvent initialRed = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                am.get(user).actor.shade(Color.RED);
            }
        }, .001f, 1);

        VisualEvent fadeAndBlacken = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                am.get(user).actor.shade(
                        new Color(am.get(user).actor.getColor().r - .01f, am.get(user).actor.getColor().g - .1f,
                                am.get(user).actor.getColor().b - .1f, am.get(user).actor.getColor().a - .1f));
            }
        }, .1f, 9);

        return new Visuals(screen, user, null,
                new Array<VisualEvent>(new VisualEvent[]{initialRed, fadeAndBlacken.copy(.275f, 1), fadeAndBlacken}), false);
    }

    public static Move Tackle(Entity user, Engine engine, Stage stage, BattleScreen screen) {
        VisualEvent TackleVis = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boardManager.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - 22.5f, t.getHeight() / 2 - 22.5f));
                Entity star = new Entity();
                star.add(new PositionComponent(tilePosition.cpy().add((float) (Math.random() * 70) - 35, (float) (Math.random() * 70) - 35)
                        , 45 * boardManager.getBoard().getScale(), 45 * boardManager.getBoard().getScale(), (float) (Math.random() * 360)));
                star.add(new LifetimeComponent(0, .6f));
                star.add(new AnimationComponent(.3f, new TextureRegion[]{atlas.findRegion("Star1"),
                        atlas.findRegion("Star2")}, Animation.PlayMode.LOOP));
                engine.addEntity(star);
            }
        }, .2f, 4);

        return new Move("Tackle", user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage, BoardComponent.boards,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp, BoardManager boards) {
                        Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk - stm.get(enemy).def, 0, 999);
                        if (vm.has(enemy) && vm.get(enemy).damageAnimation != null)
                            vm.get(enemy).damageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(screen, user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}),
                new Array<VisualEvent>(new VisualEvent[]{TackleVis.copy(0.1f, 1), TackleVis}), true));
    }

    public static Move StarSpin(Entity user, Engine engine, Stage stage, BattleScreen screen) {
        VisualEvent spin = new VisualEvent(new VisualEffect() {
            @Override
            public void doVisuals(Entity user, Array<BoardPosition> targetPositions, Engine engine, Stage stage, BoardManager boardManager) {
                BoardPosition bp = targetPositions.get(0).add(bm.get(user).pos.r, bm.get(user).pos.c);
                Tile t;
                try {
                    t = boardManager.getBoard().getTile(bp.r, bp.c);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                Vector2 tilePosition = t.localToStageCoordinates(new Vector2(t.getWidth() / 2 - 22.5f, t.getHeight() / 2 - 22.5f));
                Entity star = new Entity();
                star.add(new PositionComponent(new Vector2(tilePosition.cpy().x,
                        tilePosition.cpy().y),
                        45 * boardManager.getBoard().getScale(),
                        45 * boardManager.getBoard().getScale(),
                        0));
                star.add(new LifetimeComponent(0, 1.2f));
                star.add(new AnimationComponent(.3f, new TextureRegion[]{atlas.findRegion("Star1"),
                        atlas.findRegion("Star2")}, Animation.PlayMode.LOOP));
                star.add(new EventComponent(.1f, 0f, true, true, new GameEvent() {
                    @Override
                    public void event(Entity e, Engine engine) {
                        pm.get(e).rotation += 40;
                    }
                }));
                engine.addEntity(star);
            }
        }, 0f, 1);

        return new Move("Star Spin", user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}), engine, stage, BoardComponent.boards,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp, BoardManager boards) {
                        Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                        if (stm.has(enemy))
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                            stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk / 2 - stm.get(enemy).def, 0, 999);
                        if (vm.has(enemy) && vm.get(enemy).heavyDamageAnimation != null)
                            vm.get(enemy).heavyDamageAnimation.setPlaying(true, true);
                    }
                }, new Visuals(screen, user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, -1)}),
                new Array<VisualEvent>(new VisualEvent[]{spin}), true));
    }
}
