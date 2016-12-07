package launcher;

import game.GameForLearningV2;
import game.LearningAI.BaseLearningAI;
import game.LearningAI.LearningAIV1;
import game.LearningAI.RandomAI;
import game.LearningAI.TestAI;
import game.Object.NeuralNetwork;
import game.Object.Turn;
import game.Object.Winner;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import utils.DeepLearningHelper;
import utils.FileName;
import utils.ResultCounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class LearningLauncherV2 {
    private static int LOOP_NUM = 100;

    public static void main(String[] args) throws TException {
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServer.Client client = new LearningServer.Client(protocol);
        client.load(FileName.SAVE_FILE.getFileName());

        Pair<List<List<Short>>, List<List<Double>>> boardsAndResults1 = Pair.of(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        Pair<List<List<Short>>, List<List<Double>>> boardsAndResults2 = Pair.of(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
        for (int i = 0; ; i++) {
            final NeuralNetwork neuralNetwork = NeuralNetwork.create(
                    client.getWeight(),
                    client.getBiase()
            );

            if (i % 10 == 0) {
                boardsAndResults1 = evaluation(neuralNetwork, 100, Turn.BLACK);
                boardsAndResults2 = evaluation(neuralNetwork, 100, Turn.WHITE);
            }

            final BaseLearningAI blackAI = new LearningAIV1(Turn.BLACK, neuralNetwork);
            final BaseLearningAI whiteAI = new LearningAIV1(Turn.WHITE, neuralNetwork);

            final ResultCounter counter = new ResultCounter();

            final List<List<Short>> boards = new ArrayList<>();
            final List<List<Double>> results = new ArrayList<>();
            boards.addAll(boardsAndResults1.getLeft());
            boards.addAll(boardsAndResults2.getLeft());
            results.addAll(boardsAndResults1.getRight());
            results.addAll(boardsAndResults2.getRight());

            IntStream.range(0, LOOP_NUM).parallel().forEach(j -> {
                final GameForLearningV2 game = new GameForLearningV2(blackAI, whiteAI);
                final Winner winner = game.start();

                counter.increment(winner);
                synchronized (LearningLauncherV2.class) {
                    boards.addAll(game.getHistoryBoards());
                    for (int k = 0; k < game.getHistoryBoards().size(); k++) {
                        results.add(DeepLearningHelper.getResultFromWinner(winner));
                    }
                }
            });

            client.learning(results, boards);
            client.save("test_add_random");

            System.out.println("step:" + i);
            System.out.println(counter);
        }
    }

    private static Pair<List<List<Short>>, List<List<Double>>> evaluation(
            final NeuralNetwork neuralNetwork,
            final int num,
            final Turn turn
    ) {
        final ResultCounter counter = new ResultCounter();

        final List<List<Short>> boards = new ArrayList<>();
        final List<List<Double>> results = new ArrayList<>();

        final BaseLearningAI blackAI;
        final BaseLearningAI whiteAI;
        switch (turn) {
            case BLACK:
                blackAI = new TestAI(Turn.BLACK, neuralNetwork);
                whiteAI = new RandomAI(Turn.WHITE);
                break;
            default:
                blackAI = new RandomAI(Turn.BLACK);
                whiteAI = new TestAI(Turn.WHITE, neuralNetwork);
        }

        IntStream.range(0, num).parallel().forEach(i -> {
            final GameForLearningV2 game = new GameForLearningV2(blackAI, whiteAI);
            final Winner winner = game.start();
            counter.increment(winner);
            if (winner.getTurn() == turn) {
                return;
            }
            synchronized (LearningLauncherV2.class) {
                boards.addAll(game.getHistoryBoards());
                for (int j = 0; j < game.getHistoryBoards().size(); j++) {
                    results.add(DeepLearningHelper.getResultFromWinner(winner));
                }
            }
        });

        System.out.println();
        System.out.println(turn.toString());
        System.out.println(counter.toString());
        System.out.println();

        return Pair.of(boards, results);
    }
}
