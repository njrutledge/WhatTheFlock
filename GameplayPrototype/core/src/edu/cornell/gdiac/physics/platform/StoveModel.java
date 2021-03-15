package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

public class StoveModel extends BoxObstacle {

    private JsonValue data;

    private PolygonShape sensorShape;

    private String name;

    private String sensorName;

    private int temperature;

    private final float TEMPERATURE_TIMER = 1f;

    private final int MAX_TEMPERATURE = 30;

    private float temperature_counter = 0f;

    /** The font used to draw text on the screen*/
    private static final BitmapFont font = new BitmapFont();

    public int getTemperature() {
        return temperature;
    }

    public String getSensorName() {
        return sensorName;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isCooked(){
        return (temperature >= MAX_TEMPERATURE);
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

    @Override
    public void update(float delta) {
        super.update(delta);
        temperature_counter = MathUtils.clamp(temperature_counter += delta, 0f, TEMPERATURE_TIMER);
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }
        // Ground Sensor
        // -------------
        // Previously used to detect double-jumps, but also allows us to see hitboxes
        Vector2 sensorCenter = new Vector2(0, 0);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        JsonValue sensorjv = data.get("sensor");
        sensorShape.setAsBox(sensorjv.getFloat("height", 0),
                sensorjv.getFloat("height", 0), sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(getSensorName());
        return true;
    }


    public void cook(boolean incr){
        if (temperature_counter >= TEMPERATURE_TIMER){
            temperature = Math.max(incr ? temperature+3 : temperature-1,0);
            temperature_counter = 0f;
        }
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),.1f,.1f);
        canvas.drawText("Temp: "+temperature, font, 500,565);
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
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }
}




