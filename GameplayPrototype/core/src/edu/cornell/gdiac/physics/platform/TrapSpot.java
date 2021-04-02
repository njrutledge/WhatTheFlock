package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

public class TrapSpot extends BoxObstacle {

    //private JsonValue data;

    private CircleShape sensorShape;

    private String name;

    private String sensorName;

    private boolean hasTrap;
    /** the trap at this spot */
    private Trap trap;
    /** The font used to draw text on the screen*/
    private static final BitmapFont font = new BitmapFont();

    public String getSensorName() {
        return sensorName;
    }

    @Override
    public String getName() {
        return name;
    }

    /**Place where the player can put a trap*/
    public TrapSpot(float x, float y) {
        //set constants manually for the constructor
        //TODO change
        super(x, y, 2, 2);
        setBodyType(BodyDef.BodyType.StaticBody);
        setFixedRotation(true);
        //data = jv;
        setName("place");
        name = "place";
        sensorName = "placeRadius";
        hasTrap = false;
        trap = null;
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


    /** places the trap t at this location */
    public void placeTrap(Trap t){
        if (!hasTrap){
            trap = t;
            hasTrap = true;
        }
    }

    /**updates this TrapSpot if needed, based on trap expiry status*/
    public void update(){
        if(hasTrap && !trap.isActive()){
            hasTrap = false;
            trap = null;
        }
    }

    /** checks whether the spot is empty */
    public boolean isEmpty(){return hasTrap;}


    /** whether you can place a trap here, based on the specified location @param x and @param y */
    public boolean canPlace(float x, float y) {
        //check within bounds
        if (getX() <= x && x <= getX() + getWidth() && isEmpty()){
            if(getY() <= y && y <= getY() + getHeight()){
                hasTrap = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Draws the trap area
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        canvas.drawShape(sensorShape,Color.RED,getX(),getY(),drawScale.x,drawScale.y);
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
