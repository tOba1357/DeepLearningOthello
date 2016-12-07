package launcher.test;

import game.GameForLearningV2;
import game.LearningAI.BaseLearningAI;
import game.LearningAI.LearningAIV1;
import game.Object.Board;
import game.Object.NeuralNetwork;
import game.Object.Turn;
import launcher.LearningServer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import utils.FileName;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tatsuya Oba
 */
public class OverlapTest {
    public static void main(String[] args) throws TException {
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServer.Client client = new LearningServer.Client(protocol);
        client.load(FileName.SAVE_FILE.getFileName());

        final NeuralNetwork neuralNetwork = NeuralNetwork.create(
                client.getWeight(),
                client.getBiase()
        );
        final BaseLearningAI blackAI = new LearningAIV1(Turn.BLACK, neuralNetwork);
        final BaseLearningAI whiteAI = new LearningAIV1(Turn.WHITE, neuralNetwork);
        final Map<Board, Integer> boardMap = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            final GameForLearningV2 game = new GameForLearningV2(blackAI, whiteAI);
            game.start();
            final Board board = game.getBoard();
            final Integer counter = boardMap.getOrDefault(board, 0);
            boardMap.put(board, counter + 1);
        }
        System.out.println("size:" + boardMap.values().size());
        boardMap.values().forEach(System.out::println);
    }
}
