package com.example.sceneandsprite;

import java.io.IOException;

import org.andengine.audio.music.Music;
import org.andengine.audio.music.MusicFactory;
import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
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
import org.andengine.ui.activity.BaseGameActivity;

import android.opengl.GLES20;
import android.util.SparseArray;

public class MainActivity extends BaseGameActivity {

     // The following constants will be used to define the width and height
    // of our game's camera view
    private static final int WIDTH = 532;
    private static final int HEIGHT = 320;

    // Declare a Camera object for our activity
    private Camera mCamera;

    // Declare a Scene object for our activity
    private Scene mScene;
    
    BuildableBitmapTextureAtlas mBitmapTextureAtlas;
    private TiledTextureRegion mHelicopterTextureRegion;
    private TiledTextureRegion mBirdTextureRegion;
    private BitmapTextureAtlas mBitmapTextureAtlasControllBtn;

    private ITextureRegion mControllBtnRegion;
    private ITextureRegion mOnScreenControlKnobTextureRegion;
    private DigitalOnScreenControl mDigitalOnScreenControl;

    private AnimatedSprite mAnimatedBirdSprite;
    private AnimatedSprite mAnimatedSecondBirdSprite;
    private AnimatedSprite mAnimatedThirdBirdSprite;
    private AnimatedSprite mAnimatedFourthBirdSprite;
    private AnimatedSprite mAnimatedFifthBirdSprite;
    private AnimatedSprite mAnimatedSixthBirdSprite;

    Music mMusic;
    Sound mSound;

    private final SparseArray<Text> mScoreTextMap = new SparseArray<Text>();
    private Font mScoreFont;
    private int mScore = 0;
    /*
     * The onCreateEngineOptions method is responsible for creating the options to be
     * applied to the Engine object once it is created. The options include,
     * but are not limited to enabling/disable sounds and music, defining multitouch
     * options, changing rendering options and more.
     */
    @Override
    public EngineOptions onCreateEngineOptions() {

        // Define our mCamera object
        mCamera = new Camera(0, 0, WIDTH, HEIGHT);

        // Declare & Define our engine options to be applied to our Engine object
        EngineOptions engineOptions = new EngineOptions(true,
                ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(WIDTH, HEIGHT),
                mCamera);

        // It is necessary in a lot of applications to define the following
        // wake lock options in order to disable the device's display
        // from turning off during gameplay due to inactivity
        engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);

        // we plan to use Sound and Music objects in our game
        engineOptions.getAudioOptions().setNeedsMusic(true);
        engineOptions.getAudioOptions().setNeedsSound(true);

