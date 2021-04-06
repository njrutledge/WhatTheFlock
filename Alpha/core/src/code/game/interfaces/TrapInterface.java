package code.game.interfaces;

import code.game.models.Trap;

public interface TrapInterface {

    /**
     * Gets the trap's effect. This is either a multiplier for the slow down effect or a duration for the fire effect.
     * -1 otherwise
     *
     * @return a float determining the effect
     */
     float getEffect();

    /**
     *  Returns the enum type that represents this trap
     *
     * @return the type of this trap
     */
    Trap.type getTrapType();

    /**
     * Returns true if linger is enabled
     *
     * @return true if linger is enabled
     */
    boolean getLinger();



}
