package fop.w11pchat;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.Scanner;

public class ChatClient {
    Socket socket;
    BufferedReader bufferedReader;
    PrintStream printStream;
    String userName;
    String timeAtJoin;

    public ChatClient(Socket socket, String userName, String timeAtJoin) {
        try {
            this.socket = socket;
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            printStream = new PrintStream(socket.getOutputStream());
            this.userName = userName;
            this.timeAtJoin = timeAtJoin;
        } catch (IOException e) {
            closeAll();
        }
    }

    public void sendMessage() {
        printStream.println(userName + " " + timeAtJoin);
        printStream.flush();

        Scanner scanner = new Scanner(System.in);
        String message;
        while (true) {
            message = scanner.nextLine();
            printStream.println(message);
            if(message.equals("LOGOUT")){
                closeAll();
                break;
            }
            printStream.flush();
        }
    }

    public void getMessages() {
        new Thread(() -> {
            String message;
            while (true) {
                try {
                    message = bufferedReader.readLine();
                    System.out.println(message);
                    if(message.equals("LOGOUT")){
                        closeAll();
                        break;
                    }
                } catch (IOException e) {
                    closeAll();
                    break;
                }
             }
        }).start();
    }

    private void closeAll() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (printStream != null) {
                printStream.close();
            }
        } catch(IOException e) {
            e.getStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your name: ");
        String name = scanner.nextLine();
        Socket clientSocket = new Socket("localhost", 3000);
        String timeAtJoin = String.valueOf(LocalTime.now());
        System.out.println(timeAtJoin + ": connection accepted " + clientSocket.getInetAddress());
        ChatClient chatClient = new ChatClient(clientSocket, name, timeAtJoin);
        chatClient.getMessages();
        chatClient.sendMessage();
    }
}
