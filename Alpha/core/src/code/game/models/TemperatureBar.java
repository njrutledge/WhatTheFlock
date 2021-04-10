package code.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import code.game.views.GameCanvas;

public class TemperatureBar {
    /**The max temperature the chicken can get to (when cooked) */
    private int maxTemperature;
    /**Current temperature of the chicken */
    private float temperature;

    ///**Animated progress bar for temperature*/
    //private ProgressBar tempBar;
    ///** Texture atlas to support a temperature bar */
    //private Texture tempTexture;
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
    private final float COOL_DOWN_TIMER = 3f;
    private float cooldownCounter = 0f;
    /** boolean to enable the cooldown counter */
    private boolean useCooldown = false;

    /**The amount of heat the stove gives the player for standing by it (per second) */
    private int stoveHeat = 2;
    private final float COOLDOWN_RATE = 0.01f;


    /**The font*/
    private BitmapFont font = new BitmapFont();

    /**Create a new temperature bar with temperature range 0 thru max*/
    public TemperatureBar(TextureRegion back, TextureRegion front, int max){
        maxTemperature = max;
        temperature = 0;
        //init temperature bar
        //gatherTempAssets();
        tempBackground = back;
        tempForeground = front;
        /*ProgressBar.ProgressBarStyle barStyle =
                new ProgressBar.ProgressBarStyle(new TextureRegionDrawable(back),
                        new TextureRegionDrawable(front));
        barStyle.knobBefore = barStyle.knob;
        tempBar = new ProgressBar(0, maxTemperature, 1, true, barStyle);*/
    }

    /** Returns the temperature of the chicken
     *
     * @return temperature of the chicken
     * */
    public float getTemperature() {
        return temperature;
    }

    /**Returns the % the chicken is cooked, for calculation*/
    public float getPercentCooked(){
        return (float) temperature/ (float) maxTemperature;
    }

    /** enables or disables the cooldown effect
     *
     * @param val is true to enable the cooldown
     */
    public void setUseCooldown(boolean val){ useCooldown = val; }

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
/*        if (temperatureCounter >= TEMPERATURE_TIMER){
            temperature = MathUtils.clamp(incr ? temperature+stoveHeat : temperature,0,30);
            // temperature = incr ? temperature+stoveHeat : temperature,0,30;
            temperatureCounter = 0f;
        }*/

        if (incr) {
            cooldownCounter = 0;
            temperature += temperatureCounter*stoveHeat;
        } else {
            if (cooldownCounter > COOL_DOWN_TIMER && useCooldown) {
                temperature = MathUtils.clamp(temperature-COOLDOWN_RATE, 0, maxTemperature);
            }
        }
        temperatureCounter = 0;
    }
    /** Decreases the temperature of the player's chicken by a given amount
     *
     * @param amt - the amount to decrease by
     */
    public void reduceTemp(int amt) {
        temperature -= amt;
    }

    /**
     * Updates the object's game state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt){
        temperatureCounter = MathUtils.clamp(temperatureCounter += dt, 0f, TEMPERATURE_TIMER);
        cooldownCounter += dt;
        //update progress bar
        //tempBar.setValue(temperature);
    }
    public void draw(GameCanvas canvas){
        //draw temperature
        float scale = 1.5f;
        //0.045 bottom, 0.965 top
        canvas.draw(tempBackground, Color.WHITE, 1220f, 250f,  tempBackground.getRegionWidth()/scale, tempBackground.getRegionHeight()/scale);
        canvas.draw(tempForeground, 0.045f+((temperature*0.92f)/maxTemperature), Color.WHITE,
                tempForeground.getRegionWidth() / scale, tempForeground.getRegionHeight() / scale, 1220f, 250f,
                tempForeground.getRegionWidth() / scale, tempForeground.getRegionHeight() / scale);
        //tempBar.draw(canvas, temperature);
        //canvas.drawText("Temp: "+tempBar.getValue(), font, 500,565);
       // canvas.drawText("Temp: "+temperature, font, 575,565);

    }
}
