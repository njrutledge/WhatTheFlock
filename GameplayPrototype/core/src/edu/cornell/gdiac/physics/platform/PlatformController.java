/*
 * PlatformController.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundBuffer;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class PlatformController extends WorldController implements ContactListener {
	///TODO: Implement a proper board and interactions between the player and chickens, slap may also be implemented here
	////////////// This file puts together a lot of data, be sure that you do not modify something without knowing fully
	////////////// its purpose or you may break someone else's work, further comments are below ////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** Texture asset for character avatar */
	private TextureRegion avatarTexture;
	/** Texture asset for the bullet */
	private TextureRegion bulletTexture;
	/** Texture asset for the chicken */
	private TextureRegion chickenTexture;
	/** Texture asset for the stove */
	private TextureRegion stoveTexture;

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

	/** The current number of chickens */
	private int chickens;
	/** The number of chickens to initially spawn*/
	private int INITIAL_SPAWN = 10;
	/** The chicken spawn chance*/
	private static final int SPAWN_CHANCE = 50; //1 in 50 update calls



	// Physics objects for the game
	/** Physics constants for initialization */
	private JsonValue constants;
	/** Reference to the character avatar */
	private DudeModel avatar;
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



	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/**
	 * Creates and initialize a new instance of the platformer game
	 *
	 * The game has default gravity and other settings
	 */
	public PlatformController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
		chickens = 0;

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
		avatarTexture  = new TextureRegion(directory.getEntry("platform:dude",Texture.class));
		bulletTexture = new TextureRegion(directory.getEntry("platform:bullet",Texture.class));
		chickenTexture  = new TextureRegion(directory.getEntry("platform:chicken",Texture.class));

		jumpSound = directory.getEntry( "platform:jump", SoundBuffer.class );
		fireSound = directory.getEntry( "platform:pew", SoundBuffer.class );
		plopSound = directory.getEntry( "platform:plop", SoundBuffer.class );

		constants = directory.getEntry( "platform:constants", JsonValue.class );
		super.gatherAssets(directory);
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

		// Add level goal
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
		avatar = new DudeModel(constants.get("dude"), dwidth, dheight);
		avatar.setDrawScale(scale);
		avatar.setTexture(avatarTexture);
		addObject(avatar);

		volume = constants.getFloat("volume", 1.0f);

		// Create some chickens
		spawn_xmin = constants.get("chicken").get("spawn_range").get(0).asFloatArray()[0];
		spawn_xmax = constants.get("chicken").get("spawn_range").get(0).asFloatArray()[1];
		spawn_ymin = constants.get("chicken").get("spawn_range").get(1).asFloatArray()[0];
		spawn_ymax = constants.get("chicken").get("spawn_range").get(1).asFloatArray()[1];
		for (int i = 0; i < INITIAL_SPAWN; i++){
			spawnChicken();
		}
	}

	/**
	 * decrements the avatar health by 1
	 */
	public void decrementHealth(){
		avatar.decrementHealth();
	}

	/**
	 * kill all chickens
	 */
	public void killChickens(){
		chickens = 0;
		//TODO: delete all chicken objects from where they are being stored
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
		if (!super.preUpdate(dt)) {
			return false;
		}

		//set failure if avatar's health is 0
		if (!isFailure() && !avatar.isAlive()) {
			setFailure(true);
			return false;
		}

		//TODO check for win condition, when chickens = 0 (see var)


		if (chickens<=0){
			setComplete(true);
			return false;
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
		avatar.setVertmovement(InputController.getInstance().getVertical()*avatar.getForce());
		avatar.setShooting(InputController.getInstance().didSecondary());
		
		// Add a bullet if we fire
		if (avatar.isShooting()) {
			createBullet(InputController.getInstance().getSlapDirection());
		}
		//random chance of spawning a chicken
		if ((int)(Math.random() * (SPAWN_CHANCE + 1)) == 0) {
			spawnChicken();
		}
		for (Obstacle obj : objects) {
			//Remove a bullet if too much time passes (really fast cause its a slap)
			if (obj.isBullet()) {
				removeBullet(obj);
			}
		}

		avatar.applyForce();


	}

	/**
	 * Returns the current avatar health
	 *
	 * @return the current avatar health
	 */
	public int getHealth() {
		return avatar.getHealth();
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
		enemy = new ChickenModel(constants.get("chicken"), x, y, dwidth, dheight, avatar);
		enemy.setDrawScale(scale);
		enemy.setTexture(chickenTexture);
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
	private void createBullet(int direction) {
		//TODO: Instead of creating a bullet, should create a slap which behaves similarly (though inputs will be different)

		///////This will require work outside of this file and method, but this is primarily where the magic happens

		JsonValue bulletjv = constants.get("bullet");
		float radius = 6*bulletTexture.getRegionWidth() / (2.0f * scale.x);
		float offset = radius+1;
		WheelObstacle bullet = new WheelObstacle(avatar.getX(), avatar.getY(), radius);
		if (direction == 2 || direction == 4) {
			offset *= (direction == 2 ? 1 : -1);
			bullet.setX(avatar.getX() + offset);
		} else {
			offset *= (direction == 1 ? 1 : -1);
			bullet.setY(avatar.getY() + offset);
		}
	    bullet.setName("bullet");
		bullet.setDensity(0);
	    bullet.setDrawScale(scale);
	    bullet.setTexture(bulletTexture);
	    Filter bulletFilter = new Filter();
	    bulletFilter.groupIndex = -1;
	    bulletFilter.categoryBits = 0x0002;
	    bullet.setFilterData(bulletFilter);
	    bullet.setBullet(true);
	    bullet.setGravityScale(0);
		
		// Compute position and velocity
//		float speed = bulletjv.getFloat( "speed", 0 );
//		if (direction == 2 || direction == 4) {
//			speed *= (direction == 2 ? 1 : -1);
//			bullet.setVX(speed);
//		} else {
//			speed *= (direction == 1 ? 1 : -1);
//			bullet.setVY(speed);
//		}
		addQueuedObject(bullet);

		fireId = playSound( fireSound, fireId );
	}
	
	/**
	 * Remove a new bullet from the world.
	 *
	 * @param  bullet   the bullet to remove
	 */
	public void removeBullet(Obstacle bullet) {
		//TODO: may need to alter similar to createBullet()
	    bullet.markRemoved(true);
	    plopId = playSound( plopSound, plopId );
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

			// Test bullet collision with world
			if (bd1.getName().equals("bullet") && bd2 != avatar) {
		        removeBullet(bd1);
			}

			if (bd2.getName().equals("bullet") && bd1 != avatar) {
		        removeBullet(bd2);
			}

			//reduce health if chicken collides with avatar
			if ((bd1 == avatar && bd2.getName().equals("chicken"))
					|| (bd2 == avatar && bd1.getName().equals("chicken"))){
				avatar.decrementHealth();
			}

			//cook if player is near stove and not doing anything
			if ((bd1 == avatar && bd2 == stove)
					|| (bd2 == avatar && bd1 == stove)){
				if (avatar.getMovement() == 0f
						&& avatar.getVertMovement() == 0f
						&& !avatar.isShooting()) {
					stove.cookChick();
				}
			}

			//bullet collision with chicken eliminates chicken and bullet
			if (bd1.getName().equals("bullet") && bd2.getName().equals("chicken")) {
				removeBullet(bd1);
				removeChicken(bd2);
			}
			if (bd2.getName().equals("bullet") && bd1.getName().equals("chicken")) {
				removeBullet(bd2);
				removeChicken(bd1);
			}
			//removeChicken()

			//chicken to chicken collision does nothing

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

		if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
			(avatar.getSensorName().equals(fd1) && avatar != bd2)) {
			sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
		}
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
}