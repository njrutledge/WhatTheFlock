package code.game.controllers;

import code.game.models.Chef;
import code.game.models.Chicken;
import code.game.models.Stove;
import code.game.models.Trap;
import code.game.models.obstacle.Obstacle;
import com.badlogic.gdx.physics.box2d.*;

public class CollisionController {
    public CollisionController(){

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

        try {
            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();

            //reduce health if chicken collides with avatar
            //cook if player is near stove and not doing anything
            if ((bd1.getName().equals("chef") && bd2.getName().equals("stove"))
                    || (bd2.getName().equals("chef") && bd1.getName().equals("stove"))) {
                if (bd1.getName().equals("chef")) {
                    handleCollision((Chef) bd1, (Stove) bd2);
                } else {
                    handleCollision((Chef) bd2, (Stove) bd1);
                }
            }

            //bullet collision with chicken eliminates chicken

            if (fd1 != null) {

                if (bd2.getName().equals("bullet") && fd1.equals("chickenSensor")) {
                    /*chickHurt.stop();
                    chickHurt.play(volume);
                    fireSound.stop();
                    fireSound.play(volume);*/
                    handleSlapCollision((Chicken) bd1, potential_dmg);
                    //ChickenModel chick = (ChickenModel) bd1;

                }
                /*
                if (bd2 == avatar && fd1.equals("chickenSensor")) {
                    ChickenModel chick = (ChickenModel) bd1;
                    if (chick.chasingPlayer()) {
                        chick.startAttack();
                    }

                }

                if ((bd2 == avatar && fd1.equals("nugAttack"))&& !((ChickenModel)bd1).isAttacking()){
                    if (!avatar.isStunned()) {
                        chefOof.stop();
                        chefOof.play(volume);
                    }
                    if (parameterList[12] != 1) { avatar.decrementHealth(); }
                    ((ChickenModel) bd1).hitPlayer();


                }*/


            }

            if (fd2 != null) {
                /*
                if (bd1.getName().equals("bullet") && fd2.equals("chickenSensor")) {
                    chickHurt.stop();
                    chickHurt.play(volume);
                    fireSound.stop();
                    fireSound.play(volume);
                    ChickenModel chick = (ChickenModel) bd2;
                    chick.takeDamage(damageCalc());
                    if (!chick.isAlive()) {
                        removeChicken(bd2);
                    }
                }

                if (bd1 == avatar && fd2.equals("chickenSensor")) {
                    ChickenModel chick = (ChickenModel) bd2;
                    if (chick.chasingPlayer()) {
                        chick.startAttack();
                    }
                }

                if (bd1 == avatar && fd2.equals("nugAttack") && ((ChickenModel)bd2).isAttacking()) {
                    if (!avatar.isStunned()) {
                        chefOof.stop();
                        chefOof.play(volume);
                    }
                    if (parameterList[12] != 1) { avatar.decrementHealth(); }
                    ((ChickenModel) bd2).hitPlayer();
                }*/
            }

            //trap collision with chicken eliminates chicken
            if (fd1 != null && fd2 != null) {
                /*
                if (fd1.equals("lureHurt") && fd2.equals("chickenSensor")) {
                    ((ChickenModel) bd2).startAttack();
                }

                if (fd2.equals("lureHurt") && fd1.equals("chickenSensor")) {
                    ((ChickenModel) bd1).startAttack();
                }

                if (fd1.equals("lureHurt") && fd2.equals("nugAttack") && ((ChickenModel) bd2).isAttacking()) {
                    decrementTrap((Trap) bd1);
                    lureCrumb.play(volume);
                }

                if (fd2.equals("lureHurt") && fd1.equals("nugAttack") && ((ChickenModel) bd1).isAttacking()){
                    decrementTrap((Trap) bd2);
                    lureCrumb.play(volume);
                }

                if (fd1.equals("trapSensor") && bd2.getName().equals("chicken")) {
                    switch (((Trap) bd1).getTrapType()){
                        case LURE: //damage
                            ((ChickenModel) bd2).trapTarget((Trap) bd1);
                            break;
                        case SLOW:
                            ((ChickenModel) bd2).applySlow(((Trap) bd1).getEffect());
                            decrementTrap((Trap) bd1);
                            slowSquelch.stop();
                            slowSquelch.play(volume);
                            break;
                        case FIRE :
                            float twidth = trapTexture.getRegionWidth()/scale.x;
                            float theight = trapTexture.getRegionHeight()/scale.y;
                            trapCache = new Trap(constants.get("trap"), bd1.getX(), bd1.getY(), twidth, theight, Trap.type.FIRE_LINGER, Trap.shape.CIRCLE);
                            trapCache.setDrawScale(scale);
                            trapCache.setTexture(trapTexture);
                            addQueuedObject(trapCache);
                            decrementTrap((Trap) bd1);
                            fireTrig.play(volume);
                            fireLinger.play(volume*0.5f);
                            break;
                        case FIRE_LINGER:
                            ((ChickenModel) bd2).applyFire(((Trap) bd1).getEffect());
                            chickOnFire.stop();
                            chickOnFire.play(volume*0.5f);
                    }*/
                }
                if (fd2.equals("trapSensor") && bd1.getName().equals("chicken")) {
                    /*switch (((Trap) bd2).getTrapType()){
                        case LURE: //damage
                            ((ChickenModel) bd1).trapTarget((Trap) bd2);
                            break;
                        case SLOW:
                            ((ChickenModel) bd1).applySlow(((Trap) bd2).getEffect());
                            decrementTrap((Trap) bd2);
                            slowSquelch.stop();
                            slowSquelch.play(volume);
                            break;
                        case FIRE:
                            float twidth = trapTexture.getRegionWidth()/scale.x;
                            float theight = trapTexture.getRegionHeight()/scale.y;
                            trapCache = new Trap(constants.get("trap"), bd2.getX(), bd2.getY(), twidth, theight, Trap.type.FIRE_LINGER, Trap.shape.CIRCLE);
                            trapCache.setDrawScale(scale);
                            trapCache.setTexture(trapTexture);
                            addQueuedObject(trapCache);
                            decrementTrap((Trap) bd2);
                            fireTrig.play(volume);
                            fireLinger.play(volume*0.5f);
                            break;
                        case FIRE_LINGER:
                            ((ChickenModel) bd1).applyFire(((Trap) bd2).getEffect());
                            chickOnFire.stop();
                            chickOnFire.play(volume*0.5f);
                            break;
                    }*/
                }
                /*
                if (fd1.equals("placeRadius") && bd2==avatar){
                    avatar.setCanPlaceTrap(true);
                }
                if (fd2.equals("placeRadius") && bd1==avatar){
                    avatar.setCanPlaceTrap(true);
                }*/
            /*
            if ((bd1.getName().contains("platform")|| (bd1.getName().equals("stove") && !fix1.isSensor())) && bd2.getName().equals("chicken")){
                ((ChickenModel)bd2).hitWall();
            }
            if ((bd2.getName().contains("platform") || (bd2.getName().equals("stove") && !fix2.isSensor())) && bd1.getName().equals("chicken")){
                ((ChickenModel)bd1).hitWall();

            }*/

            } catch(Exception e){
                e.printStackTrace();
            }
        }


