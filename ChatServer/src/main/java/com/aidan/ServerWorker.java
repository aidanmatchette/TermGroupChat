package com.aidan;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("logoff".equalsIgnoreCase(line) || "quit".equalsIgnoreCase(line)) {
                    handleLogoff();
                    break;
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }

        }
        clientSocket.close();
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
        }
    }

    //format: "msg" "login" content...
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String msgContent = tokens[2];

        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker: workerList) {
            if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                String outMsg = "msg " + login +" " + msgContent + "\n";
                worker.send(outMsg);
            }
        }
    }

    private void handleLogoff() throws IOException{
        server.removeWorker(this);
        String logOffMsg = String.format("%s has logged off.", this.login);
        List<ServerWorker> workerList = server.getWorkerList();

        for (ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(logOffMsg);
            }
        }
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens. length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if (login.equals("guest") && password.equals("guest") || login.equals("aidan") && password.equals("aidan")) {
                String msg = "Valid login \n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in successfully: " + login);

                String onlineMsg = login + " is now Online" + "\n";
                List<ServerWorker> workerList = server.getWorkerList();

                //send current user all other online logins
                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin()) && worker.getLogin() != null) {
                        String currOnlineMsg= worker.getLogin() + " is online" + "\n";
                        send(currOnlineMsg);
                    }
                }

                //send other online users current user's status
                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "Error login \n";
                outputStream.write(msg.getBytes());
            }
        }
    }

    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
