package game.Object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class TrainDataSet {
    private final Turn turn;
    private final List<List<Double>> inputList;
    private final List<List<Double>> outputList;
    private final List<Position> positionList;

    public TrainDataSet(final Turn turn) {
        this.turn = turn;
        inputList = new ArrayList<>();
        outputList = new ArrayList<>();
        positionList = new ArrayList<>();
    }

    public void add(
            final List<Double> input,
            final List<Double> output,
            final Position position
    ) {
        inputList.add(input);
        outputList.add(output);
        positionList.add(position);
    }

    public List<List<List<Double>>> toTrainDataForTF(final Winner winner) {
        updateQValue(winner);
        return IntStream.range(0, inputList.size())
                .mapToObj(i -> Arrays.asList(inputList.get(i), outputList.get(i)))
                .collect(Collectors.toList());
    }

    private void updateQValue(final Winner winner) {
        IntStream.range(0, inputList.size())
                .forEach(i -> outputList.get(i).set(
                        positionList.get(i).toOneRowPosition(),
                        getNewQValue(winner, i)
                ));
    }


    private Double getNewQValue(final Winner winner, final Integer index) {
        if (index == inputList.size() - 1) {
            return getLastQValue(winner);
        }
        return outputList.get(index + 1).stream().mapToDouble(a -> a).max().getAsDouble();
    }

    private Double getLastQValue(final Winner winner) {
        switch (winner) {
            case DRAW:
                return 0d;
            default:
                return winner.getTurn() == turn ? 1d : -1d;
        }
    }

}
