package code.game.models;

import code.game.interfaces.StoveInterface;
import code.game.views.GameCanvas;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;

public class Spawn extends GameObject {

    private JsonValue data;

    /** The font used to draw text on the screen*/
    private static final BitmapFont font = new BitmapFont();

    //TODO: add comment
    public Spawn(float x, float y, float width, float height) {
        super(x, y, width, height,ObjectType.NULL);
        setBodyType(BodyDef.BodyType.StaticBody);
        setFixedRotation(true);
        setName("spawn");
        //setSensorName("cookRadius");
    }
    /**
     * Draws the stove
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        float wScale = 0.2f;
        float hScale = 0.2f;
        canvas.draw(texture, Color.WHITE ,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),displayScale.x*wScale,displayScale.y*hScale);
    }

    /**
     * Draws the outline of the game body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
    }
}




