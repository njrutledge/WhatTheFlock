package code.game.interfaces;

public interface StoveInterface {
    /** enables or disables the stove lighting.
     *
     * This should be enabled if the chef is cooking, and disabled otherwise.
     *
     * @param val is true to enable, false to disable.
     */
    void setLit(boolean val);
}
