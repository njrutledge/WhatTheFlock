package code.game.models;

import code.game.models.obstacle.BoxObstacle;
import code.game.views.GameCanvas;
import com.badlogic.gdx.physics.box2d.World;

public abstract class GameObject extends BoxObstacle {
    /** Identifier to allow us to track the sensor in ContactListener */
    //private String sensorName;
    /** A name for debugging purposes */
    private String name;
    /** The type of Object */
    private ObjectType type;

    /**
     * Represents the type of GameObject
     */
    public enum ObjectType{
        CHICKEN,
        CHEF,
        STOVE,
        SLAP,
        TRAP,
        PLACE,
        ATTACK
    }
    /**
     * Represents the type of Fixture
     */
    public enum FixtureType{
        //traps
        TRAP_SENSOR, TRAP_ACTIVATION, LURE_HURT,
        //attacks
        BASIC_ATTACK,
        //other
        CHEF_SENSOR, CHICKEN_SENSOR, SLAP_SENSOR, STOVE_SENSOR, PLACE_SENSOR, CHICKEN_HITBOX
    }

    public GameObject(float x, float y, float width, float height, ObjectType o){
        super(x,y,width,height);
        type = o;
    }

    /**
     * Returns the name of the ground sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    /*public String getSensorName() {
        return sensorName;
    }*/

    public ObjectType getObjectType(){
        return type;
    }
    /**
     * Returns the name tag of the trap
     *
     * This is used by ContactListener
     *
     * @return the name tag of the trap
     */
    public String getName() {
        return name;
    }

    /*public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }*/

    /**
     * Sets the game object tag.
     *
     * A tag is a string attached to an object, in order to identify it in debugging.
     *
     * @param  value    the game object tag
     */
    public void setName(String value) {
        name = value;
    }

    public abstract void draw(GameCanvas canvas);

    public boolean activatePhysics(World world){ return super.activatePhysics(world); }

    public void drawDebug(GameCanvas canvas){
        super.drawDebug(canvas);
    }
}

