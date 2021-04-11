package code.game.controllers;

import code.game.interfaces.CollisionControllerInterface;
import code.game.models.*;
import code.game.models.obstacle.Obstacle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;

public class CollisionController implements CollisionControllerInterface {
    /**The damage for this round of contact*/
    private float dmg;

    private TrapController trapController;

    public CollisionController(Vector2 scale, JsonValue constants){
        trapController = new TrapController(scale, constants);
    }
    /**
     * Callback method for the start of a collision
     *
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
        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        //
        try {
            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();
            handleCollision(bd1, fd1, bd2, fd2);
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    private void handleCollision(Obstacle bd1, Object fd1, Obstacle bd2, Object fd2){
        if ((bd1.getName().contains("platform") || bd1.getName().contains("wall"))) {
            if (fd2 != null && fd2.toString().equals("chickenAttackSensor")) {
                //System.out.println("Attack colliding with wall");
                ((ChickenAttack)bd2).collideObject();
            }
        }
        else if ((bd2.getName().contains("platform") || bd2.getName().contains("wall"))){
            if (fd1 != null && fd1.toString().equals("chickenAttackSensor")) {
                //System.out.println("Attack colliding with wall");
                ((ChickenAttack)bd1).collideObject();
            }
        }
        if (fd1 != null && fd2 != null) {
            switch (fd1.toString()) {
                case "chickenSensor":
                    chickenCollision((Chicken) bd1, fd1, bd2, fd2);
                    break;
                case "chickenHitbox":
                    chickenHitboxCollision((Chicken) bd1, fd1, bd2, fd2);
                    break;
                case "chickenAttackSensor":
                    chickenAttackCollision((ChickenAttack) bd1, fd1, bd2, fd2);
                    break;
                case "chefSensor":
                    chefCollision((Chef) bd1, fd1, bd2, fd2);
                    break;
                case "cookRadius":
                    stoveCollision((Stove) bd1, fd1, bd2, fd2);
                    break;
                case "slapSensor":
                    slapCollision(bd1, fd1, bd2, fd2);
                    break;
                case "trap":
                    trapCollision((Trap) bd1, fd1, bd2, fd2);
                    break;
            }
        }

        }

        /** This method handles collision with a chicken's sensor
         *
         * The chicken should produce a response when a relevant target comes into
         * their attack range.
         *
         * @param c1
         * @param fd1
         * @param bd2
         * @param fd2
         * */
        private void chickenCollision(Chicken c1, Object fd1, Obstacle bd2, Object fd2){
            switch(fd2.toString()){
                case "stove":
                case "chicken":
                    break;
                case "chickenAttackSensor": // handleChickenChickenAttack(c1, fd1, (ChickenAttack) bd2, fd2);
                    break;
                case "chefSensor": handleChefChicken((Chef)bd2, fd2, c1, fd1);
                    break;
                case "slapSensor":
                    break;
                case "trap": // handleChickenTrap(c1, fd1, (Trap)bd2, fd2);
                    break;
            }
        }

    /** This method handles collisions with a chicken's hitbox.
     *
     * The chicken should take damage or have status effects applied if their hitbox
     * has been hit by the chef or a trap.
     *
     * @param c1
     * @param fd1
     * @param bd2
     * @param fd2
     */
    private void chickenHitboxCollision(Chicken c1, Object fd1, Obstacle bd2, Object fd2){
        switch(fd2.toString()){
            case "stove":
            case "chicken":
            case "chefSensor":
                break;
            case "chickenAttackSensor": handleChickenChickenAttack(c1, fd1, (ChickenAttack) bd2, fd2);
                break;
            case "slapSensor": handleChickenSlap(c1, fd1, bd2, fd2);
                break;
            case "trap": handleChickenTrap(c1, fd1, (Trap)bd2, fd2);
                break;
        }
    }

    /** This method handles collisions with a chicken's attack.
     *
     * If a chef collides with a chicken's attack, the chef should take damage.
     * If the chicken attack is a buffalo's attack, colliding with anything should
     * cause the buffalo to stop.
     *
     * @param c1
     * @param fd1
     * @param bd2
     * @param fd2
     */
    private void chickenAttackCollision(ChickenAttack c1, Object fd1, Obstacle bd2, Object fd2){
        switch(fd2.toString()){
            case "stove":
            case "slapSensor": handleObstacleChickenAttack(bd2, fd2, c1, fd1);
                break;
            case "chickenHitbox": handleChickenChickenAttack((Chicken) bd2, fd2, c1, fd1);
                break;
            case "chefSensor": handleChefChickenAttack((Chef)bd2, fd2, c1, fd1);
                break;
            case "trap":
                break;
        }
    }

