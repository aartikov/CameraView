package com.otaliastudios.cameraview;

import android.content.Context;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

class OrientationHelper {

    final OrientationEventListener mListener;

    private final Callback mCallback;
    private int mRawOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    private int mDeviceOrientation = -1;
    private int mDisplayOffset = -1;
    private CameraOrientation mAllowedOrientation = CameraOrientation.ANY;

    interface Callback {
        void onDeviceOrientationChanged(int deviceOrientation);
    }

    OrientationHelper(Context context, @NonNull Callback callback) {
        mCallback = callback;
        mListener = new OrientationEventListener(context.getApplicationContext(), SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation != ORIENTATION_UNKNOWN) {
                    mRawOrientation = orientation;
                }
                handleOrientationChange();
            }
        };
    }

    private void handleOrientationChange() {
        int newDeviceOrientation = calculateDeviceOrientation(mAllowedOrientation, mRawOrientation, mDeviceOrientation);
        if (newDeviceOrientation != mDeviceOrientation) {
            mDeviceOrientation = newDeviceOrientation;
            mCallback.onDeviceOrientationChanged(mDeviceOrientation);
        }
    }

    private static int calculateDeviceOrientation(CameraOrientation allowedOrientation, int rawOrientation, int previousOrientation) {
        if (allowedOrientation == CameraOrientation.ANY) {
            if (rawOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return 0;
            } else if (rawOrientation >= 315 || rawOrientation < 45) {
                return 0;
            } else if (rawOrientation >= 45 && rawOrientation < 135) {
                return 90;
            } else if (rawOrientation >= 135 && rawOrientation < 225) {
                return 180;
            } else {
                return 270;
            }
        } else if (allowedOrientation == CameraOrientation.LANDSCAPE) {
            if (rawOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return 90;
            } else if (rawOrientation >= 45 && rawOrientation < 135) {
                return 90;
            } else if (rawOrientation > 255 && rawOrientation < 315) {
                return 270;
            } else {
                return previousOrientation == 90 || previousOrientation == 270 ? previousOrientation : 90;
            }
        } else if (allowedOrientation == CameraOrientation.PORTRAIT) {
            if (rawOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return 0;
            } else if (rawOrientation >= 315 || rawOrientation < 45) {
                return 0;
            } else if (rawOrientation > 135 && rawOrientation < 255) {
                return 180;
            } else {
                return previousOrientation == 0 || previousOrientation == 180 ? previousOrientation : 0;
            }
        } else {
            return 0;   // unreachable
        }
    }

    void enable(Context context) {
        Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                mDisplayOffset = 0;
                break;
            case Surface.ROTATION_90:
                mDisplayOffset = 90;
                break;
            case Surface.ROTATION_180:
                mDisplayOffset = 180;
                break;
            case Surface.ROTATION_270:
                mDisplayOffset = 270;
                break;
            default:
                mDisplayOffset = 0;
                break;
        }
        mListener.enable();
    }

    void disable() {
        mListener.disable();
        mDisplayOffset = -1;
        mDeviceOrientation = -1;
    }

    int getDeviceOrientation() {
        return mDeviceOrientation == -1 ? 0 : mDeviceOrientation;
    }

    int getDisplayOffset() {
        return mDisplayOffset == -1 ? 0 : mDisplayOffset;
    }

    public CameraOrientation getAllowedOrientation() {
        return mAllowedOrientation;
    }

    public void setAllowedOrientation(CameraOrientation allowedOrientation) {
        if (mAllowedOrientation != allowedOrientation) {
            mAllowedOrientation = allowedOrientation;
            handleOrientationChange();
        }
    }
}
