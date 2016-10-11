package launcher;

import game.Object.Board;
import game.Object.NeuarlNetwork;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

import java.util.Collections;
import java.util.List;

/**
 * @author Tatsuya Oba
 */
public class TestLauncher {
    public static void main(String[] args) throws TException {
        final TSocket transport1 = new TSocket("localhost", 9090);
        transport1.open();
        final TProtocol protocol1 = new TBinaryProtocol(transport1);
        final LearningServer.Client client = new LearningServer.Client(protocol1);
        final Board board = new Board();
        board.setInitBoard();
        final NeuarlNetwork neuarlNetwork = NeuarlNetwork.create(
                client.getWeight(),
                client.getBiase()
        );
        neuarlNetwork.forward(board.convertToOneRowDoubleList()).forEach(System.out::println);
        transport1.close();
    }
}
