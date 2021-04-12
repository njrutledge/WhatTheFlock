package code.game.models;

import code.game.views.GameCanvas;
import code.util.FilmStrip;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public class BuffaloChicken extends Chicken {
    //TODO: Implement the Enemy Chicken and its methods, feel free to add or remove methods as needed
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** Radius of chicken's sensor */
    private final float SENSOR_RADIUS = 5f;
    /** Time it takes for the chicken to begin their attack after colliding with their target */
    private final float CHARGE_DUR = 1f;
    /** Time it takes for the chicken to recover from attacking */
    private final float STOP_DUR = 2f;
    /** Whether the chicken is currently running */
    private boolean running = false;

    /**
     * Creates a new chicken avatar with the given physics data
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data  	The physics constants for all chickens
     * @param unique    The unique physics constants for nuggets
     * @param x         The x axis location of this chicken
     * @param y         The y axis location of this chicken
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     * @param player    The target player
     * @param mh        The max health of the chicken
     */
    public BuffaloChicken(JsonValue data, JsonValue unique, float x, float y, float width, float height, Chef player, int mh) {
        // The shrink factors fit the image to a tigher hitbox
        super(data, unique, x, y, width, height, player, mh, ChickenType.Buffalo);
        sensorRadius = SENSOR_RADIUS;
    }

    public void attack(float dt) {
        charge_time += dt;
        if (charge_time >= CHARGE_DUR){
            attack_timer += dt;
            if (!hitboxOut) {
                setAttackType(FixtureType.CHARGE_ATTACK);
                // No sounds for buffalo yet
                //soundCheck = true;
                makeAttack = true;
            }
            hitboxOut = true;
        }
    }

    public boolean doneCharging() {
        if (charge_time >= CHARGE_DUR) {
            return true;
        } return false;
    }

    @Override
    public boolean isRunning() { return running; };

    @Override
    public void setRunning(boolean running) { this.running = running; }

    @Override
    public void interruptAttack() { stopAttack(true); setStopped(true); setRunning(false); }

    public float getStopDur() { return STOP_DUR; }

    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture, 3, 4);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f-1);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        if (!isInvisible) {
            canvas.draw(animator, (status_timer >= 0) ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.15f, 0.15f);
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
    }

}
