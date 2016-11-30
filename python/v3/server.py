import tensorflow as tf

from learning_server_v3 import LearningServer

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

from Model import Model
from common.RandomAccessQueue import RandomAccessQueue

flags = tf.flags

flags.DEFINE_string("logdir", None, "logdir")
flags.DEFINE_integer("port", 9090, "thrift port number")
FLAGS = flags.FLAGS


class Server:
    def __init__(self, session, logdir):
        initializer = tf.random_uniform_initializer(minval=0, maxval=0.2)
        with tf.variable_scope("model", initializer=initializer):
            self.model = Model(True)
        self.session = session
        self.saver = tf.train.Saver()
        session.run(tf.initialize_all_variables())

        if not tf.gfile.Exists(logdir):
            tf.gfile.MakeDirs(logdir)
        self.summary_writer = tf.train.SummaryWriter(logdir, session.graph)
        self.data_queue = RandomAccessQueue(10000)

    def train(self, data):
        model = self.model
        self.data_queue.add(data)
        fetches = [model.train_optimizer, model.summaries, model.global_step]
        for i in range(10):
            train_data = self.data_queue.get(100)
            feed_dict = {model.input_data: train_data[0], model.labels: train_data[1]}
            _, summaries, global_step = session.run(fetches, feed_dict)
        self.summary_writer.add_summary(summaries, global_step)

    def getWeight(self):
        return self.session.run(self.model.weights)

    def getBiase(self):
        return self.session.run(self.model.biases)


if not FLAGS.logdir:
    raise ValueError("Must set --logdir=\"log/dir/path\"")

if not FLAGS.port:
    raise ValueError("Must set --port=xxxx")

with tf.Session() as session:
    server = Server(session, FLAGS.logdir)
    processor = LearningServer.Processor(server)
    transport = TSocket.TServerSocket(port=FLAGS.port)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)

    print 'server port:' + str(FLAGS.port)
    server.serve()
    print 'done.'
