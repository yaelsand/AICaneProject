import RPi.GPIO as GPIO
import time
import picamera

GPIO.setmode(GPIO.BCM)

GPIO.setup(18, GPIO.IN, pull_up_down=GPIO.PUD_UP)

while True:
    input_state = GPIO.input(18)
    if input_state == False:
        print('BUTTON PRESS')
        time.sleep(0.2)
        with picamera.PiCamera() as camera:
            camera.resolution = (1280,720)
            camera.capture("/home/pi/Desktop/ican/1.jpg")
            camera.capture("/home/pi/Desktop/ican/2.jpg")
            camera.capture("/home/pi/Desktop/ican/3.jpg")
            print("Picturesea taken")
