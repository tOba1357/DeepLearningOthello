package game;

import game.AI.BaseAI;
import game.Object.Board;
import game.Object.BoardNode;
import game.Object.Position;
import game.Object.Turn;

/**
 * @author Tatsuya Oba
 */
public class GameForLearning {
    private final Board board;
    private BaseAI blackAI;
    private BaseAI whiteAI;
    private BoardNode node;

    private Turn turn;
    private Turn winner;

    private boolean recodeBlack = true;
    private boolean recodeWhite = true;
    private boolean recodeNon = true;

    public GameForLearning(
            final BaseAI blackAI,
            final BaseAI whiteAI
    ) {
        this.blackAI = blackAI;
        this.whiteAI = whiteAI;
        this.board = new Board();
        this.board.setInitBoard();
        this.turn = Turn.BLACK;
        this.winner = null;
        this.node = new BoardNode(
                null,
                board.clone()
        );
    }

    public void start() {
        while (true) {
            if (turn == null) {
                endGameTask();
                return;
            }
            final Position putPosition =
                    Turn.BLACK.equals(turn) ? blackAI.getPutPosition(board) : whiteAI.getPutPosition(board);
            if (!board.put(putPosition, turn)) {
                System.out.println("not put error");
                return;
            }
            final BoardNode childNode = node.getNodeFromBoard(board);
            if (childNode == null) {
                node = node.addChild(board);
            }
            setNextTurn();
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
            this.winner = Turn.BLACK;
            if (recodeBlack) {
                this.node = node.setResult(winner);
            }
        } else if (blackCellNum < whiteCellNum) {
            this.winner = Turn.WHITE;
            if (recodeWhite) {
                this.node = node.setResult(winner);
            }
        } else {
            this.winner = null;
            if (recodeNon) {
                this.node = node.setResult(winner);
            }
        }
    }

    public Turn getWinner() {
        return winner;
    }

    public void clear() {
        board.setInitBoard();
        turn = Turn.BLACK;
        winner = null;
        node = node.getTopNode();
    }

    public void setWhiteAI(final BaseAI whiteAI) {
        this.whiteAI = whiteAI;
    }

    public void setBlackAI(final BaseAI blackAI) {
        this.blackAI = blackAI;
    }

    public BoardNode getNode() {
        return node;
    }

    public void setRecodeBlack(boolean recodeBlack) {
        this.recodeBlack = recodeBlack;
    }

    public void setRecodeWhite(boolean recodeWhite) {
        this.recodeWhite = recodeWhite;
    }

    public void setRecodeNon(boolean recodeNon) {
        this.recodeNon = recodeNon;
    }
}
