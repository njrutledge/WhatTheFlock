package code.game.controllers;

import code.assets.AssetDirectory;
import code.game.interfaces.TrapControllerInterface;
import code.game.models.Chicken;
import code.game.models.Trap;
import code.util.PooledList;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import java.util.Random;

public class TrapController implements TrapControllerInterface {

    /** The world scale */
    private Vector2 drawscale;

    private Vector2 displayScale;

    /** collection of constants */
    private JsonValue constants;
    /** Texture Region for Fridge traps */
    private Texture trapFridgeTexture;
    /** Texture Region for the Toaster traps*/
    private Texture trapToasterTexture;
    /** Texture Region for the Bread trap*/
    private Texture trapBreadTexture;
    /** Texture Region for Slow traps */
    private Texture trapSlowTexture;
    /** Texture Region for default traps */
    private TextureRegion trapDefaultTexture;
    /** Number of lures a toaster releases*/
    private static final int LURE_NUM = 2;

    private static Random generator = new Random(0);

    public TrapController(Vector2 scale, Vector2 displayScale){
        drawscale = scale;
        this.displayScale = displayScale;
    }

    public void setConstants (JsonValue cnst){
        constants = cnst;
    }

    public void gatherAssets(AssetDirectory directory){
        constants = directory.getEntry( "constants", JsonValue.class );
        trapSlowTexture = directory.getEntry("enviro:trap:slow", Texture.class);
        trapToasterTexture = directory.getEntry("enviro:trap:toaster", Texture.class);
        trapBreadTexture = directory.getEntry("enviro:trap:bread", Texture.class);
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
            case SLOW:
                c.inSlow(true);
                break;
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
            case BREAD_LURE:
                //c.resetTarget();
                break;
            case SLOW:
                c.inSlow(false);
                break;
            case TOASTER:
            case HOT_SAUCE:
            case COOLER:
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
        for(int i = 0; i < LURE_NUM; i++){
            float angle = rollFloat(0,360);
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
        float twidth = trapSlowTexture.getWidth()/drawscale.x;
        float theight = trapSlowTexture.getHeight()/drawscale.y;
        Trap trap = new Trap(constants.get("trap"), fridge.getX(), fridge.getY(), twidth, theight, Trap.type.SLOW);
        trap.setDrawScale(fridge.getDrawScale());
        trap.setTexture(trapSlowTexture);
        fridge.setChildTrap(trap);
        return trap;
    }

    private Trap createLure(Trap breadBomb){
        float twidth = trapBreadTexture.getWidth()/drawscale.x*displayScale.x;
        float theight = trapBreadTexture.getHeight()/drawscale.y*displayScale.y;
        Trap trap = new Trap(constants.get("trap"), breadBomb.getX(), breadBomb.getY(), twidth, theight, Trap.type.BREAD_LURE);
        trap.setDrawScale(breadBomb.getDrawScale());
        trap.setTexture(trapBreadTexture);
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

}
