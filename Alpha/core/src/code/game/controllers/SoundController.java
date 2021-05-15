package code.game.controllers;

import code.assets.AssetDirectory;
import code.audio.*;
import code.game.models.Save;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;


public class SoundController {

    //Misc
    /** Base volume for all sounds */
    private final float VOLUME = 0.5f;
    /** Current music volume */
    private float musicVol;
    /** Current sfx volume */
    private float sfxVol;
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
        PAUSE,
        WIN,
        LOSE
    }
    private CurrentScreen screen;

    /** List of all sounds for disposal later */
    private Array<SoundBuffer> sounds = new Array<>();

    //UI
    /** Sound for hitting enter on an item in the menus */
    private SoundBuffer menuEnter;
    /** Sound for choosing an item in the menus */
    private SoundBuffer menuSelecting;
    /** Sound for winning */
    private SoundBuffer victory;
    /** Sound for losing */
    private SoundBuffer failure;


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
    private SoundBuffer iceFreeze;
    /** Sound for when the cooler trap is opened */
    private SoundBuffer cooler;

    //Chickens
    /** Sound for shredded attack */
    private SoundBuffer shreddedAttack;
    /** Sound for shredded hurt */
    private SoundBuffer shreddedHurt;
    /** Sound for eggsplosion */
    private SoundBuffer eggsplosion;

    /** Sound for buffalo attacking */
    private SoundBuffer buffaloAttack;
    /** Sound for buffalo charging */
    private SoundBuffer buffaloCharge;
    /** Sound for buffalo hurt */
    private SoundBuffer buffaloHurt;

    /** Sound for hot chicken attacking */
    private SoundBuffer hotAttack;
    /** Sound for hot chicken charging */
    private SoundBuffer hotCharge;
    /** Sound for hot chicken hurt */
    private SoundBuffer hotHurt;

    /** Sound for nugget attack */
    private SoundBuffer nuggetAttack;
    /** Sound for nugget hurt */
    private SoundBuffer nuggetHurt;

    /** Sound for Dino attack */
    private SoundBuffer dinoAttack;
    /** Sound for Dino hurt */
    private SoundBuffer dinoHurt;


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

    private Save save;


    public SoundController() {
        screen = CurrentScreen.MENU;
    }

    public void gatherAssets(AssetDirectory directory) {
        //UI
        menuSelecting = directory.getEntry("sound:menu:selecting", SoundBuffer.class);
        menuEnter = directory.getEntry("sound:menu:select", SoundBuffer.class);
        victory = directory.getEntry("sound:menu:victory", SoundBuffer.class);
        failure = directory.getEntry("sound:menu:failure", SoundBuffer.class);
        sounds.add(victory, failure);
        sounds.add(menuEnter,menuSelecting);

        //Chef
        chefHurt = directory.getEntry("sound:chef:oof", SoundBuffer.class);
        emptySlap = directory.getEntry("sound:chef:emptySlap", SoundBuffer.class);
        contactSlap = directory.getEntry("sound:chef:slap", SoundBuffer.class);
        sounds.add(chefHurt,emptySlap,contactSlap);

        //Traps
        fireTrigger = directory.getEntry("sound:trap:fireTrig", SoundBuffer.class);

        breadTrigger = directory.getEntry("sound:trap:toaster", SoundBuffer.class);
        breadEat = directory.getEntry("sound:trap:lureCrumb", SoundBuffer.class);

        iceTrigger = directory.getEntry("sound:trap:iceChick", SoundBuffer.class);
        iceFreeze = directory.getEntry("sound:trap:chickFreeze", SoundBuffer.class);
        cooler = directory.getEntry("sound:trap:cooler", SoundBuffer.class);

        sounds.add(fireTrigger, breadTrigger, breadEat);
        sounds.add(iceFreeze, iceTrigger, cooler);

        //Chickens
        shreddedAttack = directory.getEntry("sound:chick:shredded:attack", SoundBuffer.class);
        shreddedHurt = directory.getEntry("sound:chick:shredded:hurt", SoundBuffer.class);

        buffaloAttack = directory.getEntry("sound:chick:buffalo:attack", SoundBuffer.class);
        buffaloCharge = directory.getEntry("sound:chick:buffalo:charge", SoundBuffer.class);
        buffaloHurt = directory.getEntry("sound:chick:buffalo:hurt", SoundBuffer.class);

        hotAttack = directory.getEntry("sound:chick:hot:attack", SoundBuffer.class);
        hotCharge = directory.getEntry("sound:chick:hot:charge", SoundBuffer.class);
        hotHurt = directory.getEntry("sound:chick:hot:hurt", SoundBuffer.class);
        eggsplosion = directory.getEntry("sound:chick:eggsplosion",SoundBuffer.class);

        nuggetAttack = directory.getEntry("sound:chick:nugget:attack", SoundBuffer.class);
        nuggetHurt = directory.getEntry("sound:chick:nugget:hurt", SoundBuffer.class);

        dinoAttack = directory.getEntry("sound:chick:dino:attack", SoundBuffer.class);
        dinoHurt = directory.getEntry("sound:chick:dino:hurt", SoundBuffer.class);

        sounds.add(shreddedAttack, shreddedHurt, eggsplosion);
        sounds.add(buffaloAttack, buffaloCharge, buffaloHurt);
        sounds.add(hotAttack, hotCharge, hotHurt);
        sounds.add(nuggetAttack, nuggetHurt);
        sounds.add(dinoAttack, dinoHurt);

        //Music
        menuTheme = directory.getEntry("music:levelSel", MusicBuffer.class);
        levelTheme1 = directory.getEntry("music:theme1", MusicBuffer.class);
        levelTheme2 = directory.getEntry("music:theme2", MusicBuffer.class);

    }

    private void playInstantSFX(SoundBuffer sound, float multiplier) {
        sound.stop();
        sound.play(sfxVol * multiplier);
    }

    /** Set the game save */
    public void setSave(Save s){
        save = s;
        musicVol = s.music_vol/100f;
        sfxVol = s.sfx_vol/100f;
    }

    public void updateVol(){
        musicVol = save.music_vol/100f;
        sfxVol = save.sfx_vol/100f;
        levelTheme1.setVolume(musicVol * LOUD);
        levelTheme2.setVolume(musicVol * LOUD);
        //levelTheme3.setVolume(musicVol * LOUD);
    }

    private void playMusicInstant(MusicBuffer sound) {
        sound.play();
    }

    private void mute() {
        if (musicVol == 0f) {
            musicVol = save.music_vol;
            sfxVol = save.sfx_vol;
        } else {
            musicVol = 0f;
            sfxVol = 0f;
        }
    }

    //Currently does all theme 1 because there is no 2nd or 3rd theme.
    public void playLevel() {
        int choose = MathUtils.random(1); //should be in range 2, but only 2 themes for now
        switch (choose) {
            case 0:
                playMusicInstant(levelTheme1);
                timer = LEVEL_T_1;
                break;
            case 1:
                playMusicInstant(levelTheme2);
                timer = LEVEL_T_2;
                break;
            case 2:
                playMusicInstant(levelTheme3);
                timer = LEVEL_T_3;
                break;
        }
    }

    private boolean musicPlaying() {
        return levelTheme1.isPlaying() || levelTheme2.isPlaying();
    }

    public void stopAllMusic() {
        menuTheme.stop();
        levelTheme1.stop();
        levelTheme2.stop();
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
                playMusicInstant(menuTheme);
                menuTheme.setVolume(musicVol * LOUD);
                break;
            case LEVEL:
                if (!musicPlaying()) {
                    playLevel();
                }
                levelTheme1.setVolume(musicVol * LOUD);
                levelTheme2.setVolume(musicVol * LOUD);
                //levelTheme3.setVolume(musicVol * LOUD);
                break;
            case PAUSE:
                if (!musicPlaying()) {
                    playLevel();
                }
                levelTheme1.setVolume(musicVol * LOUD * PAUSE_VOL);
                levelTheme2.setVolume(musicVol * LOUD * PAUSE_VOL);
                //levelTheme3.setVolume(musicVol * LOUD * PAUSE_VOL);
                break;
            case WIN:
            case LOSE:
                stopAllMusic();
                break;

        }

        timer = MathUtils.clamp(timer - dt, 0f, 600f);


    }


    //Commented out sounds have not yet been added
    //UI
    public void playMenuEnter() {
        playInstantSFX(menuEnter, LOUD);}

    public void playMenuSelecting() {
        playInstantSFX(menuSelecting, LOUD);}

    public void playVictory() {
        playInstantSFX(victory, LOUD);}

    public void playFailure() {
        playInstantSFX(failure, LOUD);}

    //Chef
    public void playChefHurt() {
        playInstantSFX(chefHurt, LOUD);}

    public void playEmptySlap() {
        playInstantSFX(emptySlap, LOUD);}

    public void playHitSlap() {
        playInstantSFX(contactSlap, LOUD);}


    //Traps
    public void playFireTrap() {
        playInstantSFX(fireTrigger, LOUD);}

    public void playBreadTrig() {
        playInstantSFX(breadTrigger, LOUD);}

    public void playBreadEat() {
        playInstantSFX(breadEat, LOUD);}

    public void playIceTrig() {
        playInstantSFX(iceFreeze, LOUD);}

    public void playIceFreeze() {
        playInstantSFX(iceFreeze, MED);}

    public void playCoolerOpen() {
        playInstantSFX(cooler, LOUD);}


    //Chickens
    public void playShredAttack() {
        playInstantSFX(shreddedAttack, MED);}

    public void playShredHurt() {
        playInstantSFX(shreddedHurt, LOUD);}

    public void playEggsplosion() {
        playInstantSFX(eggsplosion, LOUD);}

    public void playBuffCharge() {
        playInstantSFX(buffaloCharge, LOUD);}

    public void playBuffAttack() {
        playInstantSFX(buffaloAttack, LOUD);}

    public void playShredWhiff() {
        playInstantSFX(emptySlap, LOUD);}

    public void playBuffHurt() {
        playInstantSFX(buffaloHurt, LOUD);}

    public void playHotCharge() {
        playInstantSFX(hotCharge, LOUD);}

    public void playHotAttack() {
        playInstantSFX(hotAttack, LOUD);}

    public void playHotHurt() {
        playInstantSFX(hotHurt, LOUD);}

    public void playNugAttack() {
        playInstantSFX(nuggetAttack, LOUD);}

    public void playNugHurt() {
        playInstantSFX(nuggetHurt, MED);}

    public void playDinoHurt() {
        playInstantSFX(dinoHurt, LOUD);}

    public void playDinoAttack() {
        playInstantSFX(dinoAttack, LOUD);}


    public void dispose() {
        for (SoundBuffer s : sounds) {
            s.dispose();
        }
        levelTheme1.dispose();
        levelTheme2.dispose();
        menuTheme.dispose();


    }




}
