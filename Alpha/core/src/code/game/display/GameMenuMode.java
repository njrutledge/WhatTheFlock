package code.game.display;

import code.assets.AssetDirectory;
import code.game.controllers.InputController;
import code.game.controllers.SoundController;
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
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class GameMenuMode implements Screen, InputProcessor, ControllerListener {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the assets
    /** Internal assets for this loading screen */
    private AssetDirectory internal;

    /** Enum representing the menus that this class display */
    public enum Mode {
        PAUSE,
        WIN,
        LOSE
    }

    // Pause mode textures
    /** Menu texture */
    private Texture pauseMenu;
    /** Continue button */
    private Texture pauseContTexture;
    private FilmStrip pauseCont;
    /** Restart button */
    private Texture pauseRestartTexture;
    private FilmStrip pauseRestart;
    /** Options button */
    private Texture pauseOptionsTexture;
    private FilmStrip pauseOptions;
    /** Quit button */
    private Texture pauseQuitTexture;
    private FilmStrip pauseQuit;

    // Win mode textures
    /** Menu texture */
    private Texture winMenu;
    /** Next button */
    private Texture winNextTexture;
    private Texture winNextGrayTexture;
    private FilmStrip winNext;
    /** Replay button */
    private Texture winReplayTexture;
    private FilmStrip winReplay;
    /** Quit button */
    private Texture winQuitTexture;
    private FilmStrip winQuit;

    // Lose mode textures
    /** Menu texture */
    private Texture loseMenu;
    /** Retry button */
    private Texture loseRetryTexture;
    private FilmStrip loseRetry;
    /** Quit button */
    private Texture loseQuitTexture;
    private FilmStrip loseQuit;

    /**Gray background texture*/
    private Texture grayTexture;
    /** Whether or not this texture has been drawn*/
    private boolean grayDrawn;

    /**
     * The current state of the game menu
     * 0 for nothing,
     * 1 for continue (or equivalent) pressed down, 11 for continue selected
     * 2 for restart (or equivalent) pressed down, 12 for restart selected
     * 3 for pauseOptions (or equivalent) pressed down, 13 for pauseOptions selected
     * 4 for pauseQuit (or equivalent) pressed down,  14 for pauseQuit selected
     * */
    private int pressState;
    /**Corresponding codes to pressState*/
    public static final int CONT = 1;
    public static final int RESTART  = 2;
    public static final int OPTIONS = 3;
    public static final int QUIT = 4;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Height of all buttons */
    private final float BUTTON_HEIGHT = 40;
    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 48.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 27.0f;

    // Scales for pause menu assets
    /** Scale for menu */
    private final float PMENU_SCALE = 0.65f;
    /** Standard width of Continue Button */
    private static int PCONT_WIDTH = 222;
    /** Standard width of Restart Button */
    private static int PRESTART_WIDTH = 474;
    /** Standard width of Options Button */
    private static int POPTIONS_WIDTH = 292;
    /** Standard width of Quit Button */
    private static int PQUIT_WIDTH = 158;
    /** Scale for all pause buttons */
    private final float PBUTTON_SCALE = .66f;
    /** Scale of Gray Texture */
    private final float GRAY_BKG_SCALE = 1.0f;

    // Coordinates for pause menu assets
    private float pButtonsCenterX;
    private float pContCenterY;
    private float pRestartCenterY;
    private float pOptionsCenterY;
    private float pQuitCenterY;

    // Scales for win menu assets
    /** Scale for menu */
    private final float WMENU_SCALE = 0.65f;
    /** Standard width of Next Button */
    private static int WNEXT_WIDTH = 88;
    /** Standard width of Replay Button */
    private static int WREPLAY_WIDTH = 132;
    /** Standard width of Quit Button */
    private static int WQUIT_WIDTH = 85;
    /** Scale for all win and lose buttons */
    private final float LWBUTTON_SCALE = .75f;

    // Coordinates for win menu assets
    private float wButtonsCenterY;
    private float wNextCenterX;
    private float wReplayCenterX;
    private float wQuitCenterX;

    // Scales for win menu assets
    /** Scale for menu */
    private final float LMENU_SCALE = 0.65f;
    /** Standard width of Retry Button */
    private static int LRETRY_WIDTH = 132;
    /** Standard width of Quit Button */
    private static int LQUIT_WIDTH = 85;


    // Coordinates for win menu assets
    private float lButtonsCenterY;
    private float lRetryCenterX;
    private float lQuitCenterX;

    /** The option that is currently selected within the menu
     *  0 = continue, 1 = restart, 2 = options, 3 = quit
     * */
    private int selected = 0;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;
    /** The type of menu */
    private Mode mode;

    /** Whether or not this player mode is still active */
    private boolean active;
    /** Whether the next level is available */
    private boolean levelAvailable = false;

    /**Center the background*/
    private float bkgCenterX;
    private float bkgCenterY;

    /** The bounds for the game screen */
    private Rectangle bounds;
    /** The scale of the game */
    private Vector2 vscale;

    /** Reference to the SoundController created in GDXRoot */
    private SoundController sound;

    /**
     * Creates a MainMenuMode with the default size and position.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param canvas 	The game canvas to draw to
     */
    public GameMenuMode(AssetDirectory assets, GameCanvas canvas, SoundController sound) {
        this.canvas  = canvas;
        this.sound = sound;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());
        internal = assets;

        this.bounds = new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
        this.vscale = new Vector2(1,1);

        pauseMenu = internal.getEntry( "background:pause", Texture.class );
        pauseMenu.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        pauseContTexture = internal.getEntry( "ui:pause:continue", Texture.class );
        pauseCont = new FilmStrip(pauseContTexture, 1, 2);
        pauseRestartTexture = internal.getEntry( "ui:pause:restart", Texture.class );
        pauseRestart = new FilmStrip(pauseRestartTexture, 1, 2);
        pauseOptionsTexture = internal.getEntry( "ui:pause:options", Texture.class );
        pauseOptions = new FilmStrip(pauseOptionsTexture, 1, 2);
        pauseQuitTexture = internal.getEntry( "ui:pause:quit", Texture.class );
        pauseQuit = new FilmStrip(pauseQuitTexture, 1, 2);

        winMenu = internal.getEntry( "background:win", Texture.class );
        pauseMenu.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        winNextTexture = internal.getEntry("ui:win:next", Texture.class);
        winNext = new FilmStrip(winNextTexture, 1, 2);
        winNextGrayTexture = internal.getEntry("ui:win:graynext", Texture.class);
        winReplayTexture = internal.getEntry("ui:win:replay", Texture.class);
        winReplay = new FilmStrip(winReplayTexture, 1, 2);
        winQuitTexture = internal.getEntry("ui:win:quit", Texture.class);
        winQuit = new FilmStrip(winQuitTexture, 1, 2);

        loseMenu = internal.getEntry( "background:lose", Texture.class );
        loseMenu.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        loseRetryTexture = internal.getEntry("ui:lose:retry", Texture.class);
        loseRetry = new FilmStrip(loseRetryTexture, 1, 2);
        loseQuitTexture = internal.getEntry("ui:lose:quit", Texture.class);
        loseQuit = new FilmStrip(loseQuitTexture, 1, 2);

        grayDrawn = false;
        grayTexture = internal.getEntry("background:gray", Texture.class);
        pressState = 0;

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener( this );
        }
        active = true;
    }

    /** Set the type of menu that is being displayed. */
    public void setMode(Mode mode) { this.mode = mode; setSelected(); }

    /** Set the field "selected" to the appropriate value depending on the type of menu.
     *
     * This method also activates the selected frame.
     */
    private void setSelected() {
        switch (mode) {
            case PAUSE:
                selected = 0;
                pauseCont.setFrame(1);
                break;
            case WIN:
                if (levelAvailable) {
                    selected = 0;
                    winNext.setFrame(1);
                } else {
                    selected = 1;
                    winReplay.setFrame(1);
                }
                break;
            case LOSE:
                selected = 1;
                loseRetry.setFrame(1);
                break;
        }
    }

    /** Get the type of menu that is being displayed */
    public Mode getMode() { return mode; }

    /** Set whether the next level is available */
    public void setLevelAvailable(boolean bool) { levelAvailable = bool; }

    /**
     * Returns true if the user has selected an option from the menu
     *
     * @return if the player is ready to do something
     * */
    private boolean somethingSelected() {
        return pressState == CONT + 10 || pressState == QUIT + 10 || pressState == RESTART+10;
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

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        internal.unloadAssets();
        internal.dispose();
    }

    /** Resets the menu */
    public void reset(){
        pressState = 0;
        setSelected();
        resetButtons();
        grayDrawn = false;
    }

    /** Processes a keyboard input and produces the appropriate response.
     *
     * @param action the action being processed
     * */
    private void keyPressed(String action) {
        switch (action) {
            case "SELECTING":
                resetButtons();
                switch(mode) {
                    case PAUSE:
                        if (selected == 0) pauseCont.setFrame(1);
                        else if (selected == 1) pauseRestart.setFrame(1);
                        else if (selected == 2) pauseOptions.setFrame(1);
                        else if (selected == 3) pauseQuit.setFrame(1);
                        break;
                    case WIN:
                        if (levelAvailable && selected == 0) winNext.setFrame(1);
                        else if (selected == 1) winReplay.setFrame(1);
                        else if (selected == 3) winQuit.setFrame(1);
                        break;
                    case LOSE:
                        if (selected == 1) loseRetry.setFrame(1);
                        else if (selected == 3) loseQuit.setFrame(1);
                        break;
                }
                break;
            case "ENTERED":
                pressState = selected + 11;
                break;
        }
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
        if (input.didMovementKey()) {
            if (input.getVertical() != 0 && mode == Mode.PAUSE) {
                Gdx.input.setCursorCatched(true);
                if (input.getVertical() < 0) { selected = (selected + 1) % 4; }
                else { selected = selected == 0 ? 3 : selected - 1; }
                keyPressed("SELECTING");
                sound.playMenuSelecting();
            } else if (input.getHorizontal() != 0) {
                Gdx.input.setCursorCatched(true);
                if (mode == Mode.WIN) {
                    if (input.getHorizontal() < 0) {
                        selected = selected - 1 == 2 ? 1 : selected - 1;
                        if (levelAvailable) selected = selected < 0 ? 3 : selected;
                        else selected = selected < 1 ? 3 : selected;
                    }
                    else {
                        selected = selected + 1 == 2 ? 3 : selected + 1;
                        selected = selected > 3 ? 0 : selected;
                    }
                    sound.playMenuSelecting();
                } else if (mode == Mode.LOSE) { selected = selected == 1 ? 3 : 1; }
                keyPressed("SELECTING");
                sound.playMenuSelecting();
            }
        } else if (input.didEnter()) {
            Gdx.input.setCursorCatched(true);
            keyPressed("ENTERED");
            sound.playMenuEnter();
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
        sound.playMusic(SoundController.CurrentScreen.PAUSE, delta);
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        if(!grayDrawn){
            canvas.begin();
            canvas.draw(grayTexture, new Color(1, 1, 1, 0.1f), grayTexture.getWidth()/2, grayTexture.getHeight()/2,
                    bkgCenterX, bkgCenterY, 0, GRAY_BKG_SCALE* scale, GRAY_BKG_SCALE * scale);
            System.out.println("drew gray");
            grayDrawn = true;
            canvas.end();
        }

        canvas.begin();
        canvas.setIgnore(true);
        switch(mode) {
            case PAUSE:
                canvas.draw(pauseMenu, Color.WHITE, pauseMenu.getWidth()/2, pauseMenu.getHeight()/2,
                        bkgCenterX, bkgCenterY, 0, PMENU_SCALE * scale, PMENU_SCALE * scale);
                canvas.draw(pauseCont, Color.WHITE, pauseCont.getRegionWidth()/2, pauseCont.getRegionHeight()/2,
                        pButtonsCenterX, pContCenterY, 0, PBUTTON_SCALE * scale, PBUTTON_SCALE * scale);
                canvas.draw(pauseRestart, Color.WHITE, pauseRestart.getRegionWidth()/2, pauseRestart.getRegionHeight()/2,
                        pButtonsCenterX, pRestartCenterY, 0, PBUTTON_SCALE * scale, PBUTTON_SCALE * scale);
                canvas.draw(pauseOptions, Color.WHITE, pauseOptions.getRegionWidth()/2, pauseOptions.getRegionHeight()/2,
                        pButtonsCenterX, pOptionsCenterY, 0, PBUTTON_SCALE * scale, PBUTTON_SCALE * scale);
                canvas.draw(pauseQuit, Color.WHITE, pauseQuit.getRegionWidth()/2, pauseQuit.getRegionHeight()/2,
                        pButtonsCenterX, pQuitCenterY, 0, PBUTTON_SCALE * scale, PBUTTON_SCALE * scale);

                break;
            case WIN:
                canvas.draw(winMenu, Color.WHITE, winMenu.getWidth()/2, winMenu.getHeight()/2,
                        bkgCenterX, bkgCenterY, 0, WMENU_SCALE *scale, WMENU_SCALE*scale);
                if (levelAvailable) {
                    canvas.draw(winNext, Color.WHITE, winNext.getRegionWidth() / 2, winNext.getRegionHeight() / 2,
                            wNextCenterX, wButtonsCenterY, 0, LWBUTTON_SCALE * scale, LWBUTTON_SCALE * scale);
                } else {
                    canvas.draw(winNextGrayTexture, Color.WHITE, winNextGrayTexture.getWidth()/2, winNextGrayTexture.getHeight()/2,
                            wNextCenterX, wButtonsCenterY, 0, LWBUTTON_SCALE *scale, LWBUTTON_SCALE*scale);
                }
                canvas.draw(winReplay, Color.WHITE, winReplay.getRegionWidth()/2, winReplay.getRegionHeight()/2,
                        wReplayCenterX, wButtonsCenterY, 0, LWBUTTON_SCALE *scale, LWBUTTON_SCALE *scale);
                canvas.draw(winQuit, Color.WHITE, winQuit.getRegionWidth()/2, winQuit.getRegionHeight()/2,
                        wQuitCenterX, wButtonsCenterY, 0, LWBUTTON_SCALE *scale, LWBUTTON_SCALE *scale);
                break;
            case LOSE:
                canvas.draw(loseMenu, Color.WHITE, loseMenu.getWidth()/2, loseMenu.getHeight()/2,
                        bkgCenterX, bkgCenterY, 0, WMENU_SCALE *scale, WMENU_SCALE*scale);
                canvas.draw(loseRetry, Color.WHITE, loseRetry.getRegionWidth()/2, loseRetry.getRegionHeight()/2,
                        lRetryCenterX, lButtonsCenterY, 0, LWBUTTON_SCALE *scale, LWBUTTON_SCALE *scale);
                canvas.draw(loseQuit, Color.WHITE, loseQuit.getRegionWidth()/2, loseQuit.getRegionHeight()/2,
                        lQuitCenterX, lButtonsCenterY, 0, LWBUTTON_SCALE *scale, LWBUTTON_SCALE *scale);
                break;
        }
        canvas.setIgnore(false);
        canvas.end();
    }

    // ADDITIONAL SCREEN METHODS
    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only pauseQuit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            preUpdate(delta);
            update(delta);
            draw();
            if(somethingSelected() && listener != null){
                listener.exitScreen(this, pressState % 10);
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

        bkgCenterX = width*.495f;
        bkgCenterY = height/2;

        pButtonsCenterX = width/2;
        pContCenterY = 3f*height/5;
        pRestartCenterY = pContCenterY - 2f * BUTTON_HEIGHT * PBUTTON_SCALE;
        pOptionsCenterY = pRestartCenterY - 2f * BUTTON_HEIGHT * PBUTTON_SCALE;
        pQuitCenterY = pOptionsCenterY - 2f * BUTTON_HEIGHT * PBUTTON_SCALE;

        wButtonsCenterY = 1.25f*height/5;
        wNextCenterX = 4.2f*width/8;
        wReplayCenterX = 5.05f*width/8;
        wQuitCenterX = 5.9f*width/8;

        lButtonsCenterY = 1.35f*height/5;
        lRetryCenterX = 5.0f*width/8;
        lQuitCenterX = 5.9f*width/8;

        heightY = height;
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
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
        canvas.setTintGray(true);
        activateInputProcessor(true);
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
        canvas.setTintGray(false);
        activateInputProcessor(false);
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to pauseQuit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT
    /**
     * Returns if this position is over the given RECTANGULAR button
     * @param screenX The x axis screen position of the mouse interaction
     * @param screenY The y axis screen position of the mouse interaction
     * @param centerX The x axis center location of the button
     * @param centerY The y axis center location of the button
     * @param bwidth   The width of the given button
     * */
    private boolean overButton(int bwidth, int screenX, int screenY, float centerX, float centerY){
        float width = PBUTTON_SCALE * scale * bwidth;
        float height = PBUTTON_SCALE * scale * BUTTON_HEIGHT;
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
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Flip to match graphics coordinates
        Gdx.input.setCursorCatched(false);
        screenY = heightY-screenY;

        switch(mode) {
            case PAUSE:
                if(overButton(PCONT_WIDTH, screenX, screenY, pButtonsCenterX, pContCenterY)){
                    pressState = 1;
                    sound.playMenuEnter();
                }else if(overButton(PRESTART_WIDTH, screenX, screenY, pButtonsCenterX, pRestartCenterY)){
                    pressState = 2;
                    sound.playMenuEnter();
                }else if(overButton(POPTIONS_WIDTH, screenX, screenY, pButtonsCenterX, pOptionsCenterY)){
                    pressState = 3;
                    sound.playMenuEnter();
                }else if(overButton(PQUIT_WIDTH, screenX, screenY, pButtonsCenterX, pQuitCenterY)) {
                    pressState = 4;
                    sound.playMenuEnter();
                }
                break;
            case WIN:
                if(levelAvailable && overButton(WNEXT_WIDTH, screenX, screenY, wNextCenterX, wButtonsCenterY)){
                    pressState = 1;
                    sound.playMenuEnter();
                }else if (overButton(WREPLAY_WIDTH, screenX, screenY, wReplayCenterX, wButtonsCenterY)){
                    pressState = 2;
                    sound.playMenuEnter();
                } else if (overButton(WQUIT_WIDTH, screenX, screenY, wQuitCenterX, wButtonsCenterY)){
                    pressState = 4;
                    sound.playMenuEnter();
                }
                break;
            case LOSE:
                if(overButton(LRETRY_WIDTH, screenX, screenY, lRetryCenterX, lButtonsCenterY)){
                    pressState = 2;
                    sound.playMenuEnter();
                } else if (overButton(LQUIT_WIDTH, screenX, screenY, lQuitCenterX, lButtonsCenterY)){
                    pressState = 4;
                    sound.playMenuEnter();
                }
                break;
        }



        return false;
    }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Gdx.input.setCursorCatched(false);
        if (pressState > 0 && pressState <= 10){
            pressState = pressState + 10;
            return false;
        }
        return true;
    }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) {
        if (pressState == 0) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart ) {
                pressState = 1; //press start
                return false;
            }
        }
        return true;
    }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) {
        if (pressState == 1) {
            ControllerMapping mapping = controller.getMapping();
            if (mapping != null && buttonCode == mapping.buttonStart ) {
                pressState = 11; //select start
                return false;
            }
        }
        return true;
    }

    /** Reset the default button animations */
    private void resetButtons(){
        switch(mode) {
            case PAUSE:
                if (selected != 0) pauseCont.setFrame(0);
                if (selected != 1) pauseRestart.setFrame(0);
                if (selected != 2) pauseOptions.setFrame(0);
                if (selected != 3) pauseQuit.setFrame(0);
                break;
            case WIN:
                if (selected != 0) winNext.setFrame(0);
                if (selected != 1) winReplay.setFrame(0);
                if (selected != 3) winQuit.setFrame(0);
                break;
            case LOSE:
                if (selected != 1) loseRetry.setFrame(0);
                if (selected != 3) loseQuit.setFrame(0);
                break;
        }
    }

    /**
     * Called when the mouse was moved without any buttons being pressed.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        Gdx.input.setCursorCatched(false);
        // Flip to match graphics coordinates
        screenY = heightY-screenY;
        //switch animations
        switch (mode) {
            case PAUSE:
                if (overButton(PCONT_WIDTH, screenX, screenY, pButtonsCenterX, pContCenterY)){
                    selected = 0;
                    pauseCont.setFrame(1);
                } else if(overButton(PRESTART_WIDTH, screenX, screenY, pButtonsCenterX, pRestartCenterY)){
                    selected = 1;
                    pauseRestart.setFrame(1);
                } else if(overButton(POPTIONS_WIDTH, screenX, screenY, pButtonsCenterX, pOptionsCenterY)){
                    selected = 2;
                    pauseOptions.setFrame(1);
                } else if(overButton(PQUIT_WIDTH, screenX, screenY, pButtonsCenterX, pQuitCenterY)){
                    selected = 3;
                    pauseQuit.setFrame(1);
                }
                break;
            case WIN:
                if(levelAvailable && overButton(WNEXT_WIDTH, screenX, screenY, wNextCenterX, wButtonsCenterY)){
                    selected = 0;
                    winNext.setFrame(1);
                }else if (overButton(WREPLAY_WIDTH, screenX, screenY, wReplayCenterX, wButtonsCenterY)){
                    selected = 1;
                    winReplay.setFrame(1);
                } else if (overButton(WQUIT_WIDTH, screenX, screenY, wQuitCenterX, wButtonsCenterY)){
                    selected = 3;
                    winQuit.setFrame(1);
                }
                break;
            case LOSE:
                if(overButton(LRETRY_WIDTH, screenX, screenY, lRetryCenterX, lButtonsCenterY)){
                    selected = 1;
                    loseRetry.setFrame(1);
                } else if (overButton(LQUIT_WIDTH, screenX, screenY, lQuitCenterX, lButtonsCenterY)){
                    selected = 3;
                    loseQuit.setFrame(1);
                }
                break;
        }
        resetButtons();
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

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
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
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
     * Called when an axis on the Controller moved. (UNSUPPORTED)
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
