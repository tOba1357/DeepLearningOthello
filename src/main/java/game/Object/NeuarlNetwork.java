package game.Object;


import launcher.LearningServer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tatsuya Oba
 */
public class NeuarlNetwork {
    private final Layer[] layers;

    public static void main(String[] args) {
        final LearningServer server = new LearningServer();
    }

    private NeuarlNetwork(final Layer[] layers) {
        this.layers = layers;
    }

    public List<Double> calcu(final List<Double> in) {
        return calcu(in, 0);
    }

    private List<Double> calcu(final List<Double> in, final int layerIndex) {
        if (layers.length == layerIndex) return in;
        return calcu(layers[layerIndex].calcu(in), layerIndex + 1);
    }

    public static NeuarlNetwork create(
            final List<List<List<Double>>> weights,
            final List<List<Double>> biases
    ) {
        final Layer[] layers = new Layer[weights.size()];
        for (int i = 0; i < weights.size(); i++) {
            layers[i] = new Layer(weights.get(i), biases.get(i));
        }
        return new NeuarlNetwork(layers);
    }

    private static class Layer {
        final List<List<Double>> weights;
        final List<Double> biases;

        private Layer(
                final List<List<Double>> weights,
                final List<Double> biases
        ) {
            this.weights = weights;
            this.biases = biases;
        }

        private List<Double> calcu(final List<Double> in) {
            final List<Double> result = new ArrayList<>();
            for (int i = 0; i < weights.get(0).size(); i++) {
                double ans = 0;
                for (int j = 0; j < weights.size(); j++) {
                    ans += weights.get(j).get(i) * in.get(j);
                }
                ans += biases.get(i);
                result.add(ans);
            }
            return result;
        }
    }

}
