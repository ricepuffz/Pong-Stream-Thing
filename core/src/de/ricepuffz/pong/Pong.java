package de.ricepuffz.pong;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Pong extends ApplicationAdapter {
	SpriteBatch batch;
	InputStuff inputStuff = new InputStuff(this);
	
	BitmapFont font;
	
	Random random = new Random();
	
	Texture paddleTexture;
	Texture ballTexture;
	Texture vertBar;
	Texture horiBar;
	
	Texture uiButton;
	
	Sound plop;
	Sound explode;
	
	int screenWidth;
	int screenHeight;
	
	
	//Game elements
	Sprite paddleL;
	Sprite paddleR;
	Sprite ball;
	
	Sprite upBar;
	Sprite downBar;
	Sprite leftBar;
	Sprite rightBar;
	
	Sprite seperator;
	
	
	private String ipAddress = "";
	private int port = 5162;
	
	private String publicAddress = "";
	
	
	private Socket socket = null;
	
	private DataInputStream dataIn = null;
	private DataOutputStream dataOut = null;
	
	private NetInputThread netInput = null;
	
	
	public static enum ClientType {
		LOCAL,
		HOST,
		CLIENT
	}
	
	public ClientType clientType = ClientType.LOCAL;
	
	
	public static enum GameMode {
		SINGLEPLAYER,
		MULTIPLAYER
	}
	
	public GameMode currentGameMode = GameMode.SINGLEPLAYER;
	
	
	// ---- UI Elements ----
	public static enum Context {
		MENU,
		GAME,
		MULTIPLAYER_MENU,
		MULTIPLAYER_WAITING_FOR_CONNECTION,
		CONNECTION_LOST
	}
	
	public Context currentContext = Context.MENU;
	
	//Menu Elements
	Sprite spButton;
	Sprite mpButton;
	
	//MultiplayerMenu Elements
	Sprite hostButton;
	Sprite joinButton;
	Sprite ipTextBox;
	
	
	
	
	private int pointsLeft = 0;
	private int pointsRight = 0;
	
	
	private boolean isGameSetup = true;
	private boolean isGameRunning = false;
	
	Vector2 ballVelocity = new Vector2();
	
	float paddleSpeed = 4F;
	float defaultBallSpeed = 4F;
	float ballSpeed = 5F;
	float ballSpeedIncrement = 0.003F;
	
	
	@Override
	public void create () {
		Gdx.input.setInputProcessor(inputStuff);
		
		batch = new SpriteBatch();
		
		
		FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.getFileHandle("font.ttf", FileType.Internal));
		FreeTypeFontParameter fontParameter = new FreeTypeFontParameter();
		fontParameter.size = 16;
		font = fontGenerator.generateFont(fontParameter);
		fontGenerator.dispose();
		
		
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		
		paddleTexture = new Texture("paddle.png");
		ballTexture = new Texture("ball.png");
		vertBar = new Texture("vertBar.png");
		horiBar = new Texture("horiBar.png");
		uiButton = new Texture("button.png");
		
		
		plop = Gdx.audio.newSound(Gdx.files.internal("plop.ogg"));
		explode = Gdx.audio.newSound(Gdx.files.internal("explode.ogg"));
		
		
		paddleL = new Sprite(paddleTexture);
		paddleR = new Sprite(paddleTexture);
		ball = new Sprite(ballTexture);
		
		upBar = new Sprite(horiBar);
		downBar = new Sprite(horiBar);
		leftBar = new Sprite(vertBar);
		rightBar = new Sprite(vertBar);
		
		
		
		//UI Sprites
		spButton = new Sprite(uiButton);
		spButton.setPosition(screenWidth / 2 - uiButton.getWidth() / 2, screenHeight / 2 + 10 - 50);
		
		mpButton = new Sprite(uiButton);
		mpButton.setPosition(screenWidth / 2 - uiButton.getWidth() / 2, screenHeight / 2 - 10 - uiButton.getHeight() - 50);
		
		
		hostButton = new Sprite(uiButton);
		hostButton.setPosition(screenWidth / 2 - uiButton.getWidth() / 2, screenHeight / 2 - uiButton.getHeight() / 2
				+ uiButton.getHeight() / 2 + 50);
		
		ipTextBox = new Sprite(uiButton);
		ipTextBox.setScale(2, 1);
		ipTextBox.setPosition(screenWidth / 2 - uiButton.getWidth() / 2, screenHeight / 2 - uiButton.getHeight() / 2
				- 40);
		
		joinButton = new Sprite(uiButton);
		joinButton.setPosition(screenWidth / 2 - uiButton.getWidth() / 2, screenHeight / 2 - uiButton.getHeight() / 2
				- uiButton.getHeight() - 60);
		
		
		
		
		seperator = new Sprite(vertBar);
		seperator.setAlpha(0.2F);
		seperator.setX(screenWidth / 2 - vertBar.getWidth() / 2);
		
		setupGame();
		
		upBar.setPosition(0, screenHeight - horiBar.getHeight());
		rightBar.setPosition(screenWidth - vertBar.getWidth(), 0);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		//Ball speed incrementing???
		if (isGameRunning)
			ballSpeed += ballSpeedIncrement;
		
		
		//Paddle movement
		if (isGameRunning) {
			if (currentGameMode == GameMode.SINGLEPLAYER || clientType == ClientType.HOST) {
				if (inputStuff.wDown())
					paddleL.translate(0, paddleSpeed);
				if (inputStuff.sDown())
					paddleL.translate(0, -paddleSpeed);
			}
			
			if (currentGameMode == GameMode.SINGLEPLAYER || clientType == ClientType.CLIENT) {
				if (inputStuff.upArrowDown())
					paddleR.translate(0, paddleSpeed);
				if (inputStuff.downArrowDown())
					paddleR.translate(0, -paddleSpeed);
			}
		}
		
		
		//Paddle screen clamping
		if (paddleL.getBoundingRectangle().overlaps(upBar.getBoundingRectangle()))
			paddleL.setY(screenHeight - paddleTexture.getHeight() - horiBar.getHeight());
		if (paddleL.getBoundingRectangle().overlaps(downBar.getBoundingRectangle()))
			paddleL.setY(horiBar.getHeight());
		
		if (paddleR.getBoundingRectangle().overlaps(upBar.getBoundingRectangle()))
			paddleR.setY(screenHeight - paddleTexture.getHeight() - horiBar.getHeight());
		if (paddleR.getBoundingRectangle().overlaps(downBar.getBoundingRectangle()))
			paddleR.setY(horiBar.getHeight());
			
		
		//Ball movement
		if (isGameRunning)
			ball.translate(ballVelocity.x * ballSpeed, ballVelocity.y * ballSpeed);
		
		
		
		//Ball-Paddle collision logic
		if (ball.getBoundingRectangle().overlaps(paddleL.getBoundingRectangle())) {
			ballVelocity.x = -ballVelocity.x;
			ball.setX(paddleL.getX() + paddleTexture.getWidth());
			
			float paddleMiddleOffset = (ball.getY() + ballTexture.getHeight() / 2) - (paddleL.getY() + paddleTexture.getHeight() / 2);
			ballVelocity.y = paddleMiddleOffset / (paddleTexture.getHeight() / 2) + random.nextFloat() * 0.1F;
			
			plop.play();
		}
		
		if (ball.getBoundingRectangle().overlaps(paddleR.getBoundingRectangle())) {
			ballVelocity.x = -ballVelocity.x;
			ball.setX(paddleR.getX() - ballTexture.getWidth());
			
			float paddleMiddleOffset = (ball.getY() + ballTexture.getHeight() / 2) - (paddleR.getY() + paddleTexture.getHeight() / 2);
			ballVelocity.y = paddleMiddleOffset / (paddleTexture.getHeight() / 2)  + random.nextFloat() * 0.1F;
			
			plop.play();
		}
		
		
		//Ball-Top-Down collision logic
		if (ball.getBoundingRectangle().overlaps(upBar.getBoundingRectangle())) {
			ballVelocity.y = -ballVelocity.y;
			plop.play();
		}
		if (ball.getBoundingRectangle().overlaps(downBar.getBoundingRectangle())) {
			ballVelocity.y = -ballVelocity.y;
			plop.play();
		}
		
		//Ball-Left-Right collision logic
		if (ball.getBoundingRectangle().overlaps(leftBar.getBoundingRectangle())) {
			if (isGameRunning) {
				pointsRight++;
			}
			
			if (isGameRunning)
				explode.play();
			
			isGameRunning = false;
		}
		if (ball.getBoundingRectangle().overlaps(rightBar.getBoundingRectangle())) {
			if (isGameRunning) {
				pointsLeft++;
			}
			
			if (isGameRunning)
				explode.play();
			
			isGameRunning = false;
		}
		
		
		inputStuff.backspaceTicker();
		
		
		//Send game state to client if host
		if (currentContext == Context.GAME && clientType == ClientType.HOST) {
			try {
				dataOut.writeUTF("ballpos " + ball.getX() + " " + ball.getY());
				dataOut.writeUTF("ballvel " + ballVelocity.x + " " + ballVelocity.y);
				
				dataOut.writeUTF("paddleL " + paddleL.getY());
				
				dataOut.writeUTF("points " + pointsLeft + " " + pointsRight);
				
				dataOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (currentContext == Context.GAME && clientType == ClientType.CLIENT) {
			try {
				dataOut.writeUTF("paddleR " + paddleR.getY());
				
				dataOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		batch.begin();
		
		
		GlyphLayout layout;
		
		//Game
		switch (currentContext) {
			case GAME:
				seperator.draw(batch);
				
				font.getData().setScale(3);
				font.setColor(1, 1, 1, 0.5F);
				font.draw(batch, Integer.toString(pointsRight), screenWidth / 2 + 20, screenHeight - horiBar.getHeight() - 20);
				
				layout = new GlyphLayout(font, Integer.toString(pointsLeft));
				font.draw(batch, Integer.toString(pointsLeft), screenWidth / 2 - 20 - layout.width, screenHeight - horiBar.getHeight() - 20);
				
				ball.draw(batch);
				paddleL.draw(batch);
				paddleR.draw(batch);
				
				upBar.draw(batch);
				downBar.draw(batch);
				leftBar.draw(batch);
				rightBar.draw(batch);
				
				break;
			case MENU:
				font.getData().setScale(3);
				font.setColor(1, 1, 1, 1);
				
				layout = new GlyphLayout(font, "Pong");
				
				font.draw(batch, "Pong", screenWidth / 2 - layout.width / 2, 400);
				
				
				spButton.draw(batch);
				font.getData().setScale(2);
				font.setColor(0, 0, 0, 1);
				
				layout = new GlyphLayout(font, "Singleplayer");
				
				font.draw(batch, "Singleplayer", spButton.getX() + uiButton.getWidth() / 2 - layout.width / 2,
						spButton.getY() + spButton.getHeight() / 2 + layout.height / 2);
				
				
				mpButton.draw(batch);
				
				layout = new GlyphLayout(font, "Multiplayer");
				
				font.draw(batch, "Multiplayer", mpButton.getX() + uiButton.getWidth() / 2 - layout.width / 2,
						mpButton.getY() + mpButton.getHeight() / 2 + layout.height / 2);
				
				break;
			case MULTIPLAYER_MENU:
				font.getData().setScale(2);
				font.setColor(1, 1, 1, 1);
				
				layout = new GlyphLayout(font, publicAddress + ":" + port);
				font.draw(batch, publicAddress + ":" + port, screenWidth / 2 - layout.width / 2, hostButton.getY() + hostButton.getHeight()
						+ layout.height + 20);
				
				
				font.getData().setScale(2);
				font.setColor(0, 0, 0, 1);
				
				joinButton.draw(batch);
				hostButton.draw(batch);
				ipTextBox.draw(batch);
				
				layout = new GlyphLayout(font, "Host");
				font.draw(batch, "Host", hostButton.getX() + uiButton.getWidth() / 2 - layout.width / 2,
						hostButton.getY() + hostButton.getHeight() / 2 + layout.height / 2);
				
				layout = new GlyphLayout(font, "Join");
				font.draw(batch, "Join", joinButton.getX() + uiButton.getWidth() / 2 - layout.width / 2,
						joinButton.getY() + joinButton.getHeight() / 2 + layout.height / 2);
				
				String toDraw = ipAddress.equalsIgnoreCase("") ? "Enter IP Address.." : ipAddress;
				layout = new GlyphLayout(font, toDraw);
				
				float c = ipAddress.equalsIgnoreCase("") ? 0.6F : 0;
				
				font.setColor(c, c, c, 1);
				font.draw(batch, toDraw, ipTextBox.getX() + uiButton.getWidth() / 2 - layout.width / 2,
						ipTextBox.getY() + ipTextBox.getHeight() / 2 + layout.height / 2);
				
				break;
			case MULTIPLAYER_WAITING_FOR_CONNECTION:
				font.getData().setScale(4);
				font.setColor(1, 1, 1, 1);
				
				layout = new GlyphLayout(font, "Connecting...");
				
				font.draw(batch, "Connecting...", screenWidth / 2 - layout.width / 2, screenHeight / 2 + layout.height / 2);
				
				break;
			case CONNECTION_LOST:
				font.getData().setScale(4);
				font.setColor(1, 1, 1, 1);
				
				layout = new GlyphLayout(font, "Connection Lost!");
				font.draw(batch, "Connection Lost!", screenWidth / 2 - layout.width / 2, screenHeight / 2 + layout.height);
				
				font.getData().setScale(2);
				
				GlyphLayout layout2 = new GlyphLayout(font, "Please restart the game");
				font.draw(batch, "Please restart the game", screenWidth / 2 - layout2.width / 2, screenHeight / 2 - layout2.height);
				
				break;
		}
		
		batch.end();
	}
	
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		
		paddleTexture.dispose();
		ballTexture.dispose();
		
		vertBar.dispose();
		horiBar.dispose();
		
		uiButton.dispose();
		
		if (netInput != null)
			netInput.stopThisPLEASE();
	}
	
	
	public void setupGame() {
		if (clientType == ClientType.HOST) {
			try {
				dataOut.writeUTF("setupgame");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		paddleL.setPosition(30, screenHeight / 2 - paddleTexture.getHeight() / 2);
		paddleR.setPosition(screenWidth - 30 - paddleTexture.getWidth(), screenHeight / 2 - paddleTexture.getHeight() / 2);
		
		ball.setPosition(screenWidth / 2 - ballTexture.getWidth() / 2, screenHeight / 2 - ballTexture.getHeight() / 2);
		ballSpeed = defaultBallSpeed;
		
		isGameSetup = true;
	}
	
	
	public void startGame() {
		if (clientType == ClientType.HOST) {
			try {
				dataOut.writeUTF("startgame");
				dataOut.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		isGameRunning = true;
		
		ballVelocity.x = random.nextBoolean() ? 1 : -1;
		ballVelocity.y = 0;
		
		isGameSetup = false;
	}
	
	
	public boolean clickCollides(int x, int y, Rectangle r) {
		if (x > r.x && x < r.x + r.width && y > r.y && y < r.y + r.height)
			return true;
		return false;
	}
	
	public void leftClick(int x, int y) {
		if (currentContext == Context.MENU) {
			if (clickCollides(x, y, spButton.getBoundingRectangle())) {
				currentContext = Context.GAME;
				return;
			}
			if (clickCollides(x, y, mpButton.getBoundingRectangle())) {
				currentContext = Context.MULTIPLAYER_MENU;
				
				publicAddress = retrievePublicAddress();
				
				return;
			}
		} else if (currentContext == Context.MULTIPLAYER_MENU) {
			if (clickCollides(x, y, hostButton.getBoundingRectangle())) {
				hostGame();
				return;
			}
			if (clickCollides(x, y, joinButton.getBoundingRectangle())) {
				joinGame();
				return;
			}
		}
	}
	
	
	public void mpMenuType(char character) {
		ipAddress += character;
	}
	public void mpMenuBackspace() {
		if (ipAddress.length() > 0)
			ipAddress = ipAddress.substring(0, ipAddress.length() - 1);
	}
	
	
	private void hostGame() {
		System.out.println("Starting host...");
		
		currentGameMode = GameMode.MULTIPLAYER;
		clientType = ClientType.HOST;
		currentContext = Context.MULTIPLAYER_WAITING_FOR_CONNECTION;
		
		new Thread(new HostThread(this)).start();
	}
	
	private void joinGame() {
		System.out.println("Starting client...");
		
		currentGameMode = GameMode.MULTIPLAYER;
		clientType = ClientType.CLIENT;
		currentContext = Context.MULTIPLAYER_WAITING_FOR_CONNECTION;
		
		new Thread(new ConnectThread(this, ipAddress, port)).start();
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
		System.out.println(socket);
		
		try {
			dataIn = new DataInputStream(socket.getInputStream());
			dataOut = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		netInput = new NetInputThread(this, dataIn);
		new Thread(netInput).start();
		
		currentContext = Context.GAME;
	}
	
	private String retrievePublicAddress() {
		String address = "";
		
		try {
			URL url = new URL("http://bot.whatismyipaddress.com/");
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			
			address = reader.readLine();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(address);
		return address;
	}
	
	
	public boolean isGameSetup() {
		return isGameSetup;
	}
	
	public boolean isGameRunning() {
		return isGameRunning;
	}
	
	public Context currentContext() {
		return currentContext;
	}

	public int port() {
		return port;
	}
	
	public void setPoints(int left, int right) {
		pointsLeft = left;
		pointsRight = right;
	}
}
