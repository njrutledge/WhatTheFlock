package code.game.models;

import code.game.views.GameCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;

public class ChickenAttack extends GameObject {

    public enum AttackType {
        Basic,
        Charge,
        Projectile,
        Explosion
    }

    private JsonValue data;
    private Chicken chicken;
    /** The position of the target when the chicken began charging */
    private Vector2 target;
    private AttackType type;
    private CircleShape sensorShape;

    /** The maximum distance away from the chicken that a basic attack can be created */
    private final float BASIC_DIST = 1.5f;
    /** The maximum distance away from the chicken that a charge attack can be created */
    private final float CHARGE_DIST = 0.5f;
    /** The distance between this attack's destination and the actual target's position */
    private final float OVEREXTEND_DIST = 2.5f;

    /** The radius of the sensor of a basic attack */
    private final float BASIC_RADIUS = 0.5f;
    /** The duration of a basic attack */
    private final float ATTACK_DUR = 0.2f;
    /** The width of chicken attacks */
    private static final float WIDTH = 0.1f;
    /** The height of chicken attacks */
    private static final float HEIGHT = 0.1f;
    /** The width of an explosion */
    private final float EXP_RADIUS = 2.0f;

    /** The attack's destination */
    private Vector2 destination;
    /** Time passed since creation of attack */
    private float age;
    /** Whether this attack should be removed */
    private boolean remove;
    /** The maximum length of time a projectile attack can exist */
    private final float PROJECTILE_MAX_AGE = 2f;
    /** Speed of projectiles */
    private final float PROJECTILE_SPEED = 8f;

    /** Creates an instance of a basic attack */
    public ChickenAttack(float x, float y, float width, float height, Chef chef, Chicken chicken, AttackType type) {
        super(x, y, width, height, ObjectType.ATTACK);
        this.type = type;
        this.chicken = chicken;
        this.target = new Vector2(chicken.getDestination());
        setName("chickenAttack");
        //setSensorName("chickenAttackSensor");
        setDensity(0f);
        Filter filter;
        switch(type) {
            case Basic:
                setPosition(getVector(false));
                destination = getPosition();
                filter = new Filter();
                filter.categoryBits = 0x0008;
                filter.maskBits = -1;
                setFilterData(filter);
                break;
            case Charge:
                setPosition(getVector(false));
                destination = getVector(true);
                chicken.setDestination(new Vector2(destination));
                setLinearVelocity(new Vector2(destination).sub(chicken.getPosition()));
                filter = new Filter();
                filter.categoryBits = 0x0016;
                filter.maskBits = 0x0001 | 0x0004;
                setFilterData(filter);
                break;
            case Projectile:
                setPosition(getVector(false));
                destination = chicken.target.getPosition();
                setLinearVelocity(destination.sub(chicken.getPosition()).nor().scl(PROJECTILE_SPEED)); // move towards destination
                setSensor(true); // Set sensor to avoid projectile bouncing off of chicken body
                texture = ((ShreddedChicken)chicken).getProjectileTexture();
                break;
            case Explosion:
                destination = getPosition();
                break;
        }
    }

    /** Lets the chicken that this attack belongs to know that the player was hit */
    public void hitPlayer() { chicken.setStopped(true); }

    /** Returns the width of chicken attacks
     *
     * @return WIDTH
     * */
    public static float getWIDTH() { return WIDTH; }

    /** Returns the width of chicken attacks
     *
     * @return WIDTH
     * */
    public static float getHEIGHT() { return HEIGHT; }

    public void collideObject(Chicken chicken) {
        if (type == AttackType.Charge && chicken != this.chicken) {

            collideObject();
        }
    }

    public void collideObject() {
        if (type == AttackType.Charge) {  chicken.setStopped(true); chicken.interruptAttack(); remove = true; }
        //if (type == AttackType.Projectile) { remove = true; } // Delete projectile after colliding with something
    }

