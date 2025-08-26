#include "commands.h"
#include "parser.h"
#include "dictionary.h"

namespace commands {

  const int MAX_DEVICES = 10;
  Device devices[MAX_DEVICES];
  int deviceCount = 0;

  bool addDevice(String name, String typeStr, String pinStr) {
    if (deviceCount >= MAX_DEVICES) {
        Serial.println("⚠️ Device list is full.");
        return false;
    }

    for (int i = 0; i < deviceCount; i++) {
        if (devices[i].getName() == name) {
            Serial.println("⚠️ This device is already listed.");
            return false;
        }
    }

    DeviceType type = stringToDeviceType(typeStr);
    if (type == DeviceType::UNKNOWN) {
        Serial.println("❌ Unvalid device type: " + typeStr);
        return false;
    }

    int pin = parsePin(pinStr);
    if (pin < 0) {
        Serial.println("❌ Unvalid pin number.");
        return false;
    }

    devices[deviceCount] = Device(name, type, pin);
    deviceCount++;

    // New device is added to dictionary (beginning value is "off" or "0")
    if (type == DeviceType::LED || type == DeviceType::MOTOR) {
      dictionary::set(name, "off");
    } else if (type == DeviceType::SENSOR) {
      dictionary::set(name, "0");
    }

    Serial.println("Device is added: " + name + " (" + deviceTypeToString(type) + ")");
    return true;
  }

  bool removeDevice(String name) {
    bool found = false;
    for (int i = 0; i < deviceCount; i++) {
      if (devices[i].getName() == name) {
        found = true;
        if (devices[i].getType() == DeviceType::LED || devices[i].getType() == DeviceType::MOTOR) {
          devices[i].turnOff();
        }
        for (int j = i; j < deviceCount - 1; j++) {
          devices[j] = devices[j + 1];
        }
        deviceCount--;

        // remove from dict.
        dictionary::remove(name);

        Serial.println("Device is removed: " + name);
        break;
      }
    }
    if (!found) {
      Serial.println("❌ Device is cannot found: " + name);
    }
    return found;
  }

  void listDevices() {
    Serial.println(" Listed Devices:");
    if (deviceCount == 0) {
      Serial.println("List is empty");
      return;
    }
    for (int i = 0; i < deviceCount; i++) {
      Serial.print("- ");
      Serial.print(devices[i].getName());
      Serial.print(" (");
      Serial.print(deviceTypeToString(devices[i].getType()));
      Serial.print(", Pin:");
      Serial.print(devices[i].getPin());
      Serial.println(")");
    }
  }

  void controlDevice(String name, String param) {
    for (int i = 0; i < deviceCount; i++) {
      if (devices[i].getName() == name) {
        Device &dev = devices[i];
        switch (dev.getType()) {
          case DeviceType::LED:
          case DeviceType::MOTOR:
            if (param == "on") {
              dev.turnOn();
              dictionary::set(name, "on"); // update the dict.
              Serial.println(name + " is on.");
            } else if (param == "off") {
              dev.turnOff();
              dictionary::set(name, "off"); // update the dict.
              Serial.println(name + " is off.");
            } else {
              Serial.println("⚠️ Parameter must be 'on' or 'off'.");
            }
            return;
          case DeviceType::SENSOR:
            dev.updateSensorValue();
            dictionary::set(name, String(dev.getSensorValue())); // sensor value to dict.
            Serial.print(name + " sensor value: ");
            Serial.println(dev.getSensorValue());
            return;
          default:
            Serial.println("⚠️ Unvalid device type.");
            return;
        }
      }
    }
    Serial.println("❌ Device is cannot find: " + name);
  }

  void handleSet(String args) {
    String key = getFirstWord(args);
    String value = args.substring(key.length());
    value.trim();

    if (key == "" || value == "") {
      Serial.println("❌ Command 'set' needs to <key> and <value>");
      Serial.println("Usage: set <key> <value>");
      return;
    }

    bool foundDevice = false;
    for (int i = 0; i < deviceCount; i++) {
      if (devices[i].getName() == key) {
        controlDevice(key, value);
        dictionary::set(key, value);
        foundDevice = true;
        break;
      }
    }
    if (!foundDevice) {
      dictionary::set(key, value);
    }
  }

  void handleGet(String args) {
    String key = getFirstWord(args);
    if (key == "") {
      Serial.println("❌ Command 'get' needs to <key>");
      Serial.println("Usage: get <key>");
      return;
    }
    // firstly look for devices and then for dict.
    for (int i = 0; i < deviceCount; i++) {
      if (devices[i].getName() == key) {
        Device &dev = devices[i];
        switch (dev.getType()) {
          case DeviceType::LED:
          case DeviceType::MOTOR:
            Serial.print(dev.getName() + " state: ");
            Serial.println(dev.getState() ? "ON" : "OFF");
            return;
          case DeviceType::SENSOR:
            dev.updateSensorValue();
            dictionary::set(key, String(dev.getSensorValue())); // update sensor value instantly
            Serial.print(dev.getName() + " Sensor value: ");
            Serial.println(dev.getSensorValue());
            return;
          default:
            Serial.println("⚠️ Unvalid device type.");
            return;
        }
      }
    }

    String value = dictionary::get(key);
    if (value != "") {
      Serial.print(" ");
      Serial.println(value);
    } else {
      Serial.println("❌ Cannot found. ");
    }
  }

