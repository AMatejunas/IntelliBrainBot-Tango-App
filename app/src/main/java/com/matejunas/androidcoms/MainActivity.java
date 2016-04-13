package com.matejunas.androidcoms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static TangoConfig mConfig;
    public static Tango mTango;
    public static boolean adfLoaded = false;

    private static boolean isOpen = false;
    private static double[] mTarget = {3.0, 0.0, 3.0};
    private static UsbManager mUsbManager;
    private static UsbDevice device;
    private static UsbSerialDriver mDriver;
    private static UsbDeviceConnection mConnection;
    private static UsbSerialPort mPort;

    private static final String ACTION_USB_PERMISSION =
            "com.matejunas.androidcoms.USB_PERMISSION";

    private static final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.d("MainActivity", "Opening connection");
                            //Toast.makeText(context, "Opening connection", Toast.LENGTH_LONG).show();
                            if (mUsbManager == null) {
                                //Toast.makeText(context, "UsbManager is Null", Toast.LENGTH_LONG).show();
                            }
                            mConnection = mUsbManager.openDevice(device);
                            if (mConnection == null) {
                                Log.e("IBC", "Connection is null, aborting");
                                //Toast.makeText(context, "Connection is null, aborting", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if (mDriver == null) {
                                //Toast.makeText(context, "Driver is null", Toast.LENGTH_LONG).show();
                            }
                            mPort = mDriver.getPorts().get(0);
                            try {
                                mPort.open(mConnection);
                                try {
                                    mPort.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                                } catch (IOException e) {
                                    //Toast.makeText(context, "Something went wrong setting parameters", Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                //Toast.makeText(context, "Failed to open port", Toast.LENGTH_LONG).show();
                                return;
                            }
                            //Toast.makeText(context, "Got port", Toast.LENGTH_LONG).show();
                            isOpen = true;
                        }
                    } else {
                        Log.d("MainActivity", "permission denied for device " + device);
                    }
                }
            }
        }
    };
    
    private static Tango.OnTangoUpdateListener mUpdateListener = new Tango.OnTangoUpdateListener() {
        @Override
        public void onPoseAvailable(TangoPoseData pose) {
            if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
                    && pose.targetFrame == TangoPoseData.COORDINATE_FRAME_DEVICE) {
                // Process new ADF to device pose data
                // goToTarget(pose.translation, pose.rotation);

            } else if (pose.baseFrame == TangoPoseData.COORDINATE_FRAME_AREA_DESCRIPTION
                    && pose.targetFrame == TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE) {
                // Process new localization
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
        //Intent intent = getIntent();

        mTango = new Tango(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adfLoaded) {
            openConnection();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mPort.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_load_adf:
                startLoadADFActivity();
                break;
//            case R.id.button_forward:
//                try {
//                    if (!isOpen) {
//                        openConnection();
//                    }
//                    IntelliBrainCommunicator.sendData(new byte[]{'f'}, mConnection, mPort);
//                } catch (IOException e) {
//                    Log.e("MainActivity", "Failed to send command");
//                }
//                break;
//            case R.id.button_backward:
//                try {
//                    if (!isOpen) {
//                        openConnection();
//                    }
//                    IntelliBrainCommunicator.sendData(new byte[]{'b'}, mConnection, mPort);
//                } catch (IOException e) {
//
//                }
//                break;
//            case R.id.button_left:
//                try {
//                    if (!isOpen) {
//                        openConnection();
//                    }
//                    IntelliBrainCommunicator.sendData(new byte[]{'l'}, mConnection, mPort);
//                } catch (IOException e) {
//
//                }
//                break;
//            case R.id.button_right:
//                try {
//                    if (!isOpen) {
//                        openConnection();
//                    }
//                    IntelliBrainCommunicator.sendData(new byte[]{'r'}, mConnection, mPort);
//                } catch (IOException e) {
//
//                }
//                break;
//            case R.id.button_stop:
//                try {
//                    if (!isOpen) {
//                        openConnection();
//                    }
//                    IntelliBrainCommunicator.sendData(new byte[]{'s'}, mConnection, mPort);
//                } catch (IOException e) {
//
//                }
//                break;
        }
    }

    private void startLoadADFActivity() {
        Intent startADFIntent = new Intent(this, LoadADFActivity.class);
        startActivity(startADFIntent);
    }

    // Opens a connection between the tango and the IntelliBrainBot
    private void openConnection() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        if (availableDrivers.isEmpty()) {
            Log.e("IBC", "No drivers found, aborting");
            Toast.makeText(this, "No drivers found, aborting", Toast.LENGTH_LONG).show();
            return;
        }

        // Open a connection to the first available driver
        mDriver = availableDrivers.get(0);
        device = mDriver.getDevice();
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        mUsbManager.requestPermission(device, mPermissionIntent);

    }

    // Sends the byte array to the IntelliBrainBot through the open connection
    private void sendData(byte[] toSend) throws IOException {
        if (mPort == null) {
            Toast.makeText(this, "Port is null", Toast.LENGTH_LONG).show();
        }
        try {
            mPort.write(toSend, 1000);
            Toast.makeText(this, "Send " + (char)toSend[0], Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to write", Toast.LENGTH_LONG).show();
        }
    }

    private static void goToTarget(double[] location, double[] forward) {
        // Get distance to target
        double[] toTarget = new double[location.length];
        for (int i = 0; i < location.length; i++) {
            toTarget[i] = location[i] - mTarget[i];
        }

        // Get x-z angle to target
        double angleToTarget = Math.acos(toTarget[2]/toTarget[0]);
        if (toTarget[2] < 0) {
            angleToTarget *= -1.0;
        }

        // Get angle off of target
        forward[0] = 0;
        forward[2] = 0;
        double mag = Math.sqrt(forward[3] * forward[3] + forward[1] * forward[1]);
        forward[3] /= mag;
        forward[1] /= mag;

        double ang = 2*Math.acos(forward[3]);
        // convert angle to -pi to pi

        ang -= angleToTarget;

    }
}
