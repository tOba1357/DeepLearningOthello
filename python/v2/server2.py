import sys

import tensorflow as tf

sys.path.append('../gen-py')

from learning_server import LearningServer
from learning_server.ttypes import *

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

flags = tf.flags

flags.DEFINE_string("summaries_dir", None, "summaries_dir")
flags.DEFINE_integer("port", 9090, "thrift port number")
flags.DEFINE_string("data_dir", "data", "save parameters directory name")
FLAGS = flags.FLAGS


def variable_summaries(var, name):
    with tf.name_scope('summaries'):
        mean = tf.reduce_mean(var)
        tf.scalar_summary('mean/' + name, mean)
        with tf.name_scope('stddev'):
            stddev = tf.sqrt(tf.reduce_mean(tf.square(var - mean)))
        tf.scalar_summary('stddev/' + name, stddev)
        tf.scalar_summary('max/' + name, tf.reduce_max(var))
        tf.scalar_summary('min/' + name, tf.reduce_min(var))
        tf.histogram_summary(name, var)


def get_layer(input_tensor, input_dim, output_dim, layer_name, activating_function=tf.nn.relu):
    with tf.name_scope(layer_name):
        with tf.name_scope('weight'):
            weight = tf.get_variable('weight', [input_dim, output_dim], tf.float32)
            variable_summaries(weight, layer_name + "/weight")
        with tf.name_scope('bias'):
            bias = tf.get_variable('bias', [output_dim], tf.float32)
            variable_summaries(bias, layer_name + "/bias")
        layer = activating_function(tf.matmul(input_tensor, weight) + bias)
        return layer, weight, bias


class DefaultConfig(object):
    num_layers = 5
    learning_rate = 0.00025
    momentum = 0.95
    epsilon = 0.01


class OthelloModel:
    def __init__(self, config=DefaultConfig):
        self._input_data = input_data = tf.placeholder(tf.float32, [None, 128])
        self._targets = targets = tf.placeholder(tf.float32, [None, 3])

        self._weights = weights = []
        self._biases = biases = []
        self._output = output = input_data
        for i in range(config.num_layers):
            layer_name = 'layer' + str(i)
            with tf.variable_scope(layer_name):
                if (i + 1) == config.num_layers:
                    output, weight, bias = get_layer(output, 128, 3, layer_name, activating_function=tf.nn.softmax)
                else:
                    output, weight, bias = get_layer(output, 128, 128, layer_name)
                weights.append(weight)
                biases.append(bias)

        error = tf.abs(targets - output)
        quadratic_part = tf.clip_by_value(error, 0.0, 1.0)
        linear_part = error - quadratic_part
        loss = tf.reduce_mean(0.5 * tf.square(quadratic_part) + linear_part)

        tf.scalar_summary('loss', loss)
        optimizer = tf.train.RMSPropOptimizer(config.learning_rate, momentum=config.momentum, epsilon=config.epsilon)
        self._train_optimizer = optimizer.minimize(loss)

    @property
    def input_data(self):
        return self._input_data

    @property
    def targets(self):
        return self._targets

    @property
    def weights(self):
        return self._weights

    @property
    def biases(self):
        return self._biases

    @property
    def output(self):
        return self._output

    @property
    def train_optimizer(self):
        return self._train_optimizer


class CalculatorHandler:
    def __init__(self):
        initializer = tf.random_uniform_initializer(minval=-0.2, maxval=0.2)
        with tf.variable_scope('model', initializer=initializer):
            self.model = model = OthelloModel()
        self.saver = tf.train.Saver()
        self.session = session = tf.Session()
        session.run(tf.initialize_all_variables())

        self.merged = tf.merge_all_summaries()
        if not tf.gfile.Exists(FLAGS.summaries_dir):
            tf.gfile.MakeDirs(FLAGS.summaries_dir)
        self.train_writer = tf.train.SummaryWriter(FLAGS.summaries_dir, session.graph)

        self.counter = 0

    def get(self, board):
        model = self.model
        feed_dict = {model.input_data: board}
        return self.session.run(model.output, feed_dict=feed_dict)

    def learning(self, result, board):
        model = self.model
        fetches = [model.train_optimizer, self.merged]
        feed_dict = {model.input_data: board, model.targets: result}
        _, summary = self.session.run(fetches, feed_dict=feed_dict)
        self.train_writer.add_summary(summary, self.counter)
        self.counter += 1

    def save(self, file_name):
        path = FLAGS.data_dir + "/" + file_name
        self.saver.save(self.session, path)
        print "saved to " + path

    def load(self, file_name):
        path = FLAGS.data_dir + "/" + file_name
        self.saver.restore(self.session, path)
        print "load from " + path

    def getWeight(self):
        return self.session.run(self.model.weights)

    def getBiase(self):
        return self.session.run(self.model.biases)


if not FLAGS.summaries_dir:
    raise ValueError("Must set --summaries_dir")

if not FLAGS.port:
    raise ValueError("Must set --port")

handler = CalculatorHandler()
processor = LearningServer.Processor(handler)
transport = TSocket.TServerSocket(port=FLAGS.port)
tfactory = TTransport.TBufferedTransportFactory()
pfactory = TBinaryProtocol.TBinaryProtocolFactory()

server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)

print 'server port:' + str(FLAGS.port)
server.serve()
print 'done.'
