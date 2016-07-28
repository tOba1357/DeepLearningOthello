package game.Object;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {
    public static final int BOARD_SIZE = 8;

    private static final List<Direction> DIRECTIONS = Arrays.asList(
            new Direction(1, 1), new Direction(-1, -1),
            new Direction(1, -1), new Direction(-1, 1),
            new Direction(1, 0), new Direction(0, 1),
            new Direction(-1, 0), new Direction(0, -1)
    );

    private final Cell[][] board;

    public Board() {
        this.board = new Cell[BOARD_SIZE + 2][BOARD_SIZE + 2];
    }

    private Board(final Cell[][] board) {
        this.board = board;
    }

    public boolean put(final Position position, final Turn turn) {
        return put(position, turn, true) > 0;
    }

    private void reverseRow(
            final Position position,
            final Cell cell,
            final int reverseCounter,
            final Direction direction
    ) {
        for (int i = 1; i <= reverseCounter; i++) {
            final int reverseX = position.getX() + direction.getX() * i;
            final int reverseY = position.getY() + direction.getY() * i;
            board[reverseX][reverseY] = cell;
        }
    }

    public void setInitBoard() {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (i == 0 || i == (BOARD_SIZE + 1) || j == 0 || j == (BOARD_SIZE + 1)) {
                    board[i][j] = Cell.WALL;
                } else {
                    board[i][j] = Cell.BLANK;
                }
            }
        }
        board[4][4] = board[5][5] = Cell.BLACK;
        board[4][5] = board[5][4] = Cell.WHITE;
    }

    public Cell[][] getBoard() {
        return board;
    }

    public List<Position> getPutableList(final Turn turn) {
        final List<Position> puttableList = new ArrayList<>();
        for (int i = 1; i <= BOARD_SIZE; i++) {
            for (int j = 1; j <= BOARD_SIZE; j++) {
                final Position position = new Position(i, j);
                if (isPut(position, turn)) {
                    puttableList.add(position);
                }
            }
        }
        return puttableList;
    }

    public List<Board> getNextBoardList(final Turn turn) {
        final List<Board> nextBoardList = new ArrayList<>();
        Board cloneBoard = this.clone();
        for (int i = 1; i <= BOARD_SIZE; i++) {
            for (int j = 1; j <= BOARD_SIZE; j++) {
                final Position position = new Position(i, j);
                if (cloneBoard.put(position, turn)) {
                    nextBoardList.add(cloneBoard);
                    cloneBoard = this.clone();
                }
            }
        }
        return nextBoardList;
    }

    public boolean isPut(final Position position, final Turn turn) {
        return put(position, turn, false) > 0;
    }

    private int put(final Position position, final Turn turn, final boolean reverse) {
        if (!Cell.BLANK.equals(getCell(position))) {
            return 0;
        }
        final Cell cellOfTurn = Cell.getFromTurn(turn);
        final int reverseNum = DIRECTIONS.stream().mapToInt(direction -> {
            int reverseCounter = 0;
            int x = position.getX();
            int y = position.getY();
            while (true) {
                x += direction.getX();
                y += direction.getY();
                final Cell cell = board[x][y];
                if (cell == cellOfTurn) {
                    if (reverse && reverseCounter > 0) {
                        reverseRow(
                                position,
                                cellOfTurn,
                                reverseCounter,
                                direction
                        );
                    }
                    break;
                }
                if (cell == Cell.BLANK || cell == Cell.WALL) {
                    reverseCounter = 0;
                    break;
                }
                reverseCounter++;
            }
            return reverseCounter;
        }).sum();
        if (reverse && reverseNum > 0) {
            board[position.getX()][position.getY()] = cellOfTurn;
        }
        return reverseNum;
    }

    public int getBlackCellNum() {
        return getCellNum(Cell.BLACK);
    }

    public int getWhiteCellNum() {
        return getCellNum(Cell.WHITE);
    }

    public int getCellNum(final Cell cell) {
        int counter = 0;
        for (final Cell[] rows : board) {
            for (final Cell c : rows) {
                if (cell.equals(c)) {
                    counter++;
                }
            }
        }
        return counter;
    }

    public Cell getCell(final Position position) {
        return board[position.getX()][position.getY()];
    }

    public Short[] convertToOneRowArray() {
        final Short[] oneRowBoard = new Short[BOARD_SIZE * BOARD_SIZE * 2];
        for (int i = 1; i <= BOARD_SIZE; i++) {
            for (int j = 1; j <= BOARD_SIZE; j++) {
                final int index = (i - 1) * BOARD_SIZE + (j - 1);
                switch (board[i][j]) {
                    case BLACK:
                        oneRowBoard[index] = 1;
                        oneRowBoard[index + BOARD_SIZE * BOARD_SIZE] = 0;
                        break;
                    case WHITE:
                        oneRowBoard[index] = 0;
                        oneRowBoard[index + BOARD_SIZE * BOARD_SIZE] = 1;
                        break;
                    default:
                        oneRowBoard[index] = 0;
                        oneRowBoard[index + BOARD_SIZE * BOARD_SIZE] = 0;
                        break;
                }
            }
        }
        return oneRowBoard;
    }

    public Double[] convertToOneRowDoubleArray() {
        final Double[] oneRowBoard = new Double[BOARD_SIZE * BOARD_SIZE * 2];
        for (int i = 1; i <= BOARD_SIZE; i++) {
            for (int j = 1; j <= BOARD_SIZE; j++) {
                final int index = (i - 1) * BOARD_SIZE + (j - 1);
                switch (board[i][j]) {
                    case BLACK:
                        oneRowBoard[index] = 1d;
                        oneRowBoard[index + BOARD_SIZE * BOARD_SIZE] = 0d;
                        break;
                    case WHITE:
                        oneRowBoard[index] = 0d;
                        oneRowBoard[index + BOARD_SIZE * BOARD_SIZE] = 1d;
                        break;
                    default:
                        oneRowBoard[index] = 0d;
                        oneRowBoard[index + BOARD_SIZE * BOARD_SIZE] = 0d;
                        break;
                }
            }
        }
        return oneRowBoard;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (final Cell[] rows : board) {
            for (final Cell cell : rows) {
                builder.append(cell);
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public Board clone() {
        final Cell[][] cloneBoard = new Cell[BOARD_SIZE + 2][];
        for (int i = 0; i < board.length; i++) {
            cloneBoard[i] = board[i].clone();
        }
        return new Board(cloneBoard);
    }


    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Board)) {
            return false;
        }
        return Arrays.deepEquals(this.board, ((Board) obj).board);
    }

    public static Board createBoardFromString(final String boardString) {
        final Board board = new Board();
        board.setInitBoard();
        for (int i = 0; i < boardString.toCharArray().length; i++) {
            char c = boardString.toCharArray()[i];
            if ('o' == c) {
                board.getBoard()[i / 8 + 1][i % 8 + 1] = Cell.BLACK;
            }
            if ('x' == c) {
                board.getBoard()[i / 8 + 1][i % 8 + 1] = Cell.WHITE;
            }
            if ('_' == c) {
                board.getBoard()[i / 8 + 1][i % 8 + 1] = Cell.BLANK;
            }
        }
        return board;
    }
}
