import pandas as pd
import numpy as np
import sys, os

np.set_printoptions(threshold=sys.maxsize)
import matplotlib
from tensorflow.keras.models import load_model
from keras.preprocessing import sequence
import tensorflow as tf

#gets the path of this file
path = os.path.abspath(__file__+"/..")
print(path)

#loads the new data into df
df = pd.read_csv(path+"/CSV_new_data.csv", header=0)

#selects only the columns we want
df1 = df[['fsrLEFT','fsrMIDDLE','fsrRIGHT','xpin','ypin','zpin','ax','ay','az','bx','by','bz']]
values = df1.values

#creates an np.array from a list and reshapes the 2d array into to 3d array 
new_data = np.stack(values).reshape(1,31,12)

#loads the model from the pickle file (saved by the time_series_example.py)
model = load_model('best_model.pkl')

#loads the dictionary used to transform the target indexes to target labels. Ex: 'Good' represented as 2 
idx_to_label = np.load('idx_dict.npy').item()

#makes the model predict the class given the input
test_preds = model.predict_classes(new_data)

#prints the label of the class predicted
print([idx_to_label[e] for e in test_preds])
