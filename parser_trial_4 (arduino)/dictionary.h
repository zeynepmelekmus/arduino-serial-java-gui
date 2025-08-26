#ifndef DICTIONARY_H
#define DICTIONARY_H

#include <Arduino.h>

namespace dictionary {

struct KeyValue {
  String key;
  String value;
};

bool set(const String& key, const String& value);
String get(const String& key);
bool remove(const String& key);
void clear();
void print();
void findKeysByValue(const String& value);

} // namespace dictionary

#endif
