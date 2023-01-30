package com.aidan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient {
    private final String serverName;
    private final int port;
    private Socket socket;
	private InputStream serverIn;
	private OutputStream serverOut;

    public ChatClient(String serverName, int port) {
        this.serverName = serverName;
        this.port = port;
	}

	public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);
        if(client.connect()) {
            System.out.printf("Connection successfull");
            client.login("guest", "guest");
        } else {
            System.err.println("Connection failed");
        }
    }

	private void login(String login, String password) throws IOException {
        String cmd = "login" + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());
	}

	private boolean connect() {
        try {
			this.socket = new Socket(this.serverName, port);
            System.out.println("The client port: " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            return true;
		} catch (IOException e) {
			e.printStackTrace();
            return false;
		}
	}
}
