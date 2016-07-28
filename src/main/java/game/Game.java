package game;

import game.AI.BaseAI;
import game.Object.Board;
import game.Object.Position;
import game.Object.Turn;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Board board;
    private final List<Board> historyBoards;
    private final BaseAI blackAI;
    private final BaseAI whiteAI;

    private boolean isPrint = false;
    private int sleepTime = 0;

    private Turn turn;
    private Turn winner;

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

    public void start() {
        while (true) {
            if (isPrint) {
                System.out.println(board);
            }
            if (turn == null) {
                endGameTask();
                return;
            }
            if (isPrint) {
                System.out.println("Turn:" + turn.toString());
            }
            final Position putPosition = Turn.BLACK.equals(turn) ? blackAI.getPutPosition(board) : whiteAI.getPutPosition(board);
            if (!board.put(putPosition, turn)) {
                System.out.println("not put error");
                return;
            }
            setNextTurn();
            historyBoards.add(board.clone());
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
        if (board.getPutableList(turn.getEnemyTurn()).size() > 0) {
            this.turn = turn.getEnemyTurn();
        }
        if (board.getPutableList(turn).size() > 0) {
            return;
        }
        this.turn = null;
    }

    private void endGameTask() {
        final int blackCellNum = board.getBlackCellNum();
        final int whiteCellNum = board.getWhiteCellNum();
        if (blackCellNum > whiteCellNum) {
            this.winner = Turn.BLACK;
            if (isPrint) {
                System.out.println("Winner:" + winner.toString());
            }
        } else if (blackCellNum < whiteCellNum) {
            this.winner = Turn.WHITE;
            if (isPrint) {
                System.out.println("Winner:" + winner.toString());
            }
        } else {
            this.winner = null;
        }
    }

    public List<Board> getHistoryBoards() {
        return historyBoards;
    }

    public Turn getWinner() {
        return winner;
    }

    public void clear() {
        this.board.setInitBoard();
        this.historyBoards.clear();
        this.turn = Turn.BLACK;
        this.winner = null;
    }

    public void setIsPrint(final boolean isPrint) {
        this.isPrint = isPrint;
    }

    public void setSleepTime(final int ms) {
        this.sleepTime = ms;
    }
}
