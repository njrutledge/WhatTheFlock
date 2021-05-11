package code.game.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import code.game.views.GameCanvas;
import com.badlogic.gdx.math.Vector2;

public class TemperatureBar {
    /**The max temperature the chicken can get to (when cooked) */
    private int maxTemperature;
    /**Current temperature of the chicken */
    private float temperature;
    /**Current temperature of the decay bar */
    private float dtemperature;

    ///**Animated progress bar for temperature*/
    //private ProgressBar tempBar;
    ///** Texture atlas to support a temperature bar */
    //private Texture tempTexture;
    // tempBar is a "texture atlas." Break it up into parts.
    /** Empty temperature bar */
    private TextureRegion tempEmpty;
    /** Yellow temperature bar */
    private TextureRegion tempYellow;
    /** Yellow temperature no bar */
    private TextureRegion tempYellowNB;
    /** Orange temperature bar */
    private TextureRegion tempOrange;
    /** Orange temperature no bar */
    private TextureRegion tempOrangeNB;
    /** Red temperature bar */
    private TextureRegion tempRed;
    /** Red temperature no bar */
    private TextureRegion tempRedNB;
    /** Med temperature bar flame */
    private TextureRegion medFlame;
    /** Large temperature bar flame */
    private TextureRegion lrgFlame;
    /** The display scale */
    private Vector2 displayScale;

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
    /** Time to wait before beginning decay */
    private final float DECAY_TIME = 1f;
    /** Time passed since beginning decay */
    private float decay_timer;
    /**Rate in which decay bar decays */
    private final float DECAY_RATE = 0.1f;


    /**The font*/
    private BitmapFont font = new BitmapFont();

    /**Create a new temperature bar with temperature range 0 thru max*/
    public TemperatureBar(TextureRegion empty, TextureRegion yellow, TextureRegion orange,
                          TextureRegion red, TextureRegion medFlame, TextureRegion lrgFlame,
                          TextureRegion yellowNB, TextureRegion orangeNB,
                          TextureRegion redNB, int max){
        maxTemperature = max;
        temperature = 0;
        dtemperature = temperature;

        decay_timer = 0f;

        // Setting assets
        tempEmpty = empty;
        tempYellow = yellow;
        tempYellowNB = yellowNB;
        tempOrange = orange;
        tempOrangeNB = orangeNB;
        tempRed = red;
        tempRedNB = redNB;
        this.medFlame = medFlame;
        this.lrgFlame = lrgFlame;
        displayScale = new Vector2(1,1);
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
            if (temperature >= dtemperature) { dtemperature = temperature + temperatureCounter*stoveHeat; }
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
        if (temperature >= dtemperature) { dtemperature = temperature; }
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
        if (temperature >= dtemperature) { dtemperature = temperature; decay_timer = 0; }
        else {
            if (decay_timer >= DECAY_TIME) {
                dtemperature -= DECAY_RATE;
            } else {
                decay_timer += dt;
            }
        }
        //update progress bar
        //tempBar.setValue(temperature);
    }

    public void setDisplayScale(Vector2 ds){
        displayScale = ds;
    }

    public void draw(GameCanvas canvas){
        //draw temperature
        float scale = 0.75f;
        float fscale = 0.65f;
        float angle = (float)(3*Math.PI)/2;
        //0.04 bottom, 0.94 top

        TextureRegion bar;
        TextureRegion nb;
        TextureRegion flame;
        cx_temp = 30 + tempEmpty.getRegionHeight()*scale/2;
        //dont scale canvas.getHeight()
        cy_temp = canvas.getHeight() - 70*displayScale.y;
        ex_temp = 200;
        ey_temp = canvas.getHeight() - 266*displayScale.y;
        // Draw the empty temperature bar
        if (temperature/maxTemperature <= 0.22) {
            bar = tempYellow;
            nb = tempYellowNB;
        } else if (temperature/maxTemperature <= 0.6) {
            bar = tempOrange;
            nb = tempOrangeNB;
            canvas.draw(medFlame, Color.WHITE, medFlame.getRegionWidth()/2f, medFlame.getRegionHeight()/2f,
                    (cx_temp+180)*displayScale.x,  cy_temp, angle, fscale*displayScale.x, fscale*displayScale.y);
        } else {
            bar = tempRed;
            nb = tempRedNB;
            canvas.draw(lrgFlame, Color.WHITE, lrgFlame.getRegionWidth()/2f, lrgFlame.getRegionHeight()/2f,
                    (cx_temp+200)*displayScale.x,  cy_temp-3*displayScale.y, angle, fscale*displayScale.x, fscale*displayScale.y);
        }

        // -1 +1.5
        canvas.draw(tempEmpty, 1, 270, Color.WHITE,
                tempEmpty.getRegionWidth()*scale/2*displayScale.x, tempEmpty.getRegionHeight()*scale/2*displayScale.y,
                (ex_temp-1)*displayScale.x,  ey_temp+1.5f*displayScale.y,
                tempEmpty.getRegionWidth()*scale*displayScale.x, tempEmpty.getRegionHeight()*scale*displayScale.y);
        if (temperature < dtemperature) {
            canvas.draw(nb, 0.04f + ((dtemperature * 0.91f) / maxTemperature), 270, Color.GRAY,
                    bar.getRegionWidth() * scale / 2*displayScale.x, tempYellow.getRegionHeight() *scale/ 2*displayScale.y,
                    ex_temp*displayScale.x, ey_temp,//dont scale y
                    bar.getRegionWidth() * scale*displayScale.x, bar.getRegionHeight() * scale*displayScale.y);
        }

        canvas.draw(bar, 0.04f+((temperature*0.91f)/maxTemperature), 270, Color.WHITE,
                bar.getRegionWidth()*scale/2*displayScale.x, tempYellow.getRegionHeight()*scale/2*displayScale.y,
                ex_temp*displayScale.x, ey_temp,
                bar.getRegionWidth()*scale*displayScale.x, bar.getRegionHeight()*scale*displayScale.y);
    }
}
