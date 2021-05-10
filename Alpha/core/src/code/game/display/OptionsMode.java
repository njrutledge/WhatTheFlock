package code.game.display;

import code.assets.AssetDirectory;
import code.game.controllers.InputController;
import code.game.controllers.SoundController;
import code.game.models.Save;
import code.game.views.GameCanvas;
import code.util.Controllers;
import code.util.FilmStrip;
import code.util.ScreenListener;
import code.util.XBoxController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public class OptionsMode implements Screen, InputProcessor, ControllerListener {
    /** The assets of the screen */
    private AssetDirectory assets;
    /** Holds all the level file locations of the game */
    private JsonValue levels;

    /** Background texture */
    private Texture background;
    /** Texture for onTexture checkbox */
    private Texture onTexture;
    private FilmStrip on;
    /** Texture for off checkbox */
    private Texture offTexture;
    private FilmStrip off;
    /** Texture for ok */
    private Texture okTexture;
    private FilmStrip ok;
    /** Texture for fullscreen */
    private Texture fullscreenTexture;
    private FilmStrip fullscreen;
    /** Texture for windowed */
    private Texture windowedTexture;
    private FilmStrip windowed;
    /** Texture for volume */
    private Texture volumeTexture;
    private FilmStrip volume;
    /** Texture for volume handle */
    private Texture volumeHandle;
    /** Texture for selection box */
    private Texture selectBox;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 48.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 27.0f;

    /** Scale of the menu */
    private final float BACKGROUND_SCALE = 0.5f;
    /** Scale of the volume bar */
    private final float VOLUME_SCALE = 0.45f;
    /** Scale of the buttons */
    private final float BUTTON_SCALE = 0.5f;
    /** Scale of selection box */
    private final float SELECT_SCALE = 0.5f;

    /** The width of the checkbox */
    private final float CHECKBOX_WIDTH = 36;

    /** The constants for volume option */
    private final float VOLUME_WIDTH = 439;
    private final float VOLUME_HEIGHT = 18;
    private final float VOLUME_HANDLE_WIDTH = 31;
    private final float VOLUME_HANDLE_HEIGHT = 58;
    /** The constants for other options */
    private final float BUTTON_HEIGHT = 37;
    /** The center of music volume option */
    private float musicCenterX;
    private float musicCenterY;
    private float musicHandleCenterX;
    /** The center of sfx volume option */
    private float sfxCenterX;
    private float sfxCenterY;
    private float sfxHandleCenterX;
    /** The center of display options */
    private float windowCenterX;
    private float fullscreenCenterX;
    private float displayCenterY;
    private final float WINDOW_WIDTH = 323;
    private final float FULLSCREEN_WIDTH = 350;
    /** The center of auto-cook options */
    private float ACOnCenterX;
    private float ACOffCenterX;
    private float ACCenterY;
    /** The center of mouse-slap options*/
    private float MSOnCenterX;
    private float MSOffCenterX;
    private float MSCenterY;
    /** Constants for onTexture and off buttons */
    private final float ON_WIDTH = 123;
    private final float OFF_WIDTH = 147;
    /** The center and constants of ok option */
    private float okCenterX;
    private float okCenterY;
    private final float OK_HEIGHT = 46;
    private final float OK_WIDTH = 70;
    /** The constants for the selection box */
    private final float SELECT_WIDTH = 1031;

    /** Whether the user is adjusting volume */
    private boolean adjustMusic = false;
    private boolean adjustSFX = false;

    /** The bounds for the game screen */
    private Rectangle bounds;
    /** The scale of the game */
    private Vector2 vscale;

    /**Center the background*/
    private int bkgCenterX;
    private int bkgCenterY;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Reference to SoundController created by the root */
    private SoundController sound;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** Time to wait before we begin scrolling */
    private final float SCROLL_WAIT_TIME = 0.4f;
    /** Rate of scroll */
    private final float SCROLL_RATE = 0.03f;
    /** Time since a button was held down */
    private float pressDownTime = 0f;
    /** The direction of the scroll: -1 for left, 1 for right */
    private int scrollDirection = 0;
    /** Whether scrolling has began */
    private boolean scrolling = false;

    /** Which option are we currently hovering over?
     * 0 = music
     * 1 = sfx
     * 2 = display
     * 3 = toggle auto-cook
     * 4 = toggle mouse-slap
     * 5 = ok
     */
    private int selected = 0;
    /** Which selection within the option are we currently hovering over?
     * -1 = none
     * 0 = first option
     * 1 = second option
     */
    private int editing = -1;

    /** Whether back was selected */
    private boolean back = false;

    /** The music volume */
    private int music_vol = 100;
    /** The sfx volume */
    private int sfx_vol = 100;
    /** Whether window is fullscreen */
    private boolean isFullscreen = false;
    /** Whether auto-cook is toggled */
    private boolean isAutoCook = true;
    /** Whether mouse-slap is toggled */
    private boolean isMouseSlap = false;

    /** Whether or not this player mode is still active */
    private boolean active;

    /** The exit code that will be returned
     * 0 = return to main menu
     * 1 = return to game menu
     * */
    private int exitCode;

    /** The exit code representing returning to main menu */
    public static final int MAINMAIN = 0;
    /** The exit code representing returning to main menu */
    public static final int GAMEMNEU = 1;
    /** the game save */
    private Save save;

    /**
     * Creates a LevelSelectMode with the default size and position.
     * @param canvas 	The game canvas to draw to
     */
    public OptionsMode(AssetDirectory assets, GameCanvas canvas, SoundController sound) {
        this.canvas  = canvas;
        this.assets = assets;
        this.sound = sound;

        music_vol = 100;
        sfx_vol = 100;
        isAutoCook = true;
        isMouseSlap = false;
        isFullscreen = false;

        background = assets.getEntry( "background:levelselect", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );

        background = assets.getEntry("ui:options:menu", Texture.class );
        okTexture = assets.getEntry("ui:options:ok", Texture.class );

        onTexture = assets.getEntry("ui:options:on", Texture.class);
        on =  new FilmStrip(onTexture, 1, 2);
        offTexture = assets.getEntry("ui:options:off", Texture.class);
        off =  new FilmStrip(offTexture, 1, 2);
        okTexture = assets.getEntry("ui:options:ok", Texture.class);
        ok =  new FilmStrip(okTexture, 1, 2);
        volumeTexture = assets.getEntry("ui:options:volume", Texture.class);
        volume =  new FilmStrip(volumeTexture, 1, 2);
        volumeHandle = assets.getEntry("ui:options:volume_handle", Texture.class);
        fullscreenTexture = assets.getEntry("ui:options:fullscreen", Texture.class);
        fullscreen =  new FilmStrip(fullscreenTexture, 1, 2);
        windowedTexture = assets.getEntry("ui:options:windowed", Texture.class);
        windowed =  new FilmStrip(windowedTexture, 1, 2);
        selectBox = assets.getEntry("ui:options:selected", Texture.class);

        this.bounds = new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
        this.vscale = new Vector2(1,1);

        resize(canvas.getWidth(),canvas.getHeight());

        selected = 0;
        editing = -1;

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener(this);
        }

        active = true;
    }
    /**
     * Sets the save object
     * @param s the save object
     */
    public void setSave(Save s){
        save = s;
        isAutoCook = save.auto_cook;
        isFullscreen = save.fullscreen;
        music_vol = save.music_vol;
        sfx_vol = save.sfx_vol;
        isMouseSlap = save.mouse_slap;
    }
    private void updateSave(){
        save.auto_cook = isAutoCook;
        save.fullscreen = isFullscreen;
        save.music_vol = music_vol;
        save.sfx_vol = sfx_vol;
        save.mouse_slap = isMouseSlap;
    }

    /**
     * Activates or deactivates the input processor; called when screen is shown or hidden
     * @param b     whether or not to activate or deactivate the input processor
     * */
    private void activateInputProcessor(boolean b){
        if(b){
            Gdx.input.setInputProcessor(this);
        }
        else {
            Gdx.input.setInputProcessor(null);
        }
    }

    /** Returns the music volume
     *
     * @return music volume
     */
    public int getMusic_vol() { return music_vol; }

    /** Returns the music volume
     *
     * @return music volume
     */
    public int getSavedMusic_vol() { return save.music_vol; }

    /** Returns the sfx volume
     *
     * @return sfx volume
     */
    public int getSfx_vol() { return sfx_vol; }

    /** Returns whether the game is in fullscreen
     *
     * @return isFullscreen
     */
    public boolean isFullscreen() { return isFullscreen; }

    /** Returns whether auto-cook is enabled
     *
     * @return isAutoCook
     */
    public boolean isAutoCook() { return isAutoCook; }

    /** Returns whether mouse-slap is enabled
     *
     * @return isMouseSlap
     */
    public boolean isMouseSlap() { return isMouseSlap; }

    /** Resets this screen */
    public void reset(){
        selected = 0; editing = -1; back = false;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        assets.unloadAssets();
        assets.dispose();
    }

    /** Returns whether or not to scroll to the next panel
     *
     * @param dt   time since last frame
     * @return     whether or not to scroll to the next panel
     */
    private boolean processScrolling(float dt) {
        boolean valid = false;
        pressDownTime += dt;
        if (!scrolling && pressDownTime >= SCROLL_WAIT_TIME) { scrolling = true; pressDownTime = 0; }
        else if (scrolling && pressDownTime >= SCROLL_RATE) { valid = true; }
        if (scrolling && !valid) { return false; }
        if (!scrolling && scrollDirection != 0) { return false; }
        pressDownTime = 0;
        scroll();
        return true;
    }

    /** Returns whether or not to process the pre-update loop.
     *
     * This function will return false if the user is holding down
     * the scrolling key and it is not yet time to move on to the next
     * panel, or true otherwise. This function will also return true
     * if the user has let go of the scrolling key.
     */
    private boolean handleScrolling(float dt, InputController input) {
        if ((input.doneMovementKey() && input.getHorizontal() == 0)
                || (scrollDirection == 1 && input.getHorizontal() < 0)
                || (scrollDirection == -1 && input.getHorizontal() > 0)) {
            scrollDirection = 0; scrolling = false; pressDownTime = 0; return true;
        }
        return processScrolling(dt);
    }

    /** Scrolls to the next panel */
    private void scroll() {
        float lbound = sfxCenterX-volume.getRegionWidth()/2f*VOLUME_SCALE*scale;
        float rbound = sfxCenterX+volume.getRegionWidth()/2f*VOLUME_SCALE*scale;
        if (scrollDirection == 1) {
            if (selected == 0) {
                music_vol = music_vol + 1 > 100 ? music_vol:music_vol+1;
                musicHandleCenterX  = (music_vol*(rbound-lbound))/100 + lbound;
            } else if (selected == 1) {
                sfx_vol = sfx_vol + 1 > 100 ? sfx_vol:sfx_vol+1;
                sfxHandleCenterX  = (sfx_vol*(rbound-lbound))/100 + lbound;
            }
        } else {
            if (selected == 0) {
                music_vol = music_vol - 1 < 0 ? music_vol:music_vol-1;
                musicHandleCenterX  = (music_vol*(rbound-lbound))/100 + lbound;
            }
            else if (selected == 1) {
                sfx_vol = sfx_vol - 1 < 0 ? sfx_vol:sfx_vol-1;
                sfxHandleCenterX  = (sfx_vol*(rbound-lbound))/100 + lbound;
            }
        }
    }

    /** Handles the case in which the enter button is pressed. */
    public void handleEnter() {
        if (editing >= 0) {
            switch(selected){
                case 2:
                    if (editing == 0) { isFullscreen = false; } else { isFullscreen = true; }
                    break;
                case 3:
                    if (editing == 0) { isAutoCook = true; } else {isAutoCook = false; }
                    break;
                case 4:
                    if (editing == 0) { isMouseSlap = true; } else { isMouseSlap = false; }
                    break;
            }
        } else if (selected == 5){
            back = true;
        } else { editing = 0; }
    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt	Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    private boolean preUpdateHelper(float dt){
        InputController input = InputController.getInstance();
        input.readInput(bounds, vscale);
        if ((selected == 1 || selected == 0) && scrollDirection != 0) {
            handleScrolling(dt, input);
        } else {
            scrollDirection = 0;
            scrolling = false;
            pressDownTime = 0;
        }
        if (input.didESC()) { Gdx.input.setCursorCatched(true); selected = 5; editing = 0; return false; }
        else if (input.didMovementKey()) {
            Gdx.input.setCursorCatched(true);
            if (editing >= 0 && input.getHorizontal() != 0) {
                sound.playMenuSelecting();
                // right

                if (input.getHorizontal() > 0) {
                    if (selected == 0 || selected == 1) {
                        scrollDirection = 1;
                        scroll();
                    }
                    else if (selected != 5) { editing = editing + 1 > 1 ? 0:editing+1; }
                }
                // left
                else if (input.getHorizontal() < 0) {
                    if (selected == 0 || selected  == 1) {
                        scrollDirection = -1;
                        scroll();
                    }
                    else if (selected != 5) { editing = editing - 1 < 0 ? 1:editing-1; }
                }
            } else {
                if (input.getVertical() < 0) {
                    sound.playMenuSelecting();
                    selected = selected == 5 ? 0 : selected + 1;
                }
                else if (input.getVertical() > 0) {
                    sound.playMenuSelecting();
                    selected = selected == 0 ? 5 : selected - 1;
                }
                editing = -1;
            }
        } else if (input.didEnter()) {
            sound.playMenuEnter();
            handleEnter();
            return false;
        }
        return true;
    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt	Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    private boolean preUpdate(float dt) {
        return preUpdateHelper(dt);
    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        sound.playMusic(SoundController.CurrentScreen.MENU, delta);
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.clear();
        canvas.begin();
        float selectCenterY = 0;
        canvas.draw(background, Color.WHITE, background.getWidth()/2, background.getHeight()/2,
                bkgCenterX, bkgCenterY, 0, BACKGROUND_SCALE * scale, BACKGROUND_SCALE * scale);
        switch(selected){
            case 0: selectCenterY = musicCenterY; break;
            case 1: selectCenterY = sfxCenterY; break;
            case 2: selectCenterY = displayCenterY; break;
            case 3: selectCenterY = ACCenterY; break;
            case 4: selectCenterY = MSCenterY; break;
        }
        if (selected != 5) {
            canvas.draw(selectBox, Color.WHITE, selectBox.getWidth()/2, selectBox.getHeight()/2, bkgCenterX, selectCenterY,
                    0, SELECT_SCALE * scale, SELECT_SCALE * scale);
            ok.setFrame(0);
        } else {
            ok.setFrame(1);
        }
        canvas.draw(ok, Color.WHITE, ok.getRegionWidth()/2, ok.getRegionHeight()/2, bkgCenterX, okCenterY,
                0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

        Color mVolHandle, sfxVolHandle, dWin, dFull, aOn, aOff, mOn, mOff;
        if (selected == 0) {
            if (editing == 0) { mVolHandle = Color.RED; } else {mVolHandle = Color.WHITE; }
        } else { mVolHandle = Color.WHITE; }
        if (selected == 1) {
            if (editing == 0) { sfxVolHandle = Color.RED; } else {sfxVolHandle = Color.WHITE; }
        } else {sfxVolHandle = Color.WHITE; }
        if (selected == 2) {
            if (editing == 0) { dWin = Color.CYAN; dFull = Color.WHITE; }
            else if (editing == 1) { dWin = Color.WHITE; dFull = Color.CYAN; }
            else { dWin = Color.WHITE; dFull = Color.WHITE; }
        } else { dWin = Color.WHITE; dFull = Color.WHITE; }
        if (selected == 3) {
            if (editing == 0) { aOn = Color.CYAN; aOff = Color.WHITE; }
            else if (editing == 1) { aOn = Color.WHITE; aOff = Color.CYAN; }
            else { aOn = Color.WHITE; aOff = Color.WHITE; }
        } else { aOn = Color.WHITE; aOff = Color.WHITE; }
        if (selected == 4) {
            if (editing == 0) { mOn = Color.CYAN; mOff = Color.WHITE; }
            else if (editing == 1) { mOn = Color.WHITE; mOff = Color.CYAN; }
            else { mOn = Color.WHITE; mOff = Color.WHITE; }
        } else { mOn = Color.WHITE; mOff = Color.WHITE; }

        if (isFullscreen) { fullscreen.setFrame(1); windowed.setFrame(0); }
        else { fullscreen.setFrame(0); windowed.setFrame(1); }
        canvas.draw(windowed, dWin, windowed.getRegionWidth()/2, windowed.getRegionHeight()/2, windowCenterX, displayCenterY,
                0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        canvas.draw(fullscreen, dFull, fullscreen.getRegionWidth()/2, fullscreen.getRegionHeight()/2, fullscreenCenterX, displayCenterY,
                0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        if (isAutoCook) { on.setFrame(1); off.setFrame(0); }
        else { on.setFrame(0); off.setFrame(1); }
        canvas.draw(on, aOn, on.getRegionWidth()/2, on.getRegionHeight()/2, ACOnCenterX, ACCenterY,
                0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        canvas.draw(off, aOff, off.getRegionWidth()/2, off.getRegionHeight()/2, ACOffCenterX, ACCenterY,
                0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        if (isMouseSlap) { on.setFrame(1); off.setFrame(0); }
        else { on.setFrame(0); off.setFrame(1); }
        canvas.draw(on, mOn, on.getRegionWidth()/2, on.getRegionHeight()/2, MSOnCenterX, MSCenterY,
                0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
        canvas.draw(off, mOff, off.getRegionWidth()/2, off.getRegionHeight()/2, MSOffCenterX, MSCenterY,
                0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

        canvas.draw(volume, Color.WHITE, volume.getRegionWidth()/2, volume.getRegionHeight()/2, musicCenterX, musicCenterY,
                0, VOLUME_SCALE * scale, VOLUME_SCALE * scale);
        canvas.draw(volume, Color.WHITE, volume.getRegionWidth()/2, volume.getRegionHeight()/2, sfxCenterX, sfxCenterY,
                0, VOLUME_SCALE * scale, VOLUME_SCALE * scale);
        canvas.draw(volumeHandle, mVolHandle, volumeHandle.getWidth()/2, volumeHandle.getHeight()/2, musicHandleCenterX, musicCenterY,
                0, VOLUME_SCALE * scale, VOLUME_SCALE * scale);
        canvas.draw(volumeHandle, sfxVolHandle, volumeHandle.getWidth()/2, volumeHandle.getHeight()/2, sfxHandleCenterX, sfxCenterY,
                0, VOLUME_SCALE * scale, VOLUME_SCALE * scale);

        canvas.end();
    }

    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            preUpdate(delta);
            update(delta);
            draw();
            if (back && listener != null){
                canvas.clear();
                updateSave();
                listener.exitScreen(this, exitCode);
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);
        //TODO resize other things as needed
        bkgCenterX = width/2;
        bkgCenterY = height/2;

        musicCenterX = width*0.65f;
        musicCenterY = height*0.65f;

        sfxCenterX = width*0.65f;
        sfxCenterY = height*0.58f;

        float lbound = sfxCenterX-volume.getRegionWidth()/2f*VOLUME_SCALE*scale;
        float rbound = sfxCenterX+volume.getRegionWidth()/2f*VOLUME_SCALE*scale;
        musicHandleCenterX  = (music_vol*(rbound-lbound))/100 + lbound;
        sfxHandleCenterX  = (sfx_vol*(rbound-lbound))/100 + lbound;

        windowCenterX = width*0.45f;
        fullscreenCenterX = width*0.67f;
        displayCenterY = height*0.505f;
        ACOnCenterX = width*0.62f;
        ACOffCenterX = width*0.78f;
        ACCenterY = height*0.431f;

        MSOnCenterX = width*0.62f;
        MSOffCenterX = width*0.78f;
        MSCenterY = height*0.325f;

        okCenterY = height*0.235f;

        heightY = height;
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible onTexture screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
        activateInputProcessor(true);
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
        activateInputProcessor(false);
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /** Sets the exit code that will be returned when the back button is pressed
     *
     * @param exit the code to be used when exiting
     */
    public void setExitCode(int exit) { exitCode = exit; }

    // PROCESSING PLAYER INPUT
    /**
     * Returns if this position is over the given RECTANGULAR button
     * @param screenX The x axis screen position of the mouse interaction
     * @param screenY The y axis screen position of the mouse interaction
     * @param centerX The x axis center location of the button
     * @param centerY The y axis center location of the button
     * @param bwidth The width of the button
     * @param bheight The height of the button
     * */
    private boolean overButton(float bwidth, float bheight, float screenX, float screenY, float centerX, float centerY){
        float width = BUTTON_SCALE * scale * bwidth;
        float height = BUTTON_SCALE * scale * bheight;
        float xBound = centerX - width/2; //lower x bound
        float yBound = centerY - height/2;
        return ((screenX >= xBound && screenX <= xBound + width) && (screenY >= yBound && screenY <= yBound + height));
    }

    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse onTexture the screen
     * @param screenY the y-coordinate of the mouse onTexture the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Gdx.input.setCursorCatched(false);
        screenY = heightY-screenY;

        if(overButton(VOLUME_HANDLE_WIDTH, VOLUME_HANDLE_HEIGHT, screenX, screenY, musicHandleCenterX, musicCenterY)){
            adjustMusic = true;
        } else if (overButton(VOLUME_HANDLE_WIDTH, VOLUME_HANDLE_HEIGHT, screenX, screenY, sfxHandleCenterX, sfxCenterY)) {
            adjustSFX = true;
        }

        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse onTexture the screen
     * @param screenY the y-coordinate of the mouse onTexture the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        adjustMusic = false;
        adjustSFX = false;
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor
    /**
     * Called when a button onTexture the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button onTexture an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        return true;
    }

    /**
     * Called when a button onTexture the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button onTexture an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
        return true;
    }

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @paream keycode the key typed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released (UNSUPPORTED)
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse onTexture the screen
     * @param screenY the y-coordinate of the mouse onTexture the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        Gdx.input.setCursorCatched(false);
        screenY = heightY-screenY;
        if (overButton(SELECT_WIDTH, BUTTON_HEIGHT, screenX, screenY, bkgCenterX, musicCenterY)) { selected = 0; }
        else if (overButton(SELECT_WIDTH, BUTTON_HEIGHT, screenX, screenY, bkgCenterX, sfxCenterY)) { selected = 1; }
        else if (overButton(SELECT_WIDTH, BUTTON_HEIGHT, screenX, screenY, bkgCenterX, displayCenterY)) { selected = 2; }
        else if (overButton(SELECT_WIDTH, BUTTON_HEIGHT, screenX, screenY, bkgCenterX, ACCenterY)) { selected = 3; }
        else if (overButton(SELECT_WIDTH, BUTTON_HEIGHT, screenX, screenY, bkgCenterX, MSCenterY)) { selected = 4; }
        else if (overButton(OK_WIDTH, OK_HEIGHT, screenX, screenY, bkgCenterX, okCenterY)) { selected = 5; }
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param dx the amount of horizontal scroll
     * @param dy the amount of vertical scroll
     *
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(float dx, float dy) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse onTexture the screen
     * @param screenY the y-coordinate of the mouse onTexture the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float lbound = sfxCenterX-volume.getRegionWidth()/2*VOLUME_SCALE*scale;
        float rbound = sfxCenterX+volume.getRegionWidth()/2*VOLUME_SCALE*scale;
        if (adjustSFX) {
            if (screenX <= sfxCenterX+volume.getRegionWidth()/2*VOLUME_SCALE*scale && screenX >= sfxCenterX-volume.getRegionWidth()/2*VOLUME_SCALE*scale)
            { sfxHandleCenterX = screenX; }
            else if (screenX > sfxCenterX+volume.getRegionWidth()/2*VOLUME_SCALE*scale)
            { sfxHandleCenterX = sfxCenterX+volume.getRegionWidth()/2*VOLUME_SCALE*scale; }
            else { sfxHandleCenterX = sfxCenterX-volume.getRegionWidth()/2*VOLUME_SCALE*scale; }
            sfx_vol = (int)((sfxHandleCenterX-lbound)/(rbound-lbound)*100);
        } else if (adjustMusic) {
            if (screenX <= musicCenterX+volume.getRegionWidth()/2*VOLUME_SCALE*scale && screenX >= musicCenterX-volume.getRegionWidth()/2*VOLUME_SCALE*scale) {
                musicHandleCenterX = screenX; }
            else if (screenX > musicCenterX+volume.getRegionWidth()/2*VOLUME_SCALE*scale) {
                musicHandleCenterX = musicCenterX+volume.getRegionWidth()/2*VOLUME_SCALE*scale; }
            else { musicHandleCenterX = musicCenterX-volume.getRegionWidth()/2*VOLUME_SCALE*scale; }
            music_vol = (int)((musicHandleCenterX-lbound)/(rbound-lbound)*100);
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis onTexture the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }
}
