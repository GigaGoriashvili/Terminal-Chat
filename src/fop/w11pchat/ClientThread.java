package fop.w11pchat;

import java.io.*;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ClientThread extends Thread{
    private static final List<ClientThread> clientThreadList = new ArrayList<>();

    private static String[] facts = {
            "Some penguins can reach speeds of 22 miles an hour.",
            "The deepest dive ever made by a penguin was more than 6,000 feet underwater.",
            "The world's oldest penguin is estimated to be an impressive 40 years old.",
            "Emperor penguins huddle to keep warm.",
            "Penguins can't fly, but they CAN become airborne. Some can leap as high as nine feet!",
            "There was once a \"mega\" penguin that stood 6.5 feet tall and weighed more than 250 pounds.",
            "Penguins can kill KIU students, but not the creator of this chat :)"
    };
    private static String greeting = "Hello! Welcome to the chatroom." +
            "\n" +
            "Instructions:" +
            "\n" +
            "1. Simply type the message to send broadcast to all active clients" +
            "\n" +
            "2. Type @username<space>yourmessage' without quotes to send message to desired client" +
            "\n" +
            "3. Type 'WHOIS' without quotes to see list of active clients" +
            "\n" +
            "4. Type 'LOGOUT' without quotes to logoff from server " +
            "\n" +
            "5. Type 'PENGU' without quotes to request a random penguin fact";
    private Socket socket;
    private BufferedReader bufferedReader;
    private PrintStream printStream;
    private String userName;
    private String timeAtJoin;


    public ClientThread(Socket socket) {
        try {
            this.socket = socket;
            InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            printStream = new PrintStream(socket.getOutputStream());
            String firstInfo = bufferedReader.readLine();
            String[] arr = firstInfo.split(" ");
            userName = arr[0];
            timeAtJoin = arr[1];spreadMessage(userName + " has joined the chat", false);
            greetUser();
            clientThreadList.add(this);
         } catch (IOException e) {
            leaveChat();
        }
    }
    //getters for sources
    public static synchronized List<ClientThread> getClientThreadList() {
        return clientThreadList;
    }

    public static synchronized String[] getFacts() {
        return facts;
    }

    public static synchronized String getGreeting() {
        return greeting;
    }

    @Override
    public void run() {
        String message;
        while (true) {
            try {
                message = bufferedReader.readLine();
                if (message.charAt(0) == '@') {
                    int spaceIndex = message.indexOf(" ");
                    if (spaceIndex != 1 && spaceIndex != -1) {
                        String name = message.substring(1, spaceIndex);
                        String directMessage = message.substring(spaceIndex + 1);
                        sendDM(name, directMessage);
                    } else {
                        printStream.println("Please indicate the name of the user");
                        printStream.flush();
                    }
                    
                } else if (message.equals("WHOIS")) {
                    sendUsersList();

                } else if (message.equals("LOGOUT")) {
                    leaveChat();
                    return;

                } else if (message.equals("PENGU")) {
                    sendFact();
                } else {
                    spreadMessage(message, true);
                }

            } catch(IOException e) {
                leaveChat();
                break;
            }
        }
    }

    private void leaveChat() {
        try {
            getClientThreadList().remove(this);
            spreadMessage(userName + " left the chat", false);
            if (socket != null) {
                socket.close();
            }

            if (bufferedReader != null) {
                bufferedReader.close();
            }

            if (printStream != null) {
                printStream.close();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    //This method spreads the messages for all the users
    private void spreadMessage(String message, boolean isOrdinary) {
        String localTime = String.valueOf(LocalTime.now());
        if (isOrdinary) {
            for (ClientThread clientThread : getClientThreadList()) {
                if (clientThread.equals(this)) {
                    printStream.println(localTime + " You: " + message);
                    printStream.flush();
                } else {
                    clientThread.printStream.println(localTime + " " + this.userName + ": " + message);
                    clientThread.printStream.flush();
                }
            }
        } else {
            for (ClientThread clientThread : getClientThreadList()) {
                clientThread.printStream.println(localTime + " " + message);
                clientThread.printStream.flush();
            }
        }
    }

    //This method is used to send direct messages
    private void sendDM(String name, String message) {
        String localTime = String.valueOf(LocalTime.now());
        for (ClientThread clientThread : getClientThreadList()) {
            if (Objects.equals(clientThread.userName, name)) {
                clientThread.printStream.println(localTime + " " + userName + ": (DM) " + message);
                clientThread.printStream.flush();
                return;
            }
        }
        printStream.println(localTime + " User, named " + name + ", is not in chat!");
    }

    //This method is used for sending the list of users to one who needs it
    private void sendUsersList() {
        List<ClientThread> clientThreadList= getClientThreadList();
        ClientThread clientThread;
        String instructions = "";
        String localTime = String.valueOf(LocalTime.now());
        for (int i = 1; i < clientThreadList.size(); i++) {
            clientThread = clientThreadList.get(i - 1);
            instructions += i + ") " + clientThread.userName + " since " + clientThread.timeAtJoin + "\n";
        }
        clientThread = clientThreadList.get(clientThreadList.size() - 1);
        instructions += clientThreadList.size() + ") " + clientThread.userName + " since " + clientThread.timeAtJoin;

        printStream.println("List of the users connected at " + localTime + "\n" + instructions);
        printStream.flush();
    }

    //This method sends the fact about penguins randomly
    private void sendFact() {
        Random rand = new Random();
        int randomNumber = rand.nextInt(facts.length);
        String fact = getFacts()[randomNumber];
        spreadMessage(fact, false);
    }

    private void greetUser() {
        printStream.println(getGreeting());
        printStream.flush();
    }
}