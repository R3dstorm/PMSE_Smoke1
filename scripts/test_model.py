#! /usr/bin/python

import pandas
import numpy as np
import tensorflow as tf

from tensorflow.python.platform import gfile

np.set_printoptions(precision=2, suppress=True, threshold=np.nan)

feature_count  = 24

dataframe = pandas.read_csv('validation.csv', header=None, engine = 'python', sep='\s+|\t+|,')

labels = dataframe.iloc[:,0].values
dataset = dataframe.iloc[:,1:feature_count + 1].values

start = 0
length = dataset.shape[0]

f = tf.gfile.GFile("model.pb", 'rb')
graph_def = tf.GraphDef()

graph_def.ParseFromString(f.read())
f.close()

with tf.Session() as sess:
    sess.graph.as_default()

    tf.import_graph_def(graph_def)

    tensor_input = sess.graph.get_tensor_by_name('import/dense_1_input:0')
    tensor_output = sess.graph.get_tensor_by_name('import/dense_3/Sigmoid:0')
    predictions = sess.run(tf.cast(tf.round(tensor_output), tf.int64),
                           feed_dict={tensor_input: dataset[start:start + length]})

    actual = labels[start:start + length,np.newaxis]

    TP = np.count_nonzero(predictions * actual)
    TN = np.count_nonzero((predictions - 1) * (actual - 1))
    FP = np.count_nonzero(predictions * (actual - 1))
    FN = np.count_nonzero((predictions - 1) * actual)
    print "tp:", TP, " tn:", TN, " fp:", FP, " fn:", FN

    if TP + FP != 0:
        precision = np.divide(np.float(TP), TP + FP) * 100
        print "precision: {0:.2f} %".format(precision)
    if TP + FN != 0:
        recall = np.divide(np.float(TP), TP + FN) * 100
        print "recall   : {0:.2f} %".format(recall)
    if TP + FN + TN + FP != 0:
        accuracy = np.divide(np.float(TP) + TN, TP + FN + TN + FP) * 100
        print "accuracy : {0:.2f} %".format(accuracy)
