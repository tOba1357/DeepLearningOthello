package launcher;

import game.AI.BaseAI;
import game.AI.MonteCarloAI;
import game.AI.RandomAI;
import game.Game;
import game.Object.Turn;
import game.Object.Winner;
import utils.ResultCounter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class MonteCarloEvalLauncher {
    public static void main(String[] args) {
        final BaseAI blackAI = new MonteCarloAI(Turn.BLACK, 200);
        final BaseAI whiteAI = new RandomAI(Turn.WHITE);
        final ResultCounter resultCounter = new ResultCounter();
        IntStream.range(0, 500).parallel()
                .forEach(i -> {
                    final Game game = new Game(
                            blackAI,
                            whiteAI
                    );
                    resultCounter.increment(game.start());
                });
        System.out.println(resultCounter);
        final BaseAI blackAI1 = new RandomAI(Turn.BLACK);
        final BaseAI whiteAI1 = new MonteCarloAI(Turn.WHITE, 200);
        final ResultCounter resultCounter1 = new ResultCounter();
        IntStream.range(0, 500).parallel()
                .forEach(i -> {
                    final Game game = new Game(
                            blackAI1,
                            whiteAI1
                    );
                    resultCounter1.increment(game.start());
                });
        System.out.println(resultCounter1);
        final int win = resultCounter.get(Winner.BLACK) + resultCounter1.get(Winner.WHITE);
        final int loss = resultCounter.get(Winner.WHITE) + resultCounter1.get(Winner.BLACK);
        final int draw = resultCounter.get(Winner.DRAW) + resultCounter1.get(Winner.DRAW);
        final double sum = (win + draw + loss);
        final double rate = (win + draw) / sum;
        System.out.println(win + "," + loss + "," + draw + "," + rate);
    }
}
