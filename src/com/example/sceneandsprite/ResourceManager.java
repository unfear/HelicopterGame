package com.example.sceneandsprite;

import java.io.IOException;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;

import android.content.Context;
import android.util.SparseArray;

public class ResourceManager {
    private static ResourceManager INSTANCE;

    /* The variables listed should be kept public, allowing us easy access
    to them when creating new Sprites, Text objects and to play sound files */

    BuildableBitmapTextureAtlas     mBitmapTextureAtlas;
    public TiledTextureRegion      mHelicopterTextureRegion;
    public TiledTextureRegion      mBirdTextureRegion;
    public BitmapTextureAtlas      mBitmapTextureAtlasControllBtn;
    public BitmapTextureAtlas      mBitmapTextureAtlasClouds;
    public BitmapTextureAtlas      mBitmapTextureAtlasBullet;
    public TiledTextureRegion      mExplodeTextureRegion;

    public ITextureRegion          mControllBtnRegion;
    public ITextureRegion          mOnScreenControlKnobTextureRegion;
    public ITextureRegion          mClouds;
    public ITextureRegion          mBullet;
    public DigitalOnScreenControl  mDigitalOnScreenControl;

    Music                           mMusic;
    Sound                           mSound;
    
    public Font                    mScoreFont;
    
    public final SparseArray<Text> mScoreTextMap = new SparseArray<Text>();
    
    ResourceManager(){
        // The constructor is of no use to us
    }

    public synchronized static ResourceManager getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ResourceManager();
        }
        return INSTANCE;
    }
    
    /* Each scene within a game should have a loadTextures method as well
     * as an accompanying unloadTextures method. This way, we can display
     * a loading image during scene swapping, unload the first scene's textures
     * then load the next scenes textures.
     */
    public synchronized void loadGameTextures(Engine pEngine, Context pContext){
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        /* Create the texture atlas at the same dimensions as the image
        (300x50)*/
        mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(pEngine.getTextureManager(), 1359, 630, 
                                                            TextureOptions.BILINEAR);
        /* Create the TiledTextureRegion object, passing in the usual
        parameters, as well as the number of rows and columns in our sprite sheet
        for the final two parameters */
        mBitmapTextureAtlasClouds = new BitmapTextureAtlas(pEngine.getTextureManager(), 100, 50);
        mBitmapTextureAtlasBullet = new BitmapTextureAtlas(pEngine.getTextureManager(), 10, 12);

        mClouds = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasClouds, pContext, "cloud.png", 0, 0);

        mBullet = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasBullet, pContext, "bullet.png", 0, 0);

        mBirdTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, pContext, 
                                                                                            "bird.png", 3, 4);

        mExplodeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, pContext, 
                                                                                                "explosion.png", 5, 1);

        mHelicopterTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, pContext,
                                                                                            "helicopter_tiled.png", 2, 2);
        mBitmapTextureAtlasControllBtn = new BitmapTextureAtlas(pEngine.getTextureManager(), 256, 128);
        mControllBtnRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasControllBtn, pContext, "onscreen_control_base.png", 0, 0);
        mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasControllBtn, pContext, "onscreen_control_knob.png", 128, 0);
        /* Build and load the mBitmapTextureAtlas object */
        try {
            mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
        } catch (TextureAtlasBuilderException e) {
            e.printStackTrace();
        }

        mBitmapTextureAtlas.load();
        mBitmapTextureAtlasClouds.load();
        mBitmapTextureAtlasBullet.load();
        mBitmapTextureAtlasControllBtn.load();
    }
    
    /* All textures should have a method call for unloading once
     * they're no longer needed; ie. a level transition. */
    public synchronized void unloadGameTextures(){
        // call unload to remove the corresponding texture atlas from memory
        // FIXME: check whether all textures have been unloaded or not
        BitmapTextureAtlas mTexture = (BitmapTextureAtlas) mControllBtnRegion.getTexture();
        mTexture.unload();
        
        mTexture = (BitmapTextureAtlas) mOnScreenControlKnobTextureRegion.getTexture();
        mTexture.unload();
        
        mTexture = (BitmapTextureAtlas) mClouds.getTexture();
        mTexture.unload();
        
        mTexture = (BitmapTextureAtlas) mBullet.getTexture();
        mTexture.unload();

        // Once all textures have been unloaded, attempt to invoke the Garbage Collector
        System.gc();
    }
    
    /* As with textures, we can create methods to load sound/music objects
     * for different scene's within our games.
     */
    public synchronized void loadSounds(Engine pEngine, Context pContext){
        /* Set the base path for our SoundFactory and MusicFactory to
         * define where they will look for audio files.
         */
         SoundFactory.setAssetBasePath("sfx/");
         MusicFactory.setAssetBasePath("sfx/");

         // Load our "sound.mp3" file into a Sound object
         try {
             mSound = SoundFactory.createSoundFromAsset(pEngine.getSoundManager(), pContext, "bigboom.wav");
         } catch (IOException e) {
             e.printStackTrace();
         }
         // Load our "music.mp3" file into a music object
         try {
             mMusic = MusicFactory.createMusicFromAsset(pEngine.getMusicManager(), pContext, "music.mp3");
             mMusic.setVolume(0.5f);
         } catch (IOException e) {
             e.printStackTrace();
         }

         final ITexture scoreFontTexture = new BitmapTextureAtlas(pEngine.getTextureManager(), 256, 256, TextureOptions.BILINEAR);

         FontFactory.setAssetBasePath("font/");
         this.mScoreFont = FontFactory.createFromAsset(pEngine.getFontManager(), scoreFontTexture, pContext.getAssets(), "LCD.ttf", 32, true, android.graphics.Color.BLACK);
         this.mScoreFont.load();
    }
}
