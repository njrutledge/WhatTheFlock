package code.game.models;

import code.game.interfaces.TrapInterface;
import code.game.models.obstacle.BoxObstacle;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import code.game.views.GameCanvas;

public class Trap extends GameObject implements TrapInterface {

    /**
     *  Enumeration to encode the trap type
     */
    public enum type {
        LURE,
        SLOW,
        FIRE,
        FIRE_LINGER,
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

    /** The game shape of this object */
    private Shape sensorShape;
    /** true if using the second game shape */
    private boolean linger;
    /** The type of this trap */
    private type trapType;
    /** The game shape of this trap */
    private shape trapShape;

    /** The durability of this trap */
    private float durability;
    /** The max durability */
    private static final float MAX_DURABILITY = 30.0f;

    /** Hurt box shape for the Lure trap. Null for all other traps */
    private Shape lHShape;

    /** Radius which chickens get lured to the trap */
    private static final float LURE_RADIUS = 6f;
    /** Radius which chickens get slowed near the trap */
    private static final float SLOW_RADIUS = 3.5F;
    /** Radius which chickens can trigger the fire trap */
    private static final float FIRE_TRIGGER_RADIUS = 2f;
    /** Radius which chickens get set on fire */
    private static final float FIRE_LINGER_RADIUS = 4f;
    /** Radius for the Lure hurtbox */
    private static final float LURE_HURT = 1.3f;
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
     * Creates a new Trap model with the given game data
     *
     * The size is expressed in game units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the game units to pixels.
     *
     * @param data  	The game constants for this trap
     * @param x         The object's screen x location
     * @param y         The object's screen y location
     * @param width		The object width in game units
     * @param height	The object width in game units
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
        setSensorName("trapSensor");
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
     * Updates the object's game state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param delta	Number of seconds since last animation frame
     */
    @Override
    public void update(float delta) {

        super.update(delta);
        if (trapType == type.FIRE_LINGER) {
            durability = durability - (MAX_DURABILITY / FIRE_DUR * delta);
            if (durability <= 0) {
                this.markRemoved(true);
            }
        }
    }

    /**
     * Creates the game Body(s) for this object, adding them to the world.
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
     * Draws the game object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        Color c = fireColor.cpy();
        switch (trapType) {
            case FIRE:
                c = fireColor.cpy();
                break;
            case LURE:
                c = lureColor.cpy();
                break;
            case SLOW:
                c = slowColor.cpy();
                break;
            case FIRE_LINGER:
                c = Color.FIREBRICK.cpy();
                break;
        }
        c.a = durability / MAX_DURABILITY;
        canvas.draw(texture, c, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), .1f, .1f);
    }

    /**
     * Draws the outline of the game body.
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
