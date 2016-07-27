package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import game.Object.Board;
import game.Object.Cell;

import java.lang.reflect.Type;

/**
 * @author Tatsuya Oba
 */
public class JsonHelper {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Board.class, new BoardSerializer())
            .create();

    public static String toJson(final Object object) {
        return GSON.toJson(object);
    }

    public static<T> T fromJson(final String json, final Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    private static class BoardSerializer implements JsonSerializer<Board> {

        @Override
        public JsonElement serialize(Board board, Type type, JsonSerializationContext jsonSerializationContext) {
            return jsonSerializationContext.serialize(board.convertToOneRowArray());
        }

//        @Override
//        public Board deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
//            final JsonArray array = jsonElement.getAsJsonArray();
//            final Board board = new Board();
//            board.setInitBoard();
//            final Cell[][] cells = board.getBoard();
//            for (int i = 0; i < array.size(); i++) {
//                final Cell cell = Cell.values()[array.get(i).getAsInt()];
//                cells[i / Board.BOARD_SIZE + 1][i % Board.BOARD_SIZE + 1] = cell;
//            }
//            return board;
//        }
    }
}
