package game;

import game.LearningAI.BaseLearningAI;
import game.Object.Turn;
import game.Object.Winner;

/**
 * @author Tatsuya Oba
 */
public class GameForLearningV3 extends GameForLearningV2 {

    public GameForLearningV3(
            final BaseLearningAI blackAI,
            final BaseLearningAI whiteAI
    ) {
        super(blackAI, whiteAI);
    }

    @Override
    public Winner start() {
        while (true) {
            if (turn == null) {
                setWinner();
                return winner;
            }
            board = Turn.BLACK.equals(turn) ? blackAI.getNextBoard(board) : whiteAI.getNextBoard(board);
            setNextTurn();
        }
    }
}
