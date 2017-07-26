package com.mygdx.game.creators;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.boards.Board;
import com.mygdx.game.boards.BoardManager;
import com.mygdx.game.boards.BoardPosition;
import com.mygdx.game.boards.CodeBoard;
import com.mygdx.game.rules_types.Battle2PRules;
import com.mygdx.game.rules_types.Rules;
import com.mygdx.game.rules_types.Team;
import com.mygdx.game.rules_types.ZoneRules;
import com.mygdx.game.screens_ui.screens.BattleScreen;

/**
 * Class containing static methods for initializing specific {@code Board}s and {@code Rule}s. Methods change the {@code BoardManager}
 * parameter to set the boards and they return the {@code Rules} object for the board. Also, they place entities at their correct starting
 * place on the board.
 *
 * @author Phillip O'Reggio
 */
public class BoardAndRuleConstructor {

    /**
     * Creates the Board and Rules based on the board index. Indexes are arranged in groups of 3, with the 1st one being a standard
     * 2-player match, the second being a 2-player zone match, and the third being a 4-player zone match.
     * @param boardIndex Index determining what board is created and what rules are returned. <p>
     *                   1-3 : Simple <p>
     *                   4-6 : Complex <p>
     *                   7-9 : <p>
     * @return {@code Rules} that should be used in the BattleScreen.
     */
    public static Rules getBoardAndRules(int boardIndex, BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        switch (boardIndex) {
            case 1 :
                return makeSimple2P(screen, teams, boardManager);
            case 2 :
                return makeSimple2PZone(screen, teams, boardManager);
            case 3 :
                return makeComplex2P(screen, teams, boardManager);
            case 4 :
                return makeComplex2PZone(screen, teams, boardManager);
            case 5 : //fix
                return makeCompact2P(screen, teams, boardManager);
            case 6 :
                return makeCompact2PZone(screen, teams, boardManager);
            case 7:
                return makeDesert2P(screen, teams, boardManager);
            case 8:
                return makeDesert2PZone(screen, teams, boardManager);
            case 9:
                return makeForest2P(screen, teams, boardManager);
            case 10:
                return makeForest2PZone(screen, teams, boardManager);
        }
        return null;
    }

    //for reference when making boards that are greater than 7 by 7
    /*
    LESS THAN 7
       new Board(boardSize, boardSize, c, c2, 100
    GREATER
       new Board(boardSize, boardSize, c, c2, 700 / boardSize
   */

