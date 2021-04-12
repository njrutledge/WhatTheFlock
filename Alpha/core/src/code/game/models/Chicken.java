package code.game.models;

import code.game.interfaces.ChickenInterface;
import code.game.models.obstacle.Obstacle;
import code.util.FilmStrip;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
//import edu.cornell.gdiac.physics.*;
import code.game.views.GameCanvas;

public abstract class Chicken extends GameObject implements ChickenInterface {
    //TODO: Implement the Enemy Chicken and its methods, feel free to add or remove methods as needed
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** The initializing data (to avoid magic numbers) */
    protected JsonValue unique;
    /** The physics shape of this object's sensor */
    protected CircleShape sensorShape;
    /** The physics shape of this object's hitbox */
    protected PolygonShape hitboxShape;

    public boolean faceRight;

    /** The type of chicken */
    public enum ChickenType {
        Nugget,
        DinoNugget,
        Buffalo,
        Shredded
    }

    // Path finding
    /** The player character that the enemy will follow
     * We would probably want an AI Controller to handle this, but enemy movement is
     * pretty simple for the prototype */
    protected Obstacle target;
    /** The destination of this chicken, if  not the target's current position */
    protected Vector2 destination;

    /** The type of this chicken */
    private ChickenType type;
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
    protected float invuln_counter = INVULN_TIME;
    /** Time to move perpendicular to a wall upon collision before returning to normal AI */
    private final float SIDEWAYS_TIME = 0.1f;
    /** Counter for sideways movement timer*/
    private float sideways_counter = SIDEWAYS_TIME;
    /** Whether the chicken was stopped */
    protected boolean stopped;

    /** True if the chicken has just been hit and the knockback has not yet been applied*/
    private boolean hit = false;
    /** The chef that the chicken is targeting */
    private Chef player;



    /** Whether or not the attack needs to be created for this chicken */
    protected boolean makeAttack = false;
    /** Whether the chicken should stop their attack after running */
    private boolean stopThisAttack = false;
    /** Whether or not the chicken's sensor is touching its target (Not up-to-date)
     * Touching might not always be up to date as a chicken that is
     * currently performing an attack may stop touching from being
     * updated to prevent their attack from being interrupted. */
    private boolean touching = false;
    /** Whether or not the chicken's sensor is touching its target (Up-to-date)
     * This variable will be updated in place of touching if the chicken
     * is currently performing an attack. */
    private boolean last_touching = false;

    /** The type of attack that needs to be made */
    protected ChickenAttack.AttackType attackType;


    /** The damage modifier from being on fire*/
    private final int FIRE_MULT = 2;
    //TODO comments
    protected boolean finishA = false;
    protected boolean soundCheck = false;
    protected float attack_timer = -1f;

    /** Time since the chicken began charging up their attack */
    protected float charge_time = -1f;


    protected boolean hitboxOut = false;
    protected float ATTACK_DUR = 0.2f;



    private float ATTACK_RADIUS = 1.5f;

    protected FilmStrip animator;
    /** Reference to texture origin */
    protected Vector2 origin;
    /** slowness modifier for chicken speed */
    private float slow = 1f;
    /** Timer used to keep track of trap effects */
    protected float status_timer = -1.0f;
    /** True iff the chicken is currently on fire from the fire trap */
    private boolean cookin = false;
    /** Texture for chicken healthbar */
    private TextureRegion healthBar;

    /** Radius of the chicken's sensor. If the chicken's target comes into
     * contact with the sensor, the chicken will attempt to initiate
     * an attack on the target. */
    protected float sensorRadius;


