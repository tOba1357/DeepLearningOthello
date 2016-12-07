package game.LearningAI;

import game.Object.Board;
import game.Object.NeuralNetwork;
import game.Object.Turn;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class TestAI implements BaseLearningAI {
    private final Turn myTurn;
    private final NeuralNetwork neuralNetwork;

    public TestAI(
            final Turn myTurn,
            final NeuralNetwork neuralNetwork
    ) {
        this.myTurn = myTurn;
        this.neuralNetwork = neuralNetwork;
    }

    @Override
    public Board getNextBoard(Board board) {
        final List<Board> nextBoardList = board.getChildBoardList(myTurn);
        final List<Double> evaluationList = nextBoardList.stream()
                .map(Board::convertToOneRowDoubleList)
                .map(neuralNetwork::forward)
                .map(this::getEvaluationalValue)
                .collect(Collectors.toList());

        final int index = IntStream.range(0, evaluationList.size())
                .reduce((left, right) -> evaluationList.get(left) > evaluationList.get(right) ? left : right)
                .getAsInt();

        return nextBoardList.get(index);
    }


    private double getEvaluationalValue(final List<Double> values) {
        if (myTurn == Turn.BLACK) {
            return 1 - values.get(2);
        }
        return 1 - values.get(0);
    }
}
