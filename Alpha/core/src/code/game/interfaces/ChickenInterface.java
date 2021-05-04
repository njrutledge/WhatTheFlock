package code.game.interfaces;

import code.game.models.Chef;
import code.game.models.GameObject;
import code.game.models.Trap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public interface ChickenInterface {

    /**
     * Returns current chicken max health.
     *
     * @return the current chicken max health.
     */
    public int getMaxHealth();

    /**
     * Sets the current chicken max health
     * @param h - the number to set the max health of the chicken to
     *
     */
    public void setMaxHealth(int h);

    /**
     * Applies the force to the body of this chicken
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce();

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt);

    public boolean getSoundCheck();

    public void startAttack();

    public void stopAttack();

    public boolean isAttacking();

    public boolean chasingObject(GameObject player);

    public void setChaseSpeed(float spd);

    public void setTexture(Texture texture);

    public void setBarTexture(TextureRegion texture);

    /**
     * The chicken takes damage
     *
     * @param damage The amount of damage to this chicken's health
     */
    public void takeDamage(float damage);


    /**
     * Applies a slowdown modifier to the chicken's speed
     *
     * @param strength a slowdown multiplier (1f for normal speed)
     */
    public void applySlow(float strength);

    /**
     * Removes any slowdown modifiers to the chicken's speed
     */
    public void removeSlow(float strength);

    /**
     * Applies the fire effect by giving the chicken a countdown timer
     * representing the remaining time of the fire effect
     *
     * @param duration a duration for the fire effect in seconds.
     */
//    public void applyFire(float duration);
//
//    public void letItBurn();

    /**
     * Sets the chicken's target to the specific Lure trap
     *
     * @param t a Lure trap target
     */
    public void trapTarget(Trap t);

    /**
     * Resets the chicken's target to the player
     *
     */
    public void resetTarget();

    /**
     * updates the isStunned condition for the chicken
     * updates the isStunned condition for the chicken
     *
     * @param stun  whether the chicken is stunned
     */
    public void setStunned(boolean stun);

    /** If the enemy is still alive
     * @return true if chicken health > 0*/
    public boolean isAlive();

    /**
     * The chicken has collided with the player and will remain stationary for some time
     */
    public void setStopped(boolean stopped);

    /**
     * Set the value of the forceCache
     *
     * @param newForce     the new value of the forceCache
     * @param isForce       whether the new force is a force (otherwise it is a velocity)
     * */
    public void setForceCache(Vector2 newForce, boolean isForce);

    /**
     * Set the isInvisible boolean, which determines whether to draw the chicken on the screen
     *
     * @param invisible whether the chicken should be invisible
     */
    public void setInvisible(boolean invisible);
}
