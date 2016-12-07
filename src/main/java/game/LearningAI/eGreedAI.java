package game.LearningAI;

import game.Object.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class eGreedAI implements BaseLearningAI {
    private final Turn turn;
    private final NeuralNetwork neuralNetwork;
    private final Random random;
    private final TrainDataSet trainDataSet;
    private final double e;

    public eGreedAI(
            final Turn turn,
            final NeuralNetwork neuralNetwork,
            final double e
    ) {
        this.turn = turn;
        this.neuralNetwork = neuralNetwork;
        this.e = e;
        this.random = new Random();
        trainDataSet = new TrainDataSet(turn);
    }

    @Override
    public Board getNextBoard(final Board board) {
        final List<Double> input = board.convertToOneRowDoubleListDim3();
        input.addAll(turn.toList());
        final List<Double> output = neuralNetwork.forward(input);
        final List<Integer> puttablePositionList = board.getPutableList(turn)
                .stream().map(Position::toOneRowPosition)
                .collect(Collectors.toList());
        IntStream.range(0, 64)
                .filter(i -> !puttablePositionList.contains(i))
                .forEach(i -> output.set(i, -1d));

        final Integer oneRowPosition;
        if (random.nextDouble() <= e) {
            oneRowPosition = puttablePositionList.stream()
                    .mapToInt(i -> i)
                    .max().getAsInt();
        } else {
            oneRowPosition = puttablePositionList.get(random.nextInt(puttablePositionList.size()));
        }

        final Position position = Position.createFromOneRowPosition(oneRowPosition);
        trainDataSet.add(input, output, position);
        return board.put(position, turn);
    }

    public TrainDataSet getTrainDataSet() {
        return trainDataSet;
    }
}
