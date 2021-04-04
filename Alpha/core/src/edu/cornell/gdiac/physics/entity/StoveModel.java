package edu.cornell.gdiac.physics.entity;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

public class StoveModel extends BoxObstacle {

    private JsonValue data;

    private CircleShape sensorShape;

    private String name;

    private String sensorName;

    /** Whether or not the stove is lit */
    private boolean lit = false;

    /** The font used to draw text on the screen*/
    private static final BitmapFont font = new BitmapFont();

    public String getSensorName() {
        return sensorName;
    }

    @Override
    public String getName() {
        return name;
    }

    public StoveModel(JsonValue jv, float x, float y, float width, float height) {
        super(x, y,
                width * jv.get("shrink").getFloat(0),
                height * jv.get("shrink").getFloat(1));
        setBodyType(BodyDef.BodyType.StaticBody);
        setFixedRotation(true);
        data = jv;
        setName("stove");
        name = "stove";
        sensorName = "cookRadius";
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
        // Ground Sensor
        // -------------
        // Previously used to detect double-jumps, but also allows us to see hitboxes
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        sensorShape.setRadius(3f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());
        return true;
    }

    public void setLit(boolean val){lit = val;}

    /**
     * Draws the unlit stove
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        canvas.draw(texture, (lit ? Color.RED : Color.WHITE),origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),.1f,.1f);
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
}




