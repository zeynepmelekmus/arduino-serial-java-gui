#ifndef DEVICE_H
#define DEVICE_H

#include <Arduino.h>

enum class DeviceType {
  LED,
  MOTOR,
  SENSOR,
  UNKNOWN
};

class Device {
private:
  String name;
  DeviceType type;
  int pin;
  bool state;
  int sensorValue;

public:
  Device();
  Device(String _name, DeviceType _type, int _pin);

  String getName() const;
  DeviceType getType() const;
  int getPin() const;
  bool getState() const;
  int getSensorValue();

  void turnOn();
  void turnOff();
  void updateSensorValue();
};

DeviceType stringToDeviceType(const String& typeStr);
String deviceTypeToString(DeviceType type);
int parsePin(const String& pinStr);

#endif
