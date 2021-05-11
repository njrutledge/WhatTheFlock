/*
 * DesktopLauncher.java
 * 
 * LibGDX is a cross-platform development library. You write all of your code in 
 * the core project.  However, you still need some extra classes if you want to
 * deploy on a specific platform (e.g. PC, Android, Web).  That is the purpose
 * of this class.  It deploys your game on a PC/desktop computer.
 *
 * Author: Walker M. White
 * Based on original Optimization Lab by Don Holden, 2007
 * LibGDX version, 2/2/2015
 */
package code.game.desktop;

import code.backend.GDXApp;
import code.backend.GDXAppSettings;
import code.game.controllers.GDXRoot;
import code.game.models.Save;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

/**
 * The main class of the game.
 * 
 * This class sets the window size and launches the game.  Aside from modifying
 * the window size, you should almost never need to modify this class.
 */
public class DesktopLauncher {
	
	/**
	 * Classic main method that all Java programmers know.
	 * 
	 * This method simply exists to start a new LwjglApplication.  For desktop games,
	 * LibGDX is built on top of LWJGL (this is not the case for Android).
	 * 
	 * @param arg Command line arguments
	 */
	public static void main (String[] arg) {
		GDXAppSettings config = new GDXAppSettings();
		String string = Gdx.files.getLocalStoragePath();
		FileHandle file = Gdx.files.local(Save.file);
		Json json = new Json();
		Save save = json.fromJson(Save.class, file);
		config.width  = save.screen_width;
		config.height = save.screen_height;
		config.x = 0;
		config.y = 0;
		config.resizable = true;
		//config.fullscreen = true;
		config.title = "WhatTheFlock!";
		new GDXApp(new GDXRoot(), config);
	}
}