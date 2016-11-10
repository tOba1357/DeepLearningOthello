package game.Object;

/**
 * @author Tatsuya Oba
 */
public enum Winner {
    BLACK, WHITE, DRAW;
    public Turn getTurn() {
        switch (this) {
            case BLACK:
                return Turn.BLACK;
            case WHITE:
                return Turn.WHITE;
            default:
                return null;
        }
    }
}
