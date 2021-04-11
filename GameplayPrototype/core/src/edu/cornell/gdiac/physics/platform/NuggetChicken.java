package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.GameCanvas;

public class NuggetChicken extends ChickenModel {
    //TODO: Implement the Enemy Chicken and its methods, feel free to add or remove methods as needed
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();

    /** The hitbox for the nugget attack */
    private CircleShape hitbox;
    /** The radius of the nugget attack */
    private float ATTACK_RADIUS = 1.5f;

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
     * @param grid      The grid for the level
     */
    public NuggetChicken(JsonValue data, JsonValue unique, float x, float y, float width, float height, ChefModel player, int mh, Grid grid) {
        // The shrink factors fit the image to a tigher hitbox
        super(data, unique, x, y, width, height, player, mh, grid, Type.Nugget);
    }

    public void attack(float dt) {
        if (attack_timer >= 0 && attack_charge >= 0f) {
            body.setLinearVelocity(new Vector2());
            forceCache.setZero();
            attack_charge = MathUtils.clamp(attack_charge + dt,0, ATTACK_CHARGE);
            if (attack_charge == ATTACK_CHARGE){
                attack_timer = MathUtils.clamp(attack_timer - dt, 0, ATTACK_DUR);
                if (!hitboxOut) {
                    FixtureDef attack = new FixtureDef();
                    attack.density = 0.1f;
                    attack.isSensor = true;
                    hitbox = new CircleShape();
                    hitbox.setRadius(ATTACK_RADIUS);
                    attack.shape = hitbox;

                    Fixture chickAttack = body.createFixture(attack);
                    chickAttack.setUserData("nugAttack");

                    hitboxOut = true;
                }
            }
            if (attack_timer == 0f) {
                attack_charge = 0f;
                attack_timer = ATTACK_DUR;
                hitboxOut = false;
                soundCheck = true;
                if (finishA){
                    attack_timer = -1f;
                    attack_charge = -1f;
                    finishA = false;
                }
            }

        }
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        if (!isStunned() || ((int)(invuln_counter * 10)) % 2 == 0) {
            canvas.draw(animator, (status_timer >= 0) ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.25f, 0.25f);
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
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),drawScale.x,drawScale.y);
        if (hitboxOut) {
            canvas.drawPhysics(hitbox,Color.RED,getX(),getY(),drawScale.x,drawScale.y);
        }
    }

}
