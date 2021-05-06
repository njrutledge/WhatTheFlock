
package code.game.models;

import code.game.interfaces.ChefInterface;
import code.util.FilmStrip;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import code.game.views.GameCanvas;

/**
 * Player avatar for the plaform game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class Chef extends GameObject implements ChefInterface {
	///TODO: Any gameplay or design adjustments to the player will be altered here.
	//////////// Nothing explicit is needed now but may be when altering other files. /////
	//////////////////////////////////////////////////////////////////////////////////////
	/** the texture of the chef */
	//private TextureRegion chefTexture;
    /** Textures of the chef's health */
	private TextureRegion heartTexture;
	private TextureRegion halfHeartTexture;
	/** Texture of the chef's attack buff */
	private Texture attOffTexture;
	private TextureRegion attOnTexture;

	private Texture slapTexture;
	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;

	/** The factor to multiply by the input */
	private final float force;
	/** The amount to slow the character down */
	private final float damping;
	/** The maximum character speed */
	//TODO: make final after technical
	private float maxspeed;

	/** Cooldown (in animation frames) for shooting */
	private final int shotLimit;
	/** Cooldown (in animation frames) for trapping */
	private final int trapLimit;

	/** The current horizontal movement of the character */
	private float movement;
	private float vertmovement;

	/** Which direction is the character facing */
	private boolean faceRight;
	/** which direction the character is slapping */
	private int slapFace;
	/** How long until we can shoot again */
	private int shootCooldown;
	//TODO: change to slapping?
	/** Whether we are actively shooting */
	private boolean isShooting;
	/** How long until we can place another trap */
	private int trapCooldown;
	/** Is the player trying to set a trap*/
	private boolean isTrap;
	/** Can the player cook */
	private boolean isCooking;
	/** whether the chef is close enough to the stove to cook */
	private boolean inCookingRange = false;
	/** Stove the chef is or was cooking from */
	private Stove stove;
	/** Whether the player is invincible */
	private boolean invincible;

	/** The game shape of this object */
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
	private float X_HEALTH = 30;
	/** Y offset for health display */
	private int y_health;
	/** size of each heart */
	private final int HEART_SIZE = 45;
	/** The number of hearts, including both full and half hearts.
	 * Used for drawing heart textures. */
	private float full_hearts;
	/** Time until invulnerability after getting hit wears off */
	private final float INVULN_TIME = 0.5f;
	/** Chef base damage */
	private final float BASE_DAMAGE = 2;
	/** Flag for double damage */
	private boolean doubleDamage = false;
	/** Timer for double damage */
	private float damageTimer = 0.0f;
	/**max time for double damage */
	private final float DAMAGE_TIME = 7.5f;
	/** How fast we change frames (one frame per 4 calls to update */
	private static final float ANIMATION_SPEED = 0.25f;
	/** The number of animation frames in our filmstrip */
	private static final int NUM_ANIM_FRAMES = 8;
	/** True if the chef has been pushed */
	private boolean isPushed = false;
	/** saved speed for when the chef is pushed */
	private float savedSpeed = 0.0f;
	/** saved angle for when the chef is pushed */
	private float savedAngle = 0.0f;
	/** Countdown for how long to be pushed */
	private int pushedCountdown = 0;

	/** Current animation frame for this shell */
	private float animeframe;
	/** Counter for Invulnerability timer*/
	private float invuln_counter = INVULN_TIME;
	/** Cache for internal force calculations */
	private final Vector2 forceCache = new Vector2();

	/** CURRENT image for this object. May change over time. */
	protected FilmStrip animator;

	/** placeholder filmstrips until figure out animator business */
	protected FilmStrip slap_up_animator;
	protected FilmStrip slap_down_animator;
	protected FilmStrip slap_side_animator;
	protected FilmStrip hurt_animator;
	protected FilmStrip idle_animator;
	/** Reference to texture origin */
	protected Vector2 origin;


	/**
	 * Creates a new chef avatar with the given game data
	 *
	 * The size is expressed in game units NOT pixels.  In order for
	 * drawing to work properly, you MUST set the drawScale. The drawScale
	 * converts the game units to pixels.
	 *
	 * @param data  	The game constants for this chef
	 * @param x			The object x position in game units
	 * @param y			The object y posiiton in game units
	 * @param width		The object width in game units
	 * @param height	The object width in game units
	 */
	public Chef(JsonValue data, float x, float y, float width, float height) {
		// The shrink factors fit the image to a tigher hitbox
		super(	x,
				y,
				width*data.get("shrink").getFloat( 0 ),
				height*data.get("shrink").getFloat( 1 ), ObjectType.CHEF);
		setDensity(data.getFloat("density", 0));
		setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
		setFixedRotation(true);

		maxspeed = data.getFloat("maxspeed", 0);
		damping = data.getFloat("damping", 0);
		force = data.getFloat("force", 0);
		shotLimit = data.getInt( "shot_cool", 0 );
		trapLimit = 120;
		//setSensorName("chefSensor");
		this.data = data;
		// Gameplay attributes
		isShooting = false;
		faceRight = true;
		max_health = data.getInt("maxhealth",0);
		health = max_health;
		full_hearts = (float)Math.ceil(max_health/2.0f);
		//gatherHealthAssets();
		shootCooldown = 0;
		trapCooldown = 0;
		setName("chef");
		isTrap = false;
		isCooking = false;
		animeframe = 0.0f;
		Filter filter = new Filter();
		//0x0001 = player
		filter.categoryBits = 0x0001;
		//0x0002 = chickens, 0x0004 walls, 0x0016 buffalo's headbutt
		filter.maskBits = 0x0002 | 0x0004 | 0x0016;
		setFilterData(filter);
	}

	/**Sets the chef's cooking status
	 * @param b the boolean, whether cooking is true or false*/
	public void setCooking(boolean b, Stove s){
		if (s != null) {
			stove = s;
		}
		isCooking = b;
		if (b){
			inCookingRange = true;
		}
	}

	public float getMaxspeed(){
		return maxspeed;
	}

	/**Returns whether the chef is cooking.
	 * @return the cooking status of the chef. */
	public boolean isCooking() {
		return (isCooking && stove != null && stove.isActive());
	}

	public Stove getStove(){
		return stove;
	}

	/**
	 * Returns whether the chef is in cooking range of a stove
	 * @return true iff chef is in range of a stove
	 */
	public boolean inCookingRange(){
		return inCookingRange;
	}
	/**
	 * Sets whether the chef is in cooking range of a stove
	 * @param inRange	whether the chef is in cooking range
	 */
	public void setInCookingRange(boolean inRange){
		inCookingRange = inRange;
	}

	/**
	 * Returns left/right movement of this character. 0 if the chef is cooking
	 *
	 * This is the result of input times chef force.
	 *
	 * @return left/right movement of this character.
	 */
	public float getMovement() {
		return movement;
	}

	/**
	 * Returns up/down movement of this character. 0 if the chef is cooking
	 *
	 * This is the result of input times chef force.
	 *
	 * @return up/down movement of this character.
	 */
	public float getVertMovement() { return vertmovement; }

	/**
	 * Sets left/right movement of this character.
	 *
	 * This is the result of input times chef force.
	 *
	 * @param value left/right movement of this character.
	 */
	public void setMovement(float value) {
		if (shootCooldown > 0){
			movement = 0;
		} else {
			movement = value;
		}
		// Change facing if appropriate
		if (movement < 0) {
			faceRight = false;
		} else if (movement > 0) {
			faceRight = true;
		}
	}

	/**Set the vertical movement of the character*/
	public void setVertMovement(float value) {
		if (shootCooldown > 0) {
			vertmovement = 0;
		} else {
			vertmovement = value;
		}
		//TODO change if facing up/down
	}

	/**
	 * Enables or disables trap placement
	 *
	 * @param value is true if the player can place a trap.
	 */
	public void setCanPlaceTrap(boolean value) {canPlaceTrap = value;}

	/**
	 * Returns true if the chef is actively firing.
	 *
	 * @return true if the chef is actively firing.
	 */
	public boolean isShooting() {
		return isShooting && shootCooldown <= 0;
	}

	/**
	 * Returns true if the chef is trying to place a trap.
	 *
	 * @return true if the chef is trying to place a trap.
	 */
	public boolean isTrapping() { return isTrap && trapCooldown <= 0 && canPlaceTrap; }


	/**
	 * Sets whether the chef is actively firing and what direction they are.
	 *
	 * @param value whether the chef is actively firing.
	 * @param slapDirection what direction the chef is slapping in
	 */
	public void setShooting(boolean value, int slapDirection) {
		if (value) animeframe = 0;
		slapFace = slapDirection;
		isShooting = value;
	}

	/**
	 * Animates the up slap
	 * @param texture
	 */
	public void setSlapUpTexture(Texture texture){
		slap_up_animator = new FilmStrip(texture, 1, NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f + 10, animator.getRegionHeight()/2.0f + 10);
	}

	/**
	 * Animates the side slap
	 * @param texture
	 */
	public void setSlapSideTexture(Texture texture) {
		slap_side_animator = new FilmStrip(texture, 1, NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f + 10, animator.getRegionHeight()/2.0f + 10);
	}

	/**
	 * Animates the down slap
	 * @param texture
	 */
	public void setSlapDownTexture(Texture texture) {
		slap_down_animator = new FilmStrip(texture, 1, NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f + 10, animator.getRegionHeight()/2.0f + 10);
	}

	/**
	 * Animates getting damaged
	 * @param texture
	 */
	public void setHurtTexture(Texture texture) {
		hurt_animator = new FilmStrip(texture, 1, 5);
		origin = new Vector2(animator.getRegionWidth()/2.0f + 10, animator.getRegionHeight()/2.0f + 10);
	}

	/**
	 * Animates idle
	 * @param texture
	 */
	public void setIdleTexture(Texture texture) {
		idle_animator = new FilmStrip(texture, 1, 14);
		origin = new Vector2(animator.getRegionWidth()/2.0f + 10, animator.getRegionHeight()/2.0f + 10);
	}

	/**
	 * Returns if the character is alive.
	 *
	 * @return	 if the character is alive
	 */
	public boolean isAlive(){ return health > 0; }

	/** Reduces the chef's health by one. */
	public void decrementHealth() {
		animeframe = 0;
		if (!isStunned() && !invincible) {
			health --;
			invuln_counter = 0f;
		}
	}

	/**
	 * Returns true if the character has recently taken damage and is invulnerable
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
	 * Returns how much force to apply to get the chef moving
	 *
	 * Multiply this by the input to get the movement value.
	 *
	 * @return how much force to apply to get the chef moving
	 */
	public float getForce() {
		return force;
	}

	/**
	 * Returns ow hard the brakes are applied to get a chef to stop moving
	 *
	 * @return ow hard the brakes are applied to get a chef to stop moving
	 */
	private float getDamping() {
		return damping;
	}

	/**
	 * Returns the upper limit on chef left-right movement.
	 *
	 * This does NOT apply to vertical movement.
	 *
	 * @return the upper limit on chef left-right movement.
	 */
	private float getMaxSpeed() {
		return maxspeed;
	}

	/**
	 * Returns the base damage value for the chef
	 *
	 * @return the base damage value for the chef
	 */
	public float getDamage(){ return doubleDamage ? BASE_DAMAGE * 2 : BASE_DAMAGE;}

	/**
	 * sets the double damage flag for the chef, and init the counter
	 *
	 * @param bool the value of the flag
	 */
	public void setDoubleDamage(boolean bool){ doubleDamage = bool; damageTimer = DAMAGE_TIME;}

	/**
	 * Returns true if this character is facing right
	 *
	 * @return true if this character is facing right
	 */
	public boolean isFacingRight() {
		return faceRight;
	}

	/**
	 * Sets textures for the chef, including heart textures and buff textures.
	 * @param h	       heart texture
	 * @param hh       half heart texture
	 * @param att_off  attack buff off texture
	 * @param att_on   attack buff on texture
	 */
	public void setTextures(Texture c, TextureRegion h, TextureRegion hh, Texture att_off, Texture att_on){
		animator = new FilmStrip(c, 1, NUM_ANIM_FRAMES);
		origin = new Vector2(animator.getRegionWidth()/2.0f + 10, animator.getRegionHeight()/2.0f + 10);
		heartTexture = h;
		halfHeartTexture = hh;
		attOffTexture = att_off;
		attOnTexture = new TextureRegion(att_on);
	}

	/**
	 * Creates the game Body(s) for this object, adding them to the world.
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
		Vector2 sensorCenter = new Vector2(0, -getHeight()/4);
		FixtureDef sensorDef = new FixtureDef();
		sensorDef.density = data.getFloat("density",0);
		//sensorDef.isSensor = true;
		sensorDef.filter.groupIndex = -1;
		sensorDef.filter.categoryBits =  0x0002;
		sensorShape = new PolygonShape();
		JsonValue sensorjv = data.get("sensor");
		sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
				sensorjv.getFloat("height",0), sensorCenter, 0.0f);
		sensorDef.shape = sensorShape;

		// Ground sensor to represent our feet
		Fixture sensorFixture = body.createFixture( sensorDef );
		sensorFixture.setUserData(FixtureType.CHEF_HURTBOX);//getSensorName());

		return true;
	}
	

	/**
	 * Applies the force to the body of this chef
	 *
	 * This method should be called after the force attribute is set.
	 */
	public void applyForce() {
		if (!isActive()) {
			return;
		}

		// Velocity too high, clamp it?
		if (Math.abs(getVX()) > getMaxSpeed()) {
			setVX(Math.signum(getVX())*getMaxSpeed());
		} else {
			setVX(getMovement()/*getMaxSpeed()*/);
		}

		// Velocity too high, clamp it
		if (Math.abs(getVY()) > getMaxSpeed()) {
			setVY(Math.signum(getVY())*getMaxSpeed());
		} else {
			setVY(getVertMovement()/*getMaxSpeed()*/);
		}



		// Diagonal Velocity is too high (TO CHANGE IN THE FUTURE)
		if (Math.sqrt(Math.pow(getVX(),2) + Math.pow(getVY(),2)) >= getMaxSpeed()){
			float angle = MathUtils.atan2(getVY(), getVX());
			float vx = getVX();
			float vy = getVY();
			double speed = Math.sqrt((Math.pow(getVX(),2) + Math.pow(getVY(),2)));
			setVY(MathUtils.sin(angle)*getMaxSpeed());
			setVX(MathUtils.cos(angle)*getMaxSpeed());
		}

		if(isPushed){
			forceCache.set(savedSpeed*MathUtils.cos(savedAngle), savedSpeed*MathUtils.sin(savedAngle));
			body.applyForce(forceCache, getPosition(), true);
			pushedCountdown--;
			if (pushedCountdown==0){
				isPushed = false;
			}
		}else {
			if (getMovement() == 0f) {
				//forceCache.set(0, 0);
				setVX(0);
			}
			if (getVertMovement() == 0f) {
				//forceCache.set(0, 0);
				setVY(0);
			}
		}
	}

	public void markSetVelocity(float speed, float angle){
		savedAngle = angle;
		savedSpeed = speed;
		isPushed = true;
		pushedCountdown = 30;
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
		invuln_counter = MathUtils.clamp(invuln_counter+=dt,0f,INVULN_TIME);

		if (isStunned()){
			animeframe += ANIMATION_SPEED;
			if (animeframe >= 5) {
				animeframe -= 5;
			}
		} else if(getVertMovement() != 0 || getMovement() != 0 || shootCooldown > 0) {
			animeframe += ANIMATION_SPEED;
			if (animeframe >= NUM_ANIM_FRAMES) {
				animeframe -= NUM_ANIM_FRAMES;
			}
		} else if (Math.abs(getMovement()) + Math.abs(getVertMovement()) == 0 && shootCooldown <= 0) {
			animeframe += ANIMATION_SPEED;
			if (animeframe >= 14) {
				animeframe -= 14;
			}
		}

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

		if(doubleDamage){
			damageTimer -= dt;
			if(damageTimer <= 0){
				doubleDamage = false;
			}
		}
		super.update(dt);
	}
	/**
	 * Draws the game object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		float effect = faceRight ? 1.0f : -1.0f;
		float yScaleShift = 0.365f;
		float xScaleShift = 0.365f;
		if (isStunned()) {
			hurt_animator.setFrame((int) animeframe);
			canvas.draw(hurt_animator, doubleDamage ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 25, getAngle(), effect*xScaleShift, yScaleShift);
		} else if (Math.abs(getMovement()) + Math.abs(getVertMovement()) == 0 && shootCooldown <= 0){
			idle_animator.setFrame((int)animeframe);
			canvas.draw(idle_animator,doubleDamage ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 25, getAngle(), effect*xScaleShift, yScaleShift);
		} else if (shootCooldown <= 0 && (!isStunned() || ((int)(invuln_counter * 10)) % 2 == 0)) {
			animator.setFrame((int)animeframe);
			canvas.draw(animator, doubleDamage ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 25, getAngle(), effect*xScaleShift, yScaleShift);
		} else if (shootCooldown > 0) {
			if (slapFace == 1) {
				slap_up_animator.setFrame((int) animeframe);
				canvas.draw(slap_up_animator, doubleDamage ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 25, getAngle(), effect*xScaleShift, yScaleShift);
			} else if (slapFace == 3) {
				slap_down_animator.setFrame((int) animeframe);
				canvas.draw(slap_down_animator, doubleDamage ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 25, getAngle(), effect*xScaleShift, yScaleShift);
			} else if (slapFace == 2) {
				slap_side_animator.setFrame((int) animeframe);
				canvas.draw(slap_side_animator, doubleDamage ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 25, getAngle(), xScaleShift, yScaleShift);
			} else if (slapFace == 4){
				slap_side_animator.setFrame((int) animeframe);
				canvas.draw(slap_side_animator, doubleDamage ? Color.FIREBRICK : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y + 25, getAngle(), -1*xScaleShift, yScaleShift);
			}
		}
		//canvas.draw(animator,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y+20,getAngle(),effect/10,0.1f);
		//canvas.drawText("Health: " + health, font, XOFFSET, YOFFSET);
		//draw health

		if (y_health == 0) { y_health = canvas.getHeight() - HEART_SIZE - 20; }
		float x = X_HEALTH;
		for (int i = 1; i <= full_hearts; i++){
			if(i*2 <= health){
				canvas.draw(heartTexture, Color.WHITE, x, y_health, HEART_SIZE, HEART_SIZE);
			}
			else if (i*2-1 <= health){
				canvas.draw(halfHeartTexture, Color.WHITE, x, y_health, HEART_SIZE, HEART_SIZE);
				break;
			} else {
				break;
			}
			x += HEART_SIZE + HEART_SIZE/3;
		}
		if (doubleDamage) {
			float clip_pixels = 53*(1-damageTimer/DAMAGE_TIME);
			float clip_scale = (float)(1-0.36-(clip_pixels/194));
			int add_height = 194-(int)(clip_scale*attOnTexture.getRegionHeight());
			canvas.draw(attOffTexture, Color.WHITE, X_HEALTH-55, y_health - 180, attOffTexture.getWidth(), attOffTexture.getHeight());
			canvas.draw(attOnTexture, clip_scale, 0, Color.WHITE,
					attOnTexture.getRegionWidth()/2, attOnTexture.getRegionHeight()/2, X_HEALTH-55, y_health-180+add_height,
					attOnTexture.getRegionWidth(), attOnTexture.getRegionHeight());
		}
	}
	
	/**
	 * Draws the outline of the game body.
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