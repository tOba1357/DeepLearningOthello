package utils;

import game.Object.Winner;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tatsuya Oba
 */
public class DeepLearningHelper {
    private static final List<Double> BLACK_WIN_RESULT = Arrays.asList(1.0, 0.0, 0.0);
    private static final List<Double> WHITE_WIN_RESULT = Arrays.asList(0.0, 0.0, 1.0);
    private static final List<Double> DRAW_RESULT = Arrays.asList(0.0, 1.0, 0.0);

    public static List<Double> getResultFromWinner(final Winner winner) {
        switch (winner) {
            case BLACK:
                return BLACK_WIN_RESULT;
            case WHITE:
                return WHITE_WIN_RESULT;
            case DRAW:
                return DRAW_RESULT;
        }
        throw new IllegalArgumentException("not find matching counter");
    }

}
