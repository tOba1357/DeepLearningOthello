package launcher;

import game.AI.LearningAI;
import game.AI.LearningEnemyAI;
import game.AI.MonteCarloAI;
import game.AI.MyAI;
import game.AI.Player;
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
public class GameLauncher {
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
//        final MyAI blackAI = new MyAI(Turn.BLACK, neuarlNetwork, 3);
        final LearningAI blackAI = new LearningAI(Turn.BLACK, neuarlNetwork);
//        final MonteCarloAI blackAI = new MonteCarloAI(Turn.BLACK, 300);


//        final LearningEnemyAI whiteAI = new LearningEnemyAI(Turn.WHITE, client);
//        final MyAI whiteAI = new MyAI(Turn.WHITE, neuarlNetwork, 3);
//        final Player whiteAI = new Player(Turn.WHITE);
        final LearningAI whiteAI = new LearningAI(Turn.WHITE, neuarlNetwork);
//        final MonteCarloAI whiteAI = new MonteCarloAI(Turn.WHITE, 300);

        final Game game = new Game(blackAI, whiteAI);
        game.setIsPrint(true);
        game.setSleepTime(2000);
        game.start();
        transport.close();
    }
}