    /** Returns whether the destination has been reached
     *
     * @return position within range of destination
     */
    public boolean atDestination(float dt) {
        age += dt;
        if ((type==AttackType.Projectile && age> PROJECTILE_MAX_AGE) || remove) { return true; }
        if (distance(getX(), getY(), destination.x, destination.y) < 0.5f && age > ATTACK_DUR) {
            if (type == AttackType.Charge) {
                //setLinearVelocity(destination.setZero());
                remove = true;
                chicken.interruptAttack(); }
            return true;
        }
        return false;
    }

    /** Returns the distance between the points (x1, y1) and (x2, y2)
     *
     * @param x1 X coordinate of first point
     * @param x2 X coordinate of second point
     * @param y1 Y coordinate of first point
     * @param y2 Y coordinate of second point
     * @return  The distance between the points (x1, y1) and (x2, y2)
     */
    private float distance(float x1, float y1, float x2, float y2) {
        float xdiff = x2-x1;
        float ydiff = y2-y1;
        return (float)Math.sqrt(Math.pow(xdiff,2) + Math.pow(ydiff,2));
    }

    /** Returns the vector that the ChickenAttack will be spawned on.
     *
     * The computed vector will be on the path to the target, but within
     * CHIC_RADIUS distance away from the chicken.
     *
     * @return (x,y) of ChickenAttack
     * */
    private Vector2 getVector(boolean over_extend) {
        float dist;
        if(type == AttackType.Projectile) { return chicken.getPosition(); } // Fire from center of chicken
        if (over_extend) {
            dist = distance(chicken.getX(), chicken.getY(), target.x, target.y) + OVEREXTEND_DIST;
        }
        else if (type == AttackType.Charge) { dist = CHARGE_DIST; }
        else { dist = BASIC_DIST; }
        if (!over_extend && distance(chicken.getX(), chicken.getY(), target.x, target.y) < dist) {
            return target;
        } else {
            Vector2 vector = new Vector2();
            float slope = (chicken.getY()-target.y)/(chicken.getX()-target.x);
            float intercept = chicken.getY() - (slope*chicken.getX());
            float a = (float)(1 + Math.pow(slope, 2));
            float b = 2*(-chicken.getX()+((-chicken.getY() + intercept)*slope));
            float c = (float)(Math.pow(chicken.getX(),2) + Math.pow(-chicken.getY() + intercept,2)
                    - Math.pow(dist,2));
            float disc = (float)Math.sqrt(Math.pow(b,2)-(4*a*c));
            float root1 = (-b+disc)/(2*a);
            float root2 = (-b-disc)/(2*a);
            float y1 = (slope*root1) + intercept;
            float y2 = (slope*root2) + intercept;
            if (distance(root1, y1, target.x, target.y) <
            distance(root2, y2, target.x, target.y)) {
                vector.x = root1;
                vector.y = y1;
            } else {
                vector.x = root2;
                vector.y = y2;
            }
            return vector;
        }
    }

    /** Sets the position 
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
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        sensorShape.setRadius(BASIC_RADIUS);
        sensorDef.shape = sensorShape;

        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorFixture.setUserData(FixtureType.BASIC_ATTACK);

        return true;
    }

    /**
     * Set the texture for this attack
     * @param texture   the texture for this attack
     */
    public void setTexture(TextureRegion texture){
        this.texture = texture;
        origin.x = texture.getRegionWidth()/2.0f;
        origin.y = texture.getRegionHeight()/2.0f;
    }

    /**
     * Draws the unlit stove
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        switch (type) {
            case Basic:
                break;
            case Projectile:
                canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x - 50, getY() * drawScale.x - 60, getAngle(), 0.25f, 0.25f);
                break;
            case Explosion:
                //canvas.draw(texture, Color.WHITE, origin.x, origin.y, getX() * drawScale.x, getY() * drawScale.x, getAngle(), 2, 2);
                break;
        }
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
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),drawScale.x,drawScale.y);
    }
}
