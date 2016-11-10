package game.LearningAI;

import game.Object.Board;
import game.Object.NeuarlNetwork;
import game.Object.Turn;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class LearningAIV1 implements BaseLearningAI {
    private final Random random;
    private final Turn myTurn;
    private final NeuarlNetwork neuarlNetwork;

    public LearningAIV1(
            final Turn myTurn,
            final NeuarlNetwork neuarlNetwork
    ) {
        this.random = new Random();
        this.myTurn = myTurn;
        this.neuarlNetwork = neuarlNetwork;
    }

    @Override
    public Board getNextBoard(final Board board) {
        final List<Board> nextBoardList = board.getChildBoardList(myTurn);
        final List<Double> evaluationList = nextBoardList.stream()
                .map(Board::convertToOneRowDoubleList)
                .map(neuarlNetwork::forward)
                .map(this::getEvaluationalValue)
                .collect(Collectors.toList());
        if (random.nextInt(10) != 0) {
            final int index = IntStream.range(0, evaluationList.size())
                    .reduce((left, right) -> evaluationList.get(left) > evaluationList.get(right) ? left : right)
                    .getAsInt();
            return nextBoardList.get(index);
        }
        final double evaluationSum = evaluationList.stream()
                .mapToDouble(a -> a)
                .sum();
        final double selectValue = random.nextDouble();
        double sum = 0;
        for (int i = 0; i < evaluationList.size(); i++) {
            final double rate = evaluationList.get(i) / evaluationSum;
            sum += rate;
            if (sum >= selectValue) {
                return nextBoardList.get(i);
            }
        }
        throw new IllegalArgumentException("not found put position: selectValue=" + selectValue + ", sum=" + sum);
    }


    private double getEvaluationalValue(final List<Double> values) {
        if (myTurn == Turn.BLACK) {
            return 1 - values.get(2);
        }
        return 1 - values.get(0);
    }
}