    //region Simple
    public static Rules makeSimple2P(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSimple2PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(7, 7, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(maxSize, 5),
                        new BoardPosition(maxSize, 4),
                        new BoardPosition(maxSize, 3),
                        new BoardPosition(maxSize, 2)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(0, 1),
                        new BoardPosition(0, 2),
                        new BoardPosition(0, 3),
                        new BoardPosition(0, 4)})
        }));

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        //color zones
        rules.colorZones();

        return rules;
    }
    //endregion

    //region Complex
    public static Rules makeComplex2P(BattleScreen screen, Array<Team> teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }
        //place blocks randomly
        boardManager.add(EntityConstructor.cube(), new BoardPosition(1, 0));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(1, 2));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(1, 4));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(1, 6));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(3, 0));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(3, 2));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(3, 4));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(3, 6));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(5, 0));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(5, 2));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(5, 4));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(5, 6));

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeComplex2PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(7, 7, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(maxSize, 5),
                        new BoardPosition(maxSize, 4),
                        new BoardPosition(maxSize, 3),
                        new BoardPosition(maxSize, 2)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(0, 1),
                        new BoardPosition(0, 2),
                        new BoardPosition(0, 3),
                        new BoardPosition(0, 4)})
        }));

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        //place blocks
        boardManager.add(EntityConstructor.cube(), new BoardPosition(1, 0));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(2, 0));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(3, 0));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(3, 1));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(4, 0));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(4, 1));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(4, 2));

        boardManager.add(EntityConstructor.cube(), new BoardPosition(5, 6));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(4, 6));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(3, 6));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(3, 5));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(2, 6));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(2, 5));
        boardManager.add(EntityConstructor.cube(), new BoardPosition(2, 4));

        //color zones
        rules.colorZones();

        return rules;
    }
    //endregion

    //region Compact
    public static Rules makeCompact2P(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(5, 5, Color.DARK_GRAY, Color.GRAY, 100), new CodeBoard(5, 5));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 0;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 3;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeCompact2PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(6, 6, Color.DARK_GRAY, Color.GRAY, 100), new CodeBoard(6, 6));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(3, 3)}),
                new Array<BoardPosition>(new BoardPosition[]{new BoardPosition(2, 2)})
                }));

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        //place blocks
        boardManager.add(EntityConstructor.durableCube(), new BoardPosition(1, 2));
        boardManager.add(EntityConstructor.durableCube(), new BoardPosition(2, 1));
        boardManager.add(EntityConstructor.durableCube(), new BoardPosition(2, 3));
        boardManager.add(EntityConstructor.durableCube(), new BoardPosition(3, 2));
        boardManager.add(EntityConstructor.durableCube(), new BoardPosition(3, 4));
        boardManager.add(EntityConstructor.durableCube(), new BoardPosition(4, 3));

        //color zones
        rules.colorZones();

        return rules;
    }
    //endregion

    //region desert
    public static Rules makeDesert2P(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(9, 9, new Color(221f / 255, 221f / 255f, 119f / 255f, 1), new Color(1, 1, 102f / 255f, 1), 700 / 9), new CodeBoard(9, 9));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 0;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 8;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }
        //place blocks randomly
        for (int i = 0; i < 6; i++) {
            BoardPosition pos = new BoardPosition(MathUtils.random(0, 8), MathUtils.random(0, 8));
            if (boardManager.getBoard().getTile(pos.r, pos.c).isOccupied()) {
                i--;
                continue;
            }
            if (MathUtils.randomBoolean(.7f))
                boardManager.add(EntityConstructor.cactus(), pos);
            else
                boardManager.add(EntityConstructor.flowerCactus(), pos);

        }
        return new Battle2PRules(screen, teams);
    }

    public static Rules makeDesert2PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(8, 8, new Color(221f / 255, 221f / 255f, 119f / 255f, 1), new Color(1, 1, 102f / 255f, 1), 700 / 8), new CodeBoard(8, 8));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(6, 2)}),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(1, 5)})
        }));

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 6;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        //place blocks around zones
        boardManager.add(EntityConstructor.cactus(), new BoardPosition(0, 5));
        boardManager.add(EntityConstructor.cactus(), new BoardPosition(1, 4));
        boardManager.add(EntityConstructor.cactus(), new BoardPosition(1, 6));
        boardManager.add(EntityConstructor.cactus(), new BoardPosition(2, 5));

        boardManager.add(EntityConstructor.cactus(), new BoardPosition(5, 2));
        boardManager.add(EntityConstructor.cactus(), new BoardPosition(6, 1));
        boardManager.add(EntityConstructor.cactus(), new BoardPosition(6, 3));
        boardManager.add(EntityConstructor.cactus(), new BoardPosition(7, 2));

        //place blocks randomly
        for (int i = 0; i < MathUtils.random(0, 3); i++) {
            BoardPosition pos = new BoardPosition(MathUtils.random(1, 6), MathUtils.random(1, 6));
            if (boardManager.getBoard().getTile(pos.r, pos.c).isOccupied() || pos.equals(new BoardPosition(1, 5)) || pos.equals(new BoardPosition(6, 2))) {
                i--;
                continue;
            }
            if (MathUtils.randomBoolean(.7f))
                boardManager.add(EntityConstructor.cactus(), pos);
            else
                boardManager.add(EntityConstructor.flowerCactus(), pos);
        }

        //color zones
        rules.colorZones();

        return rules;
    }
    //endregion

    //region forest
    public static Rules makeForest2P(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(6, 6, new Color(36f / 255f, 106f / 255f, 0, 1), new Color(.2f, .2f, 20f / 255f, 1), 700 / 7), new CodeBoard(6, 6));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 4;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }
        //place blocks randomly
        for (int i = 0; i < 9; i++) {
            BoardPosition pos = new BoardPosition(MathUtils.random(0, 5), MathUtils.random(0, 5));
            if (boardManager.getBoard().getTile(pos.r, pos.c).isOccupied()) {
                i--;
                continue;
            }
            boardManager.add(EntityConstructor.tree(), pos);
        }
        return new Battle2PRules(screen, teams);
    }

    public static Rules makeForest2PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(6, 6, new Color(36f / 255f, 106f / 255f, 0, 1), new Color(.2f, .2f, 20f / 255f, 1), 700 / 7), new CodeBoard(6, 6));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(5, 1),
                        new BoardPosition(5, 2),
                        new BoardPosition(5, 3),
                        new BoardPosition(5, 4)
                }),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(0, 1),
                        new BoardPosition(0, 2),
                        new BoardPosition(0, 3),
                        new BoardPosition(0, 4)
                })
        }));

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = 4;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        //place blocks around zones
        for (int i = 0; i < 10; i++) {
            BoardPosition pos = new BoardPosition(MathUtils.random(0, 5), MathUtils.random(0, 5));
            if (boardManager.getBoard().getTile(pos.r, pos.c).isOccupied()) {
                i--;
                continue;
            }
            boardManager.add(EntityConstructor.tree(), pos);
        }

        //color zones
        rules.colorZones();

        return rules;
    }
    //endregion
}
