package game.AI;

import game.Object.Board;
import game.Object.Cell;
import game.Object.Position;
import game.Object.Turn;
import launcher.LearningServer;
import org.apache.thrift.TException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Tatsuya Oba
 */
public class MyAI implements BaseAI {
    private final Turn myTurn;
    private final LearningServer.Client client;
    private final Integer N;
    private Position putPosition;


    public MyAI(
            final Turn myTurn,
            final LearningServer.Client client,
            final Integer N
    ) {
        this.myTurn = myTurn;
        this.client = client;
        this.N = N;
    }

    @Override
    public String getAIName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Position getPutPosition(final Board board) {
        try {
            System.out.println(alphabeta(board, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0, myTurn));
            final Board cloneBoard = board.clone();
            cloneBoard.put(putPosition, myTurn);
            for (Double aDouble : client.get(Collections.singletonList(Arrays.asList(cloneBoard.convertToOneRowArray()))).get(0)) {
                System.out.print(aDouble + ",");
            }
            System.out.println();
        } catch (TException e) {
            e.printStackTrace();
        }
        return putPosition;
    }

    private double alphabeta(
            final Board board,
            double a,
            double b,
            final int count,
            final Turn turn
    ) throws TException {
        if (turn == null) {
            final int myCellNum = board.getCellNum(Cell.getFromTurn(myTurn));
            final int enemyCellNum = board.getCellNum(Cell.getFromTurn(myTurn.getEnemyTurn()));
            if (myCellNum > enemyCellNum) {
                return Double.POSITIVE_INFINITY;
            }
            if (myCellNum < enemyCellNum) {
                return Double.NEGATIVE_INFINITY;
            }
            return 0;
        }
        if (count == N) {
            final List<Double> calcResult = client.get(Collections.singletonList(Arrays.asList(board.convertToOneRowArray()))).get(0);
            return getEvaluationalValue(calcResult);
        }
        final List<Board> nextBoardList = board.getNextBoardList(turn);
        for (final Board nextBoard : nextBoardList) {
            final double evaluation = alphabeta(nextBoard, a, b, count + 1, getNextTurn(board, turn));
            if (turn == myTurn) {
                if (evaluation >= a) {
                    a = evaluation;
                    if (count == 0) {
                        this.putPosition = getPutPosition(
                                board.getBoard(),
                                nextBoard.getBoard()
                        );
                    }
                }
                if (a >= b) {
                    return a;
                }
            } else {
                b = Math.min(b, evaluation);
                if (a >= b) {
                    return b;
                }
            }
        }
        return (turn == myTurn) ? a : b;
    }

    private Turn getNextTurn(final Board board, final Turn turn) {
        final Turn enemyTurn = turn.getEnemyTurn();
        if (!board.getPutableList(turn).isEmpty()) {
            return enemyTurn;
        }
        if (board.getPutableList(turn).isEmpty()) {
            return turn;
        }
        return null;
    }

    private Position getPutPosition(final Cell[][] beforeBoard, final Cell[][] afterBoard) {
        for (int i = 1; i <= Board.BOARD_SIZE; i++) {
            for (int j = 1; j <= Board.BOARD_SIZE; j++) {
                if (beforeBoard[i][j] == Cell.BLANK && afterBoard[i][j] != Cell.BLANK) {
                    return new Position(i, j);
                }
            }
        }
        return null;
    }

    private double getEvaluationalValue(final List<Double> values) {
        if (myTurn == Turn.WHITE) {
            return values.get(2) - values.get(0);
        }
        return values.get(0) - values.get(2);
    }
}
