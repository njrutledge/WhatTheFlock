/*
 * WorldController.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundBuffer;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.Iterator;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class WorldController implements ContactListener, Screen {
	///TODO: Implement a proper board and interactions between the player and chickens, slap may also be implemented here
	////////////// This file puts together a lot of data, be sure that you do not modify something without knowing fully
	////////////// its purpose or you may break someone else's work, further comments are below ////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** The texture for walls and platforms */
	protected TextureRegion earthTile;
	/** The font for giving messages to the player */
	protected BitmapFont displayFont;

	/** Texture asset for character avatar */
	private TextureRegion avatarTexture;
	/** Texture asset for the bullet */
	private TextureRegion bulletTexture;
	/** Texture asset for the chicken */
	private TextureRegion chickenTexture;
	/** Texture asset for the stove */
	private TextureRegion stoveTexture;
	/** Texture asset for the trap (TEMP) */
	private TextureRegion trapTexture;
	/** Texture asset for chicken health bar */
	private TextureRegion enemyHealthBarTexture;

	/** Texture asset for the chef*/
	private Texture chefTexture;
	/** Texture asset for the nugget */
	private Texture nuggetTexture;

	/** The jump sound.  We only want to play once. */
	private SoundBuffer jumpSound;
	private long jumpId = -1;
	/** The weapon fire sound.  We only want to play once. */
	private SoundBuffer fireSound;
	private long fireId = -1;
	/** The weapon pop sound.  We only want to play once. */
	private SoundBuffer plopSound;
	private long plopId = -1;
	/** The default sound volume */
	private float volume;

	private SoundBuffer chickHurt;
	private SoundBuffer chickAttack;
	private long cSoundID = -2;
	private SoundBuffer fireTrig;
	private SoundBuffer fireLinger;
	private SoundBuffer lureCrumb;
	private SoundBuffer emptySlap;
	private SoundBuffer chickOnFire;
	private SoundBuffer slowSquelch;


	private final float DEFAULT_VOL = 0.5F;

	/** The current number of chickens */
	private int chickens;

	/** The amount of time for a physics engine step. */
	public static final float WORLD_STEP = 1/60.0f;
	/** Number of velocity iterations for the constrain solvers */
	public static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers */
	public static final int WORLD_POSIT = 2;

	/** Exit code for quitting the game */
	public static final int EXIT_QUIT = 0;
	/** Exit code for advancing to next level */
	public static final int EXIT_NEXT = 1;
	/** Exit code for jumping back to previous level */
	public static final int EXIT_PREV = 2;
	/** How many frames after winning/losing do we continue? */
	public static final int EXIT_COUNT = 120;

	/** Width of the game world in Box2d units */
	protected static final float DEFAULT_WIDTH  = 32.0f;
	/** Height of the game world in Box2d units */
	protected static final float DEFAULT_HEIGHT = 18.0f;
	/** The default value of gravity (going down) */
	protected static final float DEFAULT_GRAVITY = -4.9f;

	/** Whether or not the player is cooking, true is they are and false otherwise*/
	private boolean cooking;

	private Trap trapCache;

	// Physics objects for the game
	/** Physics constants for initialization */
	private JsonValue constants;
	/** Reference to the character avatar */
	private ChefModel avatar;
	/** Reference to the temperature*/
	private TemperatureBar temp;
	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle goalDoor;
	/** The minimum x position of a spawned chicken*/
	private static float spawn_xmin;
	/** The maximum x position of a spawned chicken*/
	private static float spawn_xmax;
	/** The minimum y position of a spawned chicken*/
	private static float spawn_ymin;
	/** The maximum y position of a spawned chicken */
	private static float spawn_ymax;
	/** Reference to the stove object */
	private StoveModel stove;

	/** The trap the player has currently selected */
	private Trap.type trapTypeSelected = Trap.type.LURE;
	/** The parameter from the list of parameters currently selected */
	private int parameterSelected = 0;
	/** List of all parameter values {player max health, chicken max health, base damage (player), spawn rate (per update frames), initial spawn}*/
	private int[] parameterList = {3, 5, 2, 200, 2, 3, 30, 10, 5, 5, 2, 5};


	/** Reference to the game canvas */
	protected GameCanvas canvas;
	/** All the objects in the world. */
	protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
	/** Queue for adding objects */
	protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** The Box2D world */
	protected World world;
	/** The boundary of the world */
	protected Rectangle bounds;
	/** The world scale */
	protected Vector2 scale;

	/** Whether or not this is an active controller */
	private boolean active;
	/** Whether we have completed this level */
	private boolean complete;
	/** Whether we have failed at this world (and need a reset) */
	private boolean failed;
	/** Whether or not debug mode is active */
	private boolean debug;
	/** Countdown active for winning or losing */
	private int countdown;

	/** Whether or not mute is toggled */
	private boolean muted = false;

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/**
	 * Returns true if debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @return true if debug mode is active.
	 */
	public boolean isDebug( ) {
		return debug;
	}

	/**
	 * Sets whether debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @param value whether debug mode is active.
	 */
	public void setDebug(boolean value) {
		debug = value;
	}

	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public WorldController() {
		this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT), new Vector2(0,DEFAULT_GRAVITY));
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
		chickens = 0;
		cooking = false;
	}


	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param bounds	The game bounds in Box2d coordinates
	 * @param gravity	The gravitational force on this Box2d world
	 */
	protected WorldController(Rectangle bounds, Vector2 gravity){
		world = new World(gravity,false);
		this.bounds = new Rectangle(bounds);
		this.scale = new Vector2(1,1);
		complete = false;
		failed = false;
		debug  = false;
		active = false;
		countdown = -1;
	}

	/**
	 * Gather the assets for this controller.
	 *
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory	Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		//TODO add temperature and health assets here
		avatarTexture  = new TextureRegion(directory.getEntry("platform:dude",Texture.class));
		bulletTexture = new TextureRegion(directory.getEntry("platform:bullet",Texture.class));
		chickenTexture  = new TextureRegion(directory.getEntry("platform:chicken",Texture.class));
		trapTexture = new TextureRegion(directory.getEntry("platform:trap",Texture.class));
		stoveTexture = new TextureRegion(directory.getEntry("platform:stove",Texture.class));
		earthTile = new TextureRegion(directory.getEntry( "shared:earth", Texture.class ));
		enemyHealthBarTexture = new TextureRegion(directory.getEntry("platform:nuggetBar", Texture.class));

		chefTexture = directory.getEntry("platform:chef", Texture.class);
		nuggetTexture = directory.getEntry("platform:nugget", Texture.class);

		displayFont = directory.getEntry( "shared:retro" ,BitmapFont.class);

		jumpSound = directory.getEntry( "platform:jump", SoundBuffer.class );
		fireSound = directory.getEntry( "platform:pew", SoundBuffer.class );
		plopSound = directory.getEntry( "platform:plop", SoundBuffer.class );

		chickHurt = directory.getEntry( "platform:chickHurt", SoundBuffer.class );
		chickAttack = directory.getEntry( "platform:chickAttack", SoundBuffer.class );
		fireTrig = directory.getEntry( "platform:fireTrig", SoundBuffer.class );
		fireLinger = directory.getEntry( "platform:fireLinger", SoundBuffer.class );
		lureCrumb = directory.getEntry( "platform:lureCrumb", SoundBuffer.class );
		emptySlap = directory.getEntry( "platform:emptySlap", SoundBuffer.class );
		chickOnFire = directory.getEntry( "platform:chickOnFire", SoundBuffer.class );
		slowSquelch = directory.getEntry("platform:squelch", SoundBuffer.class);



		constants = directory.getEntry( "platform:constants", JsonValue.class );
	}

	
	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );
		
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		
		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		chickens = 0;
		populateLevel();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		//TODO: Populate level similar to our board designs, and also change the win condition (may require work outside this method)

		String wname = "wall";
	    JsonValue walljv = constants.get("walls");
		JsonValue defaults = constants.get("defaults");
		for (int ii = 0; ii < walljv.size; ii++) {
	        PolygonObstacle obj;
	    	obj = new PolygonObstacle(walljv.get(ii).asFloatArray(), 0, 0);
			obj.setBodyType(BodyDef.BodyType.StaticBody);
			obj.setDensity(defaults.getFloat( "density", 0.0f ));
			obj.setFriction(defaults.getFloat( "friction", 0.0f ));
			obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
			obj.setDrawScale(scale);
			obj.setTexture(earthTile);
			obj.setName(wname+ii);
			addObject(obj);
	    }

	    //put some other platforms in the world
	    String pname = "platform";
		JsonValue platjv = constants.get("platforms");
	    for (int ii = 0; ii < platjv.size; ii++) {
	        PolygonObstacle obj;
	    	obj = new PolygonObstacle(platjv.get(ii).asFloatArray(), 0, 0);
			obj.setBodyType(BodyDef.BodyType.StaticBody);
			obj.setDensity(defaults.getFloat( "density", 0.0f ));
			obj.setFriction(defaults.getFloat( "friction", 0.0f ));
			obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
			obj.setDrawScale(scale);
			obj.setTexture(earthTile);
			obj.setName(pname+ii);
			addObject(obj);
	    }

	    // This world is heavier
		world.setGravity( new Vector2(0,0) );

		// Create dude
		float dwidth  = avatarTexture.getRegionWidth()/scale.x;
		float dheight = avatarTexture.getRegionHeight()/scale.y;
		avatar = new ChefModel(constants.get("dude"), dwidth, dheight, parameterList[0]);
		avatar.setDrawScale(scale);
		avatar.setTexture(chefTexture);
		//Set temperature based on difficulty of the level
		temp = new TemperatureBar(30);

		//avatar.setMaxTemp(30);

		addObject(avatar);

		// Add stove
		float swidth = stoveTexture.getRegionWidth()/scale.x;
		float sheight = stoveTexture.getRegionHeight()/scale.y;
		stove = new StoveModel(constants.get("stove"),16,9,swidth,sheight);
		stove.setDrawScale(scale);
		stove.setTexture(stoveTexture);
		addObject(stove);

		volume = constants.getFloat("volume", 1.0f);

		// Create some chickens
		spawn_xmin = constants.get("chicken").get("spawn_range").get(0).asFloatArray()[0];
		spawn_xmax = constants.get("chicken").get("spawn_range").get(0).asFloatArray()[1];
		spawn_ymin = constants.get("chicken").get("spawn_range").get(1).asFloatArray()[0];
		spawn_ymax = constants.get("chicken").get("spawn_range").get(1).asFloatArray()[1];
		for (int i = 0; i < parameterList[4]; i++){
			spawnChicken();
		}

		// Get initial values for parameters in the list
		//parameterList[0] = avatar.getMaxHealth();

	}

	/**
	 * kill all chickens
	 */
	public void killChickens(){
		chickens = 0;
		for (Obstacle obstacle: objects){
			if (obstacle.getName().equals("chicken")){
				removeChicken(obstacle);
			}
		}
	}
	
	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt	Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		if (!preUpdateHelper(dt)) {
			return false;
		}

		//set failure if avatar's health is 0
		if (!isFailure() && !avatar.isAlive()) {
			setFailure(true);
			return false;
		}

		if (temp.isCooked()){
			setComplete(true);
			return false;
		}

		return true;
	}

	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt	Number of seconds since last animation frame
	 *
	 * @return whether to process the update loop
	 */
	private boolean preUpdateHelper(float dt){
		InputController input = InputController.getInstance();
		input.readInput(bounds, scale);
		if (listener == null) {
			return true;
		}

		// Toggle debug
		if (input.didDebug()) {
			debug = !debug;
		}

		// Handle resets
		if (input.didReset()) {
			reset();
		}
		if(input.didAdvance()) {
			avatar.decrementHealth();
		}
		if(input.didRetreat()) {
			killChickens();
		}

		// Now it is time to maybe switch screens.
		if (input.didExit()) {
			pause();
			listener.exitScreen(this, EXIT_QUIT);
			return false;
		} else if (countdown > 0) {
			countdown--;
		} else if (countdown == 0) {
			if (failed) {
				reset();
			} else if (complete) {
				pause();
				listener.exitScreen(this, EXIT_NEXT);
				return false;
			}
		}
		return true;
	}


	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void update(float dt) {
		// Process actions in object model
		avatar.setMovement(InputController.getInstance().getHorizontal() *avatar.getForce());
		avatar.setVertMovement(InputController.getInstance().getVertical()*avatar.getForce());
		avatar.setShooting(InputController.getInstance().didSecondary());
		avatar.setTrap(InputController.getInstance().didTrap());

		if(InputController.getInstance().didMute()){
			muted = !muted;
		}
		if (muted) {
			volume = 0;
		} else {
			volume = DEFAULT_VOL;
		}

		// Rotate through player's available traps
		if (InputController.getInstance().didRotateTrapLeft()){
			if (trapTypeSelected == Trap.type.LURE){
				trapTypeSelected = Trap.type.FIRE;
			} else if (trapTypeSelected == Trap.type.SLOW){
				trapTypeSelected = Trap.type.LURE;
			} else {
				trapTypeSelected = Trap.type.SLOW;
			}
		} else if (InputController.getInstance().didRotateTrapRight()){
			if (trapTypeSelected == Trap.type.LURE) {
				trapTypeSelected = Trap.type.SLOW;
			} else if (trapTypeSelected == Trap.type.SLOW){
				trapTypeSelected = Trap.type.FIRE;
			} else {
				trapTypeSelected = Trap.type.LURE;
			}
		}

		// Change the parameter currently selected
		if (InputController.getInstance().didParameterToggle()){
			if (parameterSelected < parameterList.length-1){
				parameterSelected += 1;
			} else {
				parameterSelected = 0;
			}
		}
		// Increase the current parameter
		if (InputController.getInstance().didParameterIncreased()){
			parameterList[parameterSelected] = Math.max(0, parameterList[parameterSelected]+1);
		}
		// Decrease the current parameter
		if (InputController.getInstance().didParameterDecreased()){
			parameterList[parameterSelected] = Math.max(0, parameterList[parameterSelected]-1);
		}

		
		// Add a bullet if we fire
		if (avatar.isShooting()) {
			createSlap(InputController.getInstance().getSlapDirection());
		}

		// Add a trap if trying to press
		if (avatar.isTrapping()) {
			createTrap();
		}

		//random chance of spawning a chicken
		if ((int)(Math.random() * (parameterList[3] + 1)) == 0) {
			spawnChicken();
		}
		for (Obstacle obj : objects) {
			//Remove a bullet if slap is complete
			if (obj.isBullet() && (obj.getAngle() > Math.PI/8 || obj.getAngle() < Math.PI/8*-1)) {
				removeBullet(obj);
			}
		}

		avatar.applyForce();

		//update temperature
		if (cooking && (avatar.getMovement() == 0f
						&& avatar.getVertMovement() == 0f
						&& !avatar.isShooting())) {
			//avatar.cook(true);
			temp.cook(true);
		}else {
			//avatar.cook(false);
			temp.cook(false);
		}

		temp.update(dt);
	}

	/**
	 * Spawn a chicken somewhere in the world, then increments the number of chickens
	 */
	private void spawnChicken(){
		float dwidth  = chickenTexture.getRegionWidth()/scale.x;
		float dheight = chickenTexture.getRegionHeight()/scale.y;
		float x = ((float)Math.random() * (spawn_xmax - spawn_xmin) + spawn_xmin);
		float y = ((float)Math.random() * (spawn_ymax - spawn_ymin) + spawn_ymin);
		float rand = (float)Math.random();
		// Spawn chicken at the border of the world
		if (rand < 0.25){
			x = spawn_xmin;
		}
		else if (rand < 0.5){
			x = spawn_xmax;
		}
		else if (rand < 0.75){
			y = spawn_ymin;
		}
		else{
			y = spawn_ymax;
		}

		ChickenModel enemy;
		enemy = new ChickenModel(constants.get("chicken"), x, y, dwidth, dheight, avatar, parameterList[1]);
		enemy.setDrawScale(scale);
		enemy.setTexture(nuggetTexture);
		enemy.setBarTexture(enemyHealthBarTexture);
		addObject(enemy);
		chickens ++;
	}

	/**
	 * Removes the given chicken from the world, then decrements the number of chickens
	 * @param chicken	 the chicken to remove
	 */
	private void removeChicken(Obstacle chicken){
		if(!chicken.isRemoved()) {
			chicken.markRemoved(true);
			chickens--;
		}
	}
	/**
	 * Add a new bullet to the world and send it in the right direction.
	 *
	 */
	private void createSlap(int direction) {
		//TODO: Slap needs to go through multiple enemies, specific arc still needs to be tweaked, probably best if in-game changing of variables is added
		if (temp.getTemperature() == 0){
			return;
		} else{
			temp.reduceTemp(1);
		}

		float radius = 8*bulletTexture.getRegionWidth() / (2.0f * scale.x);
		float offset = 1f;
		float angvel = 6f;
		float ofratio = 0.7f;
		BoxObstacle slap;
		if (direction == 2 || direction == 4) {
			slap = new BoxObstacle(avatar.getX(), avatar.getY(), radius, 0.1f);
			slap.setSensor(true);
			offset *= (direction == 2 ? 1 : -1);
			slap.setX(avatar.getX() + offset);
			slap.setY(avatar.getY() - offset*ofratio);
			slap.setAngle((float)(-1*Math.PI/24));
			slap.setAngularVelocity(angvel);
		} else {
			slap = new BoxObstacle(avatar.getX(), avatar.getY(), 0.1f, radius);
			slap.setSensor(true);
			offset *= (direction == 1 ? 1 : -1);
			slap.setY(avatar.getY() + offset);
			slap.setX(avatar.getX() - offset*ofratio);
			slap.setAngle((float)(1*Math.PI/24));
			slap.setAngularVelocity(-1*angvel);
		}


	    slap.setName("bullet");
		slap.setDensity(0);
	    slap.setDrawScale(scale);
	    slap.setTexture(bulletTexture);
	    Filter bulletFilter = new Filter();
	    bulletFilter.groupIndex = -1;
	    bulletFilter.categoryBits = 0x0002;
	    slap.setFilterData(bulletFilter);
	    slap.setBullet(true);
	    slap.setGravityScale(0);
		
		// Compute position and velocity
		float speed = 175;
		if (direction == 2 || direction == 4) {
			speed *= (direction == 2 ? 0.1f : -0.1f);
			slap.setVY(speed);
		} else {
			speed *= (direction == 1 ? 0.1f : -0.1f);
			slap.setVX(speed);
		}
		addQueuedObject(slap);
		emptySlap.play(volume);

	}
	
	/**
	 * Remove a new bullet from the world.
	 *
	 * @param  bullet   the bullet to remove
	 */
	public void removeBullet(Obstacle bullet) {
		//TODO: may need to alter similar to createBullet()
	    bullet.markRemoved(true);
	}

	public void createTrap() {
		//spawn test traps
		trapHelper(avatar.getX(), avatar.getY(), trapTypeSelected);

	}

	public void trapHelper(float x, float y, Trap.type t){
		float twidth = trapTexture.getRegionWidth()/scale.x;
		float theight = trapTexture.getRegionHeight()/scale.y;
		Trap trap = new Trap(constants.get("trap"), avatar.getX(), avatar.getY(), twidth, theight, trapTypeSelected, Trap.shape.CIRCLE);
		trap.setDrawScale(scale);
		trap.setTexture(trapTexture);
		addObject(trap);
//		trap = new Trap(constants.get("trap"), 20, 4, twidth, theight, Trap.type.TRAP_ONE, Trap.shape.SQUARE);
//		trap.setDrawScale(scale);
//		trap.setTexture(trapTexture);
//		addObject(trap);
	}
	/**
	 *  decrement the trap durability, and remove the trap if it breaks.
	 *
	 * @param trap   the trap to decrement durability and possibly remove
	 */
	public void decrementTrap(Trap trap){
		if(!trap.isRemoved() && trap.decrementDurability()){
			trap.markRemoved(true);
		}
	}

	public float damageCalc(){
		return avatar.getDamage() + 2*avatar.getDamage()*temp.getPercentCooked();
	}
	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use 
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		//TODO: Detect if a collision is with an enemy and have an appropriate interaction
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();
		
		try {
			Obstacle bd1 = (Obstacle)body1.getUserData();
			Obstacle bd2 = (Obstacle)body2.getUserData();


			//reduce health if chicken collides with avatar
			if (bd1 == avatar && bd2.getName().equals("chicken") && !((ChickenModel)bd2).isStunned()) {
				avatar.decrementHealth();
				((ChickenModel) bd2).hitPlayer();
				chickAttack.stop();
				chickAttack.play(volume*0.5f);
			}

			if ((bd2 == avatar && bd1.getName().equals("chicken"))&& !((ChickenModel)bd1).isStunned()){
				avatar.decrementHealth();
				((ChickenModel) bd1).hitPlayer();
				chickAttack.stop();
				chickAttack.play(volume*0.5f);
			}

			//cook if player is near stove and not doing anything
			if ((bd1 == avatar && bd2 == stove)
					|| (bd2 == avatar && bd1 == stove)) {
				cooking = true;
			}

			//bullet collision with chicken eliminates chicken

			if (fd1 != null){
				if (bd2.getName().equals("bullet") && fd1.equals("chickenSensor")) {
					chickHurt.stop();
					chickHurt.play(volume);
					fireSound.stop();
					fireSound.play(volume);
					ChickenModel chick = (ChickenModel) bd1;
					chick.takeDamage(damageCalc());
					if (!chick.isAlive()) {
						removeChicken(bd1);
					}
				}
			}

			if (fd2 != null) {
				if (bd1.getName().equals("bullet") && fd2.equals("chickenSensor")) {
					chickHurt.stop();
					chickHurt.play(volume);
					fireSound.stop();
					fireSound.play(volume);
					ChickenModel chick = (ChickenModel) bd2;
					chick.takeDamage(damageCalc());
					if (!chick.isAlive()) {
						removeChicken(bd2);
					}
				}
			}

			//trap collision with chicken eliminates chicken


			if (fd1 != null && fd2 != null) {

				if (fd1.equals("lureHurt") && bd2.getName().equals("chicken")) {
					decrementTrap((Trap) bd1);
					lureCrumb.play(volume);
				}

				if (fd2.equals("lureHurt") && bd1.getName().equals("chicken")){
					decrementTrap((Trap) bd2);
					lureCrumb.play(volume);
				}

				if (fd1.equals("trapSensor") && bd2.getName().equals("chicken")) {
					switch (((Trap) bd1).getTrapType()){
						case LURE: //damage
							((ChickenModel) bd2).trapTarget((Trap) bd1);
							break;
						case SLOW:
							((ChickenModel) bd2).applySlow(((Trap) bd1).getEffect());
							decrementTrap((Trap) bd1);
							slowSquelch.stop();
							slowSquelch.play(volume);
							break;
						case FIRE :
							float twidth = trapTexture.getRegionWidth()/scale.x;
							float theight = trapTexture.getRegionHeight()/scale.y;
							trapCache = new Trap(constants.get("trap"), bd1.getX(), bd1.getY(), twidth, theight, Trap.type.FIRE_LINGER, Trap.shape.CIRCLE);
							trapCache.setDrawScale(scale);
							trapCache.setTexture(trapTexture);
							addQueuedObject(trapCache);
							decrementTrap((Trap) bd1);
							fireTrig.play(volume);
							fireLinger.play(volume*0.5f);
							break;
						case FIRE_LINGER:
							((ChickenModel) bd2).applyFire(((Trap) bd1).getEffect());
							chickOnFire.stop();
							chickOnFire.play(volume*0.5f);
					}
				}
				if (fd2.equals("trapSensor") && bd1.getName().equals("chicken")) {
					switch (((Trap) bd2).getTrapType()){
						case LURE: //damage
							((ChickenModel) bd1).trapTarget((Trap) bd2);
							break;
						case SLOW:
							((ChickenModel) bd1).applySlow(((Trap) bd2).getEffect());
							decrementTrap((Trap) bd2);
							slowSquelch.stop();
							slowSquelch.play(volume);
							break;
						case FIRE:
							float twidth = trapTexture.getRegionWidth()/scale.x;
							float theight = trapTexture.getRegionHeight()/scale.y;
							trapCache = new Trap(constants.get("trap"), bd2.getX(), bd2.getY(), twidth, theight, Trap.type.FIRE_LINGER, Trap.shape.CIRCLE);
							trapCache.setDrawScale(scale);
							trapCache.setTexture(trapTexture);
							addQueuedObject(trapCache);
							decrementTrap((Trap) bd2);
							fireTrig.play(volume);
							fireLinger.play(volume*0.5f);
							break;
						case FIRE_LINGER:
							((ChickenModel) bd1).applyFire(((Trap) bd2).getEffect());
							chickOnFire.stop();
							chickOnFire.play(volume*0.5f);
					}
				}
			}
			if ((bd1.getName().contains("platform")|| (bd1.getName().equals("stove") && !fix1.isSensor())) && bd2.getName().equals("chicken")){
				((ChickenModel)bd2).hitWall();
			}
			if ((bd2.getName().contains("platform") || (bd2.getName().equals("stove") && !fix2.isSensor())) && bd1.getName().equals("chicken")){
				((ChickenModel)bd1).hitWall();

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */ 
	public void endContact(Contact contact) {
		//TODO: Detect if collision is with an enemy and give appropriate interaction (if any needed)
		Fixture fix1 = contact.getFixtureA();
		Fixture fix2 = contact.getFixtureB();

		Body body1 = fix1.getBody();
		Body body2 = fix2.getBody();

		Object fd1 = fix1.getUserData();
		Object fd2 = fix2.getUserData();
		
		Object bd1 = body1.getUserData();
		Object bd2 = body2.getUserData();

		Obstacle b1 = (Obstacle) bd1;
		Obstacle b2 = (Obstacle) bd2;

		if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
			(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
			sensorFixtures.remove((avatar == bd1) ? fix2 : fix1);
		}

		if (avatar.getSensorName().equals(fd2) && stove.getSensorName().equals(fd1) ||
				avatar.getSensorName().equals(fd1) && stove.getSensorName().equals(fd2)){
			cooking = false;
		}
		if (b1.getName().equals("trap") && b2.getName().equals("chicken")) {
			switch (((Trap) b1).getTrapType()){
				case LURE:
					((ChickenModel) b2).resetTarget();
					break;
				case SLOW:
					((ChickenModel) b2).removeSlow();
					break;
				case FIRE :
					break;
				case FIRE_LINGER:
					((ChickenModel) b2).letItBurn();
			}
		}
		if (b2.getName().equals("trap") && b1.getName().equals("chicken")) {
			switch (((Trap) b2).getTrapType()){
				case LURE: //damage
					((ChickenModel) b1).resetTarget();
					break;
				case SLOW:
					((ChickenModel) b1).removeSlow();
					break;
				case FIRE :
					break;
				case FIRE_LINGER:
					((ChickenModel) b1).letItBurn();
			}
		}
	}

	/**
	 *
	 * Adds a physics object in to the insertion queue.
	 *
	 * Objects on the queue are added just before collision processing.  We do this to
	 * control object creation.
	 *
	 * param obj The object to add
	 */
	public void addQueuedObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		addQueue.add(obj);
	}

	/**
	 * Immediately adds the object to the physics world
	 *
	 * param obj The object to add
	 */
	protected void addObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		objects.add(obj);
		obj.activatePhysics(world);
	}

	/**
	 * Returns true if the object is in bounds.
	 *
	 * This assertion is useful for debugging the physics.
	 *
	 * @param obj The object to check.
	 *
	 * @return true if the object is in bounds.
	 */
	public boolean inBounds(Obstacle obj) {
		boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
		boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
		return horiz && vert;
	}
	
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}

	/**
	 * Called when the Screen is paused.
	 *
	 * We need this method to stop all sounds when we pause.
	 * Pausing happens when we switch game modes.
	 */
	public void pause() {
		if (jumpSound.isPlaying( jumpId )) {
			jumpSound.stop(jumpId);
		}
		if (plopSound.isPlaying( plopId )) {
			plopSound.stop(plopId);
		}
		if (fireSound.isPlaying( fireId )) {
			fireSound.stop(fireId);
		}
	}

	/**
	 * Processes physics
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * physics.  The primary method is the step() method in world.  This implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		// Add any objects created by actions
		while (!addQueue.isEmpty()) {
			addObject(addQueue.poll());
		}

		// Turn the physics engine crank.
		world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
		while (iterator.hasNext()) {
			PooledList<Obstacle>.Entry entry = iterator.next();
			Obstacle obj = entry.getValue();
			if (obj.isRemoved()) {
				obj.deactivatePhysics(world);
				entry.remove();
			} else {
				if(obj.isDirty()){
					obj.deactivatePhysics(world);
					obj.activatePhysics(world);
				}
				// Note that update is called last!
				obj.update(dt, parameterList);
			}
		}
	}

	/**
	 * Draw the physics objects to the canvas
	 *
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overriden if the world needs fancy backgrounds or the like.
	 *
	 * The method draws all objects in the order that they were added.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void draw(float dt) {
		canvas.clear();

		String s = "";
		switch (trapTypeSelected ){
			case LURE:
				s = "lure";
				break;
			case SLOW:
				s = "slow";
				break;
			case FIRE:
				s = "fire";
				break;
		}

		canvas.begin();
		canvas.drawText("Trap Selected: " + s, new BitmapFont(), 100, 540);
		// Draws out all the parameters and their values
		String[] parameters = {"player max health: ", "chicken max health: ", "base damage (player): ", "spawn rate: ", "initial spawn: ",
				"lure durability: ", "slow durability: ", "fire linger durability: ", "fire damage durability: ", "player speed: ",
				"enemy speed: ", "invulnerability time: "};
		BitmapFont pFont = new BitmapFont();
		for (int i = 0; i < parameterList.length; i++) {
			if (i == parameterSelected) {
				pFont.setColor(Color.YELLOW);
			} else {
				pFont.setColor(Color.WHITE);
			}
			canvas.drawText(parameters[i] + parameterList[i], pFont, 40, 520 - 14 * i);
		}

		for(Obstacle obj : objects) {
			obj.draw(canvas);
		}
		if ((cooking && (avatar.getMovement() == 0f
				&& avatar.getVertMovement() == 0f
				&& !avatar.isShooting()))){
			stove.drawLit(canvas);
		}
		canvas.end();

		if (debug) {
			canvas.beginDebug();
			for(Obstacle obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}

		//TODO add section for UI
		//draw temp bar
		canvas.begin();

		temp.draw(canvas);
		canvas.end();

		// Final message
		if (complete && !failed) {
			displayFont.setColor(Color.YELLOW);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("VICTORY!", displayFont, 0.0f);
			canvas.end();
		} else if (failed) {
			displayFont.setColor(Color.RED);
			canvas.begin(); // DO NOT SCALE
			canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
			canvas.end();
		}
	}

	/**
	 * Method to ensure that a sound asset is only played once.
	 *
	 * Every time you play a sound asset, it makes a new instance of that sound.
	 * If you play the sounds to close together, you will have overlapping copies.
	 * To prevent that, you must stop the sound before you play it again.  That
	 * is the purpose of this method.  It stops the current instance playing (if
	 * any) and then returns the id of the new instance for tracking.
	 *
	 * @param sound		The sound asset to play
	 * @param soundId	The previously playing sound instance
	 *
	 * @return the new sound instance for this asset.
	 */
	public long playSound(SoundBuffer sound, long soundId) {
		return playSound( sound, soundId, volume);
	}


	/**
	 * Method to ensure that a sound asset is only played once.
	 *
	 * Every time you play a sound asset, it makes a new instance of that sound.
	 * If you play the sounds to close together, you will have overlapping copies.
	 * To prevent that, you must stop the sound before you play it again.  That
	 * is the purpose of this method.  It stops the current instance playing (if
	 * any) and then returns the id of the new instance for tracking.
	 *
	 * @param sound		The sound asset to play
	 * @param soundId	The previously playing sound instance
	 * @param volume	The sound volume
	 *
	 * @return the new sound instance for this asset.
	 */
	public long playSound(SoundBuffer sound, long soundId, float volume) {
		if (soundId != -1 && sound.isPlaying( soundId )) {
			sound.stop( soundId );
		}
		return sound.play(volume);
	}

	/**
	 * Returns true if the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @return true if the level is completed.
	 */
	public boolean isComplete( ) {
		return complete;
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		complete = value;
	}

	/**
	 * Returns true if the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @return true if the level is failed.
	 */
	public boolean isFailure( ) {
		return failed;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		failed = value;
	}


	/**
	 * Returns true if this is the active screen
	 *
	 * @return true if this is the active screen
	 */
	public boolean isActive( ) {
		return active;
	}

	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 *
	 * @return the canvas associated with this controller
	 */
	public GameCanvas getCanvas() {
		return canvas;
	}

	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers.  Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 * @param canvas the canvas associated with this controller
	 */
	public void setCanvas(GameCanvas canvas) {
		this.canvas = canvas;
		this.scale.x = canvas.getWidth()/bounds.getWidth();
		this.scale.y = canvas.getHeight()/bounds.getHeight();
	}

	/**
	 * Called when the Screen is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		objects = null;
		addQueue = null;
		bounds = null;
		scale  = null;
		world  = null;
		canvas = null;
	}

	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			if (preUpdate(delta)) {
				update(delta); // This is the one that must be defined.
				postUpdate(delta);
			}
			draw(delta);
		}
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}
}