package code.game.models;

import code.game.interfaces.ChickenInterface;
import code.game.models.obstacle.CapsuleObstacle;
import code.game.models.obstacle.Obstacle;
import code.util.FilmStrip;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
//import edu.cornell.gdiac.physics.*;
import code.game.models.obstacle.*;
import code.util.FilmStrip;
import code.game.views.GameCanvas;

public class Chicken extends GameObject implements ChickenInterface {
    //TODO: Implement the Enemy Chicken and its methods, feel free to add or remove methods as needed
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** The physics shape of this object */
    private CircleShape sensorShape;
    /** The player character that the enemy will follow
     * We would probably want an AI Controller to handle this, but enemy movement is
     * pretty simple for the prototype */
    private Obstacle target;
    /** The maximum enemy speed */
    private final float maxspeed;
    /** The speed that the enemy chases the player */
    private float chaseSpeed;
    /** The amount to slow the character down */
    private final float damping;
    /** The strength of the knockback force the chicken receives after getting slapped*/
    private final float knockback;
    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();
    /** The max health of the chicken nugget */
    private int max_health;
    /** Health of the chicken*/
    // All of these variables will be put into a FSM in AIController eventually
    private float health;
    /** Time until invulnerability after getting hit wears off */
    private final float INVULN_TIME = 1f;
    /** Counter for Invulnerability timer*/
    private float invuln_counter = INVULN_TIME;
    /** Time to move perpendicular to a wall upon collision before returning to normal AI */
    private final float SIDEWAYS_TIME = 0.1f;
    /** Counter for sideways movement timer*/
    private float sideways_counter = SIDEWAYS_TIME;
    /** Time to remain stationary after hitting the player */
    private final float STOP_TIME = 1f;
    /** Counter for stop movement timer*/
    private float stop_counter = STOP_TIME;
    /** True if the chicken has just been hit and the knockback has not yet been applied*/
    private boolean hit = false;

    private Chef player;

    private final int FIRE_MULT = 2;

    private boolean finishA = false;

    private boolean soundCheck = true;

    private float attack_timer = -1f;

    private float attack_charge = 0f;

    private float ATTACK_CHARGE = 0.4f;

    private boolean hitboxOut = false;


    private float ATTACK_DUR = 0.2f;

    private CircleShape attackHit;

    private float ATTACK_RADIUS = 1.5f;

    protected FilmStrip animator;
    /** Reference to texture origin */
    protected Vector2 origin;

    private float slow = 1f;

    private float status_timer = -1.0f;

    private boolean cookin = false;
    /** Texture for chicken healthbar */
    private TextureRegion healthBar;

    private float CHICK_HIT_BOX = 0.8f;

    /** Whether the chicken movement is beign controlled by a force (otherwise a velocity)*/
    private Boolean isBeingForced = false;
    /** Whether the chicken is currently in hitstun */
    private Boolean isStunned = false;
    /** Whether the chicken is invisible due to hitstun*/
    private Boolean isInvisible = false;

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
     * @param player    The target player
     * @param mh        The max health of the chicken
     */
    public Chicken(JsonValue data, float x, float y, float width, float height, Chef player, int mh) {
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
        setSensorName("chickenSensor");
        this.target = player;
        this.player = player;
        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        chaseSpeed = data.getFloat("chasespeed", 0);
        knockback = data.getFloat("knockback", 0);
        max_health = mh;
        health = max_health;
        this.data = data;
        this.setName("chicken");

    }

    /** Returns the json data for this chicken */
    public JsonValue getJsonData(){
        return data;
    }

    /**
     * Sets the current chicken max health
     * @param h - the number to set the max health of the chicken to
     *
     */
    public void setMaxHealth(int h){
        max_health = h;
    }

