package launcher;

import game.AI.BaseAI;
import game.AI.LearningAI;
import game.AI.LearningEnemyAI;
import game.Game;
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
public class LearningLauncher {
    private static final List<Double> winResult = Arrays.asList(1.0, 0.0, 0.0);
    private static final List<Double> loseResult = Arrays.asList(0.0, 0.0, 1.0);
    private static final List<Double> drawResult = Arrays.asList(0.0, 1.0, 0.0);

    public static void main(String[] args) throws TException {
        final TSocket transport1 = new TSocket("localhost", 9090);
        transport1.open();
        final TProtocol protocol1 = new TBinaryProtocol(transport1);
        final LearningServer.Client client1 = new LearningServer.Client(protocol1);
//        client1.load(FileName.SAVE_BLACK_FILE.getFileName());


        for (int i = 0; ; i++) {
            final NeuarlNetwork neuarlNetwork = NeuarlNetwork.create(
                    client1.getWeight(),
                    client1.getBiase()
            );
            final BaseAI blackAI = new LearningAI(Turn.BLACK, neuarlNetwork);
            final BaseAI whiteAI = new LearningEnemyAI(Turn.WHITE, neuarlNetwork);
            final List<List<Short>> boardList = new ArrayList<>();
            final List<List<Double>> resultList = new ArrayList<>();
            IntStream.range(0, 100).parallel().forEach(j -> {
                final Game game = new Game(blackAI, whiteAI);
                final Winner winner = game.start();
                synchronized (LearningLauncher.class) {
                    switch (winner) {
                        case BLACK:
                            addResultList(
                                    resultList,
                                    winResult,
                                    game.getHistoryBoards().size()
                            );
                            break;
                        case WHITE:
                            addResultList(
                                    resultList,
                                    loseResult,
                                    game.getHistoryBoards().size()
                            );
                            break;
                        case DRAW:
                            addResultList(
                                    resultList,
                                    drawResult,
                                    game.getHistoryBoards().size()
                            );
                            break;
                    }
                    boardList.addAll(game.getHistoryBoards());
                }
            });
            client1.learning(
                    resultList,
                    boardList
            );
            client1.save(FileName.SAVE_FILE.getFileName());
        }
    }


    private static void addResultList(
            final List<List<Double>> resultList,
            final List<Double> result,
            final int num
    ) {
        for (int i = 0; i < num; i++) {
            resultList.add(result);
        }
    }

}
