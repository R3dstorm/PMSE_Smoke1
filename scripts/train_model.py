#! /usr/bin/python

from keras.models import Sequential
from keras.layers import Dense

import numpy

numpy.random.seed(7)

dataset = numpy.loadtxt("training.csv", delimiter=" ")

feature_count  = 24

X = dataset[:,1:feature_count + 1]
Y = dataset[:,0]

model = Sequential()
model.add(Dense(96, input_dim=feature_count, activation='relu'))
model.add(Dense(24, activation='relu'))
model.add(Dense(1, activation='sigmoid'))

model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])

model.fit(X, Y, epochs=20, batch_size=64)

scores = model.evaluate(X, Y)
print("\n%s: %.2f%%" % (model.metrics_names[1], scores[1] * 100))

model.save("model.h5")
