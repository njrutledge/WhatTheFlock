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
        FIRE
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
    /** The type of this trap */
    private type trapType;
    /** The physics shape of this trap */
    private shape trapShape;
    /** A name for debugging purposes */
    private String name;
    /** The durability of this trap */
    private float durability;
    /** The max durability */
    private static final float MAX_DURABILITY = 30.0f;


    private static final float LURE_RADIUS = 5f;

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
     * Sets the physics object tag.
     *
     * A tag is a string attached to an object, in order to identify it in debugging.
     *
     * @param  value    the physics object tag
     */
    public void setName(String value) {
        name = value;
    }


    /** Decrements the durability of the trap, and returns true if the durability is then zero.
     *
     *  @return true if durability is now zero
     */
    public boolean decrementDurability(){
        durability = Math.max(0,--durability);
        return durability == 0;
    }
    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param delta	Number of seconds since last animation frame
     */
    @Override
    public void update(float delta) {
        super.update(delta);
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

        Vector2 sensorCenter = new Vector2();
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.isSensor = true;
        JsonValue sensorjv = data.get("sensor");
        switch (trapType) {
            case LURE:
                sensorShape = new CircleShape();
                sensorShape.setRadius(sensorjv.getFloat("radius", 0));
                break;
            case SLOW:
                sensorShape = new PolygonShape();
                ((PolygonShape) sensorShape).setAsBox(sensorjv.getFloat("height", 0),
                        sensorjv.getFloat("height", 0), sensorCenter, 0.0f);
                break;
            case FIRE:

                break;
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
        Color c = new Color(255,255,255,durability/MAX_DURABILITY);
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
                break;
            case SQUARE:
                canvas.drawPhysics((PolygonShape) sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
                break;

        }
    }
}
