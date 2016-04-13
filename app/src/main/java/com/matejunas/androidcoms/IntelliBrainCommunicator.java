package com.matejunas.androidcoms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;

/**
 * Created by alexm on 03/21/16.
 */
public class IntelliBrainCommunicator {
    public static Context mContext;
//
//    private static UsbSerialDriver mDriver;
//    private static UsbDeviceConnection mConnection;
//    private static UsbSerialPort mPort;
//
//    private static UsbDevice device;
//    private static UsbManager mUsbManager;
//
//    private static final String ACTION_USB_PERMISSION =
//            "com.matejunas.androidcoms.USB_PERMISSION";
//
//    private static final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (ACTION_USB_PERMISSION.equals(action)) {
//                synchronized (this) {
//
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        if (device != null) {
//                            Log.d("MainActivity", "Opening connection");
//                            mConnection = mUsbManager.openDevice(device);
//                            if (mConnection == null) {
//                                Log.e("IBC", "Connection is null, aborting");
//                                return;
//                            }
//
//                            mPort = mDriver.getPorts().get(0);
//                        }
//                    } else {
//                        Log.d("MainActivity", "permission denied for device " + device);
//                    }
//                }
//            }
//        }
//    };
//
//    public static void openConnection() {
//        // Find all available drivers from attached devices
//        if (mContext == null) {
//            Log.e("IBC", "Context is null, aborting");
//            return;
//        }
//        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
//        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
//        if (availableDrivers.isEmpty()) {
//            Log.e("IBC", "No drivers found, aborting");
//            return;
//        }
//
//        // Open a connection to the first available driver
//        mDriver = availableDrivers.get(0);
//        device = mDriver.getDevice();
//        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
//        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//        mContext.registerReceiver(IntelliBrainCommunicator.mUsbReceiver, filter);
//        mUsbManager.requestPermission(device, mPermissionIntent);
//
//    }

    public static void sendData(byte[] toSend, UsbDeviceConnection mConnection, UsbSerialPort mPort) throws IOException {
        if (mPort == null) {
            Toast.makeText(mContext, "Port is null", Toast.LENGTH_LONG).show();
        }
            try {
                mPort.write(toSend, 1000);
                Toast.makeText(mContext, "Send " + (char)toSend[0], Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(mContext, "Failed to write", Toast.LENGTH_LONG).show();
            }
    }
}
