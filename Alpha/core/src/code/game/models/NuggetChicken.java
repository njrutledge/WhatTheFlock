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
    private final float SENSOR_RADIUS = 0.8f;
    /** Time it takes for the chicken to begin their attack after colliding with their target */
    private final float CHARGE_DUR = 0.4f;
    /** Time it takes for the chicken to recover from attacking */
    private final float STOP_DUR = 2f;

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
        sensorRadius = SENSOR_RADIUS;
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
        animator = new FilmStrip(texture, 3, 5);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        float effect = faceRight ? -1.0f:1.0f;
        if (!isInvisible) {
            canvas.draw(animator, (status_timer >= 0) ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.2f*effect, 0.2f);
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
