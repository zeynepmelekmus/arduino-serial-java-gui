#ifndef PARSER_H
#define PARSER_H

#include <Arduino.h>

String getFirstWord(String msg);
String getSecondWord(String msg);
String getThirdWord(String msg);
String getRest(String msg);

void parseCommand(String msg);

#endif
