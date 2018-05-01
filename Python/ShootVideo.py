import picamera
from time import sleep
from subprocess import call

with picamera.PiCamera() as camera:
    camera.start_recording("v1.h264")
    sleep(5)
    camera.stop_recording()

# Convert the video:
command= "MP4Box -add v1.h264 converted.mp4"
call([command], shell=True)
    
