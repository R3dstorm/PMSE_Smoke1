#! /usr/bin/python

import numpy as np

input_file = "input.csv"
output_file = "output.csv"

content = np.loadtxt(input_file, delimiter=" ")

data = content[:,1:7]
labels = content[:,0]

sliding_seconds = 1
window_seconds = 20
data_frequency = 50
feature_count  = 24 # 6 x mean, 6 x std, 6 x min, 6 x max

sliding_size = sliding_seconds * data_frequency
window_size = window_seconds * data_frequency

if data.shape[0] < window_size:
 sys.exit("data too short")
if data.shape[1] != 6:
 sys.exit("invalid data")

mean = np.zeros(6)
std = np.zeros(6)
amin = np.zeros(6)
amax = np.zeros(6)
size = data.shape[0] // sliding_size
features = np.zeros([size, feature_count + 1])
new_labels = np.zeros([size])
offset = 0
row = 0

while offset + window_size <= data.shape[0]:
  new_label = 0
  if np.count_nonzero(labels[offset:offset + window_size]) == window_size:
    new_label = 1

  for column in range(0, 6):
    mean[column] = np.mean(data[offset:offset + window_size,column])
    std[column] = np.std(data[offset:offset + window_size,column])
    amin[column] = np.min(data[offset:offset + window_size,column])
    amax[column] = np.max(data[offset:offset + window_size,column])

  features[row] = [new_label,
                   mean[0], mean[1], mean[2], mean[3], mean[4], mean[5],
                   std[0], std[1], std[2], std[3], std[4], std[5],
                   amin[0], amin[1], amin[2], amin[3], amin[4], amin[5],
                   amax[0], amax[1], amax[2], amax[3], amax[4], amax[5]]

  offset += sliding_size
  row += 1

np.savetxt(output_file, features[0:row,:], delimiter=" ",
           fmt=' '.join(['%i'] + ['%5f'] * feature_count))
