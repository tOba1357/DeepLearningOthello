package launcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import game.AI.LearningAI;
import game.AI.LearningEnemyAI;
import game.Game;
import game.Object.Turn;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import utils.FileName;
import utils.JsonHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Tatsuya Oba
 */
public class LearningLauncher {
    private static final String SAVE_FILE_NAME = FileName.SAVE_FILE.getFileName();
    private static final List<Short> winResult = Arrays.asList((short)1, (short)0, (short)0);
    private static final List<Short> loseResult = Arrays.asList((short)0, (short)0, (short)1);
    private static final List<Short> drawResult = Arrays.asList((short)0, (short)1, (short)0);

    public static void main(String[] args) throws TException {
        final TSocket transport1 = new TSocket("localhost", 9090);
        transport1.open();
        final TProtocol protocol1 = new TBinaryProtocol(transport1);
        final LearningServer.Client client = new LearningServer.Client(protocol1);
        final LearningAI blackAI = new LearningAI(Turn.BLACK, client);
        final LearningEnemyAI whiteAI = new LearningEnemyAI(Turn.WHITE, client);

        client.load(SAVE_FILE_NAME);

        int counter = 0;
        while(true) {
            final List<List<Short>> resultList = new ArrayList<>();
            final List<List<Short>> boardList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                final Game game = new Game(
                        blackAI,
                        whiteAI
                );
                counter++;
                game.start();
                boardList.addAll(game.getHistoryBoards().stream()
                        .map(board -> board.convertToOneRowArray())
                        .map(Arrays::asList)
                        .collect(Collectors.toList())
                );
                for (int j = 0; j < game.getHistoryBoards().size(); j++) {
                    resultList.add(game.getWinner() == Turn.BLACK ? winResult : game.getWinner() == Turn.WHITE ? loseResult : drawResult);
                }
            }
//            client.learning(resultList, boardList);
            System.out.println(counter);
            client.save(SAVE_FILE_NAME);
        }
//        transport1.close();
//        transport2.close();
    }
}
