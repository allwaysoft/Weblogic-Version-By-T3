import os
import socket
import time

hello = b't3 10.3.6\nAS:255\nHL:19\n\n'

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.settimeout(5)
sock.connect(('127.0.0.1', 7001))
sock.send(hello)
time.sleep(1)
resp1 = sock.recv(1024)

print(resp1)
