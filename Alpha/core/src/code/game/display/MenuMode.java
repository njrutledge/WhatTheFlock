package code.game.display;

import code.assets.AssetDirectory;
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
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class MenuMode implements Screen, InputProcessor, ControllerListener {
    // There are TWO asset managers.  One to load the loading screen.  The other to load the assets
    /** Internal assets for this loading screen */
    private AssetDirectory internal;

    /** Background texture for loading */
    private Texture background;
    /** How to play button*/
    private Texture howToPlayTexture;
    private FilmStrip howToPlay;
    /** Options button*/
    private Texture optionsTexture;
    private FilmStrip options;
    /** Quit button*/
    private Texture quitTexture;
    private FilmStrip quit;
    /** Start button*/
    private Texture startTexture;
    private FilmStrip start;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Height of the progress bar */
    private static float BUTTON_SCALE  = 0.75f;
    /** Background scale*/
    private static float BACKGROUND_SCALE = 1.3f;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /**
     * The current state of the play button
     * 0 for nothing,
     * 1 for start pressed down, 11 for start selected
     * 2 for how to play pressed down, 12 for how to play selected
     * 3 for options pressed down, 13 for options selected
     * 4 for quit pressed down,  14 for quit selected
     * */
    private int   pressState;
    /**Corresponding codes to pressState*/
    public static final int START = 1;
    public static final int GUIDE  = 2;
    public static final int OPTIONS = 3;
    public static final int QUIT = 4;

    /**The maximum number of buttons you want to support*/
    private final int NUM_BUTTONS = 10;

    /** Whether or not this player mode is still active */
    private boolean active;

    /**Center the background*/
    private int bkgCenterX;
    private int bkgCenterY;

    /**Center the various buttons*/
    private int buttonsCenterX;
    private int howToCenterY;
    private int optionsCenterY;
    private int quitCenterY;
    private int startCenterY;

    /**
     * Returns true if the user has selected an option from the menu
     *
     * @return if the player is ready to do something
     * */
    private boolean somethingSelected() {
        return pressState > 10 && pressState < 15;
    }

    /**
     * Creates a MenuMode with the default size and position.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param canvas 	The game canvas to draw to
     */
    public MenuMode(GameCanvas canvas) {
        this.canvas  = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        // We need these files loaded immediately
        internal = new AssetDirectory( "loading.json" );
        internal.loadAssets();
        internal.finishLoading();

        //button textures
        startTexture = internal.getEntry("start", Texture.class);
        start = new FilmStrip(startTexture, 1, 2);
        howToPlayTexture = internal.getEntry("howtoplay", Texture.class);
        howToPlay = new FilmStrip(howToPlayTexture, 1, 2);
        optionsTexture = internal.getEntry("options", Texture.class);
        options = new FilmStrip(optionsTexture, 1, 2);
        quitTexture = internal.getEntry("quit", Texture.class);
        quit = new FilmStrip(quitTexture, 1, 2);


        // Load the next two images immediately.
        background = internal.getEntry( "menu_background", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );
        pressState = 0;

        Gdx.input.setInputProcessor( this );

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener( this );
        }
        active = true;
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
    }

    /** Returns if all the button textures have been loaded in, which would mean that the game is done loading in
     * its assets in update */
    private boolean allAssetsLoaded(){
        return !(startTexture == null || howToPlayTexture == null || optionsTexture == null
                || quitTexture == null);
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
        //do nothing
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        //canvas.draw(background, 0, 0);
        canvas.draw(background, Color.WHITE, background.getWidth()/2, background.getHeight()/2,
                bkgCenterX, bkgCenterY, 0, BACKGROUND_SCALE * scale, BACKGROUND_SCALE * scale);

            //draw buttons
            Color startTint = (pressState == 1 ? Color.RED: Color.WHITE);
            Color howTint = (pressState == 2 ? Color.RED: Color.WHITE);
            Color optTint = (pressState == 3 ? Color.RED: Color.WHITE);
            Color quitTint = (pressState == 4 ? Color.RED: Color.WHITE);

            //start
            canvas.draw(start, startTint, start.getRegionWidth()/2, start.getRegionHeight()/2,
                    buttonsCenterX, startCenterY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            //how to play
            canvas.draw(howToPlay, howTint, howToPlay.getRegionWidth()/2, howToPlay.getRegionHeight()/2,
                    buttonsCenterX, howToCenterY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            //options
            canvas.draw(options, optTint, options.getRegionWidth()/2, options.getRegionHeight()/2,
                    buttonsCenterX, optionsCenterY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);
            //quit
            canvas.draw(quit, quitTint, quit.getRegionWidth()/2, quit.getRegionHeight()/2,
                    buttonsCenterX, quitCenterY, 0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

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

        bkgCenterX = width/2;
        bkgCenterY = height/2;
        buttonsCenterX = width/2;
        startCenterY = height/2;
        howToCenterY = 4 * height/10;
        optionsCenterY = 3 * height/10;
        quitCenterY = 2 * height/10;
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
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
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
     * @param button The texture of the given button
     * */
    private boolean overButton(Texture button, int screenX, int screenY, int centerX, int centerY){
        float width = BUTTON_SCALE * scale * button.getWidth();
        float height = BUTTON_SCALE * scale * button.getHeight();
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
        screenY = heightY-screenY;

        // TODO: Fix scaling
        if(overButton(start.getTexture(), screenX, screenY, buttonsCenterX, startCenterY)){
            pressState = 1;
        }else if(overButton(howToPlay.getTexture(), screenX, screenY, buttonsCenterX, howToCenterY)){
            pressState = 2;
        }else if(overButton(options.getTexture(), screenX, screenY, buttonsCenterX, optionsCenterY)){
            pressState = 3;
        }else if(overButton(quit.getTexture(), screenX, screenY, buttonsCenterX, quitCenterY)) {
            pressState = 4;
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
        start.setFrame(0);
        howToPlay.setFrame(0);
        options.setFrame(0);
        quit.setFrame(0);
    }

    /**
     * Called when the mouse was moved without any buttons being pressed.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        if(!allAssetsLoaded()){
            return true;
        }
        resetButtons();
        // Flip to match graphics coordinates
        screenY = heightY-screenY;
        //switch animations
        if (overButton(start.getTexture(), screenX, screenY, buttonsCenterX, startCenterY)){
            start.setFrame(1);
        } else if(overButton(howToPlay.getTexture(), screenX, screenY, buttonsCenterX, howToCenterY)){
            howToPlay.setFrame(1);
        } else if(overButton(options.getTexture(), screenX, screenY, buttonsCenterX, optionsCenterY)){
            options.setFrame(1);
        } else if(overButton(quit.getTexture(), screenX, screenY, buttonsCenterX, quitCenterY)){
            quit.setFrame(1);
        }
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
