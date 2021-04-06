package code.game.models;

import code.game.views.GameCanvas;
import code.game.models.obstacle.BoxObstacle;

// Not sure if we want this to extend BoxObstacle


public abstract class GameObject extends BoxObstacle{
    /**
     * Abstract class for objects in the game. This includes the chef, the
     * enemies, the stove, and the traps
     *
     * @param x         The x position of this object in the world
     * @param y         The y position of this object in the world
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public GameObject(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public abstract void draw(GameCanvas canvas);
}