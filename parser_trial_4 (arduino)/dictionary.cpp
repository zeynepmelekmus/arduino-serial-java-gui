#include "dictionary.h"

namespace dictionary {

  const int MAX_SIZE = 30;

  KeyValue dictionary[MAX_SIZE];
  int dictionarySize = 0;

  bool set(const String& key, const String& value) {
    for (int i = 0; i < dictionarySize; i++) {
      if (dictionary[i].key == key) {
        dictionary[i].value = value;
        return true;
      }
    }
    if (dictionarySize < MAX_SIZE) {
      dictionary[dictionarySize].key = key;
      dictionary[dictionarySize].value = value;
      dictionarySize++;
      return true;
    }
    return false;
  }

  String get(const String& key) {
    for (int i = 0; i < dictionarySize; i++) {
      if (dictionary[i].key == key) {
        return dictionary[i].value;
      }
    }
    return "";  // Anahtar yoksa boş döner
  }

  bool remove(const String& key) {
    for (int i = 0; i < dictionarySize; i++) {
      if (dictionary[i].key == key) {
        for (int j = i; j < dictionarySize - 1; j++) {
          dictionary[j] = dictionary[j + 1];
        }
        dictionarySize--;
        return true;
      }
    }
    return false;
  }

  void clear() {
    dictionarySize = 0;
    Serial.println("Dictionary is cleared.");
  }

  void print() {
    Serial.println("Dictionary content:");
    for (int i = 0; i < dictionarySize; i++) {
      Serial.print("- ");
      Serial.print(dictionary[i].key);
      Serial.print(" = ");
      Serial.println(dictionary[i].value);
    }
  }

  void findKeysByValue(const String& value) {
    bool found = false;
    String lowerValue = value;
    lowerValue.toLowerCase();

    for (int i = 0; i < dictionarySize; i++) {
      String currentValue = dictionary[i].value;
      currentValue.toLowerCase();
      if (currentValue == lowerValue) {
        Serial.println(dictionary[i].key);
        found = true;
      }
    }

    if (!found) {
      Serial.println("❌ Value is cannot found.");
    }
  }

} // namespace dictionary
