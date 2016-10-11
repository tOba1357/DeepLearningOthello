package launcher;

import game.GameForLearningV2;
import game.LearningAI.BaseLearningAI;
import game.LearningAI.LearningAIV1;
import game.Object.NeuarlNetwork;
import game.Object.Turn;
import game.Object.Winner;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import utils.FileName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class LearningLauncherV2 {
    private static int loopNum = 20;
    private static final List<Double> winResult = Arrays.asList(1.0, 0.0, 0.0);
    private static final List<Double> loseResult = Arrays.asList(0.0, 0.0, 1.0);
    private static final List<Double> drawResult = Arrays.asList(0.0, 1.0, 0.0);

    public static void main(String[] args) throws TException {
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServer.Client client = new LearningServer.Client(protocol);
        for (int i = 0; ; i++) {
            final NeuarlNetwork neuarlNetwork = NeuarlNetwork.create(
                    client.getWeight(),
                    client.getBiase()
            );
            final BaseLearningAI blackAI = new LearningAIV1(Turn.BLACK, neuarlNetwork);
            final BaseLearningAI whiteAI = new LearningAIV1(Turn.WHITE, neuarlNetwork);

            final List<List<Short>> boards = new ArrayList<>();
            final List<List<Double>> results = new ArrayList<>();
            IntStream.range(0, loopNum).parallel().forEach(j -> {
                final GameForLearningV2 game = new GameForLearningV2(blackAI, whiteAI);
                final Winner winner = game.start();
                final List<Double> result;
                switch (winner) {
                    case BLACK:
                        result = winResult;
                        break;
                    case WHITE:
                        result = loseResult;
                        break;
                    default:
                        result = drawResult;
                }
                synchronized (LearningLauncherV2.class) {
                    boards.addAll(game.getHistoryBoards());
                    for (int k = 0; k < game.getHistoryBoards().size(); k++) {
                        results.add(result);
                    }
                }
            });
            client.learning(results, boards);
            client.save(FileName.SAVE_FILE.getFileName());
            System.out.println(i);
        }
    }
}
