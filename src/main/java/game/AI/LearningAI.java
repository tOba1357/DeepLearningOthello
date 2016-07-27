package game.AI;

import game.Object.Board;
import game.Object.Cell;
import game.Object.Position;
import game.Object.Turn;
import launcher.LearningServer;
import org.apache.thrift.TException;
import utils.JsonHelper;
import utils.NeuralNetwork;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LearningAI implements BaseAI {
    private final Turn myTurn;
    private final LearningServer.Client client;

    public LearningAI(final Turn myTurn, final LearningServer.Client client) {
        this.myTurn = myTurn;
        this.client = client;
    }

    @Override
    public String getAIName() {
        return getClass().getSimpleName();
    }

    @Override
    public Position getPutPosition(final Board board) {
        try {
            final List<Board> nextBoardList = board.getNextBoardList(myTurn);
            final List<Double> evaluationList = client.get(
                    nextBoardList.stream()
                            .map(Board::convertToOneRowArray)
                            .map(Arrays::asList)
                            .collect(Collectors.toList())
            )
                    .stream()
                    .map(this::getEvaluationalValue)
                    .collect(Collectors.toList());
            final int index = evaluationList.indexOf(
                    evaluationList.stream()
                            .max((o1, o2) -> (int) ((o1 - o2) * 10000))
                            .get()
            );
            return getPutPosition(board.getBoard(), nextBoardList.get(index).getBoard());
        } catch (TException e) {
            e.printStackTrace();
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
        if (myTurn == Turn.BLACK) {
            return 1 - values.get(2);
        }
        return 1 - values.get(0);
    }
}
