# Football-Heading-System
Detecting the forces involved in the sub-concussive heading of a ball in the game of soccer. This involves implementing real-world detectors on head and ball to measure and feed information back, with Bluetooth, from two ‘Arduino’ microprocessors to an app created using ‘Android Studios’. This app analyses and displays the information and the protocol of the outcome. Over 180 training case headers were recorded and labelled to categories based on their severity. An LSTM time-series Neural Network was created in Python using TensorFlow, which achieved an accuracy of over 70%. This could be improved greatly with more data but time-restrictions prevented this from being possible. The results of new headers are used as test data and return a classification.

This project is divided into three sections:
1) Ardunio Microcontrollers (C) - Record the data from the FSRs and Accelerometers and transmit the recorded data via Bluetooth to a Mobile App. It implements Start/Stop functionality controlled by the Android App. When Start button is pressed the readings from the sensors are recorded through analog pins on the devices. When the Stop button is pressed the Microcontrollers end stop recording the data and send it via Bluetooth to the Android App which they are connected to. 

![Circuitary System](https://github.com/ronanmmurphy/Football-Heading-System/blob/main/Images/circuit_system.png?raw=true)


2) Android Studios Mobile Application (JAVA) - Connects via Bluetooth to two peripheral Microcontrollers implementing START/STOP buttons to control recordings and transmission. Once  connected to the two microprocessors the Mobile app can control the other Buttons. When Start is pressed a signal is sent over Bluetooth to both devices, when Stop is pressed a different signal is sent which then receives the recorded information in packets. Once this data has been gathered and saved to a CSV locally the ANALYSE button can be pressed which inspected the Graphs of each recorded sensor. 

![App Interface](https://github.com/ronanmmurphy/Football-Heading-System/blob/main/Images/app_interface.png?raw=true)

![Graph Activity](https://github.com/ronanmmurphy/Football-Heading-System/blob/main/Images/graph_activity_2.png?raw=true)

3) Machine Learning Algorithm (Python) - An RNN with LSTM layer was trained from 180 recorded heading events each individually labelled. This model was saved and could predict new recorded headers to a 70% accuracy. 

![RNN Learning Curve](https://github.com/ronanmmurphy/Football-Heading-System/blob/main/Images/learning_curve_RNN.PNG?raw=true)


![Dataflow Diagram](https://github.com/ronanmmurphy/Football-Heading-System/blob/main/Images/dataflow_diagram.png?raw=true)
