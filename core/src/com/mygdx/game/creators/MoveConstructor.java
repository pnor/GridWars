package com.mygdx.game.creators;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameTimer;
import com.mygdx.game.actors.Tile;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.components.AnimationComponent;
import com.mygdx.game.components.BoardComponent;
import com.mygdx.game.components.LifetimeComponent;
import com.mygdx.game.components.PositionComponent;
import com.mygdx.game.move_related.Attack;
import com.mygdx.game.move_related.Move;
import com.mygdx.game.move_related.VisualEffect;
import com.mygdx.game.move_related.Visuals;
import com.mygdx.game.screens_ui.BattleScreen;

import static com.mygdx.game.ComponentMappers.bm;
import static com.mygdx.game.ComponentMappers.stm;
import static com.mygdx.game.GridWars.atlas;

/**
 * Class that creates various moves to be used by Entities on the board.
 * @author Phillip O'Reggio
 */
public class MoveConstructor {

    public static Move Tackle(Entity user, Engine engine, Stage stage, BattleScreen screen) {
        VisualEffect TackleVis = new VisualEffect() {
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
        };

        Move move = new Move("Tackle", user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(-1, 0)}), engine, stage, BoardComponent.boards,
                new Attack() {
                    @Override
                    public void effect(Entity e, BoardPosition bp, BoardManager boards) {
                        Entity enemy = boards.getCodeBoard().get(bp.r, bp.c);
                        stm.get(enemy).hp -= MathUtils.clamp(stm.get(e).atk - stm.get(enemy).def, 0, 999);
                    }
                }, new Visuals(screen, user, new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(0, 1)}), new GameTimer(1f),
                new Array<VisualEffect>(new VisualEffect[]{TackleVis, TackleVis, TackleVis, TackleVis, TackleVis}),
                new Array<Float>(new Float[]{new Float(0.2f), new Float(0.2f), new Float(0.2f), new Float(0.2f), new Float(0.2f)})));

        return move;
    }
}
