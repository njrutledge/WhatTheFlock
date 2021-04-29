package code.game.models;

import code.game.models.obstacle.BoxObstacle;
import code.game.views.GameCanvas;
import code.util.FilmStrip;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;

public class DinoChicken extends NuggetChicken{

    /** The initializing data (to avoid magic numbers) */
    private JsonValue data;

    /** Time before the next dino smash*/
    private float smashTime;
    /** Cooldown between each smash*/
    private float smashCD = 5;

    /**
     * Creates a new chicken avatar with the given physics data
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * converts the physics units to pixels.
     *
     * @param data  	The physics constants for all chickens
     * @param unique    The unique physics constants for nuggets
     * @param x         The x axis location of this chicken
     * @param y         The y axis location of this chicken
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     * @param player    The target player
     * @param mh        The max health of the chicken
     */
    public DinoChicken(JsonValue Jdata, JsonValue unique, float x, float y, float width, float height, Chef player, int mh) {
        // The shrink factors fit the image to a tighter hitbox
        super(Jdata, unique, x, y, width, height, player, mh);
        data = Jdata;
        smashTime = 0;
    }

    @Override
    public void setTexture(Texture texture) {
        animator = new FilmStrip(texture, 1, 11);
        origin = new Vector2(animator.getRegionWidth()/2.0f, animator.getRegionHeight()/2.0f);
    }

    /**
     * Creates the physics Body(s) for this object, adding them to the world.
     *
     * This method overrides the base method to keep your ship from spinning.
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
        Vector2 sensorCenter = new Vector2(0, -getHeight()*1.25f);
        FixtureDef hitboxDef = new FixtureDef();
        hitboxDef.density = data.getFloat("density",0);
        hitboxDef.isSensor = true;
        hitboxShape = new PolygonShape();
        hitboxShape.setAsBox(getWidth(), getHeight()/2, sensorCenter, 0);
        hitboxDef.shape = hitboxShape;
        Fixture hitboxFixture = body.createFixture(hitboxDef);
        hitboxFixture.setUserData(FixtureType.CHICKEN_HURTBOX);

        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        sensorShape.setRadius(sensorRadius);
        sensorDef.shape = sensorShape;
        // Ground sensor to represent our feet
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(FixtureType.CHICKEN_HITBOX);//getSensorName());


        return true;
    }

    /**
     * Updates the object's game state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns, and control animations
     *
     * @param dt	Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {
        smashTime += dt;
        //Every smashCD seconds the dino smashes the ground, knocking away chickens/chef
        if (smashTime >= smashCD){
            smashTime = 0;
            System.out.println("smash");
        }

        super.update(dt);
    }


}
