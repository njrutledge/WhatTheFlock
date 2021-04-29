package code.game.controllers;

import code.game.models.GameObject;
import code.util.PooledList;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import code.game.models.Chef;
import code.game.models.Chicken;
import code.game.models.Grid;

import java.util.ArrayList;
import java.util.PriorityQueue;


/** This class handles the AI for the enemy chickens by using a finite state machine.
 *  Transitions between the states of the FSM are handled in this class. This class
 *  determines the action of the enemy chicken based on its current state.
 *  */
public class AIController {
    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** The initializing data (to avoid magic numbers) */
    protected JsonValue unique;
    /** The player character that the enemy will follow */
    private GameObject target;
    /** The position relative to the target that the chicken will move towards (used for flanking)*/
    private Vector2 targetOffset;
    /** The chef that the enemy wants to attack */
    private Chef chef;
    /** The speed that the enemy chases the player */
    //TODO: make final after technical
    private float chaseSpeed;
    /** The strength of the knockback force the chicken receives after getting slapped*/
    private final float knockback;
    /** Time until invulnerability after getting hit wears off */
    private final float INVULN_TIME = 0.5f;
    /** Counter for Invulnerability timer*/
    private float invuln_counter = INVULN_TIME;
    /** Time to remain stationary after hitting the player */
    private final float STOP_DUR;
    /** Counter for stop movement timer*/
    private float stop_counter;
    /** Counter for number of chickens within flanking range */
    private static int flankers = 0;
    /** Number of chickens within flanking range required to start flanking */
    private static final int FLANK_THRESHOLD = 0;
    /** Range where the chicken will start flanking */
    private static final float FLANKING_RANGE = 10;
    /** The disatnce the chicken will flank */
    private static final float FLANKING_DISTANCE = 3;
    /** Whether the chicken is currently in flanking range */
    private boolean isFlanking;
    /** Whether the chicken is dead */
    private boolean dead = false;
    /** Whether the chicken has finished flanking and is ready to chase */
    private boolean doneFlanking = false;
    /** Reference to texture origin */
    protected Vector2 origin;

    /** The chicken being controlled by this controller */
    private Chicken chicken;
    /** The states of the finite state machine for chicken AI*/
    public enum FSM{
        /** Chicken is chasing the player, but not in attack range yet*/
        CHASE,
        /** The chicken has just taken damage and is receiving a knockback force*/
        KNOCKBACK,
        /** The chicken has recently taken damage, but not receiving a knockback force*/
        STUNNED,
        /** The chicken has recently attacked and is recovering before performing an action */
        STOP,
        /** The chicken is attacking the chef */
        ATTACK,
        /** The chickens are flanking the chef */
        FLANK
    }

    // Pathfinding
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

    int iter = 0;
    /** Vector2 used for calculations to avoid making Vector2's every frame */
    private Vector2 temp = new Vector2();

    /** The current state of the AI FSM */
    private FSM state;
    /** Creates a new AIController
     *
     * @param chicken   the chicken that is being controlled
     * @param chef      the chef that the chicken is trying to attack
     * */
    public AIController(Chicken chicken, Chef chef, Grid grid){
        this.target = chef;
        this.targetOffset = new Vector2(0,0);
        this.chef = chef;
        this.chicken = chicken;
        this.data = chicken.getJsonData();
        this.unique = chicken.getJsonUnique();
        this.state = FSM.CHASE;
        this.grid = grid;
        STOP_DUR = chicken.getStopDur();
        stop_counter = STOP_DUR;
        chaseSpeed = unique.getFloat("chasespeed", 0);
        knockback = unique.getFloat("knockback", 0);
        open = new PriorityQueue<>(4, grid.getComparator());
        closed = new ArrayList<>();
    }

