package com.finalyearproject.farmlandsecurity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.util.concurrent.ExecutorService;

public class MainActivity extends AppCompatActivity {
    private Mqtt3AsyncClient mqttClient;
    private TextView mqttStatus;
    private TextView messagesTextView;
    private ExecutorService executorService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messagesTextView = findViewById(R.id.messagesTextView);
        mqttStatus = findViewById(R.id.mqttStatus);
        Button connectButton = findViewById(R.id.connectButton);
        mqttClient = MqttClient.builder()
                .useMqttVersion3()  // Ensure you're using MQTT 3.1.1
                .identifier("srikarsrikar")
                .serverHost("broker.hivemq.com")
                .serverPort(8883) // SSL port
                .sslWithDefaultConfig() // Use SSL
                .simpleAuth()
                .username("srikarbachali@gmail.com")
                .password("srikar2004".getBytes())
                .applySimpleAuth().buildAsync();

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
