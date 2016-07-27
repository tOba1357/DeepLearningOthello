package game.Object;

public enum Turn {
    BLACK("Black"),
    WHITE("White");

    private final String string;

    Turn(final String string) {
        this.string = string;
    }

    public Turn getEnemyTurn() {
        return this.equals(BLACK) ? WHITE : BLACK;
    }

    @Override
    public String toString() {
        return string;
    }
}