    /** Whether the chicken movement is beign controlled by a force (otherwise a velocity)*/
    protected Boolean isBeingForced = false;
    /** Whether the chicken is currently in hitstun */
    protected Boolean isStunned = false;
    /** Whether the chicken is invisible due to hitstun*/
    protected Boolean isInvisible = false;
    /** Whether the chicken is being slowed */
    private boolean inSlow = false;
    /** Ammount to increase or decrease the slow modifier */
    private float SLOW_EFFECT = 0.3f;



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
    public Chicken(JsonValue data, JsonValue unique, float x, float y, float width, float height, Chef player, int mh, ChickenType type) {
        // The shrink factors fit the image to a tigher hitbox
        super(x, y, width * unique.get("shrink").getFloat(0),
                height * unique.get("shrink").getFloat(1), ObjectType.CHICKEN);
        setDensity(unique.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// IT WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);
        setName("chicken");
        this.target = player;
        this.player = player;
        this.type = type;
        maxspeed = unique.getFloat("maxspeed", 0);
        damping = unique.getFloat("damping", 0);
        chaseSpeed = unique.getFloat("chasespeed", 0);
        knockback = unique.getFloat("knockback", 0);
        max_health = (int)(unique.getFloat("maxhealth",0) * (mh/100));
        faceRight = true;

        health = max_health;
        this.data = data;
        this.unique = unique;
        Filter filter = new Filter();
        //0x0002 = chickens
        filter.categoryBits = 0x0002;
        //0x0001 = players
        filter.maskBits = 0x0001 | 0x0002 | 0x0004 ;
        setFilterData(filter);
    }

    /** Returns the json data for this chicken */
    public JsonValue getJsonData(){
        return data;
    }

    /** Returns the unique json data for this chicken */
    public JsonValue getJsonUnique(){
        return unique;
    }

    /** Returns the destination of this chicken */
    public Vector2 getDestination() { return destination; }

    /** Sets the destination of this chicken */
    public void setDestination(Vector2 destination) { this.destination = destination; }

    /** Returns the type of this chicken */
    public ChickenType getType() { return type; }

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
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> shredded
     * Returns whether an attack object needs to be made for this chicken.
     *
     * @return makeAttack
     */
    public boolean makeAttack() {
        if (makeAttack) { makeAttack = false; return true; }
        return false;
    }

    /** Returns whether or not the chicken is still charging their attack */
    public abstract boolean doneCharging();

    /** Sets the type of the attack that needs to be made for this chicken.
     *
     * @param type  The type of the attack to be made
     */
    public void setAttackType(ChickenAttack.AttackType type) { attackType = type; }

    /** Returns the type of the attack that needs to be made for this chicken.
     *
     * @return attackType
     */
    public ChickenAttack.AttackType getAttackType() { return attackType; }


    /**
     * Returns current chicken slowing modifier.
     *
     * @return the current chicken slowing modifier.
     */
    public float getSlow(){ return slow;}

    /**
     * Returns the direction the chicken is facing.
     *
     * @return faceRight - true if facing right
     */
    public boolean isFacing() { return faceRight; }

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
        FixtureDef hitboxDef = new FixtureDef();
        hitboxDef.density = data.getFloat("density",0);
        hitboxDef.isSensor = true;
        hitboxShape = new PolygonShape();
        hitboxShape.setAsBox(getWidth(), getHeight()/2, sensorCenter, 0);
        hitboxDef.shape = hitboxShape;
        Fixture hitboxFixture = body.createFixture(hitboxDef);
        hitboxFixture.setUserData(FixtureType.CHICKEN_HITBOX);

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        sensorShape.setRadius(sensorRadius);
        sensorDef.shape = sensorShape;
        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(FixtureType.CHICKEN_SENSOR);//getSensorName());


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
        if (isBeingForced) {
            if (!hit){
                forceCache.set(-damping * getVX(), -damping * getVY());
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
        if (getLinearVelocity().x > 0){
            faceRight = true;
        } else {
            faceRight = false;
        }
        super.update(dt);
        applyForce();
        if (!cookin) {
            status_timer = Math.max(status_timer - dt, -1f);
        }
        if(inSlow){
            applySlow(SLOW_EFFECT*dt);
        }else {
            removeSlow(SLOW_EFFECT*dt);
        }
    }

    /** Perform an attack on the target. */
    public abstract void attack(float dt);

    /** Returns whether or not the chicken's sensor is currently touching its target */
    public boolean isTouching() { return touching; }

    //TODO: comment
    public boolean getSoundCheck() {
        if (soundCheck) {
            soundCheck = false;
            return true;
        } else
            return false;
    }

    /**
     * Start an attack
     */
    public void startAttack() {
        if (!isRunning()) {
            touching = true;
            hitboxOut = false;
            destination = new Vector2(target.getPosition());
            attack_timer = 0f;
            charge_time = 0f;
        } else { stopThisAttack = false;}
    }

    //TODO: comment
    public void stopAttack(boolean touching) {
        if (!isRunning()) {
            attack_timer = -1f;
            charge_time = -1f;
            hitboxOut = false;
            this.touching = touching;
        } else { stopThisAttack = true; last_touching = touching; }
    }

    /** Returns whether or not the chicken should stop their current attack.
     *
     * This is only applicable to buffalo chickens. When the chef moves out
     * of range, a running buffalo should not stop running to chase the chef.
     * Instead, they should finish their run, and then stop their attack.
     *
     * This function will only return true after the buffalo has finished their
     * current run.
     *
     * @return stopThisAttack
     */
    public boolean stopThisAttack() {
        if (stopThisAttack && !isRunning()) {
            stopThisAttack = false;
            return true;
        }
        return false;
    }

    /** Interrupts the current attack if running */
    public void interruptAttack() { return; }

    /** Whether or not the chicken is charging up an attack */
    public boolean isAttacking() {
        return charge_time >= 0 || isRunning();
    }

    /** Whether or not the chicken is currently running */
    public boolean isRunning() { return false; };

    /** Set running */
    public void setRunning(boolean running) { return; }

    //TODO: comment
    public boolean chasingPlayer(Chef p) { return target.equals(p); }
    //TODO: comment
    public void setChaseSpeed(float spd){
        chaseSpeed = spd;
    }

    /**
     * Sets the object texture for drawing purposes.
     *
     * @param texture  the object texture for drawing purposes.
     */
    public abstract void setTexture(Texture texture);

    /**
     * Set texture for the chicken healthbar
     * @param texture texture for chicken healthbar
     */
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
            canvas.draw(healthBar, Color.FIREBRICK, 0, origin.y, getX() * drawScale.x-17, getY() * drawScale.y+50, getAngle(), 0.08f, 0.025f);
            canvas.draw(healthBar, Color.GREEN,     0, origin.y, getX() * drawScale.x-17, getY() * drawScale.y+50, getAngle(), 0.08f*(health/max_health), 0.025f);
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
        canvas.drawPhysics(hitboxShape,Color.RED, getX(),getY(), 0, drawScale.x, drawScale.y);
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
            attack_timer = -1f;
            charge_time = -1f;
            invuln_counter = 0;
            hitboxOut = false;
            hit = true;
            isStunned = true;
        }
    }

    /**
     * Applies a slowdown modifier to the chicken's speed
     * slowing effect at 1f means normal speed, 0f means stopped
     * @param strength amount to decrease the slow multiplier, > 0
     */
    public void applySlow(float strength) {
        slow = Math.max(0, slow - strength);
    }

    /**
     * Removes a slowdown modifier to the chicken's speed
     * slowing effect at 1f means normal speed, 0f means stopped
     * @param strength amount to increase the slow multiplier, > 0
     */
    public void removeSlow(float strength) {
        slow = Math.min(1, slow + strength);
    }

    /**
     * Sets whether the chicken is currently in a slow trap or not
     * @param bool is true if the chicken is being slowed
     */
    public void inSlow(boolean bool) { inSlow = bool;}

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
     * The chicken has collided with the player and will remain stationary for some time
     */
    public void setStopped(boolean stopped){ this.stopped = stopped; }

    /** Whether or not the chicken is stationary due to hitting a player */
    public boolean isStopped() {
        if (stopped) {
            stopped = false;
            return true;
        }
        return false;
    }

    /** Returns the duration of a stop */
    public abstract float getStopDur();

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

    /**
     * Accessor for hit
     * @return  the value of hit
     */
    public Boolean getHit(){
        return hit;
    }
}