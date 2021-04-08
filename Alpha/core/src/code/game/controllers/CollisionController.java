package code.game.controllers;

import code.game.interfaces.CollisionControllerInterface;
import code.game.models.Chef;
import code.game.models.Chicken;
import code.game.models.Stove;
import code.game.models.Trap;
import code.game.models.obstacle.Obstacle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;

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

            if(fd1 != null && fd2 != null) {
                handleCollision(bd1, fd1, bd2, fd2);
            }
            //handleCollision(bd2, fd2, bd1, fd1);

            } catch(Exception e){
                e.printStackTrace();
            }
        }

        private void handleCollision(Obstacle bd1, Object fd1, Obstacle bd2, Object fd2){
            //special platform case
            if (bd1.getName().contains("platform") && bd2.getName().equals("chicken")){
                ((Chicken)bd2).hitWall();
            }
            else if (bd2.getName().contains("platform") && bd1.getName().equals("chicken")){
                ((Chicken)bd2).hitWall();
            }

            else {
                //otherwise check collisions between objects
                switch (fd1.toString()) {
                    case "chickenSensor":
                        chickenCollision((Chicken) bd1, fd1, bd2, fd2);
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

        private void chickenCollision(Chicken c1, Object fd1, Obstacle bd2, Object fd2){
            switch(fd2.toString()){
                case "stove": c1.hitWall();
                    break;
                case "chicken":
                    break;
                case "chefSensor": handleChefChicken((Chef)bd2, fd2, c1, fd1);
                    break;
                case "slapSensor": handleChickenSlap(c1, fd1, bd2, fd2);
                    break;
                case "trap": handleChickenTrap(c1, fd1, (Trap)bd2, fd2);
                    break;
            }
        }

        private void chefCollision(Chef c1, Object fd1, Obstacle bd2, Object fd2){
            switch(bd2.getName()) {
                case "stove":
                    handleStoveChef((Stove) bd2, c1);
                    break;
                case "chicken":
                    handleChefChicken(c1, fd1, (Chicken) bd2, fd2);
                    break;
                case "trapSpot":
                    c1.setCanPlaceTrap(true);
                    break;
                case "chefSensor":
                case "slapSensor":
                case "trap":
                    break;
            }
        }

        private void stoveCollision(Stove s1, Object fd1, Obstacle bd2, Object fd2){
            switch(bd2.getName()){
                case "stove":
                    break;
                case "chicken": ((Chicken)bd2).hitWall();
                    break;
                case "chefSensor": handleStoveChef(s1, (Chef)bd2);
                    break;
                case "slapSensor":
                case "trap":
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
        chef.setCanCook(true);
    }

    /**
     * Handles an interaction between a given chef and a chicken
     * @param chef
     * @param fd1
     * @param chicken
     * @param fd2
     */
    private void handleChefChicken(Chef chef, Object fd1, Chicken chicken, Object fd2){
        if(fd2 != null) {
            if (fd2.equals("chickenSensor")) {
                if (chicken.chasingPlayer()) {
                    chicken.startAttack();
                }
            } else if (fd2.equals("nugAttack")) {
                //TODO fix call to parameter?? should check
                chef.decrementHealth();
                chicken.hitPlayer();
            }
        }
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
    public void endContact(Contact contact) {
        //TODO: Detect if collision is with an enemy and give appropriate interaction (if any needed)
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        Obstacle b1 = (Obstacle) bd1;
        Obstacle b2 = (Obstacle) bd2;



    }
}