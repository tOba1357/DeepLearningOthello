package game.AI;

import game.Object.Board;
import game.Object.Position;

public interface BaseAI {
    String getAIName();
    Position getPutPosition(final Board board);
}
