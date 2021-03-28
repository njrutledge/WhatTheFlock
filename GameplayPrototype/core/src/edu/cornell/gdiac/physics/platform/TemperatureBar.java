package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.physics.GameCanvas;

public class Temperature {
    /**The max temperature the chicken can get to (when cooked) */
    private int maxTemperature;
    /** Texture atlas to support a temperature bar */
    private Texture tempTexture;
    // tempBar is a "texture atlas." Break it up into parts.
    /** Temperature background */
    private TextureRegion tempBackground;
    /** Middle portion of the temperature forground (colored region) */
    private TextureRegion tempForeground;
    /** Top cap to the temp background (grey region) */
    private TextureRegion tempBkgTop;
    /** Middle portion of the temp background (grey region) */
    private TextureRegion tempBkgMiddle;
    /** bottom cap to the temp background (grey region) */
    private TextureRegion tempBkgBottom;
    /** top cap to the temp forground (colored region) */
    private TextureRegion tempFrgTop;
    /** Middle portion of the temp forground (colored region) */
    private TextureRegion tempFrgMiddle;
    /** bottom cap to the temp forground (colored region) */
    private TextureRegion tempFrgBottom;

    /** Default budget for asset loader (do nothing but load 60 fps) */
    private static int DEFAULT_BUDGET = 15;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Ratio of the bar width to the screen */
    private static float BAR_WIDTH_RATIO  =  0.25f;
    /** Ration of the bar height to the screen */
    private static float BAR_HEIGHT_RATIO = 0.66f;
    /** Height of the progress bar */
    private static float BUTTON_SCALE  = 0.75f;

    /** The width of the progress bar */
    private int width;
    /** The y-coordinate of the center of the progress bar */
    private int centerY;
    /** The x-coordinate of the center of the progress bar */
    private int centerX;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    public Temperature(int max, int w, int cy, int cx, int canvY, float s){
        maxTemperature = max;
        width = w;
        centerY = cy;
        centerX = cx;
        heightY = canvY;
        scale = s;
    }
    
    private void gatherTempAssets(){
        AssetDirectory internal = new AssetDirectory("tempbar.json");
        internal.loadAssets();
        internal.finishLoading();
        tempTexture = internal.getEntry("tempbar", Texture.class);
        //tempForeground = internal.getEntry("tempbar.foreground", TextureRegion.class);
        tempFrgTop = internal.getEntry("tempbar.foretop", TextureRegion.class);
        tempFrgMiddle = internal.getEntry("tempbar.foreground", TextureRegion.class);
        tempFrgBottom = internal.getEntry("tempbar.forebottom", TextureRegion.class);

        tempBkgTop = internal.getEntry("tempbar.backtop", TextureRegion.class);
        tempBkgMiddle = internal.getEntry("tempbar.background", TextureRegion.class);
        tempBkgBottom = internal.getEntry("tempbar.backbottom", TextureRegion.class);
    }


    /**
     * Updates the progress bar according to loading progress
     *
     * The progress bar is composed of parts: two rounded caps on the end, 
     * and a rectangle in a middle.  We adjust the size of the rectangle in
     * the middle to represent the amount of progress.
     *
     * @param canvas The drawing context
     */
    private void drawTemp(GameCanvas canvas, int temp) {
        canvas.draw(tempBkgTop,   Color.WHITE, centerX-width/2, centerY,
                scale*tempBkgTop.getRegionWidth(), scale*tempBkgTop.getRegionHeight());
        canvas.draw(tempBkgBottom,  Color.WHITE,centerX+width/2-scale*tempBkgBottom.getRegionWidth(), centerY,
                scale*tempBkgBottom.getRegionWidth(), scale*tempBkgBottom.getRegionHeight());
        canvas.draw(tempBkgMiddle, Color.WHITE,centerX-width/2+scale*tempBkgTop.getRegionWidth(), centerY,
                width-scale*(tempBkgBottom.getRegionWidth()+tempBkgTop.getRegionWidth()),
                scale*tempBkgMiddle.getRegionHeight());

        canvas.draw(tempFrgTop,   Color.WHITE,centerX-width/2, centerY,
                scale*tempFrgTop.getRegionWidth(), scale*tempFrgTop.getRegionHeight());
        if (temp > 0) {
            float span = temp*(width-scale*(tempFrgTop.getRegionWidth()+tempFrgBottom.getRegionWidth()))/2.0f;
            canvas.draw(tempFrgBottom,  Color.WHITE,centerX-width/2+scale*tempFrgTop.getRegionWidth()+span, centerY,
                    scale*tempFrgBottom.getRegionWidth(), scale*tempFrgBottom.getRegionHeight());
            canvas.draw(tempFrgMiddle, Color.WHITE,centerX-width/2+scale*tempFrgTop.getRegionWidth(), centerY,
                    span, scale*tempFrgMiddle.getRegionHeight());
        } else {
            canvas.draw(tempFrgBottom,  Color.WHITE,centerX-width/2+scale*tempFrgTop.getRegionWidth(), centerY,
                    scale*tempFrgBottom.getRegionWidth(), scale*tempFrgBottom.getRegionHeight());
        }
    }
}
