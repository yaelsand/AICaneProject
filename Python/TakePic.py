import picamera
print("About to take a picture")
with picamera.PiCamera() as camera:
	camera.resolution = (1280,720)
	camera.capture("/home/pi/Desktop/ican/1.jpg")
	camera.capture("/home/pi/Desktop/ican/2.jpg")
	camera.capture("/home/pi/Desktop/ican/3.jpg")
print("Picturea taken")
