/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package code.game.controllers;

import code.util.Controllers;
import code.util.XBoxController;
import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;

import com.badlogic.gdx.utils.Array;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
	//TODO: Change the shooting input to follow the slapping controls that we want to use (i.e. arrow keys)

	// Sensitivity for moving crosshair with gameplay
	private static final float GP_ACCELERATE = 1.0f;
	private static final float GP_MAX_SPEED  = 10.0f;
	private static final float GP_THRESHOLD  = 0.01f;

	/** The singleton instance of the input controller */
	private static InputController theController = null;
	
	/** 
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputController getInstance() {
		if (theController == null) {
			theController = new InputController();
		}
		return theController;
	}
	
	// Fields to manage buttons
	/** Whether the button to advanced worlds was pressed. */
	private boolean nextPressed;
	private boolean nextPrevious;
	/** Whether the button to step back worlds was pressed. */
	private boolean prevPressed;
	private boolean prevPrevious;
	/** Whether the primary action button was pressed. */
	private boolean primePressed;
	private boolean primePrevious;
	/** Whether the secondary action button was pressed. */
	private boolean secondPressed;
	private boolean secondPrevious;
	/** Whether the teritiary action button was pressed. */
	private boolean tertiaryPressed;
	/** Whether a movement key was pressed*/
	private boolean movementPressed;
	private boolean movementPrevious;
	/** Whether the trap placement button ws pressed */
	private boolean trapPressed;
	private boolean trapPrevious;
	/** Whether or not rotate left through traps is pressed*/
	private boolean trapRotateLeftPressed;
	private boolean trapRLPrevious;
	/** Whether or not rotate right through traps is pressed*/
	private boolean trapRotateRightPressed;
	private boolean trapRRPrevious;

	/** Whether the debug toggle was pressed. */
	private boolean debugPressed;
	private boolean debugPrevious;
	/** Whether the exit button was pressed. */
	private boolean exitPressed;
	private boolean exitPrevious;
	/** Whether or not the mute button was pressed. */
	private boolean mutePressed;
	private boolean mutePrevious;

	/** Whether or not the mute button was pressed. */
	private boolean pausePressed;
	private boolean pausePrevious;

	/** Whether parameter toggle was pressed*/
	private boolean paraToggled;
	private boolean paraPrevious;
	/** Whether parameter increased or decreased was pressed*/
	private boolean paraIncPressed;
	private boolean paraIncPrevious;
	private boolean paraDecPressed;
	private boolean paraDecPrevious;
	/** Whether grid was toggled */
	private boolean gridToggled;
	private boolean gridPrevious;

	/** How much did we move horizontally? */
	private float horizontal;
	/** How much did we move vertically? */
	private float vertical;
	/** The crosshair position (for raddoll) */
	private Vector2 crosshair;
	/** The crosshair cache (for using as a return value) */
	private Vector2 crosscache;
	/** For the gamepad crosshair control */
	private float momentum;
	/**Slap direction (1: N, 2: E, 3: S, 4: W) */
	private int slapDirection;
	
	/** An X-Box controller (if it is connected) */
	XBoxController xbox;
	
	/**
	 * Returns the amount of sideways movement. 
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement. 
	 */
	public float getHorizontal() {
		return horizontal;
	}
	
	/**
	 * Returns the amount of vertical movement. 
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement. 
	 */
	public float getVertical() {
		return vertical;
	}

	/**
	 * Returns the slap direction
	 * 1: North, 2: East, 3: South, 4: West
	 *
	 * @return an integer between 1-4
	 */
	public int getSlapDirection() { return slapDirection; }
	
	/**
	 * Returns the current position of the crosshairs on the screen.
	 *
	 * This value does not return the actual reference to the crosshairs position.
	 * That way this method can be called multiple times without any fair that 
	 * the position has been corrupted.  However, it does return the same object
	 * each time.  So if you modify the object, the object will be reset in a
	 * subsequent call to this getter.
	 *
	 * @return the current position of the crosshairs on the screen.
	 */
	public Vector2 getCrossHair() {
		return crosscache.set(crosshair);
	}

	/**
	 * Returns true if the primary action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the primary action button was pressed.
	 */
	public boolean didPrimary() {
		return primePressed && !primePrevious;
	}

	/**
	 * Returns true if the secondary action button was pressed.
	 *
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the secondary action button was pressed.
	 */
	public boolean didSecondary() {
		return secondPressed && !secondPrevious;
	}

	/**
	 * Returns true if a movement key was pressed
	 *
	 *
	 * @return true if a movement button was pressed.
	 */
	public boolean didMovementKey() { return movementPressed && !movementPrevious; }

	/**
	 * Returns true iff a movement key is currently being pressed
	 * @return a movement key is being pressed
	 */
	public boolean isMovementPressed(){
		return movementPressed;
	}
	/**
	 * Returns true if the tertiary action button was pressed.
	 *
	 * This is a sustained button. It will returns true as long as the player
	 * holds it down.
	 *
	 * @return true if the secondary action button was pressed.
	 */
	public boolean didTertiary() {
		return tertiaryPressed;
	}

	/**
	 * Returns true if the trap button was pressed.
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the trap button was pressed
	 */
	public boolean didTrap() {return trapPressed && !trapPrevious; }

	/**
	 * Returns true if the left rotate trap button is pressed.
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * 	@return true if the left rotate trap button was pressed
	 */
	public boolean didRotateTrapLeft() {return trapRotateLeftPressed && !trapRLPrevious; }

	/**
	 * Returns true if the right rotate trap button is pressed.
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * 	@return true if the right rotate trap button was pressed
	 */
	public boolean didRotateTrapRight() {return trapRotateRightPressed && !trapRRPrevious; }

	/**
	 * Returns true if the parameter toggle button is pressed.
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * 	@return true if the parameter toggled button was pressed
	 */
	public boolean didParameterToggle() {return paraToggled && !paraPrevious; }

	/**
	 * Returns true if the mute button is pressed.
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * 	@return true if the mute button was pressed
	 */
	public boolean didMute() {return mutePressed && !mutePrevious; }

	/**
	 *  Returns true if the pause button is pressed.
	 *  This is a one-press button. It only returns true at the moment it was
	 *  pressed, and returns false at any frame afterwards.
	 *
	 * @return true if the pause button was pressed
	 */
	public boolean didPause() { return pausePressed && !pausePrevious; }

	/**
	 * Returns true if the parameter increased button is pressed.
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * 	@return true if the parameter increased button was pressed
	 */
	public boolean didParameterIncreased() {return paraIncPressed && !paraIncPrevious; }

	/**
	 * Returns true if the parameter decreased button is pressed.
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * 	@return true if the parameter decreased button was pressed
	 */
	public boolean didParameterDecreased() {return paraDecPressed && !paraDecPrevious; }

	/**
	 * Returns true if the parameter decreased button is pressed.
	 * This is a one-press button. It only returns true at the moment it was
	 * pressed, and returns false at any frame afterwards.
	 *
	 * 	@return true if the parameter decreased button was pressed
	 */
	public boolean didGridToggle() { return gridToggled && !gridPrevious; }

	/**
	 * Returns true if the player wants to go to the next level.
	 *
	 * @return true if the player wants to go to the next level.
	 */
	public boolean didAdvance() {
		return nextPressed && !nextPrevious;
	}
	
	/**
	 * Returns true if the player wants to go to the previous level.
	 *
	 * @return true if the player wants to go to the previous level.
	 */
	public boolean didRetreat() {
		return prevPressed && !prevPrevious;
	}
	
	/**
	 * Returns true if the player wants to go toggle the debug mode.
	 *
	 * @return true if the player wants to go toggle the debug mode.
	 */
	public boolean didDebug() {
		return debugPressed && !debugPrevious;
	}
	
	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() {
		return exitPressed && !exitPrevious;
	}
	
	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() {
		// If we have a game-pad for id, then use it.
		Array<XBoxController> controllers = Controllers.get().getXBoxControllers();
		if (controllers.size > 0) {
			xbox = controllers.get( 0 );
		} else {
			xbox = null;
		}
		crosshair = new Vector2();
		crosscache = new Vector2();
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	public void readInput(Rectangle bounds, Vector2 scale) {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		primePrevious  = primePressed;
		secondPrevious = secondPressed;
		debugPrevious  = debugPressed;
		exitPrevious = exitPressed;
		nextPrevious = nextPressed;
		prevPrevious = prevPressed;
		trapPrevious = trapPressed;
		trapRLPrevious = trapRotateLeftPressed;
		trapRRPrevious = trapRotateRightPressed;
		paraPrevious = paraToggled;
		paraIncPrevious = paraIncPressed;
		paraDecPrevious = paraDecPressed;
		mutePrevious = mutePressed;
		pausePrevious = pausePressed;
		gridPrevious = gridToggled;
		movementPrevious = movementPressed;
		
		// Check to see if a GamePad is connected
		if (xbox != null && xbox.isConnected()) {
			readGamepad(bounds, scale);
			readKeyboard(bounds, scale, true); // Read as a back-up
		} else {
			readKeyboard(bounds, scale, false);
		}
	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	private void readGamepad(Rectangle bounds, Vector2 scale) {
		exitPressed  = xbox.getBack();
		nextPressed  = xbox.getRBumper();
		prevPressed  = xbox.getLBumper();
		primePressed = xbox.getA();
		debugPressed  = xbox.getY();

		// Increase animation frame, but only if trying to move
		horizontal = xbox.getLeftX();
		vertical   = xbox.getLeftY();
		secondPressed = xbox.getRightTrigger() > 0.6f;
		
		// Move the crosshairs with the right stick.
		tertiaryPressed = xbox.getA();
		crosscache.set(xbox.getLeftX(), xbox.getLeftY());
		if (crosscache.len2() > GP_THRESHOLD) {
			momentum += GP_ACCELERATE;
			momentum = Math.min(momentum, GP_MAX_SPEED);
			crosscache.scl(momentum);
			crosscache.scl(1/scale.x,1/scale.y);
			crosshair.add(crosscache);
		} else {
			momentum = 0;
		}
		clampPosition(bounds);
	}

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */
	private void readKeyboard(Rectangle bounds, Vector2 scale, boolean secondary) {
		// Give priority to gamepad results
		debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.X));
		primePressed = (secondary && primePressed) || (Gdx.input.isKeyPressed(Input.Keys.UP));
		secondPressed = (secondary && secondPressed) || (Gdx.input.isKeyPressed(Input.Keys.UP)) ||
				(Gdx.input.isKeyPressed(Input.Keys.LEFT)) || (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) ||
						(Gdx.input.isKeyPressed(Input.Keys.DOWN));
		nextPressed = (secondary && nextPressed) || (Gdx.input.isKeyPressed(Input.Keys.N));
/*		exitPressed  = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));*/
		movementPressed = ((Gdx.input.isKeyPressed(Input.Keys.W)) || (Gdx.input.isKeyPressed(Input.Keys.A)) ||
				(Gdx.input.isKeyPressed(Input.Keys.S)) || (Gdx.input.isKeyPressed(Input.Keys.D)));
		trapPressed = (Gdx.input.isKeyPressed(Input.Keys.SPACE));
		trapRotateLeftPressed = (Gdx.input.isKeyPressed(Input.Keys.Q));
		trapRotateRightPressed = (Gdx.input.isKeyPressed(Input.Keys.E));
		paraToggled = (Gdx.input.isKeyPressed(Input.Keys.O));
		paraDecPressed = (Gdx.input.isKeyPressed(Input.Keys.I));
		paraIncPressed = (Gdx.input.isKeyPressed(Input.Keys.P));
		mutePressed = (Gdx.input.isKeyPressed(Input.Keys.M));
		pausePressed = (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
		gridToggled = (Gdx.input.isKeyPressed(Input.Keys.G));
		
		// Directional controls
		if (horizontal > 0 && Gdx.input.isKeyPressed(Input.Keys.D)){
			horizontal = 0;
		} else if (horizontal < 0 && Gdx.input.isKeyPressed(Input.Keys.A)){
			horizontal = 0;
		} else if (!Gdx.input.isKeyPressed(Input.Keys.D) && !Gdx.input.isKeyPressed(Input.Keys.A)){
			horizontal = 0;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			horizontal = 1f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			horizontal = -1f;
		}

		if (vertical > 0 && Gdx.input.isKeyPressed(Input.Keys.W)){
			vertical = 0;
		} else if (vertical < 0 && Gdx.input.isKeyPressed(Input.Keys.S)){
			vertical = 0;
		} else if (!Gdx.input.isKeyPressed(Input.Keys.W) && !Gdx.input.isKeyPressed(Input.Keys.S)){
			vertical = 0;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			vertical = 1f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			vertical = -1f;
		}

		// Directional slap
		if (Gdx.input.isKeyPressed(Input.Keys.UP)){
			slapDirection = 1;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			slapDirection = 2;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)){
			slapDirection = 3;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			slapDirection = 4;
		}
		
		// Mouse results
        tertiaryPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
		crosshair.set(Gdx.input.getX(), Gdx.input.getY());
		crosshair.scl(1/scale.x,-1/scale.y);
		crosshair.y += bounds.height;
		clampPosition(bounds);
	}
	
	/**
	 * Clamp the cursor position so that it does not go outside the window
	 *
	 * While this is not usually a problem with mouse control, this is critical 
	 * for the gamepad controls.
	 */
	private void clampPosition(Rectangle bounds) {
		crosshair.x = Math.max(bounds.x, Math.min(bounds.x+bounds.width, crosshair.x));
		crosshair.y = Math.max(bounds.y, Math.min(bounds.y+bounds.height, crosshair.y));
	}
}