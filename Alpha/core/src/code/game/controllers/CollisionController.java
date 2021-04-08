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
                switch (bd1.getName()) {
                    case "chicken":
                        chickenCollision((Chicken) bd1, fd1, bd2, fd2);
                        break;
                    case "chef":
                        chefCollision((Chef) bd1, fd1, bd2, fd2);
                        break;
                    case "stove":
                        stoveCollision((Stove) bd1, fd1, bd2, fd2);
                        break;
                    case "bullet":
                        slapCollision(bd1, fd1, bd2, fd2);
                        break;
                    case "trap":
                        trapCollision((Trap) bd1, fd1, bd2, fd2);
                        break;
                }
            }
        }

        private void chickenCollision(Chicken c1, Object fd1, Obstacle bd2, Object fd2){
            switch(bd2.getName()){
                case "stove": c1.hitWall();
                    break;
                case "chicken":
                    break;
                case "chef": handleChefChicken((Chef)bd2, fd2, c1, fd1);
                    break;
                case "bullet": handleChickenSlap(c1, fd1, bd2, fd2);
                    break;
                case "trap": handleChickenTrap(c1, fd1, (Trap)bd2, fd2);
                    break;
            }
        }

        private void chefCollision(Chef c1, Object fd1, Obstacle bd2, Object fd2){
            switch(bd2.getName()){
                case "stove": handleStoveChef((Stove)bd2, c1);
                    break;
                case "chicken": handleChefChicken(c1, fd1, (Chicken)bd2, fd2);
                    break;
                case "chef":
                case "bullet":
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
                case "chef": handleStoveChef(s1, (Chef)bd2);
                    s1.setLit(true);
                    break;
                case "bullet":
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
        if(chicken.isAttacking()){
            chef.decrementHealth();
            chicken.hitPlayer();
        }
        else if (chicken.chasingPlayer(chef)){
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

        if(bd1.getName().equals("chef") || bd2.getName().equals("chef")){
            Chef chef = (Chef)(bd1.getName().equals("chef") ? bd1 : bd2);
            if ((chef.getSensorName().equals(fd2) && chef != bd1)
                    ||(chef.getSensorName().equals(fd1) && chef != bd2) ){
                sensorFixtures.remove((chef.equals(bd1)) ? fix2 : fix1);
            }
        }

        switch (bd1.getName()) {
            case "chicken":
                endChickenCollision((Chicken) bd1, fd1, bd2, fd2);
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
        }
    }

    private void endChickenCollision(Chicken c1, Object fd1, Obstacle bd2, Object fd2){
        switch(bd2.getName()){
            case "stove":
                break;
            case "chicken":
                break;
            case "chef": endChickenChef(c1, fd1, (Chef)bd2, fd2);
                break;
            case "bullet":
                break;
            case "trap": endChickenTrap(c1, fd1, (Trap)bd2, fd2);
                break;
        }
    }

    private void endChefCollision(Chef chef, Object fd1, Obstacle bd2, Object fd2){
        switch(bd2.getName()){
            case "stove": endStoveChef((Stove) bd2, fd2, chef, fd1);
                break;
            case "chicken": endChickenChef((Chicken)bd2, fd2, chef, fd1);
                break;
            case "chef":
            case "bullet":
            case "trap":
                break;
        }
    }

    private void endStoveCollision(Stove s1, Object fd1, Obstacle bd2, Object fd2){
        switch(bd2.getName()){
            case "stove":
                break;
            case "chicken":
                break;
            case "chef": endStoveChef(s1, fd1, (Chef)bd2, fd2);
                break;
            case "bullet":
            case "trap":
                break;
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
        chicken.stopAttack();
    }

    private void endStoveChef(Stove stove, Object fd1, Chef chef, Object fd2){
        //if (chef.getSensorName().equals(fd2) && stove.getSensorName().equals(fd1)){
        chef.setCanCook(false);
        stove.setLit(false);
    }


}