package code.game.models;

import code.game.views.GameCanvas;
import code.util.FilmStrip;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;

public class ChickenAttack extends GameObject {

    public enum AttackType {
        Basic,
        Charge,
        Knockback,
        Projectile,
        Explosion,
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
    /** The rate at which the buffalo's speed increases */
    private final float CHARGE_RATE = 1.006f;

    /** The maximum degrees that a buffalo's charge can change */
    private final float MAX_ANGLE = 0.6f;

    /** The radius of the sensor of a basic attack */
    private final float BASIC_RADIUS = 0.5f;
    /** The duration of a basic attack */
    private final float ATTACK_DUR = 0.2f;
    /** The width of chicken attacks */
    private static final float WIDTH = 0.1f;
    /** The height of chicken attacks */
    private static final float HEIGHT = 0.1f;
    /** The width of chicken knockback attacks */
    private static final float KNOCKWIDTH = 160.0f;
    /** The height of chicken knockback attacks */
    private static final float KNOCKHEIGHT = 50.0f;
    /** The width of an explosion */
    private final float EXP_RADIUS = 2.0f;

    /** The attack's destination */
    private Vector2 destination;
    /** Time passed since creation of attack */
    private float age;
    /** Whether this attack should be removed */
    private boolean remove;
    /** The current speed of the attack */
    private float speed;

    /** The maximum length of time a projectile attack can exist */
    private final float PROJECTILE_MAX_AGE = 2f;
    /** Speed of projectiles */
    private final float PROJECTILE_SPEED = 6f;
    /** The starting speed of a charge attack */
    private final float CHARGE_SPEED = 4.0f;

    /** The buffalo's starting position */
    private Vector2 origin_vector;
    /** The buffalo's starting velocity */
    private Vector2 origin_velocity;
    /** The point representing the "end" of the line that contains the origin_vector
     * and curr_vector. This point should be unreachable. */
    private Vector2 end_vector;
    /** The current radius of the buffalo's charge */
    private float charge_radius;
    /** A point that is on the line that contains origin_vector and the vector
     * representing the buffalo's initial charge direction. This point is also
     * on the circumference of a circle that is charge_radius distance away
     * from origin_vector. */
    private Vector2 curr_vector;
    /** Whether the attack has been reflected */
    private boolean reflected = false;
    /** Filmstrips for egg animations */
    private FilmStrip eggSpin;
    private FilmStrip eggSplat;
    /** Origin positions of egg textures */
    Vector2 originSpin = new Vector2();
    Vector2 originSplat = new Vector2();
    /** Speed of egg animations */
    private static float egg_animation_speed = 0.15f;
    /** Sped of egg breaking animation */
    private static float egg_break_speed = 0.025f;
    /** Current animation frame for the egg */
    private float animeframe = 0;
    /** whether the projectile egg is breaking (used for egg animation purposes) */
    private boolean breaking = false;

    /** Creates an instance of a basic attack */
    public ChickenAttack(float x, float y, float width, float height, Chef chef, Chicken chicken, AttackType type) {
        super(x, y, width, height, ObjectType.ATTACK);
        this.type = type;
        this.chicken = chicken;
        this.target = new Vector2(chicken.getDestination());
        origin_vector = new Vector2(chicken.getPosition());
        setName("chickenAttack");
        setDensity(0f);
        Filter filter;
        switch(type) {
            case Basic:
                setPosition(getVector(chicken.getPosition(),target));
                destination = getPosition();
                filter = new Filter();
                filter.categoryBits = 0x0008;
                filter.maskBits = -1;
                filter.groupIndex = -1;
                setFilterData(filter);
                break;
            case Charge:
                setPosition(getVector(chicken.getPosition(),target));
                destination = target;
                chicken.setDestination(new Vector2(destination));
                setLinearVelocity((new Vector2(destination)).sub(chicken.getPosition()).nor().scl(CHARGE_SPEED));
                origin_velocity = new Vector2(getLinearVelocity());
                charge_radius = distance(destination.x, destination.y, getX(), getY());
                speed = CHARGE_SPEED;
                end_vector = destination.sub(chicken.getPosition()).scl(100f);
                filter = new Filter();
                filter.categoryBits = 0x0010;
                filter.maskBits = 0x0001 | 0x0004;
                setFilterData(filter);
                break;
            case Projectile:
                setPosition(getVector(chicken.getPosition(),target));
                destination = chicken.target.getPosition();
                setLinearVelocity(destination.sub(chicken.getPosition()).nor().scl(PROJECTILE_SPEED)); // move towards destination
                setSensor(true); // Set sensor to avoid projectile bouncing off of chicken body
                //texture = ((ShreddedChicken)chicken).getProjectileTexture();
                break;
            case Explosion:
                destination = getPosition();
                break;
            case Knockback:
                //float angle =(MathUtils.atan2(chicken.getY()-chef.getY(),chicken.getX()-chef.getX()));
                //setAngle(angle);
                setSensor(true);
                this.origin = new Vector2(0,0);//new Vector2(width/2.0f,height/2.0f);
                //setX(x-25*width*MathUtils.cos(angle));
                //setY(y-25*width*MathUtils.sin(angle));
                destination = getPosition();
                filter = new Filter();
                filter.categoryBits = 0x0008;
                filter.maskBits = -1;
                filter.groupIndex = -1;
                setFilterData(filter);
                break;
        }
    }

