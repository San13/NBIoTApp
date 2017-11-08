package com.example.san.nbiotapp;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.san.nbiotapp.util.BluetoothConstants;
import com.example.san.nbiotapp.util.UartService;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Home extends AppCompatActivity {
    @BindView(R.id.txtMsg)
    TextView txtMsg;
    @BindView(R.id.txtResult) TextView txtResult;
    SingleSelectToggleGroup single;
    private static final String LOG_TAG = Home.class.getSimpleName();
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private String actiom="";

    int[] nfcOffSet = {0,16,24,28,32,48};
    int [] nfcSize  = {16,7,4,4,16,16};


    int[] mduOffSet = {0,1,2,4,5,6,7,8,12,16,17};
    int[] mduSize = {1,1,1,1,1,1,1,4,4,1,4};

    int[] table = {
            0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
            0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
            0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
            0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
            0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
            0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
            0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
            0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
            0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
            0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
            0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
            0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
            0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
            0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
            0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
            0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
            0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
            0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
            0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
            0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
            0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
            0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
            0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
            0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
            0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
            0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
            0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
            0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
            0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
            0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
            0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
            0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
    };



    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(LOG_TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(LOG_TAG, "Unable to initialize Bluetooth");
                finish();

            }
            // Automatically connects to the device upon successful start-up initialization.
            connect();

        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };


    public final BroadcastReceiver scanReceiverListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothConstants.ACTION_GATT_CONNECTED.equals(action)) {
                setMessage("Connected!");
            }
            if (BluetoothConstants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mService.enableTXNotification();
                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                txtResult.setText(Arrays.toString(txValue));
                // Log.i(LOG_TAG,txValue..toString());

            }
            if (BluetoothConstants.ACTION_GATT_DISCONNECTED.equals(action)) {
                setMessage("Disconnected!");
            }
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                mService.disconnect();
                Log.i(LOG_TAG,"Not supported");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        service_init();
        init();
        Intent i = getIntent();
        String macAddress = i.getStringExtra("macAddress");
        mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress);
        //mService.connect(mDevice.getAddress());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(scanReceiverListener, makeGattUpdateIntentFilter());

    }

//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if (mService != null) {
//            mService.disconnect();
//        }
//        mService = null;
//        this.unbindService(mServiceConnection);
//                  /* Write into diagnostic_log file*/
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(scanReceiverListener);
//    }

    private void init() {
        final SingleSelectToggleGroup single = (SingleSelectToggleGroup) findViewById(R.id.group_choices);
        single.setOnCheckedChangeListener(new SingleSelectToggleGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
                Log.v(LOG_TAG, "onCheckedChanged(): checkedId = " + checkedId);
                switch (checkedId) {
                    case R.id.tgnfc:
                        //   byte[] data = {0,27,0,4,1,0,0,16,0,48};
                        //    byte[] checkSum =  intToByteArray(calculateCheckSum(data));
                        //  byte[] result = combineByteArrays(data,checkSum);
                        // Log.i(LOG_TAG,result.toString());
                        // mService.writeRXCharacteristic(data);
                        Log.i(LOG_TAG,"NFC");
                        //Toast.makeText(getApplicationContext(), "NFC", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tgModule:
                        //byte[] data = {27,0,4,0,1,28,0,4,-63,-35}; // 28 offset
                        //byte[] data = {27,0,4,0,2,0,0,1,-127,-99}; // pg 2 offset 0
                        byte[] data = {1,28,0,4};
                        calculateCheckSum(data);


                        mService.writeRXCharacteristic(data);
                        //Toast.makeText(getApplicationContext(), "Module", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tgCtr:

                        Log.i(LOG_TAG,"NFC");
                        //Toast.makeText(getApplicationContext(), "Ctr", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.tgProcess:
                        Log.i(LOG_TAG,"NFC");
                        //Toast.makeText(getApplicationContext(), "Process", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.tgEnv:

                        Log.i(LOG_TAG,"NFC");
                        //Toast.makeText(getApplicationContext(), "Env", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

     /*   MultiSelectToggleGroup multi = (MultiSelectToggleGroup) findViewById(R.id.group_weekdays);
        multi.setOnCheckedChangeListener(new MultiSelectToggleGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedStateChanged(MultiSelectToggleGroup group, int checkedId, boolean isChecked) {
                Log.v(LOG_TAG, "onCheckedStateChanged(): group.getCheckedIds() = " + group.getCheckedIds());
            }
        });*/
    }

    public void writeData(int Page,int offSet, int Length, int Size )
    {
        for (int i = 0 ; i <Page; i++) {
            byte[] data = {27, 0, 0,0, 1, 2, 17, 4};
            byte[] checkSum = intToByteArray(calculateCheckSum(data));
            byte[] result = combineByteArrays(data, checkSum);
            mService.writeRXCharacteristic(result);
        }
    }

    public static byte[] intToByteArray(int a){
        byte[] ret = new byte[2];
        ret[0] = (byte) (a & 0xFF);
        ret[1] = (byte) ((a >> 8) & 0xFF);
        return ret;
    }

    byte[] combineByteArrays(byte[] a, byte[] b)
    {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a,0,result,0,a.length);
        System.arraycopy(b,0,result,a.length,b.length);
        return  result;
    }

    public void connect() {
        mService.connect(mDevice.getAddress());
    }




    // Set message
    private void setMessage (String msg)
    {
        txtMsg.setText(msg);
    }


    public int calculateCheckSum(byte[] bytes){
        int crc = 0xffff;
        for (byte b : bytes) {
            crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff];
        }
        //Log.i(LOG_TAG+"CRC16 = ", Arrays.toString(crc));

        //Log.i(LOG_TAG+"CRC16 = ", Integer.toHexString(crc));
        // StdOut.println("CRC16 = " + Integer.toHexString(crc));



        /*int polynomial = 0x8005;
        int CRC = 0xFFFF;

        for (byte b : arr)
        {
            CRC ^= b;
            for (int i = 8; i != 0; i--)
            {
                if ((CRC & 0x0001) != 0)
                {
                    CRC = (CRC >> 1) ^ polynomial;
                }
                else
                {
                    CRC >>= 1;
                }
            }
        }

        Log.i(LOG_TAG, Arrays.toString(intToByteArray(CRC)));*/
        return crc;
    }


    //Broadcasting Function
    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }



}