        private void chefCollision(Chef c1, Object fd1, Obstacle bd2, Object fd2){
            switch(fd2.toString()) {
                case "stoveSensor":
                    handleStoveChef((Stove) bd2, c1);
                    break;
                case "chickenSensor":
                    handleChefChicken(c1, fd1, (Chicken) bd2, fd2);
                    break;
                case "placeRadius":
                    c1.setCanPlaceTrap(true);
                    break;
                case "chickenAttackSensor": handleChefChickenAttack(c1, fd1, (ChickenAttack)bd2, fd2);
                case "chefSensor":
                case "slapSensor":
                case "trap":
                    break;
            }
        }

        private void stoveCollision(Stove s1, Object fd1, Obstacle bd2, Object fd2){
            switch(bd2.getName()){
                case "slapSensor":
                case "trap":
                case "chicken":
                    break;
                case "chefSensor":
                    handleStoveChef(s1, (Chef)bd2);
                    break;
                case "chickenAttack":
                    handleObstacleChickenAttack(s1, fd1, (ChickenAttack) bd2, fd2);
                break;
            }
    }


        private void slapCollision(Obstacle bd1, Object fd1, Obstacle bd2, Object fd2){
        //TODO make slap class
            if(bd2.getName().equals("chicken")){
                handleChickenSlap((Chicken)bd2, fd2, bd1, fd1);
            }
        }

        private void trapCollision(Trap t1, Object fd1, Obstacle bd2, Object fd2){
            if(bd2.getName().equals("chicken")){
                handleChickenTrap((Chicken)bd2, fd2, t1, fd1);
            }
        }

    /****************************************
     * HELPER METHODS
     ****************************************/
    /**
     * Handles an interaction between a stove and a chef
     * @param stove     a given stove
     * @param chef      a chef
     */
    private void handleStoveChef(Stove stove, Chef chef){
        //System.out.println("Cooking");
        chef.setCooking(true);
        chef.setMovement(0);
        chef.setVertMovement(0);
        stove.setLit(true);
    }

    /**
     * Handles an interaction between a given chef and a chicken
     * @param chef
     * @param fd1
     * @param chicken
     * @param fd2
     */
    private void handleChefChicken(Chef chef, Object fd1, Chicken chicken, Object fd2){
        if (chicken.chasingPlayer(chef)){
            chicken.startAttack();
        }
        //else {chicken.startAttack();}
        /*
        if(fd2 != null) {
            if (fd2.equals("chickenSensor")) {
                if (chicken.chasingPlayer()) {
                    chicken.startAttack();
                }
            } else if (fd2.equals("nugAttack") && chicken.isAttacking()) {
                //TODO fix call to parameter?? should check
                chef.decrementHealth();
                chicken.hitPlayer();
            }
        }*/
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
        //System.out.println("Chicken attack colliding with obstacle " + fd1.toString());
        attack.collideObject();
    }

    /** Handles an interaction between a chicken and a chicken attack */
    private void handleChickenChickenAttack(Chicken c1, Object fd1, ChickenAttack attack, Object fd2) {
        attack.collideObject(c1);
    }

    /**
     * Handles an interaction between a chicken and a slap
     * @param c1
     * @param fd1
     * @param bd2
     * @param fd2
     */
    private void handleChickenSlap(Chicken c1, Object fd1, Obstacle bd2, Object fd2){
        c1.takeDamage(dmg);
        if(!c1.isAlive()){
            c1.markRemoved(true);
        }
    }

    /**
     * Handles an interaction between a chicken and a trap
     * @param c1
     * @param fd1
     * @param t2
     * @param fd2
     */
    private void handleChickenTrap(Chicken c1, Object fd1, Trap t2, Object fd2){
        trapController.applyTrap(t2, c1);
    }

