package code.game.display;

import code.assets.AssetDirectory;
import code.audio.SoundBuffer;
import code.game.controllers.SoundController;
import code.game.views.GameCanvas;
import code.util.Controllers;
import code.util.ScreenListener;
import code.util.XBoxController;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class LevelSelectMode implements Screen, InputProcessor, ControllerListener {
    /** The assets of the screen */
    private AssetDirectory assets;
    /** Holds all the level file locations of the game */
    private JsonValue levels;

    /** Background texture for start-up */
    private Texture background;
    /** The texture for the knife*/
    private Texture knifeTexture;
    /** The font for the text */
    private BitmapFont displayFont;
    /** The font for info text */
    private BitmapFont infoFont;
    /** The texture for the selection arrows */
    private Texture arrowLeftTexture;
    private Texture arrowRightTexture;
    /** The texture for the back button */
    private Texture backTexture;

    private float themeCounter = 0;
    private final float THEME_DURATION = 72f;
    private SoundBuffer theme;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Height of info text character */
    private static float INFO_HEIGHT = 28;
    /** Width of info text character */
    private static float INFO_WIDTH = 15.5f;
    /** Width of the blade of the knife */
    private static float BLADE_WIDTH;
    /** Height of the blade of the knife */
    private static float BLADE_HEIGHT;
    /** Ratio of the knife to the screen */
    private static float KNIFE_RATIO = 1.0f;
    /** Background scale*/
    private static float BACKGROUND_SCALE = 1.35f;
    /** Scale of arrows*/
    private static float ARROW_SCALE = 0.5f;
    /** Scale of back button */
    private static float BACK_SCALE = 0.75f;
    /** The distance between each knife */
    private static float DIST = 300;
    /** Shadow offset */
    private static float SHADOW_OFFSET = 35;

    /** Exit code signaling that a level has been selected */
    public static final int EXIT_LEVEL = 0;
    /** Exit code signaling that we are returning to the main menu */
    public static final int EXIT_MENU = 1;

    /**Center the background*/
    private int bkgCenterX;
    private int bkgCenterY;

    /**Center the knife*/
    private float knifeCenterX;
    private float knifeCenterY;

    /**Center the actual blade of the knife */
    private float bladeCenterX;
    private float bladeCenterY;

    /** info about the level */
    private float textCenterY;

    /** center for arrows*/
    private int arrowCenterY;
    private int leftArrowCenterX;
    private int rightArrowCenterX;

    /** center for back button */
    private int backCenterY;
    private int backCenterX;

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
    /**The list of levels*/
    private String[] levelList;
    /**
     * The status of user input
     * 1 for clicked on knife, 2 for selected knife
     * 3 for clicked left arrow, 4 for selected left arrow
     * 5 for clicked right arrow, 6 for selected right arrow
     * 7 for clicked on back, 8 for selected back
     * */
    private int  pressState;
    /**The total number of levels we have. Should be equal to length of levelList. */
    private int numLevels;

    /** The first knife on the screen, i.e. left-most knife in level select */
    private int leftIndex;

    /** The level info of the selected level, to pass onto GameController*/
    private JsonValue levelSelected;
    /** Whether or not this player mode is still active */
    private boolean active;
    /** The index of the highlighted knife */
    private int highlightedIndex;
    /** Whether left arrow is highlighted */
    private boolean leftHighlighted;
    /** Whether right arrow is highlighted */
    private boolean rightHighlighted;

    /**
     * Creates a LevelSelectMode with the default size and position.
     * @param canvas 	The game canvas to draw to
     */
    public LevelSelectMode(AssetDirectory assets, GameCanvas canvas, SoundController sound) {
        this.canvas  = canvas;
        this.assets = assets;
        this.sound = sound;

        background = assets.getEntry( "background:levelselect", Texture.class );
        background.setFilter( Texture.TextureFilter.Linear, Texture.TextureFilter.Linear );

        knifeTexture = assets.getEntry("ui:knife", Texture.class );
        arrowRightTexture = assets.getEntry("ui:arrowRight", Texture.class);
        arrowLeftTexture = assets.getEntry("ui:arrowLeft", Texture.class);
        backTexture = assets.getEntry("ui:back", Texture.class);
        displayFont = assets.getEntry("font:PTSans64", BitmapFont.class);
        infoFont = assets.getEntry("font:PTSans32", BitmapFont.class);
        displayFont.setColor(Color.BLACK);
        infoFont.setColor(Color.BLACK);
        //TODO probably want to share some font assets
        levels = assets.getEntry("levels", JsonValue.class );
        theme = assets.getEntry("sound:music:levelSel", SoundBuffer.class);

        resize(canvas.getWidth(),canvas.getHeight());

        levelList = new String[levels.size];
        for (int i = 0; i < levels.size; i++){
            levelList[i] = levels.get(i).name;
        }
        //levelList = levels.asStringArray();
        leftIndex = 0;

        numLevels = levelList.length;

        pressState = -1; //initialize as -1, which is an invalid level
        //Gdx.input.setInputProcessor( this );

        // Let ANY connected controller start the game.
        for (XBoxController controller : Controllers.get().getXBoxControllers()) {
            controller.addListener(this);
        }
        highlightedIndex = 1;
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

    /**
     * Sets the JSON value of levelSelected to the given name, specified in levelselect.json
     * */
    private void setLevelSelected(String name){
        JsonReader json = new JsonReader();
        try{
            JsonValue jsonVal = json.parse(Gdx.files.internal(levels.getString(name)));
            levelSelected = jsonVal;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**Resets this screen*/
    public void reset(){
        theme.stop();
        themeCounter = 0f;
        pressState = -1;
    }

    /**
     * Gets the JsonValue of the level this mode selected
     * Can be null
     * */
    public JsonValue getLevelSelected(){
        return levelSelected;
    }

    /**
     * Checks if pressState pressed level
     * @return  whether or not pressed a knife
     */
    private boolean validLevelSelected(){
        if(pressState == 2){
            theme.stop();
            return true;
        }
        return false;
    }

    /**
     * Checks if pressState pressed back
     * @return whether or not pressed back
     */
    private boolean backSelected(){
        if(pressState == 8) { return true; }
        return false;
    }

    /**Switch to the next level, loop back around at edges */
    private void switchToLevel(boolean right){
        if (right) { leftIndex += 1; } else { leftIndex -= 1; }
    }

    private String getText(int i){
        return levelList[i];
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        assets.unloadAssets();
        assets.dispose();
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
        canvas.begin();
        canvas.draw(background, Color.WHITE, background.getWidth()/2 - 10, background.getHeight()/2,
                bkgCenterX, bkgCenterY, 0, BACKGROUND_SCALE * scale, BACKGROUND_SCALE * scale);
        //draw knife and two arrows
        Color leftTint = (leftHighlighted ? Color.RED: Color.GRAY);
        Color rightTint = (rightHighlighted ? Color.RED: Color.GRAY);
        String text;
        GlyphLayout layout = new GlyphLayout();
        for (int i = 0; i < 3; i++) {
            // Draw the knife
            if (i == highlightedIndex) {
                canvas.draw(knifeTexture, Color.DARK_GRAY, knifeTexture.getWidth()/2, knifeTexture.getHeight()/2,
                        knifeCenterX+DIST*highlightedIndex-SHADOW_OFFSET, knifeCenterY-SHADOW_OFFSET, 0, KNIFE_RATIO * scale, KNIFE_RATIO * scale);
            }
            canvas.draw(knifeTexture, Color.WHITE, knifeTexture.getWidth()/2, knifeTexture.getHeight()/2,
                    knifeCenterX+DIST*i, knifeCenterY, 0, KNIFE_RATIO * scale, KNIFE_RATIO * scale);


            // Draw level name
            text = getText(leftIndex+i);
            layout.setText(infoFont, text);
            if (layout.width > BLADE_WIDTH * KNIFE_RATIO * scale) {
                int row = (int)Math.ceil(layout.width / BLADE_WIDTH * KNIFE_RATIO * scale) - 1;
                String string = "";
                for (String word: text.split(" ")) {
                    layout.setText(infoFont, string + word);
                    if (layout.width >= BLADE_WIDTH * KNIFE_RATIO * scale) {
                        layout.setText(infoFont, string.substring(0, string.length()-1));
                        canvas.drawText(string, infoFont, bladeCenterX+DIST*i-layout.width/2, bladeCenterY+30+INFO_HEIGHT*row);
                        row -= 1;
                        string = "";
                    }
                    string += word + " ";
                }
                if (string != "") {
                    layout.setText(infoFont, string.substring(0, string.length()-1));
                    canvas.drawText(string, infoFont, bladeCenterX+DIST*i-layout.width/2, bladeCenterY+30+INFO_HEIGHT*row);
                }
            }
            else { canvas.drawText(text, infoFont, bladeCenterX + i*DIST-layout.width/2, bladeCenterY+30+INFO_HEIGHT/2); }

            // Draw level number
            layout.setText(displayFont, ""+(leftIndex+i+1));
            canvas.drawText(""+(leftIndex+i+1), displayFont, bladeCenterX+DIST*i-layout.width/2, bladeCenterY-20);
        }
        //canvas.drawText("|", infoFont, bladeCenterX, bladeCenterY);
        //arrow
        if (leftIndex > 0) {
            canvas.draw(arrowLeftTexture, leftTint, arrowLeftTexture.getWidth() / 2, arrowLeftTexture.getHeight() / 2,
                    leftArrowCenterX, arrowCenterY, 0, ARROW_SCALE * scale, ARROW_SCALE * scale);
        }
        if (leftIndex + 2 < numLevels-1) {
            canvas.draw(arrowRightTexture, rightTint, arrowRightTexture.getWidth() / 2, arrowRightTexture.getHeight() / 2,
                    rightArrowCenterX, arrowCenterY, 0, ARROW_SCALE * scale, ARROW_SCALE * scale);
        }
        canvas.draw(backTexture, Color.WHITE, backTexture.getWidth()/2, backTexture.getHeight()/2,
                backCenterX, backCenterY, 0, BACK_SCALE*scale, BACK_SCALE*scale);
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
            if (validLevelSelected() && listener != null){
                listener.exitScreen(this, EXIT_LEVEL); //nothing much to do with exit code
            } else if (backSelected() && listener != null){
                listener.exitScreen(this, EXIT_MENU);
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

        arrowCenterY = height/2;
        leftArrowCenterX = width/10;
        rightArrowCenterX = 9 * width/10;

        backCenterX = width/15;
        backCenterY = height/12;

/*        knifeCenterX = width/2;
        knifeCenterY = height/3;*/
        knifeCenterX = (float)(width * .18 + knifeTexture.getWidth()/2);
        knifeCenterY = height/3;

        bladeCenterX = (float)(knifeCenterX - 0.12 * knifeTexture.getWidth());
        bladeCenterY = (float)(knifeCenterY + 0.15 * knifeTexture.getHeight());

        BLADE_WIDTH = knifeTexture.getWidth()*0.75f;
        BLADE_HEIGHT = knifeTexture.getHeight()*0.7f;

        textCenterY = knifeCenterY;
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

    // PROCESSING PLAYER INPUT
    /**
     * Returns if this position is over the given RECTANGULAR button
     * @param screenX The x axis screen position of the mouse interaction
     * @param screenY The y axis screen position of the mouse interaction
     * @param centerX The x axis center location of the button
     * @param centerY The y axis center location of the button
     * @param button The texture of the given button
     * */
    private int overKnife(Texture button, float screenX, float screenY, float centerX, float centerY){
        float width = KNIFE_RATIO * scale * BLADE_WIDTH;
        float height = KNIFE_RATIO * scale * BLADE_HEIGHT;
        int i;
        float xBound, yBound;
        if (screenX <= centerX + width/2) { i = 0; xBound = centerX - width/2; }
        else if (screenX <= centerX + width/2 + DIST) { i = 1; xBound = centerX + DIST - width/2; }
        else { i = 2; xBound = centerX + DIST*2 - width/2; }
        yBound = centerY - height/2;
/*        float xBound = centerX - width/2; //lower x bound
        float yBound = centerY - height/2;*/
        if ((screenX >= xBound && screenX <= xBound + width) && (screenY >= yBound && screenY <= yBound + height)) {
            return i;
        }
        return -1;
    }
    /**
     * Returns if this position is over the given ARROW button
     * @param screenX The x axis screen position of the mouse interaction
     * @param screenY The y axis screen position of the mouse interaction
     * @param centerX The x axis center location of the button
     * @param centerY The y axis center location of the button
     * @param button The texture of the given button
     * */
    private boolean overArrow(Texture button, int screenX, int screenY, int centerX, int centerY){
        //TODO make it not a rectangular area
        float width = ARROW_SCALE * scale * button.getWidth();
        float height = ARROW_SCALE * scale * button.getHeight();
        float xBound = centerX - width/2; //lower x bound
        float yBound = centerY - height/2;
        return (screenX >= xBound && screenX <= xBound + width) && (screenY >= yBound && screenY <= yBound + height);
    }

    /**
     * Returns if this position is over the given ARROW button
     * @param screenX The x axis screen position of the mouse interaction
     * @param screenY The y axis screen position of the mouse interaction
     * @param centerX The x axis center location of the button
     * @param centerY The y axis center location of the button
     * @param button The texture of the given button
     * */
    private boolean overBack(Texture button, int screenX, int screenY, int centerX, int centerY){
        //TODO make it not a rectangular area
        float width =  scale * button.getWidth();
        float height = scale * button.getHeight();
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
        screenY = heightY-screenY;

        if(leftIndex - 1 >= 0 && overArrow(arrowLeftTexture, screenX, screenY, leftArrowCenterX, arrowCenterY)){
            pressState = 3;
        }else if(leftIndex + 3 < numLevels && overArrow(arrowRightTexture, screenX, screenY, rightArrowCenterX, arrowCenterY)){
            pressState = 5;
        } else if(overKnife(knifeTexture, screenX, screenY, bladeCenterX, bladeCenterY) >= 0) {
            pressState = 1;
        } else if (overBack(backTexture, screenX, screenY, backCenterX, backCenterY)) {
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
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressState == 1){
            pressState = 2;
            setLevelSelected(levelList[leftIndex + highlightedIndex]);
            return false;
        }else if (pressState == 3){
            pressState = 4;
            switchToLevel(false);
        }else if (pressState == 5){
            pressState = 6;
            switchToLevel(true);
        } else if (pressState == 7){
            pressState = 8;

        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor
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
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        screenY = heightY-screenY;
        highlightedIndex = overKnife(knifeTexture, screenX, screenY, bladeCenterX, bladeCenterY);
        leftHighlighted = overArrow(arrowLeftTexture, screenX, screenY, leftArrowCenterX, arrowCenterY);
        rightHighlighted = overArrow(arrowRightTexture, screenX, screenY, rightArrowCenterX, arrowCenterY);
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
