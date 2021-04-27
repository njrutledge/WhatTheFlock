package code.game.controllers;

import code.assets.AssetDirectory;
import code.audio.SoundBuffer;
import com.badlogic.gdx.math.MathUtils;

import java.awt.*;

public class SoundController {

    //Misc
    /** Base volume for all sounds */
    private final float VOLUME = 0.5f;
    /** Current volume */
    private float volume = VOLUME;
    /** Loud sound multiplier, used to lower volume of louder sounds */
    private final float LOUD = 0.2f;
    /** Medium sound multiplier, does nothing */
    private final float MED = 1f;
    /** Soft sound multiplier, used to raise volume of softer sounds */
    private final float SOFT = 1.3f;
    /** Enumerator to check what screen we are on for the music */
    private enum CurrentScreen {
        MENU,
        LEVEL,
        PAUSE
    }
    private CurrentScreen screen = CurrentScreen.MENU;

    //Chef
    /** Sound for when chef attacks, regardless of whether it hits */
    private SoundBuffer emptySlap;
    /** Sound for when chef attacks and hits */
    private SoundBuffer contactSlap;
    /** Sound for chef getting hurt*/
    private SoundBuffer chefHurt;


    //Traps
    /** Sound for when fire trap triggers */
    private SoundBuffer fireTrigger;

    /** Sound for when bread trap triggers */
    private SoundBuffer breadTrigger;
    /** Sound for when bread trap is being eaten */
    private SoundBuffer breadEat;

    /** Sound for when ice-cream trap triggers */
    private SoundBuffer iceTrigger;
    /** Sound for when chicken treads on ice-cream trap */
    private SoundBuffer iceTread;

    //Chickens
    /** Sound for shredded attack */
    private SoundBuffer shreddedAttack;
    /** Sound for shredded hurt */
    private SoundBuffer shreddedHurt;
    /** Sound for eggsplosion */
    private SoundBuffer eggsplosion;

    /** Sound for buffalo charging */
    private SoundBuffer buffaloAttack;
    /** Sound for buffalo charging */
    private SoundBuffer buffaloCharge;
    /** Sound for buffalo hurt */
    private SoundBuffer buffaloHurt;

    /** Sound for nugget attack */
    private SoundBuffer nuggetAttack;
    /** Sound for nugget hurt */
    private SoundBuffer nuggetHurt;


    //Music
    /** Interval timer for music */
    private float timer = 0f;
    /** SoundID for theme playing */
    private long musicID = -1;
    /** Pause volume lowering */
    private final float PAUSE_VOL = 0.1f;

    /** Menu music */
    private SoundBuffer menuTheme;
    /** Menu music duration */
    private final float MENU_DURATION = 72f;

    /** Level music 1 */
    private SoundBuffer levelTheme1;
    /** Level music 1 duration */
    private final float LEVEL_T_1 = 68f;

    /** Level music 2 */
    private SoundBuffer levelTheme2;
    /** Level music 2 duration */
    private final float LEVEL_T_2 = 0f;

    /** Level music 3 */
    private SoundBuffer levelTheme3;
    /** Level music 3 duration */
    private final float LEVEL_T_3 = 0f;





    public SoundController(AssetDirectory directory) {
        //Chef
        chefHurt = directory.getEntry("sound:chef:oof", SoundBuffer.class);
        emptySlap = directory.getEntry("sound:chef:emptySlap", SoundBuffer.class);
        contactSlap = directory.getEntry("sound:chef:slap", SoundBuffer.class);

        //Traps
        fireTrigger = directory.getEntry("sound:trap:fireTrig", SoundBuffer.class);

        breadTrigger = directory.getEntry("sound:trap:lureCrumb", SoundBuffer.class);

        //Chickens
        shreddedAttack = directory.getEntry("sound:chick:shredded:attack", SoundBuffer.class);
        shreddedHurt = directory.getEntry("sound:chick:shredded:hurt", SoundBuffer.class);
        eggsplosion = directory.getEntry("sound:chick:eggsplosion",SoundBuffer.class);

        buffaloAttack = directory.getEntry("sound:chick:buffalo:attack", SoundBuffer.class);
        buffaloCharge = directory.getEntry("sound:chick:buffalo:charge", SoundBuffer.class);

        nuggetAttack = directory.getEntry("sound:chick:nugget:attack", SoundBuffer.class);
        nuggetHurt = directory.getEntry("sound:chick:nugget:hurt", SoundBuffer.class);

        //Music
        menuTheme = directory.getEntry("sound:music:levelSel", SoundBuffer.class);
        levelTheme1 = directory.getEntry("sound:music:theme1", SoundBuffer.class);

    }

