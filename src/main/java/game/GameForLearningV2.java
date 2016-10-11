package game;

import game.LearningAI.BaseLearningAI;
import game.Object.Board;
import game.Object.Turn;
import game.Object.Winner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tatsuya Oba
 */
public class GameForLearningV2 {
    private Board board;
    private final BaseLearningAI blackAI;
    private final BaseLearningAI whiteAI;

    private Turn turn;
    private Winner winner;

    private final List<List<Short>> historyBoards;

    public GameForLearningV2(
            final BaseLearningAI blackAI,
            final BaseLearningAI whiteAI
    ) {
        this.blackAI = blackAI;
        this.whiteAI = whiteAI;
        this.board = new Board();
        this.board.setInitBoard();
        this.historyBoards = new ArrayList<>();
        this.turn = Turn.BLACK;
        this.winner = null;
    }

    public Winner start() {
        while (true) {
            if (turn == null) {
                setWinner();
                return winner;
            }
            board = Turn.BLACK.equals(turn) ? blackAI.getNextBoard(board) : whiteAI.getNextBoard(board);
            setNextTurn();
            historyBoards.add(board.convertToOneRowList());
        }
    }

    private void setNextTurn() {
        if (board.getChildBoardList(turn.getEnemyTurn()).size() > 0) {
            this.turn = turn.getEnemyTurn();
        }
        if (board.getChildBoardList(turn).size() > 0) {
            return;
        }
        this.turn = null;
    }

    private void setWinner() {
        final int blackCellNum = board.getBlackCellNum();
        final int whiteCellNum = board.getWhiteCellNum();
        if (blackCellNum > whiteCellNum) {
            this.winner = Winner.BLACK;
        } else if (blackCellNum < whiteCellNum) {
            this.winner = Winner.WHITE;
        } else {
            this.winner = Winner.DRAW;
        }
    }

    public Winner getWinner() {
        return winner;
    }

    public List<List<Short>> getHistoryBoards() {
        return historyBoards;
    }
}

