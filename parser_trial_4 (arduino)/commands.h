#ifndef COMMANDS_H
#define COMMANDS_H

#include <Arduino.h>
#include "Device.h"

namespace commands {

void handleSet(String args);
void handleGet(String args);
void handlePrint(String args);
void handleClear(String args);
void handleFindKey(String args);
void handleAddDevice(String args);
void handleRemoveDevice(String args);
void handleHelp();
void handleUpdateDevice(String args);
void handleToggle(String args);

void listDevices();

bool addDevice(String name, String typeStr, String pinStr);
bool removeDevice(String name);
void controlDevice(String name, String param);

} // namespace commands

#endif
