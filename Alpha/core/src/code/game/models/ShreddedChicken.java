package code.game.models;

import code.util.FilmStrip;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import code.game.views.GameCanvas;

/**
 * The shredded chicken attacks the player by throwing projectile eggs that can damage the player and
 * disable the stove.
 */
public class ShreddedChicken extends Chicken {
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** Radius of sensor */
    private final float SENSOR_RADIUS = 4f;
    /** Time it takes for the chicken to begin their attack after colliding with their target */
    private final float CHARGE_DUR = .5f;
    /** Time it takes for the chicken to recover from attacking */
    private final float STOP_DUR = 2f;
    /** Texture region for egg projectile */
    private TextureRegion eggTexture;
    /** Attack angle save */
    private float attackAngle = 0.0f;

    private JsonValue data;

    /**
     * Creates a new chicken avatar with the given physics data
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data  	The physics constants for all chickens
     * @param unique    The unique physics constants for Shredded
     * @param x         The x axis location of this chicken
     * @param y         The y axis location of this chicken
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     * @param player    The target player
     * @param mh        The max health of the chicken
     */
    public ShreddedChicken(JsonValue data, JsonValue unique, float x, float y, float width, float height, Chef player, int mh) {
        // The shrink factors fit the image to a tigher hitbox
        super(data, unique, x, y, width, height, player, mh, ChickenType.Shredded);
        this.data = data;
        sensorRadius = SENSOR_RADIUS;
    }

    /**
     * if the chicken is ready to attack, then attack by throwing an egg
     * @param dt    time since last attack
     */
    public void attack(float dt) {
        // Charge up before attacking
        if (charge_time==0){
            attackAngle = MathUtils.atan2(getY()-target.getY(),getX()-target.getX());
        }
        charge_time += dt;
        if (charge_time >= CHARGE_DUR){
            // Duration that the attack stays on screen
            attack_timer += dt;
            if (!hitboxOut) {
                setAttackType(ChickenAttack.AttackType.Knockback);
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

    public float getAttackAngle(){
        return attackAngle;
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
     * Set the texture of the shredded chicken
     * @param texture  the object texture for drawing purposes.
     */
    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture, 1, 1);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
    }

    /** Get the egg texture
     *
     * @return the egg texture
     */
    public TextureRegion getProjectileTexture(){return eggTexture;}

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    @Override
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
        // Ground Sensor
        // -------------
        // Previously used to detect double-jumps, but also allows us to see hitboxes
        Vector2 sensorCenter = new Vector2(0, getHeight());
        FixtureDef hitboxDef = new FixtureDef();
        hitboxDef.density = data.getFloat("density",0);
        hitboxDef.isSensor = true;
        hitboxShape = new PolygonShape();
        hitboxShape.setAsBox(getWidth()*1.5f, getHeight()*1.5f, sensorCenter, 0);
        hitboxDef.shape = hitboxShape;
        Fixture hitboxFixture = body.createFixture(hitboxDef);
        hitboxFixture.setUserData(FixtureType.CHICKEN_HURTBOX);

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        sensorShape.setRadius(sensorRadius);
        sensorDef.shape = sensorShape;
        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(FixtureType.CHICKEN_HITBOX);//getSensorName());


        return true;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        float effect = faceRight ? -1.0f : 1.0f;
        float wScale = 0.8f;
        float hScale = 0.7f;
        if (!isInvisible) {
            canvas.draw(animator, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 40, getAngle(), displayScale.x*wScale*effect, displayScale.y*hScale);
        }
        canvas.draw(healthBar, Color.FIREBRICK, 0, origin.y, getX() * drawScale.x-17, getY() * drawScale.y+ 175, getAngle(), 0.08f, 0.025f);
        canvas.draw(healthBar, Color.GREEN,     0, origin.y, getX() * drawScale.x-17, getY() * drawScale.y+ 175, getAngle(), 0.08f*(health/max_health), 0.025f);
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
