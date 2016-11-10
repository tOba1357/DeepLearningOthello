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

    public Integer toInt() {
        switch (this) {
            case BLACK:
                return 1;
            case BLANK:
                return 0;
            case WHITE:
                return 2;
        }
        return null;
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
