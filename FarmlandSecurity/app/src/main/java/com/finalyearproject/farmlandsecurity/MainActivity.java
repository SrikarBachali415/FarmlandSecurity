package com.finalyearproject.farmlandsecurity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Mqtt3AsyncClient mqttClient;
    private TextView mqttStatus;
    private MessagesAdapter messagesAdapter;
    private MessagesDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mqttStatus = findViewById(R.id.mqttStatus);
        Button connectButton = findViewById(R.id.connectButton);

        // Initialize RecyclerView and database helper
        RecyclerView messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        dbHelper = new MessagesDatabaseHelper(this);
        messagesAdapter = new MessagesAdapter(dbHelper.getAllMessages(), dbHelper);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messagesAdapter);

        // Set up MQTT client
        mqttClient = MqttClient.builder()
                .useMqttVersion3()
                .identifier("srikarsrikar")
                .serverHost("broker.hivemq.com")
                .serverPort(8883) // SSL port
                .sslWithDefaultConfig() // Use SSL
                .simpleAuth()
                .username("srikarbachali@gmail.com")
                .password("srikar2004".getBytes())
                .applySimpleAuth()
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
                    String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    // Save message to database
                    dbHelper.addMessage(message, timestamp);

                    // Update RecyclerView
                    runOnUiThread(() -> {
                        messagesAdapter.setMessages(dbHelper.getAllMessages());
                    });
                })
                .send();
    }
}
