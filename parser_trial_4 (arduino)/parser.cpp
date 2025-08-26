#include "parser.h"
#include "commands.h"

String getFirstWord(String msg) {
  int spaceIndex = msg.indexOf(' ');
  if (spaceIndex == -1) return msg;
  return msg.substring(0, spaceIndex);
}

String getSecondWord(String msg) {
  int firstSpace = msg.indexOf(' ');
  if (firstSpace == -1) return "";
  int secondSpace = msg.indexOf(' ', firstSpace + 1);
  if (secondSpace == -1) return msg.substring(firstSpace + 1);
  return msg.substring(firstSpace + 1, secondSpace);
}

String getThirdWord(String msg) {
  int firstSpace = msg.indexOf(' ');
  if (firstSpace == -1) return "";
  int secondSpace = msg.indexOf(' ', firstSpace + 1);
  if (secondSpace == -1) return "";
  int thirdSpace = msg.indexOf(' ', secondSpace + 1);
  if (thirdSpace == -1) return msg.substring(secondSpace + 1);
  return msg.substring(secondSpace + 1, thirdSpace);
}

String getRest(String msg) {
  int firstSpace = msg.indexOf(' ');
  if (firstSpace == -1) return "";
  int secondSpace = msg.indexOf(' ', firstSpace + 1);
  if (secondSpace == -1) return "";
  return msg.substring(secondSpace + 1);
}

void parseCommand(String msg) {
  msg.trim();
  if (msg.length() == 0) return;

  msg.toLowerCase();

  String command = getFirstWord(msg);
  command.toLowerCase();

  String args = msg.substring(command.length());
  args.trim();

  if (command == "set") {
    commands::handleSet(args);
  } else if (command == "get") {
    commands::handleGet(args);
  } else if (command == "print") {
    commands::handlePrint(args);
  } else if (command == "clear") {
    commands::handleClear(args);
  } else if (command == "findkey") {
    commands::handleFindKey(args);
  } else if (command == "adddevice") {
    commands::handleAddDevice(args);
  } else if (command == "removedevice") {
    commands::handleRemoveDevice(args);
  } else if (command == "updatedevice"){
    commands::handleUpdateDevice(args);
  } else if (command == "help") {
    commands::handleHelp(); 
  } else if (command == "toggle") {
    commands::handleToggle(args);
  } else {
    Serial.print("❌ Unknown Command: ");
    Serial.println(command);
    Serial.println("Valid Commands: set, get, print, clear, findkey, adddevice, removedevice, updatedevice, toggle, help");
  }
}