    /**
     * Activates ability to cook when the chef is near the stove
     * @param avatar
     * @param stove
     */
    private void handleCollision(Chef avatar, Stove stove){
        avatar.setCanCook(true);
    }
    /**
     * Activates ability to cook when the chef is near the stove
     * @param avatar
     * @param chick
     */
    private void handleCollision(Chef avatar, Chicken chick){

    }

    private void handleSlapCollision(Chicken chick, float dmg){
        chick.takeDamage(dmg);
        if (!chick.isAlive()) {
            chick.markRemoved(true); //was originally removeChicken(), idk if this breaks anything
        }
    }

    private void handleCollision(Chicken chick, Trap trap){

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
        /*Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        Obstacle b1 = (Obstacle) bd1;
        Obstacle b2 = (Obstacle) bd2;

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove((avatar == bd1) ? fix2 : fix1);
        }

        if (avatar.getSensorName().equals(fd2) && stove.getSensorName().equals(fd1) ||
                avatar.getSensorName().equals(fd1) && stove.getSensorName().equals(fd2)){
            avatar.setCanCook(false);
        }
        if (b1.getName().equals("trap") && b2.getName().equals("chicken")) {
            switch (((Trap) b1).getTrapType()){
                case LURE:
                    ((ChickenModel) b2).resetTarget();
                    break;
                case SLOW:
                    ((ChickenModel) b2).removeSlow();
                    break;
                case FIRE :
                    break;
                case FIRE_LINGER:
                    ((ChickenModel) b2).letItBurn();
            }
        }
        if (b2.getName().equals("trap") && b1.getName().equals("chicken")) {
            switch (((Trap) b2).getTrapType()){
                case LURE: //damage
                    ((ChickenModel) b1).resetTarget();
                    break;
                case SLOW:
                    ((ChickenModel) b1).removeSlow();
                    break;
                case FIRE :
                    break;
                case FIRE_LINGER:
                    ((ChickenModel) b1).letItBurn();
            }
        }

        if (fd1 != null && fd2 != null) {
            if (fd1.equals("lureHurt") && fd2.equals("chickenSensor")) {
                ((ChickenModel) bd2).stopAttack();
            }

            if (fd2.equals("lureHurt") && fd1.equals("chickenSensor")) {
                ((ChickenModel) bd1).stopAttack();
            }

            if (fd1.equals("placeRadius") && bd2 == avatar) {
                avatar.setCanPlaceTrap(false);
            }
            if (fd2.equals("placeRadius") && bd1 == avatar) {
                avatar.setCanPlaceTrap(false);
            }
        }

        if (fd1 != null) {
            if (bd2 == avatar && fd1.equals("chickenSensor")){
                ((ChickenModel) bd1).stopAttack();
            }
        }

        if (fd2 != null) {
            if (bd1 == avatar && fd2.equals("chickenSensor")) {
                ((ChickenModel) bd2).stopAttack();
            }
        }*/
    }
}