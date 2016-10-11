import sys
import tensorflow as tf
import json
import os.path

board_size = 128
out_put_size = 3
file_name = "data.ckpt"
# setup variables
# board size = 8 * 8 * 2 = 128
# out puts are win, lose, or draw
x = tf.placeholder(tf.float32, [None, board_size])
W = tf.Variable(tf.zeros([board_size, out_put_size]))
b = tf.Variable(tf.zeros([out_put_size]))
y = tf.nn.softmax(tf.matmul(x, W) + b)

# set up training
# use to training with cross-entropy
# y'
# -sigma(y' * log(y))
y_ = tf.placeholder(tf.float32, [None, out_put_size])
cross_entropy = tf.reduce_mean(-tf.reduce_sum(y_ * tf.log(y), reduction_indices=[1]))
train_step = tf.train.GradientDescentOptimizer(0.5).minimize(cross_entropy)

# run
saver = tf.train.Saver()
init = tf.initialize_all_variables()
session = tf.Session()
session.run(init)
saver.restore(session, file_name)

print session.run(W)
print session.run(b)