    /** Returns a vector representing this attack's next linear velocity
     *
     */
    public Vector2 updateLinearVelocity() {
        speed *= CHARGE_RATE;
        target = chicken.getDestination();
        charge_radius = distance(origin_vector.x, origin_vector.y, chicken.getX(), chicken.getY());
        Vector2 destination = getVector(origin_vector, target);
        // If the chef is in front of us
        if (destination != target) {
            curr_vector = getVector(origin_vector, end_vector);
            Vector2 destination_nor = new Vector2(destination).sub(origin_vector).nor();
            curr_vector.sub(origin_vector).nor();
            // Angle change between origin vector and the chef's location
            float origin_change = MathUtils.atan2(destination_nor.y, destination_nor.x) -
                    MathUtils.atan2(curr_vector.y, curr_vector.x);
            // Is the angle change less than how much the buffalo is able to turn?
            if ((Math.abs(origin_change) <= MAX_ANGLE)) {
                setLinearVelocity((target.sub(chicken.getPosition())).nor().scl(speed));
            } else {
                setLinearVelocity(getLinearVelocity().nor().scl(speed));
            }
        } else {
            setLinearVelocity(getLinearVelocity().nor().scl(speed));
        }
        return getLinearVelocity();
    }

    /** Returns the width of chicken attacks
     *
     * @return WIDTH
     * */
    public static float getWIDTH() { return WIDTH; }

    /** Returns the height of chicken attacks
     *
     * @return HEIGHT
     * */
    public static float getHEIGHT() { return HEIGHT; }

    /** Returns the width of chicken knockback attacks
     *
     * @return KNOCKWIDTH
     * */
    public static float getKNOCKWIDTH() { return KNOCKWIDTH; }

    /** Returns the height of chicken knockback attacks
     *
     * @return KNOCKHEIGHT
     * */
    public static float getKNOCKHEIGHT() { return KNOCKHEIGHT; }

    /** Returns the type of the chicken attack
     *
     * @return attack type
     */
    public AttackType getType(){ return type;}

    public void collideObject(Chicken chicken) {
        if (type == AttackType.Charge && chicken != this.chicken) {
            collideObject();
        }
    }

    public void collideObject() {
        if (type == AttackType.Charge) {
            chicken.setStopped(true);
            chicken.interruptAttack();
            remove = true;
        }
        else if (type == AttackType.Knockback || type == AttackType.Basic){
            remove = true;
        }
        if (type == AttackType.Projectile && reflected){remove = true;}
        //if (type == AttackType.Projectile) { remove = true; } // Delete projectile after colliding with something
    }

    /**
     * Reflect the projectile in the opposite direction after getting slapped
     * @param chefPos   position of chef
     */
    public void reflect(Vector2 chefPos){
        if (!breaking) {
            age = 0;
            setLinearVelocity(getPosition().sub(chefPos).nor().scl(PROJECTILE_SPEED)); // move away from chef
            reflected = true;
        }
    }

    /** Get whether the attack has been reflected */
    public boolean isReflected(){return reflected;}

