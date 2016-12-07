package game.AI;


import game.Object.*;
import game.Object.NeuralNetwork;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Tatsuya Oba
 */
public class LearningEnemyAI implements BaseAI {
    private final Random random;
    private final Turn myTurn;
    private final NeuralNetwork neuralNetwork;
    private final AtomicInteger counter;

    public LearningEnemyAI(
            final Turn myTurn,
            final NeuralNetwork neuralNetwork
    ) {
        this.random = new Random(System.currentTimeMillis());
        this.myTurn = myTurn;
        this.neuralNetwork = neuralNetwork;
        this.counter = new AtomicInteger();
    }

    @Override
    public String getAIName() {
        return getClass().getSimpleName();
    }

    @Override
    public Position getPutPosition(final Board board) {
        final List<Board> nextBoardList = board.getChildBoardList(myTurn);

        final List<Double> evaluationList = nextBoardList.stream()
                .map(Board::convertToOneRowDoubleList)
                .map(neuralNetwork::forward)
                .map(this::getEvaluationalValue)
                .collect(Collectors.toList());

        final double evaluationSum = evaluationList.stream()
                .mapToDouble(a -> a)
                .sum();

        //selected put position by evaluation
        //selected rate = evaluation / sum
        final double selectValue = random.nextDouble();
        double tempSum = 0;
        for (int i = 0; i < evaluationList.size(); i++) {
            final double rate = evaluationList.get(i) / evaluationSum;
            tempSum += rate;
            if (tempSum >= selectValue) {
                return getPutPosition(board.getBoard(), nextBoardList.get(i).getBoard());
            }
        }
        final StringBuilder builder = new StringBuilder();
        builder.append("size:").append(evaluationList.size()).append(".\n");
        builder.append("[");
        evaluationList.forEach(evaluation -> builder.append(evaluation).append(","));
        builder.append("]");
        throw new IllegalArgumentException("not found put position\n" + builder.toString());
    }

    private Position getPutPosition(final Cell[][] beforeBoard, final Cell[][] afterBoard) {
        for (int i = 1; i <= Board.BOARD_SIZE; i++) {
            for (int j = 1; j <= Board.BOARD_SIZE; j++) {
                if (beforeBoard[i][j] == Cell.BLANK && afterBoard[i][j] != Cell.BLANK) {
                    return new Position(i, j);
                }
            }
        }
        throw new IllegalArgumentException("not found put position");
    }

    private double getEvaluationalValue(final List<Double> values) {
        if (myTurn == Turn.BLACK) {
            return 1.2 - values.get(2);
        }
        return 1.2 - values.get(0);
    }
}
