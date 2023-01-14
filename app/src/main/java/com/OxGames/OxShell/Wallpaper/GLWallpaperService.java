package com.OxGames.OxShell.Wallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

//Source: https://www.learnopengles.com/how-to-use-opengl-es-2-in-an-android-live-wallpaper/
public class GLWallpaperService extends WallpaperService {
    private Context context;

    @Override
    public Engine onCreateEngine() {
        Log.d("GLWallpaperService", "onCreateEngine");
        context = this;
        return new GLEngine();
    }

    public class GLEngine extends WallpaperService.Engine implements SensorEventListener {
        private WallpaperGLSurfaceView glSurfaceView;
        private GLSurfaceView.Renderer renderer;
        private boolean rendererHasBeenSet;

        // source: https://stackoverflow.com/questions/11925039/wallpaperservice-and-accelerometer
        private SensorManager mSensorManager;
        private Sensor mAccelerometer;
        private Display mDisplay;

        public GLEngine() {
            super();
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            mDisplay = mWindowManager.getDefaultDisplay();
        }

        public void registerSensors() {
            Log.d("GLEngine", "registerSensors()");
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        public void unregisterSensors() {
            Log.d("GLEngine", "unregisterSensors()");
            mSensorManager.unregisterListener(this);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            Log.d("GLEngine", "onSurfaceDestroyed");
            super.onSurfaceDestroyed(holder);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            Log.d("GLEngine", "Creating...");
            glSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);

            final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
            final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            int glVersion = configurationInfo.reqGlEsVersion;

            setEGLContextClientVersion(glVersion >= 0x30000 ? 3 : (glVersion >= 0x20000 ? 2 : 1));
            // On Honeycomb+ devices, this improves the performance when
            // leaving and resuming the live wallpaper.
            setPreserveEGLContextOnPause(true); // when not set, causes values/animation to reset when switching apps and locking screen
            // Set the renderer to our user-defined renderer.
            setRenderer(new GLRenderer(context, glVersion));

            registerSensors();
        }
        @Override
        public void onDestroy() {
            super.onDestroy();
            Log.d("GLEngine", "Destroying...");
            glSurfaceView.onDestroy();
            unregisterSensors();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            //Log.d("GLEngine", "Is visible " + visible + " renderer is set " + rendererHasBeenSet);

            if (rendererHasBeenSet) {
                if (visible) {
                    glSurfaceView.onResume();
                } else {
                    glSurfaceView.onPause();
                }
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (rendererHasBeenSet) {
                if (renderer instanceof GLRenderer) {
                    ((GLRenderer)renderer).onTouchEvent(event);
                }
            }
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            //TODO: figure out gyro/accelerometer stuff
            //Log.d("GLEngine", event.toString());
            //if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            //    return;

//            float mSensorX = 0;
//            float mSensorY = 0;
//            float mSensorZ;
//            switch (mDisplay.getRotation()) {
//                case Surface.ROTATION_0:
//                    mSensorX = event.values[0];
//                    mSensorY = event.values[1];
//                    break;
//                case Surface.ROTATION_90:
//                    mSensorX = -event.values[1];
//                    mSensorY = event.values[0];
//                    break;
//                case Surface.ROTATION_180:
//                    mSensorX = -event.values[0];
//                    mSensorY = -event.values[1];
//                    break;
//                case Surface.ROTATION_270:
//                    mSensorX = event.values[1];
//                    mSensorY = -event.values[0];
//                    break;
//            }
//            mSensorZ = event.values[2];
            float mSensorX = event.values[0];
            float mSensorY = event.values[1];
            float mSensorZ = event.values[2];
            //float mSensorW = event.values[3];
            //This is your Accelerometer X,Y,Z values
            //Log.d("GLEngine", event.sensor.getType() + ", X: " + mSensorX + ", Y: " + mSensorY + ", Z: " + mSensorZ);// + ", W: " + mSensorW);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Log.d("GLEngine", accuracy + ", " + sensor.toString());
        }

        protected void setRenderer(GLSurfaceView.Renderer renderer) {
            this.renderer = renderer;
            glSurfaceView.setRenderer(renderer);
            rendererHasBeenSet = true;
        }
        protected void setEGLContextClientVersion(int version) {
            glSurfaceView.setEGLContextClientVersion(version);
        }
        protected void setPreserveEGLContextOnPause(boolean preserve) {
            glSurfaceView.setPreserveEGLContextOnPause(preserve);
        }

        public class WallpaperGLSurfaceView extends GLSurfaceView {
            WallpaperGLSurfaceView(Context context) {
                super(context);
            }
            @Override
            public SurfaceHolder getHolder() {
                return getSurfaceHolder();
            }
            public void onDestroy() {
                Log.d("WallpaperGLSurfaceView", "onDestroy");
                super.onDetachedFromWindow();
            }
        }
    }
}
