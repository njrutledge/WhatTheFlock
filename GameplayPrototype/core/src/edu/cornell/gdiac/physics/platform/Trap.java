package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

public class Trap extends BoxObstacle {

    /**
     *  Enumeration to encode the trap type
     */
    public enum type {
        LURE,
        SLOW,
        FIRE,
        FIRE_LINGER
    }

    /**
     *  Enumeration to encode the trap shape
     */
    public enum shape {
        /** Circular trap */
        CIRCLE,
        /** Square trap */
        SQUARE
    }

    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;
    /** Identifier to allow us to track the sensor in ContactListenr */
    private String sensorName;
    /** The physics shape of this object */
    private Shape sensorShape;
    /** The second physics shape of this object, if used */
    private Shape lingerSensorShape;
    /** true if using the second physics shape */
    private boolean linger;
    /** The type of this trap */
    private type trapType;
    /** The physics shape of this trap */
    private shape trapShape;
    /** A name for debugging purposes */
    private String name;
    /** The durability of this trap */
    private float durability;
    /** The max durability */
    //TODO: make final after technical
    private static float MAX_DURABILITY = 30.0f;

    /** Hurt box shape for the Lure trap. Null for all other traps */
    private Shape lHShape;

    /** Radius which chickens get lured to the trap */
    private static final float LURE_RADIUS = 5f;
    /** Radius which chickens get slowed near the trap */
    private static final float SLOW_RADIUS = 3F;
    /** Radius which chickens can trigger the fire trap */
    private static final float FIRE_TRIGGER_RADIUS = 2f;
    /** Radius which chickens get set on fire */
    private static final float FIRE_LINGER_RADIUS = 4f;
    /** Radius for the Lure hurtbox */
    private static final float LURE_HURT = 1.5f;
    /** Colors of Fire trap */
    private static final Color fireColor = Color.RED;
    /** Colors of slow trap */
    private static final Color slowColor = Color.CYAN;
    /** Colors of lure trap */
    private static final Color lureColor = Color.YELLOW;


    private float lure_ammount=6;
    /** Lure Durability */
    private float LURE_CRUMBS = MAX_DURABILITY / lure_ammount;
    /** Slow effect strength */
    private float SLOW_EFFECT = 0.5f;
    /** Fire duration effect */
    private float FIRE_DUR = 10.0f;

    private float FIRE_DAM_DUR = 5.0f;

    /**
     * Creates a new Trap model with the given physics data
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data  	The physics constants for this trap
     * @param x         The object's screen x location
     * @param y         The object's screen y location
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     * @param t         The Trap.type of this trap
     * @param s         The Trap.shape of this trap
     */
    public Trap(JsonValue data, float x, float y, float width, float height, type t, shape s) {
        super(x, y,
                width * data.get("shrink").getFloat(0),
                height * data.get("shrink").getFloat(1));
        setBodyType(BodyDef.BodyType.StaticBody);
        setFixedRotation(true);
        this.data = data;
        setName("trap");
        trapType = t;
        trapShape = s;
        sensorName = "trapSensor";
        setSensor(true);
        durability = MAX_DURABILITY;
        linger = false;
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
            case FIRE_LINGER:
                return FIRE_DAM_DUR;
        }
        return -1;
    }

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
     * Returns the name tag of the trap
     *
     * This is used by ContactListener
     *
     * @return the name tag of the trap
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *  Returns the enum type that represents this trap
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
    public boolean getLinger(){ return linger; }

    /**
     * Sets the trapType of this trap.
     *
     * @param t is the type to switch to.
     */
    public void setTrapType(type t){ trapType=t;}

    /**
     * Sets the physics object tag.
     *
     * A tag is a string attached to an object, in order to identify it in debugging.
     *
     * @param  value    the physics object tag
     */
    public void setName(String value) {
        name = value;
    }

    /** enables or disables the linger effect for fire traps
     *
     * @param b is true if enabling, and false if disabling.
     */
    public void enableLinger(boolean b){linger = b;}


    /** Decrements the durability of the trap, and returns true if the durability is then zero.
     *
     *  @return true if durability is now zero
     */
    public boolean decrementDurability(){
        switch (trapType){
            case LURE: durability = Math.max(0, durability - LURE_CRUMBS);
                break;
            case FIRE: durability = 0; //FIRE transitions into FIRE_LINGER
                break;
            case SLOW:
                durability = Math.max(0,--durability);
                break;

        }

        return durability == 0;
    }
    /**Whether the trap is still active or not*/
    public boolean isActive(){
        return durability > 0;
    }
    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param delta	Number of seconds since last animation frame
     */
    @Override
    public void update(float delta, int[] plist) {

        super.update(delta, plist);
        if (trapType == type.FIRE_LINGER) {
            durability = durability - (MAX_DURABILITY / FIRE_DUR * delta);
            if (durability <= 0) {
                this.markRemoved(true);
            }
        }

        //TODO: delete after technical
        float next_val = 0f;
        float diff = 0f;
        switch (trapType){
            case LURE:
                next_val = plist[5];
                diff = next_val - lure_ammount;
                durability += diff * LURE_CRUMBS;
                lure_ammount = next_val;
            break;
            case SLOW:
                next_val = plist[6];
                diff = next_val - MAX_DURABILITY;
                durability += diff;
                MAX_DURABILITY = next_val;
            break;
            case FIRE_LINGER:
                next_val = plist[7];
                diff = next_val - FIRE_DUR;
                durability = MathUtils.clamp(durability+ MAX_DURABILITY*diff * delta, 0, MAX_DURABILITY);
                FIRE_DUR = next_val;
                FIRE_DAM_DUR = plist[8];
            break;
        }


    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
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
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        switch (trapType) {
            case LURE:
                sensorShape.setRadius(LURE_RADIUS);
                FixtureDef sensHurt = new FixtureDef();
                sensHurt.isSensor = true;
                lHShape= new CircleShape();
                lHShape.setRadius(LURE_HURT);
                sensHurt.shape = lHShape;
                Fixture sensorHurtF = body.createFixture(sensHurt);
                sensorHurtF.setUserData("lureHurt");
                break;
            case SLOW:
                sensorShape.setRadius(SLOW_RADIUS);
                break;
            case FIRE:
                sensorShape.setRadius(FIRE_TRIGGER_RADIUS);
                break;
            case FIRE_LINGER:
                sensorShape.setRadius(FIRE_LINGER_RADIUS);

        }
        sensorDef.shape = sensorShape;
        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());
        return true;
    }


    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        Color c = fireColor.cpy();
        switch (trapType){
            case FIRE: c = fireColor.cpy();
            break;
            case LURE: c = lureColor.cpy();
            break;
            case SLOW: c = slowColor.cpy();
            break;
        }
        c.a = durability/MAX_DURABILITY;
        canvas.draw(texture, c, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), .1f, .1f);
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
        switch (trapShape) {
            case CIRCLE:
                canvas.drawPhysics((CircleShape) sensorShape,Color.RED,getX(),getY(),drawScale.x,drawScale.y);
                if (lHShape != null) {
                    canvas.drawPhysics((CircleShape) lHShape,Color.BLUE,getX(),getY(),drawScale.x,drawScale.y);
                }

                break;
            case SQUARE:
                canvas.drawPhysics((PolygonShape) sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
                break;

        }
    }
}
