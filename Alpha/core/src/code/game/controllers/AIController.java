package code.game.controllers;

import code.game.models.GameObject;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.utils.JsonValue;

import code.game.models.ChefModel;
import code.game.models.ChickenModel;
import code.game.models.obstacle.Obstacle;
import code.util.FilmStrip;


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




    private ChickenModel chicken;

    private ChefModel chef;

    public static enum FSM{
        CHASE,
        KNOCKBACK,
        STUNNED,
        ATTACK
    }

    Vector2 temp = new Vector2();


    private FSM state;
    /** Creates a new AIController
     *
     * @param chicken   the chicken that is being controlled
     * @param chef      the chef that the chicken is trying to attack
     * */
    public AIController(ChickenModel chicken, ChefModel chef){
        this.target = chef;
        this.chef = chef;
        this.chicken = chicken;
        this.data = chicken.getJsonData();
        this.state = FSM.CHASE;
        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        chaseSpeed = data.getFloat("chasespeed", 0);
        knockback = data.getFloat("knockback", 0);
    }

    /**
     * If applicable, change the FSM state for this AI controller based on the current state and
     * recent interactions between the chicken and traps/chef
     */
    private void changeState(){
        switch(state){
            case CHASE:
                if (Math.random()<0.005){// TODO if the chicken just got hit
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
        invuln_counter   = MathUtils.clamp(invuln_counter+=dt,0f,INVULN_TIME);
        sideways_counter = MathUtils.clamp(sideways_counter+=dt,0f,SIDEWAYS_TIME);
        stop_counter = MathUtils.clamp(stop_counter+=dt,0f,STOP_TIME);
        changeState();
        setForceCache();
    }

    /**
     * Sets the forcecache of the chicken based on the current state of the AI Controller.
     * This forcecache will eventually be used in ChickenModel to alter the physics of the chicken
     * */
    private void setForceCache(){
        switch(state){
            case CHASE:
                temp.set(target.getPosition().sub(chicken.getPosition()));
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
}