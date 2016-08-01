import sys
import tensorflow as tf
import json
import os.path
sys.path.append('./gen-py')

from learning_server import LearningServer
from learning_server.ttypes import *

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer


weights_list = []
biases_list = []
def weight_variable(shape):
    """Create a weight variable with appropriate initialization."""
    initial = tf.truncated_normal(shape, stddev=0.1)
    return tf.Variable(initial)

def bias_variable(shape):
    """Create a bias variable with appropriate initialization."""
    initial = tf.constant(0.1, shape=shape)
    return tf.Variable(initial)

def variable_summaries(var, name):
    """Attach a lot of summaries to a Tensor."""
    with tf.name_scope('summaries'):
        mean = tf.reduce_mean(var)
        tf.scalar_summary('mean/' + name, mean)
        with tf.name_scope('stddev'):
            stddev = tf.sqrt(tf.reduce_sum(tf.square(var - mean)))
        tf.scalar_summary('sttdev/' + name, stddev)
        tf.scalar_summary('max/' + name, tf.reduce_max(var))
        tf.scalar_summary('min/' + name, tf.reduce_min(var))
        tf.histogram_summary(name, var)

def nn_layer(input_tensor, input_dim, output_dim, layer_name, act=tf.nn.relu):
    """Reusable code for making a simple neural net layer.
    It does a matrix multiply, bias add, and then uses relu to nonlinearize.
    It also sets up name scoping so that the resultant graph is easy to read,
    and adds a number of summary ops.
    """
    # Adding a name scope ensures logical grouping of the layers in the graph.
    with tf.name_scope(layer_name):
        # This Variable will hold the state of the weights for the layer
        with tf.name_scope('weights'):
            weights = weight_variable([input_dim, output_dim])
            weights_list.append(weights)
            variable_summaries(weights, layer_name + '/weights')
        with tf.name_scope('biases'):
            biases = bias_variable([output_dim])
            biases_list.append(biases)
            variable_summaries(biases, layer_name + '/biases')
        with tf.name_scope('Wx_plus_b'):
            preactivate = tf.matmul(input_tensor, weights) + biases
            tf.histogram_summary(layer_name + '/pre_activations', preactivate)
        activations = act(preactivate, 'activation')
        tf.histogram_summary(layer_name + '/activations', activations)
        return activations


class CalculatorHandler:
    board_size = 128
    out_put_size = 3

    def __init__(self):
        self.log = {}
        with tf.name_scope('input'):
            self.x = tf.placeholder(tf.float32, [None, self.board_size], name='x-input')
            self.y_ = tf.placeholder(tf.float32, [None, self.out_put_size], name='y-input')

        self.layer1 = nn_layer(self.x, self.board_size, self.board_size, 'layer1')
        self.layer2 = nn_layer(self.layer1, self.board_size, self.board_size, 'layer2')
        self.layer3 = nn_layer(self.layer2, self.board_size, self.board_size, 'layer3')
        self.y = nn_layer(self.layer3, self.board_size, self.out_put_size, 'layer4', act=tf.nn.softmax)
        # self.y = nn_layer(self.x, self.board_size, self.out_put_size, 'layer2', act=tf.nn.softmax)

        with tf.name_scope('cross_entropy'):
            diff = self.y_ * tf.log(self.y)
            with tf.name_scope('total'):
                cross_entropy = -tf.reduce_mean(diff)
            tf.scalar_summary('cross entropy', cross_entropy)

        with tf.name_scope('train'):
            self.train_step = tf.train.AdamOptimizer(0.001).minimize(cross_entropy)

        # run
        self.saver = tf.train.Saver()
        self.session = tf.Session()
        init = tf.initialize_all_variables()
        self.merged = tf.merge_all_summaries()
        if not tf.gfile.Exists("./log"):
            tf.gfile.MakeDirs("./log")
        self.train_writer = tf.train.SummaryWriter("./log", self.session.graph)
        self.session.run(init)

        self.counter = 0

    def create_layer(self, input_num, output_num):
        return (tf.Variable(tf.zeros([input_num, output_num])), tf.Variable(tf.zeros([output_num])))

    def get(self, board):
        return self.session.run(self.y, feed_dict={self.x: board})

    def learning(self, result, board):
        run_options = tf.RunOptions(trace_level=tf.RunOptions.FULL_TRACE)
        run_metadata = tf.RunMetadata()
        summary, _ = self.session.run(
            [self.merged, self.train_step],
            feed_dict={self.x: board, self.y_: result},
            options=run_options,
            run_metadata=run_metadata
        )
        self.train_writer.add_run_metadata(run_metadata, 'step%d' % self.counter)
        self.train_writer.add_summary(summary, self.counter)
        self.counter += 1

    def save(self, file_name):
        self.saver.save(self.session, file_name)
        print "saved to " + file_name

    def load(self, file_name):
        self.saver.restore(self.session, file_name)
        print "load from " + file_name

    def getWeight(self):
        rtn = []
        for weights in weights_list:
            rtn.append(self.session.run(weights))
        return rtn

    def getBiase(self):
        rtn = []
        for biases in biases_list:
            rtn.append(self.session.run(biases))
        return rtn


argvs = sys.argv
if len(argvs) == 1:
    print "set port numer"
    quit()

handler = CalculatorHandler()
processor = LearningServer.Processor(handler)
port = int(argvs[1])
transport = TSocket.TServerSocket(port=port)
tfactory = TTransport.TBufferedTransportFactory()
pfactory = TBinaryProtocol.TBinaryProtocolFactory()

server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)

print 'Starting the server port' + str(port)
server.serve()
print 'done.'
