package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.util.FilmStrip;

public class ChickenModel extends CapsuleObstacle {
    //TODO: Implement the Enemy Chicken and its methods, feel free to add or remove methods as needed
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** The physics shape of this object */
    private PolygonShape sensorShape;
    /** Identifier to allow us to track the sensor in ContactListener */
    private String sensorName;
    /** The player character that the enemy will follow
     * We would probably want an AI Controller to handle this, but enemy movement is
     * pretty simple for the prototype */
    private ChefModel player;
    /** The maximum enemy speed */
    private final float maxspeed;
    /** The speed that the enemy chases the player */
    private final float chaseSpeed;
    /** The amount to slow the character down */
    private final float damping;
    /** The strength of the knockback force the chicken receives after getting slapped*/
    private final float knockback;
    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();
    private static int INITIAL_HEALTH = 2;
    /** Health of the chicken*/
    private float health;
    /** Time until invulnerability after getting hit wears off */
    private final float INVULN_TIME = 1f;
    /** Counter for Invulnerability timer*/
    private float invuln_counter = INVULN_TIME;
    /** True if the chicken has just been hit and the knockback has not yet been applied*/
    private boolean hit = false;

    protected FilmStrip animator;
    /** Reference to texture origin */
    protected Vector2 origin;

    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return sensorName;
    }

    /**
     * Creates a new chicken avatar with the given physics data
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data  	The physics constants for this dude
     * @param x         The x axis location of this chicken
     * @param y         The y axis location of this chicken
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public ChickenModel(JsonValue data, float x, float y, float width, float height, ChefModel player) {
        // The shrink factors fit the image to a tigher hitbox
        super(/*data.get("pos").getFloat(0),
                data.get("pos").getFloat(1),*/
                x, y,
                width * data.get("shrink").getFloat(0),
                height * data.get("shrink").getFloat(1));
        setDensity(data.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// IT WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);
        setName("chicken");

        this.player = player;
        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        chaseSpeed = data.getFloat("chasespeed", 0);
        knockback = data.getFloat("knockback", 0);
        health = INITIAL_HEALTH;
        this.data = data;

    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
     *
     * @param world Box2D world to store body
     *
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
        // Ground Sensor
        // -------------
        // Previously used to detect double-jumps, but also allows us to see hitboxes
        Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = data.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
                sensorjv.getFloat("height",0), sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());
        return true;
    }

    /**
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        if (hit){
            hit = false;
        }
        else {
            forceCache.set(-damping * getVX(), -damping * getVY());
        }
        body.applyForce(forceCache,getPosition(),true);

        // Velocity too high, clamp it
        if (Math.abs(getVX()) >= maxspeed) {
            setVX(Math.signum(getVX())*maxspeed);
        }

        // Velocity too high, clamp it
        if (Math.abs(getVY()) >= maxspeed) {
            setVY(Math.signum(getVY())*maxspeed);
        }

    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt) {
        super.update(dt);
        invuln_counter = MathUtils.clamp(invuln_counter+=dt,0f,INVULN_TIME);
        if (player.isActive()) {
            forceCache.set(player.getPosition().sub(getPosition()));
            forceCache.nor();
            forceCache.scl(chaseSpeed);
            if (isStunned()) {
                forceCache.scl(-knockback);
                applyForce();
            }
            else{
                setVX(forceCache.x);
                setVY(forceCache.y);
            }
        }
    }

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
        if (!isStunned() || ((int)(invuln_counter * 10)) % 2 == 0) {
            canvas.draw(animator, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), 0.1f, 0.1f);
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
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }

    /**
     * The chicken takes damage
     *
     * @param damage The amount of damage to this chicken's health
     *
     * @return true if the chicken has no health after taking damage
     */
    public Boolean takeDamage(float damage) {
        if (!isStunned()) {
            health -= damage;
            invuln_counter = 0;
            hit = true;
            return health <= 0;
        }

        return false;
    }

    /** If the enemy is still alive
     * @return true if chicken health > 0*/
    public boolean isAlive() {return health <= 0;}

    public Boolean isStunned(){
        return invuln_counter < INVULN_TIME;
    }
}
