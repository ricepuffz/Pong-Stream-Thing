package de.ricepuffz.pong;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

public class InputStuff implements InputProcessor {
	Pong pong;
	
	private boolean wDown = false;
	private boolean sDown = false;
	private boolean upArrowDown = false;
	private boolean downArrowDown = false;
	
	private boolean backspaceDown = false;
	private long backspaceDownSince = -1;
	
	
	static private final String validIpChars = "1234567890.:";
	
	
	public InputStuff(Pong pong) {
		this.pong = pong;
	}
	
	
	
	public void backspaceTicker() {
		if (backspaceDown && backspaceDownSince == -1)
			backspaceDownSince = System.currentTimeMillis();
		else if (!backspaceDown)
			backspaceDownSince = -1;
		
		if (backspaceDownSince != -1 && backspaceDownSince + 500 < System.currentTimeMillis())
			pong.mpMenuBackspace();
	}
	
																
	
	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.ENTER:
				if (pong.currentContext() == Pong.Context.GAME &&
						(pong.currentGameMode == Pong.GameMode.SINGLEPLAYER || pong.clientType == Pong.ClientType.HOST)) {
					if (!pong.isGameRunning()) {
						if (pong.isGameSetup())
							pong.startGame();
						else
							pong.setupGame();
					}
				}
				break;
			case Input.Keys.W:
				wDown = true;
				break;
			case Input.Keys.S:
				sDown = true;
				break;
			case Input.Keys.UP:
				upArrowDown = true;
				break;
			case Input.Keys.DOWN:
				downArrowDown = true;
				break;
			case Input.Keys.ESCAPE:
				Gdx.app.exit();
		}
		
		if (pong.currentContext == Pong.Context.MULTIPLAYER_MENU) {
			if (keycode == Input.Keys.BACKSPACE) {
				pong.mpMenuBackspace();
				backspaceDown = true;
			}
		}
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
			case Input.Keys.W:
				wDown = false;
				break;
			case Input.Keys.S:
				sDown = false;
				break;
			case Input.Keys.UP:
				upArrowDown = false;
				break;
			case Input.Keys.DOWN:
				downArrowDown = false;
				break;
			case Input.Keys.BACKSPACE:
				backspaceDown = false;
				break;
			default:
				return false;
		}
			
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		if (pong.currentContext == Pong.Context.MULTIPLAYER_MENU) {
			String characterString = "" + character;
			
			if (validIpChars.contains(characterString))
				pong.mpMenuType(character);
		}
		
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == 0)
			pong.leftClick(screenX, Gdx.graphics.getHeight() - screenY);
		
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	
	public boolean wDown() {
		return wDown;
	}
	
	public boolean sDown() {
		return sDown;
	}

	public boolean upArrowDown() {
		return upArrowDown;
	}

	public boolean downArrowDown() {
		return downArrowDown;
	}
}
