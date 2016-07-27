package game.AI;


import game.Object.Board;
import game.Object.Cell;
import game.Object.Position;
import game.Object.Turn;
import launcher.LearningServer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.thrift.TException;
import utils.JsonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private final LearningServer.Client client;
    private final AtomicInteger counter;

    public LearningEnemyAI(final Turn myTurn, final LearningServer.Client client) {
        this.random = new Random(System.currentTimeMillis());
        this.myTurn = myTurn;
        this.client = client;
        this.counter = new AtomicInteger();
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
            final double evaluationSum = evaluationList.stream()
                    .mapToDouble(Double::valueOf)
                    .sum();

            //selected put position by evaluation
            //selected rate = evaluation / sum
            final double selectValue = random.nextDouble();
            double tempSum = 0;
            for (int i = 0; i < evaluationList.size(); i++) {
                final double rate = evaluationList.get(i) / evaluationSum;
                tempSum += rate;
                if (tempSum >= selectValue) {
                    counter.incrementAndGet();
                    return getPutPosition(board.getBoard(), nextBoardList.get(i).getBoard());
                }
            }
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
        if (counter.get() <= 5) {
            return random.nextDouble();
        }
        if (myTurn == Turn.BLACK) {
            return 1 - values.get(2);
        }
        return 1 - values.get(0);
    }
}
