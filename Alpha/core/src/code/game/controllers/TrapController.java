package code.game.controllers;

import code.game.interfaces.TrapControllerInterface;
import code.game.models.Chicken;
import code.game.models.Trap;
import code.util.PooledList;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import java.util.Random;

public class TrapController implements TrapControllerInterface {

    /** The world scale */
    private Vector2 drawscale;

    /** collection of constants */
    private JsonValue constants;
    /** Texture Region for Fridge traps */
    private TextureRegion trapFridgeTexture;
    /** Texture Region for Slow traps */
    private TextureRegion trapSlowTexture;
    /** Texture Region for default traps */
    private TextureRegion trapDefaultTexture;

    private static Random generator = new Random(0);

    public TrapController(Vector2 scale){
        drawscale = scale;
    }

    public void setConstants (JsonValue cnst){
        constants = cnst;
    }

    public void setTrapAssets(TextureRegion trapFridgeTexture, TextureRegion trapSlowTexture, TextureRegion trapDefaultTexture) {
        this.trapFridgeTexture = trapFridgeTexture;
        this.trapSlowTexture = trapSlowTexture;
        this.trapDefaultTexture = trapDefaultTexture;
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
                if(!c.getType().equals(Chicken.ChickenType.Shredded)) {
                    c.trapTarget(t);
                }
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

    public void handleLureHit(Trap lure){
       lure.markHit();
    }

    public PooledList<Trap> createLures(Trap breadBomb){
        /** trap to return if applyTrap is true */
        PooledList<Trap> trapCache = new PooledList<Trap>();
        for(int i = 0; i < 5; i++){
            float angle = rollFloat(72*i, 72*(i+1));
            Trap trap = createLure(breadBomb);
            float speed = 16.0f;
            float vx = speed * MathUtils.cosDeg(angle);
            float vy = speed * MathUtils.sinDeg(angle);
            trap.setVX(vx);
            trap.setVY(vy);
            trap.setLinearDamping(2f);
            trapCache.add(trap);
        }
        return trapCache;
    }

    public Trap createSlow(Trap fridge){
        float twidth = trapSlowTexture.getRegionWidth()/drawscale.x;
        float theight = trapSlowTexture.getRegionHeight()/drawscale.y;
        Trap trap = new Trap(constants.get("trap"), fridge.getX(), fridge.getY(), twidth, theight, Trap.type.SLOW);
        trap.setDrawScale(fridge.getDrawScale());
        trap.setTexture(trapSlowTexture);
        return trap;
    }

    private Trap createLure(Trap breadBomb){
        float twidth = trapDefaultTexture.getRegionWidth()/drawscale.x;
        float theight = trapDefaultTexture.getRegionHeight()/drawscale.y;
        Trap trap = new Trap(constants.get("trap"), breadBomb.getX(), breadBomb.getY(), breadBomb.getWidth(), breadBomb.getHeight(), Trap.type.LURE);
        trap.setDrawScale(breadBomb.getDrawScale());
        trap.setTexture(trapDefaultTexture);
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
