package com.matejunas.androidcoms;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.io.IOException;

/**
 * Created by alexm on 04/20/16.
 */
public class IntelliBrainCommunicator {
    private static final String TAG = IntelliBrainCommunicator.class.getSimpleName();

    private Context mContext;

    public static UsbSerialDriver mDriver;
    public static UsbManager mUsbManager;

    private static UsbDevice mDevice;
    private static UsbDeviceConnection mConnection;
    private static UsbSerialPort mPort;

    public IntelliBrainCommunicator(Context context) {
        mContext = context;
        openConnection();
    }

    // Opens a connection between the tango and the IntelliBrainBot
    private void openConnection() {
        mDevice = mDriver.getDevice();
        if (mDevice != null) {
            Log.d(TAG, "Opening connection");
            Toast.makeText(mContext, "Opening connection", Toast.LENGTH_LONG).show();
            if (mUsbManager == null) {
                Toast.makeText(mContext, "UsbManager is Null", Toast.LENGTH_LONG).show();
            }
            mConnection = mUsbManager.openDevice(mDevice);
            if (mConnection == null) {
                Log.e("IBC", "Connection is null, aborting");
                Toast.makeText(mContext, "Connection is null, aborting", Toast.LENGTH_LONG).show();
                return;
            }
            if (mDriver == null) {
                Toast.makeText(mContext, "Driver is null", Toast.LENGTH_LONG).show();
            }
            mPort = mDriver.getPorts().get(0);
            try {
                mPort.open(mConnection);
                try {
                    mPort.setParameters(115200, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                } catch (IOException e) {
                    Toast.makeText(mContext, "Something went wrong setting parameters", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Toast.makeText(mContext, "Failed to open port", Toast.LENGTH_LONG).show();
                return;
            }
            Toast.makeText(mContext, "Got port", Toast.LENGTH_LONG).show();
        }
    }

    // Sends the byte array to the IntelliBrainBot through the open connection
    public void sendData(byte[] toSend) {
        if (mPort == null) {
            //Toast.makeText(mContext, "Port is null", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            mPort.write(toSend, 1000);
            //Toast.makeText(mContext, "Send " + (char) toSend[0], Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            //Toast.makeText(mContext, "Failed to write", Toast.LENGTH_LONG).show();
        }
    }

    public void close() {
        try {
            mPort.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
