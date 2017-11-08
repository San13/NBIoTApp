package com.example.san.nbiotapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.san.nbiotapp.util.BluetoothConstants;
import com.example.san.nbiotapp.util.UartService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private UartService mService = null;
    Toolbar toolbar;
    public static final String TAG = "UartScan";
    private BluetoothAdapter mBtAdapter = null;
    List<BluetoothDevice> deviceList;
    private BluetoothLeScanner mLeScanner;
    private ServiceConnection onService = null;
    Map<String, Integer> devRssiValues;
    private static final long SCAN_PERIOD = 5000; //10 seconds
    private boolean mScanning;
    private DeviceAdapter mLeDeviceListAdapter;
    String selectedBLE;

    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private Handler mHandler = new Handler() {
        @Override

        //Handler events that received from UART service
        public void handleMessage(Message msg) {

        }
    };

    public final BroadcastReceiver scanReceiverListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothConstants.ACTION_GATT_CONNECTED.equals(action)) {
            }
            if (BluetoothConstants.ACTION_GATT_DISCONNECTED.equals(action)) {
            }
            if (BluetoothConstants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            }
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
            }

            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                mService.disconnect();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        blePermission();       // Bluetooth permission
        populateList();    // Populate the Available device list
        // service_init();     // Initiate Uart Service
    }



    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            mLeScanner.stopScan(mScanCallback);
            Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());
            view.setSelected(true);
            selectedBLE = mLeDeviceListAdapter.devices.get(position).getAddress();
            view.setBackgroundColor(Color.BLUE);


            Intent intent = new Intent(getApplicationContext(), Home.class);
            intent.putExtra("macAddress", selectedBLE);
            startActivity(intent);
        }
    };




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress);
        }
        return true;
    }


    //region Core Function BlePermisson,populateList()
    private void blePermission() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        // Checks if Bluetooth is supported on the device.
        if (mBtAdapter == null) {
            Toast.makeText(this,R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void populateList() {
        /* Initialize device list container */

        Log.d(TAG, "populateList");
        deviceList = new ArrayList<BluetoothDevice>();
        mLeDeviceListAdapter = new DeviceAdapter(this, deviceList);
        devRssiValues = new HashMap<String, Integer>();

        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mLeDeviceListAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        //newDevicesListView.setOnItemLongClickListener(this);


        //scanLeDevice(true);

    }


    private void scanLeDevice(final boolean enable) {
        // final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mLeScanner.stopScan(mScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mLeScanner.startScan(mScanCallback);
            //cancelButton.setText(R.string.cancel);
        } else {
            mScanning = false;
            mLeScanner.stopScan(mScanCallback);
            // cancelButton.setText(R.string.scan);
        }

        invalidateOptionsMenu();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_scan:
                scanLeDevice(true);
                mLeDeviceListAdapter.devices.clear();
                mLeDeviceListAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
            default:
                onBackPressed();
        }
        return true;
    }


    public ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null) {
                addDevice(device, result.getRssi());
            }
        }
    };

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : deviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }
        devRssiValues.put(device.getAddress(), rssi);
        if (!deviceFound) {
            deviceList.add(device);
            mLeDeviceListAdapter.notifyDataSetChanged();
        }
    }


    protected void onResume() {
        super.onResume();
        if (!mBtAdapter.isEnabled()) {
            if (!mBtAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        if (deviceList == null) {
            deviceList = new ArrayList<BluetoothDevice>();
        }

        // LocalBroadcastManager.getInstance(this).registerReceiver(myUpdateReceiver, makeGattUpdateIntentFilter());
        mLeDeviceListAdapter = new DeviceAdapter(this, deviceList);
        mLeScanner = mBtAdapter.getBluetoothLeScanner();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
        LocalBroadcastManager.getInstance(this).unregisterReceiver((scanReceiverListener));
    }


    class DeviceAdapter extends BaseAdapter {
        Context context;
        List<BluetoothDevice> devices;
        LayoutInflater inflater;


        public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.devices = devices;
        }

        @Override
        public int getCount() {
            return devices.size();
        }

        @Override
        public Object getItem(int position) {
            return devices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.devices, null);
            }

            BluetoothDevice device = devices.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
            final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

            tvrssi.setVisibility(View.VISIBLE);
            byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
            if (rssival != 0) {
                tvrssi.setText("Rssi = " + String.valueOf(rssival));
            }

            tvname.setText(device.getName());
            Log.v("Device Name", tvname.getText().toString());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::" + device.getName());
                tvpaired.setTextColor(Color.BLACK);
                tvpaired.setVisibility(View.VISIBLE);
                tvrssi.setVisibility(View.VISIBLE);
            } else {
                tvname.setTextColor(Color.BLACK);
                tvpaired.setVisibility(View.GONE);
                tvrssi.setVisibility(View.VISIBLE);
            }
            return vg;
        }

    }



    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    //Broadcasting Function
    private void service_init() {
        Intent bindIntent = new Intent(getApplicationContext(), UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(scanReceiverListener, makeGattUpdateIntentFilter());
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
