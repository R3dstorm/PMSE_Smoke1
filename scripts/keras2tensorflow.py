#! /usr/bin/python

from keras.models import load_model
from keras import backend as K
import tensorflow as tf

def freeze_session(session, keep_var_names=None, output_names=None, clear_devices=True):
    from tensorflow.python.framework.graph_util import convert_variables_to_constants
    graph = session.graph
    with graph.as_default():
        freeze_var_names = list(set(v.op.name for v in tf.global_variables()).difference(keep_var_names or []))
        output_names = output_names or []
        output_names += [v.op.name for v in tf.global_variables()]
        input_graph_def = graph.as_graph_def()
        if clear_devices:
            for node in input_graph_def.node:
                node.device = ""
        frozen_graph = convert_variables_to_constants(session, input_graph_def, output_names, freeze_var_names)
    return frozen_graph

model = load_model('model.h5')

print(model.inputs)
print(model.outputs)

frozen_graph = freeze_session(K.get_session(), output_names=[out.op.name for out in model.outputs])

tf.train.write_graph(frozen_graph, "", "model.pb", as_text=False)