    /**
     * Returns current chicken max health.
     *
     * @return the current chicken max health.
     */
    public int getMaxHealth(){ return max_health;}

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
        sensorShape = new CircleShape();
        sensorShape.setRadius(CHICK_HIT_BOX);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());


        return true;
    }

    /**
     * Applies the force to the body of this chicken
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }
        /**
         if (hit){
         hit = false;
         }
         else {
         forceCache.set(-damping * getVX(), -damping * getVY());
         }
         */
        if (isBeingForced) {
            if (!hit){
                forceCache.set(-damping * getVX(), -damping * getVY());
            }
            else{
                hit = false;
            }
            body.applyForce(forceCache,getPosition(),true);
        }
        else{
            setLinearVelocity(forceCache);
        }

        /**
         // Velocity too high, clamp it
         if (Math.abs(getVX()) >= maxspeed) {
         setVX(Math.signum(getVX())*maxspeed);
         }

         // Velocity too high, clamp it
         if (Math.abs(getVY()) >= maxspeed) {
         setVY(Math.signum(getVY())*maxspeed);
         }
         */
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {
        super.update(dt);
        applyForce();

        if (!cookin) {
            status_timer = Math.max(status_timer - dt, -1f);
        }
        /**
         invuln_counter   = MathUtils.clamp(invuln_counter+=dt,0f,INVULN_TIME);
         sideways_counter = MathUtils.clamp(sideways_counter+=dt,0f,SIDEWAYS_TIME);
         stop_counter = MathUtils.clamp(stop_counter+=dt,0f,STOP_TIME);
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
         attackHit = new CircleShape();
         attackHit.setRadius(ATTACK_RADIUS);
         attack.shape = attackHit;

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

         } else if (target.isActive()) {
         forceCache.set(target.getPosition().sub(getPosition()));
         forceCache.nor();
         forceCache.scl(chaseSpeed * slow);
         if (isStunned()) {
         forceCache.scl(-knockback);
         applyForce();
         }
         else{
         if (sideways_counter < SIDEWAYS_TIME){
         forceCache.rotate90(0);
         }
         if (stop_counter < STOP_TIME){
         forceCache.setZero();
         }
         setVX(forceCache.x);
         setVY(forceCache.y);

         }

         }
         */
    }


    //TODO: comment
    public boolean getSoundCheck() {
        if (soundCheck) {
            soundCheck = false;
            return true;
        } else
            return false;
    }
    //TODO: comment
    public void startAttack() {
        attack_timer = ATTACK_DUR;
        attack_charge = 0f;
        hitboxOut = true;
    }
    //TODO: comment
    public void stopAttack() {
        finishA = true;
    }
    //TODO: comment
    public boolean isAttacking(){
        return hitboxOut;
    }
    //TODO: comment
    public boolean chasingPlayer(Chef p) { return target.equals(p); }
    //TODO: comment
    public void setChaseSpeed(float spd){
        chaseSpeed = spd;
    }
    //TODO: comment
    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture, 3, 5);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
    }
    //TODO: comment
    public void setBarTexture(TextureRegion texture){
        healthBar = texture;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (!isInvisible) {
            canvas.draw(healthBar, Color.FIREBRICK, 0, origin.y, getX() * drawScale.x-17, getY() * drawScale.y+40, getAngle(), 0.08f, 0.025f);
            canvas.draw(healthBar, Color.GREEN,     0, origin.y, getX() * drawScale.x-17, getY() * drawScale.y+40, getAngle(), 0.08f*(health/max_health), 0.025f);
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
        if (sensorShape != null) {
            canvas.drawPhysics(sensorShape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
            if (hitboxOut && attackHit != null) {
                canvas.drawPhysics(attackHit, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
            }
        }
    }

    /**
     * The chicken takes damage
     *
     * @param damage The amount of damage to this chicken's health
     */
    public void takeDamage(float damage) {
        if (!isStunned) {
            if (status_timer >= 0) {
                health -= damage * FIRE_MULT;
            } else {
                health -= damage;
            }
            finishA = true;
            attack_timer = -1f;
            attack_charge = -1f;
            //invuln_counter = 0;
            hitboxOut = false;
            hit = true;
            isStunned = true;
        }
    }

    /**
     * Applies a slowdown modifier to the chicken's speed
     *
     * @param strength a slowdown multiplier (1f for normal speed)
     */
    public void applySlow(float strength) {
        slow = strength;
    }

    /**
     * Removes any slowdown modifiers to the chicken's speed
     */
    public void removeSlow() {
        slow = 1f;
    }

    /**
     * Applies the fire effect by giving the chicken a countdown timer
     * representing the remaining time of the fire effect
     *
     * @param duration a duration for the fire effect in seconds.
     */
    public void applyFire(float duration) {
        status_timer = duration;
        cookin = true;
    }

    //TODO: comment
    public void letItBurn() {
        cookin = false;
    }

    /**
     * Sets the chicken's target to the specific Lure trap
     *
     * @param t a Lure trap target
     */
    public void trapTarget(Trap t) {
        target = t;
    }

    /**
     * Resets the chicken's target to the player
     *
     */
    public void resetTarget() {
        target = player;
    }

    /**
     * updates the isStunned condition for the chicken
     * updates the isStunned condition for the chicken
     *
     * @param stun  whether the chicken is stunned
     */
    public void setStunned(Boolean stun){
        isStunned = stun;
    }

    /** If the enemy is still alive
     * @return true if chicken health > 0*/
    public boolean isAlive() {return health > 0;}



    /**
     * The chicken has collided with a wall and will move perpendicularly to get around the wall
     */
    public void hitWall(){
        if (!isStunned){
            sideways_counter = 0;
        }
    }

    /**
     * The chicken has collided with the player and will remain stationary for some time
     */
    public void hitPlayer(){
        stop_counter = 0;
    }

    /**
     * Set the value of the forceCache
     *
     * @param newForce     the new value of the forceCache
     * @param isForce       whether the new force is a force (otherwise it is a velocity)
     * */
    public void setForceCache(Vector2 newForce, Boolean isForce){
        forceCache.set(newForce);
        this.isBeingForced = isForce;
    }

    /**
     * Set the isInvisible boolean, which determines whether to draw the chicken on the screen
     *
     * @param invisible whether the chicken should be invisible
     */
    public void setInvisible(Boolean invisible){
        isInvisible = invisible;
    }
    /** accessor for hit
     * @return  hit */
    public Boolean getHit(){
        return hit;
    }

    /**
     * setter for hit
     * @param hit   new value for hit
     */
    public void setHit(Boolean hit){
        this.hit = hit;
    }

}