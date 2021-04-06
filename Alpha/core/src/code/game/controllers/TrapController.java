package code.game.controllers;

import code.game.models.Chicken;
import code.game.models.Trap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

public class TrapController extends GameController{

    /** The world scale */
    private Vector2 drawscale;

    private JsonValue constants;

    public TrapController(Vector2 scale, JsonValue json){
        drawscale = scale;
        constants = json;
    }

    /**
     * Applies the current trap t to chicken c. The effects depend on the type of trap.
     * This should be called on a contact starting between a trap and a chicken.
     * @param t the trap interacting with a chicken
     * @param c the chicken in question
     */
    public void applyTrap(Trap t, Chicken c){
        switch(t.getTrapType()){
            case LURE: //damage
                c.trapTarget(t);
                break;
            case SLOW:
                c.applySlow(t.getEffect());
                decrementTrap(t);
                break;
            case FIRE:
                TextureRegion trapTexture = t.getTexture();
                float twidth = trapTexture.getRegionWidth()/drawscale.x;
                float theight = trapTexture.getRegionHeight()/drawscale.y;
                Trap trapCache = new Trap(constants.get("trap"), t.getX(), t.getY(), twidth, theight, Trap.type.FIRE_LINGER, Trap.shape.CIRCLE);
                trapCache.setDrawScale(drawscale);
                trapCache.setTexture(trapTexture);
                addQueuedObject(trapCache);
                trapCache = null;
                decrementTrap(t);
                break;
            case FIRE_LINGER:
                c.applyFire(t.getEffect());
                break;
        }
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
                c.removeSlow();
                break;
            case FIRE :
                break;
            case FIRE_LINGER:
                c.letItBurn();
        }

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
