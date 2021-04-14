package code.game.models;

import code.game.interfaces.StoveInterface;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import code.game.views.GameCanvas;

public class Stove extends GameObject implements StoveInterface {

    private JsonValue data;

    private CircleShape sensorShape;

    private boolean active = false;

    /** Whether or not the stove is lit */
    private boolean lit = false;

    /** The font used to draw text on the screen*/
    private static final BitmapFont font = new BitmapFont();

    //TODO: add comment
    public Stove(JsonValue jv, float x, float y, float width, float height) {
        super(x, y,
                width * jv.get("shrink").getFloat(0),
                height * jv.get("shrink").getFloat(1), ObjectType.STOVE);
        setBodyType(BodyDef.BodyType.StaticBody);
        setFixedRotation(true);
        data = jv;
        setName("stove");
        //setSensorName("cookRadius");
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
        // Ground Sensor
        // -------------
        // Previously used to detect double-jumps, but also allows us to see hitboxes
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        sensorShape.setRadius(1.6f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(FixtureType.STOVE_SENSOR);//getSensorName());
        return true;
    }

    /** enables or disables the stove lighting.
     *
     * This should be enabled if the chef is cooking, and disabled otherwise.
     *
     * @param val is true to enable, false to disable.
     */
    public void setLit(boolean val){lit = val;}

    /**
     * Sets the stove to active so that the chef can cook from it
     */
    public void setActive() {active = true;}

    /**
     * Sets the stove to inactive so that the chef cannot cook from it
     */
    public void setInactive() {active = false;}

    /**
     * Checks whether the stove is active
     */
    public boolean isActive() {return active;}

    /**
     * Draws the stove
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        canvas.draw(texture, (active ? (lit ? Color.RED : Color.WHITE) : Color.DARK_GRAY),origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),.1f,.1f);
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
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),drawScale.x,drawScale.y);
    }
}




