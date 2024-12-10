import cv2
import time
import threading
import queue
import logging
import os
import pygame
import paho.mqtt.client as mqtt
from ultralytics import YOLO
from picamera2 import Picamera2

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s: %(message)s',
    datefmt='%Y-%m-%d %H:%M:%S'
)
logger = logging.getLogger(__name__)

# HiveMQ public broker details
BROKER = "broker.hivemq.com"
PORT = 1883
TOPIC = "test/topic/srikar"

CONFIG = {
    'SUMMARY_INTERVAL': 5,          # Seconds to summarize detected classes
    'DETECTION_INTERVAL': 0.25,     # Seconds between frame processing
    'CONFIDENCE_THRESHOLD': 0.7,    # Minimum confidence for detection
    'HARMFUL_CLASSES': [
        'Leopard', 'LeopardCat', 'RedFox', 'WildBoar', 
        'AmurTiger', 'Badger', 'BlackBear', 'Bluebull'
    ],
    'SOUND_PATH': r'/home/visal/Downloads/buzzer.wav'
}

class ObjectDetector:
    def __init__(self, model_path=r"/home/visal/Downloads/weights/best.pt"):
        # Initialize pygame mixer for sound
        pygame.mixer.init()
        self.sound = pygame.mixer.Sound(CONFIG['SOUND_PATH'])
        
        # Suppress YOLO model's internal logging
        self.model = YOLO(model_path)
        self.model.overrides['verbose'] = False  # Disable verbose logging

        self.detected_harmful_classes = []  # List of harmful classes detected in current window
        self.last_summary_time = time.time()  # Timer for summary messages
        self.message_queue = queue.Queue()
        self.client = mqtt.Client(client_id="PythonPublisher")
        
        # Track sound playing to avoid constant repetition
        self.sound_playing = False
        self.last_sound_time = 0
        self.sound_cooldown = 0  # 10 seconds between sound repetitions

        self.client.on_connect = self.on_connect
        self.client.connect(BROKER, PORT, keepalive=60)
        self.client.loop_start()

    def on_connect(self, client, userdata, flags, rc, properties=None):
        if rc == 0:
            logger.info("Connected to MQTT broker.")
        else:
            logger.error(f"MQTT connection failed: {rc}")

    def play_sound(self):
        """Play sound if not recently played and harmful animals detected"""
        current_time = time.time()
        if not self.sound_playing and (current_time - self.last_sound_time > self.sound_cooldown):
            try:
                self.sound.play()
                self.sound_playing = True
                self.last_sound_time = current_time
                
                # Stop the sound after 5 seconds
                threading.Timer(5, self.stop_sound).start()
            except Exception as e:
                logger.error(f"Error playing sound: {e}")

    def stop_sound(self):
        """Stop the sound and reset sound playing flag"""
        self.sound.stop()
        self.sound_playing = False

    def mqtt_publisher(self):
        while True:
            try:
                message = self.message_queue.get()
                if message:
                    self.client.publish(TOPIC, message)
                    logger.info(f"Published: {message}")
                    self.message_queue.task_done()
            except Exception as e:
                logger.error(f"Publishing error: {e}")

    def detect_objects(self):
        # Initialize Picamera2
        picam2 = Picamera2()
        picam2.preview_configuration.main.size = (1280, 720)
        picam2.preview_configuration.main.format = "RGB888"
        picam2.preview_configuration.align()
        picam2.configure("preview")
        picam2.start()

        while True:
            frame = picam2.capture_array()
            current_time = time.time()

            # Process the frame every 1/4th of a second
            if current_time - self.last_summary_time < CONFIG['DETECTION_INTERVAL']:
                continue

            results = self.model(frame)
            detections = results[0].boxes.data.cpu().numpy() if results else []

            # Track harmful classes and play sound if detected
            harmful_detected = False

            # Add unique detected harmful classes from this frame
            for detection in detections:
                confidence = detection[4]
                cls = int(detection[5])
                class_name = self.model.names[cls]

                if (confidence > CONFIG['CONFIDENCE_THRESHOLD'] and 
                    class_name in CONFIG['HARMFUL_CLASSES'] and 
                    class_name not in self.detected_harmful_classes):
                    self.detected_harmful_classes.append(class_name)
                    harmful_detected = True

            # Play sound if harmful animals are detected
            if harmful_detected:
                self.play_sound()

            # Send summary message every 5 seconds
            if current_time - self.last_summary_time >= CONFIG['SUMMARY_INTERVAL']:
                if self.detected_harmful_classes:
                    detected_classes_list = ', '.join(self.detected_harmful_classes)
                    message = f"Detected harmful animal(s): {detected_classes_list}"
                    self.message_queue.put(message)
                    logger.info(message)

                    # Clear the detected harmful classes array
                    self.detected_harmful_classes.clear()

                # Reset summary timer
                self.last_summary_time = current_time

    def run(self):
        publisher_thread = threading.Thread(target=self.mqtt_publisher, daemon=True)
        publisher_thread.start()

        self.detect_objects()

        self.message_queue.join()

def main():
    detector = ObjectDetector()
    detector.run()

if __name__ == "__main__":
    main()
