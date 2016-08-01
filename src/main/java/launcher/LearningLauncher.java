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

        final TSocket transport2 = new TSocket("localhost", 9091);
        transport2.open();
        final TProtocol protocol2 = new TBinaryProtocol(transport2);
        final LearningServer.Client client2 = new LearningServer.Client(protocol2);
//        client2.load(FileName.SAVE_WHITE_FILE.getFileName());

        for (int i = 0; ; i++) {
            final NeuarlNetwork blackNeuarlNetwork = NeuarlNetwork.create(
                    client1.getWeight(),
                    client1.getBiase()
            );
            final NeuarlNetwork whiteNeuarlNetwork = NeuarlNetwork.create(
                    client2.getWeight(),
                    client2.getBiase()
            );
            final BaseAI blackAI1 = new LearningAI(Turn.BLACK, blackNeuarlNetwork);
            final BaseAI whiteAI1 = new LearningEnemyAI(Turn.WHITE, whiteNeuarlNetwork);
            final Thread blackThread = createThread(
                    blackAI1,
                    whiteAI1,
                    100,
                    client1
            );
            blackThread.run();

            final BaseAI blackAI2 = new LearningEnemyAI(Turn.BLACK, blackNeuarlNetwork);
            final BaseAI whiteAI2 = new LearningAI(Turn.WHITE, whiteNeuarlNetwork);
            final Thread whiteThread = createThread(
                    blackAI2,
                    whiteAI2,
                    100,
                    client2
            );
            whiteThread.run();

            try {
                blackThread.join();
                whiteThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client1.save(FileName.SAVE_BLACK_FILE.getFileName());
            client2.save(FileName.SAVE_WHITE_FILE.getFileName());
        }

    }

    private static Thread createThread(
            final BaseAI blackAI,
            final BaseAI whiteAI,
            final int gameCountNum,
            final LearningServer.Client client
    ) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                final List<List<Short>> boardList = new ArrayList<>();
                final List<List<Double>> resultList = new ArrayList<>();
                IntStream.range(0, gameCountNum).parallel().forEach(i -> {
                    final Game game = new Game(blackAI, whiteAI);
                    final Winner winner = game.start();
                    synchronized (this) {
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
                try {
                    client.learning(
                            resultList,
                            boardList
                    );
                } catch (TException e) {
                    e.printStackTrace();
                }
            }
        });
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
