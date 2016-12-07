package game.Object;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Tatsuya Oba
 */
public class NeuralNetwork {
    private final List<Layer> layerList;


    private NeuralNetwork(final List<Layer> layerList) {
        this.layerList = layerList;
    }

    public List<Double> forward(final List<Double> in) {
        return layerList.stream()
                .reduce(
                        in,
                        (input, layer) -> layer.forward(input),
                        (input, output) -> output
                );
    }

    private static List<Double> softmax(final List<Double> in) {
        final List<Double> expList = in.stream().map(Math::exp).collect(Collectors.toList());
        final Double sum = expList.stream().mapToDouble(a -> a).sum();
        return expList.stream()
                .map(a -> a / sum)
                .collect(Collectors.toList());
    }

    public static NeuralNetwork create(
            final List<List<List<Double>>> weights,
            final List<List<Double>> biases
    ) {
        final List<Layer> layerList = new ArrayList<>();
        for (int i = 0; i < weights.size() - 1; i++) {
            layerList.add(new Layer(
                    weights.get(i),
                    biases.get(i),
                    nums -> nums.stream().map(num -> Math.max(0, num)).collect(Collectors.toList())
            ));
        }
        layerList.add(new Layer(
                weights.get(weights.size() - 1),
                biases.get(weights.size() - 1),
                NeuralNetwork::softmax
        ));
        return new NeuralNetwork(layerList);
    }

    public static NeuralNetwork createTanh(
            final List<List<List<Double>>> weights,
            final List<List<Double>> biases
    ) {
        final List<Layer> layerList = new ArrayList<>();
        for (int i = 0; i < weights.size() - 1; i++) {
            layerList.add(new Layer(
                    weights.get(i),
                    biases.get(i),
                    nums -> nums.stream().map(num -> Math.max(0, num)).collect(Collectors.toList())
            ));
        }
        layerList.add(new Layer(
                weights.get(weights.size() - 1),
                biases.get(weights.size() - 1),
                nums -> nums.stream().map(Math::tanh).collect(Collectors.toList())
        ));
        return new NeuralNetwork(layerList);
    }

    private static class Layer {
        final List<List<Double>> weights;
        final List<Double> biases;
        final Function<List<Double>, List<Double>> activationFunction;

        private Layer(
                final List<List<Double>> weights,
                final List<Double> biases,
                final Function<List<Double>, List<Double>> activationFunction
        ) {
            this.weights = weights;
            this.biases = biases;
            this.activationFunction = activationFunction;
        }

        private List<Double> forward(final List<Double> in) {
            final List<Double> result = new ArrayList<>();
            for (int i = 0; i < weights.get(0).size(); i++) {
                double ans = 0;
                for (int j = 0; j < weights.size(); j++) {
                    ans += weights.get(j).get(i) * in.get(j);
                }
                ans += biases.get(i);
                result.add(ans);
            }
            return activationFunction.apply(result);
        }
    }
}
