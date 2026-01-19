import socket
from datetime import datetime

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(('0.0.0.0', 12345))
server_socket.listen(1)

print("Serverul rulează...")

while True:
    client_socket, address = server_socket.accept()
    print(f"Conexiune de la {address}")
    current_time = datetime.now().strftime("%H:%M:%S")
    client_socket.send(current_time.encode())
    client_socket.close()