package code.game.models;

import code.game.interfaces.StoveInterface;
import code.game.views.GameCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;

public class Slap extends GameObject {

    private JsonValue data;

    private PolygonShape sensorShape;
    private float offset;
    private float angvel;
    private float ofratio;
    private float width;
    private float height;

    //TODO: add comment
    public Slap(JsonValue jv, float x, float y, float width, float height, int direction) {
        super(x, y, width, height);
        setFixedRotation(true);
        data = jv;
        setName("bullet");
        setSensorName("slapSensor");
        setSensor(true);
        setDensity(0);
        offset = data.getFloat("offset",0);
        angvel = data.getFloat("angvel",0);
        ofratio = data.getFloat("ofratio",0);

        if (direction == 2 || direction == 4) {
            this.width = width/2.0f;
            this.height = height;
            offset *= (direction == 2 ? 1 : -1);
            setX(x + offset);
            setY(y - offset*ofratio);
            setAngle((float)(-1*Math.PI/24));
            setAngularVelocity(angvel);
        } else {
            this.width = width;
            this.height = height/2.0f;
            offset *= (direction == 1 ? 1 : -1);
            setY(y + offset);
            setX(x - offset*ofratio);
            setAngle((float)(1*Math.PI/24));
            setAngularVelocity(-1*angvel);
        }

        Filter bulletFilter = new Filter();
        bulletFilter.groupIndex = -1;
        bulletFilter.categoryBits = 0x0002;
        setFilterData(bulletFilter);
        setBullet(true);
        setGravityScale(0);
    }

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
        // Ground Sensor
        // -------------
        // Previously used to detect double-jumps, but also allows us to see hitboxes
        Vector2 sensorCenter = new Vector2(0,0);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(width, height, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        return true;
    }

    /**
     * Draws the unlit stove
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),2,2);
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
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }
}
