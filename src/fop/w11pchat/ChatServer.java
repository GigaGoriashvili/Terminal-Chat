package fop.w11pchat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;

public class ChatServer {
    ServerSocket serverSocket;
    Socket clientSocket;
    public ChatServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        while (true) {
            try {
                System.out.println(String.valueOf(LocalTime.now()) + " Server is waiting on port " + serverSocket.getLocalPort());
                this.clientSocket = this.serverSocket.accept();
                System.out.println(String.valueOf(LocalTime.now()) + " Client has connected to the server");

                //We should create threads for ability to listen all the connected users simultaneously
                ClientThread clientThread = new ClientThread(this.clientSocket);
                clientThread.start();
            } catch (IOException e) {
                try {
                    serverSocket.close();
                } catch (IOException f) {
                    f.getStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) throws IOException {
        ServerSocket socket = new ServerSocket(3000);
        ChatServer server = new ChatServer(socket);
    }
}
    
