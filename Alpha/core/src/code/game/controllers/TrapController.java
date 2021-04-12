package code.game.controllers;

import code.game.interfaces.TrapControllerInterface;
import code.game.models.Chicken;
import code.game.models.Trap;
import code.util.PooledList;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import java.util.Random;

public class TrapController implements TrapControllerInterface {

    /** The world scale */
    private Vector2 drawscale;

    /** collection of constants */
    private JsonValue constants;

    /** trap to return if applyTrap is true */
    private PooledList<Trap> trapCache = new PooledList<Trap>();

    private static Random generator = new Random(0);

    public TrapController(Vector2 scale){
        drawscale = scale;
    }

    public void setConstants (JsonValue cnst){
        constants = cnst;
    }

    /**
     * Applies the current trap t to chicken c. The effects depend on the type of trap.
     * This should be called on a contact starting between a trap and a chicken.
     * @param t the trap interacting with a chicken
     * @param c the chicken in question
     * @return true if a new trap is being created
     */
    public boolean applyTrap(Trap t, Chicken c){
        switch(t.getTrapType()){
            case LURE: //damage
                c.trapTarget(t);
                break;
            case SLOW:
                c.inSlow(true);
                break;
            /*case FIRE:
                TextureRegion trapTexture = t.getTexture();
                float twidth = trapTexture.getRegionWidth()/drawscale.x;
                float theight = trapTexture.getRegionHeight()/drawscale.y;
                Trap trap = new Trap(constants.get("trap"), t.getX(), t.getY(), twidth, theight, Trap.type.FIRE_LINGER, Trap.shape.CIRCLE, true);
                trapCache.setDrawScale(drawscale);
                trapCache.setTexture(trapTexture);
                //We need to let GameController know that a trap needs to be created
                return true;
                break;*/
        }
        return false;
    }


    /**
     * Stops the application of the current trap t to chicken c. The effects depend on the type of trap.
     * This should be called on a contact ending between a trap and a chicken.
     * @param t the trap interacting with a chicken
     * @param c the chicken in question
     */
    public void stopTrap(Trap t, Chicken c){
        switch(t.getTrapType()){
            case LURE:
                c.resetTarget();
                break;
            case SLOW:
                c.inSlow(false);
                break;
            case BREAD_BOMB:
            case FAULTY_OVEN:
            case FRIDGE:
                //do nothing!
                break;
        }

    }

    public void createLures(Vector2 spawnPos){
        for(int i = 1; i <= 5; i++){

        }
    }

    public Trap createSlow(Trap t){
        Trap trap = new Trap(constants.get("trap"), t.getX(), t.getY(), t.getWidth(), t.getHeight(), Trap.type.SLOW);
        trap.setDrawScale(t.getDrawScale());
        trap.setTexture(t.getTexture());
        return trap;
    }

    /**
     * Returns a random float between min and max (inclusive).
     *
     * @param min Minimum value in random range
     * @param max Maximum value in random range
     *
     * @return a random float between min and max (inclusive).
     */
    private static float rollFloat(float min, float max) {
        return generator.nextFloat() * (max - min) + min;
    }

    /** returns the new trap generated by applyTrap
     *
     * @return a new trap to create. Will be null if applyTrap is not called first, or if applyTrap returns false.
     */
    public PooledList<Trap> getNewTrap(){
        return trapCache;
    }

    /**
     *  decrement the trap durability, and remove the trap if it breaks.
     *
     * @param trap   the trap to decrement durability and possibly remove
     */
    private void decrementTrap(Trap trap){
        if(!trap.isRemoved() && trap.decrementDurability()){
            trap.markRemoved(true);
        }
    }
}
