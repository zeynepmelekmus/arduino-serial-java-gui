# Arduino + Java Swing GUI Project | Arduino + Java Swing GUI Projesi

---

## 🌐 Languages | Diller
- [English](#english)  
- [Türkçe](#türkçe)  

---

# English

## 🌍 Introduction
This repository contains a **command-based control system** developed with **Arduino Mega 2560** and a **Java Swing GUI**.  
It demonstrates how to establish **serial communication** between a computer and Arduino, manage devices dynamically through a mini parser, and control them via an easy-to-use desktop interface.  

---

## 🚀 Running Instructions

### a. Using IDEs

#### Arduino Side
1. Open `Arduino/parser.ino` in the Arduino IDE  
2. Select **Arduino Mega 2560** as the board  
3. Upload the code to the board  

#### Java GUI Side
1. Open the `JavaGUI` folder in your preferred IDE (IntelliJ IDEA / NetBeans / Eclipse)  
2. Add the `jSerialComm` library to the project  
3. Run the GUI application  
4. Select the serial port and click the **Connect** button  
5. Send commands and view Arduino responses in the log panel  

---

### b. Without Using IDEs
If you don’t want to use Arduino IDE or a Java IDE, you can directly run the `.exe` file inside the `arduino serial program` folder.  
- Before connecting to the port, click the **Upload to Arduino** button to upload the embedded Arduino code to the **Arduino Mega 2560** board.  
- After uploading, connect to the board and start using the program immediately.  

---

## 📖 Example Commands
- `adddevice led1 led 1` → Adds a new device  
- `set led1 on` → Turns on the device  
- `toggle led1` → Toggles the device state  
- `print devices` → Lists all devices  
- `clear dictionary` → Clears the dictionary  

---

## 👩‍💻 Developer
**Zeynep Melek Muş**  
2nd-year Software Engineering Student – Erciyes University  

---

## 📌 References
- [Arduino Mega 2560 Official Documentation](https://docs.arduino.cc/hardware/mega-2560)  
- [Arduino IDE](https://www.arduino.cc/en/software)  
- [Fazecast jSerialComm](https://fazecast.github.io/jSerialComm/)  

---

# Türkçe

## 🌍 Giriş
Bu depo, **Arduino Mega 2560** ve **Java Swing GUI** kullanılarak geliştirilmiş **komut tabanlı bir kontrol sistemini** içermektedir.  
Proje, bilgisayar ile Arduino arasında **seri port haberleşmesinin** nasıl kurulacağını, bir mini parser aracılığıyla cihazların dinamik olarak nasıl yönetileceğini ve kullanıcı dostu bir masaüstü arayüzünden nasıl kontrol edileceğini göstermektedir.  

---

## 🚀 Çalıştırma Adımları

### a. IDE Kullanarak

#### Arduino Tarafı
1. `Arduino/parser.ino` dosyasını Arduino IDE ile aç  
2. Kart türü olarak **Arduino Mega 2560** seç  
3. Kodu karta yükle  

#### Java GUI Tarafı
1. `JavaGUI` klasörünü IDE (IntelliJ IDEA / NetBeans / Eclipse) ile aç  
2. `jSerialComm` kütüphanesini projeye ekle  
3. GUI uygulamasını çalıştır  
4. Seri portu seç ve **bağlan** butonuna tıkla  
5. Komutları göndererek Arduino’dan gelen cevapları log ekranında görüntüle  

---

### b. IDE Kullanmadan
Eğer Arduino IDE veya Java IDE kullanmak istemezseniz, `arduino serial program` klasöründeki `.exe` dosyasını doğrudan çalıştırabilirsiniz.  
- Porta bağlanmadan önce **Upload to Arduino** butonu ile gömülü Arduino kodunu **Arduino Mega 2560** kartına yükleyin.  
- Yükleme tamamlandıktan sonra karta bağlanarak programı hemen kullanabilirsiniz.  

---

## 📖 Örnek Komutlar
- `adddevice led1 led 1` → Yeni cihaz ekler  
- `set led1 on` → Cihazı açar  
- `toggle led1` → Cihazın durumunu değiştirir  
- `print devices` → Tüm cihazları listeler  
- `clear dictionary` → Dictionary’yi temizler  

---

## 👩‍💻 Geliştirici
**Zeynep Melek Muş**  
2. sınıf Yazılım Mühendisliği öğrencisi – Erciyes Üniversitesi  

---

## 📌 Kaynaklar
- [Arduino Mega 2560 Resmi Dokümantasyonu](https://docs.arduino.cc/hardware/mega-2560)  
- [Arduino IDE](https://www.arduino.cc/en/software)  
- [Fazecast jSerialComm](https://fazecast.github.io/jSerialComm/)  
