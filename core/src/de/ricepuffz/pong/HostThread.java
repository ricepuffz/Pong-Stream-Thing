package de.ricepuffz.pong;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HostThread implements Runnable {
	private Pong pong;
	
	
	public HostThread(Pong pong) {
		this.pong = pong;
	}
	
	
	@Override
	public void run() {
		Socket socket = null;
		
		
		try {
			ServerSocket serverSocket = new ServerSocket(pong.port());
			socket = serverSocket.accept();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pong.setSocket(socket);
	}
}
