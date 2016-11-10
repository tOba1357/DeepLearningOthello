package game.LearningAI;

import game.Object.Board;
import game.Object.NeuarlNetwork;
import game.Object.Turn;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Tatsuya Oba
 */
public class RandomAI implements BaseLearningAI {
    private final Random random;
    private final Turn myTurn;

    public RandomAI(final Turn myTurn) {
        this.random = new Random();
        this.myTurn = myTurn;
    }

    @Override
    public Board getNextBoard(Board board) {
        final List<Board> nextBoardList = board.getChildBoardList(myTurn);
        return nextBoardList.get(random.nextInt(nextBoardList.size()));
    }
}