        // Return the engineOptions object, passing it to the engine
        return engineOptions;
    }

    /*
     * The onCreateResources method is in place for resource loading, including textures,
     * sounds, and fonts for the most part. 
     */
    @Override
    public void onCreateResources(
            OnCreateResourcesCallback pOnCreateResourcesCallback) {

        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        /* Create the texture atlas at the same dimensions as the image
        (300x50)*/
        mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(mEngine.getTextureManager(), 768, 512, 
                                                            TextureOptions.BILINEAR);
        /* Create the TiledTextureRegion object, passing in the usual
        parameters, as well as the number of rows and columns in our sprite sheet
        for the final two parameters */
        mBirdTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, 
                                                                                            "bird.png", 3, 4);
        mHelicopterTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this,
                                                                                            "helicopter_tiled.png", 2, 2);
        mBitmapTextureAtlasControllBtn = new BitmapTextureAtlas(this.getTextureManager(), 256, 128);
        mControllBtnRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasControllBtn, this, "onscreen_control_base.png", 0, 0);
        mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasControllBtn, this, "onscreen_control_knob.png", 128, 0);
        /* Build and load the mBitmapTextureAtlas object */
        try {
            mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
        } catch (TextureAtlasBuilderException e) {
            e.printStackTrace();
        }

        mBitmapTextureAtlas.load();
        mBitmapTextureAtlasControllBtn.load();

        /* Set the base path for our SoundFactory and MusicFactory to
         * define where they will look for audio files.
         */
         SoundFactory.setAssetBasePath("sfx/");
         MusicFactory.setAssetBasePath("sfx/");

         // Load our "sound.mp3" file into a Sound object
         try {
             mSound = SoundFactory.createSoundFromAsset(getSoundManager(), this, "bigboom.wav");
         } catch (IOException e) {
             e.printStackTrace();
         }
         // Load our "music.mp3" file into a music object
         try {
             mMusic = MusicFactory.createMusicFromAsset(getMusicManager(), this, "music.mp3");
             mMusic.setVolume(0.5f);
         } catch (IOException e) {
             e.printStackTrace();
         }

         final ITexture scoreFontTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);

         FontFactory.setAssetBasePath("font/");
         this.mScoreFont = FontFactory.createFromAsset(this.getFontManager(), scoreFontTexture, this.getAssets(), "LCD.ttf", 32, true, android.graphics.Color.BLACK);
         this.mScoreFont.load();
        /* We should notify the pOnCreateResourcesCallback that we've finished
         * loading all of the necessary resources in our game AFTER they are loaded.
         * onCreateResourcesFinished() should be the last method called.  */
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    /* The onCreateScene method is in place to handle the scene initialization and setup.
     * In this method, we must at least *return our mScene object* which will then 
     * be set as our main scene within our Engine object (handled "behind the scenes").
     * This method might also setup touch listeners, update handlers, or more events directly
     * related to the scene.
     */
    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) {
        // Create the Scene object
        this.mEngine.registerUpdateHandler(new FPSLogger());
        mScene = new Scene();
        mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));

        /* Continuously flying helicopter. */
        final AnimatedSprite helicopter = new AnimatedSprite(320, HEIGHT - 128, this.mHelicopterTextureRegion, this.getVertexBufferObjectManager());
        helicopter.animate(new long[] { 100, 100 }, 1, 2, true);
        
        /* Create a new animated sprite in the center of the scene */

        mAnimatedBirdSprite = new AnimatedSprite(120, 10, mBirdTextureRegion, 
                this.getVertexBufferObjectManager());
        mAnimatedBirdSprite.animate(new long[] { 200, 200, 200 }, 6, 8, true);

        mAnimatedSecondBirdSprite = new AnimatedSprite(0, 60, mBirdTextureRegion, 
                this.getVertexBufferObjectManager());
        mAnimatedSecondBirdSprite.animate(new long[] { 250, 250, 250 }, 6, 8, true);

        mAnimatedThirdBirdSprite = new AnimatedSprite(60, 130, mBirdTextureRegion, 
                this.getVertexBufferObjectManager());
        mAnimatedThirdBirdSprite.animate(new long[] { 250, 250, 250 }, 6, 8, true);
        
        mAnimatedFourthBirdSprite = new AnimatedSprite(10, 180, mBirdTextureRegion, 
                this.getVertexBufferObjectManager());
        mAnimatedFourthBirdSprite.animate(new long[] { 250, 250, 250 }, 6, 8, true);
        
        mAnimatedFifthBirdSprite = new AnimatedSprite(100, 230, mBirdTextureRegion, 
                this.getVertexBufferObjectManager());
        mAnimatedFifthBirdSprite.animate(new long[] { 250, 260, 250 }, 6, 8, true);
        
        mAnimatedSixthBirdSprite = new AnimatedSprite(20, 280, mBirdTextureRegion, 
                this.getVertexBufferObjectManager());
        mAnimatedSixthBirdSprite.animate(new long[] { 250, 310, 250 }, 6, 8, true);

        mScene.attachChild(helicopter);
        mScene.attachChild(mAnimatedBirdSprite);
        mScene.attachChild(mAnimatedSecondBirdSprite);
        mScene.attachChild(mAnimatedThirdBirdSprite);
        mScene.attachChild(mAnimatedFourthBirdSprite);
        mScene.attachChild(mAnimatedFifthBirdSprite);
        mScene.attachChild(mAnimatedSixthBirdSprite);

        final Text scoreLeft = new Text(0, 0, this.mScoreFont, "Score: 0", 10, this.getVertexBufferObjectManager());
        scoreLeft.setPosition(0, scoreLeft.getY());
        this.mScoreTextMap.put(0, scoreLeft);
        mScene.attachChild(scoreLeft);

        /* Make the Bird move every 0.2 seconds. */
        mScene.registerUpdateHandler(new TimerHandler(0.2f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(final TimerHandler pTimerHandler)
            {
                float xPos = mAnimatedBirdSprite.getX() + 10;
                if(xPos > WIDTH - mAnimatedBirdSprite.getWidth())
                    xPos = 0;

                float x2Pos = mAnimatedSecondBirdSprite.getX() + 10;
                if(x2Pos > WIDTH - mAnimatedSecondBirdSprite.getWidth())
                    x2Pos = 0;
                
                float x3Pos = mAnimatedThirdBirdSprite.getX() + 10;
                if(x3Pos > WIDTH - mAnimatedThirdBirdSprite.getWidth())
                    x3Pos = 0;
                
                float x4Pos = mAnimatedFourthBirdSprite.getX() + 10;
                if(x4Pos > WIDTH - mAnimatedFourthBirdSprite.getWidth())
                    x4Pos = 0;
                
                float x5Pos = mAnimatedFifthBirdSprite.getX() + 10;
                if(x5Pos > WIDTH - mAnimatedFifthBirdSprite.getWidth())
                    x5Pos = 0;
                
                float x6Pos = mAnimatedSixthBirdSprite.getX() + 10;
                if(x6Pos > WIDTH - mAnimatedSixthBirdSprite.getWidth())
                    x6Pos = 0;

                updateBirdPosition(xPos, mAnimatedBirdSprite.getY(), mAnimatedBirdSprite);
                updateBirdPosition(x2Pos, mAnimatedSecondBirdSprite.getY(), mAnimatedSecondBirdSprite);
                updateBirdPosition(x3Pos, mAnimatedThirdBirdSprite.getY(), mAnimatedThirdBirdSprite);
                updateBirdPosition(x4Pos, mAnimatedFourthBirdSprite.getY(), mAnimatedFourthBirdSprite);
                updateBirdPosition(x5Pos, mAnimatedFifthBirdSprite.getY(), mAnimatedFifthBirdSprite);
                updateBirdPosition(x6Pos, mAnimatedSixthBirdSprite.getY(), mAnimatedSixthBirdSprite);

                if(mAnimatedBirdSprite.collidesWith(helicopter) || mAnimatedSecondBirdSprite.collidesWith(helicopter)
                   || mAnimatedThirdBirdSprite.collidesWith(helicopter) || mAnimatedFourthBirdSprite.collidesWith(helicopter)
                   || mAnimatedFifthBirdSprite.collidesWith(helicopter) || mAnimatedSixthBirdSprite.collidesWith(helicopter) )
                {
                    if(helicopter.isAnimationRunning())
                    {
                        mSound.play();
                        helicopter.setCurrentTileIndex(3);
                        helicopter.stopAnimation(3);
                        mScore++;
                        updateScore(0, mScore);
                    }
                }
                else
                {
                    if(!helicopter.isAnimationRunning())
                    {
                        helicopter.reset();
                        helicopter.animate(new long[] { 100, 100 }, 1, 2, true);
                    }
                }

                if(helicopter.getX() > WIDTH)
                    helicopter.setX(WIDTH);
                if(helicopter.getX() < 0)
                    helicopter.setX(0);
                if(helicopter.getY() > HEIGHT)
                    helicopter.setY(HEIGHT);
                if(helicopter.getY() < 0)
                    helicopter.setY(0);
            }
        }));

        final PhysicsHandler physicsHandler = new PhysicsHandler(helicopter);
        helicopter.registerUpdateHandler(physicsHandler);

        this.mDigitalOnScreenControl = new DigitalOnScreenControl(0, HEIGHT - this.mControllBtnRegion.getHeight(), this.mCamera, this.mControllBtnRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IOnScreenControlListener() {
            @Override
            public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
                float x = pValueX * 100;
                float y = pValueY * 100;
                
                physicsHandler.setVelocity(x, y);
            }
        });
        this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        this.mDigitalOnScreenControl.getControlBase().setAlpha(0.5f);
        this.mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
        this.mDigitalOnScreenControl.getControlBase().setScale(1.25f);
        this.mDigitalOnScreenControl.getControlKnob().setScale(1.25f);
        this.mDigitalOnScreenControl.refreshControlKnobPosition();
        this.mDigitalOnScreenControl.setAllowDiagonal(true);

        mScene.setChildScene(this.mDigitalOnScreenControl);
        // Notify the callback that we're finished creating the scene, returning
        // mScene to the mEngine object (handled automatically)

        pOnCreateSceneCallback.onCreateSceneFinished(mScene);
    }

    /* The onPopulateScene method was introduced to AndEngine as a way of separating
     * scene-creation from scene population. This method is in place for attaching 
     * child entities to the scene once it has already been returned to the engine and
     * set as our main scene.
     */
    @Override
    public void onPopulateScene(Scene pScene,
            OnPopulateSceneCallback pOnPopulateSceneCallback) {

        // onPopulateSceneFinished(), similar to the resource and scene callback
        // methods, should be called once we are finished populating the scene.
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    public void updateBirdPosition(final float pX, final float pY, AnimatedSprite sprite)
    {
        sprite.setPosition(pX, pY);
    }

    /* Music objects which loop continuously should be played in
    * onResumeGame() of the activity life cycle
    */
    @Override
    public synchronized void onResumeGame() {
        if(mMusic != null && !mMusic.isPlaying()){
            mMusic.play();
        }
        super.onResumeGame();
    }

    /* Music objects which loop continuously should be paused in
    * onPauseGame() of the activity life cycle
    */
    @Override
    public synchronized void onPauseGame() {
        if(mMusic != null && mMusic.isPlaying()){
            mMusic.pause();
        }
        super.onPauseGame();
    }
    
    public void updateScore(final int pPaddleID, final int pPoints) {
        final Text scoreText = this.mScoreTextMap.get(pPaddleID);
        if(pPoints >= 10)
            scoreText.setText("GAME OVER");
        else
            scoreText.setText("Score: " + String.valueOf(pPoints));

        /* Adjust position of left Score, so that it doesn't overlap the middle line. */
        scoreText.setPosition(0, scoreText.getY());
    }
}