    /**
     * if any of the tiles are null
     * @return
     */
    private boolean anyTilesNull(){
        return (child_tile == null || start_tile == null || move_tile == null || target_tile == null);
        //return false;
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
                } else if (stop_counter < STOP_DUR) {
                    state = FSM.STOP;
                }
                else if (!chicken.isLured() && chicken.isAttacking()) {
                    state = FSM.ATTACK;
                }
                else if (!chicken.isLured() && isFlanking && !doneFlanking && flankers >= FLANK_THRESHOLD){
                    state = FSM.FLANK;
                    float rand = (float)Math.random();
                    temp.set(target.getPosition());
                    temp.sub(chicken.getPosition()).setLength(FLANKING_DISTANCE);
                    targetOffset.set(temp);
                    //targetOffset = target.getPosition().cpy().sub(chicken.getPosition()).setLength(FLANKING_DISTANCE);
                    if (rand < 0.33) {
                        targetOffset.rotate90(-1);
                    }
                    else if (rand < 0.66) {
                        targetOffset.rotate90(1);
                    }
                    else{
                        targetOffset.setZero();
                    }

                }
                break;
            case FLANK:
                if (chicken.getHit()){
                    state = FSM.KNOCKBACK;
                    targetOffset.setZero();
                } else if (stop_counter < STOP_DUR) {
                    state = FSM.STOP;
                    targetOffset.setZero();
                }
                else if (!chicken.isLured() && chicken.isAttacking()) {
                    state = FSM.ATTACK;
                    targetOffset.setZero();
                    doneFlanking = true;
                }
                else if (!isFlanking || chicken.isLured()){
                    state = FSM.CHASE;
                    targetOffset.setZero();
                    //doneFlanking = true;
                }
                else {
                    temp.set(target.getPosition());
                    temp.add(targetOffset);
                    if (chicken.getPosition().dst(temp) < 1f){
                        state = FSM.CHASE;
                        targetOffset.setZero();
                        doneFlanking = true;
                    }
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
                break;
            case STOP:
                if (chicken.getHit()){
                    state = FSM.KNOCKBACK;
                }
                else if (stop_counter >= STOP_DUR) {
                    if (!chicken.isLured() && chicken.isTouching()) {
                        state = FSM.ATTACK; chicken.startAttack();
                    }
                    else { state = FSM.CHASE; }
                }
                break;
            case ATTACK:
                if (chicken.getHit()){
                    state = FSM.KNOCKBACK;
                }
                else if ((chicken.isLured() || chicken.stopThisAttack() || !chicken.isAttacking() && !chicken.isTouching())) {
                    state = FSM.CHASE;
                }
                else if (stop_counter < STOP_DUR) {
                    state = FSM.STOP;
                }
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
        iter++;
        if (chicken.isStopped()) { stop_counter = 0; }
        invuln_counter   = MathUtils.clamp(invuln_counter+=dt,0f,INVULN_TIME);
        stop_counter = MathUtils.clamp(stop_counter+=dt,0f,STOP_DUR);
        changeState();
        //state = FSM.STOP;
        setForceCache();
        if (state == FSM.ATTACK && target.isActive()) {
            chicken.attack(dt);
        }
        target = (GameObject) chicken.getTarget();

        // If in flanking range, start flanking
        if (chicken.getPosition().dst(chef.getPosition()) <= FLANKING_RANGE && !isFlanking){
            isFlanking = true;
            flankers++;
        }
        // If not in flanking range, stop flanking
        else if (chicken.getPosition().dst(chef.getPosition()) > FLANKING_RANGE && isFlanking){
            isFlanking = false;
            flankers--;
        }
    }

