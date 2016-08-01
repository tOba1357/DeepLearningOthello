package game;

import game.AI.BaseAI;
import game.Object.Board;
import game.Object.Position;
import game.Object.Turn;
import game.Object.Winner;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Board board;
    private final List<List<Short>> historyBoards;
    private final BaseAI blackAI;
    private final BaseAI whiteAI;

    private boolean isPrint = false;
    private int sleepTime = 0;

    private Turn turn;
    private Winner winner;

    public Game(
            final BaseAI blackAI,
            final BaseAI whiteAI
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
            if (isPrint) {
                System.out.println(board);
            }
            if (turn == null) {
                endGameTask();
                return winner;
            }
            if (isPrint) {
                System.out.println("Turn:" + turn.toString());
            }
            final Position putPosition = Turn.BLACK.equals(turn) ? blackAI.getPutPosition(board) : whiteAI.getPutPosition(board);
            if (!board.put(putPosition, turn)) {
                System.out.println("not put error");
                return null;
            }
            setNextTurn();
            historyBoards.add(board.convertToOneRowList());
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setNextTurn() {
        if (board.getNextBoardList(turn.getEnemyTurn()).size() > 0) {
            this.turn = turn.getEnemyTurn();
        }
        if (board.getNextBoardList(turn).size() > 0) {
            return;
        }
        this.turn = null;
    }

    private void endGameTask() {
        final int blackCellNum = board.getBlackCellNum();
        final int whiteCellNum = board.getWhiteCellNum();
        if (blackCellNum > whiteCellNum) {
            this.winner = Winner.BLACK;
            if (isPrint) {
                System.out.println("Winner:" + winner.toString());
            }
        } else if (blackCellNum < whiteCellNum) {
            this.winner = Winner.WHITE;
            if (isPrint) {
                System.out.println("Winner:" + winner.toString());
            }
        } else {
            this.winner = Winner.DRAW;
        }
    }

    public List<List<Short>> getHistoryBoards() {
        return historyBoards;
    }

    public Winner getWinner() {
        return winner;
    }

    public void setIsPrint(final boolean isPrint) {
        this.isPrint = isPrint;
    }

    public void setSleepTime(final int ms) {
        this.sleepTime = ms;
    }
}
