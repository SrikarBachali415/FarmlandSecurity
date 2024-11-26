# Farmland Security - MQTT Messaging App

This project is an Android application designed to connect to an MQTT broker (e.g., HiveMQ Public Broker) to publish and subscribe to topics. The app includes features such as message history storage with timestamps, the ability to delete messages, and live message updates.

---

## Features

- **Connect to MQTT Broker**: Connects to the HiveMQ broker using the MQTT 3.1.1 protocol.
- **Subscribe to Topics**: Automatically subscribes to a given topic upon connection.
- **Publish Messages**: Allows publishing messages to a specific topic.
- **Message History**:
  - Stores all received messages locally with timestamps.
  - Displays messages in a list.
  - Provides a delete button for each message.
- **Live Updates**: Updates the UI in real time when new messages are received.

---

## Screenshots
<p align="center">
  <img src="https://github.com/user-attachments/assets/48641716-c028-4093-9cb2-373b2cc095af" alt="Screenshot 1" width=180 height = 360 />
  <img src="https://github.com/user-attachments/assets/84860853-14ba-4daa-83c7-f4004875cf3a" alt="Screenshot 2" width=180 height = 360 />
</p>

---

## Technology Stack

- **Programming Language**: Java
- **Development Environment**: Android Studio
- **Libraries**:
  - [HiveMQ MQTT Client](https://hivemq.github.io/hivemq-mqtt-client/) for MQTT connection
  - SQLite for local message storage
  - RecyclerView for displaying message history

---

## Requirements

- Android 5.0 (API 21) and above
- HiveMQ Public Broker (or any compatible MQTT broker)
- Android Studio (for development)

---

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name
```

### 2. Open in Android Studio
Open Android Studio.
Select File > Open.
Navigate to the cloned project folder and open it.

### 3. Configure the Broker
In MainActivity.java, ensure the correct broker details are configured:
```
mqttClient = MqttClient.builder()
    .useMqttVersion3()
    .identifier("your-unique-client-id")
    .serverHost("broker.hivemq.com")
    .serverPort(8883) // SSL port
    .sslWithDefaultConfig()
    .buildAsync();
```
### 4. Build and Run
Connect your Android device or use an emulator.
Click Run in Android Studio to install and start the app.

### Usage
- 1.Connecting:
Open the app and click the Connect button to establish a connection with the broker.
- 2.Subscribing:
Subscribes automatically to a predefined topic.
- 3.Receiving Messages:
Messages received on the subscribed topic will appear in the message list with timestamps.
- 4.Publishing Messages:
Add a topic and message in the UI and click Publish.
- 5.Deleting Messages:
Use the delete button next to a message to remove it from the history.

### Dependencies
Add the following dependencies in your build.gradle file:
```
dependencies {
    implementation 'com.hivemq:hivemq-mqtt-client:1.3.0'
    implementation 'androidx.recyclerview:recyclerview:1.3.1'
    implementation 'androidx.room:room-runtime:2.5.2'
    annotationProcessor 'androidx.room:room-compiler:2.5.2'
}
```
