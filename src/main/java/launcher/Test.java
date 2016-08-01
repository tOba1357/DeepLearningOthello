package launcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tatsuya Oba
 */
public class Test {
    public static void main(String[] args) {
        final List<String> list = new ArrayList<>(20);
        System.out.println(list.size());
        list.forEach(System.out::println);
    }
}
