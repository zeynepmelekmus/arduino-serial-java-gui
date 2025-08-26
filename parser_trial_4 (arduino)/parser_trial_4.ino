#include <Arduino.h>
#include "parser.h"
#include "commands.h"

void setup() {
  Serial.begin(9600);
  Serial.println("Connecting is ready! Commands:");
  Serial.println("set <key> <value>");
  Serial.println("get <key>");
  Serial.println("print [dictionary|devices]");
  Serial.println("clear [dictionary|devices]");
  Serial.println("findkey <value>");
  Serial.println("adddevice <name> <type> <pin>");
  Serial.println("removedevice <name>");
  Serial.println("updatedevice <name> <type> <pin>");
  Serial.println("toggle <name>");
  Serial.println("help");

}

void loop() {
  if (Serial.available()) {
    String msg = Serial.readStringUntil('\n');
    parseCommand(msg);
  }
}
