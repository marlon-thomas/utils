## Description

Simple port forwarding console application written in Java. Useful for testing networked applications

## Overview
1. Listening on Local Port: The application listens for incoming connections on a specified local port.
2. Connecting to Remote Host: Upon receiving a connection, it establishes a connection to the remote host and port.
3. Data Forwarding: It forwards data between the local client and the remote server bidirectionally.
4. Handling Multiple Connections: Each connection is handled in a separate thread to allow multiple simultaneous connections.

## How to Run

1. **Compile the Code**:

   Save the code in a file named `PortForwarder.java` and compile it using the `javac` compiler:

   ```bash
   javac PortForwarder.java

1. **Run the Application:**:

   Use the `java` command to run the application, providing the remote host, remote port, and local port as arguments.

   ```bash
   java PortForwarder <remote_host> <remote_port> <local_port>

   #Example: To forward local port `8080` to `example.com` on port `80`:
   
   java PortForwarder example.com 80 8080
   
   