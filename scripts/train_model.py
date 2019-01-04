#! /usr/bin/python

from keras.models import Sequential
from keras.layers import Dense

import numpy

numpy.random.seed(7)

dataset = numpy.loadtxt("training.csv", delimiter=" ")

feature_count  = 12

X = dataset[:,1:feature_count + 1]
Y = dataset[:,0]

model = Sequential()
model.add(Dense(640, input_dim=feature_count, activation='relu'))
model.add(Dense(320, activation='relu'))
model.add(Dense(160, activation='relu'))
model.add(Dense(40, activation='relu'))
model.add(Dense(10, activation='relu'))
model.add(Dense(1, activation='sigmoid'))

model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])

model.fit(X, Y, epochs=100, batch_size=32)

scores = model.evaluate(X, Y)
print("\n%s: %.2f%%" % (model.metrics_names[1], scores[1] * 100))

model.save("model.h5")
