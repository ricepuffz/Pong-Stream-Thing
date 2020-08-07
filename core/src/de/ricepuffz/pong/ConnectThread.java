package de.ricepuffz.pong;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectThread implements Runnable {
	private Pong pong;
	
	private String address;
	private int port;
	
	public ConnectThread(Pong pong, String address, int port) {
		this.pong = pong;
		this.address = address;
		this.port = port;
	}
	
	@Override
	public void run() {
		Socket socket = null;
		
		try {
			socket = new Socket(address, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		pong.setSocket(socket);
	}
}
