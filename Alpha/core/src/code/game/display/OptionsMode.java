package code.game.display;

import code.assets.AssetDirectory;
import code.game.controllers.GameController;
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
    private final float VOLUME_SCALE = 0.55f;
    /** Scale of the buttons */
    private final float BUTTON_SCALE = 0.45f;
    /** Scale of selection box */
    private final float SELECT_SCALE = 0.4f;
    /** Scale of the OK button */
    private final float OK_SCALE = 0.7f;

    /** The width of the checkbox */
    private final float CHECKBOX_WIDTH = 100;

    /** The constants for selection box position */
    private float left_column;
    private float right_column;
    /** The constants for volume option */
    private final float VOLUME_WIDTH = 439;
    private final float VOLUME_HEIGHT = 18;
    private final float VOLUME_HANDLE_WIDTH = 31;
    private final float VOLUME_HANDLE_HEIGHT = 125;
    /** The constants for other options */
    private final float BUTTON_HEIGHT = 37;
    private final float SELECT_HEIGHT = 200;
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
    private float windowBoxCenterX;
    private float fullscreenCenterX;
    private float fullscreenBoxCenterX;
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
    private float ACOnCheckCenterX;
    private float ACOffCheckCenterX;
    private float MSOnCheckCenterX;
    private float MSOffCheckCenterX;
    private final float ON_WIDTH = 123;
    private final float OFF_WIDTH = 147;
    /** The center and constants of ok option */
    private float okCenterY;
    private final float OK_HEIGHT = 120;
    private final float OK_WIDTH = 220;
    /** The constants for the selection box */
    private final float SELECT_WIDTH = 2500;

    /** Whether the user is adjusting volume */
    private boolean adjustMusic = false;
    private boolean adjustSFX = false;

    /** The bounds for the game screen */
    private Rectangle bounds;
    /** The scale of the game */
    private Vector2 vscale;

    /**Center the background*/
    private float bkgCenterX;
    private float bkgCenterY;

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
     * 0 = display
     * 1 = oggle auto-cook
     * 2 = music
     * 3 = sfx
     * 4 = toggle mouse-slap
     * 5 = ok
     */
    private int selected = 0;

    /**
     * The current state of the play button
     * 0 for nothing,
     * 1 for windowed pressed down,
     * 2 for fullscreen pressed down,
     * 3 for auto cook on pressed down,
     * 4 for auto cook off pressed down,
     * 5 for mouse slap on pressed down
     * 6 for mouse slap off pressed down
     * 7 for back pressed down
     * */
    private int   pressState;

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
    public static final int MAINMENU = 0;
    /** The exit code representing returning to main menu */
    public static final int GAMEMNEU = 1;
    /** The game save */
    private Save save;
    /** The game controller */
    private GameController controller;
    /** The main menu */
    private MainMenuMode mainMenu;
    /** Whether we are in the main menu */
    public boolean inMainMenu = true;

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
        pressState = 0;

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener(this);
        }

        active = true;
    }

    public void setController(GameController c){
        controller = c;
    }

    public void setMenu(MainMenuMode mmm){
        mainMenu = mmm;
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

    /** Resets this screen */
    public void reset(){
        selected = 0; editing = -1; back = false; pressState = 0;
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
        if (input.didArrowKey() && input.didMovementKey()) {
            scrollDirection = 0; scrolling = false; pressDownTime = 0; return true;
        }
        if ((input.doneMovementKey() && input.getHorizontal() == 0)
                || (scrollDirection == 1 && input.getHorizontal() < 0)
                || (scrollDirection == -1 && input.getHorizontal() > 0)) {
            scrollDirection = 0; scrolling = false; pressDownTime = 0; return true;
        }
        if ((input.doneArrowKey() && input.getArrowHorizontal() == 0)
                || (scrollDirection == 1 && input.getArrowHorizontal() < 0)
                || (scrollDirection == -1 && input.getArrowHorizontal() > 0)) {
            scrollDirection = 0; scrolling = false; pressDownTime = 0; return true;
        }
        return processScrolling(dt);
    }

    /** Scrolls to the next panel */
    private void scroll() {
        float lbound = sfxCenterX-volume.getRegionWidth()/2f*VOLUME_SCALE*scale;
        float rbound = sfxCenterX+volume.getRegionWidth()/2f*VOLUME_SCALE*scale;
        if (scrollDirection == 1) {
            if (selected == 2) {
                music_vol = music_vol + 1 > 100 ? music_vol:music_vol+1;
                musicHandleCenterX  = (music_vol*(rbound-lbound))/100 + lbound;
            } else if (selected == 3) {
                sfx_vol = sfx_vol + 1 > 100 ? sfx_vol:sfx_vol+1;
                sfxHandleCenterX  = (sfx_vol*(rbound-lbound))/100 + lbound;
            }
        } else {
            if (selected == 2) {
                music_vol = music_vol - 1 < 0 ? music_vol:music_vol-1;
                musicHandleCenterX  = (music_vol*(rbound-lbound))/100 + lbound;
            }
            else if (selected == 3) {
                sfx_vol = sfx_vol - 1 < 0 ? sfx_vol:sfx_vol-1;
                sfxHandleCenterX  = (sfx_vol*(rbound-lbound))/100 + lbound;
            }
        }
    }

    /** Handles the case in which the enter button is pressed. */
    public void handleEnter() {
        if (editing >= 0) {
            switch(selected){
                case 0:
                    if (editing == 0) { isFullscreen = false; } else { isFullscreen = true; }
                    break;
                case 1:
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
        if ((selected == 2 || selected == 3) && scrollDirection != 0) {
            handleScrolling(dt, input);
        } else {
            scrollDirection = 0;
            scrolling = false;
            pressDownTime = 0;
        }
        if (input.didESC()) {
            if (selected == 5) { handleEnter(); sound.playMenuEnter(); return false; }
            else {
                Gdx.input.setCursorCatched(true);
                sound.playMenuSelecting();
                selected = 5;
                editing = -1;
                return true;
            }
        }
        else if (input.didMovementKey() || input.didArrowKey()) {
            Gdx.input.setCursorCatched(true);
            if (input.getHorizontal() != 0 || input.getArrowHorizontal() != 0) {
                // right
                if (input.getHorizontal() > 0 || input.getArrowHorizontal() > 0) {
                    if (editing >= 0) {
                        if (selected == 2 || selected == 3) { scrollDirection = 1; scroll(); }
                        else if (selected != 5) { editing = editing + 1 > 1 ? 0 : editing + 1; }
                    } else {
                        sound.playMenuSelecting();
                        if (selected == 0) { selected = 2; }
                        else if (selected == 1) { selected = 4; }
                    }
                }
                // left
                else if (input.getHorizontal() < 0 || input.getArrowHorizontal() < 0) {
                    if (editing >= 0) {
                        if (selected == 2 || selected == 3) { scrollDirection = -1; scroll(); }
                        else if (selected != 5) { editing = editing - 1 < 0 ? 1 : editing - 1; }
                    } else {
                        sound.playMenuSelecting();
                        if (selected == 2 || selected == 3) { selected = 0; }
                        else if (selected == 4) { selected = 1; }
                    }
                }
            } else {
                // down
                if (input.getVertical() < 0 || input.getArrowVertical() < 0) {
                    sound.playMenuSelecting();
                    if (selected == 1 || selected == 4) { selected = 5; }
                    else if (selected != 5) { selected += 1; }
                }
                // up
                else if (input.getVertical() > 0 || input.getArrowVertical() > 0) {
                    sound.playMenuSelecting();
                    if (selected == 5) { selected = 4; }
                    else if (selected != 2 && selected != 0) { selected -= 1; }
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
        canvas.setTintGray(true);
        if(inMainMenu){
            mainMenu.draw();
        }else{
            controller.draw(0);
        }
        canvas.setIgnore(true);
        canvas.begin();
        float selectCenterY = 0;
        float selectCenterX = 0;
        canvas.draw(background, Color.WHITE, background.getWidth()/2, background.getHeight()/2,
                bkgCenterX, bkgCenterY, 0, BACKGROUND_SCALE * scale, BACKGROUND_SCALE * scale);
        switch(selected){
            case 0: selectCenterY = displayCenterY; selectCenterX = left_column; break;
            case 1: selectCenterY = ACCenterY; selectCenterX = left_column; break;
            case 2: selectCenterY = musicCenterY; selectCenterX = right_column; break;
            case 3: selectCenterY = sfxCenterY; selectCenterX = right_column; break;
            case 4: selectCenterY = MSCenterY; selectCenterX = right_column; break;
        }
        if (selected != 5) {
            canvas.draw(selectBox, Color.WHITE, selectBox.getWidth()/2, selectBox.getHeight()/2, selectCenterX, selectCenterY,
                    0, SELECT_SCALE * scale, SELECT_SCALE * scale);
            ok.setFrame(0);
        } else {
            ok.setFrame(1);
        }
        canvas.draw(ok, Color.WHITE, ok.getRegionWidth()/2, ok.getRegionHeight()/2, bkgCenterX, okCenterY,
                0, OK_SCALE * scale, OK_SCALE * scale);

        Color mVolHandle, sfxVolHandle, dWin, dFull, aOn, aOff, mOn, mOff;
        if (selected == 2) {
            if (editing == 0) { mVolHandle = Color.RED; } else {mVolHandle = Color.WHITE; }
        } else { mVolHandle = Color.WHITE; }
        if (selected == 3) {
            if (editing == 0) { sfxVolHandle = Color.RED; } else {sfxVolHandle = Color.WHITE; }
        } else {sfxVolHandle = Color.WHITE; }
        if (selected == 0) {
            if (editing == 0) { dWin = Color.CYAN; dFull = Color.WHITE; }
            else if (editing == 1) { dWin = Color.WHITE; dFull = Color.CYAN; }
            else { dWin = Color.WHITE; dFull = Color.WHITE; }
        } else { dWin = Color.WHITE; dFull = Color.WHITE; }
        if (selected == 1) {
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
        canvas.setIgnore(false);
        canvas.setTintGray(false);
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
        bkgCenterX = width*.495f;
        bkgCenterY = height/2;

        musicCenterX = width*0.625f;
        musicCenterY = height*0.59f;

        sfxCenterX = width*0.625f;
        sfxCenterY = height*0.48f;

        left_column = width*0.35f;
        right_column = width*0.65f;

        float lbound = sfxCenterX-volume.getRegionWidth()/2f*VOLUME_SCALE*scale;
        float rbound = sfxCenterX+volume.getRegionWidth()/2f*VOLUME_SCALE*scale;
        musicHandleCenterX  = (music_vol*(rbound-lbound))/100 + lbound;
        sfxHandleCenterX  = (sfx_vol*(rbound-lbound))/100 + lbound;

        windowCenterX = width*0.255f;
        windowBoxCenterX = (float)(windowCenterX-0.3*windowed.getRegionWidth());
        fullscreenCenterX = width*0.405f;
        fullscreenBoxCenterX = (float)(fullscreenCenterX-0.3*fullscreen.getRegionWidth());
        displayCenterY = height*0.57f;

        ACOnCenterX = width*0.22f;
        ACOffCenterX = width*0.33f;
        ACCenterY = height*0.33f;

        MSOnCenterX = width*0.555f;
        MSOffCenterX = width*0.645f;
        MSCenterY = height*0.33f;

        ACOnCheckCenterX = (float)(ACOnCenterX-0.238*on.getRegionWidth());
        ACOffCheckCenterX = (float)(ACOffCenterX-0.265*off.getRegionWidth());
        MSOnCheckCenterX = (float)(MSOnCenterX-0.238*on.getRegionWidth());
        MSOffCheckCenterX = (float)(MSOffCenterX-0.265*off.getRegionWidth());
        okCenterY = height*0.2f;

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
     * @param scale The scale of the asset
     * */
    private boolean overButton(float bwidth, float bheight, float screenX, float screenY, float centerX, float centerY, float scale){
        float width = scale * scale * bwidth;
        float height = scale * scale * bheight;
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

        if(overButton(VOLUME_HANDLE_WIDTH, VOLUME_HANDLE_HEIGHT, screenX, screenY, musicHandleCenterX, musicCenterY, VOLUME_SCALE)){
            System.out.println("Over music handle");
            adjustMusic = true;
        } else if (overButton(VOLUME_HANDLE_WIDTH, VOLUME_HANDLE_HEIGHT, screenX, screenY, sfxHandleCenterX, sfxCenterY, VOLUME_SCALE)) {
            adjustSFX = true;
        } else if (overButton(CHECKBOX_WIDTH, CHECKBOX_WIDTH, screenX, screenY, windowBoxCenterX, displayCenterY, BUTTON_SCALE)) {
            pressState = 1;
        } else if (overButton(CHECKBOX_WIDTH, CHECKBOX_WIDTH, screenX, screenY, fullscreenBoxCenterX, displayCenterY, BUTTON_SCALE)) {
            pressState = 2;
        } else if (overButton(CHECKBOX_WIDTH, CHECKBOX_WIDTH, screenX, screenY, ACOnCheckCenterX, ACCenterY, BUTTON_SCALE)) {
            pressState = 3;
        } else if (overButton(CHECKBOX_WIDTH, CHECKBOX_WIDTH, screenX, screenY, ACOffCheckCenterX, ACCenterY, BUTTON_SCALE)) {
            pressState = 4;
        } else if (overButton(CHECKBOX_WIDTH, CHECKBOX_WIDTH, screenX, screenY, MSOnCheckCenterX, MSCenterY, BUTTON_SCALE)) {
            pressState = 5;
        } else if (overButton(CHECKBOX_WIDTH, CHECKBOX_WIDTH, screenX, screenY, MSOffCheckCenterX, MSCenterY, BUTTON_SCALE)) {
            pressState = 6;
        } else if (overButton(OK_WIDTH, OK_HEIGHT, screenX, screenY, bkgCenterX, okCenterY, OK_SCALE)) {
            sound.playMenuEnter();
            pressState = 7;
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
        if (pressState > 0) {
            if (pressState == 1) { isFullscreen = false; }
            else if (pressState == 2) { isFullscreen = true; }
            else if (pressState == 3) { isAutoCook = true; }
            else if (pressState == 4) { isAutoCook = false; }
            else if (pressState == 5) { isMouseSlap = true; }
            else if (pressState == 6) { isMouseSlap = false; }
            else if (pressState == 7) { back = true; return false; }
        }
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
        if (overButton(SELECT_WIDTH, SELECT_HEIGHT, screenX, screenY, left_column, displayCenterY, BUTTON_SCALE)) {
            if (selected != 0) { sound.playMenuSelecting(); }
            selected = 0;
        }
        else if (overButton(SELECT_WIDTH, SELECT_HEIGHT, screenX, screenY, left_column, ACCenterY, BUTTON_SCALE)) {
            if (selected != 1) { sound.playMenuSelecting(); }
            selected = 1;
        }
        else if (overButton(SELECT_WIDTH, SELECT_HEIGHT, screenX, screenY, right_column, musicCenterY, BUTTON_SCALE)) {
            if (selected != 2) { sound.playMenuSelecting(); }
            selected = 2;
        }
        else if (overButton(SELECT_WIDTH, SELECT_HEIGHT, screenX, screenY, right_column, sfxCenterY, BUTTON_SCALE)) {
            if (selected != 3) { sound.playMenuSelecting(); }
            selected = 3;
        }
        else if (overButton(SELECT_WIDTH, SELECT_HEIGHT, screenX, screenY, right_column, MSCenterY, BUTTON_SCALE)) {
            if (selected != 4) { sound.playMenuSelecting(); }
            selected = 4;
        }
        else if (overButton(OK_WIDTH, OK_HEIGHT, screenX, screenY, bkgCenterX, okCenterY, OK_SCALE)) {
            if (selected != 5) { sound.playMenuSelecting(); }
            selected = 5;
        }
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
