package launcher;

import game.GameForLearningV2;
import game.LearningAI.BaseLearningAI;
import game.LearningAI.RandomAI;
import game.LearningAI.TestAI;
import game.Object.NeuarlNetwork;
import game.Object.Turn;
import game.Object.Winner;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import utils.FileName;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Tatsuya Oba
 */
public class LearningLauncherPhase2V2 {
    private static final String boardFile = "resources/board.dat";
    private static final String resultFile = "resources/result.dat";

    private static final List<Double> winResult = Arrays.asList(1.0, 0.0, 0.0);
    private static final List<Double> loseResult = Arrays.asList(0.0, 0.0, 1.0);
    private static final List<Double> drawResult = Arrays.asList(0.0, 1.0, 0.0);
    public static void main(String[] args) throws TException {
        final List<List<Short>> boards = loadBoards();
        final List<List<Double>> results = loadResults();

//        createData();
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServer.Client client = new LearningServer.Client(protocol);

        client.initial();
        client.learningPhase2(results, boards);
        client.save("phase2");
        transport.close();
    }

    private static List<List<Short>> loadBoards() {
        try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(boardFile))) {
            return (List<List<Short>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("error");
    }


    private static List<List<Double>> loadResults() {
        try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(resultFile))) {
            return (List<List<Double>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException("error");
    }

    private static void createData() throws TException {
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServer.Client client = new LearningServer.Client(protocol);
        client.load(FileName.SAVE_FILE.getFileName());
        final NeuarlNetwork neuarlNetwork = NeuarlNetwork.create(client.getWeight(), client.getBiase());

        final List<List<Short>> boards = new ArrayList<>();
        final List<List<Double>> results = new ArrayList<>();

        final BaseLearningAI blackAI1 = new TestAI(Turn.BLACK, neuarlNetwork);
        final BaseLearningAI whiteAI1 = new RandomAI(Turn.WHITE);
        IntStream.range(0, 10000).parallel().forEach(i -> {
            final GameForLearningV2 game = new GameForLearningV2(blackAI1, whiteAI1);
            final Winner winner = game.start();
            synchronized (LearningLauncherPhase2V2.class) {
                switch (winner) {
                    case WHITE:
                        boards.addAll(game.getHistoryBoards());
                        for (int j = 0; j < game.getHistoryBoards().size(); j++) {
                            results.add(loseResult);
                        }
                        break;
                    case DRAW:
                        boards.addAll(game.getHistoryBoards());
                        for (int j = 0; j < game.getHistoryBoards().size(); j++) {
                            results.add(drawResult);
                        }
                        break;
                }
            }
        });
        System.out.println("end black");
        final BaseLearningAI blackAI2 = new RandomAI(Turn.BLACK);
        final BaseLearningAI whiteAI2 = new TestAI(Turn.WHITE, neuarlNetwork);
        IntStream.range(0, 10000).parallel().forEach(i -> {
            final GameForLearningV2 game = new GameForLearningV2(blackAI2, whiteAI2);
            final Winner winner = game.start();
            synchronized (LearningLauncherPhase2V2.class) {
                switch (winner) {
                    case BLACK:
                        boards.addAll(game.getHistoryBoards());
                        for (int j = 0; j < game.getHistoryBoards().size(); j++) {
                            results.add(winResult);
                        }
                        break;
                    case DRAW:
                        boards.addAll(game.getHistoryBoards());
                        for (int j = 0; j < game.getHistoryBoards().size(); j++) {
                            results.add(drawResult);
                        }
                        break;
                }
            }
        });
        System.out.println("end white");
        System.out.println("date size:" + boards.size());
        try(final ObjectOutputStream oss = new ObjectOutputStream(new FileOutputStream(boardFile))) {
            oss.writeObject(boards);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try(final ObjectOutputStream oss = new ObjectOutputStream(new FileOutputStream(resultFile))) {
            oss.writeObject(results);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
