package com.finalyearproject.farmlandsecurity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {
    private Mqtt3AsyncClient mqttClient;
    private TextView mqttStatus;
    private TextView messagesTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messagesTextView = findViewById(R.id.messagesTextView);
        mqttStatus = findViewById(R.id.mqttStatus);
        Button connectButton = findViewById(R.id.connectButton);

        // Initialize the MQTT client with HiveMQ public broker details
        mqttClient = MqttClient.builder()
                .useMqttVersion3()  // Ensure you're using MQTT 3.1.1
                .identifier("srikarsrikar")
                .serverHost("broker.hivemq.com")
                .serverPort(1883)
                .buildAsync();


        connectButton.setOnClickListener(v -> connectToMQTTBroker());
    }

    @SuppressLint("NewApi")
    private void connectToMQTTBroker() {
            mqttClient.connect()
                    .whenComplete((connAck, throwable) -> {
                        if (throwable == null) {
                            // Successfully connected
                            runOnUiThread(() -> {
                                mqttStatus.setText("MQTT Status: Connected");
                                Toast.makeText(MainActivity.this, "Connected to HiveMQ Public Broker", Toast.LENGTH_SHORT).show();
                                subscribeToTopic("test/topic/srikar");  // Subscribe to a topic after connecting
                            });
                        } else {
                            // Failed to connect
                            runOnUiThread(() -> {
                                mqttStatus.setText("MQTT Status: Failed to connect");
                                Toast.makeText(MainActivity.this, "Failed to connect to HiveMQ Public Broker", Toast.LENGTH_SHORT).show();
                            });
                            Log.e("MQTT", "Connection error: " + throwable.getMessage(), throwable);  // Log the error
                        }
                    });

    }

    private void subscribeToTopic(String topic) {
        mqttClient.toAsync().subscribeWith()
                .topicFilter(topic)
                .callback(publish -> {
                    // Handle incoming messages
                    String message = new String(publish.getPayloadAsBytes());

                    // Display the message in the TextView
                    runOnUiThread(() -> {
                        // Append the message to the existing messages in the TextView
                        String currentText = messagesTextView.getText().toString();
                        String updatedText = currentText + "\n" + message;
                        messagesTextView.setText(updatedText);
                    });
                })
                .send();
    }
}
