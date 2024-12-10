import os
from ultralytics import YOLO
import torch
from torch.optim import AdamW
from torch.optim.lr_scheduler import CosineAnnealingWarmRestarts
import wandb

# Configuration
DATASET_PATH = r"E:\YOLO Combined Dataset\data.yaml"
NUM_EPOCHS = 150
DEVICE = 'cuda' if torch.cuda.is_available() else 'cpu'
PRETRAINED_WEIGHTS = 'yolov8n.pt'  # or yolov11n.pt
OUTPUT_DIR = r"E:\YOLO Combined Dataset\runs"

def setup_wandb():
    """Initialize Weights & Biases for experiment tracking."""
    wandb.init(
        project="Wildlife-Detection-TransferLearning",
        config={
            "architecture": "YOLOv8",
            "dataset": "Combined COCO, VOC, Harmful Animals",
            "epochs": NUM_EPOCHS,
            "pretrained_weights": PRETRAINED_WEIGHTS
        }
    )


def train_yolov8():
    """Perform transfer learning with advanced techniques."""
    # Initialize wandb for experiment tracking
    setup_wandb()
    
    # Load pre-trained YOLOv8 model
    model = YOLO(PRETRAINED_WEIGHTS)
    
    # Train with custom configurations
    results = model.train(
        
        data=DATASET_PATH,
        epochs=50,
        imgsz=416,  # Reduced size
        batch=16,   # Confirmed batch size
        device='cpu',
        patience=10,
        
        # Learning rate parameters mimicking Cosine Annealing
        lr0=0.001,       # Initial learning rate
        lrf=0.01,        # Final learning rate fraction
        warmup_epochs=3, # Warmup period
        
        # Cosine-like decay behavior
        cos_lr=True, 
        
        # Diagnostic settings
        verbose=True,
        plots=True,
        close_mosaic=10,  # Close mosaic augmentation in last 10 epochs
        
        # Mixed precision training
        amp=True,  
        # More controlled augmentations
        augment=True,
        hsv_h=0.1,    # Moderate hue changes
        hsv_s=0.5,    # Moderate saturation changes
        degrees=15,   # Reasonable rotation
        translate=0.1,# Slight translation
        scale=0.1    # Small scale variations
    )

    """"
        hsv_h=0.015,  # Image HSV-Hue augmentation
        hsv_s=0.7,   # Image HSV-Saturation augmentation
        hsv_v=0.4,   # Image HSV-Value augmentation
        degrees=0.373,  # Image rotation (+/- deg)
        translate=0.1,  # Image translation (+/- fraction)
        scale=0.898,   # Image scale (+/- gain)
        shear=0.602,   # Image shear (+/- deg)
        perspective=0.0,  # Image perspective
        flipud=0.00867,  # Image flip up-down
        fliplr=0.5,     # Image flip left-right
        mosaic=1.0,     # Mosaic augmentation
    """
    # Export for deployment
    export_models(model)
    
    # Close wandb
    wandb.finish()

def export_models(model):
    
    # Export to NCNN for Raspberry Pi
    try:
        model.export(format='ncnn')
        print("Successfully exported to NCNN format!")
    except Exception as e:
        print(f"NCNN export failed: {e}")


def main():
    # Create output directory
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    # Set global seeds for reproducibility
    torch.manual_seed(42)
    
    # Train the model
    train_yolov8()

if __name__ == "__main__":
    main()
