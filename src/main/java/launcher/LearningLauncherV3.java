package launcher;

import game.GameForLearningV2;
import game.GameForLearningV3;
import game.LearningAI.BaseLearningAI;
import game.LearningAI.GreedAI;
import game.LearningAI.RandomAI;
import game.LearningAI.eGreedAI;
import game.Object.NeuralNetwork;
import game.Object.TrainDataSet;
import game.Object.Turn;
import game.Object.Winner;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Tatsuya Oba
 */
public class LearningLauncherV3 {
    public static void main(String[] rags) throws TException, InterruptedException {
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServerV3.Client client = new LearningServerV3.Client(protocol);

        Thread trainThread = null;
        for (int i = 0; ; i++) {
            final NeuralNetwork neuralNetwork = NeuralNetwork.createTanh(
                    client.getWeight(),
                    client.getBiase()
            );
            if (i % 20 == 0) {
                eval(neuralNetwork);
            }
            for (int j = 0; j < 10; j++) {
                final List<List<List<Double>>> allDataSet = IntStream.range(0, 20).parallel()
                        .mapToObj(k -> {
                            final eGreedAI blackAI = new eGreedAI(Turn.BLACK, neuralNetwork, 0.7);
                            final eGreedAI whiteAI = new eGreedAI(Turn.WHITE, neuralNetwork, 0.7);
                            final GameForLearningV3 game = new GameForLearningV3(blackAI, whiteAI);
                            final Winner winner = game.start();
                            final TrainDataSet dataSet1 = blackAI.getTrainDataSet();
                            final TrainDataSet dataSet2 = whiteAI.getTrainDataSet();
                            final List<List<List<Double>>> dataset = dataSet1.toTrainDataForTF(winner);
                            dataset.addAll(dataSet2.toTrainDataForTF(winner));
                            return dataset;
                        })
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                if (trainThread != null) {
                    try {
                        trainThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                trainThread = new Thread(() -> {
                    try {
                        client.train(allDataSet);
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                });
                trainThread.run();
            }
            trainThread.join();
            client.save();
        }
    }

    public static void eval(final NeuralNetwork neuralNetwork) {
        final AtomicInteger winCounter = new AtomicInteger();
        final AtomicInteger loseCounter = new AtomicInteger();
        final AtomicInteger drawCounter = new AtomicInteger();

        final BaseLearningAI blackAI1 = new GreedAI(Turn.BLACK, neuralNetwork);
        final BaseLearningAI whiteAI1 = new RandomAI(Turn.WHITE);
        IntStream.range(0, 500).parallel().forEach(i -> {
            final GameForLearningV3 game = new GameForLearningV3(
                    blackAI1,
                    whiteAI1
            );
            switch (game.start()) {
                case BLACK:
                    winCounter.incrementAndGet();
                    break;
                case WHITE:
                    loseCounter.incrementAndGet();
                    break;
                case DRAW:
                    drawCounter.incrementAndGet();
                    break;
            }
        });

        final BaseLearningAI blackAI2 = new RandomAI(Turn.BLACK);
        final BaseLearningAI whiteAI2 = new GreedAI(Turn.WHITE, neuralNetwork);
        IntStream.range(0, 500).parallel().forEach(i -> {
            final GameForLearningV3 game = new GameForLearningV3(
                    blackAI2,
                    whiteAI2
            );
            switch (game.start()) {
                case WHITE:
                    winCounter.incrementAndGet();
                    break;
                case BLACK:
                    loseCounter.incrementAndGet();
                    break;
                case DRAW:
                    drawCounter.incrementAndGet();
                    break;
            }
        });
        System.out.println(winCounter.get() + "," + loseCounter.get() + "," + drawCounter.get());
    }
}
