package code.game.models;

import code.game.interfaces.TrapInterface;
import code.util.FilmStrip;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import code.game.views.GameCanvas;

public class Trap extends GameObject implements TrapInterface {

    /**
     * Enumeration to encode the trap type
     */
    public enum type {
        BREAD_LURE,
        SLOW,
        //FIRE,
        //FIRE_LINGER,
        COOLER,
        HOT_SAUCE,
        TOASTER
    }

    /**
     * Enumeration to encode the trap shape
     */
    public enum shape {
        /**
         * Circular trap
         */
        CIRCLE,
        /**
         * Square trap
         */
        SQUARE
    }

    /**
     * The initializing data (to avoid magic numbers)
     */
    private JsonValue data;

    /**
     * The game shape of this object
     */
    private Shape sensorShape;
    /**
     * true if using the second game shape
     */
    private boolean linger;
    /**
     * The type of this trap
     */
    private type trapType;

    /**
     * The durability of this trap
     */
    private float durability;
    /**
     * The max durability
     */
    private static final float MAX_DURABILITY = 30.0f;

    /**
     * Hurt box shape for the Lure trap. Null for all other traps
     */
    private Shape lHShape;

    /**
     * Radius which chickens get lured to the trap
     */
    private static final float LURE_RADIUS = 6f;
    /**
     * Radius which chickens get slowed near the trap
     */
    private static final float SLOW_RADIUS = 3.5F;
    /**
     * Radius which chickens can trigger the fire trap
     */
    private static final float FIRE_TRIGGER_RADIUS = 2f;
    /**
     * Radius which chickens get set on fire
     */
    private static final float FIRE_LINGER_RADIUS = 4f;
    /**
     * Radius for the Lure hurtbox
     */
    private static final float LURE_HURT = 1.3f;
    /**
     * Colors of Fire trap
     */
    private static final Color fireColor = Color.RED;
    /**
     * Colors of slow trap
     */
    private static final Color slowColor = Color.CYAN;
    /**
     * Colors of lure trap
     */
    private static final Color lureColor = Color.YELLOW;
    /**
     * durability of lure
     */
    private float lure_amount = 3;

    /**
     * max durability of lure
     */
    private final float MAX_LURE_AMMOUNT = 6;
    /**
     * Slow effect strength
     */
    private float SLOW_EFFECT = 0.5f;
    /**
     * Fire duration effect
     */
    private float FIRE_DUR = 10.0f;
    /**
     * Fire damage duration
     */
    private float FIRE_DAM_DUR = 5.0f;
    /**
     * Activation radius for environmental traps
     */
    private final float ACTIVATION_RADIUS = 1.0f;
    /**
     * Recharge time for fridge traps
     */
    private final float FRIDGE_RECHARGE_TIME = 15.0f;
    /**
     * Recharge time for Bread Bombtraps
     */
    private final float BREAD_BOMB_RECHARGE_TIME = 15.0f;
    /**
     * Recharge time for faulty oven traps
     */
    private final float FAULTY_OVEN_RECHARGE_TIME = 15.0f;
    /**
     * Active time for slow traps
     */
    private final float SLOW_ACTIVE_TIME = 10.0f;
    /**
     * Timer for how long the environmental trap is active
     */
    private float activeTimer = 0.0f;
    /**
     * Time needed to reset the trap after use
     */
    private float READY_TIME = 15.0f;
    /**
     * Timer for how long the environmental trap is not ready
     */
    private float readyTimer = 0.0f;

