import bluetooth
import pprint
from subprocess import call
from time import sleep

MAC_ID = "08:3D:88:59:21:8D"
CHANNEL = "12"

def receiveMessages():
  server_sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
  
  port = 12
  server_sock.bind(("",port))
  server_sock.listen(12)
  
  client_sock,address = server_sock.accept()
  print("Accepted connection from " + str(address))
  
  data = client_sock.recv(1024)
  print("received [%s]" % data)
  
  client_sock.close()
  server_sock.close()
  
def sendMessageTo(targetBluetoothMacAddress):
  port = 12
  sock=bluetooth.BluetoothSocket( bluetooth.RFCOMM )
  sock.connect((targetBluetoothMacAddress, port))
  #command0 = "sdptool browse 08:3D:88:59:21:8D"
  #command= "ussp-push 08:3D:88:59:21:8D@12 /home/pi/child.jpg child.jpg"
  command = "obexftp --nopath --noconn --uuid none --bluetooth 08:3D:88:59:21:8D --channel 12 -p tuxcase.jpg"
  #command= "obexftp --nopath --noconn --uuid none --bluetooth "+ MAC_ID + " --channel 12 -p /home/pi/child.jpg"
  call([command], shell=True)
  sock.close()
  
def lookUpNearbyBluetoothDevices():
  nearby_devices = bluetooth.discover_devices()
  for bdaddr in nearby_devices:
    print(str(bluetooth.lookup_name( bdaddr )) + " [" + str(bdaddr) + "]")
    
    
sendMessageTo(MAC_ID)

 #devices = bluetooth.discover_devices()
#service = bluetooth.find_service(address='08:3D:88:59:21:8D')
#pprint.pprint(service)
#OBEX Object Push
