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

public class ChickenModel extends CapsuleObstacle {
    //TODO: Implement the Enemy Chicken and its methods, feel free to add or remove methods as needed
    ///////// Currently only dude methods which I thought were important are included, they will likely need to be
    ///////// altered or removed, but should provide a good base to start with.

    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** The physics shape of this object */
    private CircleShape sensorShape;
    /** Identifier to allow us to track the sensor in ContactListener */
    private String sensorName;

    // Path finding
    /** The player character that the enemy will follow
     * We would probably want an AI Controller to handle this, but enemy movement is
     * pretty simple for the prototype */
    private Obstacle target;
    /** The grid */
    private Grid grid;
    /** Goal tiles */
    private PriorityQueue<Grid.Tile> open;
    /** Closed tiles / tiles already evaluated */
    private ArrayList<Grid.Tile> closed;
    /** The tile that the chicken is on */
    private Grid.Tile start_tile;
    /** The tile that the target is on */
    private Grid.Tile target_tile;
    /** The tile that the chicken will move to */
    private Grid.Tile move_tile;
    /** The tile that is the child of move_tile */
    private Grid.Tile child_tile;

    /** The maximum enemy speed */
    //TODO: make final after technical
    private float maxspeed;
    /** The speed that the enemy chases the player */
    //TODO: make final after technical
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
    private final float SIDEWAYS_TIME = 1f;
    /** Counter for sideways movement timer*/
    private float sideways_counter = SIDEWAYS_TIME;
    /** Time to remain stationary after hitting the player */
    private final float STOP_TIME = 1f;
    /** Counter for stop movement timer*/
    private float stop_counter = STOP_TIME;
    /** True if the chicken has just been hit and the knockback has not yet been applied*/
    private boolean hit = false;

    private ChefModel player;

    private final int FIRE_MULT = 2;

    protected FilmStrip animator;
    /** Reference to texture origin */
    protected Vector2 origin;

    private float slow = 1f;

    private float status_timer = 0f;

    private boolean cookin = false;

    private TextureRegion healthBar;

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
    public ChickenModel(JsonValue data, float x, float y, float width, float height, ChefModel player, int mh, Grid grid) {
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
        sensorName = "chickenSensor";
        this.target = player;
        this.player = player;
        this.grid = grid;
        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        chaseSpeed = data.getFloat("chasespeed", 0);
        knockback = data.getFloat("knockback", 0);
        max_health = mh;
        health = max_health;
        this.data = data;
        open = new PriorityQueue<>(4, new TileComparator());
        closed = new ArrayList<>();
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

    /** Determines the direction of the force that will move the chicken forward
     *  based on the closest possible path towards the target
     */
    public Grid.Tile AStar() {
        while (!open.isEmpty()) {
            Grid.Tile curr = open.peek();
            if (curr == target_tile) {
                if (curr.getParent() == null) { return curr; }
                child_tile = curr;
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
        open.clear();
        closed.clear();
        grid.clearCosts();
        invuln_counter   = MathUtils.clamp(invuln_counter+=dt,0f,INVULN_TIME);
        sideways_counter = MathUtils.clamp(sideways_counter+=dt,0f,SIDEWAYS_TIME);
        stop_counter = MathUtils.clamp(stop_counter+=dt,0f,STOP_TIME);

       if (target.isActive()) {
            if (grid.sameTile(target.getX(), target.getY(), getX(), getY())) {
                forceCache.set(target.getPosition().sub(getPosition()));
            } else {
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
            }
            //forceCache.set(target.getPosition().sub(getPosition()));
            forceCache.nor();
            forceCache.scl(chaseSpeed * slow);
            if (isStunned()) {
                forceCache.scl(-knockback);
                applyForce();
            }
            else{
/*                if (sideways_counter < SIDEWAYS_TIME) {
                    forceCache.rotate90(0);
                }*/
                if (stop_counter < STOP_TIME){
                    forceCache.setZero();
                }
                setVX(forceCache.x);
                setVY(forceCache.y);

            }
            if (!cookin) {
                status_timer = Math.max(status_timer - dt, -1f);
            }
            //TODO: delete after technical
            setMaxHealth(plist[1]);
            setChaseSpeed(plist[10]);
        }
    }

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
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),drawScale.x,drawScale.y);
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
            invuln_counter = 0;
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

    /**
     * Comparator for Tile class that have the following rules:
     *
     * If Tile1's FCost < Tile2's FCost : -1
     * If Tile1's FCost > Tile2's FCost : 1
     * Otherwise, 0
     */
    public class TileComparator implements Comparator<Grid.Tile> {
        @Override
        public int compare(Grid.Tile tile1, Grid.Tile tile2) {
            return tile1.getFcost() < tile2.getFcost() ? -1: tile1.getFcost() > tile2.getFcost()? 1: 0;
        }
    }
}
