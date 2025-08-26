#include "Device.h"

Device::Device() : name(""), type(DeviceType::UNKNOWN), pin(-1), state(false), sensorValue(0) {}

Device::Device(String _name, DeviceType _type, int _pin) {
  name = _name;
  type = _type;
  pin = _pin;
  state = false;
  sensorValue = 0;

  if (type == DeviceType::LED || type == DeviceType::MOTOR) {
    pinMode(pin, OUTPUT);
    digitalWrite(pin, LOW);
  } else if (type == DeviceType::SENSOR) {
    pinMode(pin, INPUT);
  }
}

String Device::getName() const {
  return name;
}

DeviceType Device::getType() const {
  return type;
}

int Device::getPin() const {
  return pin;
}

bool Device::getState() const {
  return state;
}

int Device::getSensorValue() {
  if (type == DeviceType::SENSOR) {
    sensorValue = analogRead(pin);
  }
  return sensorValue;
}

void Device::turnOn() {
  if (type == DeviceType::LED || type == DeviceType::MOTOR) {
    digitalWrite(pin, HIGH);
    state = true;
  }
}

void Device::turnOff() {
  if (type == DeviceType::LED || type == DeviceType::MOTOR) {
    digitalWrite(pin, LOW);
    state = false;
  }
}

void Device::updateSensorValue() {
  if (type == DeviceType::SENSOR) {
    sensorValue = analogRead(pin);
  }
}

DeviceType stringToDeviceType(const String& typeStr) {
  String lower = typeStr;
  lower.toLowerCase();
  if (lower == "led") return DeviceType::LED;
  if (lower == "motor") return DeviceType::MOTOR;
  if (lower == "sensor") return DeviceType::SENSOR;
  return DeviceType::UNKNOWN;
}

String deviceTypeToString(DeviceType type) {
  switch(type) {
    case DeviceType::LED: return "LED";
    case DeviceType::MOTOR: return "MOTOR";
    case DeviceType::SENSOR: return "SENSOR";
    default: return "UNKNOWN";
  }
}

int parsePin(const String& pinStr) {
  String str = pinStr;
  str.trim();
  str.toUpperCase();
  if (str.startsWith("A")) {
    int analogPinNum = str.substring(1).toInt();
    return A0 + analogPinNum;
  } else {
    int pin = str.toInt();
    if (pin < 0) return -1;
    return pin;
  }
}
