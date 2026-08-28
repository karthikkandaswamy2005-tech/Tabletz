package com.example.data.hardware

object HardwareGuideData {

  val HARDWARE_SPECS = """
    Autonomous Medicine Delivery Robot - Embedded Hardware Architecture:
    
    1. Main Processing Unit: ESP32-WROOM-32 (Dual-Core 240MHz, 2.4GHz Wi-Fi + BLE)
    2. Motor Controller / Co-processor: Arduino UNO R4 / L298N Dual H-Bridge Driver
    3. Navigation: 5-Channel TCRT5000 Infrared Line Tracking Array (Predefined hospital floor corridors)
    4. Obstacle Sensing: HC-SR04 Ultrasonic Transducer Sensor (Front buffer zone: 2cm - 400cm, 15° cone)
    5. Access Control: RC522 13.56 MHz RFID Reader Module (SPI Interface) for Nurse Staff Badge Verification
    6. Safety & Audio: Active Buzzer (obstacle & arrival alerts) + RGB Neopixel Status Ring
    7. Power Subsystem: 12V 3S Li-ion Battery Pack (2600mAh) + LM2596 Step-Down Buck Converter to 5V 3A
  """.trimIndent()

  val PINOUT_TABLE = listOf(
    "ESP32 Pin GPIO 5" to "RC522 SS (SDA) - SPI Chip Select",
    "ESP32 Pin GPIO 18" to "RC522 SCK - SPI Clock",
    "ESP32 Pin GPIO 23" to "RC522 MOSI - SPI Master Out",
    "ESP32 Pin GPIO 19" to "RC522 MISO - SPI Master In",
    "ESP32 Pin GPIO 22" to "RC522 RST - Reset Pin",
    "ESP32 Pin GPIO 4" to "HC-SR04 Ultrasonic TRIG Pin",
    "ESP32 Pin GPIO 16" to "HC-SR04 Ultrasonic ECHO Pin (via voltage divider 5V->3.3V)",
    "ESP32 Pin GPIO 13, 12" to "L298N IN1, IN2 (Left Motor Direction)",
    "ESP32 Pin GPIO 14, 27" to "L298N IN3, IN4 (Right Motor Direction)",
    "ESP32 Pin GPIO 25, 26" to "L298N ENA, ENB (PWM Speed Control)",
    "ESP32 Pin GPIO 34, 35, 32, 33, 21" to "IR Line Tracking Sensor Array (Analog / Digital inputs)",
    "ESP32 Pin GPIO 2" to "Piezo Alert Buzzer & Status LED"
  )

  val REST_PAYLOAD_EXAMPLE = """
    // 1. ESP32 Sends Robot Telemetry (HTTP POST /api/robot/telemetry)
    {
      "robot_id": "R01",
      "order_id": "MED-2026-00125",
      "current_status": "EN_ROUTE",
      "current_checkpoint": "CHECKPOINT_C2",
      "battery_percent": 94,
      "ultrasonic_distance_cm": 115.4,
      "obstacle_detected": false,
      "rfid_reader_ready": true,
      "timestamp": 1756372800000
    }

    // 2. ESP32 Sends RFID Event (HTTP POST /api/robot/rfid_verify)
    {
      "robot_id": "R01",
      "order_id": "MED-2026-00125",
      "rfid_uid": "E2 80 68 3F",
      "staff_badge_id": "STAFF-RFID-8829",
      "timestamp": 1756372950000
    }

    // 3. ESP32 Sends Obstacle Alert (HTTP POST /api/robot/obstacle_alert)
    {
      "robot_id": "R01",
      "order_id": "MED-2026-00125",
      "obstacle_detected": true,
      "distance_cm": 18.2,
      "action_taken": "ROBOT_HALTED_SAFE_WAIT",
      "checkpoint": "CHECKPOINT_C2"
    }
  """.trimIndent()

  val ESP32_SAMPLE_CODE = """
#include <WiFi.h>
#include <HTTPClient.h>
#include <SPI.h>
#include <MFRC522.h>

#define SS_PIN    5
#define RST_PIN   22
#define TRIG_PIN  4
#define ECHO_PIN  16
#define BUZZER_PIN 2

const char* ssid = "Hospital_Internal_WiFi";
const char* password = "Secure_Staff_Password";
const char* serverUrl = "http://hospital-medbot-server.local/api/robot";

MFRC522 rfid(SS_PIN, RST_PIN);

void setup() {
  Serial.begin(115200);
  SPI.begin();
  rfid.PCD_Init();
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  pinMode(BUZZER_PIN, OUTPUT);

  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nMedBot R01 Connected to Hospital Network!");
}

float measureDistanceCm() {
  digitalWrite(TRIG_PIN, LOW);
  delayMicroseconds(2);
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIG_PIN, LOW);
  long duration = pulseIn(ECHO_PIN, HIGH, 30000);
  if (duration == 0) return 400.0;
  return duration * 0.034 / 2.0;
}

void loop() {
  // 1. Obstacle Scanning
  float dist = measureDistanceCm();
  if (dist < 25.0 && dist > 2.0) {
    digitalWrite(BUZZER_PIN, HIGH);
    sendObstacleAlert("R01", dist);
    stopMotors();
  } else {
    digitalWrite(BUZZER_PIN, LOW);
  }

  // 2. RFID Tag Scanning at Destination
  if (rfid.PICC_IsNewCardPresent() && rfid.PICC_ReadCardSerial()) {
    String tagUID = "";
    for (byte i = 0; i < rfid.uid.size; i++) {
      tagUID += String(rfid.uid.uidByte[i] < 0x10 ? " 0" : " ");
      tagUID += String(rfid.uid.uidByte[i], HEX);
    }
    tagUID.trim();
    tagUID.toUpperCase();
    sendRfidVerification("R01", tagUID);
    rfid.PICC_HaltA();
  }
  delay(100);
}
  """.trimIndent()
}
