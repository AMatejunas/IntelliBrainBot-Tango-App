package com.matejunas.androidcoms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import java.util.ArrayList;

public class AreaLearningActivity extends AppCompatActivity {
    private static final String TAG = AreaLearningActivity.class.getSimpleName();
    private static final String uuid = "13bbad56-df34-46c8-81db-b8ae8539e5d1";
    private Tango.OnTangoUpdateListener mUpdateListener = new Tango.OnTangoUpdateListener() {
        @Override
        public void onPoseAvailable(TangoPoseData pose) {
            if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
                    && pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
                // Process new ADF to device pose data
                // goToTarget(pose.translation, pose.rotation);
                //Log.i(TAG, "x: " + pose.translation[0] + "\tz: " + pose.translation[2]);
                final TangoPoseData poseData = pose;
                runOnUiThread( new Runnable(){
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.pose_text)).setText("x: " + (((int) (poseData.translation[0] * 10)) / 10.0) +
                                "\tz: " + (((int) (poseData.translation[2] * 10)) / 10.0));
                    }
                });
            } else if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
                    && pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE) {
                // Process new localization
                Log.i(TAG, "ADF Localized");
            }
        }

        @Override
        public void onXyzIjAvailable(TangoXyzIjData tangoXyzIjData) {

        }

        @Override
        public void onFrameAvailable(int i) {

        }

        @Override
        public void onTangoEvent(TangoEvent tangoEvent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "Starting ADF Load", Toast.LENGTH_SHORT).show();
        loadAdf();
        Toast.makeText(this, "ADF Loaded", Toast.LENGTH_SHORT).show();
    }

    private void loadAdf() {
        Tango mTango = new Tango(this);
        TangoConfig mConfig = mTango.getConfig(TangoConfig.CONFIG_TYPE_AREA_DESCRIPTION);
        try {
            mConfig.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, uuid);
        } catch (TangoErrorException e) {
            e.printStackTrace();
            Toast.makeText(this, "Couldn't put uuid", Toast.LENGTH_LONG).show();
        }
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                TangoPoseData.COORDINATE_FRAME_DEVICE));
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION,
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE));
        mTango.connectListener(framePairs, mUpdateListener);
        try {
            mTango.connect(mConfig);
        } catch (TangoOutOfDateException e) {
            e.printStackTrace();
        } catch (TangoErrorException e) {
            e.printStackTrace();
        } catch (TangoInvalidException e) {
            e.printStackTrace();
        }
        MainActivity.mTango = mTango;
        MainActivity.adfLoaded = true;
    }

    }
}
