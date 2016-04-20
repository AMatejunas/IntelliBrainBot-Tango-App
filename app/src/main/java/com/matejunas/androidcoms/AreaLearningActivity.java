package com.matejunas.androidcoms;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import java.util.Arrays;

public class AreaLearningActivity extends AppCompatActivity {

    private static final String TAG = AreaLearningActivity.class.getSimpleName();
    private static final String uuid = "13bbad56-df34-46c8-81db-b8ae8539e5d1";
    private static double[] mTarget;
    private static double[] mPosition;
    private static double[] mRotation;
    private static char currentCommand;
    private static IntelliBrainCommunicator robot;
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
                                "\tz: " + (((int) (poseData.translation[1] * 10)) / 10.0));
                    }
                });
                mPosition = Arrays.copyOf(poseData.translation, 3);
                mRotation = Arrays.copyOf(poseData.rotation, 4);
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
        setContentView(R.layout.activity_area_learning);
        Toast.makeText(this, "Starting ADF Load", Toast.LENGTH_SHORT).show();
        loadAdf();
        Toast.makeText(this, "ADF Loaded", Toast.LENGTH_SHORT).show();
        robot = new IntelliBrainCommunicator(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        robot.close();
    }

    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_record_target:
                mTarget = Arrays.copyOf(mPosition, 3);
                Toast.makeText(this, "Target set to x: " + mTarget[0] + "\tz: " + mTarget[1], Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_go_to_target:
                Toast.makeText(this, R.string.button_go_to_target + " pressed", Toast.LENGTH_SHORT).show();
                rotateToTarget();
                break;
            case R.id.button_forward:
                robot.sendData(new byte[] {'f'});
                break;
        }
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
    }

    private void rotateToTarget() {
        // Record current position and translation so nothing is overwritten
        double[] location = Arrays.copyOf(mPosition, 3);
        double[] rotation = Arrays.copyOf(mRotation, 4);

        // Get distance to target
        double[] toTarget = new double[location.length];
        for (int i = 0; i < location.length; i++) {
            toTarget[i] = location[i] - mTarget[i];
        }

        // Get x-y angle to target
        double angleToTarget = Math.acos(toTarget[1]/toTarget[0]);
        if (toTarget[1] < 0) {
            angleToTarget *= -1.0;
        }

        final double printAng = angleToTarget;

        // Get angle off of target
        final double ang = Math.atan2(2*(rotation[3] * rotation[2] + rotation[1] * rotation[0]), 2*(rotation[1] * rotation[1] + rotation[0] * rotation[0]));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.angle_to_target_text)).setText("To Target: " + printAng);
                ((TextView) findViewById(R.id.angle_tango)).setText("Tango rotation: " + ang);
            }
        });

        double trueAngle = angleToTarget - ang;
        if (Math.abs(trueAngle) > 0.1) {
            if (trueAngle < 0.0 && currentCommand != 'l') {
                robot.sendData(new byte[] {'l'});
                currentCommand = 'l';
            } else if (trueAngle > 0.0 && currentCommand != 'r'){
                robot.sendData(new byte[] {'r'});
                currentCommand = 'r';
            }
        } else {
            robot.sendData(new byte[] {'s'});
            currentCommand = 's';
        }
    }
}
