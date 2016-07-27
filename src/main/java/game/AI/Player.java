package game.AI;

import game.Object.Board;
import game.Object.Position;
import game.Object.Turn;

import java.util.Scanner;

/**
 * @author Tatsuya Oba
 */
public class Player implements BaseAI {
    private final Turn myTurn;
    private final Scanner scanner;

    public Player(final Turn myTurn) {
        this.myTurn = myTurn;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String getAIName() {
        return getClass().getSimpleName();
    }

    @Override
    public Position getPutPosition(final Board board) {
        while (true) {
            final int x = scanner.nextInt();
            final int y = scanner.nextInt();
            final Position position = new Position(x, y);
            if (board.isPut(position, myTurn)) {
                return position;
            }
        }
    }
}
