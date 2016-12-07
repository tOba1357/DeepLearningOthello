import tensorflow as tf


class Config:
    input_size = 64 * 3 + 2  # board + turn
    label_size = 64
    hidden_layer_size_list = [200, 200, 200]
    keep_prob = 0.9
    initial_learning_rate = 0.2
    decay_steps = 1000
    decay_rate = 0.97
    clip_norm = 4


def dtype():
    return tf.float32


def create_layer(layer_name, tensor, input_dim, output_dim, activation_function, keep_prob=None):
    with tf.name_scope(layer_name):
        weight = tf.get_variable("weight", [input_dim, output_dim], dtype())
        bias = tf.get_variable("bias", [output_dim], dtype())
        if activation_function is None:
            layer = tf.matmul(tensor, weight) + bias
        else:
            layer = activation_function(tf.matmul(tensor, weight) + bias)
        if keep_prob is not None:
            layer = tf.nn.dropout(layer, keep_prob)
        return layer, weight, bias


class Model:
    def __init__(self, is_train, config=Config):
        self._input_data = input_data = tf.placeholder(dtype(), [None, config.input_size], "input_data")
        self._labels = labels = tf.placeholder(dtype(), [None, config.label_size], "labels")

        keep_prob = config.keep_prob
        if not is_train:
            keep_prob = None
        self._weights = weights = []
        self._biases = biases = []
        logits = input_data
        logits_dim = config.input_size
        for i, layer_size in enumerate(config.hidden_layer_size_list):
            layer_name = "layer-%d" % i
            with tf.variable_scope(layer_name):
                logits, weight, bias = create_layer(layer_name, logits, logits_dim, layer_size,
                                                    tf.nn.relu, keep_prob)
                weights.append(weight)
                biases.append(bias)
                logits_dim = layer_size
        with tf.variable_scope("layer-softmax"):
            logits, weight, bias = create_layer("layer-softmax", logits, logits_dim, config.label_size, tf.nn.tanh)
        weights.append(weight)
        biases.append(bias)
        self._logits = logits
        if is_train:
            self._loss = loss = tf.reduce_mean(tf.reduce_max(tf.square(labels - logits), 1))
            self._global_step = global_step = tf.Variable(0, trainable=False, name='global_step')
            if config.decay_rate and config.decay_steps:
                self._learning_rate = learning_rate = tf.train.exponential_decay(
                    config.initial_learning_rate, global_step, config.decay_steps,
                    config.decay_rate, staircase=True, name='learning_rate')
            else:
                self._learning_rate = learning_rate = config.initial_learning_rate
            params = tf.trainable_variables()
            train_opt = tf.train.AdamOptimizer(learning_rate)
            gradients = tf.gradients(loss, params)
            clipped_gradients, _ = tf.clip_by_global_norm(gradients, config.clip_norm)
            self._gradients = clipped_gradients
            self._train_optimizer = train_opt.apply_gradients(zip(clipped_gradients, params), global_step)
            self._summaries = tf.merge_summary([
                tf.scalar_summary('loss', loss),
                tf.scalar_summary('learning_rate', learning_rate)
            ])

    @property
    def input_data(self):
        return self._input_data

    @property
    def labels(self):
        return self._labels

    @property
    def logits(self):
        return self._logits

    @property
    def loss(self):
        return self._loss

    @property
    def global_step(self):
        return self._global_step

    @property
    def learning_rate(self):
        return self._learning_rate

    @property
    def train_optimizer(self):
        return self._train_optimizer

    @property
    def summaries(self):
        return self._summaries

    @property
    def weights(self):
        return self._weights

    @property
    def biases(self):
        return self._biases

    @property
    def gradients(self):
        return self._gradients