    /** The child of this trap */
    private Trap childTrap;
    /**
     * flag for if the trap is ready to become active
     */
    private boolean isReady = true;
    /** flag for if the trap is invulnerable*/
    private boolean invuln = true;
    /** invulnerability Time */
    private float INVULN_TIME = .75f;
    /**Timer for how long the trap is invulnverable */
    private float invulnTimer = INVULN_TIME;
    /** Counter for how many chickens are hitting this trap */
    private int HitCount = 0;
    /**
     * Fixture for hit box of active traps
     */
    private Fixture hitFixture;
    /** texture for slap indicator above trap */
    private TextureRegion slapIndicator;
    /** Origin for slapIndicator texture */
    private Vector2 indicatorOrigin;
    /** Whether this trap has a slap indicator */
    private boolean hasIndicator = false;
    /** The animation corresponding to the trap*/
    private FilmStrip animation;
    /** The current delay on the animation; counter */
    private int anim_delay = 0;
    /** Frame delay on the trap animation*/
    private final int FRAME_DELAY = 4;
    /** Frame delay on cooler*/
    private final int COOLER_FRAME_DELAY = 40;
    /** Delay on the smoke frame animations*/
    private final int SMOKE_FRAMES_DELAY = 20;
    /**
     * Creates a new Trap model with the given game data
     * <p>
     * The size is expressed in game units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the game units to pixels.
     *
     * @param data   The game constants for this trap
     * @param x      The object's screen x location
     * @param y      The object's screen y location
     * @param width  The object width in game units
     * @param height The object width in game units
     * @param t      The Trap.type of this trap
     */
    public Trap(JsonValue data, float x, float y, float width, float height, type t) {
        super(x, y,
                width * data.get("shrink").getFloat(0),
                height * data.get("shrink").getFloat(1), ObjectType.TRAP);
        //setBodyType(BodyDef.BodyType.StaticBody);
        setFixedRotation(true);
        this.data = data;
        setName("trap");
        trapType = t;
        //setSensorName("trapSensor");
        if (!trapType.equals(type.BREAD_LURE)){
            setSensor(true);
        }else{
            setBullet(true);
            Filter lure_filter = new Filter();
            lure_filter.categoryBits = 0x0080;
            lure_filter.maskBits = 0x0004;
            setFilterData(lure_filter);
            setRestitution(.8f);

        }
        durability = MAX_DURABILITY;
        linger = false;
        indicatorOrigin = new Vector2();
    }

    /**Sets the animation corresponding to this trap*/
    public void setAnimation(FilmStrip f){
        animation = f;
    }

    /**
     * Gets the trap's effect. This is either a multiplier for the slow down effect or a duration for the fire effect.
     * -1 otherwise
     *
     * @return a float determining the effect
     */
    public float getEffect() {
        switch (trapType) {
            case SLOW:
                return SLOW_EFFECT;
            //case FIRE_LINGER:
              //  return FIRE_DAM_DUR;
        }
        return -1;
    }

    /**
     * Returns the enum type that represents this trap
     *
     * @return the type of this trap
     */
    public type getTrapType() {
        return trapType;
    }

    /**
     * Returns true if linger is enabled
     *
     * @return true if linger is enabled
     */
    public boolean getLinger() {
        return linger;
    }

    /**
     * Returns true if the trap is ready
     *
     * @return true if the trap is ready
     */
    public boolean getReady() {
        return isReady;
    }

    /**
     * enables or disables the linger effect for fire traps
     *
     * @param b is true if enabling, and false if disabling.
     */
    public void enableLinger(boolean b) {
        linger = b;
    }


    /**
     * Decrements the durability of the trap, and returns true if the durability is then zero.
     *
     * @return true if durability is now zero
     */
    public boolean decrementDurability() {
        switch (trapType) {
            case BREAD_LURE:
                lure_amount--;
                break;
            /*case FIRE:
                durability = 0; //FIRE transitions into FIRE_LINGER
                break;
             */
            case SLOW:
                durability = Math.max(0, --durability);
                break;

        }

        return durability == 0;
    }
    /** Sets a child trap for this trap, if there is one */
    public void setChildTrap(Trap child){
        childTrap = child;
    }

    /**
     * Updates the object's game state (NOT GAME LOGIC).
     * <p>
     * We use this method to reset cooldowns.
     *
     * @param delta Number of seconds since last animation frame
     */
    @Override
    public void update(float delta) {

        super.update(delta);
        /*if (trapType == type.FIRE_LINGER) {
            durability = durability - (MAX_DURABILITY / FIRE_DUR * delta);
            if (durability <= 0) {
                this.markRemoved(true);
            }
        }*/

        switch(trapType){
            case COOLER:
                if(childTrap != null && childTrap.isRemoved()){
                    childTrap = null;
                }
            case TOASTER:
            case HOT_SAUCE:
                if (!isReady) {
                    readyTimer -= delta;
                    if (readyTimer <= 0) {
                        isReady = true;
                    }
                }
                break;
            case SLOW:
                if(activeTimer <=0){
                    markRemoved(true);
                }else{
                    activeTimer -= delta;
                }
                break;
            case BREAD_LURE:
                if (invuln){
                    invulnTimer -= delta;
                    if(invulnTimer <= 0){
                        invuln = false;
                    }
                }else if(isHit()){
                    lure_amount--;
                    invuln = true;
                    invulnTimer = INVULN_TIME;
                }
                if(lure_amount <=0){
                    markRemoved(true);
                }
                break;
        }

    }

