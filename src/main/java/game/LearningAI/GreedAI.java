package game.LearningAI;

import game.Object.Board;
import game.Object.NeuralNetwork;
import game.Object.Position;
import game.Object.Turn;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class GreedAI implements BaseLearningAI {
    private final Turn turn;
    private final NeuralNetwork neuralNetwork;

    public GreedAI(Turn turn, NeuralNetwork neuralNetwork) {
        this.turn = turn;
        this.neuralNetwork = neuralNetwork;
    }

    @Override
    public Board getNextBoard(Board board) {
        final List<Double> input = board.convertToOneRowDoubleListDim3();
        input.addAll(turn.toList());
        final List<Double> output = neuralNetwork.forward(input);
        final List<Integer> puttablePositionList = board.getPutableList(turn)
                .stream().map(Position::toOneRowPosition)
                .collect(Collectors.toList());

        final Integer oneRowPosition = puttablePositionList.stream()
                    .mapToInt(i -> i)
                    .max().getAsInt();

        final Position position = Position.createFromOneRowPosition(oneRowPosition);
        return board.put(position, turn);

    }
}