  void handlePrint(String args) {
    String arg = getFirstWord(args);
    if (arg == "" || arg == "dictionary") {
      dictionary::print();
    } else if (arg == "devices") {
      listDevices();
    } else {
      Serial.println("❌ Unvalid print argument. Usage: print [dictionary|devices]");
    }
  }

  void handleClear(String args) {
    String arg = getFirstWord(args);
    if (arg == "" || arg == "dictionary") {
      dictionary::clear();
    } else if (arg == "devices") {
      for (int i = 0; i < deviceCount; i++) {
        if (devices[i].getType() == DeviceType::LED || devices[i].getType() == DeviceType::MOTOR) {
          devices[i].turnOff();
        }
      }
      deviceCount = 0;
      Serial.println(" Device list is cleared.");
    } else {
      Serial.println("❌ Unvalid clear argument. Usage: clear [dictionary|devices]");
    }
  }

  void handleFindKey(String args) {
    String value = getFirstWord(args);
    if (value == "") {
      Serial.println("❌ Command 'findkey' needs to <value>");
      Serial.println("Usage: findkey <value>");
      return;
    }
    dictionary::findKeysByValue(value);
  }

  void handleAddDevice(String args) {
    String name = getFirstWord(args);
    String rest = args.substring(name.length());
    rest.trim();
    String type = getFirstWord(rest);
    String pinStr = rest.substring(type.length());
    pinStr.trim();

    if (name == "" || type == "" || pinStr == "") {
      Serial.println("❌ Command 'adddevice' needs to <name> <type> <pin>");
      Serial.println("Usage: adddevice <name> <type> <pin>");
      return;
    }

    addDevice(name, type, pinStr);
  }

  void handleRemoveDevice(String args) {
    String name = getFirstWord(args);
    if (name == "") {
      Serial.println("❌ Command 'removedevice' needs to <name>");
      Serial.println("Usage: removedevice <name>");
      return;
    }
    removeDevice(name);
  }

  void handleHelp() {
    Serial.println(" Commands List:");
    Serial.println(" set <key> <value>       - Set or update a key/value");
    Serial.println(" get <key>               - Get value or device state");
    Serial.println(" print [dictionary|devices] - Print dictionary or device list");
    Serial.println(" clear [dictionary|devices] - Clear dictionary or device list");
    Serial.println(" findkey <value>         - Find keys by value");
    Serial.println(" adddevice <name> <type> <pin> - Add new device (type: led|motor|sensor)");
    Serial.println(" removedevice <name>     - Remove a device");
    Serial.println(" updatedevice <name> <type> <pin> - Update device type/pin");
    Serial.println(" toggle <name>           - Toggle LED/MOTOR device state");
    Serial.println(" help                    - Show help list on Arduino");
  }

  void handleUpdateDevice(String args) {
    String name = getFirstWord(args);
    String rest = args.substring(name.length());
    rest.trim();
    String typeStr = getFirstWord(rest);
    String pinStr = rest.substring(typeStr.length());
    pinStr.trim();

    if (name == "" || typeStr == "" || pinStr == "") {
      Serial.println("❌ Command 'updatedevice' needs to <name> <type> <pin>.");
      return;
    }

    for (int i = 0; i < deviceCount; i++) {
      if (devices[i].getName() == name) {
        DeviceType type = stringToDeviceType(typeStr);
        if (type == DeviceType::UNKNOWN) {
          Serial.println("❌ Unvalid device type.");
          return;
        }
        int pin = parsePin(pinStr);
        if (pin < 0) {
          Serial.println("❌ Unvalid pin number.");
          return;
        }

        devices[i] = Device(name, type, pin);

        if (type == DeviceType::LED || type == DeviceType::MOTOR) {
          dictionary::set(name, "off");
        } else if (type == DeviceType::SENSOR) {
          dictionary::set(name, "0");
        }

        Serial.println(" Device is updated: " + name);
        return;
      }
    }
    Serial.println("❌ No device found to update.");
  }

  void handleToggle(String args) {
    args.trim();
    if (args.length() == 0) {
        Serial.println("❌ Command 'toggle' is needs to <name> parameter.");
        return;
    }

    String name = getFirstWord(args);

    for (int i = 0; i < deviceCount; i++) {
        if (devices[i].getName() == name) {
            DeviceType type = devices[i].getType();
            if (type == DeviceType::LED || type == DeviceType::MOTOR) {
                bool currentState = devices[i].getState();
                if (currentState) {
                    devices[i].turnOff();
                    dictionary::set(name, "off");
                } else {
                    devices[i].turnOn();
                    dictionary::set(name, "on");
                }
                Serial.println("🔄 " + name + " state is toggled: " +
                               String(!currentState ? "OPEN" : "CLOSE"));
                return;
            } else {
                Serial.println("⚠️ For '" + name + "', toggle is not supported.");
                return;
            }
        }
    }
    Serial.println("❌ Device is cannot found: " + name);
  }

} // namespace commands
