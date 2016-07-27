package game.Object;

public enum Cell {
    BLANK("_"),
    BLACK("o"),
    WHITE("x"),
    WALL(" ");

    private final String string;

    Cell(final String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }

    public static Cell getFromTurn(final Turn turn) {
        switch (turn) {
            case BLACK:
                return BLACK;
            case WHITE:
                return WHITE;
            default:
                return null;
        }
    }
}
