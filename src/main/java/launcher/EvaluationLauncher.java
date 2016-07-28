package launcher;

import game.AI.LearningAI;
import game.AI.RandomAI;
import game.Game;
import game.Object.NeuarlNetwork;
import game.Object.Turn;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import utils.FileName;

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
        client.load(SAVE_FILE_NAME);

        final NeuarlNetwork neuarlNetwork = NeuarlNetwork.create(
                client.getWeight(),
                client.getBiase()
        );
        final LearningAI blackAI = new LearningAI(Turn.BLACK, neuarlNetwork);

        final RandomAI whiteAI = new RandomAI(Turn.WHITE);

        final Game game = new Game(
                blackAI,
                whiteAI
        );

        int winCounter = 0, loseCounter = 0, drawCounter = 0;
        for (int i = 0; i < 1000; i++) {
            game.start();
            if (game.getWinner() == Turn.BLACK) {
                winCounter++;
            } else if (game.getWinner() == Turn.WHITE) {
                loseCounter++;
            } else {
                drawCounter++;
            }
            game.clear();
        }
        System.out.println("win/lose/draw");
        System.out.println(winCounter + "/" + loseCounter + "/" + drawCounter);
        transport.close();
    }
}
