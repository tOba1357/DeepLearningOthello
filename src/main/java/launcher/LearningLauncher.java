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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class LearningLauncher {
    private static int loopNum = 100;
    private static final List<Double> winResult = Arrays.asList(1.0, 0.0, 0.0);
    private static final List<Double> loseResult = Arrays.asList(0.0, 0.0, 1.0);
    private static final List<Double> drawResult = Arrays.asList(0.0, 1.0, 0.0);
    private static List<List<Short>> boardList = new ArrayList<>();
    private static List<List<Double>> resultList = new ArrayList<>();
    private static AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) throws TException, InterruptedException {
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServer.Client client = new LearningServer.Client(protocol);
//        client1.load(FileName.SAVE_FILE.getFileName()));

        for (int i = 0; ; i++) {
            boardList = new ArrayList<>();
            resultList = new ArrayList<>();
            counter.set(0);
            final NeuarlNetwork neuarlNetwork = NeuarlNetwork.create(
                    client.getWeight(),
                    client.getBiase()
            );

            final BaseAI blackAI1 = new LearningAI(Turn.BLACK, neuarlNetwork);
            final BaseAI whiteAI1 = new LearningEnemyAI(Turn.WHITE, neuarlNetwork);
            final Thread thread1 = createThread(
                    blackAI1,
                    whiteAI1,
                    loopNum,
                    true
            );
            thread1.run();

            final BaseAI blackAI2 = new LearningEnemyAI(Turn.BLACK, neuarlNetwork);
            final BaseAI whiteAI2 = new LearningAI(Turn.WHITE, neuarlNetwork);
            final Thread thread2 = createThread(
                    blackAI2,
                    whiteAI2,
                    loopNum,
                    false
            );
            thread2.run();

            thread1.join();
            thread2.join();
            client.learning(
                    resultList,
                    boardList
            );
            client.save(FileName.SAVE_FILE2.getFileName());
            if (loopNum > 10000) break;
            loopNum /= counter.get() / 100.0;
            System.out.println(counter.get() + "," + loopNum);
        }
        transport.close();
    }

    private static Thread createThread(
            final BaseAI blackAI,
            final BaseAI whiteAI,
            final int loopNum,
            final boolean black
    ) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                IntStream.range(0, loopNum).parallel().forEach(j -> {
                    final Game game = new Game(blackAI, whiteAI);
                    final Winner winner = game.start();
                    synchronized (LearningLauncher.class) {
                        switch (winner) {
                            case BLACK:
                                if (!black) {
                                    counter.getAndIncrement();
                                    addResultList(
                                            resultList,
                                            winResult,
                                            game.getHistoryBoards().size()
                                    );
                                    boardList.addAll(game.getHistoryBoards());
                                }
                                break;
                            case WHITE:
                                if (black) {
                                    counter.getAndIncrement();
                                    addResultList(
                                            resultList,
                                            loseResult,
                                            game.getHistoryBoards().size()
                                    );
                                    boardList.addAll(game.getHistoryBoards());
                                }
                                break;
                            case DRAW:
                                counter.getAndIncrement();
                                addResultList(
                                        resultList,
                                        drawResult,
                                        game.getHistoryBoards().size()
                                );
                                boardList.addAll(game.getHistoryBoards());
                                break;
                        }
                    }
                });
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
