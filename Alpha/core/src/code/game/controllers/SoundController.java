package code.game.controllers;

import code.assets.AssetDirectory;
import code.audio.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;


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
    public enum CurrentScreen {
        MENU,
        LEVEL,
        PAUSE
    }
    private CurrentScreen screen;

    /** List of all sounds for disposal later */
    private Array<SoundBuffer> sounds = new Array<>();

    //UI
    /** Sound for hitting enter on an item in the menus */
    private SoundBuffer menuEnter;
    /** Sound for choosing an item in the menus */
    private SoundBuffer menuSelecting;


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
    private final float PAUSE_VOL = 0.3f;

    /** Menu music */
    private MusicBuffer menuTheme;
    /** Menu music duration */
    private final float MENU_DURATION = 72f;

    /** Level music 1 */
    private MusicBuffer levelTheme1;
    /** Level music 1 duration */
    private final float LEVEL_T_1 = 68f;

    /** Level music 2 */
    private MusicBuffer levelTheme2;
    /** Level music 2 duration */
    private final float LEVEL_T_2 = 0f;

    /** Level music 3 */
    private MusicBuffer levelTheme3;
    /** Level music 3 duration */
    private final float LEVEL_T_3 = 0f;


    public SoundController() {
        screen = CurrentScreen.MENU;
    }

    public void gatherAssets(AssetDirectory directory) {
        //UI
        menuSelecting = directory.getEntry("sound:menu:selecting", SoundBuffer.class);
        menuEnter = directory.getEntry("sound:menu:select", SoundBuffer.class);
        sounds.add(menuEnter,menuSelecting);

        //Chef
        chefHurt = directory.getEntry("sound:chef:oof", SoundBuffer.class);
        emptySlap = directory.getEntry("sound:chef:emptySlap", SoundBuffer.class);
        contactSlap = directory.getEntry("sound:chef:slap", SoundBuffer.class);
        sounds.add(chefHurt,emptySlap,contactSlap);

        //Traps
        fireTrigger = directory.getEntry("sound:trap:fireTrig", SoundBuffer.class);

        breadTrigger = directory.getEntry("sound:trap:lureCrumb", SoundBuffer.class);

        sounds.add(fireTrigger, breadTrigger);

        //Chickens
        shreddedAttack = directory.getEntry("sound:chick:shredded:attack", SoundBuffer.class);
        shreddedHurt = directory.getEntry("sound:chick:shredded:hurt", SoundBuffer.class);
        eggsplosion = directory.getEntry("sound:chick:eggsplosion",SoundBuffer.class);

        buffaloAttack = directory.getEntry("sound:chick:buffalo:attack", SoundBuffer.class);
        buffaloCharge = directory.getEntry("sound:chick:buffalo:charge", SoundBuffer.class);

        nuggetAttack = directory.getEntry("sound:chick:nugget:attack", SoundBuffer.class);
        nuggetHurt = directory.getEntry("sound:chick:nugget:hurt", SoundBuffer.class);

        sounds.add(shreddedAttack, shreddedHurt, eggsplosion);
        sounds.add(buffaloAttack, buffaloCharge);
        sounds.add(nuggetAttack, nuggetHurt);

        //Music
        menuTheme = directory.getEntry("music:levelSel", MusicBuffer.class);
        levelTheme1 = directory.getEntry("music:theme1", MusicBuffer.class);

    }

    private void playInstant(SoundBuffer sound, float multiplier) {
        sound.stop();
        sound.play(volume * multiplier);
    }

    private void playMusicInstant(MusicBuffer sound, float multiplier) {
        sound.play();
        sound.setVolume(multiplier * volume);

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
                playMusicInstant(levelTheme1, MED);
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

    public void stopAllMusic() {
        menuTheme.stop();
        levelTheme1.stop();
        //levelTheme2.stop();
        //levelTheme3.stop();
    }

    public void playMusic(CurrentScreen s, float dt) {
        if (s != screen) {
            if (screen == CurrentScreen.MENU || s == CurrentScreen.MENU) {
                timer = 0f;
                stopAllMusic();
            }
        }
        screen = s;

        switch (screen){
            case MENU:
                playMusicInstant(menuTheme, LOUD);
                break;
            case LEVEL:
                if (!levelTheme1.isPlaying()) {
                    playLevel();
                }
                levelTheme1.setVolume(volume * LOUD);
                //levelTheme2.setVolume(volume * LOUD);
                //levelTheme3.setVolume(volume * LOUD);
                break;
            case PAUSE:
                if (!levelTheme1.isPlaying()) {
                    playLevel();
                }
                levelTheme1.setVolume(volume * LOUD * PAUSE_VOL);
                //levelTheme2.setVolume(volume * LOUD * PAUSE_VOL);
                //levelTheme3.setVolume(volume * LOUD * PAUSE_VOL);
                break;

        }

        timer = MathUtils.clamp(timer - dt, 0f, 600f);


    }


    //Commented out sounds have not yet been added
    //UI
    public void playMenuEnter() {playInstant(menuEnter, LOUD);}

    public void playMenuSelecting() {playInstant(menuSelecting, LOUD);}

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


    public void dispose() {
        for (SoundBuffer s : sounds) {
            s.dispose();
        }
        //levelTheme1.dispose();
        //menuTheme.dispose();


    }




}
