package com.mygdx.game.creators;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
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
import com.mygdx.game.screens.BattleScreen;
import com.mygdx.game.ui.LerpColorManager;

import static com.mygdx.game.GridWars.atlas;

/**
 * Class containing static methods for initializing specific {@code Board}s and {@code Rule}s. Methods change the {@code BoardManager}
 * parameter to set the boards and they return the {@code Rules} object for the board. Also, they place entities at their correct starting
 * place on the board.
 *
 * @author Phillip O'Reggio
 */
public class BoardAndRuleConstructor {
    private static boolean ready;
    private static LerpColorManager lerpColorManager;

    /**
     * Readies the {@link MoveConstructor} for use.
     * @param colorManager LerpColor Manager of the {@link BattleScreen}
     **/
    public static void initialize(LerpColorManager colorManager) {
        lerpColorManager = colorManager;
        ready = true;
    }

    /**
     * Clears static fields in {@link MoveConstructor}
     */
    public static void clear() {
       lerpColorManager = null;
       ready = false;
    }

    public static boolean isReady() {
        return ready;
    }

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
            //region regular battle
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
            case 11:
                return makeIsland2P(screen, teams, boardManager);
            case 12:
                return makeIsland2PZone(screen, teams, boardManager);
            //endregion
            //region survival
            //1-10
            case 13: // 1
                return makeSNEntrance(screen, teams, boardManager);
                //return makeSZPaths(screen, teams, boardManager);
            case 14:
                return makeSNBasic(screen, teams, boardManager);
            case 15:
                return makeSNHallwayOpen(screen, teams, boardManager);
            case 16:
                return makeSNBasic(screen, teams, boardManager);
            case 17: // 5
                return makeSNTorchRoom(screen, teams, boardManager);
            case 18:
                return makeSNHallway(screen, teams, boardManager);
            case 19:
                return makeSNCircle(screen, teams, boardManager);
            case 20:
                return makeSNHallwayCurved(screen, teams, boardManager);
            case 21:
                return makeSNSquaresConnect(screen, teams, boardManager);
            case 22: // 10
                return makeSNBlazePneumaArena(screen, teams, boardManager);
            //11-19
            case 23:
                return makeSZPaths(screen, teams, boardManager);
            case 24:
                return makeSZSquaresConnect2(screen, teams, boardManager);
            case 25:
                return makeSNSmall(screen, teams, boardManager);
            case 26:
                return makeSNBasic2(screen, teams, boardManager);
            case 27: // 15
                return makeSNSmall(screen, teams, boardManager);
            case 28:
                return makeSZRoundAbout(screen, teams, boardManager);
            case 29:
                return makeSNTowers(screen, teams, boardManager);
            case 30:
                return makeSZXPath(screen, teams, boardManager);
            case 31:
                return makeSNBasic2FocusOnFirst(screen, teams, boardManager);
            case 32: // 20
                return makeSNAquaPneumaArena(screen, teams, boardManager);
            case 33:
                return makeSNHoles(screen, teams, boardManager);
            case 34:
                return makeSNHolesLarge(screen, teams, boardManager);
            case 35:
                return makeSNHolesLargeAltTowerPlacement(screen, teams, boardManager);
            case 36:
                return makeSNFatCross(screen, teams, boardManager);
            case 37: //25
                return makeSNCopmlexPaths(screen, teams, boardManager);
            case 38:
                return makeSNDiagDownSlantPillars(screen, teams, boardManager);
            case 39:
                return makeSNDiagUpSlantHoles(screen, teams, boardManager);
            case 40:
                return makeSNEmptyRowMiddle(screen, teams, boardManager);
            case 41:
                return makeSNFatCross(screen, teams, boardManager);
            case 42: //30
                return makeSNElectroPneumaArena(screen, teams, boardManager);
            case 43:
                return makeSNBasic3(screen, teams, boardManager);
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            //endregion
        }
        return null;
    }

    /*
    For reference when making boards that are greater than 7 by 7
    LESS THAN 7
       new Board(boardSize, boardSize, c, c2, 100
    GREATER
       new Board(boardSize, boardSize, c, c2, 700 / boardSize
   */

    //region regular battle stages

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
        rules.colorZones(lerpColorManager);

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
        rules.colorZones(lerpColorManager);

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
        rules.colorZones(lerpColorManager);

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
        rules.colorZones(lerpColorManager);

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
        rules.colorZones(lerpColorManager);

        return rules;
    }
    //endregion

    //region island
    public static Rules makeIsland2P(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(229f / 255f, 238f / 255f, 220f / 255f, 1), new Color(220f / 255f, 238f / 255f, 239f / 255f, 1), 700 / 7), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int j = 0;
        while (j < teams.get(0).getEntities().size) {
            if (j == 0)
                boardManager.add(teams.get(0).getEntities().get(0), new BoardPosition(1, 1));
            else if (j == 1)
                boardManager.add(teams.get(0).getEntities().get(1), new BoardPosition(0, 2));
            else if (j == 2)
                boardManager.add(teams.get(0).getEntities().get(2), new BoardPosition(0, 4));
            else if (j == 3)
                boardManager.add(teams.get(0).getEntities().get(3), new BoardPosition(1, 5));
            j++;
        }
        
        j = 0;
        while (j < teams.get(1).getEntities().size) {
            if (j == 0)
                boardManager.add(teams.get(1).getEntities().get(0), new BoardPosition(5, 1));
            else if (j == 1)
                boardManager.add(teams.get(1).getEntities().get(1), new BoardPosition(6, 2));
            else if (j == 2)
                boardManager.add(teams.get(1).getEntities().get(2), new BoardPosition(6, 4));
            else if (j == 3)
                boardManager.add(teams.get(1).getEntities().get(3), new BoardPosition(5, 5));
            j++;
        }

        //remove tiles
        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(0, 1).setInvisible(true);
        boardManager.getBoard().getTile(1, 0).setInvisible(true);
        boardManager.getBoard().getTile(0, 5).setInvisible(true);
        boardManager.getBoard().getTile(0, 6).setInvisible(true);
        boardManager.getBoard().getTile(1, 6).setInvisible(true);

        boardManager.getBoard().getTile(6, 0).setInvisible(true);
        boardManager.getBoard().getTile(6, 1).setInvisible(true);
        boardManager.getBoard().getTile(5, 0).setInvisible(true);
        boardManager.getBoard().getTile(6, 5).setInvisible(true);
        boardManager.getBoard().getTile(6, 6).setInvisible(true);
        boardManager.getBoard().getTile(5, 6).setInvisible(true);

        //place blocks randomly
        for (int i = 0; i < 2; i++) {
            BoardPosition pos = new BoardPosition(MathUtils.random(0, 5), MathUtils.random(0, 5));
            if (boardManager.getBoard().getTile(pos.r, pos.c).isOccupied()) {
                i--;
                continue;
            }
            boardManager.add(EntityConstructor.tree(), pos);
        }
        return new Battle2PRules(screen, teams);
    }

    public static Rules makeIsland2PZone(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        //declare rules
        boardManager.setBoards(new Board(7, 7, new Color(234f / 255f, 155f / 255f, 35f / 255f, 1), new Color(220f / 255f, 238f / 255f, 239f / 255f, 1), 700 / 7), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        ZoneRules rules = new ZoneRules(screen, teams, new Array<Array<BoardPosition>>(new Array[] {
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(6, 3)
                }),
                new Array<BoardPosition>(new BoardPosition[]{
                        new BoardPosition(0, 3)
                })
        }));

        //place entities
        int j = 0;
        while (j < teams.get(0).getEntities().size) {
            if (j == 0)
                boardManager.add(teams.get(0).getEntities().get(0), new BoardPosition(1, 1));
            else if (j == 1)
                boardManager.add(teams.get(0).getEntities().get(1), new BoardPosition(0, 2));
            else if (j == 2)
                boardManager.add(teams.get(0).getEntities().get(2), new BoardPosition(0, 4));
            else if (j == 3)
                boardManager.add(teams.get(0).getEntities().get(3), new BoardPosition(1, 5));
            j++;
        }

        j = 0;
        while (j < teams.get(1).getEntities().size) {
            if (j == 0)
                boardManager.add(teams.get(1).getEntities().get(0), new BoardPosition(5, 1));
            else if (j == 1)
                boardManager.add(teams.get(1).getEntities().get(1), new BoardPosition(6, 2));
            else if (j == 2)
                boardManager.add(teams.get(1).getEntities().get(2), new BoardPosition(6, 4));
            else if (j == 3)
                boardManager.add(teams.get(1).getEntities().get(3), new BoardPosition(5, 5));
            j++;
        }

        //remove tiles
        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(0, 1).setInvisible(true);
        boardManager.getBoard().getTile(1, 0).setInvisible(true);
        boardManager.getBoard().getTile(0, 5).setInvisible(true);
        boardManager.getBoard().getTile(0, 6).setInvisible(true);
        boardManager.getBoard().getTile(1, 6).setInvisible(true);

        boardManager.getBoard().getTile(6, 0).setInvisible(true);
        boardManager.getBoard().getTile(6, 1).setInvisible(true);
        boardManager.getBoard().getTile(5, 0).setInvisible(true);
        boardManager.getBoard().getTile(6, 5).setInvisible(true);
        boardManager.getBoard().getTile(6, 6).setInvisible(true);
        boardManager.getBoard().getTile(5, 6).setInvisible(true);

        boardManager.getBoard().getTile(3, 3).setInvisible(true);



        //place blocks randomly
        for (int i = 0; i < 2; i++) {
            BoardPosition pos = new BoardPosition(MathUtils.random(0, 5), MathUtils.random(0, 5));
            if (boardManager.getBoard().getTile(pos.r, pos.c).isOccupied()) {
                i--;
                continue;
            }
            boardManager.add(EntityConstructor.tree(), pos);
        }

        //color zones
        rules.colorZones(lerpColorManager);

        return rules;
    }
    //endregion
    //endregion

    //region survival
    //SN : Survival Normal Rules
    //SZ : Survival Zone Rules

    //region Floor 1-9
    public static Rules makeSNEntrance(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(6, 6, Color.DARK_GRAY, Color.LIGHT_GRAY, 100), new CodeBoard(6, 6));
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

        //remove corners
        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(1, 0).setInvisible(true);
        boardManager.getBoard().getTile(0, 5).setInvisible(true);
        boardManager.getBoard().getTile(1, 5).setInvisible(true);
        boardManager.getBoard().getTile(4, 0).setInvisible(true);
        boardManager.getBoard().getTile(5, 0).setInvisible(true);
        boardManager.getBoard().getTile(4, 5).setInvisible(true);
        boardManager.getBoard().getTile(5, 5).setInvisible(true);

        //add items
        boardManager.add(EntityConstructor.torch(), new BoardPosition(2, 0));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(3, 0));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(2, 5));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(3, 5));

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNBasic(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, Color.DARK_GRAY, Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 2)
                col ++;
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }

        //place things
        boardManager.add(EntityConstructor.torch(), new BoardPosition(2, 2));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(2, 4));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(4, 2));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(4, 4));

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNHallwayOpen(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, Color.DARK_GRAY, Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 2)
                col ++;
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }

        //add things
        for (int i = 0; i < boardManager.getBoard().getRowSize(); i++) {
            if (i < 2 || i > 4) {
                boardManager.getBoard().getTile(i, 0).setInvisible(true);
                boardManager.getBoard().getTile(i, boardManager.getBoard().getColumnSize() - 1).setInvisible(true);
            } else {
                boardManager.add(EntityConstructor.pillar(), new BoardPosition(i, 0));
                boardManager.add(EntityConstructor.pillar(), new BoardPosition(i, boardManager.getBoard().getColumnSize() - 1));
            }

        }
        boardManager.add(EntityConstructor.torch(), new BoardPosition(2, 1));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(2, 5));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(4, 1));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(4, 5));


        //remove corners
        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(1, 0).setInvisible(true);
        boardManager.getBoard().getTile(0, boardManager.getBoard().getColumnSize() - 1).setInvisible(true);
        boardManager.getBoard().getTile(1, boardManager.getBoard().getColumnSize() - 1).setInvisible(true);

        boardManager.getBoard().getTile(boardManager.getBoard().getRowSize() - 1, 0).setInvisible(true);
        boardManager.getBoard().getTile(boardManager.getBoard().getRowSize() - 2, 0).setInvisible(true);
        boardManager.getBoard().getTile(boardManager.getBoard().getRowSize() - 1, boardManager.getBoard().getColumnSize() - 1).setInvisible(true);
        boardManager.getBoard().getTile(boardManager.getBoard().getRowSize() - 2, boardManager.getBoard().getColumnSize() - 1).setInvisible(true);


        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNHallwayCurved(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, Color.DARK_GRAY, Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //remove edge
        final int START_POS = 3;
        final int CORNER_SIZE = 4 + START_POS;
        for (int i = START_POS; i < CORNER_SIZE; i++) {
            for (int j = START_POS; j < CORNER_SIZE; j++) {
                boardManager.getBoard().getTile(i, j).setInvisible(true);
            }
        }

        //place entities
        int tryRow = 0;
        int tryCol = maxSize;
        for (int i = 0; i < teams.get(0).getEntities().size; i++) {
            if (boardManager.getBoard().getTile(tryRow, tryCol).isInvisible()) {
                tryRow = 0;
                tryCol--;
            }
            boardManager.add(teams.get(0).getEntities().get(i), new BoardPosition(tryRow, tryCol));
            tryRow++;
        }
        tryRow = maxSize;
        tryCol = 0;
        for (int i = 0; i < teams.get(1).getEntities().size; i++) {
            if (boardManager.getBoard().getTile(tryRow, tryCol).isInvisible()) {
                tryCol = 0;
                tryRow--;
            }
            boardManager.add(teams.get(1).getEntities().get(i), new BoardPosition(tryRow, tryCol));
            tryCol++;
        }

        //place things
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(0, 0));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(0, 1));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(1, 0));


        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNHallway(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(6, 6, Color.DARK_GRAY, Color.LIGHT_GRAY, 100), new CodeBoard(6, 6));
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

        //remove sides
        for (int i = 0; i < boardManager.getBoard().getRowSize(); i++) {
            boardManager.getBoard().getTile(i, 0).setInvisible(true);

            boardManager.getBoard().getTile(i, 5).setInvisible(true);
        }

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNTorchRoom(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(5, 5, Color.DARK_GRAY, Color.LIGHT_GRAY, 100), new CodeBoard(5, 5));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        //place entities
        int col = 0;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 1)
                col ++;
            col++;
        }
        col = 4;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 3)
                col--;
            col--;
        }

        //add items

        boardManager.add(EntityConstructor.torch(), new BoardPosition(1, 0));
        boardManager.add(EntityConstructor.stoneTorch(), new BoardPosition(1, 2));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(1, 4));

        boardManager.add(EntityConstructor.stoneTorch(), new BoardPosition(2, 0));
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(2, 2));
        boardManager.add(EntityConstructor.stoneTorch(), new BoardPosition(2, 4));

        boardManager.add(EntityConstructor.torch(), new BoardPosition(3, 0));
        boardManager.add(EntityConstructor.stoneTorch(), new BoardPosition(3, 2));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(3, 4));


        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNCircle(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(6, 6, Color.DARK_GRAY, Color.LIGHT_GRAY, 100), new CodeBoard(6, 6));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        //place entities
        int count = 0;
        for (Entity e : teams.get(0).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(1, 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(0, 2));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, 3));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(1, 4));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize - 1, 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize, 2));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize, 3));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize - 1, 4));
                    break;
            }
            count++;
        }

        //remove corners
        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(1, 0).setInvisible(true);
        boardManager.getBoard().getTile(0, 1).setInvisible(true);

        boardManager.getBoard().getTile(0, maxSize).setInvisible(true);
        boardManager.getBoard().getTile(1, maxSize).setInvisible(true);
        boardManager.getBoard().getTile(0, maxSize - 1).setInvisible(true);

        boardManager.getBoard().getTile(maxSize, 0).setInvisible(true);
        boardManager.getBoard().getTile(maxSize - 1, 0).setInvisible(true);
        boardManager.getBoard().getTile(maxSize, 1).setInvisible(true);

        boardManager.getBoard().getTile(maxSize, maxSize).setInvisible(true);
        boardManager.getBoard().getTile(maxSize - 1, maxSize).setInvisible(true);
        boardManager.getBoard().getTile(maxSize, maxSize - 1).setInvisible(true);

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNSquaresConnect(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(5, 5, Color.DARK_GRAY, Color.LIGHT_GRAY, 100), new CodeBoard(5, 5));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        //place entities
        int count = 0;
        for (Entity e : teams.get(0).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(0, 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(1, 0));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(2, 0));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize - 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize - 1, maxSize));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize - 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize - 2, maxSize));
                    break;
            }
            count++;
        }

        //remove Corner
        boardManager.getBoard().getTile(0, 3).setInvisible(true);
        boardManager.getBoard().getTile(0, 4).setInvisible(true);
        boardManager.getBoard().getTile(1, 3).setInvisible(true);
        boardManager.getBoard().getTile(1, 4).setInvisible(true);

        boardManager.getBoard().getTile(3, 0).setInvisible(true);
        boardManager.getBoard().getTile(3, 1).setInvisible(true);
        boardManager.getBoard().getTile(4, 0).setInvisible(true);
        boardManager.getBoard().getTile(4, 1).setInvisible(true);

        //add items
        boardManager.add(EntityConstructor.spike(), new BoardPosition(1, 1));
        boardManager.add(EntityConstructor.spike(), new BoardPosition(maxSize - 1, maxSize - 1));

        return new Battle2PRules(screen, teams);
    }
    //endregion
    //region Floor 11 - 19
    public static Rules makeSZPaths(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(0, .3f, .8f, 1), Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 2)
                col ++;
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }

        //remove edge
        int colNum = 0;
        for (int i = 1; i < maxSize; i++)
            boardManager.getBoard().getTile(i, colNum).setInvisible(true);

        colNum = 2;
        for (int i = 2; i < maxSize - 1; i++)
            boardManager.getBoard().getTile(i, colNum).setInvisible(true);

        colNum = 4;
        for (int i = 2; i < maxSize - 1; i++)
            boardManager.getBoard().getTile(i, colNum).setInvisible(true);

        colNum = maxSize;
        for (int i = 1; i < maxSize; i++)
            boardManager.getBoard().getTile(i, colNum).setInvisible(true);

        //place things
        boardManager.add(EntityConstructor.torch(), new BoardPosition(1, 2));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(1, 4));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(5, 2));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(5, 4));

        //Create Zones
        Array<Array<BoardPosition>> zones = new Array<>(2);
        zones.add(new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(maxSize, 2),
                new BoardPosition(maxSize, 3),
                new BoardPosition(maxSize, 4)
        }));
        zones.add(new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(0, 2),
                new BoardPosition(0, 3),
                new BoardPosition(0, 4)
        }));

        ZoneRules rules = new ZoneRules(screen, teams, zones);
        rules.colorZones(lerpColorManager);
        return rules;
    }

    public static Rules makeSZSquaresConnect2(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(5, 5, new Color(0, .3f, .8f, 1), Color.LIGHT_GRAY, 100), new CodeBoard(5, 5));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int count = 0;
        for (Entity e : teams.get(0).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(0, 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(1, 0));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(2, 0));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize - 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize - 1, maxSize));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize - 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize - 2, maxSize));
                    break;
            }
            count++;
        }

        //remove middle edge
        boardManager.getBoard().getTile(1, 3).setInvisible(true);
        boardManager.getBoard().getTile(3, 1).setInvisible(true);

        //add items
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(1, 1));
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(maxSize - 1, maxSize - 1));

        Array<Array<BoardPosition>> zones = new Array<>(2);
        zones.add(new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(maxSize, maxSize)
        }));
        zones.add(new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(0, 0)
        }));

        ZoneRules rules = new ZoneRules(screen, teams, zones);
        rules.colorZones(lerpColorManager);
        return rules;
    }

    public static Rules makeSNSmall(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(5, 5, new Color(0, .3f, .8f, 1), Color.LIGHT_GRAY, 100), new CodeBoard(5, 5));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 0;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = maxSize;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        //place things
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(2, 2));

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNBasic2(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(0, .3f, .8f, 1), Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 2)
                col ++;
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }

        //place things
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(2, 2));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(2, 4));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(4, 2));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(4, 4));

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSZRoundAbout(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(9, 9, new Color(0, .3f, .8f, 1), Color.LIGHT_GRAY, 700 / 9), new CodeBoard(9, 9));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int count = 0;
        for (Entity e : teams.get(0).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(0, 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(1, 0));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(1, 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(2, 1));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize - 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize - 1, maxSize));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize - 2, maxSize - 1));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize - 1, maxSize - 2));
                    break;
            }
            count++;
        }

        //remove middle + corner
        boardManager.getBoard().getTile(2, 3).setInvisible(true);
        boardManager.getBoard().getTile(2, 4).setInvisible(true);
        boardManager.getBoard().getTile(2, 5).setInvisible(true);
        boardManager.getBoard().getTile(2, 6).setInvisible(true);

        boardManager.getBoard().getTile(3, 2).setInvisible(true);
        boardManager.getBoard().getTile(3, 3).setInvisible(true);
        boardManager.getBoard().getTile(3, 4).setInvisible(true);
        boardManager.getBoard().getTile(3, 5).setInvisible(true);
        boardManager.getBoard().getTile(3, 6).setInvisible(true);

        boardManager.getBoard().getTile(4, 2).setInvisible(true);
        boardManager.getBoard().getTile(4, 3).setInvisible(true);
        boardManager.getBoard().getTile(4, 5).setInvisible(true);
        boardManager.getBoard().getTile(4, 6).setInvisible(true);

        boardManager.getBoard().getTile(5, 2).setInvisible(true);
        boardManager.getBoard().getTile(5, 3).setInvisible(true);
        boardManager.getBoard().getTile(5, 4).setInvisible(true);
        boardManager.getBoard().getTile(5, 5).setInvisible(true);
        boardManager.getBoard().getTile(5, 6).setInvisible(true);

        boardManager.getBoard().getTile(6, 2).setInvisible(true);
        boardManager.getBoard().getTile(6, 3).setInvisible(true);
        boardManager.getBoard().getTile(6, 4).setInvisible(true);
        boardManager.getBoard().getTile(6, 5).setInvisible(true);

        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(maxSize, maxSize).setInvisible(true);

        //place entities
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(4, 4));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(0, maxSize));
        boardManager.add(EntityConstructor.brokenPillar(), new BoardPosition(maxSize, 0));

        Array<Array<BoardPosition>> zones = new Array<>(2);
        zones.add(new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(maxSize - 1, maxSize),
                new BoardPosition(maxSize, maxSize - 1)
        }));
        zones.add(new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(1, 0),
                new BoardPosition(0, 1)
        }));

        ZoneRules rules = new ZoneRules(screen, teams, zones);
        rules.colorZones(lerpColorManager);
        return rules;
    }

    public static Rules makeSNTowers(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(6, 6, new Color(0, .3f, .8f, 1), Color.LIGHT_GRAY, 100), new CodeBoard(6, 6));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }

        int count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize, 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize, 4));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize, 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize, 3));
                    break;
            }
            count++;
        }

        count = 0;
        for (Entity e : teams.get(2).getEntities()) { //towers
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(2, 0));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(3, maxSize));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(3, 0));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(2, maxSize));
                    break;
            }
            count++;
        }

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSZXPath(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(6, 6, new Color(0, .3f, .8f, 1), Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
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

        //remove edge
        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(1, 1).setInvisible(true);

        boardManager.getBoard().getTile(0, maxSize).setInvisible(true);
        boardManager.getBoard().getTile(1, maxSize - 1).setInvisible(true);

        boardManager.getBoard().getTile(maxSize, 0).setInvisible(true);
        boardManager.getBoard().getTile(maxSize - 1, 1).setInvisible(true);

        boardManager.getBoard().getTile(maxSize, maxSize).setInvisible(true);
        boardManager.getBoard().getTile(maxSize - 1, maxSize - 1).setInvisible(true);

        //place things
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(2, 1));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(3, 1));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(2, 4));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(3, 4));

        //Create Zones
        Array<Array<BoardPosition>> zones = new Array<>(2);
        zones.add(new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(maxSize, 1),
                new BoardPosition(maxSize, 2),
                new BoardPosition(maxSize, 3),
                new BoardPosition(maxSize, 4)
        }));
        zones.add(new Array<BoardPosition>(new BoardPosition[]{
                new BoardPosition(0, 1),
                new BoardPosition(0, 2),
                new BoardPosition(0, 3),
                new BoardPosition(0, 4)
        }));

        ZoneRules rules = new ZoneRules(screen, teams, zones);
        rules.colorZones(lerpColorManager);
        return rules;
    }

    public static Rules makeSNBasic2FocusOnFirst(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(0, .3f, .8f, 1), Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 2)
                col ++;
            col++;
        }
        int count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize, 3));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize, 2));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize, 4));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize - 1, 3));
                    break;
            }
            count++;
        }


        //place things
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(2, 2));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(2, 4));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(4, 2));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(4, 4));

        return new Battle2PRules(screen, teams);
    }
    //endregion
    //region Floor 21 - 29
    public static Rules makeSNHoles(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(5, 5, new Color(.7f, .7f, 0, 1), Color.LIGHT_GRAY, 100), new CodeBoard(5, 5));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int count = 0;
        for (Entity e : teams.get(0).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(1, 0));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(0, 1));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, maxSize - 1));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(1, maxSize));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize - 1, 0));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize, 1));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize - 1, maxSize));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize - 1));
                    break;
            }
            count++;
        }

        //remove spaces
        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(0, 2).setInvisible(true);
        boardManager.getBoard().getTile(0, maxSize).setInvisible(true);
        boardManager.getBoard().getTile(2, 0).setInvisible(true);
        boardManager.getBoard().getTile(2, 2).setInvisible(true);
        boardManager.getBoard().getTile(2, maxSize).setInvisible(true);
        boardManager.getBoard().getTile(maxSize, 0).setInvisible(true);
        boardManager.getBoard().getTile(maxSize, 2).setInvisible(true);
        boardManager.getBoard().getTile(maxSize, maxSize).setInvisible(true);

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNHolesLarge(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(.7f, .7f, 0, 1), Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int count = 0;
        for (Entity e : teams.get(0).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(1, 0));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(0, 1));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, maxSize - 1));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(1, maxSize));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize - 1, 0));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize, 1));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize - 1, maxSize));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize - 1));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(2).getEntities()) { //towers
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize / 2, 0));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize / 2, maxSize));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, maxSize / 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize / 2));
                    break;
            }
            count++;
        }

        //remove spaces
        for (int i = 0; i <= maxSize; i += 2) {
            for (int j = 0; j <= maxSize; j += 2) {
                boardManager.getBoard().getTile(i, j).setInvisible(true);
            }
        }

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNHolesLargeAltTowerPlacement(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(.7f, .7f, 0, 1), Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int count = 0;
        for (Entity e : teams.get(0).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(1, 0));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(0, 1));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, maxSize - 1));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(1, maxSize));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize - 1, 0));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize, 1));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize - 1, maxSize));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize - 1));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(2).getEntities()) { //towers
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize / 2, maxSize / 2));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize / 2, maxSize));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, maxSize / 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize / 2));
                    break;
            }
            count++;
        }

        //remove spaces
        for (int i = 0; i <= maxSize; i += 2) {
            for (int j = 0; j <= maxSize; j += 2) {
                boardManager.getBoard().getTile(i, j).setInvisible(true);
            }
        }

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNFatCross(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(6, 6, new Color(.7f, .7f, 0, 1), Color.LIGHT_GRAY, 100), new CodeBoard(6, 6));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            col++;
        }
        col = maxSize - 1;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            col--;
        }

        //remove spaces
        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(0, maxSize).setInvisible(true);
        boardManager.getBoard().getTile(maxSize, 0).setInvisible(true);
        boardManager.getBoard().getTile(maxSize, maxSize).setInvisible(true);

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNCopmlexPaths(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(9, 9, new Color(.7f, .7f, 0, 1), Color.LIGHT_GRAY, 700 / 9), new CodeBoard(9, 9));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int count = 0;
        for (Entity e : teams.get(0).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(0, 0));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(0, 1));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(1, 0));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(1, 1));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(0, maxSize));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(5, 4));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize, 0));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize));
                    break;
            }
            count++;
        }

        //remove middle areas
        boardManager.getBoard().getTile(1, 2).setInvisible(true);
        boardManager.getBoard().getTile(1, 6).setInvisible(true);
        boardManager.getBoard().getTile(1, 7).setInvisible(true);

        boardManager.getBoard().getTile(2, 1).setInvisible(true);
        boardManager.getBoard().getTile(2, 2).setInvisible(true);
        boardManager.getBoard().getTile(2, 3).setInvisible(true);
        boardManager.getBoard().getTile(2, 4).setInvisible(true);
        boardManager.getBoard().getTile(2, 6).setInvisible(true);
        boardManager.getBoard().getTile(2, 7).setInvisible(true);

        boardManager.getBoard().getTile(3, 4).setInvisible(true);

        boardManager.getBoard().getTile(4, 1).setInvisible(true);
        boardManager.getBoard().getTile(4, 2).setInvisible(true);
        boardManager.getBoard().getTile(4, 4).setInvisible(true);
        boardManager.getBoard().getTile(4, 5).setInvisible(true);
        boardManager.getBoard().getTile(4, 6).setInvisible(true);
        boardManager.getBoard().getTile(4, 7).setInvisible(true);

        boardManager.getBoard().getTile(5, 1).setInvisible(true);
        boardManager.getBoard().getTile(5, 2).setInvisible(true);

        boardManager.getBoard().getTile(6, 1).setInvisible(true);
        boardManager.getBoard().getTile(6, 2).setInvisible(true);
        boardManager.getBoard().getTile(6, 3).setInvisible(true);
        boardManager.getBoard().getTile(6, 6).setInvisible(true);
        boardManager.getBoard().getTile(6, 7).setInvisible(true);

        boardManager.getBoard().getTile(7, 2).setInvisible(true);
        boardManager.getBoard().getTile(7, 3).setInvisible(true);
        boardManager.getBoard().getTile(7, 6).setInvisible(true);

        //place entities
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(6, 4));
        boardManager.add(EntityConstructor.brokenPillar(), new BoardPosition(6, 5));

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNDiagUpSlantHoles(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(.7f, .7f, 0, 1), Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 2)
                col ++;
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }

        boardManager.getBoard().getTile(0, 0).setInvisible(true);

        boardManager.getBoard().getTile(3, 0).setInvisible(true);
        boardManager.getBoard().getTile(2, 2).setInvisible(true);
        boardManager.getBoard().getTile(1, 4).setInvisible(true);
        boardManager.getBoard().getTile(0, maxSize).setInvisible(true);

        boardManager.getBoard().getTile(maxSize, 0).setInvisible(true);
        boardManager.getBoard().getTile(maxSize - 1, 2).setInvisible(true);
        boardManager.getBoard().getTile(maxSize - 2, 4).setInvisible(true);
        boardManager.getBoard().getTile(maxSize - 3, maxSize).setInvisible(true);

        boardManager.getBoard().getTile(maxSize, maxSize).setInvisible(true);

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNDiagDownSlantPillars(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(.7f, .7f, 0, 1), Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 2)
                col ++;
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }

        boardManager.add(EntityConstructor.pillar(), 0, maxSize);

        boardManager.add(EntityConstructor.pillar(), 0, 0);
        boardManager.add(EntityConstructor.pillar(), 1, 2);
        boardManager.add(EntityConstructor.pillar(), 2, 4);
        boardManager.add(EntityConstructor.pillar(), 3, maxSize);

        boardManager.add(EntityConstructor.pillar(), maxSize - 3, 0);
        boardManager.add(EntityConstructor.pillar(), maxSize - 2, 2);
        boardManager.add(EntityConstructor.pillar(), maxSize - 1, 4);
        boardManager.add(EntityConstructor.pillar(), maxSize, maxSize);

        boardManager.add(EntityConstructor.pillar(), maxSize, 0);

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNEmptyRowMiddle(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(.7f, .7f, 0, 1), Color.LIGHT_GRAY, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;
        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 2)
                col ++;
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }

        //remove spaces
        for (int i = 1; i <= maxSize; i += 2) {
            for (int j = 1; j <= maxSize - 1; j ++) {
                boardManager.getBoard().getTile(i, j).setInvisible(true);
            }
        }

        return new Battle2PRules(screen, teams);
    }


    public static Rules makeSNRoundAbout(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(9, 9, new Color(0, .3f, .8f, 1), Color.LIGHT_GRAY, 700 / 9), new CodeBoard(9, 9));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int count = 0;
        for (Entity e : teams.get(0).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(0, 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(1, 0));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(1, 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(2, 1));
                    break;
            }
            count++;
        }
        count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(maxSize, maxSize - 1));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(maxSize - 1, maxSize));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(maxSize - 2, maxSize - 1));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(maxSize - 1, maxSize - 2));
                    break;
            }
            count++;
        }

        //remove middle + corner
        boardManager.getBoard().getTile(2, 3).setInvisible(true);
        boardManager.getBoard().getTile(2, 4).setInvisible(true);
        boardManager.getBoard().getTile(2, 5).setInvisible(true);
        boardManager.getBoard().getTile(2, 6).setInvisible(true);

        boardManager.getBoard().getTile(3, 2).setInvisible(true);
        boardManager.getBoard().getTile(3, 3).setInvisible(true);
        boardManager.getBoard().getTile(3, 4).setInvisible(true);
        boardManager.getBoard().getTile(3, 5).setInvisible(true);
        boardManager.getBoard().getTile(3, 6).setInvisible(true);

        boardManager.getBoard().getTile(4, 2).setInvisible(true);
        boardManager.getBoard().getTile(4, 3).setInvisible(true);
        boardManager.getBoard().getTile(4, 5).setInvisible(true);
        boardManager.getBoard().getTile(4, 6).setInvisible(true);

        boardManager.getBoard().getTile(5, 2).setInvisible(true);
        boardManager.getBoard().getTile(5, 3).setInvisible(true);
        boardManager.getBoard().getTile(5, 4).setInvisible(true);
        boardManager.getBoard().getTile(5, 5).setInvisible(true);
        boardManager.getBoard().getTile(5, 6).setInvisible(true);

        boardManager.getBoard().getTile(6, 2).setInvisible(true);
        boardManager.getBoard().getTile(6, 3).setInvisible(true);
        boardManager.getBoard().getTile(6, 4).setInvisible(true);
        boardManager.getBoard().getTile(6, 5).setInvisible(true);

        boardManager.getBoard().getTile(0, 0).setInvisible(true);
        boardManager.getBoard().getTile(maxSize, maxSize).setInvisible(true);

        //place entities
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(4, 4));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(0, maxSize));
        boardManager.add(EntityConstructor.brokenPillar(), new BoardPosition(maxSize, 0));

        return new Battle2PRules(screen, teams);
    }
    //endregion
    //region Floor 31 - 39
    public static Rules makeSNBasic3(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        boardManager.setBoards(new Board(7, 7, new Color(.2f, .7f, .2f, 1), new Color(.2f, .2f, .2f, 1), 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int col = 1;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(0, col));
            if (col == 2)
                col ++;
            col++;
        }
        col = 5;
        for (Entity e : teams.get(1).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }

        //place things
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(1, 2));
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(1, 4));
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(5, 2));
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(5, 4));
        boardManager.add(EntityConstructor.gargoyleStatue(), new BoardPosition(3, 3));

        return new Battle2PRules(screen, teams);
    }

    //endregion


    //region Boss Arenas
    public static Rules makeSNBlazePneumaArena(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        Sprite lightTiles = atlas.createSprite("LightTileFancy");
        lightTiles.setColor(new Color(.4f, .1f, .1f, 1));
        Sprite darkTiles = atlas.createSprite("DarkTileFancy");
        boardManager.setBoards(new Board(7, 7, darkTiles, lightTiles, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int col = 5;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }
        int count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(0, 3));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(0, 2));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, 4));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(1, 3));
                    break;
            }
            count++;
        }

        //place things
        boardManager.add(EntityConstructor.torch(), new BoardPosition(2, 2));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(2, 4));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(4, 2));
        boardManager.add(EntityConstructor.torch(), new BoardPosition(4, 4));

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNAquaPneumaArena(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        Sprite lightTiles = atlas.createSprite("LightTileFancy");
        lightTiles.setColor(new Color(.2f, .4f, .9f, 1));
        Sprite darkTiles = atlas.createSprite("DarkTileFancy");
        darkTiles.setColor(new Color(0, .3f, .8f, 1));
        boardManager.setBoards(new Board(5, 5, darkTiles, lightTiles, 100), new CodeBoard(5, 5));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int col = 4;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 3)
                col --;
            col--;
        }
        int count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(0, 2));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(0, 1));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, 3));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(1, 2));
                    break;
            }
            count++;
        }

        //place things
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(1, 1));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(1, maxSize - 1));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(maxSize - 1, 1));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(maxSize - 1, maxSize - 1));

        return new Battle2PRules(screen, teams);
    }

    public static Rules makeSNElectroPneumaArena(BattleScreen screen, Array<Team>  teams, BoardManager boardManager) {
        Sprite lightTiles = atlas.createSprite("LightTileFancy");
        lightTiles.setColor(new Color(.8f, .8f, .2f, 1));
        Sprite darkTiles = atlas.createSprite("DarkTileFancy");
        darkTiles.setColor(new Color(.3f, .3f, .3f, 1));
        boardManager.setBoards(new Board(7, 7, darkTiles, lightTiles, 100), new CodeBoard(7, 7));
        final int maxSize = boardManager.getBoard().getColumnSize() - 1;

        //place entities
        int col = 5;
        for (Entity e : teams.get(0).getEntities()) {
            boardManager.add(e, new BoardPosition(maxSize, col));
            if (col == 4)
                col --;
            col--;
        }
        int count = 0;
        for (Entity e : teams.get(1).getEntities()) {
            switch (count) {
                case 0:
                    boardManager.add(e, new BoardPosition(0, 3));
                    break;
                case 1:
                    boardManager.add(e, new BoardPosition(0, 4));
                    break;
                case 2:
                    boardManager.add(e, new BoardPosition(0, 2));
                    break;
                case 3:
                    boardManager.add(e, new BoardPosition(1, 3));
                    break;
            }
            count++;
        }

        //place things
        boardManager.add(EntityConstructor.brokenPillar(), new BoardPosition(1, 1));
        boardManager.add(EntityConstructor.brokenPillar(), new BoardPosition(1, maxSize - 1));
        boardManager.add(EntityConstructor.brokenPillar(), new BoardPosition(maxSize - 1, 1));
        boardManager.add(EntityConstructor.pillar(), new BoardPosition(maxSize - 1, maxSize - 1));

        //make invisible
        boardManager.getBoard().getTile(3, 3).setInvisible(true);
        return new Battle2PRules(screen, teams);
    }
    //endregion
}
