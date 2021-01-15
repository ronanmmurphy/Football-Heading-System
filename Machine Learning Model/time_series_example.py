import pandas as pd
import numpy as np
import sys

np.set_printoptions(threshold=sys.maxsize)
import csv
import matplotlib
matplotlib.use('agg')
import matplotlib.pyplot as plt
from os import listdir
from keras.preprocessing import sequence
import tensorflow as tf
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense
from tensorflow.keras.layers import LSTM
from tensorflow.keras.layers import Flatten
from tensorflow.keras import losses
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.models import load_model
from tensorflow.keras.callbacks import ModelCheckpoint

path = 'FYPData/HeadData'
sequences = list()
for i in range(1,180):
	file_path = path + str(i)+ '.csv'
	df = pd.read_csv(file_path, header=0)
	#only gets the columns we are interested in
	df1 = df[['fsrLEFT','fsrMIDDLE','fsrRIGHT','xpin','ypin','zpin','ax','ay','az','bx','by','bz']]
	values = df1.values
	sequences.append(values)

targets = pd.read_csv('FYPData/HeadData_target.csv')

#gets only the second columns for the targets
targets= targets.values[:,1]

#transforms the strings ('Good', 'Bad', etc) into numbers
#unique holds the numbers used for each string label
targets,unique = pd.factorize(targets)

#creates a dictionary with unique, so we can go back from numbers to labels (will be used on load_model.py)
idx_to_label = {k:v for k,v in enumerate(unique)}
#saves the dict to a file
np.save('idx_dict.npy', idx_to_label)

#transforms the single digit numbers into one-hot vectors
z = np.zeros((len(targets),len(set(targets))))
z[np.arange(len(targets)), targets] = 1

#uses the one-hot vectors as targets
targets = z

split_ratio = 0.8
trainingval = sequences[0:int(np.ceil(len(sequences)*split_ratio))]

test = sequences[int(np.ceil(len(sequences)*split_ratio)):]
train = trainingval[0:int(np.ceil(len(trainingval)*split_ratio))]
validation = trainingval[int(np.ceil(len(trainingval)*split_ratio)):]

"""split the target training, test and validate data ratio train:test 80:20, train:validate 80:20"""
trainingval_target = targets[0:int(np.ceil(len(targets)*split_ratio))]
test_target = targets[int(np.ceil(len(targets)*split_ratio)):len(targets)]
train_target = trainingval_target[0:int(np.ceil(len(trainingval_target)*split_ratio))]
validation_target = trainingval_target[int(np.ceil(len(trainingval_target)*split_ratio)):len(trainingval_target)]

#transforms a list into an np.array
train = np.stack(train)
validation = np.array(validation)
test = np.array(test)

train_target = np.array(train_target, dtype=np.float64)
validation_target = np.array(validation_target)
test_target = np.array(test_target)

"""create LSTM model length of each frame"""

#one suggestion is to determine the size the layers same as the input, instead of hard-coded
model = Sequential()
model.add(LSTM(256, input_shape=(31, 12)))
model.add(Flatten())
model.add(Dense(4, activation='sigmoid'))
model.summary()

adam = Adam(lr=0.001)
chk = ModelCheckpoint('best_model.pkl', monitor='val_acc', save_best_only=True, mode='max', verbose=1)
model.compile(loss='categorical_crossentropy', optimizer=adam, metrics=['accuracy'])
model.fit(train, train_target, epochs=200, batch_size=128, callbacks=[chk], validation_data=(validation,validation_target))

#loading the model and checking accuracy on the test data
model = load_model('best_model.pkl')

from sklearn.metrics import accuracy_score
#makes predicitons for the test
test_preds = model.predict_classes(test)
#transforms the test_target from one-hot to single integer labels
test_target = [np.argmax(e) for e in test_target]
#prints out the accuracy based on the right values and what the model predicted
print("Test Accuracy: ",accuracy_score(test_target, test_preds))

