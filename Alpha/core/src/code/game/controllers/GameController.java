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
package code.game.controllers;

import code.assets.AssetDirectory;
import code.audio.SoundBuffer;
import code.game.models.*;
import code.game.models.obstacle.BoxObstacle;
import code.game.models.obstacle.Obstacle;
import code.game.views.GameCanvas;
import code.util.FilmStrip;
import code.util.PooledList;
import code.util.ScreenListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class GameController implements ContactListener, Screen, InputProcessor {
	///TODO: Implement a proper board and interactions between the player and chickens, slap may also be implemented here
	////////////// This file puts together a lot of data, be sure that you do not modify something without knowing fully
	////////////// its purpose or you may break someone else's work, further comments are below ////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/** The texture for the background */
	protected TextureRegion background;
	//~~~~~~~~~~~~~~~ The textures for walls ~~~~~~~~~~~~~~~~~~~~~
	protected Texture wallCenterTile;
	protected Texture wallLeftTile;
	protected Texture wallRightTile;
	protected Texture wallBottomTile;
	protected Texture wallTopTile;
	protected Texture wallTop2Tile;
	protected Texture wallTopLeftCornerTile;
	protected Texture wallTopRightCornerTile;
	protected Texture wallTopLeftCorner2Tile;
	protected Texture wallTopRightCorner2Tile;
	protected Texture wallBottomLeftCorner;
	protected Texture wallBottomRightCorner;
	protected Texture wallYellowCenterTile;
	protected Texture wallYellowBottomTile;
	protected Texture wallCeilingTile;
	protected Texture wallCeilingBottomTile;

	/** The font for giving messages to the player */
	protected BitmapFont displayFont;

	/** Texture asset for the bullet */
	private Texture bulletTexture;

	/** Texture asset for the chicken */
	private TextureRegion chickenTexture;
	/** Texture asset for chicken health bar */
	private TextureRegion enemyHealthBarTexture;

	/** Texture asset for the stove */
	private Texture stoveTexture;
	/** Texture asset for active stove*/
	private Texture stoveActiveTexture;
	/** Texture asset for inactive stove*/
	private Texture stoveInactiveTexture;
	/** Texture asset for default trap (TEMP) */
	private Texture trapDefaultTexture;
	/** Texture asset for Fidge trap */
	private Texture trapCoolerTexture;
	/** Texture for animating Cooler trap*/
	private Texture trapCoolerActivate;
	private final int COOLER_NUM_FRAMES = 17;

	/** Texture asset for toaster trap */
	private Texture trapToasterTexture;
	/** The texture for animating the Toaster*/
	private Texture trapToasterActivate;
	/** Number of frames in Toaster texture*/
	private final int TOASTER_NUM_FRAMES = 12;
	/** Texture asset for hot sauce trap */
	private Texture trapHotSauceTexture;
	/** Texture for animating Hot Sauce Trap*/
	private Texture trapHotSauceActivate;
	/** Number of frames in Hot Sauce texture*/
	private final int HOTSAUCE_NUM_FRAMES = 8;
	/** Texture asset for slap indicator above traps */
	private TextureRegion indicatorTexture;
	/** Texture asset for the spawnpoint*/
	private Texture spawnTexture;

	private float gridWidth;
	private float gridHeight;

	/** Texture asset for the chef*/
	private Texture chefTexture;
	/** Texture asset for the nugget */
	private Texture nuggetTexture;
	/** Texture asset for nugget attack */
	private Texture nuggetAttackTexture;
	/** Texture asset for the nugget hurt texture*/
	private Texture nuggetHurtTexture;
	/** Texture asset for the buffalo */
	private Texture buffaloTexture;
	/** Texture asset for buffalo hurt texture */
	private Texture buffaloHurtTexture;
	/** Texture asset for buffalo start of charge */
	private Texture buffaloChargeStartTexture;
	/** Texture asset for buffalo charging */
	private Texture buffaloChargingTexture;
	/** Texture asset for the shredded chicken */
	private Texture shreddedTexture;
	/** Texture asset for the hot chick */
	private Texture hotTexture;
	/** Texture asset for the hot chick's attack*/
	private Texture hotAttackTexture;
	/** Texture asset for the dino chicken */
	private Texture dinoTexture;
	/** Texture asset for the dino nugget attack */
	private Texture dinoAttackTexture;
	/** Texture asset for the dino hurt texture */
	private Texture dinoHurtTexture;

	/** Texture asset for empty temp bar */
	private TextureRegion tempEmpty;
	/**Texture asset for yellow temp bar */
	private TextureRegion tempYellow;
	/**Texture asset for orange temp bar */
	private TextureRegion tempOrange;
	/**Texture asset for red temp bar */
	private TextureRegion tempRed;

	/**Texture asset for medium flames */
	private TextureRegion tempMedFlame;
	/**Texture asset for red temp bar */
	private TextureRegion tempLrgFlame;

	/** Health textures*/
	private TextureRegion heartTexture;
	private TextureRegion halfHeartTexture;

	/** Attack buff textures */
	private Texture attOffTexture;
	private Texture attOnTexture;

	/**Slap attack texture strips*/
	private Texture slapSideTexture;
	private Texture slapUpTexture;
	private Texture slapDownTexture;
	/**Chef hurt and idle animation strips */
	private Texture chefHurtTexture;
	private Texture chefIdleTexture;

	/** Egg animation strips */
	private Texture eggSpinTexture;
	private Texture eggSplatTexture;

	///** The current number of chickens */
	//private int chickens;

	/** The amount of time for a game engine step. */
	public static final float WORLD_STEP = 1/60.0f;
	/** Number of velocity iterations for the constrain solvers */
	public static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers */
	public static final int WORLD_POSIT = 2;
	/** Time required until active stove is swapped */
	public static final float STOVE_RESET = 20f;

	/** Exit code for winning the level */
	public static final int EXIT_WIN = 0;
	/** Exit code for losing the level */
	public static final int EXIT_LOSE = 1;
	/** Exit code for pausing the game */
	public static final int EXIT_PAUSE = 2;

	///** Exit code for starting in Easy */
	//public static final int EASY = 0;
	///** Exit code for starting in Medium */
	//public static final int MED = 1;
	///** Exit code for starting in Hard */
	//public static final int HARD = 2;

	/** Width of the game world in Box2d units */
	protected static final float DEFAULT_WIDTH  = 48.0f;
	/** Height of the game world in Box2d units */
	protected static final float DEFAULT_HEIGHT = 27.0f;
	/** The default value of gravity (going down) */
	protected static final float DEFAULT_GRAVITY = 0f;
/** ~~~~~~~~~~~~~~~~~~~~~~~ LEVEL FILE KEYS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	/** Name of center wall in level files */
	protected static final String LEVEL_WALL_CENTER = "wall";
	/** Name of bottom wall in level files */
	protected static final String LEVEL_WALL_BOTTOM = "wall_b";
	/** Name of left wall in level files */
	protected static final String LEVEL_WALL_LEFT = "wall_l";
	/** Name of right wall in level files */
	protected static final String LEVEL_WALL_RIGHT = "wall_r";
	/** Name of top wall in level files */
	protected static final String LEVEL_WALL_TOP = "wall_t";
	/** Name of top2 wall in level files */
	protected static final String LEVEL_WALL_TOP2 = "wall_t_2";
	/** Name of top left corner wall in level files */
	protected static final String LEVEL_WALL_TLC = "wall_tlc";
	/** Name of top right corner wall in level files */
	protected static final String LEVEL_WALL_TRC = "wall_trc";
	/** Name of top left corner wall in level files */
	protected static final String LEVEL_WALL_TLC2 = "wall_tlc_2";
	/** Name of top right corner wall in level files */
	protected static final String LEVEL_WALL_TRC2 = "wall_trc_2";
	/** Name of bottom left corner wall in level files */
	protected static final String LEVEL_WALL_BLC = "wall_blc";
	/** Name of bottom right corner wall in level files */
	protected static final String LEVEL_WALL_BRC = "wall_brc";
	/** Name of yellow wall in level files */
	protected static final String LEVEL_WALL_YELLOW_CENTER = "ywall";
	/** Name of yellow bottom wall in level files */
	protected static final String LEVEL_WALL_YELLOW_BOTTOM = "ywall_b";
	/** Name of ceiling wall in level files */
	protected static final String LEVEL_WALL_CEILING = "ceiling";
	/**Name of ceiling bottom wall in level files */
	protected static final String LEVEL_WALL_CEILING_BOTTOM = "ceiling_b";
	/** Name of spawnpoint in level files */
	protected static final String LEVEL_SPAWN = "spawn";
	/** Name of stove in level files */
	protected static final String LEVEL_STOVE = "stove";
	/** Name of slow trap in level files */
	protected static final String LEVEL_SLOW = "slow";
	/** Name of lure trap in level files */
	protected static final String LEVEL_LURE = "lure";
	/** Name of fire trap in level files */
	protected static final String LEVEL_FIRE = "fire";
	/** Name of chef in level files */
	protected static final String LEVEL_CHEF = "chef";
/** ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
	///** Whether or not the player is cooking, true is they are and false otherwise*/
	//private boolean cooking;

	// Physics objects for the game
	/** Physics constants for initialization */
	private JsonValue constants;
	/** Holds all level file locations for the game */
	private JsonValue levels;
	/** Reference to the character chef */
	private Chef chef;
	/** array of chicken spawn points */
	private List<Spawn> spawnPoints = new ArrayList<Spawn>();
	/** Reference to the temperature*/
	private TemperatureBar temp;

	/** How much the temperature reduces on each slap */
	private float temp_reduction;

	/** Whether or not autoCooking is turned on */
	private boolean autoCook = true;

	/** maps chickens to their corresponding AI controllers*/
	private HashMap<Chicken, AIController> ai = new HashMap<>();
	/** Reference to the active stove object */
	private Stove ActiveStove;
	/** Which stove (index) was the last to be used */
	private int lastStove;
	/** Other possible choices for the stove */
	private List<Integer> nonActiveStoves = new ArrayList<>();
	/** List of all inactive stoves in the level */
	private List<Stove> Stoves = new ArrayList<>();
	/** Timer for the current active stove */
	private float stoveTimer;

	/** The trap the player has currently selected */
	private Trap.type trapTypeSelected = Trap.type.BREAD_LURE;
	/** List of all parameter values {player max health, chicken max health, base damage (player), spawn rate (per update frames), initial spawn}*/
	private int[] parameterList = {3, 100, 2, 100, 2, 6, 30, 10, 5, 5, 5, 5, 0};
	//TODO MAKE CONSTANT


	/** Reference to the game canvas */
	protected GameCanvas canvas;
	/** All the objects in the world. */
	protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
	/** Queue for adding objects */
	protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();
	/** All walls and stoves in the world. */
	protected PooledList<Obstacle> walls = new PooledList<Obstacle>();
	/** All traps in the world. */
	protected PooledList<Obstacle> traps = new PooledList<Obstacle>();
	/** All traps effects in the world. */
	protected PooledList<Obstacle> trapEffects = new PooledList<Obstacle>();
	/** All enemies in the world. */
	protected PooledList<Obstacle> chickens = new PooledList<Obstacle>();
	/** All other objects in the world. */
	protected PooledList<Obstacle> others = new PooledList<Obstacle>();
	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** The Box2D world */
	protected World world;
	/** The boundary of the world */
	protected Rectangle bounds;
	/** The world scale */
	protected Vector2 scale;
	/** The display scale */
	protected Vector2 displayScale;
	/** The grid */
	protected Grid grid;

	/** Whether or not this is an active controller */
	private boolean active;
	/** Whether we have completed this level */
	private boolean complete;
	/** Whether we have failed at this world (and need a reset) */
	private boolean failed;
	/** Whether or not debug mode is active */
	private boolean debug;
	/** Whether or not the grid should be displayed */
	private boolean grid_toggle;

	/** Whether or not mute is toggled */
	private boolean muted = false;

	/** Whether or not pause is toggled */
	private boolean paused = false;

	/** Whether or not the cooldown effect is enabled */
	private boolean cooldown;

	/** Mark set to handle more sophisticated collision callbacks */
	protected ObjectSet<Fixture> sensorFixtures;

	/**The collision controller for this game*/
	private CollisionController collisionController;

	/** The Sound controller reference */
	private SoundController sound;

	/** How much time has passed in the game */
	private float gameTime;

	/** Whether the cursor was in a cached state right before this menu was set as screen */
	private boolean cached;

	/**Wave-related variables, to be changed to take in input via level editor later */
	private float[] probs; // probability spread for each chicken
	private int startWaveSize; // Starting wave size, increases by incWaveSize each wave
	private int maxWaveSize; // Maximum size of a wave
	private float spreadability; // how much time between each spawn of the wave (in seconds)
	private float replenishTime; // how long before the wave is replenished (in seconds)
	private int enemiesLeft; // how many enemies left in the wave
	private float waveStartTime; // when did this wave start
	private float lastEnemySpawnTime; // when did the last enemy spawn
	// the total pool of enemies for this level
	private ArrayList<Integer> enemyPool = new ArrayList<>(); // the enemies in the pool but not on the board
	private ArrayList<Integer> enemyBoard = new ArrayList<>(); // the enemies on the board


	/**
	 * Returns true if debug mode is active.
	 *
	 * If true, all objects will display their game bodies.
	 *
	 * @return true if debug mode is active.
	 */
	public boolean isDebug( ) {
		return debug;
	}

	/**
	 * Sets whether debug mode is active.
	 *
	 * If true, all objects will display their game bodies.
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
	public GameController() {
		this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT), new Vector2(0,DEFAULT_GRAVITY));
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
		sensorFixtures = new ObjectSet<Fixture>();
		collisionController = new CollisionController(scale, displayScale);
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
	protected GameController(Rectangle bounds, Vector2 gravity){
		world = new World(gravity,false);
		this.bounds = new Rectangle(bounds);
		this.scale = new Vector2(1,1);
		this.displayScale = new Vector2(1,1);
		complete = false;
		failed = false;
		debug  = false;
		active = false;
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
		//textures
			//environment
		background = new TextureRegion(directory.getEntry("enviro:background",Texture.class));
		wallCenterTile = directory.getEntry("enviro:wall:center", Texture.class);
		wallLeftTile = directory.getEntry("enviro:wall:left", Texture.class);
		wallRightTile = directory.getEntry( "enviro:wall:right", Texture.class);
		wallBottomTile = directory.getEntry( "enviro:wall:bottom", Texture.class);
		wallYellowCenterTile = directory.getEntry( "enviro:wall:yellow:center", Texture.class);
		wallYellowBottomTile = directory.getEntry( "enviro:wall:yellow:bottom", Texture.class);
		wallTopTile = directory.getEntry( "enviro:wall:top", Texture.class);
		wallTopLeftCornerTile =directory.getEntry("enviro:wall:topleftcorner", Texture.class);
		wallTopRightCornerTile = directory.getEntry("enviro:wall:toprightcorner", Texture.class);
		//TODO: change top corners back
		wallTop2Tile = directory.getEntry( "enviro:wall:top", Texture.class );
		wallTopLeftCorner2Tile = directory.getEntry("enviro:wall:topleftcorner", Texture.class);
		wallTopRightCorner2Tile = directory.getEntry("enviro:wall:toprightcorner", Texture.class);
		wallBottomLeftCorner = directory.getEntry("enviro:wall:bottomleftcorner", Texture.class);
		wallBottomRightCorner = directory.getEntry("enviro:wall:bottomrightcorner", Texture.class);
		wallCeilingTile = directory.getEntry("enviro:wall:ceiling", Texture.class);
		wallCeilingBottomTile = directory.getEntry("enviro:wall:ceiling:bottom", Texture.class);
		stoveTexture = directory.getEntry("enviro:stove",Texture.class);
		stoveActiveTexture = directory.getEntry("enviro:stoveActive", Texture.class);
		stoveInactiveTexture = directory.getEntry("enviro:stoveInactive", Texture.class);
			//traps
		trapDefaultTexture = directory.getEntry("enviro:trap:spike",Texture.class);
		trapCoolerTexture = directory.getEntry("enviro:trap:cooler",Texture.class);
		trapCoolerActivate = directory.getEntry("enviro:trap:coolerActivate", Texture.class);
		trapToasterTexture = directory.getEntry("enviro:trap:toaster",Texture.class);
		trapToasterActivate = directory.getEntry("enviro:trap:toasterActivate", Texture.class);

		trapHotSauceTexture = directory.getEntry("enviro:trap:hotSauce", Texture.class);
		trapHotSauceActivate = directory.getEntry("enviro:trap:hotSauceActivate", Texture.class);
		spawnTexture = directory.getEntry("enviro:spawn", Texture.class);
		indicatorTexture = new TextureRegion(directory.getEntry("enviro:indicator", Texture.class));
			//characters
		bulletTexture = directory.getEntry("char:bullet",Texture.class);
		chickenTexture  = new TextureRegion(directory.getEntry("char:chicken",Texture.class));
		enemyHealthBarTexture = new TextureRegion(directory.getEntry("char:nuggetBar", Texture.class));
		chefTexture = directory.getEntry("char:chef", Texture.class);
		nuggetTexture = directory.getEntry("char:nugget", Texture.class);
		nuggetHurtTexture = directory.getEntry("char:nuggetHurt", Texture.class);
		nuggetAttackTexture = directory.getEntry("char:nuggetAttack", Texture.class);
		buffaloTexture = directory.getEntry("char:buffalo",Texture.class);
		buffaloHurtTexture = directory.getEntry("char:buffaloHurt", Texture.class);
		buffaloChargeStartTexture = directory.getEntry("char:buffaloStart", Texture.class);
		buffaloChargingTexture = directory.getEntry("char:buffaloCharge", Texture.class);
		shreddedTexture = directory.getEntry("char:shredded",Texture.class);
		hotTexture = directory.getEntry("char:hot", Texture.class);
		hotAttackTexture = directory.getEntry("char:hotAttack", Texture.class);
		dinoTexture = directory.getEntry("char:dino", Texture.class);
		dinoAttackTexture = directory.getEntry("char:dinoAttack", Texture.class);
		dinoHurtTexture = directory.getEntry("char:dinoHurt", Texture.class);
		slapSideTexture = directory.getEntry("char:slapSide", Texture.class);
		slapDownTexture = directory.getEntry("char:slapDown", Texture.class);
		slapUpTexture = directory.getEntry("char:slapUp", Texture.class);
		chefHurtTexture = directory.getEntry("char:chefHurt", Texture.class);
		chefIdleTexture = directory.getEntry("char:chefIdle", Texture.class);
		eggSpinTexture = directory.getEntry("char:eggSpin", Texture.class);
		eggSplatTexture = directory.getEntry("char:eggSplat", Texture.class);

		//ui
		tempEmpty = directory.getEntry("ui:tempBar.empty", TextureRegion.class);
		tempYellow = directory.getEntry("ui:tempBar.yellow", TextureRegion.class);
		tempOrange = directory.getEntry("ui:tempBar.orange", TextureRegion.class);
		tempRed = directory.getEntry("ui:tempBar.red", TextureRegion.class);
		tempMedFlame = directory.getEntry("ui:tempBarMedFlame.flame", TextureRegion.class);
		tempLrgFlame = directory.getEntry("ui:tempBarLargeFlame.flame", TextureRegion.class);
		heartTexture = directory.getEntry("ui:healthUnit.full", TextureRegion.class);
		halfHeartTexture = directory.getEntry("ui:healthUnit.half", TextureRegion.class);
		attOffTexture = directory.getEntry("ui:att_off", Texture.class);
		attOnTexture = directory.getEntry("ui:att_on", Texture.class);

		//fonts
		displayFont = directory.getEntry( "font:retro" ,BitmapFont.class);

		//constants
		constants = directory.getEntry( "constants", JsonValue.class );
		levels = directory.getEntry("levels", JsonValue.class );
		//set assets
		collisionController.setConstants(constants);
		collisionController.gatherAssets(directory);
	}

	public void setSoundController(SoundController sound) {
		this.sound = sound;
		collisionController.setSound(sound);
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
		walls.clear();
		trapEffects.clear();
		traps.clear();
		others.clear();
		chickens.clear();
		ActiveStove = null;
		lastStove = 0;
		Stoves.clear();
		nonActiveStoves.clear();
		world.dispose();
		enemyPool.clear();
		enemyBoard.clear();
		spawnPoints.clear();
		
		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
	}
	public void initEasy(){
		parameterList = new int []{5, 100, 3, 100, 2, 6, 30, 10, 5, 5, 3, 5, 0};
		cooldown = false;
	}

	public void initMed(){
		parameterList = new int []{4, 100, 2, 100, 3, 6, 30, 10, 5, 5, 4, 5, 0};
		cooldown = false;
	}

	public void initHard(){
		parameterList = new int []{3, 100, 2, 100, 4, 6, 30, 10, 5, 5, 5, 5, 0};
		cooldown = true;
	}

	/**
	 * Lays out the game geography.
	 */
	public void populateLevel(JsonValue level) {
		//TODO: Populate level similar to our board designs, and also change the win condition (may require work outside this method)\
		reset();
		//world.setGravity( new Vector2(0,0) );
		temp = new TemperatureBar(tempEmpty, tempYellow, tempOrange, tempRed, tempMedFlame, tempLrgFlame, level.get("temp").asInt());
		temp_reduction = level.get("temp_reduction").asFloat();
		temp.setUseCooldown(cooldown);

		doNewPopulate(level);
		//add chef here!
		addObject(chef, GameObject.ObjectType.NULL);
		//set the chef in the collision controller now that it exists
		collisionController.setChef(chef);
		for (int i = 0; i < parameterList[4]-2; i++){
			spawnChicken(Chicken.ChickenType.Nugget);
		}

		// Get initial values for parameters in the list
		//parameterList[0] = chef.getMaxHealth();


	}

	private void doNewPopulate(JsonValue level){
		initGrid();
		grid.clearObstacles();
		String[] stuff = level.get("items").asStringArray();
		JsonValue defaults = constants.get("defaults");
		probs = level.get("spawn_probs").asFloatArray();
		startWaveSize = level.get("starting_wave_size").asInt();
		maxWaveSize = level.get("max_wave_size").asInt();
		spreadability = level.get("spawn_gap").asFloat();
		replenishTime = level.get("wave_gap").asFloat();

		gameTime = 0;
		waveStartTime = gameTime;
		lastEnemySpawnTime = gameTime;
		stoveTimer = gameTime;
		enemiesLeft = startWaveSize;
		enemyPool = new ArrayList<>();
		enemyBoard = new ArrayList<>();
		// sets up the initial enemy pool
		for (int i = 0; i < maxWaveSize; i++){
			double r = Math.random();
			float sum = probs[0];
			int k = 1;
			while (k < probs.length && r > sum){
				sum += probs[k];
				k++;
			}
			enemyPool.add(k-1);
		}

		//0x0001 = player, 0x0002 = chickens, 0x0004 walls, 0x0008 chicken basic attack,
		// 0x0010 buffalo's headbutt, 0x0020 spawn

		//The filter for all obstacles
		Filter obstacle_filter = new Filter();
		obstacle_filter.categoryBits = 0x0004;
		obstacle_filter.maskBits = 0x0001 | 0x0002 | 0x0004 | 0x0010 | 0x0080;

		Filter player_filter = new Filter();
		player_filter.categoryBits = 0x0001;
		player_filter.maskBits = 0x0004 | 0x0008;

		Filter chicken_filter = new Filter();
		chicken_filter.categoryBits = 0x0002;
		chicken_filter.maskBits = 0x0004;

		Filter spawn_filter = new Filter();
		spawn_filter.groupIndex = -1;

		for(int ii = 0; ii < stuff.length; ii++){
			int x = ii % grid.getColCount();
			int y = (stuff.length - 1 - ii) / grid.getColCount();
			switch(stuff[ii]){
				case LEVEL_WALL_CENTER:
					//add center wall
					createWall(defaults, wallCenterTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_TOP:
					//add top wall
					createWall(defaults, wallTopTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_TOP2:
					//add top wall
					createWall(defaults, wallTop2Tile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_BOTTOM:
					//add bottom wall
					createWall(defaults, wallBottomTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_LEFT:
					//add left wall
					createWall(defaults, wallLeftTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_RIGHT:
					//add right wall
					createWall(defaults, wallRightTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_TLC:
					//add top left corner
					createWall(defaults, wallTopLeftCornerTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_TRC:
					//add top right corner
					createWall(defaults, wallTopRightCornerTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_TLC2:
					//add top left corner
					createWall(defaults, wallTopLeftCorner2Tile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_TRC2:
					//add top right corner
					createWall(defaults, wallTopRightCorner2Tile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_BLC:
					//add bottom left corner
					createWall(defaults, wallBottomLeftCorner, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_BRC:
					//add bottom right corner
					createWall(defaults, wallBottomRightCorner, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_YELLOW_CENTER:
					//add wall
					createWall(defaults, wallYellowCenterTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_YELLOW_BOTTOM:
					//add wall
					createWall(defaults, wallYellowBottomTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_CEILING:
					//add ceiling
					createWall(defaults, wallCeilingTile, obstacle_filter, x, y);
					break;
				case LEVEL_WALL_CEILING_BOTTOM:
					//add ceiling
					createWall(defaults, wallCeilingBottomTile, obstacle_filter, x, y);
					break;
				case LEVEL_STOVE:
					// Add stove
					float swidth = stoveTexture.getWidth()/scale.x*displayScale.x;
					float sheight = stoveTexture.getHeight()/scale.y*displayScale.y;
					Stove stove = new Stove(constants.get(LEVEL_STOVE),x,y,swidth,sheight);
					stove.setDrawScale(scale);
					stove.setDisplayScale(displayScale);
					stove.setTexture(stoveTexture);
					stove.setActiveTexture(stoveActiveTexture);
					stove.setInactiveTexture(stoveInactiveTexture);
					stove.setFilterData(obstacle_filter);
					addObject(stove, GameObject.ObjectType.WALL);
					grid.setObstacle(x,y);
					grid.setObstacle(x-1,y);
					grid.setObstacle(x,y-1);
					grid.setObstacle(x-1,y-1);
					Stoves.add(stove);
					if (ActiveStove == null){
						ActiveStove = stove;
						lastStove = 0;
						ActiveStove.setActive();
					} else if (nonActiveStoves.size() < Stoves.size() - 1){
						nonActiveStoves.add(nonActiveStoves.size() + 1);
					}
					break;
				case LEVEL_CHEF:
					// Create chef
					//TODO: FIX AFTER WE HAVE FILMSTRIP!
					float cwidth  = 16/scale.x*displayScale.x;
					float cheight = 32/scale.y*displayScale.y;
					chef = new Chef(constants.get(LEVEL_CHEF), x, y, cwidth, cheight);
					chef.setDrawScale(scale);
					chef.setDisplayScale(displayScale);
					chef.setTextures(chefTexture, heartTexture, halfHeartTexture, attOffTexture, attOnTexture);
					chef.setSlapSideTexture(slapSideTexture);
					chef.setSlapUpTexture(slapUpTexture);
					chef.setHurtTexture(chefHurtTexture);
					chef.setSlapDownTexture(slapDownTexture);
					chef.setIdleTexture(chefIdleTexture);
					chef.setFilterData(player_filter);

					//don't add chef here! add it later so its on top easier
					break;
				case LEVEL_SPAWN:
					Spawn spawn = new Spawn(x, y, 1, 1);
					spawn.setSensor(true);
					spawn.setDrawScale(scale);
					spawn.setDisplayScale(displayScale);
					spawn.setTexture(spawnTexture);
					spawn.setName(LEVEL_SPAWN);
					spawn.setFilterData(spawn_filter);
					addObject(spawn, GameObject.ObjectType.WALL);
					spawnPoints.add(spawn);
					break;
				case LEVEL_SLOW:
					trapHelper(x, y, Trap.type.COOLER);
					break;
				case LEVEL_LURE:
					trapHelper(x, y, Trap.type.TOASTER);
					break;
				case LEVEL_FIRE:
					trapHelper(x, y, Trap.type.HOT_SAUCE);
					break;
			}
		}

	}

	/**
	 * Activates or deactivates the input processor; called when screen is shown or hidden
	 * @param b     whether or not to activate or deactivate the input processor
	 * */
	private void activateInputProcessor(boolean b){
		if(b){
			Gdx.input.setInputProcessor(this);
			cached = Gdx.input.isCursorCatched();
			Gdx.input.setCursorCatched(false);
		}
		else {
			Gdx.input.setCursorCatched(cached);
			Gdx.input.setInputProcessor(null);
		}
	}

	private void createWall(JsonValue defaults, Texture texture, Filter obstacle_filter, float x, float y){
		BoxObstacle obj = new BoxObstacle(x+.5f,y+.5f,1,1);//displayScale.x,displayScale.y);
		// PolygonObstacle obj = new PolygonObstacle(new float[]{x, y, x+1, y, x+1, y+1, x, y+1});
		obj.setBodyType(BodyDef.BodyType.StaticBody);
		obj.setDensity(defaults.getFloat( "density", 0.0f ));
		obj.setFriction(defaults.getFloat( "friction", 0.0f ));
		obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
		obj.setDrawScale(scale);
		obj.setDisplayScale(displayScale);
		obj.setTexture(texture);
		obj.setName(LEVEL_WALL_CENTER);//+ii); If we need to specify name further, its here
		obj.setFilterData(obstacle_filter);
		addObject(obj, GameObject.ObjectType.WALL);
		grid.setObstacle(x,y);
	}

	/*******************************************************************************************
	 * COLLISIONS
	 ******************************************************************************************/
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}
	/** Unused ContactListener method */
	public void preSolve(Contact contact, Manifold oldManifold) {}

	/** Set the value of autoCooking
	 * @param val 		boolean value to set autoCooking
	 */
	public void setAutoCook(boolean val){
		autoCook = val;
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
	public void beginContact(Contact contact){
		collisionController.beginContact(contact, damageCalc());
	}

		//TODO: Detect if a collision is with an enemy and have an appropriate interaction

	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  The main use of this method
	 * is to determine when the characer is NOT on the ground.  This is how we prevent
	 * double jumping.
	 */
	public void endContact(Contact contact) {
		//TODO: Detect if collision is with an enemy and give appropriate interaction (if any needed)
		collisionController.endContact(contact, sensorFixtures);
	}
	/*******************************************************************************************
	 * UPDATING LOGIC
	 ******************************************************************************************/
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

		//set failure if chef's health is 0
		if (!isFailure() && !chef.isAlive()) {
			setFailure(true);
			return false;
		}

		if (temp.isCooked()){
			setComplete(true);
			return false;
		}

		for (Chicken chick: ai.keySet()){
			// Remove ai controller for dead chicken
			if (chick.isRemoved() && ai.containsKey(chick)){
				ai.get(chick).die();
				//ai.remove(chick); // This causes a concurrency error :(
			}
			// Checking for attack here fixes bug where shredded/buffalo would keep chasing even if chef is in attack range
			if (!chick.isAttacking() && chef.getPosition().dst(chick.getPosition()) < chick.getAttackRange()){
				chick.startAttack();
				chick.setTouching(true);
			}
			// Check if chicken is in lure range
			if(chick.isActive()){
				ai.get(chick).update(dt);
				for (Obstacle ob: trapEffects){
					Trap tr = (Trap)ob;
					if (tr.getTrapType().equals(Trap.type.BREAD_LURE) && tr.getPosition().dst(chick.getPosition()) < 6f){
						chick.trapTarget(tr);
					}

					if ((chick.getTrap() == null || chick.getTrap().isRemoved()) && chick.isLured()){
						chick.resetTarget();
					}
				}
			}
		}
		return !paused;
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
		// Toggle grid
		if (input.didGridToggle()) {
			grid_toggle = !grid_toggle;
		}


		if(input.didAdvance()) {
			chef.decrementHealth();
		}
		if(input.didRetreat()) {
			killChickens();
		}

		// Now it is time to maybe switch screens.
		if (input.didESC()){
			pause();
			paused = true;
			listener.exitScreen(this, EXIT_PAUSE);
		} else if (failed) {
			pause();
			listener.exitScreen(this, EXIT_LOSE);
			return false;
		} else if (complete) {
			pause();
			listener.exitScreen(this, EXIT_WIN);
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

		// Music
		sound.playMusic(SoundController.CurrentScreen.LEVEL, dt);




		// Process actions in object model
		float movement = InputController.getInstance().getHorizontal() * chef.getMaxspeed();
		chef.setMovement(movement);
		chef.setVertMovement(InputController.getInstance().getVertical()* chef.getMaxspeed());

		if (!(chef.isCooking() && (autoCook || InputController.getInstance().didCook()))) {
			chef.setShooting(InputController.getInstance().didSecondary(), InputController.getInstance().getSlapDirection());
		}
		gameTime += dt;

		if(InputController.getInstance().didMute()){
			muted = !muted;
		}
		
		// Add a bullet if we fire
		if (chef.isShooting()) {
			createSlap(InputController.getInstance().getSlapDirection());
		}

		// Add a trap if trying to press
		if (chef.isTrapping()) {
			createTrap();
		}

		// Stove updating mechanics
		if (Stoves.size() > 1 && gameTime > stoveTimer + STOVE_RESET) {
			stoveTimer = gameTime;
			ActiveStove.setInactive();
			int ind = nonActiveStoves.remove(MathUtils.floor(MathUtils.random(0, nonActiveStoves.size() - 1)));
			nonActiveStoves.add(lastStove);
			ActiveStove = Stoves.get(ind);
			lastStove = ind;
			ActiveStove.setActive();
		}

		// Wave spawning logic

		if (gameTime > waveStartTime + replenishTime){
			waveStartTime = gameTime;
			enemiesLeft = Math.min(maxWaveSize, startWaveSize + 1);
			startWaveSize = Math.min(maxWaveSize, startWaveSize + 1);
		}

		if (gameTime > lastEnemySpawnTime + spreadability && enemiesLeft > 0 && enemyPool.size() > 0){
			int r = (int)Math.floor((maxWaveSize - enemyBoard.size())*Math.random());
			enemyBoard.add(enemyPool.get(r));
			int chicken = enemyPool.remove(r);
			if (chicken == 0){
				spawnChicken(Chicken.ChickenType.Nugget);
			} else if (chicken == 1){
				spawnChicken(Chicken.ChickenType.Buffalo);
			} else if (chicken == 2) {
				spawnChicken(Chicken.ChickenType.Shredded);
			} else if (chicken == 3) {
				spawnChicken(Chicken.ChickenType.DinoNugget);
			} else if (chicken == 4) {
				spawnChicken(Chicken.ChickenType.Hot);
			}
			lastEnemySpawnTime = gameTime;
			enemiesLeft -= 1;
		}


		for (Obstacle obj : objects) {
			//Remove a bullet if slap is complete
			if (obj.isBullet() && (obj.getAngle() > Math.PI/8 || obj.getAngle() < Math.PI/8*-1)) {
				removeBullet(obj);
			}
			if (obj.getName().equals("chicken")){
				Chicken chicken = ((Chicken) obj);
				if (chicken.makeAttack()) {
					switch(chicken.getAttackType()) {
						case Basic:
						case Charge:
						case Projectile:
							createChickenAttack(chicken, chicken.getAttackType());
							break;
						case Knockback:
							createKnockbackAttack((ShreddedChicken)chicken);
						case Explosion:
							break;
					}
				}
			} else if (obj.getName().equals("chickenAttack")) {
				ChickenAttack attack = (ChickenAttack)obj;
				if (!attack.isRemoved()) {
					if (attack.atDestination(dt)) {
						attack.beginSplat();
					}
					if (attack.readyToRemove()) {
						attack.markRemoved(true);
					}
				}
			}
		}

		chef.applyForce();

		// if the chef tries to perform an action, move or gets hit, stop cooking
//		if ((InputController.getInstance().isMovementPressed()|| InputController.getInstance().didSecondary()
//		|| chef.isStunned())){
//			chef.setCooking(false, null);
//		}
//		else{
//			chef.setCooking(chef.inCookingRange(), null);
//		}

		chef.setCooking(chef.inCookingRange(), null);

		//update temperature and stove draw type
		Stove stove = chef.getStove();
		if (chef.isCooking() && (autoCook || InputController.getInstance().didCook())) {
			temp.cook(true);
			if (stove!=null) chef.getStove().setLit(true);
		}else {
			temp.cook(false);
			if (stove!=null) chef.getStove().setLit(false);
		}

		temp.update(dt);
	}

	/*******************************************************************************************
	 * UPDATE HELPERS
	 ******************************************************************************************/
	/**
	 * Removes the given chicken from the world, then decrements the number of chickens
	 * @param chicken	 the chicken to remove
	 */
	private void removeChicken(Obstacle chicken){
		if(!chicken.isRemoved()) {
			chicken.markRemoved(true);
			ai.get(chicken).die();
			ai.remove(chicken);
		}
	}

	/**
	 * Kills all chickens in the world
	 */
	public void killChickens(){
		for (Obstacle obstacle: objects){
			if (obstacle.getName().equals("chicken")){
				removeChicken(obstacle);
			}
		}
	}
	/**
	 * Spawn a chicken somewhere in the world, then increments the number of chickens
	 */
	private void spawnChicken(Chicken.ChickenType type){
		float dwidth  = chickenTexture.getRegionWidth()/scale.x*displayScale.x;
		float dheight = chickenTexture.getRegionHeight()/scale.y*displayScale.y;
		int index = (int) (Math.random() * spawnPoints.size());
		Spawn spawn = spawnPoints.get(index);
		//float x = ((float)Math.random() * (spawn_xmax - spawn_xmin) + spawn_xmin);
		//float y = ((float)Math.random() * (spawn_ymax - spawn_ymin) + spawn_ymin);
		float rand = (float)Math.random();
		float x = spawn.getX();
		float y = spawn.getY();
		// Spawn chicken at the border of the world
		/*if (rand < 0.25){
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
		}*/

		Chicken enemy;
		Chicken enemy2;
/*			if (type == Chicken.Type.Nugget) {
			enemy = new NuggetChicken(constants.get("chicken"), constants.get("nugget"), x, y, dwidth, dheight, chef, parameterList[1]);
		} else {
			enemy = new NuggetChicken(constants.get("chicken"), constants.get("nugget"), x, y, dwidth, dheight, chef, parameterList[1]);
		}*/
		if (type == Chicken.ChickenType.Nugget) {
			enemy = new NuggetChicken(constants.get("chicken"), constants.get("nugget"), x, y, dwidth, dheight, chef, parameterList[1]);
			enemy.setTexture(nuggetTexture);
			enemy.setAttackTexture(nuggetAttackTexture);
			enemy.setHurtTexture(nuggetHurtTexture);
		} else if (type == Chicken.ChickenType.Shredded){
			enemy = new ShreddedChicken(constants.get("chicken"), constants.get("shredded"), x, y, dwidth, dheight, chef, parameterList[1]);
			enemy.setTexture(shreddedTexture);
		} else if (type == Chicken.ChickenType.Hot){
			enemy = new HotChicken(constants.get("chicken"), constants.get("hot"), x, y, dwidth, dheight, chef, parameterList[1]);
			enemy.setTexture(hotTexture);
			enemy.setAttackTexture(hotAttackTexture);
		}
		else if (type == Chicken.ChickenType.Buffalo){
			enemy = new BuffaloChicken(constants.get("chicken"), constants.get("buffalo"), x, y, dwidth, dheight, chef, parameterList[1]);
			enemy.setTexture(buffaloTexture);
			enemy.setHurtTexture(buffaloHurtTexture);
			((BuffaloChicken) enemy).setChargeStartTexture(buffaloChargeStartTexture);
			((BuffaloChicken) enemy).setChargingTexture(buffaloChargingTexture);
		} else if (type == Chicken.ChickenType.DinoNugget){
			enemy = new DinoChicken(constants.get("chicken"), constants.get("dino"), x, y, dwidth, dheight, chef, parameterList[1]);
			enemy.setTexture(dinoTexture);
			enemy.setAttackTexture(dinoAttackTexture);
			enemy.setHurtTexture(dinoHurtTexture);
		} else { // Should not reach this state
			assert false;
			enemy = new NuggetChicken(constants.get("chicken"), constants.get("nugget"), x, y, dwidth, dheight, chef, parameterList[1]);
		}
		enemy.setDrawScale(scale);
		enemy.setDisplayScale(displayScale);
		enemy.setBarTexture(enemyHealthBarTexture);
		addObject(enemy, GameObject.ObjectType.CHICKEN);
		ai.put(enemy, new AIController(enemy, chef, grid));
	}

	/**
	 * Add a new bullet to the world and send it in the right direction.
	 *
	 */
	private void createSlap(int direction) {
		//TODO: Slap needs to go through multiple enemies, specific arc still needs to be tweaked, probably best if in-game changing of variables is added
		if (temp.getTemperature() > 0){
			temp.reduceTemp(temp_reduction);
		}
		/*
		float radius = 8*bulletTexture.getRegionWidth() / (2.0f * scale.x);
		float offset = 1f;
		float angvel = 6f;
		float ofratio = 0.7f;
		BoxObstacle slap;
		if (direction == 2 || direction == 4) {
			slap = new BoxObstacle(chef.getX(), chef.getY(), radius, 0.1f);
			slap.setSensor(true);
			offset *= (direction == 2 ? 1 : -1);
			slap.setX(chef.getX() + offset);
			slap.setY(chef.getY() - offset*ofratio);
			slap.setAngle((float)(-1*Math.PI/24));
			slap.setAngularVelocity(angvel);
		} else {
			slap = new BoxObstacle(chef.getX(), chef.getY(), 0.1f, radius);
			slap.setSensor(true);
			offset *= (direction == 1 ? 1 : -1);
			slap.setY(chef.getY() + offset);
			slap.setX(chef.getX() - offset*ofratio);
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
		*/

		float radius = 8*bulletTexture.getWidth() / (2.0f * scale.x)*displayScale.y;
		Slap slap;
		if(direction == 2 || direction == 4) {
			slap = new Slap(constants.get("slap"), chef.getX(), chef.getY(), radius, 0.1f, direction);
		}else {
			slap = new Slap(constants.get("slap"), chef.getX(), chef.getY(), 0.1f, radius, direction);
		}
		slap.setTexture(bulletTexture);
		slap.setDrawScale(scale);
		slap.setDisplayScale(displayScale);
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
		sound.playEmptySlap();

	}

	private void createKnockbackAttack(ShreddedChicken shredded){

		ChickenAttack attack = new ChickenAttack(shredded.getX()-ChickenAttack.getKNOCKWIDTH()/2.0f*MathUtils.cos(shredded.getAttackAngle()),
				shredded.getY()-ChickenAttack.getKNOCKHEIGHT()/2.0f*MathUtils.sin(shredded.getAttackAngle()),
				ChickenAttack.getKNOCKWIDTH(), ChickenAttack.getKNOCKHEIGHT(), chef, shredded, ChickenAttack.AttackType.Knockback);
		shredded.setChickenAttack(attack);
		attack.setAngle(shredded.getAttackAngle());
		attack.setDrawScale(scale);
		attack.setDisplayScale(displayScale);
		addQueuedObject(attack);
		sound.playShredAttack();
	}

	/** Adds a chickenAttack to the world */
	private void createChickenAttack(Chicken chicken, ChickenAttack.AttackType type) {
		ChickenAttack attack = new ChickenAttack(chicken.getX(), chicken.getY(), ChickenAttack.getWIDTH(),
				ChickenAttack.getHEIGHT(), chef, chicken, type);
		if (type == ChickenAttack.AttackType.Charge && grid.isObstacleAt(attack.getX(), attack.getY())) {
			attack.markRemoved(true);
			chicken.forceStopAttack();

		} else {
			chicken.setChickenAttack(attack);
			attack.setDrawScale(scale);
			attack.setDisplayScale(displayScale);
			addQueuedObject(attack);
			switch (type) {
				case Basic:
					sound.playNugAttack();
					break;
				case Projectile:
					attack.setEggAnimators(eggSpinTexture, eggSplatTexture);
					sound.playShredAttack();
					break;
				case Charge:
					if (chicken.getType() == Chicken.ChickenType.Buffalo) {
						sound.playBuffAttack();
					}
					break;

			}
		}
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
		trapHelper(chef.getX(), chef.getY(), trapTypeSelected);

	}

	public void trapHelper(float x, float y, Trap.type t){
		Texture trapTexture = trapDefaultTexture;
		FilmStrip anim = null;
		switch (t){
			case COOLER:
				trapTexture = trapCoolerTexture;
				anim = new FilmStrip(trapCoolerActivate, 1, COOLER_NUM_FRAMES);
				break;
			case TOASTER:
				trapTexture = trapToasterTexture;
				anim = new FilmStrip(trapToasterActivate, 1, TOASTER_NUM_FRAMES);
				break;
			case HOT_SAUCE:
				trapTexture = trapHotSauceTexture;
				anim = new FilmStrip(trapHotSauceActivate, 1, HOTSAUCE_NUM_FRAMES);
				break;
		}
		float twidth = trapTexture.getWidth()/scale.x*displayScale.x;
		float theight = trapTexture.getHeight()/scale.y*displayScale.y;
		Trap trap = new Trap(constants.get("trap"), x, y, twidth, theight, t);
		if(anim != null){
			trap.setAnimation(anim);
		}
		trap.setDrawScale(scale);
		trap.setDisplayScale(displayScale);
		trap.setTexture(trapTexture);
		trap.setIndicatorTexture(indicatorTexture);
		addObject(trap, GameObject.ObjectType.TRAP);
	}



	public float damageCalc(){
		return (temp.getTemperature() <= 0) ? 0 : chef.getDamage() + 2 * chef.getDamage()*temp.getPercentCooked();
	}


	/**
	 *
	 * Adds a game object in to the insertion queue.
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
	 * Immediately adds the object to the game world
	 *
	 * param obj The object to add
	 */
	protected void addObject(Obstacle obj, GameObject.ObjectType type) {
		assert inBounds(obj) : "Object is not in bounds";
		objects.add(obj);
		obj.activatePhysics(world);
		//for drawing priorities
		switch (type){
			case WALL:
				walls.add(obj);
				break;
			case TRAP_EFFECT:
				trapEffects.add(obj);
				break;
			case TRAP:
				traps.add(obj);
				break;
			case CHICKEN:
				chickens.add(obj);
				break;
			case NULL:
				others.add(obj);
		}

	}

	/**
	 * Returns true if the object is in bounds.
	 *
	 * This assertion is useful for debugging the game.
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

	/**
	 * Called when the Screen is paused.
	 *
	 * We need this method to stop all sounds when we pause.
	 * Pausing happens when we switch game modes.
	 */
	public void pause() {
		//stop all sounds
	}

	/**
	 * Processes game
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * game.  The primary method is the step() method in world.  This implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		// Add any objects created by actions
		//TODO: look about moving these while loops below world.step()
		while(!collisionController.getNewTraps().isEmpty()){
			addObject(collisionController.getNewTraps().poll(), GameObject.ObjectType.TRAP_EFFECT);
		}
		//TODO: make sure this is only used for slaps
		while (!addQueue.isEmpty()) {
			addObject(addQueue.poll(), GameObject.ObjectType.NULL);
		}

		// Turn the game engine crank.
		world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		iterateThrough(walls.entryIterator(), dt);
		iterateThrough(trapEffects.entryIterator(),dt);
		iterateThrough(traps.entryIterator(),dt);
		iterateThrough(chickens.entryIterator(),dt);
		iterateThrough(others.entryIterator(),dt);
	}

	private void iterateThrough(Iterator<PooledList<Obstacle>.Entry> iterator, float dt){
		while (iterator.hasNext()) {
			PooledList<Obstacle>.Entry entry = iterator.next();
			Obstacle obj = entry.getValue();
			if (obj.isRemoved()) {
				obj.deactivatePhysics(world);
				entry.remove();
			} else {
				// Note that update is called last!
				obj.update(dt);
			}
		}
	}

	/**
	 * Draw the game objects to the canvas
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
		canvas.begin();
		canvas.draw(background,Color.WHITE,0,0,background.getRegionWidth()*displayScale.x,background.getRegionHeight()*displayScale.y);

		//priority: Walls < trap effects < traps < chickens < other < chef

		for(Obstacle wall : walls){
			wall.draw(canvas);
		}

//		for (Obstacle spawn : spawnPoints){
//			spawn.draw(canvas);
//		}

		for (Obstacle trapE : trapEffects){
			trapE.draw(canvas);
		}
		for (Obstacle trap : traps){
			trap.draw(canvas);
		}
		for (Obstacle c : chickens){
			c.draw(canvas);
		}
		for (Obstacle other : others){
			other.draw(canvas);
		}

		//draw chef last
		chef.draw(canvas);

		canvas.end();

		if (debug) {
			canvas.beginDebug();
			for(Obstacle obj : walls){
				obj.drawDebug(canvas);
			}
			for (Obstacle trapE : trapEffects){
				trapE.drawDebug(canvas);
			}
			for (Obstacle trap : traps){
				trap.drawDebug(canvas);
			}
			for (Obstacle c : chickens){
				c.drawDebug(canvas);
			}
			for (Obstacle other : others){
				other.drawDebug(canvas);
			}
			if (grid_toggle) {
				grid.drawDebug(canvas);
			}
			canvas.endDebug();
		}

		//TODO add section for UI
		//draw temp bar
		canvas.begin();

		temp.draw(canvas);

		//draw gametime
//		canvas.drawText("Time: " + (double) Math.round(gameTime * 10) / 10, new BitmapFont(), 1000, 700);
		canvas.end();

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
		return playSound( sound, soundId, 0);
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
		return 0;
	}

	/**
	 * Returns true if the level is completed.
	 *
	 * @return true if the level is completed.
	 */
	public boolean isComplete() {
		return complete;
	}

	/**
	 * Sets whether the level is completed.
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
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
		float y = canvas.getHeight();
		float x = canvas.getWidth();
		//canvas.setSize(1920, 1080);
		//canvas.resize();
		//canvas.resetCamera();
		this.scale.x = x/bounds.getWidth();
		this.scale.y = y/bounds.getHeight();
		//everything based off 1080p
		this.displayScale.x = x/1920f;
		this.displayScale.y = y/1080f;
		gridWidth = x;
		gridHeight = y;
	}

	public void initGrid(){
		grid = new Grid(canvas.getWidth(), canvas.getHeight(), scale);
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
		this.scale.x = width/bounds.getWidth();
		this.scale.y = height/bounds.getHeight();
		this.displayScale.x = width/1920f;
		this.displayScale.y = height/1080f;
		for(Obstacle obj : walls){
			obj.setDrawScale(scale);
			obj.setDisplayScale(displayScale);
		}
		for (Obstacle trapE : trapEffects){
			trapE.setDrawScale(scale);
			trapE.setDisplayScale(displayScale);
		}
		for (Obstacle trap : traps){
			trap.setDrawScale(scale);
			trap.setDisplayScale(displayScale);
		}
		for (Obstacle c : chickens){
			c.setDrawScale(scale);
			c.setDisplayScale(displayScale);
			float cwidth = chickenTexture.getRegionWidth()/scale.x*displayScale.x;
			float cheight = chickenTexture.getRegionHeight()/scale.y*displayScale.y;
			((Chicken) c).resize(cwidth,cheight);
		}
		for (Obstacle other : others){
			other.setDrawScale(scale);
			other.setDisplayScale(displayScale);
		}
		if(chef!= null) {
			chef.setDrawScale(scale);
			chef.setDisplayScale(displayScale);
		}
		if(grid!=null){
			grid.resize(width,height,scale);
		}
	}

	/**
	 * If this GameController has been populated with information.
	 * @return
	 */
	public boolean initialized(){
		return objects == null;
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
		paused = false;
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
		activateInputProcessor(true);
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
		activateInputProcessor(false);
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		//register clicking to pause menu
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		//register pausing
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}