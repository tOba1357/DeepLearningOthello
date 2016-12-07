package game.Object;

public class Position {
    private final Integer x;
    private final Integer y;

    public Position(Integer x, Integer y) {
        this.x = x;
        this.y = y;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Integer toOneRowPosition() {
        return (x - 1) * 8 + (y - 1);
    }

    public static Position createFromOneRowPosition(final Integer pos) {
        return new Position(
                pos / 8 + 1,
                pos % 8 + 1
        );
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
