package code.game.controllers;

import code.game.models.GameObject;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.utils.JsonValue;

import code.game.models.Chef;
import code.game.models.Chicken;
import code.game.models.obstacle.Obstacle;
import code.util.FilmStrip;

import code.game.models.Grid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;


/** This class handles the AI for the enemy chickens by using a finite state machine.
 *  Transitions between the states of the FSM are handled in this class. This class
 *  determines the action of the enemy chicken based on its current state.
 *  */
public class AIController {
    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** The player character that the enemy will follow */
    private GameObject target;
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

    private float status_timer = 0f;

    private boolean cookin = false;

    private TextureRegion healthBar;

    private float CHICK_HIT_BOX = 0.8f;

    // Path finding
    /** The player character that the enemy will follow
     * We would probably want an AI Controller to handle this, but enemy movement is
     * pretty simple for the prototype */
    //private Obstacle target;
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



    /** The chicken being controlled by this controller */
    private Chicken chicken;
    /** The chef that this chicken is targeting*/
    private Chef chef;
    /** The states of the finite state machine for chicken AI*/
    public static enum FSM{
        CHASE, /** Chicken is chasing the player, but not in attack range yet*/
        KNOCKBACK,/** The chicken has just taken damage and is receiving a knockback force*/
        STUNNED,/** The chicken has recently taken damage, but not receiving a knockback force*/
        ATTACK /** The chicken is attacking the chef */
    }
    /** Vector2 used for calculations to avoid making Vector2's every frame */
    private Vector2 temp = new Vector2();

    /** The current state of the AI FSM */
    private FSM state;
    /** Creates a new AIController
     *
     * @param chicken   the chicken that is being controlled
     * @param chef      the chef that the chicken is trying to attack
     * @param grid      The grid object used to perform A* pathfinding
     * */
    public AIController(Chicken chicken, Chef chef, Grid grid){
        this.target = chef;
        this.chef = chef;
        this.chicken = chicken;
        this.data = chicken.getJsonData();
        this.state = FSM.CHASE;
        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        chaseSpeed = data.getFloat("chasespeed", 0);
        knockback = data.getFloat("knockback", 0);

        this.grid = grid;
        open = new PriorityQueue<>(4, new TileComparator());
        closed = new ArrayList<>();
    }

    /**
     * If applicable, change the FSM state for this AI controller based on the current state and
     * recent interactions between the chicken and traps/chef
     */
    private void changeState(){
        switch(state){
            case CHASE:
                if (chicken.getHit()){
                    state = FSM.KNOCKBACK;
                }
                break;
            case KNOCKBACK:
                chicken.setStunned(true);
                invuln_counter = 0;
                state = FSM.STUNNED;
                break;
            case STUNNED:
                chicken.setInvisible(((int)(invuln_counter * 10)) % 2 == 0);
                if (invuln_counter >= INVULN_TIME){
                    state = FSM.CHASE;
                    chicken.setStunned(false);
                    chicken.setInvisible(false);
                }
            case ATTACK:
                break;
            default: // This shouldn't happen
                break;
        }
    }
    /**
     * Update the cooldown times of the AI controller
     *
     * @param dt    the number of seconds since the last animiation frame
     * */
    public void update(float dt){
        open.clear();
        closed.clear();
        grid.clearCosts();
        invuln_counter   = MathUtils.clamp(invuln_counter+=dt,0f,INVULN_TIME);
        sideways_counter = MathUtils.clamp(sideways_counter+=dt,0f,SIDEWAYS_TIME);
        stop_counter = MathUtils.clamp(stop_counter+=dt,0f,STOP_TIME);
        setForceCache();
        changeState();

    }

    /**
     * Sets the forcecache of the chicken based on the current state of the AI Controller.
     * This forcecache will eventually be used in ChickenModel to alter the physics of the chicken
     * */
    private void setForceCache(){
        switch(state){
            case CHASE:
                //temp.set(target.getPosition().sub(chicken.getPosition()));
                if (grid.sameTile(target.getX(), target.getY(), chicken.getX(), chicken.getY())) {
                    temp.set(target.getPosition().sub(chicken.getPosition()));
                } else {
                    start_tile = grid.getTile(chicken.getPosition().x, chicken.getPosition().y);
                    start_tile.setGcost(0);
                    start_tile.setHcost(distance(target.getPosition(), grid.getPosition(start_tile.row, start_tile.col)));
                    start_tile.setFcost(start_tile.getHcost());
                    open.add(start_tile);
                    target_tile = grid.getTile(target.getX(), target.getY());
                    move_tile = AStar();
                    // Moving in a straight line?
                    if (child_tile.row == start_tile.row || child_tile.col == start_tile.col || move_tile == target_tile) {
                        temp.set(grid.getPosition(move_tile.row, move_tile.col).sub(chicken.getPosition()));
                    } else {
                        temp.set(grid.getPosition(child_tile.row, child_tile.col).sub(chicken.getPosition()));
                    }

                }

                temp.nor();
                temp.scl(chaseSpeed * slow);
                chicken.setForceCache(temp, false);
                break;
            case KNOCKBACK:
                temp.set(target.getPosition().sub(chicken.getPosition()));
                temp.nor();
                temp.scl(-knockback);
                chicken.setForceCache(temp, true);
                break;
            case STUNNED:
                temp.setZero();
                chicken.setForceCache(temp, true);
                break;
            case ATTACK:
                break;
            default: // This shouldn't happen
                break;
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