package game.Object;

import java.util.Arrays;
import java.util.List;

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

    private static final List<Double> BLACK_LIST = Arrays.asList(1d, 0d);
    private static final List<Double> WHITE_LIST = Arrays.asList(0d, 1d);
    public List<Double> toList() {
        switch (this) {
            case BLACK:
                return BLACK_LIST;
            case WHITE:
                return WHITE_LIST;
        }
        return null;
    }
}
