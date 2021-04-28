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
    /** Empty temperature bar */
    private TextureRegion tempEmpty;
    /** Yellow temperature bar */
    private TextureRegion tempYellow;
    /** Orange temperature bar */
    private TextureRegion tempOrange;
    /** Red temperature bar */
    private TextureRegion tempRed;
    /** Med temperature bar flame */
    private TextureRegion medFlame;
    /** Large temperature bar flame */
    private TextureRegion lrgFlame;

    //private int WIDTH = 1;
    //private int HEIGHT = 300;

    /** The x position of the temperature bar (if drawing from center) */
    private float cx_temp;
    /** The y position of the temperature bar (if drawing from center) */
    private float cy_temp;
    /** The x position of the temperature bar (if drawing from edge) */
    private float ex_temp;
    /** The x position of the temperature bar (if drawing from edge) */
    private float ey_temp;

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
    public TemperatureBar(TextureRegion empty, TextureRegion yellow, TextureRegion orange,
                          TextureRegion red, TextureRegion medFlame, TextureRegion lrgFlame,
                          int max){
        maxTemperature = max;
        temperature = 0;

        // Setting assets
        tempEmpty = empty;
        tempYellow = yellow;
        tempOrange = orange;
        tempRed = red;
        this.medFlame = medFlame;
        this.lrgFlame = lrgFlame;
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
    public void reduceTemp(float amt) {
        temperature -= Math.max(0,amt);
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
        float scale = 0.75f;
        float fscale = 0.65f;
        float angle = (float)(3*Math.PI)/2;
        //0.04 bottom, 0.94 top

        TextureRegion bar;
        TextureRegion flame;
        if (cx_temp == 0) { cx_temp = 25 + tempEmpty.getRegionHeight()*scale/2; }
        if (cy_temp == 0) { cy_temp = canvas.getHeight() - 90; }
        if (ex_temp == 0) { ex_temp = 268; }
        if (ey_temp == 0) { ey_temp = canvas.getHeight()/2 + 4; }
        // Draw the empty temperature bar
        if (temperature/maxTemperature <= 0.22) {
            bar = tempYellow;
        } else if (temperature/maxTemperature <= 0.6) {
            bar = tempOrange;
            canvas.draw(medFlame, Color.WHITE, medFlame.getRegionWidth()/2, medFlame.getRegionHeight()/2,
                    cx_temp+180,  cy_temp-3, angle, fscale, fscale);
        } else {
            bar = tempRed;
            canvas.draw(lrgFlame, Color.WHITE, lrgFlame.getRegionWidth()/2, lrgFlame.getRegionHeight()/2,
                    cx_temp+200,  cy_temp-6, angle, fscale, fscale);
        }

        canvas.draw(tempEmpty, 1, 270, Color.WHITE, tempEmpty.getRegionWidth()*scale/2,
                tempEmpty.getRegionHeight()/2, ex_temp-1,  ey_temp+1.5f, tempEmpty.getRegionWidth()*scale,
                tempEmpty.getRegionHeight()*scale);
        canvas.draw(bar, 0.04f+((temperature*0.91f)/maxTemperature),
                270, Color.WHITE, bar.getRegionWidth()*scale/2,
                tempYellow.getRegionHeight()/2, ex_temp, ey_temp,
                bar.getRegionWidth()*scale, bar.getRegionHeight()*scale);
    }
}
