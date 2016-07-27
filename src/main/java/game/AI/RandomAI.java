package game.AI;

import game.Object.Board;
import game.Object.Position;
import game.Object.Turn;

import java.util.List;
import java.util.Random;

public class RandomAI implements BaseAI {
    private final Random random;
    private final Turn myTurn;

    public RandomAI(final Turn myTurn) {
        this.random = new Random(System.currentTimeMillis());
        this.myTurn = myTurn;
    }

    @Override
    public String getAIName() {
        return getClass().getSimpleName();
    }

    @Override
    public Position getPutPosition(Board board) {
        final List<Position> puttablePositions = board.getPutableList(myTurn);
        return puttablePositions.get(random.nextInt(puttablePositions.size()));
    }
}
