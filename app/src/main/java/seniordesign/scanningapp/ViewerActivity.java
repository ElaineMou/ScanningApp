package seniordesign.scanningapp;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import java.io.File;

public class ViewerActivity extends AppCompatActivity{

    // GLSurfaceView and its renderer, all of the graphic content is rendered
    // through OpenGL ES 2.0 in the native code.
    private ViewerRenderer mRenderer;
    private GLSurfaceView mGLView;

    // Screen size for normalizing the touch input for orbiting the render camera.
    private Point mScreenSize = new Point();
    private int mRes = 3;

    private SeekBar topSeekBar;
    private VerticalSeekBar rightSeekBar;
    private SeekBar bottomSeekBar;
    private FloatingActionButton showHideFab;
    private FloatingActionButton addMarksFab;

    private GestureDetector mGestureDetector;
    private android.view.GestureDetector mTapDetector;
    private float mMoveX = 0;
    private float mMoveY = 0;
    private float mMoveZ;
    private float mPitch = 0;
    private float mRoll = 0;
    private float mYaw = 0;
    private float mZoom = 5;
    private boolean markersVisible = true;
    private boolean addingMarkers = false;
    // Tango Service connection.
    ServiceConnection mTangoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            double res      = mRes * 0.01;
            double dmin     = 0.6f;
            double dmax     = mRes;

            if(mRes == 0) {
                res = 0.005;
                dmax = 1.0;
            }

            // The following code block does setup and connection to Tango.
            TangoJNINative.onTangoServiceConnectedViewer(service, res, dmin, dmax, 9, false, false, false);
            //TangoJNINative.setView(0, 0, 0, 0, true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // Handle this if you need to gracefully shutdown/retry
            // in the event that Tango itself crashes/gets upgraded while running.
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getSize(mScreenSize);

        TangoJNINative.onCreateViewer(this, display.getRotation());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Setting content view of this activity and getting the mIsAutoRecovery
        // flag from StartActivity.
        setContentView(R.layout.activity_viewer);

        // OpenGL view where all of the graphics are drawn
        mGLView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

        // Configure OpenGL renderer
        mGLView.setEGLContextClientVersion(2);

        // Configure OpenGL renderer. The RENDERMODE_WHEN_DIRTY is set explicitly
        // for reducing the CPU load. The request render function call is triggered
        // by the onTextureAvailable callback from the Tango Service in the native
        // code.
        mRenderer = new ViewerRenderer(getAssets());
        mGLView.setRenderer(mRenderer);

        topSeekBar = (SeekBar) findViewById(R.id.top_seekBar);
        topSeekBar.setProgress((int)(.5*topSeekBar.getMax()));
        topSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean byUser)
            {
                mRoll = (float) Math.toRadians(-(value - .5*seekBar.getMax()));
                TangoJNINative.setViewViewer(mYaw, mPitch, mRoll, mMoveX, mMoveY, mMoveZ);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });

        rightSeekBar = (VerticalSeekBar) findViewById(R.id.right_seekBar);
        rightSeekBar.setProgress((int)(.5*rightSeekBar.getMax()));
        rightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean byUser)
            {
                mYaw = (float) Math.toRadians(-(value - .5*seekBar.getMax()));
                TangoJNINative.setViewViewer(mYaw, mPitch, mRoll, mMoveX, mMoveY, mMoveZ);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });

        bottomSeekBar = (SeekBar) findViewById(R.id.bottom_seekBar);
        bottomSeekBar.setProgress((int)(.5*bottomSeekBar.getMax()));
        bottomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean byUser)
            {
                mPitch = (float) Math.toRadians(-(value - .5*seekBar.getMax()));
                TangoJNINative.setViewViewer(mYaw, mPitch, mRoll, mMoveX, mMoveY, mMoveZ);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });

        mGestureDetector = new GestureDetector(new GestureDetector.GestureListener()
        {
            @Override
            public void OnMove(float dx, float dy)
            {
                double angle = -mYaw;
                float f = getMoveFactor();
                mMoveX += dx * f * Math.cos( angle ) + dy * f * Math.sin( angle );
                mMoveY += dx * f * Math.sin( angle ) + dy * f * Math.cos( angle );
                TangoJNINative.setViewViewer(mYaw, mPitch, mRoll, mMoveX, mMoveY, mMoveZ);
            }

            @Override
            public void OnRotation(float angle)
            {
                mYaw = (float) Math.toRadians(-angle);
                TangoJNINative.setViewViewer(mYaw, mPitch, mRoll, mMoveX, mMoveY, mMoveZ);
            }

            @Override
            public void OnZoom(float diff)
            {
                mZoom -= diff;
                int min = 1;
                if(mZoom < min)
                    mZoom = min;
                if(mZoom > 10)
                    mZoom = 10;
                TangoJNINative.setZoomViewer(mZoom);
            }
        }, this);

        mTapDetector = new android.view.GestureDetector(ViewerActivity.this, new android.view.GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                TangoJNINative.handleTouchViewer(motionEvent.getX()/mScreenSize.x,motionEvent.getY()/mScreenSize.y);
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return true;
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showHideFab = (FloatingActionButton) findViewById(R.id.fab);
        showHideFab.setRippleColor(ContextCompat.getColor(ViewerActivity.this,R.color.colorPrimary));
        showHideFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markersVisible = !markersVisible;
                if(!markersVisible) {
                    addingMarkers = false;
                    TangoJNINative.setAddingMarkersViewer(addingMarkers);
                    addMarksFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ViewerActivity.this, R.color.colorAccent)));
                    ((FloatingActionButton)view).setImageDrawable(ContextCompat.getDrawable(ViewerActivity.this, R.drawable.openeye));
                } else {
                    ((FloatingActionButton)view).setImageDrawable(ContextCompat.getDrawable(ViewerActivity.this, R.drawable.blindeye));
                }
                TangoJNINative.setMarkersVisibleViewer(markersVisible);
            }
        });

        addMarksFab = (FloatingActionButton) findViewById(R.id.fab2);
        addMarksFab.setRippleColor(ContextCompat.getColor(ViewerActivity.this,R.color.colorPrimary));
        addMarksFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addingMarkers = !addingMarkers;
                if(addingMarkers) {
                    markersVisible = true;
                    showHideFab.setImageDrawable(ContextCompat.getDrawable(ViewerActivity.this, R.drawable.blindeye));
                    view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ViewerActivity.this, R.color.pressedButton)));
                } else {
                    view.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ViewerActivity.this, R.color.colorAccent)));
                }
                TangoJNINative.setAddingMarkersViewer(addingMarkers);
            }
        });
    }



    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();

        TangoJNINative.onPauseViewer();
        unbindService(mTangoServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TangoJNINative.onDestroyViewer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();

        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = Environment.getExternalStorageDirectory().getPath() + "/Models/";
                String fileName = getIntent().getStringExtra(FileActivity.FILE_NAME_KEY);
                File file = new File(path,fileName);
                TangoJNINative.loadViewer(file.toString());
            }
        }).start();

        TangoInitializationHelper.bindTangoService(this, mTangoServiceConnection);
    }


    // This function is called from the
    // native code.
    public void setAddingFalse() {
        addingMarkers = false;
        addMarksFab.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ViewerActivity.this, R.color.colorAccent)));
    }


    @Override
    public void onBackPressed()
    {
        System.exit(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mTapDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getSize(mScreenSize);

        TangoJNINative.onConfigurationChangedViewer(display.getRotation());
    }

    private float getMoveFactor()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return 2.0f / (size.x + size.y) * (float)Math.pow(mZoom, 0.5f) * 2.0f;
    }
}
