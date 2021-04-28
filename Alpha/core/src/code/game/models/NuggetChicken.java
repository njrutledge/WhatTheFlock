package code.game.models;

import code.util.FilmStrip;
import com.badlogic.gdx.graphics.Color;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;

import com.badlogic.gdx.utils.JsonValue;
import code.game.views.GameCanvas;

public class NuggetChicken extends Chicken {
    //TODO: Implement the Enemy Chicken and its methods, feel free to add or remove methods as needed
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** Radius of sensor */
    private float sensor_radius;
    /** Time it takes for the chicken to begin their attack after colliding with their target */
    private final float CHARGE_DUR = 0.4f;
    /** Time it takes for the chicken to recover from attacking */
    private final float STOP_DUR = 1f;

    /** How fast we change frames (one frame per 4 calls to update */
    protected float animation_speed;
    /** The number of animation frames in our filmstrip */
    protected int num_anim_frames;

    /** Nugget scale differences*/
    private float hScale;
    private float wScale;

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
    public NuggetChicken(JsonValue data, JsonValue unique, float x, float y, float width, float height, Chef player, int mh) {
        // The shrink factors fit the image to a tigher hitbox
        super(data, unique, x, y, width, height, player, mh, ChickenType.Nugget);
        hScale = unique.getFloat("hScale", 1);
        wScale = unique.getFloat("wScale", 1);
        animation_speed = unique.getFloat("animation_speed", 0.25f);
        num_anim_frames = unique.getInt("num_anim_frames", 8);
        sensor_radius = unique.getFloat("sensor_radius", 0.8f);
        animeframe = 0.0f;
        sensorRadius = sensor_radius;
    }

    public void attack(float dt) {
        // Charge up before attacking
        charge_time += dt;
        if (charge_time >= CHARGE_DUR){
            // Duration that the attack stays on screen
            attack_timer += dt;
            if (!hitboxOut) {
                destination = new Vector2(target.getPosition());
                setAttackType(ChickenAttack.AttackType.Basic);
                soundCheck = true;
                makeAttack = true;
            }
            hitboxOut = true;
            if (attack_timer >= ATTACK_DUR) {
                // Recharge attack
                charge_time = 0f;
                attack_timer = 0f;
                doneAttack = true;
                hitboxOut = false;
            }
        }

    }

    public boolean doneCharging() { return charge_time >= CHARGE_DUR; };

    public float getStopDur() { return STOP_DUR; }

    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture, 1, 8);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
    }

    /**
     * Updates the object's game state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns, and control animations
     *
     * @param dt	Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {

        if (isStunned) {
            animeframe += animation_speed*4;
            if (animeframe >= 5) {
                animeframe -= 5;
            }
        } else if(getLinearVelocity().x != 0 || getLinearVelocity().y != 0) {
            animeframe += animation_speed;
            if (animeframe >= num_anim_frames) {
                animeframe -= num_anim_frames;
            }
        } else if (isAttacking && attack_animator != null){
            animeframe += animation_speed;
            if (animeframe >= 9) {
                animeframe -= 9;
            }
        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        float effect = faceRight ? 1.0f:-1.0f;
        if (isAttacking && attack_animator != null) {
            attack_animator.setFrame((int) animeframe);
            canvas.draw(attack_animator, (status_timer >= 0) ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.1f*effect*wScale, 0.1f*hScale);
        } else if (!isStunned){
            animator.setFrame((int) animeframe);
            canvas.draw(animator, (status_timer >= 0) ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.1f*effect*wScale, 0.1f*hScale);
        } else if (isStunned){
            hurt_animator.setFrame((int)(animeframe));
            canvas.draw(hurt_animator, (status_timer >= 0) ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.1f*effect*wScale, 0.1f*hScale);
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
