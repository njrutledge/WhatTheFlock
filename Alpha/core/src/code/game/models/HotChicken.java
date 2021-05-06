package code.game.models;

import code.game.views.GameCanvas;
import code.util.FilmStrip;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

/**
 * The hot chicken attacks the player by throwing projectile eggs that can damage the player and
 * disable the stove.
 */
public class HotChicken extends Chicken {
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** Radius of sensor */
    private final float SENSOR_RADIUS = 8f;
    /** Time it takes for the chicken to begin their attack after colliding with their target */
    private final float CHARGE_DUR = 1f;
    /** Time it takes for the chicken to recover from attacking */
    private final float STOP_DUR = 2f;
    /** Animation speed */
    private final float animation_speed = 0.15f;
    /** number of animation frames */
    private final int num_anime_frames = 12;
    /** width draw scaling */
    private float wscale;
    /** height draw scaling */
    private float hscale;


    /**
     * Creates a new chicken avatar with the given physics data
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data  	The physics constants for all chickens
     * @param unique    The unique physics constants for hot chick
     * @param x         The x axis location of this chicken
     * @param y         The y axis location of this chicken
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     * @param player    The target player
     * @param mh        The max health of the chicken
     */
    public HotChicken(JsonValue data, JsonValue unique, float x, float y, float width, float height, Chef player, int mh) {
        // The shrink factors fit the image to a tigher hitbox
        super(data, unique, x, y, width, height, player, mh, ChickenType.Hot);
        wscale = unique.getFloat("wscale", 1);
        hscale = unique.getFloat("hscale", 1);
        sensorRadius = SENSOR_RADIUS;
    }

    /**
     * if the chicken is ready to attack, then attack by throwing an egg
     * @param dt    time since last attack
     */
    public void attack(float dt) {
        // Charge up before attacking
        charge_time += dt;
        if (charge_time >= CHARGE_DUR){
            // Duration that the attack stays on screen
            attack_timer += dt;
            if (!hitboxOut) {
                setAttackType(ChickenAttack.AttackType.Projectile);
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

    /**
     * whether the chicken is done charging an attacking
     */
    public boolean doneCharging() { return charge_time >= CHARGE_DUR; }

    /**
     * Get the stop duration
     * @return  stop duration
     */
    public float getStopDur() { return STOP_DUR; }

    /**
     * Set the texture of the hot chicken
     * @param texture  the object texture for drawing purposes.
     */
    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture, 1, 12);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
    }

    /**
     * Set the attack texture of the hot chicken
     * @param texture the appropriate spreadsheet
     */
    public void setAttackTexture(Texture texture) {
        attack_animator = new FilmStrip(texture, 1, 17);
        origin = new Vector2(attack_animator.getRegionWidth()/2.0f, attack_animator.getRegionHeight()/2.0f);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        float effect = faceRight ? -1.0f : 1.0f;
        Color c = getColor();
        if (isAttacking && attack_animator != null && !isLured()) {
            attack_animator.setFrame((int) animeframe);
            canvas.draw(attack_animator,c, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), wscale*effect, hscale);
        } else if (!isStunned) {
            animator.setFrame((int) animeframe);
            canvas.draw(animator, c, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), wscale*effect, hscale);
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

    /**
     * Updates the object's game state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns, and control animations
     *
     * @param dt	Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {
        super.update(dt);
        if (isStunned) {
            animeframe += animation_speed*4;
            if (animeframe >= 5) {
                animeframe -= 5;
            }
        } else if(getLinearVelocity().x != 0 || getLinearVelocity().y != 0) {
            animeframe += animation_speed;
            if (animeframe >= num_anime_frames) {
                animeframe -= num_anime_frames;
            }
        } else if (isAttacking && attack_animator != null && !isLured()){
            animeframe += animation_speed;
            if (animeframe >= 17) {
                animeframe -= 17;
            }
        }

    }

}