    /**
     * Sets the forcecache of the chicken based on the current state of the AI Controller.
     * This forcecache will eventually be used in ChickenModel to alter the physics of the chicken
     * */
    private void setForceCache(){
        switch(state){
            case CHASE:
            case FLANK:
                move();
                //if (anyTilesNull()) {return;} //TODO fix the real issues, bandaid
                Grid.Tile chickTile = grid.getTile(chicken.getX(),chicken.getY());
                //temp.set(grid.getPosition(move_tile.getRow() - chickTile.getRow(),move_tile.getCol()-chickTile.getCol()));
                temp.set(grid.getPosition(move_tile.getRow(), move_tile.getCol()).sub(chicken.getPosition()));
                //temp = fixTemp(temp);
                temp.nor();
                temp.scl(chaseSpeed * chicken.getSlow());

                if (chicken.isLured() && chicken.getPosition().dst(target.getPosition()) < 0.5){
                    temp.setZero();
                }
                chicken.setForceCache(temp, false);
                break;
            case KNOCKBACK:
                temp.set(chef.getPosition().sub(chicken.getPosition()));
                temp.nor();
                temp.scl(-knockback);
                chicken.setForceCache(temp, true);
                break;
            case STUNNED:
                temp.setZero();
                chicken.setForceCache(temp, true);
                break;
            case STOP:
                temp.setZero();
                chicken.setForceCache(temp, false);
                break;
            case ATTACK:
                switch(chicken.getType()) {
                    case Buffalo:
                        if (chicken.doneCharging()) {
                            if (!chicken.isRunning()) {
                                chicken.setRunning(true);
                                temp.set(chicken.getChickenAttack().getLinearVelocity());
                            } else {
                                chicken.setDestination(target.getPosition());
                                temp.set(chicken.getChickenAttack().updateLinearVelocity());
                            }
                        }
                        else { temp.setZero(); }
                        chicken.setForceCache(temp, false);
                        break;
                    default:
                        temp.setZero();
                        chicken.setForceCache(temp, false);
                        break;
                }
            default: // This shouldn't happen
                break;
        }
    }

    private Vector2 fixTemp(Vector2 temp){
        Grid.Tile chicken_tile = grid.getTile(chicken.getX(),chicken.getY());
        //bottom left corner
        if (Math.abs(chicken_tile.getCol() - move_tile.getCol())==1 && chicken_tile.getRow()==move_tile.getRow()){
            if(grid.isObstacleTile(move_tile.getRow()+1, move_tile.getCol())){
                temp.x = 0;
                temp.y = 1;
            }else if (grid.isObstacleTile(move_tile.getRow()-1,move_tile.getCol())){
                temp.x = 0;
                temp.y = -1;
            }
        }else if (chicken_tile.getCol()==move_tile.getCol() && Math.abs(chicken_tile.getRow()-move_tile.getRow())==1){
            if(grid.isObstacleTile(move_tile.getRow(), move_tile.getCol()+1)){
                temp.y = 0;
                temp.y = 1;
            }else if (grid.isObstacleTile(move_tile.getRow(),move_tile.getCol()-1)){
                temp.y = 0;
                temp.x = -1;
            }
        }
        return temp;
    }

    /** Returns the distance between Vectors a and b
     *
     * @param a The first vector
     * @param b The second vector
     * @return  The distance between a and b
     */
    //TODO There is a built in distance function Vector2.dst, do we really need this?
    private float distance(Vector2 a, Vector2 b) {
        float xdiff = b.x - a.x;
        float ydiff = b.y - a.y;
        return (float)Math.sqrt(Math.pow(xdiff,2) + Math.pow(ydiff,2));
    }

