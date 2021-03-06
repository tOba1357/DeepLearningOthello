package launcher;

import game.Object.Board;
import game.Object.NeuralNetwork;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

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
        final NeuralNetwork neuralNetwork = NeuralNetwork.create(
                client.getWeight(),
                client.getBiase()
        );
        neuralNetwork.forward(board.convertToOneRowDoubleList()).forEach(System.out::println);
        transport1.close();
    }
}
