package game.LearningAI;

import game.Object.Board;

/**
 * @author Tatsuya Oba
 */
public interface BaseLearningAI {
    Board getNextBoard(final Board board);
}
