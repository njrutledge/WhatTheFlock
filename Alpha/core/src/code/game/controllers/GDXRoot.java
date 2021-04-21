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
import code.game.display.LevelSelectMode;
import code.game.display.LoadingMode;
import code.game.display.MainMenuMode;
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
	/** Player mode for the the game proper (CONTROLLER CLASS) */
	private int current;
	/** List of all WorldControllers */
	private GameController controller;
	
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
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		if(controller.initialized()) {
			controller.dispose();
			controller = null;
		}
		//loading.dispose();
		if(menu != null) {
			menu.dispose();
			menu = null;
		}
		if(levelselect != null) {
			levelselect.dispose();
			levelselect = null;
		}
		//TODO dispose others if needed
		if(canvas != null) {
			canvas.dispose();
			canvas = null;
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
		canvas.resize();
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
			controller.initGrid();

			//make other modes with assets
			menu = new MainMenuMode(directory, canvas);
			levelselect = new LevelSelectMode(directory, canvas);

			//set listeners
			controller.setScreenListener(this);
			menu.setScreenListener(this);
			levelselect.setScreenListener(this);

			loading.dispose();
			loading = null;

			//menu.activateInputProcessor(true);
			setScreen(menu);
			//setScreen(levelselect);
		}
		else if (screen == menu){
			//menu.activateInputProcessor(false);
			menu.reset();
			switch (exitCode){
				case MainMenuMode.START: //TODO go to level select
					//controller.reset();
					levelselect.reset();
					//levelselect.activateInputProcessor(true);
					setScreen(levelselect);
					break;
				case MainMenuMode.GUIDE: //TODO go to guide
					break;
				case MainMenuMode.OPTIONS: //TODO go to options
					break;
				case MainMenuMode.QUIT: //TODO go to quit
					// We quit the main application
					dispose();
					Gdx.app.exit();
					break;
			}
		}
		else if (screen == levelselect){
			controller.reset();
			//levelselect.activateInputProcessor(false);
			if(exitCode == 0){
				controller.populateLevel(levelselect.getLevelSelected());
				levelselect.reset();
				setScreen(controller);
			}
		}
		else if (screen == controller){
			switch (exitCode){
				case GameController.EXIT_NEXT:
					menu.reset();
					setScreen(menu);
					break;
				case GameController.EXIT_PREV:
					menu.reset();
					setScreen(menu);
					break;
				case GameController.EXIT_QUIT:
					//go back to menu select screen
					menu.reset();
					setScreen(menu);
					break;
			}
		}
	}

}
