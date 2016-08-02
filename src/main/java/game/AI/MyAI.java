package game.AI;

import game.Object.Board;
import game.Object.Cell;
import game.Object.NeuarlNetwork;
import game.Object.Position;
import game.Object.Turn;
import org.apache.thrift.TException;

import java.util.Collections;
import java.util.List;

/**
 * @author Tatsuya Oba
 */
public class MyAI implements BaseAI {
    private final Turn myTurn;
    private final NeuarlNetwork neuarlNetwork;
    private final Integer N;
    private Position putPosition;


    public MyAI(
            final Turn myTurn,
            final NeuarlNetwork neuarlNetwork,
            final Integer N
    ) {
        this.myTurn = myTurn;
        this.neuarlNetwork = neuarlNetwork;
        this.N = N;
    }

    @Override
    public String getAIName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Position getPutPosition(final Board board) {
        try {
            System.out.println(alphabeta(
                    board,
                    Double.NEGATIVE_INFINITY,
                    Double.POSITIVE_INFINITY,
                    0,
                    myTurn
            ));
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
            final List<Double> calcResult = neuarlNetwork.calcu(board.convertToOneRowDoubleList());
            return getEvaluationalValue(calcResult, turn);
        }
        final List<Board> childBoardList = board.getChildBoardList(turn);
        for (int i = 0; i < childBoardList.size(); i++) {
            final double evaluation = alphabeta(childBoardList.get(i), a, b, count + 1, getNextTurn(board, turn));
            if (turn == myTurn) {
                if (evaluation >= a) {
                    if (i != 0) {
                        childBoardList.add(0, childBoardList.get(i));
                        childBoardList.remove(i + 1);
                    }
                    a = evaluation;
                    if (count == 0) {
                        this.putPosition = getPutPosition(
                                board.getBoard(),
                                childBoardList.get(i).getBoard()
                        );
                    }
                }
                if (a >= b) {
                    return a;
                }
            } else {
                if (evaluation < b) {
                    b = evaluation;
                    if (i != 0) {
                        childBoardList.add(0, childBoardList.get(i));
                        childBoardList.remove(i + 1);
                    }
                }
                if (a >= b) {
                    return b;
                }
            }
        }
        return (turn == myTurn) ? a : b;
    }

    private Turn getNextTurn(final Board board, final Turn turn) {
        final Turn enemyTurn = turn.getEnemyTurn();
        if (!board.getChildBoardList(enemyTurn).isEmpty()) {
            return enemyTurn;
        }
        if (!board.getChildBoardList(turn).isEmpty()) {
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

    private double getEvaluationalValue(
            final List<Double> values,
            final Turn turn
    ) {
        if (myTurn == Turn.BLACK) {
            if (turn == Turn.BLACK) {
                return 1 - values.get(0);
            }
            return values.get(2) - 1;
        }
        if (turn == Turn.BLACK) {
            return values.get(0) - 1;
        }
        return 1 - values.get(2);

    }
}
