import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class PortForwarder {

    private String remoteHost;
    private int remotePort;
    private int localPort;

    public PortForwarder(String remoteHost, int remotePort, int localPort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.localPort = localPort;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(localPort)) {
            System.out.println("Port Forwarder started.");
            System.out.println("Listening on port " + localPort + " and forwarding to " + remoteHost + ":" + remotePort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());

                // Handle each connection in a new thread
                new Thread(new Forwarder(clientSocket, remoteHost, remotePort)).start();
            }

        } catch (IOException e) {
            System.err.println("Error starting port forwarder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class Forwarder implements Runnable {
        private Socket clientSocket;
        private String remoteHost;
        private int remotePort;

        public Forwarder(Socket clientSocket, String remoteHost, int remotePort) {
            this.clientSocket = clientSocket;
            this.remoteHost = remoteHost;
            this.remotePort = remotePort;
        }

        @Override
        public void run() {
            try (
                Socket remoteSocket = new Socket(remoteHost, remotePort);
                InputStream clientIn = clientSocket.getInputStream();
                OutputStream clientOut = clientSocket.getOutputStream();
                InputStream remoteIn = remoteSocket.getInputStream();
                OutputStream remoteOut = remoteSocket.getOutputStream();
            ) {
                // Thread to forward data from client to remote
                Thread forwardClientToRemote = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = clientIn.read(buffer)) != -1) {
                            remoteOut.write(buffer, 0, bytesRead);
                            remoteOut.flush();
                        }
                    } catch (IOException e) {
                        // Silent catch: connections may close normally
                    }
                });

                // Thread to forward data from remote to client
                Thread forwardRemoteToClient = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = remoteIn.read(buffer)) != -1) {
                            clientOut.write(buffer, 0, bytesRead);
                            clientOut.flush();
                        }
                    } catch (IOException e) {
                        // Silent catch: connections may close normally
                    }
                });

                forwardClientToRemote.start();
                forwardRemoteToClient.start();

                // Wait for both threads to finish
                forwardClientToRemote.join();
                forwardRemoteToClient.join();

            } catch (IOException | InterruptedException e) {
                System.err.println("Connection error: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    // Ignore
                }
                System.out.println("Connection closed: " + clientSocket.getInetAddress());
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java PortForwarder <remote_host> <remote_port> <local_port>");
            System.out.println("Example: java PortForwarder example.com 80 8080");
            return;
        }

        String remoteHost = args[0];
        int remotePort;
        int localPort;

        try {
            remotePort = Integer.parseInt(args[1]);
            localPort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Port numbers must be integers.");
            return;
        }

        PortForwarder forwarder = new PortForwarder(remoteHost, remotePort, localPort);
        forwarder.start();
    }
}
