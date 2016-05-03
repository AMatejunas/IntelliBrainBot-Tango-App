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

    private static final int MODE_WAIT_FOR_START = 0;
    private static final int MODE_INITIAL_APPROACH = 1;
    private static final int MODE_ENGAGE = 2;
    private static final int MODE_FINAL_APPROACH = 3;
    private static final int MODE_DONE = 4;


    private static final double TOLERANCE_ANGLE = 0.10;
    private static final double TOLERANCE_INITIAL_POSITION = 0.1;
    private static final double TOLERANCE_FINAL_POSITION = 0.05;
    private static final String TAG = AreaLearningActivity.class.getSimpleName();
    private static final String uuid = "53d08217-e720-44d4-94da-3e2c45f1b447";
    //private static final String uuid = "14416671-6e5d-40a6-bf1b-f3c4277f6a8c";
    //private static final String uuid = "13f0226b-5cbb-4693-99ee-619a07959231";
    //private static final String uuid = "455426f3-a468-435d-bc49-1a39f4fa01c9";
    private static double[] mTarget;
    private static double[] mPosition;
    private static char currentCommand;
    //private boolean enableRotate = false;
//    private boolean engagedTarget = false;
    private int mode = MODE_WAIT_FOR_START;
    private static IntelliBrainCommunicator robot;
    //private SerialThread mSerialThread = new SerialThread();
    private Tango.OnTangoUpdateListener mUpdateListener = new Tango.OnTangoUpdateListener() {
        @Override
        public void onPoseAvailable(TangoPoseData pose) {
            if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
                    && pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
                // Process new ADF to device pose data
                // goToTarget(pose.translation, pose.rotation);
                //Log.i(TAG, "x: " + pose.translation[0] + "\tz: " + pose.translation[2]);
                final TangoPoseData poseData = pose;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.pose_text)).setText("x: " + (((int) (poseData.translation[0] * 10)) / 10.0) +
                                "\tz: " + (((int) (poseData.translation[1] * 10)) / 10.0));
                        ((TextView) findViewById(R.id.mode)).setText("Mode: " + mode);
                    }
                });
                mPosition = Arrays.copyOf(pose.translation, 3);
                //if (mode == MODE_INITIAL_ROTATION || mode == MODE_ENGAGE_TARGET) {
                if (mode != MODE_WAIT_FOR_START && mode != MODE_DONE) {
                    goToTarget(pose.translation, pose.rotation);
                }

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
        //mSerialThread.getHandler().setRobot(new IntelliBrainCommunicator(this));
        //mSerialThread.getHandler().mContext = this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        robot.close();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_record_target:
                mTarget = Arrays.copyOf(mPosition, 3);
                Toast.makeText(this, "Target set to x: " + mTarget[0] + "\tz: " + mTarget[1], Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_go_to_target:
                Log.i(TAG, getResources().getText(R.string.button_go_to_target) + " pressed");
                //Toast.makeText(this, getResources().getText(R.string.button_go_to_target) + " pressed", Toast.LENGTH_SHORT).show();
                //enableRotate = true;
                mode = MODE_INITIAL_APPROACH;
                break;
            case R.id.button_abort:
                robot.sendData(new byte[]{'s'});
                mode = MODE_WAIT_FOR_START;
                //enableRotate = false;
                currentCommand = 's';
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

    private void goToTarget(double[] location, double[] rotation) {

        // Get distance to target
        //Log.i(TAG, "Calculating angle to target");
        double[] toTarget = new double[location.length];
        for (int i = 0; i < location.length; i++) {
            toTarget[i] = mTarget[i] - location[i];
        }

        // Get distance to target
        double dist = calcDistance(toTarget);

        double toRotate = calcAngle(rotation, toTarget);


        if (Math.abs(toRotate) < (Math.PI - TOLERANCE_ANGLE) && dist >= TOLERANCE_INITIAL_POSITION) {
            if (toRotate < 0.0 && currentCommand != 'l') {
                //robot.sendData(new byte[] {'l'});
                sendMessage('l');
                currentCommand = 'l';
            } else if (toRotate > 0.0 && currentCommand != 'r') {
                //robot.sendData(new byte[] {'r'});
                sendMessage('r');
                currentCommand = 'r';
            }
        } else if (dist >= TOLERANCE_FINAL_POSITION) {
            sendMessage('f');
            currentCommand = 'f';
        } else {
            //robot.sendData(new byte[]{'s'});
            sendMessage('s');
            currentCommand = 's';
            mode = MODE_DONE;
        }

    }

    private double calcDistance(double[] distVec) {
        return Math.sqrt(distVec[0] * distVec[0] + distVec[1] * distVec[1]);
    }

    private double calcAngle(double[] rotation, double[] toTarget) {
        // Get x-y angle to target
        //Log.i(TAG, "Calculating rotation to engage target");
        double angleToTarget = Math.atan2(toTarget[0], toTarget[1]);
        angleToTarget = angleToTarget < (-1 * Math.PI) ? angleToTarget + (2 * Math.PI) : angleToTarget;
        angleToTarget = angleToTarget > (Math.PI) ? angleToTarget - (2 * Math.PI) : angleToTarget;
        final double toTargetDeg2SigFig = ((int) (angleToTarget * 1800.0 / Math.PI)) / 10.0;
        //Log.i(TAG, "Angle to target is " + toTargetDeg2SigFig);
        // Get angle off of target
        //final double angle = Math.atan2(2 * (rotation[3] * rotation[2] + rotation[1] * rotation[0]), 2 * (rotation[1] * rotation[1] + rotation[0] * rotation[0]));
        double tangoAngle = 2 * Math.atan2(rotation[0], rotation[1]);
        tangoAngle = tangoAngle < (-1 * Math.PI) ? tangoAngle + (2 * Math.PI) : tangoAngle;
        tangoAngle = tangoAngle > (Math.PI) ? tangoAngle - (2 * Math.PI) : tangoAngle;
        double toRotate = tangoAngle - angleToTarget;
        toRotate = toRotate < (-1 * Math.PI) ? toRotate + (2 * Math.PI) : toRotate;
        toRotate = toRotate > (Math.PI) ? toRotate - (2 * Math.PI) : toRotate;
        final double tangoAngleDeg2SigFig = ((int) (tangoAngle * 1800.0 / Math.PI)) / 10.0;
        final double toRotateDeg2SigFig = ((int) (toRotate * 1800.0 / Math.PI)) / 10.0;
        //Log.i(TAG, "Tango Angle is " + tangoAngleDeg2SigFig);
        //Log.i(TAG, "Angle to rotate is " + toRotateDeg2SigFig);
        //Toast.makeText(this, "Calling runOnUIThread", Toast.LENGTH_SHORT).show();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.angle_to_target_text)).setText("To Target: " + toTargetDeg2SigFig);
                ((TextView) findViewById(R.id.angle_tango)).setText("Tango rotation: " + tangoAngleDeg2SigFig);
                ((TextView) findViewById(R.id.angle_to_rotate)).setText("To Rotate: " + toRotateDeg2SigFig);
            }
        });
        return toRotate;
    }
    public void sendMessage(char c) {
        //final byte b = (byte) c;
        //new Thread(new Runnable() {
        //@Override
        //public void run() {
        robot.sendData(new byte[]{(byte) c});
        // }
        //}).start();
//        Message msg = Message.obtain(mSerialThread.getHandler());
//        Bundle b = new Bundle();
//        b.putByteArray("usb", new byte[] {(byte) c});
//        msg.setData(b);
//        getWindow().getDecorView().getHandler().sendMessage(msg);
//        Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
    }
}
