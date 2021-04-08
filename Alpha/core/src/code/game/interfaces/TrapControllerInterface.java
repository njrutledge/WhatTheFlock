package code.game.interfaces;

import code.game.models.Chicken;
import code.game.models.Trap;

public interface TrapControllerInterface {


    /**
     * Applies the current trap t to chicken c. The effects depend on the type of trap.
     * This should be called on a contact starting between a trap and a chicken.
     *
     * @param t the trap interacting with a chicken
     * @param c the chicken in question
     */
    public boolean applyTrap(Trap t, Chicken c);

    /**
     * Stops the application of the current trap t to chicken c. The effects depend on the type of trap.
     * This should be called on a contact ending between a trap and a chicken.
     *
     * @param t the trap interacting with a chicken
     * @param c the chicken in question
     */
    public void stopTrap(Trap t, Chicken c);
}
