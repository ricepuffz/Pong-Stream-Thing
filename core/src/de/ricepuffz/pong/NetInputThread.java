package de.ricepuffz.pong;

import java.io.DataInputStream;
import java.io.IOException;

import com.badlogic.gdx.math.Vector2;

public class NetInputThread implements Runnable {
	private Pong pong;
	private boolean isStuffStillAlive = true;
	private DataInputStream dataIn;
	
	
	public NetInputThread(Pong pong, DataInputStream dataIn) {
		this.pong = pong;
		this.dataIn = dataIn;
	}

	
	@Override
	public void run() {
		while (isStuffStillAlive) {
			String input = "";
			
			try {
				input = dataIn.readUTF();
			} catch (IOException e) {
				pong.currentContext = Pong.Context.CONNECTION_LOST;
			}
			
			if (!input.equalsIgnoreCase("")) {
				String[] segments = input.split(" ");
				
				switch (segments[0]) {
					case "setupgame":
						pong.setupGame();
						break;
					case "startgame":
						pong.startGame();
						break;
					case "ballpos":
						pong.ball.setPosition(Float.parseFloat(segments[1]), Float.parseFloat(segments[2]));
						break;
					case "ballvel":
						pong.ballVelocity.x = Float.parseFloat(segments[1]);
						pong.ballVelocity.y = Float.parseFloat(segments[2]);
						break;
					case "paddleL":
						pong.paddleL.setY(Float.parseFloat(segments[1]));
						break;
					case "paddleR":
						pong.paddleR.setY(Float.parseFloat(segments[1]));
						break;
					case "points":
						pong.setPoints(Integer.parseInt(segments[1]), Integer.parseInt(segments[2]));
				}
			}
		}
	}
	
	
	public void stopThisPLEASE() {
		isStuffStillAlive = false;
	}
}
