package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

public class Trap extends BoxObstacle {

    private JsonValue data;

    private Shape sensorShape;

    public enum type {
        TRAP_ONE,
        TRAP_TWO,
        TRAP_THREE
    }

    public enum shape {
        CIRCLE,
        SQUARE

    }

    private type trapType;

    private String sensorName;

    private shape trapShape;

    private String name;

    public Trap(JsonValue data, float x, float y, float width, float height, type t, shape s) {
        super(x, y,
                width * data.get("shrink").getFloat(0),
                height * data.get("shrink").getFloat(1));
        setBodyType(BodyDef.BodyType.StaticBody);
        setFixedRotation(true);
        this.data = data;
        name = "trap";
        trapType = t;
        trapShape = s;
        sensorName = "trapSensor";
        setSensor(true);
    }

    public String getSensorName() {
        return sensorName;
    }

    @Override
    public String getName() {
        return name;
    }

    public type getTrapType() {
        return trapType;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        Vector2 sensorCenter = new Vector2();
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.isSensor = true;
        JsonValue sensorjv = data.get("sensor");
        switch (trapShape) {
            case CIRCLE:
                sensorShape = new CircleShape();
                sensorShape.setRadius(sensorjv.getFloat("radius", 0));
                break;
            case SQUARE:
                sensorShape = new PolygonShape();
                ((PolygonShape) sensorShape).setAsBox(sensorjv.getFloat("height", 0),
                        sensorjv.getFloat("height", 0), sensorCenter, 0.0f);
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
        canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.y, getAngle(), .1f, .1f);
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
                canvas.drawPhysics((CircleShape) sensorShape,Color.RED,10,9);
                break;
            case SQUARE:
                canvas.drawPhysics((PolygonShape) sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
                break;

        }
    }
}