    /**
     * Returns the distance between two Grid tiles
     * @param one
     * @param two
     * @return
     */
    private float distance(Grid.Tile one, Grid.Tile two){
        Vector2 a = grid.getPosition(one.getRow(), one.getCol());
        Vector2 b = grid.getPosition(two.getRow(), two.getCol());
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
     */
    public void AStar() {
        boolean first = true;
        while (!open.isEmpty()) {
            Grid.Tile curr = open.peek();
            if (curr == target_tile) {
                child_tile = curr;
                if (curr.getParent() == null) { move_tile = curr; return; }
                Grid.Tile parent = curr.getParent();
                while (parent.getParent() != null) {
                    child_tile = curr;
                    curr = curr.getParent();
                    parent = curr.getParent();
                }
                move_tile = curr;
                return;
            }
            for (Grid.Tile neighbor: curr.getNeighbors()) {
                if ((!neighbor.isObstacle()) && isReachable(neighbor, curr, first)) {
                    //heuristic function cost
                    float hcost = getHCost(neighbor, curr);
                    // ndist = distance between curr and neighbor
                    float ndist = distance(curr, neighbor);
                    float gcost = ndist + curr.getGcost();
                    float fcost = hcost + gcost;

                    if (!closed.contains(neighbor) && !open.contains(neighbor)) {
                        neighbor.setParent(curr);
                        neighbor.setGcost(gcost);
                        neighbor.setHcost(hcost);
                        neighbor.setFcost(neighbor.getGcost() + distance(neighbor, target_tile));
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
            first = false;
        }
    }

    private float getHCost(Grid.Tile neighbor, Grid.Tile curr){
       float hcost = distance(target_tile, neighbor);
        for(int row = neighbor.getRow()-1; row <= neighbor.getRow()+1;row++){
            for (int col = neighbor.getCol()-1; col <= neighbor.getCol()+1;col++){
                if(grid.isObstacleTile(row,col)){
                    hcost+=15;
                }
            }
        }
        //System.out.println(hcost);
        return hcost;
    }

    private boolean isReachable(Grid.Tile spot, Grid.Tile curr, boolean first){
        if (first) {
            PooledList<Grid.Tile> tiles = getChickenTiles();
            for(Grid.Tile chickSpot : tiles){
                if (chickSpot.getRow() != curr.getRow() && chickSpot.getCol() != curr.getCol()) {
                    int diag1Row = chickSpot.getRow();
                    int diag1Col = curr.getCol();
                    int diag2Row = curr.getRow();
                    int diag2Col = spot.getCol();
                    if(grid.isObstacleTile(diag1Row, diag1Col) || grid.isObstacleTile(diag2Row, diag2Col)){
                        return false;
                    }
                }
            }
            return true;
        } else{
            if (spot.getRow() != curr.getRow() && spot.getCol() != curr.getCol()) {
                int diag1Row = spot.getRow();
                int diag1Col = curr.getCol();
                int diag2Row = curr.getRow();
                int diag2Col = spot.getCol();
                return !(grid.isObstacleTile(diag1Row, diag1Col) || grid.isObstacleTile(diag2Row, diag2Col));
            }
        }
        return true;
    }

    private PooledList<Grid.Tile> getChickenTiles() {
        float x = chicken.getX();
        float y = chicken.getY();
        PooledList<Grid.Tile> tiles = new PooledList<Grid.Tile>();
        Grid.Tile main = grid.getTile(x,y);
        tiles.add(main);
        float xmax = x + chicken.getWidth()/2.0f;
        float xmin = x - chicken.getWidth()/2.0f;
        float ymax = y + chicken.getHeight()/2.0f;
        float ymin = y - chicken.getHeight()/2.0f;
        for (float ii = x - chicken.getWidth()/2.0f; ii<=x + chicken.getWidth()/2.0f; ii += chicken.getWidth()/2.0f){
            for (float jj = y - chicken.getHeight()/2.0f; jj <= y + chicken.getHeight()/2.0f; jj += chicken.getWidth()/2.0f){
                Grid.Tile next = grid.getTile(ii,jj);
                if(!next.equals(main)){
                    tiles.add(next);
                }
            }
        }
        return tiles;
    }

    /** Handles chicken movement.
     *
     * The chicken will always move towards its target unless the
     * chicken has recently attacked or the chicken has been stunned.
     * This move function utilizes A* pathfinding.
     */
    public void move() {
        float x = target.getX() + targetOffset.x;
        float y = target.getY() + targetOffset.y;
        if(!grid.inBounds(grid.getTile(x, y).getRow(), grid.getTile(x, y).getCol()) || grid.getTile(x, y).isObstacle()){
            targetOffset = Vector2.Zero;
        }

        open.clear();
        closed.clear();
        grid.clearCosts();
        start_tile = grid.getTile(chicken.getX(), chicken.getY());
        start_tile.setGcost(0);
        start_tile.setHcost(distance(target.getPosition().cpy().add(targetOffset), grid.getPosition(start_tile.getRow(), start_tile.getCol())));
        start_tile.setFcost(start_tile.getHcost());
        open.add(start_tile);
        target_tile = grid.getTile(target.getX() + targetOffset.x, target.getY() + targetOffset.y); //could be getting a null tile?
        AStar();

        if ((child_tile.getRow() != start_tile.getRow() && child_tile.getCol() != start_tile.getCol()) || move_tile == target_tile) {
                move_tile = child_tile;
        }

    }

    /** Chicken dies */
    public void die(){
        if (!dead) {
            if (isFlanking) {
                flankers--;
            }
            dead = true;
        }
    }
}