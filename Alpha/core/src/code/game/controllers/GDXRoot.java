/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter. 
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
 package code.game.controllers;

import code.assets.AssetDirectory;
import code.game.display.*;
import code.game.models.Save;
import code.game.views.GameCanvas;
import code.util.ScreenListener;
import com.badlogic.gdx.*;

/**
 * Root class for a LibGDX.  
 * 
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However, 
 * those classes are unique to each platform, while this class is the same across all 
 * plaforms. In addition, this functions as the root class all intents and purposes, 
 * and you would draw it as a root class in an architecture specification.  
 */
public class GDXRoot extends Game implements ScreenListener {
	//TODO: If other scenes are necessary, it will be done here, otherwise not much should change

	/** AssetManager to load game assets (textures, sounds, etc.) */
	AssetDirectory directory;
	/** Drawing context to display graphics (VIEW CLASS) */
	private GameCanvas canvas;
	/** Player mode for the asset loading screen (CONTROLLER CLASS) */
	private LoadingMode loading;
	/** Player mode for the menu screen (CONTROLLER CLASS)*/
	private MainMenuMode menu;
	/** Player mode for the level selection screen (CONTROLLER CLASS)*/
	private LevelSelectMode levelselect;
	/** Player mode for the gamemenu screen (CONTROLLER CLASS)*/
	private GameMenuMode gamemenu;
	/** Player mode for the options menu (CONTROLLER CLASS)*/
	private OptionsMode options;
	/** Player mode for the the game proper (CONTROLLER CLASS) */
	private int current;
	/** List of all WorldControllers */
	private GameController controller;
	/** Sound system for music and audio (CONTROLLER CLASS) */
	private SoundController sound;
	
	/**
	 * Creates a new game from the configuration settings.
	 *
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() { }

	/** 
	 * Called when the Application is first created.
	 * 
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		sound = new SoundController();
		canvas  = new GameCanvas();
		loading = new LoadingMode("assets.json",canvas,1);

		// Initialize the three game worlds
		controller = new GameController();
		current = 0;
		loading.setScreenListener(this);
		setScreen(loading);
	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to gamemenu().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		if(controller!=null && controller.initialized()) {
			controller.dispose();

		}
		controller = null;
		//loading.dispose();
		if(menu != null) {
			menu.dispose();
			menu = null;
		}
		if(levelselect != null) {
			levelselect.dispose();
			levelselect = null;
		}
		if(gamemenu != null) {
			gamemenu.dispose();
			gamemenu = null;
		}

		//TODO dispose others if needed
		if(canvas != null) {
			canvas.dispose();
			canvas = null;
		}
		if (sound != null) {
			sound.dispose();
			sound = null;
		}
	
		// Unload all of the resources
		// Unload all of the resources
		if (directory != null) {
			directory.unloadAssets();
			directory.dispose();
			directory = null;
		}
		super.dispose();
	}
	
	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		canvas.clear();
		canvas.setSize(Math.max(width,1280),Math.max(height,720));
		canvas.resetCamera();
		canvas.resize();
		if(controller != null) controller.resize(width, height);
		super.resize(width,height);
	}
	
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) {
		if (screen == loading) {
			directory = loading.getAssets();
			controller.gatherAssets(directory);
			//controller.setScreenListener(this);

			controller.setCanvas(canvas);
			//TODO:replace with save value
			//resize(1280,720);

			sound.gatherAssets(directory);
			controller.setSoundController(sound);

			//make other modes with assets
			menu = new MainMenuMode(directory, canvas, sound);
			levelselect = new LevelSelectMode(directory, canvas, sound);
			gamemenu = new GameMenuMode(directory, canvas, sound);
			options = new OptionsMode(directory, canvas, sound);

			//set listeners
			controller.setScreenListener(this);
			menu.setScreenListener(this);
			levelselect.setScreenListener(this);
			gamemenu.setScreenListener(this);
			options.setScreenListener(this);

			loading.dispose();
			loading = null;

			//menu.activateInputProcessor(true);
			//Load save
			Save save = controller.getSave();
			levelselect.setSave(save);
			gamemenu.setSave(save);
			options.setSave(save);

			setScreen(menu);
			controller.setOptions();
		}
		else if (screen == menu){
			//menu.activateInputProcessor(false);
			switch (exitCode){
				case MainMenuMode.START:
					levelselect.reset();
					levelselect.setHighlightedIndex(menu.didMouseEnter());
					setScreen(levelselect);
					break;
				case MainMenuMode.GUIDE: //TODO go to guide
					break;
				case MainMenuMode.OPTIONS:
					options.setExitCode(0);
					setScreen(options);
					break;
				case MainMenuMode.QUIT:
					// We quit the main application
					dispose();
					Gdx.app.exit();
					break;
			}
		}
		else if (screen == levelselect){
			controller.reset();
			//levelselect.activateInputProcessor(false);
			switch(exitCode) {
				case LevelSelectMode.EXIT_LEVEL:
					controller.populateLevel(levelselect.getLevelSelected());
					levelselect.reset();
					setScreen(controller);
					break;
				case LevelSelectMode.EXIT_MENU:
					menu.reset();
					levelselect.reset();
					setScreen(menu);
					break;
			}
		}
		else if (screen == controller){
			switch (exitCode){
				case GameController.EXIT_WIN:
					gamemenu.setLevelAvailable(levelselect.levelAvailable());
					levelselect.advanceSave();
					controller.writeSave();
					gamemenu.setMode(GameMenuMode.Mode.WIN);
					setScreen(gamemenu);
					break;
				case GameController.EXIT_LOSE:
					gamemenu.setMode(GameMenuMode.Mode.LOSE);
					setScreen(gamemenu);
					break;
				case GameController.EXIT_PAUSE:
					gamemenu.setMode(GameMenuMode.Mode.PAUSE);
					setScreen(gamemenu);
					break;
			}

		} else if (screen == gamemenu){
			switch (exitCode){
				case GameMenuMode.CONT:
					switch(gamemenu.getMode()){
						case PAUSE:
							gamemenu.reset();
							controller.resume();
							//levelselect.updateSave(gamemenu.getSave());
							//controller.updateSave(gamemenu.getSave());
							controller.writeSave();
							setScreen(controller);
							break;
						case WIN:
							gamemenu.reset();
							controller.reset();
							levelselect.setNextLevel();
							controller.populateLevel(levelselect.getLevelSelected());
							setScreen(controller);
							break;
					}
					break;
				case GameMenuMode.RESTART:
					gamemenu.reset();
					controller.populateLevel(levelselect.getLevelSelected());
					controller.resume();
					setScreen(controller);
					break;
				case GameMenuMode.OPTIONS:
					options.setExitCode(1);
					setScreen(options);
					break;
				case GameMenuMode.QUIT:
					gamemenu.reset();
					controller.resume();
					controller.reset();
					setScreen(levelselect);
					break;
			}
		} else if (screen == options){
			//controller.writeSave();
			controller.setOptions();
			//controller.updateSaveValues();
			switch(exitCode){
				case OptionsMode.MAINMAIN:
					menu.reset();
					setScreen(menu);
					options.reset();
					break;
				case OptionsMode.GAMEMNEU:
					gamemenu.reset();
					setScreen(gamemenu);
					options.reset();
					break;
			}
		}
	}

}
