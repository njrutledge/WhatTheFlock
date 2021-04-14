package code.game.controllers;

import code.game.interfaces.CollisionControllerInterface;
import code.game.models.*;
import code.game.models.GameObject;
import code.game.models.obstacle.Obstacle;
import code.util.PooledList;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import code.game.models.GameObject.ObjectType;
import code.game.models.GameObject.FixtureType;

public class CollisionController implements CollisionControllerInterface {
    /**The damage for this round of contact*/
    private float dmg;

    private TrapController trapController;

    public PooledList<Trap> trapCache = new PooledList<Trap>();

    private Chef chef;

    public CollisionController(Vector2 scale) {
        trapController = new TrapController(scale);
    }

    /**
     * Sets the constants parameter of trapController
     * @param constants the jsonValue to be set
     */
    public void setConstants(JsonValue constants) {
        trapController.setConstants(constants);
    }

    /**
     * Sets the current chef
     *
     * @param c the Chef
     */
    public void setChef(Chef c) {
        chef = c;
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact, float potential_dmg) {
        dmg = potential_dmg;
        //TODO: Detect if a collision is with an enemy and have an appropriate interaction
        //hitbox
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();
        //game body
        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();
        //the object
        FixtureType fd1 = (FixtureType) fix1.getUserData();
        FixtureType fd2 = (FixtureType) fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();

            //process GameObject collisions
            if (bd1 instanceof GameObject && bd2 instanceof GameObject) {
                handleCollision((GameObject) bd1, fd1, fix1, (GameObject) bd2, fd2, fix2);
            } else {
                //process Charge Attack collision with walls
                if (bd1.getName().contains("wall") || bd2.getName().contains("wall")) {
                    if (fd1 != null && fd1 == FixtureType.CHARGE_ATTACK) {
                        ((ChickenAttack) bd1).collideObject();
                    } else if (fd2 != null && fd2 == FixtureType.CHARGE_ATTACK) {
                        ((ChickenAttack) bd2).collideObject();
                    }
                    else if (fd1 != null && fd1.equals(FixtureType.LURE_HURT)) {
                        bd1.setLinearVelocity(Vector2.Zero);
                    } else if (fd2 != null && fd2.equals(FixtureType.LURE_HURT)) {
                        bd2.setLinearVelocity(Vector2.Zero);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the collision based on the type of GameObjects passed in.
     */
    private void handleCollision(GameObject bd1, FixtureType fd1, Fixture fix1,
                                 GameObject bd2, FixtureType fd2, Fixture fix2) {
        switch (bd1.getObjectType()) {
            case CHICKEN:
                chickenCollision((Chicken) bd1, fd1, fix1, bd2, fd2, fix2);
                break;
            case CHEF:
                chefCollision((Chef) bd1, fd1, fix1, bd2, fd2, fix2);
                break;
            case STOVE:
                stoveCollision((Stove) bd1, fd1, bd2, fd2);
                break;
            case SLAP:
                slapCollision((Slap) bd1, fd1, bd2, fd2);
                break;
            case TRAP:
                trapCollision((Trap) bd1, fd1, bd2, fd2);
                break;
        }
    }

    /**
     * Handles a collision between a Chicken and another GameObject
     *
     * @param c1
     * @param fd1
     * @param fix1
     * @param bd2
     * @param fd2
     * @param fix2
     */
    private void chickenCollision(Chicken c1, FixtureType fd1, Fixture fix1, GameObject bd2, FixtureType fd2, Fixture fix2) {
        switch (bd2.getObjectType()) {
            case CHEF:
                handleChefChicken((Chef) bd2, fd2, c1, fd1);
                break;
            case SLAP:
                handleChickenSlap(c1, fd1, bd2, fd2);
                break;
            case TRAP:
                handleChickenTrap(c1, fd1, (Trap) bd2, fd2);
                break;
        }
    }

    /**
     * Handles a collision between a Chef and another Object
     *
     * @param c1
     * @param fd1
     * @param fix1
     * @param bd2
     * @param fd2
     * @param fix2
     */
    private void chefCollision(Chef c1, FixtureType fd1, Fixture fix1, GameObject bd2, FixtureType fd2, Fixture fix2) {
        switch (bd2.getObjectType()) {
            case STOVE:
                handleStoveChef((Stove) bd2, c1);
                break;
            case CHICKEN:
                handleChefChicken(c1, fd1, (Chicken) bd2, fd2);
                break;
            case PLACE:
                c1.setCanPlaceTrap(true);
                break;
            case ATTACK:
                handleChefChickenAttack(c1, fd1, (ChickenAttack) bd2, fd2);
                break;
        }
    }

    private void stoveCollision(Stove s1, FixtureType fd1, GameObject bd2, FixtureType fd2) {
        if (bd2.getObjectType().equals(ObjectType.CHEF)) {
            handleStoveChef(s1, (Chef) bd2);
        }
    }

        private void slapCollision(Slap s1, FixtureType fd1, GameObject bd2, FixtureType fd2) {
            if (fd2 != null) {
                switch (fd2) {
                    case CHICKEN_HURTBOX:
                        handleChickenSlap((Chicken) bd2, fd2, s1, fd1);
                        break;
                    case TRAP_ACTIVATION:
                        handleTrapSlap((Trap) bd2, fd2, s1, fd1);
                        break;
                }
            }
        }


    private void trapCollision(Trap t1, FixtureType fd1, GameObject bd2, FixtureType fd2) {
        if (bd2.getObjectType().equals(ObjectType.SLAP)) {
            handleTrapSlap(t1, fd1, (Slap) bd2, fd2);
        }

        if (bd2.getObjectType().equals(ObjectType.CHICKEN)) {
            handleChickenTrap((Chicken) bd2, fd2, t1, fd1);
        }
    }

    /****************************************
     * HELPER METHODS
     ****************************************/
    /**
     * Handles an interaction between a stove and a chef
     *
     * @param stove a given stove
     * @param chef  a chef
     */
    private void handleStoveChef(Stove stove, Chef chef) {
        chef.setCooking(true, stove);
        chef.setMovement(0);
        chef.setVertMovement(0);
        stove.setLit(true);

    }

    private void handleTrapSlap(Trap t1, FixtureType fd1, Slap s2, FixtureType fd2) {
        if(fd1!=null && fd1.equals(FixtureType.TRAP_ACTIVATION)) {
            switch (t1.getTrapType()) {
                case FAULTY_OVEN:
                    chef.setDoubleDamage(true);
                    break;
                case BREAD_BOMB:
                    t1.markReady(false);
                    trapCache.addAll(trapController.createLures(t1));
                    break;
                case FRIDGE:
                    t1.markReady(false);
                    trapCache.add(trapController.createSlow(t1));
                    break;
            }
        }
    }

    /**
     * Handles an interaction between a given chef and a chicken
     *
     * @param chef
     * @param fd1
     * @param chicken
     * @param fd2
     */
    private void handleChefChicken(Chef chef, FixtureType fd1, Chicken chicken, FixtureType fd2){
        //TODO: why are we passing in the fixture itself when fd1 and fd2 are already the user datas?
        if (fd2 == FixtureType.CHICKEN_HITBOX && chicken.chasingPlayer(chef)){
            chicken.startAttack();
        }
    }

    /**
     * Handles an interaction between a given chef and a chicken attack
     */
    private void handleChefChickenAttack(Chef chef, Object fd1, ChickenAttack attack, Object fd2){
        chef.decrementHealth();
        attack.collideObject();
    }

    /**
     * Handles an interaction between a non-chef obstacle and a chicken attack
     */
    private void handleObstacleChickenAttack(Obstacle obstacle, Object fd1, ChickenAttack attack, Object fd2){
        attack.collideObject();
    }

    /** Handles an interaction between a chicken and a chicken attack */
    private void handleChickenChickenAttack(Chicken c1, Object fd1, ChickenAttack attack, Object fd2) {
        attack.collideObject(c1);
    }

    /**
     * Handles an interaction between a chicken and a slap
     *
     * @param c1
     * @param fd1
     * @param bd2
     * @param fd2
     */
    private void handleChickenSlap(Chicken c1, FixtureType fd1, GameObject bd2, FixtureType fd2) {
        c1.takeDamage(dmg);
        if (!c1.isAlive()) {
            c1.markRemoved(true);
        }
    }

    /**
     * Handles an interaction between a chicken and a trap
     *
     * @param c1
     * @param fd1
     * @param t2
     * @param fd2
     */
    private void handleChickenTrap(Chicken c1, FixtureType fd1, Trap t2, FixtureType fd2) {
        if(fd1 != null && fd1.equals(FixtureType.CHICKEN_HURTBOX)){
            trapController.applyTrap(t2, c1);
        }
        if(t2.getTrapType().equals(Trap.type.LURE) && fd2 != null && fd2.equals(FixtureType.LURE_HURT)){
            t2.markHit();
        }
    }

    /**
     * Gets all the new traps that need to be added
     * @return a PooledList of traps
     */
    public PooledList<Trap> getNewTraps(){
        return trapCache;
    }

    /****************************************
     * END COLLISION
     ****************************************/

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact, ObjectSet<Fixture> sensorFixtures) {
        //TODO: Detect if collision is with an enemy and give appropriate interaction (if any needed)
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();
        //game body
        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();
        //the object
        FixtureType fd1 = (FixtureType) fix1.getUserData();
        FixtureType fd2 = (FixtureType) fix2.getUserData();

        Obstacle bd1 = (Obstacle) body1.getUserData();
        Obstacle bd2 = (Obstacle) body2.getUserData();

        //TODO: what does this do...?
        /*if(bd1.getName().equals("chef") || bd2.getName().equals("chef")){
            Chef chef = (Chef)(bd1.getName().equals("chef") ? bd1 : bd2);
            if ((chef.getSensorName().equals(fd2) && chef != bd1)
                    ||(chef.getSensorName().equals(fd1) && chef != bd2) ){
                sensorFixtures.remove((chef.equals(bd1)) ? fix2 : fix1);
            }
        }*/
        //process collisions between GameObjects
        if (bd1 instanceof GameObject && bd2 instanceof GameObject) {
            processEndContact((GameObject) bd1, fd1, (GameObject) bd2, fd2);
        }

    }

    private void processEndContact(GameObject bd1, FixtureType fd1, GameObject bd2, FixtureType fd2) {
        switch (bd1.getObjectType()) {
            case CHICKEN:
                endChickenCollision((Chicken) bd1, fd1, bd2, fd2);
                break;
            case CHEF:
                endChefCollision((Chef) bd1, fd1, bd2, fd2);
                break;
            case STOVE:
                endStoveCollision((Stove) bd1, fd1, bd2, fd2);
                break;
            case SLAP:
                endSlapCollision(bd1, fd1, bd2, fd2);
                break;
            case TRAP:
                endTrapCollision((Trap) bd1, fd1, bd2, fd2);
                break;
        }
    }

    private void endChickenCollision(Chicken c1, FixtureType fd1, GameObject bd2, FixtureType fd2) {
        switch (bd2.getObjectType()) {
            case CHEF:
                endChickenChef(c1, fd1, (Chef) bd2, fd2);
                break;
            case TRAP:
                if(fd2!= null && fd2.equals(FixtureType.TRAP_HITBOX)) {
                    endChickenTrap(c1, fd1, (Trap) bd2, fd2);
                }
                break;
        }
    }

    private void endChefCollision(Chef chef, FixtureType fd1, GameObject bd2, FixtureType fd2) {
        switch (bd2.getObjectType()) {
            case STOVE:
                endStoveChef((Stove) bd2, fd2, chef, fd1);
                break;
            case CHICKEN:
                endChickenChef((Chicken) bd2, fd2, chef, fd1);
                break;
        }
    }

    private void endStoveCollision(Stove s1, FixtureType fd1, GameObject bd2, FixtureType fd2) {
        switch (bd2.getObjectType()) {
            case CHEF:
                endStoveChef(s1, fd1, (Chef) bd2, fd2);
                break;
        }
    }


    private void endSlapCollision(GameObject bd1, FixtureType fd1, GameObject bd2, FixtureType fd2) {
        //TODO make slap class
        if (bd2.getObjectType().equals(ObjectType.CHICKEN)) {
        }
    }

    private void endTrapCollision(Trap t1, FixtureType fd1, GameObject bd2, FixtureType fd2) {
        if (bd2.getObjectType().equals(ObjectType.CHICKEN) && fd1!= null && fd1.equals(FixtureType.TRAP_HITBOX)) {
            endChickenTrap((Chicken) bd2, fd2, t1, fd1);
        }
    }

    /**
     * Handles the end of an interaction between a chicken and a trap
     *
     * @param c1
     * @param fd1
     * @param t2
     * @param fd2
     */
    private void endChickenTrap(Chicken c1, FixtureType fd1, Trap t2, FixtureType fd2) {
        trapController.stopTrap(t2, c1);
        if(t2.getTrapType().equals(Trap.type.LURE) && fd2 != null && fd2.equals(FixtureType.LURE_HURT)){
            t2.removeHit();
        }

    }

    private void endChickenChef(Chicken chicken, FixtureType fd1, Chef chef, FixtureType fd2){
        if (fd1 == FixtureType.CHICKEN_HITBOX) {
            chicken.stopAttack(false);
        }
    }

    private void endStoveChef(Stove stove, FixtureType fd1, Chef chef, FixtureType fd2) {
        //if (chef.getSensorName().equals(fd2) && stove.getSensorName().equals(fd1)){
        chef.setCooking(false, stove);
        chef.setInCookingRange(false);
        stove.setLit(false);
    }

    public void setTrapAssets(TextureRegion trapFridgeTexture, TextureRegion trapSlowTexture, TextureRegion trapDefaultTexture) {
        trapController.setTrapAssets(trapFridgeTexture, trapSlowTexture, trapDefaultTexture);
    }
}