    /**
     * Callback method for the start of a collision
     *
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
        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Obstacle bd1 = (Obstacle) body1.getUserData();
        Obstacle bd2 = (Obstacle) body2.getUserData();
        if (bd1.getName() != "wall" && bd2.getName() != "wall") {
            if (bd1.getName() != "trap" && bd2.getName() != "trap") {
                //System.out.print(bd1.getName() + "'s " + (fd1 != null ? fd1.toString() : ""));
                //System.out.println(" ending contact with " + bd2.getName() + "'s " + (fd2 != null ? fd2.toString() : ""));
            }
        }

        if(bd1.getName().equals("chef") || bd2.getName().equals("chef")){
            Chef chef = (Chef)(bd1.getName().equals("chef") ? bd1 : bd2);
            if ((chef.getSensorName().equals(fd2) && chef != bd1)
                    ||(chef.getSensorName().equals(fd1) && chef != bd2) ){
                sensorFixtures.remove((chef.equals(bd1)) ? fix2 : fix1);
            }
        }

/*        switch (bd1.getName()) {
            case "chicken":
                endChickenCollision((Chicken) bd1, fd1, bd2, fd2);
                break;
            case "chickenAttack":
                endChickenAttackCollision((ChickenAttack) bd1, fd1, bd2, fd2);
                break;
            case "chef":
                endChefCollision((Chef) bd1, fd1, bd2, fd2);
                break;
            case "stove":
                endStoveCollision((Stove) bd1, fd1, bd2, fd2);
                break;
            case "bullet":
                endSlapCollision(bd1, fd1, bd2, fd2);
                break;
            case "trap":
                endTrapCollision((Trap) bd1, fd1, bd2, fd2);
                break;
        }*/
        if (fd1 != null && fd2 != null) {
            switch (fd1.toString()) {
                case "chickenSensor": endChickenSensorCollision((Chicken)bd1, fd1, bd2, fd2); break;
                case "chefSensor": endChefSensorCollision((Chef)bd1, fd1, bd2, fd2); break;
                case "cookRadius": endStoveSensorCollision((Stove)bd1, fd1, bd2, fd2); break;
                case "default": break;
            }
        }
    }

    private void endChickenSensorCollision(Chicken c1, Object fd1, Obstacle bd2, Object fd2){
/*        switch(bd2.getName()){
            case "stove":
            case "chicken":
            case "chickenAttackSensor":
            case "bullet":
                break;
            case "chef": endChickenChef(c1, fd1, (Chef)bd2, fd2);
                break;
            case "trap": endChickenTrap(c1, fd1, (Trap)bd2, fd2);
                break;
        }*/
        switch(fd2.toString()) {
            case "chefSensor": endChickenChef(c1, fd1, (Chef)bd2, fd2);
            case "default": break;
        }
    }

    private void endChefSensorCollision(Chef chef, Object fd1, Obstacle bd2, Object fd2){
/*        switch(bd2.getName()){
            case "stove": endStoveChef((Stove) bd2, fd2, chef, fd1);
                break;
            case "chicken": endChickenChef((Chicken)bd2, fd2, chef, fd1);
                break;
            case "chickenAttackSensor":
            case "chef":
            case "bullet":
            case "trap":
                break;
        }*/
        switch (fd2.toString()) {
            case "chickenSensor": endChickenChef((Chicken)bd2, fd2, chef, fd1); break;
            case "cookRadius": endStoveChef((Stove)bd2, fd2, chef, fd1); break;
            case "default": break;
        }
    }

    private void endStoveSensorCollision(Stove s1, Object fd1, Obstacle bd2, Object fd2){
/*        switch(bd2.getName()){
            case "stove":
            case "chicken":
            case "chickenAttackSensor":
            case "bullet":
            case "trap":
                break;
            case "chef": endStoveChef(s1, fd1, (Chef)bd2, fd2);
                break;
        }*/
        switch(fd2.toString()) {
            case "chefSensor": endStoveChef(s1, fd1, (Chef)bd2, fd2); break;
            case "default": break;
        }
    }


    private void endSlapCollision(Obstacle bd1, Object fd1, Obstacle bd2, Object fd2){
        //TODO make slap class
        if(bd2.getName().equals("chicken")){

        }
    }

    private void endTrapCollision(Trap t1, Object fd1, Obstacle bd2, Object fd2){
        if(bd2.getName().equals("chicken")){
            endChickenTrap((Chicken)bd2, fd2, t1, fd1);
        }
    }

    /**
     * Handles the end of an interaction between a chicken and a trap
     * @param c1
     * @param fd1
     * @param t2
     * @param fd2
     */
    private void endChickenTrap(Chicken c1, Object fd1, Trap t2, Object fd2){
        //trapController.applyTrap(t2, c1);
    }

    private void endChickenChef(Chicken chicken, Object fd1, Chef chef, Object fd2){
        chicken.stopAttack(false);
    }

    private void endStoveChef(Stove stove, Object fd1, Chef chef, Object fd2){
        //if (chef.getSensorName().equals(fd2) && stove.getSensorName().equals(fd1)){
        chef.setCooking(false);
        stove.setLit(false);
    }


}