    /**
     * sets the trap as hit by a chicken
     */
    public void markHit(){ HitCount++; }

    public void removeHit(){ HitCount = Math.max(0,HitCount-1); }

    private boolean isHit(){
        return HitCount>0 && !invuln;
    }

    /**
     *  Returns if the trap is invulnerable.
     * @return true if the trap is invulnerable, false otherwise.
     */
    public boolean isInvuln(){ return invuln; }

    /**
     * Creates the game Body(s) for this object, adding them to the world.
     *
     * @param world Box2D world to store body
     * @return true if object allocation succeeded
     */
    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        switch (trapType) {
            case BREAD_LURE:
                sensorShape.setRadius(LURE_RADIUS);
                FixtureDef sensHurt = new FixtureDef();
                sensHurt.isSensor = true;
                lHShape = new CircleShape();
                lHShape.setRadius(LURE_HURT);
                sensHurt.shape = lHShape;
                Fixture sensorHurtF = body.createFixture(sensHurt);
                sensorHurtF.setUserData(FixtureType.LURE_HURT);//"lureHurt");
                sensorDef.shape = sensorShape;
                hitFixture = body.createFixture(sensorDef);
                hitFixture.setUserData(FixtureType.TRAP_HITBOX);
                break;
            case SLOW:
                sensorShape.setRadius(SLOW_RADIUS);
                activeTimer = SLOW_ACTIVE_TIME;
                sensorDef.shape = sensorShape;
                hitFixture = body.createFixture(sensorDef);
                hitFixture.setUserData(FixtureType.TRAP_HITBOX);
                break;
            /*case FIRE:
                sensorShape.setRadius(FIRE_TRIGGER_RADIUS);
                break;
            case FIRE_LINGER:
                sensorShape.setRadius(FIRE_LINGER_RADIUS);
            */case COOLER:
            case TOASTER:
            case HOT_SAUCE:
                sensorShape.setRadius(ACTIVATION_RADIUS);
                sensorDef.shape = sensorShape;
                Fixture sensorFixture = body.createFixture(sensorDef);
                sensorFixture.setUserData(FixtureType.TRAP_ACTIVATION);//"trapActivationRadius");
                break;


        }
        //getSensorName());
        return true;
    }

    /**
     * Marks the trap as active or inactive.
     *
     * If the trap is not ready, the trap will be marked inactive
     * @param bool is true if the trap is to become ready.
     */
    public void markReady(boolean bool){
        if(isReady && !bool){
            switch(trapType){
                case COOLER:
                    readyTimer = FRIDGE_RECHARGE_TIME;
                    break;
                case HOT_SAUCE:
                    readyTimer = FAULTY_OVEN_RECHARGE_TIME;
                    break;
                case TOASTER:
                    readyTimer = BREAD_BOMB_RECHARGE_TIME;
            }
        }
        isReady = bool;
    }
    /** Return the given increment to the frame based on the current anim_delay and the delay specified*/
    private int incrWithDelay(int delay){
        if(anim_delay == 0){
            anim_delay = delay;
            return 1;
        }
        else{
            anim_delay --;
            return 0;
        }
    }
    /**
     * Draws the game object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        Color c = Color.WHITE.cpy();
        float scale = .1f;
        int frame = 0;

        switch (trapType) {
            case HOT_SAUCE:
                //c = Color.GRAY;//fireColor.cpy();
                //play once
                frame = animation.getFrame();

                if(isReady) {
                    frame = 0;
                }
                else if(animation.getFrame() < 7){
                    if(anim_delay == 0){
                        anim_delay = FRAME_DELAY;
                    }
                    else{
                        anim_delay --;
                        break;
                    }
                    frame ++;
                }
                break;
            case BREAD_LURE:
                //c = lureColor.cpy();
                c.a = Math.max(0, lure_amount / MAX_LURE_AMMOUNT);
                break;
            case SLOW:
                c = slowColor.cpy();
                scale = .3f;
                c.a = Math.max(0, activeTimer / SLOW_ACTIVE_TIME);
                break;
            case COOLER:
                c = Color.WHITE.cpy();//Color.BLUE.cpy();
                scale = .2f;
                frame = animation.getFrame();
                // reset frame at beginning if ready
               if (isReady && frame > 4) {
                   frame = 0;
               }
               //add icicles
               else if (frame < 4){
                   //force a delay
                   if(anim_delay != 0){
                       anim_delay --;
                   }
                   else {
                       anim_delay = COOLER_FRAME_DELAY;
                       frame ++;
                   }
               }
               else if (isReady && frame == 4){
                   break;
               }
                // trap has been pressed, open the cooler
                else if (4 < frame && frame < 10){
                    if(anim_delay == 0) {
                        frame++;
                        anim_delay = FRAME_DELAY;
                    }
                    else{
                        anim_delay --;
                    }
                }
                //flip between 11 and 10 for smoke
                else if (childTrap != null && childTrap.activeTimer > 0){
                    if(anim_delay == 0) {
                        anim_delay = SMOKE_FRAMES_DELAY;
                        frame = (animation.getFrame() == 10 ? 11 : 10);
                    }
                    else{
                        anim_delay --;
                    }
                }
                //close trap
               else if(frame < 16 && frame > 9){
                   if(anim_delay == 0) {
                       frame++;
                   }
                   else {
                       anim_delay --;
                   }
               }
                //frame 16, reset to 0 and wait
               else if (frame == 16){
                   //stay iced
                   c = Color.BLUE.cpy();
                   break;

               }

               break;
            case TOASTER:
                c = Color.WHITE.cpy();
                frame = animation.getFrame();
                if(animation.getFrame() < 5){
                    if(anim_delay == 0){
                        anim_delay = FRAME_DELAY;
                        frame ++;
                    }
                    else{
                        anim_delay --;
                    }
                    //frame = animation.getFrame() + 1;
                }
                else if (!isReady){
                    //switch between frames 6 and 7 for smoke
                    c = Color.LIGHT_GRAY.cpy();
                    //frame = animation.getFrame();
                    if(anim_delay == 0){
                        anim_delay = SMOKE_FRAMES_DELAY; //add delay time to be less frenetic
                        frame = (animation.getFrame() == 5 ? 6:5);
                    }else {
                        anim_delay --;
                    }

                }
                else{
                    //frame = animation.getFrame();
                    if(animation.getFrame() < animation.getSize() - 1){
                        frame++;
                    }
                }
                break;

        }
        if(animation != null){
            animation.setFrame(frame);
            canvas.draw(animation, isReady ? Color.WHITE: c, origin.x, origin.y,
                    getX() * drawScale.x, getY() * drawScale.y, getAngle(), displayScale.x*scale, displayScale.y*scale);
            if (hasIndicator && isReady) {
                canvas.draw(slapIndicator, Color.WHITE, indicatorOrigin.x, indicatorOrigin.y,
                        getX() * drawScale.x, getY() * drawScale.y + 50, getAngle(), 0.5f, 0.5f);
            }
        }
        else {
            if (!isReady) {
                int breaking = 1;
            }
            canvas.draw(texture, isReady ? c : Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), displayScale.x*scale, displayScale.y*scale);
            if (hasIndicator && isReady) {
                canvas.draw(slapIndicator, Color.WHITE, indicatorOrigin.x, indicatorOrigin.y, getX() * drawScale.x, getY() * drawScale.y + 50, getAngle(), displayScale.x*0.5f, displayScale.y*0.5f);
            }
        }
    }

    /**
     * Draws the outline of the game body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics((CircleShape) sensorShape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
        if (lHShape != null) {
            canvas.drawPhysics((CircleShape) lHShape, Color.BLUE, getX(), getY(), drawScale.x, drawScale.y);
        }
    }

    public void setIndicatorTexture(TextureRegion texture){
        this.slapIndicator = texture;
        indicatorOrigin.set(texture.getRegionWidth()/2, texture.getRegionHeight()/2);
        hasIndicator = true;
    }
}
