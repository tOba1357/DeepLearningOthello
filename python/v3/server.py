import tensorflow as tf
import sys
sys.path.append("../common")
from learning_server_v3 import LearningServer

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer

from Model import Model
from RandomAccessQueue import RandomAccessQueue

flags = tf.flags

flags.DEFINE_string("logdir", None, "logdir")
flags.DEFINE_integer("port", 9090, "thrift port number")
FLAGS = flags.FLAGS


class Server:
    def __init__(self, session, logdir):
        initializer = tf.random_uniform_initializer(minval=-0.2, maxval=0.2)
        with tf.variable_scope("model", initializer=initializer):
            self.model = Model(True)
        self.session = session
        self.logdir = logdir
        self.saver = tf.train.Saver()
        session.run(tf.global_variables_initializer())

        if not tf.gfile.Exists(logdir):
            tf.gfile.MakeDirs(logdir)
        else:
            self.saver.restore(session, logdir + "/data")
            print "model load from " + logdir
        self.summary_writer = tf.train.SummaryWriter(logdir, session.graph)
        self.data_queue = RandomAccessQueue(10000)
        self.global_step = session.run(self.model.global_step)

    def train(self, data):
        model = self.model
        self.data_queue.add(data)
        for i in range(10):
            train_data = self.data_queue.get(100)
            inputs = [a[0] for a in train_data]
            labels = [a[1] for a in train_data]
            feed_dict = {model.input_data: inputs, model.labels: labels}
            if i == 9:
                _, summaries, self.global_step = session.run(
                    [model.train_optimizer, model.summaries, model.global_step], feed_dict)
                self.summary_writer.add_summary(summaries, self.global_step)
            else:
                _, self.global_step = session.run([model.train_optimizer, model.global_step], feed_dict)

    def getWeight(self):
        return self.session.run(self.model.weights)

    def getBiase(self):
        return self.session.run(self.model.biases)

    def save(self):
        self.saver.save(self.session, self.logdir + "/data")


if not FLAGS.logdir:
    raise ValueError("Must set --logdir=/log/dir/path")

if not FLAGS.port:
    raise ValueError("Must set --port=xxxx")

with tf.Session() as session:
    server_handler = Server(session, FLAGS.logdir)
    processor = LearningServer.Processor(server_handler)
    transport = TSocket.TServerSocket(port=FLAGS.port)
    tfactory = TTransport.TBufferedTransportFactory()
    pfactory = TBinaryProtocol.TBinaryProtocolFactory()

    server = TServer.TSimpleServer(processor, transport, tfactory, pfactory)

    print 'server port:' + str(FLAGS.port)
    server.serve()
    print 'done.'
