package launcher;

import game.Object.Board;
import game.Object.NeuarlNetwork;
import game.Object.Turn;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

/**
 * @author Tatsuya Oba
 */
public class Test {
    public static void main(String[] args) throws TException {
        final Board board = Board.createBoardFromString(
                " ________ \n" +
                " ________ \n" +
                " ______o_ \n" +
                " ___oxo__ \n" +
                " _xxxxxo_ \n" +
                " _xxxxoxx \n" +
                " __x_xx__ \n" +
                " __x__x__ "
        );
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServer.Client client = new LearningServer.Client(protocol);
        final NeuarlNetwork neuarlNetwork = NeuarlNetwork.create(
                client.getWeight(),
                client.getBiase()
        );
        board.getChildBoardList(Turn.BLACK).forEach(board1 -> {
            final StringBuilder builder = new StringBuilder();
            builder.append("[");
            neuarlNetwork.forward(board1.convertToOneRowDoubleList())
                    .forEach(evaluation -> builder.append(evaluation).append(","));
            builder.append("]");
            builder.append(board1.toString());
            System.out.println(builder);
        });
        transport.close();
    }
}