    private void playInstant(SoundBuffer sound, float multiplier) {
        sound.stop();
        sound.play(volume * multiplier);
    }

    private void playMusicInstant(SoundBuffer sound, float multiplier) {
        sound.stop();
        musicID = sound.play(volume * multiplier);
    }

    private void mute() {
        if (volume == 0f) {
            volume = VOLUME;
        } else {
            volume = 0f;
        }
    }

    //Currently does all theme 1 because there is no 2nd or 3rd theme.
    public void playLevel() {
        //int choose = MathUtils.random(2);
        int choose = 0;
        switch (choose) {
            case 0:
                playMusicInstant(levelTheme1, LOUD);
                timer = LEVEL_T_1;
                break;
            case 1:
                playMusicInstant(levelTheme2, LOUD);
                timer = LEVEL_T_2;
                break;
            case 2:
                playMusicInstant(levelTheme3, LOUD);
                timer = LEVEL_T_3;
                break;
        }
    }

    public void playMusic(CurrentScreen s, float dt) {
        boolean pause = false;
        if (s != screen) {
            timer = 0f;
            if (s == CurrentScreen.PAUSE) {
                pause = true;
            }
            screen = s;
        }

        switch (screen){
            case MENU:
                if (timer == 0f) {
                    timer = MENU_DURATION;
                    playMusicInstant(menuTheme, LOUD);
                }
            case LEVEL:
                if (pause) {
                    levelTheme1.setVolume(musicID, volume * LOUD);
                    levelTheme2.setVolume(musicID, volume * LOUD);
                    levelTheme3.setVolume(musicID, volume * LOUD);
                }
                if (timer == 0f) {
                    playLevel();
                }
            case PAUSE:
                if (timer == 0f) {
                    playLevel();
                }
                levelTheme1.setVolume(musicID, volume * LOUD * PAUSE_VOL);
                levelTheme2.setVolume(musicID, volume * LOUD * PAUSE_VOL);
                levelTheme3.setVolume(musicID, volume * LOUD * PAUSE_VOL);
        }

        timer = MathUtils.clamp(timer - dt, 0f, 600f);


    }


    //Commented out sounds have not yet been added
    //Chef
    public void playChefHurt() {playInstant(chefHurt, MED);}

    public void playEmptySlap() {playInstant(emptySlap, LOUD);}

    public void playHitSlap() {playInstant(contactSlap, LOUD);}


    //Traps
    public void playFireTrap() {playInstant(fireTrigger, LOUD);}

    //public void playBreadTrig() {playInstant(breadTrigger, MED);}

    public void playBreadEat() {playInstant(breadEat, MED);}

    //public void playIceTrig() {playInstant(iceTrigger, LOUD);}

    //public void playIceFreeze() {playInstant(iceTread, MED);}


    //Chickens
    public void playShredAttack() {playInstant(shreddedAttack, MED);}

    public void playShredHurt() {playInstant(shreddedHurt, LOUD);}

    public void playEggsplosion() {playInstant(eggsplosion, LOUD);}

    public void playBuffCharge() {playInstant(buffaloCharge, LOUD);}

    public void playBuffAttack() {playInstant(buffaloAttack, LOUD);}

    //public void playBuffHurt() {playInstant(buffaloHurt, LOUD);}

    public void playNugAttack() {playInstant(nuggetAttack, LOUD);}

    public void playNugHurt() {playInstant(nuggetHurt, MED);}





}
