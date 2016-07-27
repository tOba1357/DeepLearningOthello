package game.AI;


import game.Object.Board;
import game.Object.Cell;
import game.Object.Position;
import game.Object.Turn;

import java.util.List;
import java.util.Random;

public class MonteCarloAI implements BaseAI {
    private final Turn myTurn;
    private final int playOutNum;
    private final Random random;

    public MonteCarloAI(final Turn myTurn, final int playOutNum) {
        this.myTurn = myTurn;
        this.playOutNum = playOutNum;
        this.random = new Random(System.currentTimeMillis());
    }

    @Override
    public String getAIName() {
        return getClass().getSimpleName();
    }

    @Override
    public Position getPutPosition(final Board board) {
        final List<Position> puttablePositions = board.getPutableList(myTurn);
        final int playOutNumOfOneNode = puttablePositions.size() / playOutNum;
        double maxEvaluation = 0;
        Position putPosition = null;
        for (final Position position : puttablePositions) {
            final Board nextBoard = board.clone();
            nextBoard.put(position, myTurn);
            final double evaluation = playOut(nextBoard, playOutNumOfOneNode);
            if (maxEvaluation <= evaluation) {
                maxEvaluation = evaluation;
                putPosition = position;
            }
        }

        return putPosition;
    }

    private double playOut(final Board board, final int num) {
        double counter = 0;
        for (int i = 0; i < num; i++) {
            counter += playOut(board.clone(), getNextTurn(board, myTurn));
        }
        return counter;
    }

    private double playOut(final Board board, final Turn turn) {
        if (turn == null) {
            final int myNum = board.getCellNum(Cell.getFromTurn(myTurn));
            final int enemyNum = board.getCellNum(Cell.getFromTurn(myTurn.getEnemyTurn()));
            if (myNum < enemyNum) {
                return 0;
            }
            if (myNum > enemyNum) {
                return 1;
            }
            return 0.3;
        }
        final List<Position> puttablePositions = board.getPutableList(turn);
        board.put(puttablePositions.get(random.nextInt(puttablePositions.size())), turn);
        return playOut(board, getNextTurn(board, turn));
    }

    private Turn getNextTurn(final Board board, final Turn turn) {
        if (board.getPutableList(turn.getEnemyTurn()).size() > 0) {
            return turn.getEnemyTurn();
        }
        if (board.getPutableList(turn).size() > 0) {
            return turn;
        }
        return null;
    }
}
