/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package seniordesign.scanningapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

/**
 * The main activity of the application which shows debug information and a
 * glSurfaceView that renders graphic content.
 */
public class AugmentedRealityActivity extends Activity {
    // GLSurfaceView and its renderer, all of the graphic content is rendered
    // through OpenGL ES 2.0 in the native code.
    private AugmentedRealityRenderer mRenderer;
    private GLSurfaceView mGLView;

    // Screen size for normalizing the touch input for orbiting the render camera.
    private Point mScreenSize = new Point();
    private int mRes = 3;

    private SeekBar topSeekBar;
    private VerticalSeekBar rightSeekBar;
    private SeekBar bottomSeekBar;

    private GestureDetector mGestureDetector;
    private android.view.GestureDetector mTapDetector;
    private float mPitch = 0;
    private float mRoll = 0;
    private float mYaw = 0;
    private float mZoom = 5;

    private ArrayList<MarkerInfo> markerList = new ArrayList<>();

    // Tango Service connection.
    ServiceConnection mTangoServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            double res      = mRes * 0.01;
            double dmin     = 0.6f;
            double dmax     = mRes;
            int noise       = isNoiseFilterOn() ? 9 : 0;
            boolean land    = !isPortrait(AugmentedRealityActivity.this);
            boolean photo   = isPhotoModeOn();
            boolean txt     = isTexturingOn();

            if (android.os.Build.DEVICE.toLowerCase().startsWith("yellowstone"))
                land = !land;

            if (photo && (mRes > 0))
                dmax = 5.0;
            if(mRes == 0) {
                res = 0.005;
                dmax = photo ? 2.0 : 1.0;
            }

            // The following code block does setup and connection to Tango.
            JNINative.onTangoServiceConnectedAugmentedReality(service, res, dmin, dmax, noise, land, photo, txt);
            //JNINative.setView(0, 0, 0, 0, true);
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

        JNINative.onCreateAugmentedReality(this, display.getRotation());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Setting content view of this activity and getting the mIsAutoRecovery
        // flag from StartActivity.
        setContentView(R.layout.activity_augmented_reality);

        // OpenGL view where all of the graphics are drawn
        mGLView = (GLSurfaceView) findViewById(R.id.gl_surface_view);

        // Configure OpenGL renderer
        mGLView.setEGLContextClientVersion(2);

        // Configure OpenGL renderer. The RENDERMODE_WHEN_DIRTY is set explicitly
        // for reducing the CPU loadAugmentedReality. The request render function call is triggered
        // by the onTextureAvailable callback from the Tango Service in the native
        // code.
        mRenderer = new AugmentedRealityRenderer(getAssets());
        mGLView.setRenderer(mRenderer);

        topSeekBar = (SeekBar) findViewById(R.id.top_seekBar);
        topSeekBar.setProgress((int)(.5*topSeekBar.getMax()));
        topSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean byUser)
            {
                mRoll = (float) Math.toRadians(-(value - .5*seekBar.getMax()));
                JNINative.setViewAugmentedReality(mYaw, mPitch, mRoll);
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
                mYaw = (float) Math.toRadians(value - .5*seekBar.getMax());
                JNINative.setViewAugmentedReality(mYaw, mPitch, mRoll);
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
                JNINative.setViewAugmentedReality(mYaw, mPitch, mRoll);
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
                float f = getMoveFactor();
                JNINative.moveModelAugmentedReality(f,dx,dy);
            }

            @Override
            public void OnRotation(float angle)
            {
                /*mYaw = (float) Math.toRadians(-angle);
                JNINative.setViewAugmentedReality(mYaw, mPitch, mRoll);*/
            }

            @Override
            public void OnZoom(float diff)
            {
                mZoom += diff;
                int min = 1;
                if(mZoom < min)
                    mZoom = min;
                if(mZoom > 10)
                    mZoom = 10;
                JNINative.setZoomAugmentedReality(mZoom);
            }
        }, this);

        mTapDetector = new android.view.GestureDetector(AugmentedRealityActivity.this, new android.view.GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                JNINative.handleTouchAugmentedReality(motionEvent.getX()/mScreenSize.x,motionEvent.getY()/mScreenSize.y);
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

    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();

        JNINative.onPauseAugmentedReality();
        unbindService(mTangoServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        JNINative.onDestroyAugmentedReality();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();

        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        String markersString = getIntent().getStringExtra(RouteActivity.ROUTE_MARKERS_KEY);
        if(markersString!=null) {
            try {
                markerList = MarkerInfo.MarkersFromJson(markersString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            for(MarkerInfo info : markerList) {
                JNINative.renderMarkerAugmentedReality(info.getTransform()[0],info.getTransform()[1],info.getTransform()[2]);
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                /*String path = Environment.getExternalStorageDirectory().getPath() + "/Models/";
                String fileName = getIntent().getStringExtra(FileActivity.FILE_NAME_KEY);
                File file = new File(path,fileName);*/
                File root = getFilesDir();
                String modelFolder = getIntent().getStringExtra(WallActivity.FOLDER_NAME_KEY);
                File dir = new File(root, modelFolder);
                File file = new File(dir, WallActivity.WALL_MODEL_NAME);
                JNINative.loadAugmentedReality(file.getPath());
            }
        }).start();

        TangoInitializationHelper.bindTangoService(this, mTangoServiceConnection);
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

        JNINative.onConfigurationChangedAugmentedReality(display.getRotation());
    }

    private float getMoveFactor()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return 2.0f / (size.x + size.y) * (float)Math.pow(mZoom, 0.5f) * 2.0f;
    }

    // Request onGlSurfaceDrawFrameAugmentedReality on the glSurfaceView. This function is called from the
    // native code, and it is triggered from the onTextureAvailable callback from
    // the Tango Service.
    public void requestRender() {
        if (mGLView.getRenderMode() != GLSurfaceView.RENDERMODE_CONTINUOUSLY) {
            mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }
        mGLView.requestRender();
    }

    public void showMarkerInfo(final int index) {
        Log.d("ARActivity","SHOW MARKER INFO " + index);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_show_marker,null);

        TextView textView = (TextView) dialogView.findViewById(R.id.description_textbox);
        textView.setText(markerList.get(index).getDetails());
        textView = (TextView) dialogView.findViewById(R.id.hold_type_textbox);
        MarkerInfo.HOLD_TYPE hold = markerList.get(index).getHoldType();
        textView.setText(hold == null ? "" : hold.toString());
        MarkerInfo.MOVE_TYPE move = markerList.get(index).getMoveType();
        textView = (TextView) dialogView.findViewById(R.id.move_type_textbox);
        textView.setText(move == null ? "" : move.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true).setPositiveButton("Okay", null)
                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        markerList.remove(index);
                        JNINative.removeMarkerAtViewer(index);
                    }
                });
        builder.setView(dialogView);
        builder.create().show();
    }


    public boolean isNoiseFilterOn()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String key = getString(R.string.pref_noisefilter);
        return pref.getBoolean(key, false);
    }

    public static boolean isPortrait(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pref_landscape);
        return !pref.getBoolean(key, false);
    }

    public boolean isPhotoModeOn()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String key = getString(R.string.pref_photomode);
        return pref.getBoolean(key, false);
    }

    public boolean isTexturingOn()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String key = getString(R.string.pref_texture);
        return pref.getBoolean(key, true);
    }
}
