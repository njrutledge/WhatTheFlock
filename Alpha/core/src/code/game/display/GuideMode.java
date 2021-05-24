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

import java.util.ArrayList;
import java.util.List;


public class GuideMode implements Screen, InputProcessor, ControllerListener {
    /** The assets of the screen */
    private AssetDirectory assets;

    /** Background texture */
    private Texture background;

    /** Texture for ok */
    private Texture okTexture;
    private FilmStrip ok;
    /** Texture for arrow */
    private Texture arrowRightTexture;
    private FilmStrip arrowRight;
    private Texture arrowLeftTexture;
    private FilmStrip arrowLeft;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 48.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 27.0f;

    /** Scale of the menu */
    private final float BACKGROUND_SCALE = .5f;
    /** Scale of the buttons */
    private final float BUTTON_SCALE = 0.5f;

    /** Constants for arrow drawing */
    private float leftArrowCenterX = 450;
    private float rightArrowCenterX = 1750;
    private float arrowCenterY = 500;
    private final float ARROW_SCALE = 0.5f;

    /** The constants for the selection box */
    private final float OK_HEIGHT = 46;
    private final float OK_WIDTH = 70;
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
    private float okCenterY;

    private MainMenuMode menu;

    /** 1 = selected OK, 11 = confirmed OK
     * 2 = selected left, 12 = confirmed left
     * 3 = selected right, 13 = confirmed right */
    private int pressState = 0;
    /** 0 = page 1
     * 1 = page 2
     * 2 = page 3
     * 3 = page 4
     * 4 = page 5 */
    private int selected = 0;

    /** Whether back was selected */
    private boolean back = false;

    /** Whether or not this player mode is still active */
    private boolean active;

    /** The exit code that will be returned
     * 0 = return to main menu
     * */
    private int exitCode;

    /** The exit code representing returning to main menu */
    public static final int MAINMAIN = 0;
    /** list of possible background */
    private List<Texture> backgrounds = new ArrayList<>();

    /**
     * Creates a LevelSelectMode with the default size and position.
     * @param canvas 	The game canvas to draw to
     */
    public GuideMode(AssetDirectory assets, GameCanvas canvas, SoundController sound) {
        this.canvas  = canvas;
        this.assets = assets;
        this.sound = sound;

        okTexture = assets.getEntry("ui:options:ok", Texture.class);
        ok =  new FilmStrip(okTexture, 1, 2);

        backgrounds.add(assets.getEntry( "ui:guide:1", Texture.class ));
        backgrounds.add(assets.getEntry( "ui:guide:2", Texture.class ));
        backgrounds.add(assets.getEntry( "ui:guide:3", Texture.class ));
        backgrounds.add(assets.getEntry( "ui:guide:4", Texture.class ));
        backgrounds.add(assets.getEntry( "ui:guide:5", Texture.class ));
        background = backgrounds.get(0);
        //background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );

        //background = assets.getEntry("ui:options:menu", Texture.class );
        okTexture = assets.getEntry("ui:options:ok", Texture.class );

        arrowRightTexture = assets.getEntry("ui:arrowRight", Texture.class);
        arrowRight = new FilmStrip(arrowRightTexture, 1, 1);
        arrowLeftTexture = assets.getEntry("ui:arrowLeft", Texture.class);
        arrowLeft = new FilmStrip(arrowLeftTexture, 1, 1);
        this.bounds = new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
        this.vscale = new Vector2(1,1);
        selected = 0;
        resize(canvas.getWidth(),canvas.getHeight());

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener(this);
        }

        ok.setFrame(1);
        active = true;
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
        pressState = 0;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        assets.unloadAssets();
        assets.dispose();
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
        if (input.didESC()) {
            Gdx.input.setCursorCatched(true);
            back = true;
            sound.playMenuEnter();
            return false;
        }
        else if (input.didMovementKey() || input.didArrowKey()) {
            Gdx.input.setCursorCatched(true);
                // right
                if (input.getHorizontal() > 0 || input.getArrowHorizontal() > 0) {
                    if (selected != 4) { selected += 1; }
                }
                // left
                else if (input.getHorizontal() < 0 || input.getArrowHorizontal() < 0) {
                    if (selected != 0) { selected -= 1; }
                }
                background = backgrounds.get(selected);
        } else if (input.didEnter()) {
            sound.playMenuEnter();
            back = true;
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

    public void setMenu(MainMenuMode m){
        menu = m;
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
        menu.draw();
        canvas.setTintGray(false);
        canvas.begin();
        float selectCenterY = 0;
        canvas.draw(background, Color.WHITE, background.getWidth()/2, background.getHeight()/2,
                bkgCenterX, bkgCenterY, 0, BACKGROUND_SCALE * scale, BACKGROUND_SCALE * scale);

        canvas.draw(ok, Color.WHITE, ok.getRegionWidth()/2, ok.getRegionHeight()/2, bkgCenterX, okCenterY,
                0, BUTTON_SCALE * scale, BUTTON_SCALE * scale);

        int index = backgrounds.indexOf(background);

        if(index > 0) {
            canvas.draw(arrowLeft, Color.WHITE, arrowLeft.getRegionWidth() / 2, arrowLeft.getRegionHeight() / 2, leftArrowCenterX,
                    arrowCenterY, 0, ARROW_SCALE * scale, ARROW_SCALE * scale);
        }
        if(index < 4) {
            canvas.draw(arrowRight, Color.WHITE, arrowRight.getRegionWidth() / 2, arrowRight.getRegionHeight() / 2, rightArrowCenterX,
                    arrowCenterY, 0, ARROW_SCALE * scale, ARROW_SCALE * scale);
        }
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
                back = false;
                canvas.clear();
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

        okCenterY = height*0.075f;

        arrowCenterY = height * 0.5f;
        leftArrowCenterX = width * 0.08f;//0.078125f;
        rightArrowCenterX = width * 0.92f;

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

        if (overButton(OK_WIDTH, OK_HEIGHT, screenX, screenY, bkgCenterX, canvas.getHeight() - okCenterY)){
            pressState = 1;
        }
        else if (overButton(arrowLeft.getRegionWidth(), arrowLeft.getRegionHeight(), screenX, screenY, leftArrowCenterX, arrowCenterY)){
            pressState = 2;
        }
        else if (overButton(arrowRight.getRegionWidth(), arrowRight.getRegionHeight(), screenX, screenY, rightArrowCenterX, arrowCenterY)){
            pressState = 3;
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
        if(pressState == 1){
            back = true;
            return false;
        }
        else if (pressState == 2){
            int index = backgrounds.indexOf(background);
            if (index != 0){
                background = backgrounds.get(index-1);
            }
        }
        else if (pressState == 3){
            int index = backgrounds.indexOf(background);
            if (index != 4){
                background = backgrounds.get(index+1);
            }
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
