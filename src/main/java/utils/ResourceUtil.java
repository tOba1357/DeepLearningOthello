package utils;

import game.Object.BoardNode;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ResourceUtil {
    public static double[] loadWeights(final String fileName) throws FileNotFoundException {
        final Scanner scanner = new Scanner(new File(fileName));
        final List<Double> weights = new ArrayList<>();
        while (scanner.hasNextDouble()) {
            weights.add(scanner.nextDouble());
        }
        scanner.close();
        final double[] rtnWeights = new double[weights.size()];
        for (int i = 0; i < weights.size(); i++) {
            rtnWeights[i] = weights.get(i);
        }
        return rtnWeights;
    }

    public static void saveWeights(final double[] weights, final String fileName) throws FileNotFoundException {
        final PrintWriter writer = new PrintWriter(fileName);
        for (final double weight : weights) {
            writer.println(weight);
        }
        writer.close();
    }

    public static BoardNode loadNode(final String fileName) {
        final String jsonString;
        try {
            jsonString = String.join("", Files.readAllLines(Paths.get(fileName)));
            if (StringUtils.isBlank(jsonString)) {
                return null;
            }
            return JsonHelper.fromJson(jsonString, BoardNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveNode(final BoardNode node, final String fileName) {
        try (final PrintWriter writer = new PrintWriter(fileName)) {
            writer.print(node.toString());
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
