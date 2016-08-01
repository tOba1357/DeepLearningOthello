package game.AI;

import game.Object.Board;
import game.Object.Position;
import game.Object.Turn;
import launcher.LearningServer;
import org.apache.thrift.TException;

import java.util.Collections;
import java.util.List;

/**
 * @author Tatsuya Oba
 */
public class SimpleMyAI implements BaseAI {
    private final Turn myTurn;
    private final LearningServer.Client client;

    public SimpleMyAI(final Turn myTurn, final LearningServer.Client client) {
        this.myTurn = myTurn;
        this.client = client;
    }

    @Override
    public String getAIName() {
        return getClass().getSimpleName();
    }

    @Override
    public Position getPutPosition(final Board board) {
        Board cloneBoard = board.clone();
        Position putPosition = null;
        double maxEvaluationValue = Integer.MIN_VALUE;
        try {
            for (int i = 1; i <= Board.BOARD_SIZE; i++) {
                for (int j = 1; j <= Board.BOARD_SIZE; j++) {
                    if (cloneBoard.put(new Position(i, j), myTurn)) {
                        final List<Double> calcResult = client.get(Collections.singletonList(cloneBoard.convertToOneRowList())).get(0);
                        final double evaluationValue = getEvaluationalValue(calcResult);
                        if (maxEvaluationValue < evaluationValue) {
                            putPosition = new Position(i, j);
                            maxEvaluationValue = evaluationValue;
                        }
                        cloneBoard = board.clone();
                    }
                }
            }
        } catch (TException e) {
            e.printStackTrace();
        }
        System.out.println(maxEvaluationValue);
        return putPosition;
    }

    private double getEvaluationalValue(final List<Double> values) {
        if (myTurn == Turn.WHITE) {
            return values.get(2) - values.get(0);
        }
        return values.get(0) - values.get(2);
    }

}
