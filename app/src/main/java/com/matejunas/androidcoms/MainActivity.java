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
import android.widget.TextView;
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

    private static UsbManager mUsbManager;
    private static UsbDevice device;
    private static UsbSerialDriver mDriver;

    private static final String ACTION_USB_PERMISSION =
            "com.matejunas.androidcoms.USB_PERMISSION";

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        startActivityForResult(
                                Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE), 0);
                    } else {
                        Log.d("MainActivity", "permission denied for device " + device);
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_load_adf:
                startLoadADFActivity();
                break;
            case R.id.button_left:
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
                break;
        }
    }

    private void startLoadADFActivity() {
        Intent startADFIntent = new Intent(this, AreaLearningActivity.class);
        IntelliBrainCommunicator.mDriver = mDriver;
        IntelliBrainCommunicator.mUsbManager = mUsbManager;
        startActivity(startADFIntent);
    }
}
