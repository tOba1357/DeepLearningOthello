package game.Object;

import org.apache.commons.lang3.tuple.Pair;
import utils.JsonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tatsuya Oba
 */
public class BoardNode {
    private final BoardNode parent;
    private List<BoardNode> childList;
    private final Board board;
    private int win;
    private int lose;
    private int draw;

    public BoardNode(
            final BoardNode parent,
            final Board board
    ) {
        this.childList = new ArrayList<>();
        this.board = board;
        this.parent = parent;
        this.win = 0;
        this.lose = 0;
        this.draw = 0;
    }

    public boolean contain(final Board board) {
        if (childList == null) {
            return false;
        }
        return childList.stream()
                .map(node -> node.board)
                .collect(Collectors.toList())
                .contains(board);
    }

    public List<BoardNode> getChildList() {
        return childList;
    }

    public BoardNode getParent() {
        return parent;
    }

    public Board getBoard() {
        return board;
    }

    public BoardNode getNodeFromBoard(final Board board) {
        if (childList == null) {
            return null;
        }
        return childList.stream()
                .filter(node -> node.board.equals(board))
                .findAny()
                .orElse(null);
    }

    /**
     * @param board
     * @return child node add
     */
    public BoardNode addChild(final Board board) {
        final BoardNode node = new BoardNode(
                this,
                board
        );
        this.childList.add(node);
        return node;
    }

    public int addWin() {
        return ++win;
    }

    public int addLose() {
        return ++lose;
    }

    public int addDraw() {
        return ++draw;
    }

    public int getWin() {
        return win;
    }

    public int getLose() {
        return lose;
    }

    public int getDraw() {
        return draw;
    }

    public List<Double> getRate() {
        final int sum = win + lose + draw;
        return Arrays.asList((double) win / sum, (double) draw / sum, (double) lose / sum);
    }

    /**
     * @param winner
     * @return top node
     */
    public BoardNode setResult(final Turn winner) {
        BoardNode parent = this;
        while (true) {
            if (winner == null) {
                parent.addDraw();
            } else if (winner == Turn.BLACK) {
                parent.addWin();
            } else {
                parent.addLose();
            }
            final BoardNode tmp = parent.getParent();
            if (tmp == null) break;
            parent = parent.getParent();
        }
        return parent;
    }

    public BoardNode getTopNode() {
        BoardNode topNode = this;
        while (true) {
            final BoardNode parent = topNode.getParent();
            if (parent == null) {
                break;
            }
            topNode = parent;
        }
        return topNode;
    }

    public Pair<List<List<Double>>, List<List<Short>>> convertToObjectForLearning() {
        final BoardNode topNode = getTopNode();
        final List<List<Double>> resultList = new ArrayList<>();
        final List<List<Short>> boardList = new ArrayList<>();
        convertToObjectForLearning(
                topNode,
                resultList,
                boardList
        );
        return Pair.of(resultList, boardList);
    }

    private void convertToObjectForLearning(
            final BoardNode node,
            final List<List<Double>> resultList,
            final List<List<Short>> boardList
    ) {
        if (node == null) {
            return;
        }
        if (node.getWin() + node.getLose() + node.getDraw() == 0) return;
        resultList.add(node.getRate());
        boardList.add(Arrays.asList(board.convertToOneRowArray()));
        node.childList.forEach(child -> {
            convertToObjectForLearning(
                    node.getParent(),
                    resultList,
                    boardList
            );
        });
    }
}
