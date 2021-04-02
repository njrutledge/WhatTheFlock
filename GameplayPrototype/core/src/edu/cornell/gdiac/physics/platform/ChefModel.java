/*
 * DudeModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.util.FilmStrip;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class ChefModel extends CapsuleObstacle {
	///TODO: Any gameplay or design adjustments to the player will be altered here.
	//////////// Nothing explicit is needed now but may be when altering other files. /////
	//////////////////////////////////////////////////////////////////////////////////////
	/** the texture of the chef */
	private TextureRegion chefTexture;
	/** Health textures*/
	private TextureRegion healthTexture;
	private TextureRegion noHealthTexture;

	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;

	/** The factor to multiply by the input */
	private final float force;
	/** The amount to slow the character down */
	private final float damping;
	/** The maximum character speed */
	//TODO: make final after technical
	private float maxspeed;

	/** Identifier to allow us to track the sensor in ContactListener */
	private final String sensorName;
	/** Cooldown (in animation frames) for shooting */
	private final int shotLimit;
	/** Cooldown (in animation frames) for trapping */
	private final int trapLimit;

	/** The current horizontal movement of the character */
	private float movement;
	private float vertmovement;

	/** Which direction is the character facing */
	private boolean faceRight;
	/** How long until we can shoot again */
	private int shootCooldown;
	/** Whether we are actively shooting */
	private boolean isShooting;
	/** How long until we can place another trap */
	private int trapCooldown;
	/** Is the player trying to set a trap*/
	private boolean isTrap = false;
	/** The physics shape of this object */
	private PolygonShape sensorShape;
	/**Whether or not the player can place a trap */
	private boolean canPlaceTrap;

	/**The maximum health a player can have */
	private int max_health;



	/**The current health of the player, >= 0*/
	private int health;
	/** The font used to draw text on the screen*/
	private static final BitmapFont font = new BitmapFont();
	/** X offset for health display */
	private final float X_HEALTH = 935;
	/** Y offset for health display */
	private final float Y_HEALTH = 450;
	/** size of each heart */
	private final int HEART_SIZE = 30;
	/** Time until invulnerability after getting hit wears off */
	//TODO: make final after technical
	private float INVULN_TIME = 1;
	//TODO: make final after technical
	private float BASE_DAMAGE = 2;

	/** Counter for Invulnerability timer*/
	private float invuln_counter = INVULN_TIME;
	/** Cache for internal force calculations */
	private final Vector2 forceCache = new Vector2();

	/** CURRENT image for this object. May change over time. */
	protected FilmStrip animator;
	/** Reference to texture origin */
	protected Vector2 origin;

	/**
	 * Returns left/right movement of this character.
	 *
	 * This is the result of input times dude force.
	 *
	 * @return left/right movement of this character.
	 */
	public float getMovement() {
		return movement;
	}

	public float getVertMovement() { return vertmovement; }

	/**
	 * Enables or disables placement of a trap.
	 *
	 * @param val is true if a trap can be placed.
	 */
	public void setCanPlaceTrap(boolean val){canPlaceTrap = val;}

	/**
	 * Sets left/right movement of this character.
	 *
	 * This is the result of input times dude force.
	 *
	 * @param value left/right movement of this character.
	 */
	public void setMovement(float value) {
		movement = value;
		// Change facing if appropriate
		if (movement < 0) {
			faceRight = false;
		} else if (movement > 0) {
			faceRight = true;
		}
	}

	/**Set the vertical movement of the character*/
	public void setVertMovement(float value) {
		vertmovement = value;
		//TODO change if facing up/down
	}

	/**
	 * Returns true if the dude is actively firing.
	 *
	 * @return true if the dude is actively firing.
	 */
	public boolean isShooting() {
		return isShooting && shootCooldown <= 0;
	}

	/**
	 * Returns true if the dud is trying to place a trap.
	 *
	 * @return true if the dud is trying to place a trap.
	 */
	public boolean isTrapping() {
		return isTrap && trapCooldown <= 0 && canPlaceTrap;
	}

	/**
	 * Sets whether the dude is actively firing.
	 *
	 * @param value whether the dude is actively firing.
	 */
	public void setShooting(boolean value) {
		isShooting = value;
	}

	/**
	 * Sets whether or not the chef is trying to place a trap
	 *
	 * @param bln whether or not the chef is trying to place a trap
	 */
	public void setTrap(boolean bln) { isTrap = bln; }

	/** */
	public void setTexture(Texture texture) {
		animator = new FilmStrip(texture, 2, 5);
		origin = new Vector2(animator.getRegionWidth()/2.0f + 10, animator.getRegionHeight()/2.0f + 10);
	}

	/**
	 * Returns if the character is alive.
	 *
	 * @return	 if the character is alive
	 */
	public boolean isAlive(){ return health > 0; }

	/** Reduces the dude's health by one. */
	public void decrementHealth() {
		if (!isStunned()) {
			health --;
			invuln_counter = 0f;
		}
	}

	/**Returns true if the character has recently taken damage and is not invulnerable
	 *
	 * @return true if the character is stunned, false otherwise
	 */
	public boolean isStunned(){
		return invuln_counter < INVULN_TIME;
	}

	/**
	 * Sets the current character max health
	 * @param h - the number to set the max health of the player to
	 *
	 */
	public void setMaxHealth(int h){
		max_health = h;
	}
	public void setMaxSpeed(float spd){
		maxspeed = spd;
	}

	/**
	 * Returns current character max health.
	 *
	 * @return the current character max health.
	 */
	public int getMaxHealth(){ return max_health;}

	/**
	 * Sets the current character health
	 * @param h - the number to set the health of the player to
	 *
	 */
	public void setHealth(int h){
		health = h;
	}

	/**
	 * Returns current character health.
	 *
	 * @return the current character health.
	 */
	public int getHealth(){ return health;}

	/**
	 * Returns how much force to apply to get the dude moving
	 *
	 * Multiply this by the input to get the movement value.
	 *
	 * @return how much force to apply to get the dude moving
	 */
	public float getForce() {
		return force;
	}

	/**
	 * Returns ow hard the brakes are applied to get a dude to stop moving
	 *
	 * @return ow hard the brakes are applied to get a dude to stop moving
	 */
	public float getDamping() {
		return damping;
	}

	/**
	 * Returns the upper limit on dude left-right movement.
	 *
	 * This does NOT apply to vertical movement.
	 *
	 * @return the upper limit on dude left-right movement.
	 */
	public float getMaxSpeed() {
		return maxspeed;
	}

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
	 * Returns the base damage value for the chef
	 *
	 * @return the base damage value for the chef
	 */
	public float getDamage(){ return BASE_DAMAGE;}

	/**
	 * Returns true if this character is facing right
	 *
	 * @return true if this character is facing right
	 */
	public boolean isFacingRight() {
		return faceRight;
	}

	/**Gather art assets for health display*/
	private void gatherHealthAssets(){
		AssetDirectory internal = new AssetDirectory("health.json");
		internal.loadAssets();
		internal.finishLoading();
		healthTexture = internal.getEntry("progress.on", TextureRegion.class);
		noHealthTexture = internal.getEntry("progress.off", TextureRegion.class);
	}
	/**
	 * Creates a new dude avatar with the given physics data
	 *
	 * The size is expressed in physics units NOT pixels.  In order for 
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param data  	The physics constants for this dude
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 * @param maxHealth The maximum health of the chef
	 */
	public ChefModel(JsonValue data, float width, float height, int maxHealth) {
		// The shrink factors fit the image to a tigher hitbox
		super(	data.get("pos").getFloat(0),
				data.get("pos").getFloat(1),
				width*data.get("shrink").getFloat( 0 ),
				height*data.get("shrink").getFloat( 1 ));
        setDensity(data.getFloat("density", 0));
		setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
		setFixedRotation(true);

		maxspeed = data.getFloat("maxspeed", 0);
		damping = data.getFloat("damping", 0);
		force = data.getFloat("force", 0);
		shotLimit = data.getInt( "shot_cool", 0 );
		trapLimit = 120;
		sensorName = "DudeGroundSensor";
		this.data = data;
		// Gameplay attributes
		isShooting = false;
		faceRight = true;
		max_health = maxHealth;
		health = max_health;
		gatherHealthAssets();
		shootCooldown = 0;
		trapCooldown = 0;
		setName("dude");
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
		sensorDef.filter.groupIndex = -1;
		sensorDef.filter.categoryBits =  0x0002;
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
		
		// Don't want to be moving. Damp out player motion
		if (getMovement() == 0f) {
			forceCache.set(-getDamping()*getVX(),0);
			body.applyForce(forceCache,getPosition(),true);
		}
		
		// Velocity too high, clamp it
		if (Math.abs(getVX()) >= getMaxSpeed()) {
			setVX(Math.signum(getVX())*getMaxSpeed());
		} else {
			forceCache.set(getMovement(),0);
			body.applyForce(forceCache,getPosition(),true);
		}

		if (getVertMovement() == 0f) {
			forceCache.set(0,-getDamping()*getVY());
			body.applyForce(forceCache,getPosition(),true);
		}

		// Velocity too high, clamp it
		if (Math.abs(getVY()) >= getMaxSpeed()) {
			setVY(Math.signum(getVY())*getMaxSpeed());
		} else {
			forceCache.set(0,getVertMovement());
			body.applyForce(forceCache,getPosition(),true);
		}

	}
	
	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method to reset cooldowns.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void update(float dt, int[] plist) {
		invuln_counter = MathUtils.clamp(invuln_counter+=dt,0f,INVULN_TIME);
		if (isShooting()) {
			shootCooldown = shotLimit;
		} else {
			shootCooldown = Math.max(0, shootCooldown - 1);
		}
		if (isTrapping()) {
			trapCooldown = trapLimit;
		} else {
			trapCooldown = Math.max(0, trapCooldown - 1);
		}
		super.update(dt, plist);

		//TODO: remove after technical
		setMaxHealth(plist[0]);
		BASE_DAMAGE = plist[2];
		setMaxSpeed(plist[9]);
		float next_max = (float) plist[11] / 5;
		if(next_max != INVULN_TIME){
			invuln_counter = next_max;
		}
		INVULN_TIME = next_max;

	}
	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		float effect = faceRight ? 1.0f : -1.0f;
		if (!isStunned() || ((int)(invuln_counter * 10)) % 2 == 0) {
			canvas.draw(animator, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 20, getAngle(), effect / 10, 0.1f);
		}

		//canvas.draw(animator,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y+20,getAngle(),effect/10,0.1f);
		//canvas.drawText("Health: " + health, font, XOFFSET, YOFFSET);
		//draw health
		float x = X_HEALTH;
		float y = Y_HEALTH;
		for (int i = 1; i <= max_health; i++){
			if(i <= health){
				canvas.draw(healthTexture, Color.WHITE, x, y, HEART_SIZE, HEART_SIZE);
			}
			else {
				canvas.draw(noHealthTexture, Color.WHITE, x, y, HEART_SIZE, HEART_SIZE);
			}
			y -= HEART_SIZE + HEART_SIZE/3;
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
}