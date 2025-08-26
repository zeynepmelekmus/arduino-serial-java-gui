package serialgui5;

import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class SerialGui5 extends JFrame {
    
    private static class Device {
        String name;
        String type;
        String pin;
        boolean state; // on/off for LED and MOTOR, may be not using for SENSOR

        Device(String name, String type, String pin) {
            this.name = name;
            this.type = type.toUpperCase();
            this.pin = pin;
            this.state = false;
        }

        @Override
        public String toString() {
            return name + " (" + type + ") - Pin: " + pin + " - State: " + (state ? "ON" : "OFF");
        }
    }

    private ArrayList<Device> deviceList = new ArrayList<>();

    private static class Theme {
        Color background;
        Color foreground;
        Color buttonBackground;
        Color buttonForeground;
        Color comboBoxBackground;
        Color comboBoxForeground;

        Theme(Color bg, Color fg, Color btnBg, Color btnFg, Color cbBg, Color cbFg) {
            background = bg;
            foreground = fg;
            buttonBackground = btnBg;
            buttonForeground = btnFg;
            comboBoxBackground = cbBg;
            comboBoxForeground = cbFg;
        }
    }

    private Theme darkTheme = new Theme(
            new Color(30, 30, 30),
            new Color(200, 255, 200),
            new Color(173, 216, 230),
            new Color(30, 30, 30),
            new Color(173, 216, 230),
            new Color(30, 30, 30)
    );

    private Theme lightTheme = new Theme(
            Color.WHITE,
            Color.BLACK,
            new Color(220, 220, 220),
            Color.BLACK,
            Color.WHITE,
            Color.BLACK
    );

    private boolean isDarkTheme = true;

    private JComboBox<String> portList;
    private JComboBox<String> baudRateList;
    private JButton connectButton, refreshButton, clearButton, sendButton, themeToggleButton, logButton, uploadButton;
    private JTextField commandField;
    private JTextPane outputPane;
    private StyledDocument doc;
    private JLabel statusLabel;
    private JLabel portLabel, baudRateLabel;

    private JPanel topPanel, bottomPanel, mainPanel;

    private SerialPort serialPort;
    private OutputStream out;

    private ArrayList<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SerialGui5() {
        setTitle("Arduino Serial GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());
        topPanel = new JPanel(new GridBagLayout());
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        portLabel = new JLabel("Port: ");
        baudRateLabel = new JLabel("Baud Rate: ");
        portList = new JComboBox<>();
        baudRateList = new JComboBox<>(new String[]{"300", "1200", "2400", "4800", "9600", "14400", "19200", "38400", "57600", "115200"});
        baudRateList.setSelectedItem("9600");

        connectButton = new JButton("Connect");
        refreshButton = new JButton("Refresh 🔄");
        clearButton = new JButton("Clear");
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        themeToggleButton = new JButton("Switch Theme");
        logButton = new JButton("Save Log File");
        uploadButton = new JButton("Upload to Arduino");

        commandField = new JTextField(60);

        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setFont(new Font("Consolas", Font.PLAIN, 14));
        doc = outputPane.getStyledDocument();

        statusLabel = new JLabel("Not Connected");
        statusLabel.setForeground(Color.RED);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        topPanel.add(portLabel, gbc);

        gbc.gridx = 2;
        topPanel.add(baudRateLabel, gbc);

        gbc.gridx = 1;
        topPanel.add(portList, gbc);

        gbc.gridx = 3;
        topPanel.add(baudRateList, gbc);

        gbc.gridx = 4;
        topPanel.add(statusLabel, gbc);

        gbc.gridx = 5;
        topPanel.add(refreshButton, gbc);

        gbc.gridx = 6;
        topPanel.add(connectButton, gbc);
        
        gbc.gridx = 7;
        topPanel.add(uploadButton, gbc);

        gbc.gridx = 8;
        topPanel.add(clearButton, gbc);

        gbc.gridx = 9;
        topPanel.add(themeToggleButton, gbc);

        bottomPanel.add(commandField);
        bottomPanel.add(sendButton);
        bottomPanel.add(logButton);
        bottomPanel.add(themeToggleButton);

        JScrollPane scrollPane = new JScrollPane(outputPane);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu helpMenu = new JMenu("Help");

        JMenuItem showCommandsItem = new JMenuItem("Show Commands (help)");
        showCommandsItem.setToolTipText("Send 'help' to Arduino and list available commands.");
        showCommandsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        showCommandsItem.addActionListener(e -> sendQuick("help"));
        helpMenu.add(showCommandsItem);

        JMenuItem cheatSheetItem = new JMenuItem("Command Cheat Sheet");
        cheatSheetItem.addActionListener(e -> showCheatSheetDialog());
        helpMenu.add(cheatSheetItem);

        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        JMenu devicesMenu = new JMenu("Devices");

        JMenuItem listDevicesItem = new JMenuItem("List Devices");
        listDevicesItem.addActionListener(e -> {
            if (deviceList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No devices in Java list yet.", "Devices", JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder sb = new StringBuilder();
                for (Device d : deviceList) {
                    sb.append(d.toString()).append("\n");
                }
                JTextArea area = new JTextArea(sb.toString(), 12, 40);
                area.setEditable(false);
                JScrollPane sp = new JScrollPane(area);
                JOptionPane.showMessageDialog(this, sp, "Devices in Java List", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        devicesMenu.add(listDevicesItem);

        JMenuItem addDeviceItem = new JMenuItem("Add Device");
        addDeviceItem.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField typeField = new JTextField();
            JTextField pinField = new JTextField();
            Object[] message = {
                "Name:", nameField,
                "Type (LED, MOTOR, SENSOR):", typeField,
                "Pin:", pinField
            };
            int option = JOptionPane.showConfirmDialog(this, message, "Add Device", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    String name = nameField.getText().trim();
                    String type = typeField.getText().trim();
                    String pin = pinField.getText().trim();
                    addDeviceToList(name, type, pin);
                    sendQuick("adddevice " + name + " " + type + " " + pin); // send to arduino
                } catch (Exception ex) {
                    appendOutput("⚠️ Invalid input for new device.");
                }
            }
        });

        devicesMenu.add(addDeviceItem);
        menuBar.add(devicesMenu);

        applyTheme(darkTheme);
        listSerialPorts();

        refreshButton.addActionListener(e -> {
            portList.removeAllItems();
            listSerialPorts();
            appendOutput(portList.getItemCount() == 0 ? "⚠️ Cannot found any serial port." : "🔁 Port list is refreshed.");
        });

        connectButton.addActionListener(e -> connectToPort());
        sendButton.addActionListener(e -> sendCommand());
        clearButton.addActionListener(e -> {
            outputPane.setText("");
            appendOutput("🧹 Terminal is cleared.");
        });

        commandField.addActionListener(e -> sendCommand());

        commandField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP && !commandHistory.isEmpty() && historyIndex < commandHistory.size() - 1) {
                    historyIndex++;
                    commandField.setText(commandHistory.get(commandHistory.size() - 1 - historyIndex));
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (historyIndex > 0) {
                        historyIndex--;
                        commandField.setText(commandHistory.get(commandHistory.size() - 1 - historyIndex));
                    } else if (historyIndex == 0) {
                        historyIndex--;
                        commandField.setText("");
                    }
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (serialPort != null && serialPort.isOpen()) serialPort.closePort();
            }
        });

        themeToggleButton.addActionListener(e -> {
            if (isDarkTheme) {
                applyTheme(lightTheme);
            } else {
                applyTheme(darkTheme);
            }
            isDarkTheme = !isDarkTheme;
        });
        
        logButton.addActionListener(e -> {
        String terminalText = outputPane.getText();
        logCurrentTerminal(terminalText);
        });
        
        uploadButton.addActionListener(e -> uploadToArduino());
        
        setVisible(true);
    }

    private void applyTheme(Theme theme) {
        mainPanel.setBackground(theme.background);
        topPanel.setBackground(theme.background);
        bottomPanel.setBackground(theme.background);

        portLabel.setForeground(theme.foreground);
        baudRateLabel.setForeground(theme.foreground);

        portList.setBackground(theme.comboBoxBackground);
        portList.setForeground(theme.comboBoxForeground);

        baudRateList.setBackground(theme.comboBoxBackground);
        baudRateList.setForeground(theme.comboBoxForeground);

        connectButton.setBackground(theme.buttonBackground);
        connectButton.setForeground(theme.buttonForeground);

        refreshButton.setBackground(theme.buttonBackground);
        refreshButton.setForeground(theme.buttonForeground);

        clearButton.setBackground(theme.buttonBackground);
        clearButton.setForeground(theme.buttonForeground);

        sendButton.setBackground(theme.buttonBackground);
        sendButton.setForeground(theme.buttonForeground);

        themeToggleButton.setBackground(theme.buttonBackground);
        themeToggleButton.setForeground(theme.buttonForeground);

        commandField.setBackground(theme.background);
        commandField.setForeground(theme.foreground);

        outputPane.setBackground(theme.background);
        outputPane.setForeground(theme.foreground);

        statusLabel.setForeground(theme.foreground);
    }

    private void listSerialPorts() {
        for (SerialPort port : SerialPort.getCommPorts()) {
            portList.addItem(port.getSystemPortName());
        }
    }

    private StringBuilder serialBuffer = new StringBuilder();

    private void connectToPort() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            appendOutput("🔌 Connection is lost.");
            connectButton.setText("Connect");
            portList.setEnabled(true);
            baudRateList.setEnabled(true);
            refreshButton.setEnabled(true);
            statusLabel.setText("Not Connected");
            statusLabel.setForeground(Color.RED);
            sendButton.setEnabled(false);
            return;
        }

        String selectedPort = (String) portList.getSelectedItem();
        if (selectedPort == null) {
            appendOutput("⚠️ Please select a serial port.");
            return;
        }

        int baudRate = Integer.parseInt((String) baudRateList.getSelectedItem());
        serialPort = SerialPort.getCommPort(selectedPort);
        serialPort.setBaudRate(baudRate);

        if (serialPort.openPort()) {
            appendOutput("✅ Connected: " + selectedPort + " @ " + baudRate + " baud");
            connectButton.setText("Disconnect");
            portList.setEnabled(false);
            baudRateList.setEnabled(false);
            refreshButton.setEnabled(false);
            statusLabel.setText("Connected");
            statusLabel.setForeground(new Color(0, 200, 0));
            sendButton.setEnabled(true);

            try {
                out = serialPort.getOutputStream();
            } catch (Exception e) {
                appendOutput("⚠️ Cannot receive output stream: " + e.getMessage());
            }

            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                        return;

                    byte[] newData = new byte[serialPort.bytesAvailable()];
                    int numRead = serialPort.readBytes(newData, newData.length);

                    String receivedPart = new String(newData, 0, numRead);
                    serialBuffer.append(receivedPart);

                    int index;
                    while ((index = serialBuffer.indexOf("\n")) != -1) {
                        String line = serialBuffer.substring(0, index).trim();
                        serialBuffer.delete(0, index + 1);

                        String finalLine = line;
                        SwingUtilities.invokeLater(() -> {
                            appendOutput("📥 " + finalLine);
                            //logToFile("IN: " + finalLine);
                        });
                    }
                }
            });

        } else {
            String msg = "Port is cannot selected. May be used by another application (such as Arduino IDE).";
            JOptionPane.showMessageDialog(this, msg, "Port is unavailable.", JOptionPane.ERROR_MESSAGE);
            appendOutput(msg);
        }
    }

    private void addDeviceToList(String name, String type, String pin) {
        for (Device d : deviceList) {
            if (d.name.equals(name)) {
                appendOutput("⚠️ Device " + name + " already exists in Java list.");
                return;
            }
        }
        Device device = new Device(name, type, pin);
        deviceList.add(device);
        appendOutput("✅ Device added to Java list: " + device);
    }

    private void toggleDeviceState(String name) {
        for (Device d : deviceList) {
            if (d.name.equalsIgnoreCase(name)) {
                d.state = !d.state;
                appendOutput("🔁 Device state changed locally: " + d);
                break;
            }
        }
    }
    
    private void setDeviceState(String deviceName, String state) {
    for (Device d : deviceList) {
        if (d.name.equalsIgnoreCase(deviceName)) {
            d.state = state.equalsIgnoreCase("on") ? true : false; // state'i boolean olarak saklıyorsak
            appendOutput("⚡ Device " + d.name + " set to " + (d.state ? "ON" : "OFF"));
            // Burada istersen serialPort üzerinden cihazı kontrol eden komut da gönderebilirsin
            return;
        }
    }
    appendOutput("⚠️ Device " + deviceName + " not found.");
}

    private void showDeviceList() {
        if (deviceList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No devices added yet.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Device d : deviceList) {
            sb.append(d.toString()).append("\n");
        }
        JTextArea area = new JTextArea(sb.toString(), 16, 40);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(area);
        JOptionPane.showMessageDialog(this, sp, "Device List", JOptionPane.INFORMATION_MESSAGE);
    }

    private void sendCommand() {
        if (serialPort == null || !serialPort.isOpen()) {
            appendOutput("⚠️ Firstly connect a port.");
            return;
        }
        try {
            String command = commandField.getText().trim();
            if (!command.isEmpty()) {
                out.write((command + "\n").getBytes());
                out.flush();
                appendOutput("📤 " + command);
                //logToFile("OUT: " + command);
                commandHistory.add(command);
                historyIndex = -1;
                commandField.setText("");
            }
            String cmdLower = command.toLowerCase();
           if (cmdLower.startsWith("adddevice")) {
                String[] parts = command.split(" ");
                if (parts.length >= 4) {
                    String name = parts[1];
                    String type = parts[2];
                    String pin = parts[3];
                    addDeviceToList(name, type, pin); // Java listesine ekle
                }
            } else if (cmdLower.startsWith("updatedevice")) {
                String[] parts = command.split(" ");
                if (parts.length >= 4) {
                    String name = parts[1];
                    String type = parts[2];
                    String pin = parts[3];
                    boolean found = false;
                    for (Device d : deviceList) {
                        if (d.name.equalsIgnoreCase(name)) {
                            d.type = type.toUpperCase();
                            d.pin = pin;
                            appendOutput("🔄 Device updated in Java: " + d);
                            found = true;
                            break;
                        }
                    }
                    if (!found) appendOutput("⚠️ Device " + name + " not found in Java list.");
                }
            } else if (cmdLower.startsWith("toggle")) {
                String[] parts = command.split(" ");
                if (parts.length >= 2) toggleDeviceState(parts[1]);
            } else if (cmdLower.startsWith("set")) {
                String[] parts = command.split(" ");
                if (parts.length >= 3) setDeviceState(parts[1], parts[2]); 
            } else if (cmdLower.startsWith("removedevice")) {
                String[] parts = command.split(" ");
                if (parts.length >= 2) {
                    String name = parts[1];
                    boolean removed = deviceList.removeIf(d -> d.name.equalsIgnoreCase(name));
                    if (removed) appendOutput("️ Device removed in Java: " + name);
                    else appendOutput("⚠️ Device " + name + " not found in Java list.");
                }
            } else if (cmdLower.equals("clear devices")) {
                deviceList.clear();
                appendOutput(" Java device list cleared.");
            }       
        } catch (Exception e) {
            appendOutput("⚠️ Failed to send command. " + e.getMessage());
        }
    }
    
    private void sendQuick(String command) {
        if (serialPort == null || !serialPort.isOpen()) {
            appendOutput("⚠️ Connect to a port first.");
            return;
        }
        try {
            out.write((command + "\n").getBytes());
            out.flush();
            appendOutput("📤 " + command);
            //logToFile("OUT: " + command);
            commandHistory.add(command);
            historyIndex = -1;
        } catch (Exception e) {
            appendOutput("⚠️ Failed to send command: " + e.getMessage());
        }
    }
    
    private void showCheatSheetDialog() {
        String text =
                "Available Commands (quick reference):\n" +
                "  set <key> <value>                - Set or update a key/value\n" +
                "  get <key>                        - Get value or device state\n" +
                "  print [dictionary|devices]       - Print dictionary or device list\n" +
                "  clear [dictionary|devices]       - Clear dictionary or device list\n" +
                "  findkey <value>                  - Find keys by value\n" +
                "  adddevice <name> <type> <pin>    - Add new device (type: led|motor|sensor)\n" +
                "  removedevice <name>              - Remove a device\n" +
                "  updatedevice <name> <type> <pin> - Update device type/pin\n" +
                "  toggle <name>                    - Toggle LED/MOTOR device state\n" +
                "  help                             - Show this list on Arduino\n";

        JTextArea area = new JTextArea(text, 16, 52);
        area.setEditable(false);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane sp = new JScrollPane(area);
        JOptionPane.showMessageDialog(this, sp, "Command Cheat Sheet", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void requestAndShowList(String command, String title) {
        if (serialPort == null || !serialPort.isOpen()) {
            appendOutput("⚠️ Firstly connect a port.");
            return;
        }

        try {
            out.write((command + "\n").getBytes());
            out.flush();
            appendOutput("📤 " + command);
        } catch (Exception e) {
            appendOutput("⚠️ Failed to send command. " + e.getMessage());
            return;
        }

        new Thread(() -> {
            try {
                Thread.sleep(500); // wait for response from arduino
            } catch (InterruptedException ignored) { }

            String response;
            synchronized (serialBuffer) {
                response = serialBuffer.toString().trim();
                serialBuffer.setLength(0); // clear the buffer
            }

            String finalResponse = response.isEmpty() ? "⚠️ Failed to response." : response;
            SwingUtilities.invokeLater(() -> {
                JTextArea textArea = new JTextArea(finalResponse, 15, 50);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
            });
        }).start();
    }

    private void appendOutput(String text) {
        String timestamp = "[" + dtf.format(LocalDateTime.now()) + "] ";
        Color color = new Color(0, 255, 0);
        if (text.startsWith("📤")) color = new Color(100, 149, 237);
        else if (text.startsWith("⚠️") || text.startsWith("❌")) color = Color.RED;
        else if (text.startsWith("📥")) color = new Color(0, 200, 0);
        else if (text.startsWith("✅")) color = new Color(0, 255, 0);
        else if (text.startsWith("🔁") || text.startsWith("🧹") || text.startsWith("🔌")) color = new Color(255, 215, 0);
        else if (text.startsWith("📖")) color = new Color(100, 149, 237); // help başlıkları

        appendColoredOutput(timestamp + text, color);
    }

    private void appendColoredOutput(String text, Color color) {
        try {
            StyleContext sc = StyleContext.getDefaultStyleContext();
            AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
            doc.insertString(doc.getLength(), text + "\n", aset);
            outputPane.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private void logToFile(String line) {
        File logFile = new File("serial_log.txt");
        if (logFile.exists() && logFile.length() > 5_000_000) {
            logFile.renameTo(new File("serial_log_backup_" + System.currentTimeMillis() + ".txt"));
        }
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(line + "\n");
        } catch (Exception ignored) {
        }
    }*/
    
    private void logCurrentTerminal(String terminalText) {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
        File logFile = new File(logDir, "serial_log_" + timestamp + ".txt");

        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write("=== LOG START ===\n");
            writer.write("Date: " + timestamp + "\n\n");
            writer.write(terminalText);
            writer.write("\n=== LOG END ===\n");

            JOptionPane.showMessageDialog(this,
                "Log kaydedildi:\n" + logFile.getAbsolutePath(),
                "Log Kaydedildi",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Log kaydedilirken hata oluştu:\n" + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void uploadToArduino() {
        String selectedPort = (String) portList.getSelectedItem();
        if (selectedPort == null) {
            appendOutput("⚠️ Please select a COM port before uploading.");
            return;
        }

        // Dosya yollarını ayarla (resources içinden temp'e çıkarılacak)
        try {
            File tempDir = Files.createTempDirectory("arduino_upload").toFile();
            File avrdudeExe = extractResource("/avrdude.exe", tempDir);
            File avrdudeConf = extractResource("/avrdude.conf", tempDir);
            File hexFile = extractResource("/parser_trial_4.ino.hex", tempDir); // hex dosyan

            String[] cmd = {
                avrdudeExe.getAbsolutePath(),
                "-C" + avrdudeConf.getAbsolutePath(),
                "-v",
                "-patmega2560",
                "-cwiring",
                "-P" + selectedPort,
                "-b115200",
                "-D",
                "-Uflash:w:" + hexFile.getAbsolutePath() + ":i"
            };

            appendOutput("📤 Upload started...");

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Çıktıyı outputPane'e yaz
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String l = line; 
                        SwingUtilities.invokeLater(() -> appendOutput("📥 " + l));
                    }
                    int exitCode = process.waitFor();
                    SwingUtilities.invokeLater(() -> {
                        if (exitCode == 0) appendOutput("✅ Upload completed successfully!");
                        else appendOutput("❌ Upload failed. Check connection and Arduino model.");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> appendOutput("❌ Error during upload: " + e.getMessage()));
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            appendOutput("❌ Failed to prepare upload: " + e.getMessage());
        }
    }

    // Resource çıkarma metodu
    private File extractResource(String resourcePath, File targetDir) throws IOException {
        InputStream is = getClass().getResourceAsStream(resourcePath);
        if (is == null) throw new FileNotFoundException("Resource not found: " + resourcePath);

        File outFile = new File(targetDir, new File(resourcePath).getName());
        try (OutputStream os = new FileOutputStream(outFile)) {
            is.transferTo(os);
        }
        return outFile;
    }

  
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(SerialGui5::new);
    }
}
