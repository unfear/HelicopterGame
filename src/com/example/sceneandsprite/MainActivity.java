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
import org.andengine.entity.sprite.Sprite;
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

    private static final int BIRD_SPEED = 5;
    private static int BIRD_COUNTER = 0;
    private static int HEALTH_COUNTER = 5;

    // Declare a Camera object for our activity
    private Camera mCamera;

    // Declare a Scene object for our activity
    private Scene mScene;
    
    BuildableBitmapTextureAtlas mBitmapTextureAtlas;
    private TiledTextureRegion mHelicopterTextureRegion;
    private TiledTextureRegion mBirdTextureRegion;
    private BitmapTextureAtlas mBitmapTextureAtlasControllBtn;
    private BitmapTextureAtlas mBitmapTextureAtlasClouds;
    private BitmapTextureAtlas mBitmapTextureAtlasBullet;
    private TiledTextureRegion mExplodeTextureRegion;

    private ITextureRegion mControllBtnRegion;
    private ITextureRegion mOnScreenControlKnobTextureRegion;
    private ITextureRegion mClouds;
    private ITextureRegion mBullet;
    private DigitalOnScreenControl mDigitalOnScreenControl;

    private AnimatedSprite mAnimatedBirdSprite;
    private AnimatedSprite mAnimatedSecondBirdSprite;
    private AnimatedSprite mAnimatedThirdBirdSprite;
    private AnimatedSprite mAnimatedFourthBirdSprite;
    private AnimatedSprite mAnimatedFifthBirdSprite;
    private AnimatedSprite mAnimatedSixthBirdSprite;
    private AnimatedSprite mAnimatedExplodeSprite;

    private Sprite mCloudsSprite1;
    private Sprite mCloudsSprite2;
    private Sprite mCloudsSprite3;
    
    private Sprite mBulletSprite;
    
    Music mMusic;
    Sound mSound;

    private final SparseArray<Text> mScoreTextMap = new SparseArray<Text>();
    private Font mScoreFont;
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
        mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(mEngine.getTextureManager(), 1359, 630, 
                                                            TextureOptions.BILINEAR);
        /* Create the TiledTextureRegion object, passing in the usual
        parameters, as well as the number of rows and columns in our sprite sheet
        for the final two parameters */
        mBitmapTextureAtlasClouds = new BitmapTextureAtlas(this.getTextureManager(), 100, 50);
        mBitmapTextureAtlasBullet = new BitmapTextureAtlas(this.getTextureManager(), 10, 12);

        mClouds = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasClouds, this, "cloud.png", 0, 0);

        mBullet = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlasBullet, this, "bullet.png", 0, 0);

        mBirdTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, 
                                                                                            "bird.png", 3, 4);

        mExplodeTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBitmapTextureAtlas, this, 
                                                                                                "explosion.png", 5, 1);

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
        mBitmapTextureAtlasClouds.load();
        mBitmapTextureAtlasBullet.load();
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

        mCloudsSprite1 = new Sprite(0, 0, this.mClouds, this.getVertexBufferObjectManager());
        mCloudsSprite2 = new Sprite(0, 100, this.mClouds, this.getVertexBufferObjectManager());
        mCloudsSprite3 = new Sprite(0, 200, this.mClouds, this.getVertexBufferObjectManager());

        /* Continuously flying helicopter. */
        final AnimatedSprite helicopter = new AnimatedSprite(320, HEIGHT - 128, this.mHelicopterTextureRegion, this.getVertexBufferObjectManager());
        helicopter.animate(new long[] { 100, 100 }, 1, 2, true);

        mBulletSprite = new Sprite(-20, -20, this.mBullet, this.getVertexBufferObjectManager());
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

        mAnimatedExplodeSprite = new AnimatedSprite(-100, -100, mExplodeTextureRegion, 
                this.getVertexBufferObjectManager());

        mScene.attachChild(mCloudsSprite1);
        mScene.attachChild(mCloudsSprite2);
        mScene.attachChild(mCloudsSprite3);
        mScene.attachChild(mBulletSprite);
        mScene.attachChild(helicopter);
        mScene.attachChild(mAnimatedBirdSprite);
        mScene.attachChild(mAnimatedSecondBirdSprite);
        mScene.attachChild(mAnimatedThirdBirdSprite);
        mScene.attachChild(mAnimatedFourthBirdSprite);
        mScene.attachChild(mAnimatedFifthBirdSprite);
        mScene.attachChild(mAnimatedSixthBirdSprite);
        mScene.attachChild(mAnimatedExplodeSprite);

        final Text scoreLeft = new Text(0, 0, this.mScoreFont, "Score: 0", 10, this.getVertexBufferObjectManager());
        scoreLeft.setPosition(0, scoreLeft.getY());
        this.mScoreTextMap.put(0, scoreLeft);
        mScene.attachChild(scoreLeft);

        final Text healthRight = new Text(0, 0, this.mScoreFont, "Health: 5", 10, this.getVertexBufferObjectManager());
        healthRight.setPosition(WIDTH-200, 0);
        this.mScoreTextMap.put(1, healthRight);
        mScene.attachChild(healthRight);

        /* Make the Bird move every 0.2 seconds. */
        mScene.registerUpdateHandler(new TimerHandler(0.1f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(final TimerHandler pTimerHandler)
            {
                float xPos = mAnimatedBirdSprite.getX() + 5;
                if(xPos > WIDTH - mAnimatedBirdSprite.getWidth())
                    xPos = 0;

                float x2Pos = mAnimatedSecondBirdSprite.getX() + BIRD_SPEED;
                if(x2Pos > WIDTH - mAnimatedSecondBirdSprite.getWidth())
                    x2Pos = 0;
                
                float x3Pos = mAnimatedThirdBirdSprite.getX() + BIRD_SPEED;
                if(x3Pos > WIDTH - mAnimatedThirdBirdSprite.getWidth())
                    x3Pos = 0;
                
                float x4Pos = mAnimatedFourthBirdSprite.getX() + BIRD_SPEED;
                if(x4Pos > WIDTH - mAnimatedFourthBirdSprite.getWidth())
                    x4Pos = 0;
                
                float x5Pos = mAnimatedFifthBirdSprite.getX() + BIRD_SPEED;
                if(x5Pos > WIDTH - mAnimatedFifthBirdSprite.getWidth())
                    x5Pos = 0;

                float x6Pos = mAnimatedSixthBirdSprite.getX() + BIRD_SPEED;
                if(x6Pos > WIDTH - mAnimatedSixthBirdSprite.getWidth())
                    x6Pos = 0;

                float x7Pos = mCloudsSprite1.getX() + 2;
                if(x7Pos > WIDTH)
                    x7Pos = -mCloudsSprite1.getWidth();

                float x8Pos = mCloudsSprite2.getX() + 3;
                if(x8Pos > WIDTH)
                    x8Pos = -mCloudsSprite2.getWidth();

                float x9Pos = mCloudsSprite3.getX() + 1;
                if(x9Pos > WIDTH)
                    x9Pos = -mCloudsSprite3.getWidth();

                float xBulletPos = mBulletSprite.getX() - 10;
                float yBulletPos = mBulletSprite.getY();

                if(xBulletPos < 0)
                {
                    xBulletPos = helicopter.getX();
                    yBulletPos = helicopter.getY();
                }
                
                updateFlyingObjPosition(xPos, mAnimatedBirdSprite.getY(), mAnimatedBirdSprite);
                updateFlyingObjPosition(x2Pos, mAnimatedSecondBirdSprite.getY(), mAnimatedSecondBirdSprite);
                updateFlyingObjPosition(x3Pos, mAnimatedThirdBirdSprite.getY(), mAnimatedThirdBirdSprite);
                updateFlyingObjPosition(x4Pos, mAnimatedFourthBirdSprite.getY(), mAnimatedFourthBirdSprite);
                updateFlyingObjPosition(x5Pos, mAnimatedFifthBirdSprite.getY(), mAnimatedFifthBirdSprite);
                updateFlyingObjPosition(x6Pos, mAnimatedSixthBirdSprite.getY(), mAnimatedSixthBirdSprite);
                updateFlyingObjPosition(x7Pos, mCloudsSprite1.getY(), mCloudsSprite1);
                updateFlyingObjPosition(x8Pos, mCloudsSprite2.getY(), mCloudsSprite2);
                updateFlyingObjPosition(x9Pos, mCloudsSprite3.getY(), mCloudsSprite3);
                updateFlyingObjPosition(xBulletPos, yBulletPos, mBulletSprite);

                if(mAnimatedBirdSprite.collidesWith(helicopter) || mAnimatedSecondBirdSprite.collidesWith(helicopter)
                   || mAnimatedThirdBirdSprite.collidesWith(helicopter) || mAnimatedFourthBirdSprite.collidesWith(helicopter)
                   || mAnimatedFifthBirdSprite.collidesWith(helicopter) || mAnimatedSixthBirdSprite.collidesWith(helicopter) )
                {
                    if(helicopter.isAnimationRunning())
                    {
                        mSound.play();
                        helicopter.setCurrentTileIndex(3);
                        helicopter.stopAnimation(3);
                        HEALTH_COUNTER--;
                        updateHealth(1, HEALTH_COUNTER);
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
                final int ACCURACY = 30;
                if(mAnimatedBirdSprite.collidesWith(mBulletSprite))
                {
                    mAnimatedExplodeSprite.setPosition(mAnimatedBirdSprite.getX() - ACCURACY, mAnimatedBirdSprite.getY() - ACCURACY);
                    updateFlyingObjPosition(0, mAnimatedBirdSprite.getY(), mAnimatedBirdSprite);
                    mAnimatedExplodeSprite.animate(new long[] { 50, 50, 50, 50, 50 }, 0, 4, false);
                    updateFlyingObjPosition(helicopter.getX(), helicopter.getY(), mBulletSprite);
                    BIRD_COUNTER++;
                }
                else if(mAnimatedSecondBirdSprite.collidesWith(mBulletSprite))
                {
                    mAnimatedExplodeSprite.setPosition(mAnimatedSecondBirdSprite.getX() - ACCURACY, mAnimatedSecondBirdSprite.getY() - ACCURACY);
                    updateFlyingObjPosition(0, mAnimatedSecondBirdSprite.getY(), mAnimatedSecondBirdSprite);
                    mAnimatedExplodeSprite.animate(new long[] { 50, 50, 50, 50, 50 }, 0, 4, false);
                    updateFlyingObjPosition(helicopter.getX(), helicopter.getY(), mBulletSprite);
                    BIRD_COUNTER++;
                }
                else if(mAnimatedThirdBirdSprite.collidesWith(mBulletSprite))
                {
                    mAnimatedExplodeSprite.setPosition(mAnimatedThirdBirdSprite.getX() - ACCURACY, mAnimatedThirdBirdSprite.getY() - ACCURACY);
                    updateFlyingObjPosition(0, mAnimatedThirdBirdSprite.getY(), mAnimatedThirdBirdSprite);
                    mAnimatedExplodeSprite.animate(new long[] { 50, 50, 50, 50, 50 }, 0, 4, false);
                    updateFlyingObjPosition(helicopter.getX(), helicopter.getY(), mBulletSprite);
                    BIRD_COUNTER++;
                }
                else if(mAnimatedFourthBirdSprite.collidesWith(mBulletSprite))
                {
                    mAnimatedExplodeSprite.setPosition(mAnimatedFourthBirdSprite.getX() - ACCURACY, mAnimatedFourthBirdSprite.getY() - ACCURACY);
                    updateFlyingObjPosition(0, mAnimatedFourthBirdSprite.getY(), mAnimatedFourthBirdSprite);
                    mAnimatedExplodeSprite.animate(new long[] { 50, 50, 50, 50, 50 }, 0, 4, false);
                    updateFlyingObjPosition(helicopter.getX(), helicopter.getY(), mBulletSprite);
                    BIRD_COUNTER++;
                }
                else if(mAnimatedFifthBirdSprite.collidesWith(mBulletSprite))
                {
                    mAnimatedExplodeSprite.setPosition(mAnimatedFifthBirdSprite.getX() - ACCURACY, mAnimatedFifthBirdSprite.getY() - ACCURACY);
                    updateFlyingObjPosition(0, mAnimatedFifthBirdSprite.getY(), mAnimatedFifthBirdSprite);
                    mAnimatedExplodeSprite.animate(new long[] { 50, 50, 50, 50, 50 }, 0, 4, false);
                    updateFlyingObjPosition(helicopter.getX(), helicopter.getY(), mBulletSprite);
                    BIRD_COUNTER++;
                }
                else if(mAnimatedSixthBirdSprite.collidesWith(mBulletSprite))
                {
                    mAnimatedExplodeSprite.setPosition(mAnimatedSixthBirdSprite.getX() - ACCURACY, mAnimatedSixthBirdSprite.getY() - ACCURACY);
                    updateFlyingObjPosition(0, mAnimatedSixthBirdSprite.getY(), mAnimatedSixthBirdSprite);
                    mAnimatedExplodeSprite.animate(new long[] { 50, 50, 50, 50, 50 }, 0, 4, false);
                    updateFlyingObjPosition(helicopter.getX(), helicopter.getY(), mBulletSprite);
                    BIRD_COUNTER++;
                }

                if(HEALTH_COUNTER > 0)
                    updateScore(0, BIRD_COUNTER);

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

    public void updateFlyingObjPosition(final float pX, final float pY, Sprite sprite)
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
        if(pPoints >= 99)
            scoreText.setText("YOU WIN");
        else
            scoreText.setText("Score: " + String.valueOf(pPoints));

        /* Adjust position of left Score, so that it doesn't overlap the middle line. */
        scoreText.setPosition(0, scoreText.getY());
    }

    public void updateHealth(final int pPaddleID, final int pPoints) {
        final Text scoreText = this.mScoreTextMap.get(pPaddleID);
        if(pPoints <= 0)
            scoreText.setText("Game Over");
        else
            scoreText.setText("Health: " + String.valueOf(pPoints));

        /* Adjust position of left Score, so that it doesn't overlap the middle line. */
        scoreText.setPosition(WIDTH-200, 0);
    }
}
