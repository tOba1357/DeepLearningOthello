package game.AI;

import game.Object.Board;
import game.Object.Cell;
import game.Object.NeuarlNetwork;
import game.Object.Position;
import game.Object.Turn;

import java.util.List;
import java.util.stream.Collectors;

public class LearningAI implements BaseAI {
    private final Turn myTurn;
    private final NeuarlNetwork neuarlNetwork;

    public LearningAI(
            final Turn myTurn,
            final NeuarlNetwork neuarlNetwork
    ) {
        this.myTurn = myTurn;
        this.neuarlNetwork = neuarlNetwork;
    }

    @Override
    public String getAIName() {
        return getClass().getSimpleName();
    }

    @Override
    public Position getPutPosition(final Board board) {
        final List<Board> nextBoardList = board.getNextBoardList(myTurn);
        final List<Double> evaluationList = nextBoardList.stream()
                .map(Board::convertToOneRowDoubleList)
                .map(neuarlNetwork::calcu)
                .map(this::getEvaluationalValue)
                .collect(Collectors.toList());

        final int index = evaluationList.indexOf(
                evaluationList.stream()
                        .max((o1, o2) -> (int) ((o1 - o2) * 10000))
                        .get()
        );
        return getPutPosition(board.getBoard(), nextBoardList.get(index).getBoard());
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
            return 1 - values.get(2);
        }
        return 1 - values.get(0);
    }
}
