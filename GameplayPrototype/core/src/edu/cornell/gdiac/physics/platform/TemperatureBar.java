package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.GameCanvas;

public class TemperatureBar {
    /**The max temperature the chicken can get to (when cooked) */
    private int maxTemperature;
    /**Current temperature of the chicken */
    private int temperature;

    /**Animated progress bar for temperature*/
    private ProgressBar tempBar;
    /** Texture atlas to support a temperature bar */
    private Texture tempTexture;
    // tempBar is a "texture atlas." Break it up into parts.
    /** Temperature background */
    private TextureRegion tempBackground;
    /** Middle portion of the temperature forground (colored region) */
    private TextureRegion tempForeground;
    //private int WIDTH = 1;
    //private int HEIGHT = 300;


    /**Using to determine how fast the chicken cooks */
    private final float TEMPERATURE_TIMER = 1f;
    private float temperatureCounter = 0f;

    /**The amount of heat the stove gives the player for standing by it (per second) */
    private int stoveHeat = 2;
    private final float LOSE_HEAT_RATE = 0.1f;


    /**The font*/
    private BitmapFont font = new BitmapFont();

    /**Create a new temperature bar with temperature range 0 thru max*/
    public TemperatureBar(int max){
        maxTemperature = max;
        temperature = 0;
        //init temperature bar
        gatherTempAssets();

        ProgressBar.ProgressBarStyle barStyle =
                new ProgressBar.ProgressBarStyle(new TextureRegionDrawable(tempBackground),
                        new TextureRegionDrawable(tempForeground));
        barStyle.knobBefore = barStyle.knob;

        //barStyle.background = new TextureRegionDrawable(tempBackground);
        //barStyle.knob = new TextureRegionDrawable(tempForeground);
        //barStyle.knob.setMinHeight(0);
        //barStyle.knob.setTopHeight(HEIGHT);
        //barStyle.knobAfter = barStyle.knob;
        //barStyle.knobBefore = barStyle.background;
        tempBar = new ProgressBar(0, maxTemperature, 1, true, barStyle);
        //tempBar.setWidth(WIDTH);
        //tempBar.setHeight(HEIGHT);
        tempBar.setAnimateDuration(1);

    }
    /**Gather art assets for temperature from the JSON file specifications and the corresponding image*/
    private void gatherTempAssets(){
        AssetDirectory internal = new AssetDirectory("tempbar.json");
        internal.loadAssets();
        internal.finishLoading();
        tempTexture = internal.getEntry("tempbar", Texture.class);
        tempBackground = internal.getEntry("progress.background", TextureRegion.class);
        tempForeground = internal.getEntry("progressfull.foreground", TextureRegion.class);
        //tempForeground.flip(false,true);
    }
    /** Returns the temperature of the chicken
     *
     * @return temperature of the chicken
     * */
    public int getTemperature() {
        return temperature;
    }

    /**Returns the % the chicken is cooked, for calculation*/
    public float getPercentCooked(){
        return (float) temperature/ (float) maxTemperature;
    }

    /**Returns true if the chicken is cooked (temp > max) and false otherwise
     *
     * @return true if chicken is cooked and false otherwise
     */
    public boolean isCooked(){
        return (temperature >= maxTemperature);
    }
    /** If incr is true, increases the temperature of the chicken, void otherwise
     * @param incr - whether or not the temperature should be increasing
     */

    public void cook(boolean incr){
        if (temperatureCounter >= TEMPERATURE_TIMER){
            temperature = MathUtils.clamp(incr ? temperature+stoveHeat : temperature,0,30);
            // temperature = incr ? temperature+stoveHeat : temperature,0,30;
            temperatureCounter = 0f;
        }
    }
    /** Decreases the temperature of the player's chicken by a given amount
     *
     * @param amt - the amount to decrease by
     */
    public void reduceTemp(int amt) {
        temperature -= amt;
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt){
        temperatureCounter = MathUtils.clamp(temperatureCounter += dt, 0f, TEMPERATURE_TIMER);
        //update progress bar
        tempBar.setValue(temperature);
    }
    public void draw(GameCanvas canvas){
        //draw temperature
        //System.out.println(getTemperature());
        float scale = 1.5f;
        canvas.draw(tempBackground, Color.WHITE, 960f, 250f,  tempBackground.getRegionWidth()/scale, tempBackground.getRegionHeight()/scale);
        canvas.draw(tempForeground, (tempBar.getValue() / maxTemperature), Color.WHITE,
                tempForeground.getRegionWidth() / scale, tempForeground.getRegionHeight() / scale, 960f, 250f,
                tempForeground.getRegionWidth() / scale, tempForeground.getRegionHeight() / scale);

        //tempBar.draw(canvas, temperature);
        //canvas.drawText("Temp: "+tempBar.getValue(), font, 500,565);
       // canvas.drawText("Temp: "+temperature, font, 575,565);
    }
}
