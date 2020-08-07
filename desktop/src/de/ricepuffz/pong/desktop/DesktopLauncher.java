package de.ricepuffz.pong.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import de.ricepuffz.pong.Pong;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		
		config.resizable = false;
		
		config.width = 800;
		config.height = 500;
		
		
		new LwjglApplication(new Pong(), config);
	}
}
