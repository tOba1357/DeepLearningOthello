package launcher;

import game.AI.LearningAI;
import game.AI.RandomAI;
import game.GameForLearning;
import game.Object.Turn;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import utils.FileName;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tatsuya Oba
 */
public class LearningLoseLauncher {
    private static final String SAVE_FILE_NAME = FileName.SAVE_FILE.getFileName();
    private static final List<Short> winResult = Arrays.asList((short)1, (short)0, (short)0);
    private static final List<Short> loseResult = Arrays.asList((short)0, (short)0, (short)1);
    private static final List<Short> drawResult = Arrays.asList((short)0, (short)1, (short)0);

    public static void main(String[] args) throws TException {
        final TSocket transport = new TSocket("localhost", 9090);
        transport.open();
        final TProtocol protocol = new TBinaryProtocol(transport);
        final LearningServer.Client client = new LearningServer.Client(protocol);

        final LearningAI blackAI1 = new LearningAI(Turn.BLACK, client);
        final LearningAI whiteAI2 = new LearningAI(Turn.WHITE, client);

        final RandomAI whiteAI1 = new RandomAI(Turn.WHITE);
        final RandomAI blackAI2 = new RandomAI(Turn.BLACK);

        client.load(SAVE_FILE_NAME);

        for(int i = 0; ; i++) {
            final GameForLearning game = new GameForLearning(
                    blackAI1,
                    whiteAI1
            );
            int counter = 0;
            int drawCounter = 0;
            game.setRecodeBlack(false);
            game.setRecodeWhite(true);
            game.setRecodeNon(true);
            for (int j = 0; ; j++) {
                game.clear();
                game.start();
                if (game.getWinner() == Turn.BLACK) continue;
                if (game.getWinner() == null) {
                    drawCounter++;
                }
                if (++counter >= 50) {
                    System.out.println(i + ":" + drawCounter + "/" + j + "," + (counter - drawCounter) + "/" + j);
                    break;
                }
            }
            counter = 0;
            drawCounter = 0;
            game.setBlackAI(blackAI2);
            game.setWhiteAI(whiteAI2);
            game.setRecodeBlack(true);
            game.setRecodeWhite(false);
            game.setRecodeNon(true);
            for (int j = 0; ; j++) {
                game.clear();
                game.start();

                if (game.getWinner() == Turn.WHITE) continue;
                if (game.getWinner() == null) {
                    drawCounter++;
                }
                if (++counter >= 50) {
                    System.out.println(i + ":" + drawCounter + "/" + j + "," + (counter - drawCounter) + "/" + j);
                    break;
                }
            }
            final Pair<List<List<Double>>, List<List<Short>>> data = game.getNode().convertToObjectForLearning();
            client.learning(data.getLeft(), data.getRight());
            client.save(SAVE_FILE_NAME);
            System.out.println();
        }
//        transport1.close();
    }
}
