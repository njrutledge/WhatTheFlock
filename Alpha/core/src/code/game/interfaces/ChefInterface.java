package code.game.interfaces;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface ChefInterface {
    /**Sets the chef's cooking status
     * @param b the boolean, whether cooking is true or false*/
    void setCooking(boolean b);

    /**Returns whether the chef is cooking.
     * @return the cooking status of the chef. */
    boolean isCooking();

    /**
     * Returns left/right movement of this character.
     *
     * This is the result of input times chef force.
     *
     * @return left/right movement of this character.
     */
    float getMovement();
    /**
     * Returns up/down movement of this character.
     *
     * This is the result of input times chef force.
     *
     * @return up/down movement of this character.
     */
    float getVertMovement();

    /**
     * Sets left/right movement of this character.
     *
     * This is the result of input times chef force.
     *
     * @param value left/right movement of this character.
     */
    void setMovement(float value);

    /**Set the vertical movement of the character*/
    void setVertMovement(float value);

    /**
     * Enables or disables trap placement
     *
     * @param value is true if the player can place a trap.
     */
    void setCanPlaceTrap(boolean value);

    /**
     * Returns true if the chef is actively firing.
     *
     * @return true if the chef is actively firing.
     */
    boolean isShooting();

    /**
     * Returns true if the chef is trying to place a trap.
     *
     * @return true if the chef is trying to place a trap.
     */
    boolean isTrapping();

    /**
     * Sets whether or not the chef is trying to place a trap
     *
     * @param bln whether or not the chef is trying to place a trap
     */
    void setTrap(boolean bln);

    void setTexture(Texture texture);

    /**
     * Returns if the character is alive.
     *
     * @return	 if the character is alive
     */
    boolean isAlive();

    /** Reduces the chef's health by one. */
    void decrementHealth();

    /**
     * Returns true if the character has recently taken damage and is not invulnerable
     *
     * @return true if the character is stunned, false otherwise
     */
    boolean isStunned();

    /**
     * Returns how much force to apply to get the chef moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get the chef moving
     */
    float getForce();

    /**
     * Returns the base damage value for the chef
     *
     * @return the base damage value for the chef
     */
    float getDamage();

    void setHealthTexture(TextureRegion t);

    /**
     * Applies the force to the body of this chef
     *
     * This method should be called after the force attribute is set.
     */
    void applyForce();

    /**
     * Updates the object's game state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */

    void update(float dt);

}
