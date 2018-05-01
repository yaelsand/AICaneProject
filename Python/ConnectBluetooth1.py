import bluetooth
from subprocess import call
import time
loop = True
while loop:
    result = bluetooth.lookup_name('08:3D:88:59:21:8D', timeout=20)
    if (result == None):
        print("not detected")
    else:
        print("Button found")
    break


time.sleep(5)

server_sock=bluetooth.BluetoothSocket( bluetooth.L2CAP )
port = 0x1001
server_sock.bind(("",port))
server_sock.listen(1)
print("listening on port %d" % port)
bd_addr = '08:3D:88:59:21:8D'
service_matches = bluetooth.find_service( name = None, uuid = None, address = bd_addr)

if len(service_matches) == 0:
    print("cound't find service")
    sys.exit
first_match = service_matches[0]
port = first_match["port"]
name = first_match["name"]
host = first_match["host"]
protocol = first_match["protocol"]

print(port)
print(name)
print(host)
print(protocol)

command0 = "sdptool browse 08:3D:88:59:21:8D"
command= "ussp-push 08:3D:88:59:21:8D@5 /home/pi/Downloads/child.jpg child.jpg"
call([command], shell=True)


server_sock.close()