    /** Returns whether the destination has been reached
     *
     * @return position within range of destination
     */
    public boolean atDestination(float dt) {
        age += dt;
        if (type == AttackType.Basic || type == AttackType.Knockback) { return true; }
        if ((type==AttackType.Projectile && age> PROJECTILE_MAX_AGE) || remove) { return true; }
        if (type!=AttackType.Charge && distance(getX(), getY(), destination.x, destination.y) < 0.5f && age > ATTACK_DUR) {
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
    private Vector2 getVector(Vector2 start, Vector2 destination) {
        float dist;
        if(type == AttackType.Projectile) { return chicken.getPosition(); } // Fire from center of chicken
        if (type == AttackType.Charge) {
            if (charge_radius > 0) { dist = charge_radius;}
            else { dist = CHARGE_DIST; }
        }
        else { dist = BASIC_DIST; }
        if (distance(start.x, start.y, destination.x, destination.y) < dist) {
            return destination;
        } else {
            Vector2 vector = new Vector2();
            float slope = (start.y-destination.y)/(start.x-destination.x);
            float intercept = start.y - (slope*start.x);
            float a = (float)(1 + Math.pow(slope, 2));
            float b = 2*(-start.x+((-start.y + intercept)*slope));
            float c = (float)(Math.pow(start.x,2) + Math.pow(-start.y + intercept,2)
                    - Math.pow(dist,2));
            float disc = (float)Math.sqrt(Math.pow(b,2)-(4*a*c));
            float root1 = (-b+disc)/(2*a);
            float root2 = (-b-disc)/(2*a);
            float y1 = (slope*root1) + intercept;
            float y2 = (slope*root2) + intercept;
            if (distance(root1, y1, destination.x, destination.y) <
            distance(root2, y2, destination.x, destination.y)) {
                vector.x = root1;
                vector.y = y1;
            } else {
                vector.x = root2;
                vector.y = y2;
            }
            return vector;
        }
    }

    /** Returns the number of degrees between the given coordinate
     * and its corresponding coordinate in curr_vector
     *
     * @param point the point in question
     * @param x     whether the point is an X coordinate
     * @return number of degrees between point and corresponding point in origin_vector
     */
    private float getDegrees(float point, float curr, boolean x) {
        //S = coefficient of sin
        //C = coefficient of cos
        //o = corresponding origin coordinate
        // a,b,c = coefficients for the quadratic formula
        float S, C, a, b, c, o;
        if (x) { o = origin_vector.x; point -= o; S = -(curr-o); C = (curr-o);}
        else { o = origin_vector.y; point -= o; S=(curr-o); C = (curr-o);}
        a = -(S*S) - (C*C);
        b = -(2*point*S);
        c = -((float)Math.pow(point-o,2)) + (C*C);

        float disc = (float)Math.sqrt(b*b - 4*a*c);
        float root1 = (-b+disc)/(2*a);
        float root2 = (-b-disc)/(2*a);

        if (Math.abs(root1) < Math.abs(root2)) { return root1; }
        else { return root2; }
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
        if (type == AttackType.Basic) {
            sensorFixture.setUserData(FixtureType.BASIC_ATTACK);
        } else if (type == AttackType.Charge) {
            sensorFixture.setUserData(FixtureType.CHARGE_ATTACK);
        } else if (type == AttackType.Explosion) {
            sensorFixture.setUserData(FixtureType.EXPLOSION_ATTACK);
        } else {
            sensorFixture.setUserData(FixtureType.PROJECTILE_ATTACK);
        }

        return true;
    }


    public void setEggAnimators(Texture spin, Texture splat){
        eggSpin = new FilmStrip(spin, 1, 8);
        eggSplat = new FilmStrip(splat, 1,4);

        originSpin.x = eggSpin.getRegionWidth()/2;
        originSpin.y = eggSpin.getRegionHeight()/2;

        originSplat.x = eggSplat.getRegionWidth()/2;
        originSplat.y = eggSplat.getRegionHeight()/2;
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
                if (!breaking) {
                    eggSpin.setFrame((int) animeframe);
                    canvas.draw(eggSpin, Color.WHITE, originSpin.x, originSpin.y, getX() * drawScale.x, getY() * drawScale.x, getAngle(), displayScale.x*0.25f, displayScale.y*0.25f);
                }
                else{
                    eggSplat.setFrame((int) animeframe);
                    canvas.draw(eggSplat, Color.WHITE, originSplat.x, originSplat.y, getX() * drawScale.x, getY() * drawScale.x, getAngle(), displayScale.x*0.15f, displayScale.y*0.15f);
                }
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

    /**
     * This should only be called for the egg, not other attacks
     * Updates the object's game state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns, and control animations
     *
     * @param dt	Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {
        super.update(dt);
        if (!breaking) {
            animeframe += egg_animation_speed * 4;
            if (animeframe >= 8) {
                animeframe -= 8;
            }
        }
        else{
            animeframe += egg_break_speed * 4;
            if (animeframe >= 4){
                animeframe -= 4;
                remove = true;
            }
        }

    }

    public void beginSplat(){
        if(!breaking) {
            breaking = true;
            animeframe = 0;
            setLinearVelocity(Vector2.Zero);
        }
    }

    public boolean readyToRemove(){
        return remove;
    }

    public boolean isBreaking(){
        return breaking;
    }
}
