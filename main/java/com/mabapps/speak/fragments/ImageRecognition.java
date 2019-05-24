package com.mabapps.speak.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.mabapps.speak.R;
import com.mabapps.speak.helper.classes.CameraOverlay;
import com.mabapps.speak.helper.classes.CameraSurfacePreview;
import com.mabapps.speak.helper.classes.FaceOverlayGraphics;

import java.io.IOException;


/**
 * Created by Ariful Islam on 28-Nov-17.
 */

public class ImageRecognition extends Fragment {
    private static final String TAG = "FaceTrackerDemo";
    private CameraSource mCameraSource = null;
    private CameraSurfacePreview mPreview;
    private CameraOverlay cameraOverlay;
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_image_recognition, container, false);
        mPreview = (CameraSurfacePreview) rootView.findViewById(R.id.preview);
        cameraOverlay = (CameraOverlay) rootView.findViewById(R.id.faceOverlay);
        int rc = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
//            requestCameraPermission();
        }
        return rootView;
    }

    private void requestCameraPermission() {

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = getActivity();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(cameraOverlay, "Camera permission is required",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", listener)
                .show();
    }


    private void createCameraSource() {

        Context context = getContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new ImageRecognition.GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {

            Log.e(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(getContext(), detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();

        startCameraSource();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("FaceTrackerDemo")
                .setMessage("Need Camera access permission!")
                .setPositiveButton("OK", listener)
                .show();
    }

    private void finish() {
        finish();
    }


    private void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, cameraOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    public class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new ImageRecognition.GraphicFaceTracker(cameraOverlay);
        }
    }

    public class GraphicFaceTracker extends Tracker<Face> {
        public CameraOverlay mOverlay;
        public FaceOverlayGraphics faceOverlayGraphics;

        GraphicFaceTracker(CameraOverlay overlay) {
            mOverlay = overlay;
            faceOverlayGraphics = new FaceOverlayGraphics(overlay);
        }

        @Override
        public void onNewItem(int faceId, Face item) {
            faceOverlayGraphics.setId(faceId);
        }

        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(faceOverlayGraphics);
            faceOverlayGraphics.updateFace(face);
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(faceOverlayGraphics);
        }

        @Override
        public void onDone() {
            mOverlay.remove(faceOverlayGraphics);
        }
    }


}
