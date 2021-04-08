package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Queue;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public abstract class ChickenModel extends CapsuleObstacle {
    //TODO: Implement the Enemy Chicken and its methods, feel free to add or remove methods as needed
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** The initializing data (to avoid magic numbers) */
    protected JsonValue data;
    /** The initializing data (to avoid magic numbers) */
    protected JsonValue unique;
    /** The physics shape of this object */
    protected CircleShape sensorShape;
    /** Identifier to allow us to track the sensor in ContactListener */
    private String sensorName;

    /** The type of chicken */
    enum Type {
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
    /** The grid */
    protected Grid grid;
    /** Goal tiles */
    protected PriorityQueue<Grid.Tile> open;
    /** Closed tiles / tiles already evaluated */
    protected ArrayList<Grid.Tile> closed;
    /** The tile that the chicken is on */
    protected Grid.Tile start_tile;
    /** The tile that the target is on */
    protected Grid.Tile target_tile;
    /** The tile that the chicken will move to */
    protected Grid.Tile move_tile;
    /** The tile that is the child of move_tile */
    protected Grid.Tile child_tile;

    /** The maximum enemy speed */
    protected final float maxspeed;
    /** The speed that the enemy chases the player */
    protected float chaseSpeed;
    /** The strength of the knockback force the chicken receives after getting slapped*/
    protected final float knockback;
    /** Cache for internal force calculations */
    protected final Vector2 forceCache = new Vector2();
    /** The amount to slow the character down */
    protected final float damping;
    /** The max health of the chicken nugget */
    private int max_health;

    // All of these variables will be put into a FSM in AIController eventually
    /** Health of the chicken*/
    private float health;
    /** Time until invulnerability after getting hit wears off */
    protected final float INVULN_TIME = 1f;
    /** Counter for Invulnerability timer*/
    protected float invuln_counter = INVULN_TIME;
    /** Time to move perpendicular to a wall upon collision before returning to normal AI */
    protected final float SIDEWAYS_TIME = 0.1f;
    /** Counter for sideways movement timer*/
    protected float sideways_counter = SIDEWAYS_TIME;
    /** Time to remain stationary after hitting the player */
    protected final float STOP_TIME = 1f;
    /** Counter for stop movement timer*/
    protected float stop_counter = STOP_TIME;
    /** True if the chicken has just been hit and the knockback has not yet been applied*/
    protected boolean hit = false;
    /** Multiplier for damage taken when on fire */
    private final int FIRE_MULT = 2;

    /** The player */
    private ChefModel player;

    /** Whether or not chicken sound is playing */
    protected boolean soundCheck = true;

    protected boolean finishA = false;
    protected float attack_timer = -1f;

    protected float attack_charge = 0f;

    protected float ATTACK_CHARGE = 0.4f;

    protected boolean hitboxOut = false;


    protected float ATTACK_DUR = 0.2f;

    protected FilmStrip animator;

    /** Reference to texture origin */
    protected Vector2 origin;

    private float slow = 1f;

    protected float status_timer = 0f;

    private boolean cookin = false;

    protected TextureRegion healthBar;

    private float CHICK_HIT_BOX = 0.8f;

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
     * @param player    The target player
     * @param mh        The max health of the chicken
     */
    public ChickenModel(JsonValue data, JsonValue unique, float x, float y, float width, float height, ChefModel player, int mh, Grid grid, Type type) {
        // The shrink factors fit the image to a tigher hitbox
        super(x, y, width * unique.get("shrink").getFloat(0),
                height * unique.get("shrink").getFloat(1));
        setDensity(unique.getFloat("density", 0));
        setFriction(data.getFloat("friction", 0));  /// IT WILL STICK TO WALLS IF YOU FORGET
        setFixedRotation(true);
        setName("chicken");
        this.target = player;
        this.player = player;
        this.grid = grid;
        this.data = data;
        this.unique = unique;
        sensorName = "chickenSensor";
        damping = data.getFloat("damping", 0);
        maxspeed = unique.getFloat("maxspeed",0);
        chaseSpeed = unique.getFloat("chasespeed",0);
        knockback = unique.getFloat("knockback",0);
        max_health = (int)(unique.getFloat("maxhealth",0) * (mh/100));
        health = max_health;
        open = new PriorityQueue<>(4, grid.getComparator());
        closed = new ArrayList<>();
    }

    /**
     * Sets the current chicken max health
     * @param h - the number to set the max health of the chicken to
     *
     */
    public void setMaxHealth(int h){ max_health = h; }

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
        sensorDef.density = unique.getFloat("density",0);
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
     * Applies the force to the body of this dude
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!isActive()) {
            return;
        }

        if (hit){ hit = false; }
        else { forceCache.set(-damping * getVX(), -damping * getVY()); }
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

    /** Returns the distance between Vectors a and b
     *
     * @param a The first vector
     * @param b The second vector
     * @return  The distance between a and b
     */
    private float distance(Vector2 a, Vector2 b) {
        float xdiff = b.x - a.x;
        float ydiff = b.y - a.y;
        return (float)Math.sqrt(Math.pow(xdiff,2) + Math.pow(ydiff,2));
    }

    /** This method returns the tile that the chicken will move towards.
     *
     * Using A* algorithm, this method determines the shortest path
     * to the player. Once the shortest path has been found, the
     * algorithm will retrace steps until it finds the first
     * tile in the path. This method also stores the second
     * tile in the path into child_tile.
     *
     * @return the next tile to move towards
     */
    public Grid.Tile AStar() {
        while (!open.isEmpty()) {
            Grid.Tile curr = open.peek();
            if (curr == target_tile) {
                child_tile = curr;
                if (curr.getParent() == null) { return curr; }
                Grid.Tile parent = curr.getParent();
                while (parent.getParent() != null) {
                    child_tile = curr;
                    curr = curr.getParent();
                    parent = curr.getParent();
                }
                return curr;
            }
            for (Grid.Tile neighbor: curr.getNeighbors()) {
                if (!neighbor.isObstacle()) {
                    float hcost = distance(grid.getPosition(target_tile.row, target_tile.col), grid.getPosition(neighbor.row,neighbor.col));
                    // ndist = distance between curr and neighbor
                    float ndist = distance(grid.getPosition(curr.row, curr.col), grid.getPosition(neighbor.row, neighbor.col));
                    float gcost = ndist + curr.getGcost();
                    float fcost = hcost + gcost;

                    if (!closed.contains(neighbor) && !open.contains(neighbor)) {
                        neighbor.setParent(curr);
                        neighbor.setGcost(gcost);
                        neighbor.setHcost(hcost);
                        neighbor.setFcost(neighbor.getGcost() + distance(grid.getPosition(neighbor.row, neighbor.col), grid.getPosition(target_tile.row, target_tile.col)));
                        open.add(neighbor);
                    } else {
                        if (fcost < neighbor.getGcost()) {
                            neighbor.setGcost(ndist + curr.getGcost());
                            neighbor.setFcost(fcost);
                            neighbor.setParent(curr);
                            if (closed.contains(neighbor)) {
                                closed.remove(neighbor);
                                if (!open.contains(neighbor)) { open.add(neighbor); }
                            }
                        }
                    }
                }
            }
            closed.add(curr);
            open.remove(curr);
        }
        return null;
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt, int[] plist) {
        super.update(dt, plist);
        invuln_counter   = MathUtils.clamp(invuln_counter+=dt,0f,INVULN_TIME);
        sideways_counter = MathUtils.clamp(sideways_counter+=dt,0f,SIDEWAYS_TIME);
        stop_counter = MathUtils.clamp(stop_counter+=dt,0f,STOP_TIME);
        if (attack_timer >= 0 && attack_charge >= 0f) { attack(dt); }
        else if (target.isActive()) { move(); }
        if (!cookin) {
            status_timer = Math.max(status_timer - dt, -1f);
        }
        //TODO: delete after technical
        setMaxHealth((int)(unique.getFloat("maxhealth",0)*(plist[1]/100)));
        setChaseSpeed(plist[10]);
    }

    /** Perform an attack on the target. */
    public abstract void attack(float dt);

    /** Handles chicken movement.
     *
     * The chicken will always move towards its target unless the
     * chicken has recently attacked or the chicken has been stunned.
     * This move function utilizes A* pathfinding.
     */
    public void move() {
        open.clear();
        closed.clear();
        grid.clearCosts();

        start_tile = grid.getTile(getPosition().x, getPosition().y);
        start_tile.setGcost(0);
        start_tile.setHcost(distance(target.getPosition(), grid.getPosition(start_tile.row, start_tile.col)));
        start_tile.setFcost(start_tile.getHcost());
        open.add(start_tile);
        target_tile = grid.getTile(target.getX(),target.getY());
        move_tile = AStar();

        // Moving in a straight line?
        if (child_tile.row == start_tile.row || child_tile.col == start_tile.col || move_tile == target_tile) {
            forceCache.set(grid.getPosition(move_tile.row, move_tile.col).sub(getPosition()));
        } else {
            forceCache.set(grid.getPosition(child_tile.row, child_tile.col).sub(getPosition()));
        }
        System.out.println(forceCache.x + " " + forceCache.y);
        forceCache.nor();
        forceCache.scl(chaseSpeed * slow);
        if (isStunned()) {
            forceCache.scl(-knockback);
            applyForce();
        }
        else{
            if (stop_counter < STOP_TIME){ forceCache.setZero(); }
            setVX(forceCache.x);
            setVY(forceCache.y);
        }
    }

/*    public boolean getSoundCheck() {
        if (soundCheck) {
            soundCheck = false;
            return true;
        }
        return false;
    }*/

    public void startAttack() {
        attack_timer = ATTACK_DUR;
        attack_charge = 0f;
    }

    public void stopAttack() {
        finishA = true;
    }

    public boolean isAttacking(){
        return hitboxOut;
    }

    public boolean chasingPlayer() { return target.equals(player); }

    public void setChaseSpeed(float spd){
        chaseSpeed = spd;
    }

    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture, 3, 5);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
    }

    public void setBarTexture(TextureRegion texture){
        healthBar = texture;
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (!isStunned() || ((int)(invuln_counter * 10)) % 2 == 0) {
            canvas.draw(healthBar, Color.FIREBRICK, 0, origin.y, getX() * drawScale.x - 17, getY() * drawScale.y + 40, getAngle(), 0.08f, 0.025f);
            canvas.draw(healthBar, Color.GREEN, 0, origin.y, getX() * drawScale.x - 17, getY() * drawScale.y + 40, getAngle(), 0.08f * (health / max_health), 0.025f);
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
    }

    /**
     * The chicken takes damage
     *
     * @param damage The amount of damage to this chicken's health
     */
    public void takeDamage(float damage) {
        if (!isStunned()) {
            if (status_timer >= 0) {
                health -= damage * FIRE_MULT;
            } else {
                health -= damage;
            }
            finishA = true;
            attack_timer = -1f;
            attack_charge = -1f;
            invuln_counter = 0;
            hitboxOut = false;
            hit = true;
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
     * Whether the chicken is currently in hitstun
     *
     * @return true if the chicken is currently stunned
     */
    public Boolean isStunned(){
        return invuln_counter < INVULN_TIME;
    }

    /** If the enemy is still alive
     * @return true if chicken health > 0*/
    public boolean isAlive() {return health > 0;}



    /**
     * The chicken has collided with a wall and will move perpendicularly to get around the wall
     */
    public void hitWall(){
        if (!isStunned()){
            sideways_counter = 0;
        }
        //grid.setObstacle(getX(),getY());
    }

    /**
     * The chicken has collided with the player and will remain stationary for some time
     */
    public void hitPlayer(){
        stop_counter = 0;
    }
}
