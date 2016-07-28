package game.Object;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tatsuya Oba
 */
public class NeuarlNetwork {
    private final Layer[] layers;


    private NeuarlNetwork(final Layer[] layers) {
        this.layers = layers;
    }

    public List<Double> calcu(final List<Double> in) {
        return softmax(calcu(in, 0));
    }

    private static List<Double> softmax(final List<Double> in) {
        final List<Double> expList = in.stream().map(Math::exp).collect(Collectors.toList());
        final Double sum = expList.stream().mapToDouble(a -> a).sum();
        return expList.stream()
                .map(a -> a / sum)
                .collect(Collectors.toList());
    }

    private List<Double> calcu(final List<Double> in, final int layerIndex) {
        if (layers.length == layerIndex) return in;
        return calcu(layers[layerIndex].calcu(in, layerIndex == (layers.length - 1)), layerIndex + 1);
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

        private List<Double> calcu(final List<Double> in, final boolean last) {
            final List<Double> result = new ArrayList<>();
            for (int i = 0; i < weights.get(0).size(); i++) {
                double ans = 0;
                for (int j = 0; j < weights.size(); j++) {
                    ans += weights.get(j).get(i) * in.get(j);
                }
                ans += biases.get(i);
                if (!last)
                    result.add(Math.max(ans, 0f));
                else
                    result.add(ans);
            }
            return result;
        }
    }
}
