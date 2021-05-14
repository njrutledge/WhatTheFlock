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
    private final float SENSOR_RADIUS = 7f;
    /** Time it takes for the chicken to begin their attack after colliding with their target */
    private final float CHARGE_DUR = 1f;
    /** Time it takes for the chicken to recover from attacking */
    private final float STOP_DUR = 2f;
    /** Whether the chicken is currently running */
    private boolean running = false;

    /** How fast we change frames */
    private static final float ANIMATION_SPEED = 0.2f;
    /** The number of animation frames in our filmstrip */
    private static final int NUM_ANIM_FRAMES = 10;

    /** Animator for buffalo charge ramp up*/
    protected FilmStrip charge_start_animator;
    /** Animator for buffalo charging*/
    protected FilmStrip charge_animator;

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
        //faceRight = true;
    }

    public void attack(float dt) {
        charge_time += dt;
        if (charge_time >= CHARGE_DUR){
            attack_timer += dt;
            if (!hitboxOut) {
                setDestination(target.getPosition());
                setAttackType(ChickenAttack.AttackType.Charge);
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
    public void interruptAttack() { setRunning(false); stopAttack(); setStopped(true); doneAttack = true; }

    public float getStopDur() { return STOP_DUR; }

    /**
     * Animates the texture (moves along the filmstrip)
     *
     * @param texture
     */
    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture, 1, NUM_ANIM_FRAMES);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f - 1);
    }

    /**
     * Animates the charge start texture (moves along the filmstrip)
     *
     * @param texture
     */
    public void setChargeStartTexture(Texture texture) {
        charge_start_animator = new FilmStrip(texture, 1, 11);
        origin = new Vector2(charge_start_animator.getRegionWidth()/2.0f, charge_start_animator.getRegionHeight()/2.0f - 1);
    }

    /**
     * Animates the charging texture (moves along the filmstrip)
     *
     * @param texture
     */
    public void setChargingTexture(Texture texture) {
        charge_animator = new FilmStrip(texture, 1, 6);
        origin = new Vector2(charge_animator.getRegionWidth()/2.0f, charge_animator.getRegionHeight()/2.0f - 1);
    }

    public void update(float dt) {
        if (isStunned) {
            animeframe += ANIMATION_SPEED * 4;
            if (animeframe >= 5) {
                animeframe -= 5;
            }
        } else if (!isRunning() && !isAttacking()) {
            animeframe += ANIMATION_SPEED;
            if (animeframe >= NUM_ANIM_FRAMES) {
                animeframe -= NUM_ANIM_FRAMES;
            }
        } else if (isAttacking() && doneCharging()) {
            animeframe += ANIMATION_SPEED;
            if (animeframe >= 6) {
                animeframe -= 6;
            }
        } else if (isAttacking()) {
            animeframe += ANIMATION_SPEED/1.25;
            if (animeframe >= 11) {
                animeframe -= 11;
            }
        }
        if(isLured() && chickenAttack!=null){
            chickenAttack.collideObject();
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
        float effect = faceRight ? 1.0f : -1.0f;
        Color c = Color.WHITE.cpy();
        float wScale = 0.4f;
        float hScale = 0.4f;

        if (isAttacking() && !doneCharging() && getLinearVelocity().len2() == 0) {
            charge_start_animator.setFrame((int) animeframe);
            canvas.draw(charge_start_animator, c, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 20, getAngle(), displayScale.x*wScale* effect, displayScale.y*hScale);
        } else if (running){
            charge_animator.setFrame((int) animeframe);
            canvas.draw(charge_animator, c, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 20, getAngle(), displayScale.x*wScale* effect, displayScale.y*hScale);
        }else if (!isStunned) {
            animator.setFrame(((int)animeframe) % 6);
            canvas.draw(animator, c, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 20, getAngle(), displayScale.x*wScale*effect, displayScale.y*hScale);
        } else if (isStunned){
            hurt_animator.setFrame((int)(animeframe));
            canvas.draw(hurt_animator, c, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 20, getAngle(), displayScale.x*wScale*effect, displayScale.y*hScale);
        }
        drawSlow(canvas, getX() * drawScale.x, getY() * drawScale.y, displayScale.x*wScale*effect*0.5f, displayScale.y*hScale*0.5f);
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
