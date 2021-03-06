package game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import level.Level;
import level.Sprite;
import movable.CharControls;
import movable.CharSprite;
import movable.Character;
import movable.Projectile;
import utility.Loader;

public class Play extends BasicGameState{
	private Image sheet;
	private Input input;
	private Music music;
	private boolean goToMenu;
	private Level firstArea;
	private HashMap<String, Sprite> sprites;
	private ArrayList<Projectile> bullets;
	private ArrayList<Character> characters;
	
	private static long runningTime;
	
	public Play(int state){
		runningTime = 0;
	}
	
	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException{
		input = new Input(Game.HEIGHT);
		goToMenu = false;
		
		bullets = new ArrayList<Projectile>();
		characters = new ArrayList<Character>();
		
		setInput(input);
		input.disableKeyRepeat();
		input.addListener(this);
		
		sheet = new Image(Game.SHEET_PATH);
		sheet.setFilter(Image.FILTER_NEAREST);
		
		try {
			sprites = Loader.loadSprites(Game.SPRITES_PATH, sheet);
		} catch(IOException e1){
			e1.printStackTrace();
		}
		
		Rectangle charBoundBox = new Rectangle(26, 6, 13, 53);
		
		CharControls heroControls = new CharControls(Input.KEY_A, Input.KEY_D,
					Input.KEY_W, Input.KEY_S, Input.KEY_SPACE);
		characters.add(new Character(0, 0, charBoundBox, new CharSprite(sheet, 0, 0), sprites.get("bullet"), heroControls));
		
		CharControls antiheroControls = new CharControls(Input.KEY_LEFT, Input.KEY_RIGHT,
				Input.KEY_UP, Input.KEY_DOWN, Input.KEY_RSHIFT);
		characters.add(new Character(600, 1200, charBoundBox, new CharSprite(sheet, 0, 0), sprites.get("bullet"), antiheroControls));
		

		
		String[][] areaMap = null;
		try {
			areaMap = Loader.loadTerrainMap(Game.LEVEL_PATH);
		} catch(IOException e){
			e.printStackTrace();
		}
		
		try {
			firstArea = new Level(areaMap, sprites, Loader.loadProps(Game.LEVEL_PROPS_PATH, sprites));
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		
		//load and start music
		music = new Music(Game.MUSIC_PATH);
		music.loop();
		music.pause();
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException{
		renderView(g, new Rectangle(0, 0, Game.WIDTH, Game.HEIGHT), characters.get(0));
		if(Game.SPLITSCREEN) renderView(g, new Rectangle(Game.WIDTH, 0, Game.WIDTH, Game.HEIGHT), characters.get(1));
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int d)
			throws SlickException{
		runningTime += d;
		
		for(Iterator<Projectile> it = bullets.iterator(); it.hasNext();){
			Projectile bullet = it.next();
			bullet.update(d, firstArea, characters);
			if(bullet.isOut(firstArea)) it.remove();
		}
		for(Iterator<Character> it = characters.iterator(); it.hasNext();){
			Character character = it.next();
			character.update(input, d, firstArea, characters, bullets, runningTime);
		}
		if(goToMenu){
			goToMenu = false;
			sbg.enterState(Game.MENU);
		}
	}

	@Override
	public int getID(){
		return Game.PLAY;
	}
	
	@Override
	public void keyPressed(int key, char c){
		if(key == Input.KEY_ESCAPE){
			goToMenu = true;
		}
	}
	
	public static long getTime(){
		return runningTime;
	}
	
	private void renderView(Graphics g, Rectangle rect, Character hero){
		g.setClip(rect);
		g.setColor(Color.black);
		g.fill(rect);
		
		float camX = hero.getCenteredCameraX();
		float camY = hero.getCenteredCameraY();
	
		firstArea.draw(g, camX - rect.getX(), camY - rect.getY());
		
		for(Projectile bullet : bullets){
			bullet.draw(g, camX - rect.getX(), camY - rect.getY());
		}
		
		for(Iterator<Character> it = characters.iterator(); it.hasNext();){
			Character character = it.next();
			character.draw(g, camX - rect.getX(), camY - rect.getY());
		}
	}
	
	
}
