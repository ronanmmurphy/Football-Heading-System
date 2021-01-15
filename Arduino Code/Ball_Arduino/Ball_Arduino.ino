#include <CurieBLE.h>
#include "CurieIMU.h"




#define SAMPLE_TIME 40

int bx, by, bz;


unsigned long timestamp;


// BLE objects
BLEPeripheral blePeripheral;


// GAP properties
char device_name[] = "BALL_ARD";

BLEService HEAD_ARD_SERVICE("19B10000-E8F2-537E-4F6C-D104768A1214");

BLEIntCharacteristic RUN("19B10001-E8F2-537E-4F6C-D104768A1214", BLERead | BLEWrite);
BLECharacteristic TRANSFER("19b10002-e8f2-537e-4f6c-d104768a1214", BLERead | BLENotify, 20);




void setup() {
  Serial.begin(9600);

  pinMode(LED_BUILTIN, OUTPUT);
  
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

          unsigned char data_arrby[8] = {0,0,0,0,0,0,0,0};  // Arrby for storing all data
          
          unsigned long timestamp_start = millis();

          while(true) {
            if ((millis() - timestamp) >= SAMPLE_TIME) {
              timestamp = millis();
              CurieIMU.readAccelerometer(bx, by, bz);
              data_arrby[1] = (int)(timestamp - timestamp_start) & 0xFF;
              data_arrby[0] = ((int)(timestamp - timestamp_start) >> 8) & 0xFF;


              
            
              
              data_arrby[3] = bx & 0xFF;
              data_arrby[2] = (bx >> 8) & 0xFF;

              
              data_arrby[5] = by & 0xFF;
              data_arrby[4] = (by >> 8) & 0xFF;

              
              data_arrby[7] = bz & 0xFF;
              data_arrby[6] = (bz >> 8) & 0xFF;

              TRANSFER.setValue(data_arrby, 8);
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
