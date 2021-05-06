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

    /** collection of constants */
    private JsonValue constants;
    /** Texture Region for Fridge traps */
    private TextureRegion trapFridgeTexture;
    /** Texture Region for the Toaster traps*/
    private TextureRegion trapToasterTexture;
    /** Texture Region for the Bread trap*/
    private TextureRegion trapBreadTexture;
    /** Texture Region for Slow traps */
    private TextureRegion trapSlowTexture;
    /** Texture Region for default traps */
    private TextureRegion trapDefaultTexture;
    /** Number of lures a toaster releases*/
    private static final int LURE_NUM = 3;

    private static Random generator = new Random(0);

    public TrapController(Vector2 scale){
        drawscale = scale;
    }

    public void setConstants (JsonValue cnst){
        constants = cnst;
    }

    public void gatherAssets(AssetDirectory directory){
        constants = directory.getEntry( "constants", JsonValue.class );
        trapSlowTexture = new TextureRegion(directory.getEntry("enviro:trap:slow", Texture.class));
        trapDefaultTexture = new TextureRegion(directory.getEntry("enviro:trap:spike",Texture.class));
        trapToasterTexture = new TextureRegion(directory.getEntry("enviro:trap:toaster", Texture.class));
        trapBreadTexture = new TextureRegion(directory.getEntry("enviro:trap:bread", Texture.class));
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
        float twidth = trapBreadTexture.getRegionWidth()/drawscale.x;
        float theight = trapBreadTexture.getRegionHeight()/drawscale.y;
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
