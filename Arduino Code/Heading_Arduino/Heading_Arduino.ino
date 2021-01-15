#include <CurieBLE.h>
#include "CurieIMU.h"


// set analog pins
#define fsrLEFT A0
#define fsrMIDDLE A1
#define fsrRIGHT A2
#define xpin A3 // x-axis of the accelerometer
#define ypin A4 // y-axis
#define zpin A5 // z-axis


#define SAMPLE_TIME 40

int ax, ay, az;


unsigned long timestamp;


// BLE objects
BLEPeripheral blePeripheral;


// GAP properties
char device_name[] = "HEAD_ARD";

BLEService HEAD_ARD_SERVICE("19B10000-E8F2-537E-4F6C-D104768A1214");

BLEIntCharacteristic RUN("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLECharacteristic TRANSFER("19b10002-e8f2-537e-4f6c-d104768a1214", BLERead | BLENotify, 20);


inline int FSR_CAL(int fsr_pin) {
  return map(analogRead(fsr_pin), 0, 1023, 0, 3300);
}

inline int ACC_CAL(int acc_pin) {
  return map(analogRead(acc_pin), 0, 1023, 0, 3300);
}


void setup() {
  Serial.begin(9600);

  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(fsrLEFT, INPUT);
  pinMode(fsrMIDDLE, INPUT);
  pinMode(fsrRIGHT, INPUT);
  pinMode(xpin, INPUT);
  pinMode(ypin, INPUT);
  pinMode(zpin, INPUT);
  
  blePeripheral.setLocalName(device_name);
  blePeripheral.setDeviceName(device_name);
  blePeripheral.setAdvertisedServiceUuid(HEAD_ARD_SERVICE.uuid());

  blePeripheral.addAttribute(HEAD_ARD_SERVICE);
  blePeripheral.addAttribute(RUN);
  blePeripheral.addAttribute(TRANSFER);


  // Set the initial value for the characeristic
  RUN.setValue(0);
  
  blePeripheral.begin();
  
  CurieIMU.begin();
  CurieIMU.setAccelerometerRange(2);

  timestamp = millis();
}

void loop() {
  BLECentral central = blePeripheral.central();
  
  if (central) { 
    Serial.print("Connected to central: ");
    Serial.println(central.address());
    
    while (central.connected()) {
      
      if (RUN.written()) {
        if (RUN.value() == 1) {
          Serial.println("START LOGGING");
          digitalWrite(LED_BUILTIN, HIGH);

          unsigned char data_array[20] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};  // Array for storing all data
          int fsr_data;
          int acc1_data;
          unsigned long timestamp_start = millis();

          while(true) {
            if ((millis() - timestamp) >= SAMPLE_TIME) {
              timestamp = millis();
              CurieIMU.readAccelerometer(ax, ay, az);
              Serial.println(ax);
              Serial.println(ay);
              Serial.println(az);
              data_array[1] = (int)(timestamp - timestamp_start) & 0xFF;
              data_array[0] = ((int)(timestamp - timestamp_start) >> 8) & 0xFF;


              fsr_data = FSR_CAL(fsrLEFT);
              data_array[3] = fsr_data & 0xFF;
              data_array[2] = (fsr_data >> 8) & 0xFF;

              fsr_data = FSR_CAL(fsrMIDDLE);
              data_array[5] = fsr_data & 0xFF;
              data_array[4] = (fsr_data >> 8) & 0xFF;
              
              fsr_data = FSR_CAL(fsrRIGHT);
              data_array[7] = fsr_data & 0xFF;
              data_array[6] = (fsr_data >> 8) & 0xFF;


              acc1_data = ACC_CAL(xpin);
              data_array[9] = acc1_data & 0xFF;
              data_array[8] = (acc1_data >> 8) & 0xFF;

              acc1_data = ACC_CAL(ypin);
              data_array[11] = acc1_data & 0xFF;
              data_array[10] = (acc1_data >> 8) & 0xFF;

              acc1_data = ACC_CAL(zpin);
              data_array[13] = acc1_data & 0xFF;
              data_array[12] = (acc1_data >> 8) & 0xFF;
            
              
              data_array[15] = ax & 0xFF;
              data_array[14] = (ax >> 8) & 0xFF;

              
              data_array[17] = ay & 0xFF;
              data_array[16] = (ay >> 8) & 0xFF;

              
              data_array[19] = az & 0xFF;
              data_array[18] = (az >> 8) & 0xFF;

              TRANSFER.setValue(data_array, 20);
            }

            // Check if we need to stop
            if (RUN.written()) {
              if (RUN.value() == 0) {
                Serial.println("STOP LOGGING");
                digitalWrite(LED_BUILTIN, LOW); 
                break;
              }
            }
          }
        }
      }
      // when the central disconnects, print it out:
  //    Serial.print(F("Disconnected from central: "));
 //     Serial.println(central.address());
    }
  }
}
