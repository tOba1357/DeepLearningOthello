package launcher;

import game.AI.LearningAI;
import game.AI.RandomAI;
import game.Game;
import game.Object.NeuralNetwork;
import game.Object.Turn;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import utils.FileName;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class EvaluationLauncher {
    private static final String SAVE_FILE_NAME = FileName.SAVE_FILE.getFileName();

    public static void main(String[] args) throws TException {
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServer.Client client = new LearningServer.Client(protocol);
        client.load("phase2");

        final NeuralNetwork neuralNetwork = NeuralNetwork.create(
                client.getWeight(),
                client.getBiase()
        );
        final LearningAI blackAI = new LearningAI(Turn.BLACK, neuralNetwork);

        final RandomAI whiteAI = new RandomAI(Turn.WHITE);

        final AtomicInteger winCounter = new AtomicInteger();
        final AtomicInteger loseCounter = new AtomicInteger();
        final AtomicInteger drawCounter = new AtomicInteger();
        IntStream.range(0, 1000).parallel().forEach(i -> {
            final Game game = new Game(
                    blackAI,
                    whiteAI
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
        System.out.println("win/lose/draw");
        System.out.println(winCounter.get() + "/" + loseCounter.get() + "/" + drawCounter.get());
        transport.close();
    }
}
