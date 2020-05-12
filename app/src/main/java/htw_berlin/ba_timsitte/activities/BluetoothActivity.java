package htw_berlin.ba_timsitte.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.communication.BluetoothDeviceListAdapter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import htw_berlin.ba_timsitte.communication.BluetoothService;

public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @BindView(R.id.btnOnOff) Button mbtnOnOff;
    @BindView(R.id.btnDiscoverability) Button mbtnDiscoverability;
    @BindView(R.id.btnDiscover) Button mbtnDiscover;
    @BindView(R.id.lvDevices) ListView mlvDevices;
    @BindView(R.id.app_toolbar) Toolbar mToolbar;
    @BindView(R.id.btnStartService) Button mbtnStartService;


    private static final String TAG = "BluetoothActivity";

    BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<>();
    public BluetoothDeviceListAdapter mBluetoothDeviceListAdapter;
    public ArrayList<BluetoothDevice> mPairedBluetoothDevices = new ArrayList<>();


    /**
     * Broadcast receiver for changes made to bluetooth states
     */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals((mBluetoothAdapter.ACTION_STATE_CHANGED))){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast receiver for activating the discoverability
     */
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals((BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability disabled. Able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                        break;
                }
            }
        }
    };

    /**
     * Broadcast receiver for listing devices which are not yet paired
     */
    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mBluetoothDevices.contains(device)){
                    mBluetoothDevices.add(device);
                }
                Log.d(TAG, "onReceive " + device.getName() + ": " + device.getAddress());
                mBluetoothDeviceListAdapter = new BluetoothDeviceListAdapter(context, R.layout.bluetooth_device_adapter_view, mBluetoothDevices);
                mlvDevices.setAdapter(mBluetoothDeviceListAdapter);
            }

        }
    };

    /**
     * Broadcast receiver for pairing devices
     */
    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BroadcastReceiver4: BOND_BONDED");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BroadcastReceiver4: BOND_BONDING");
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BroadcastReceiver4: BOND_NONE");
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        ButterKnife.bind(this);

        initiateSupportActionBar();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mlvDevices.setOnItemClickListener(BluetoothActivity.this);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, intentFilter);

        //set everything up in regards to bluetooth situation
        if (mBluetoothAdapter == null){
            statusDisabled();
        }
        if (mBluetoothAdapter.isEnabled()){
            statusOn();
        }
        if (!mBluetoothAdapter.isEnabled()){
            statusOff();
        }
    }

    public void onDestroy(){
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }

    // ----------------- Toolbar methods -----------------
    public void initiateSupportActionBar(){
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Bluetooth Connection");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_btncommand:
                Intent intent = new Intent(this, CommandActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ----------------- OnClick methods -----------------

    @OnClick(R.id.btnOnOff)
    public void enableDisableBT(){
        Log.d(TAG, "onClick: enabling/disabling bluetooth.");
        if (mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);

            statusOn();
        }
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter((BluetoothAdapter.ACTION_STATE_CHANGED));
            registerReceiver(mBroadcastReceiver1, BTIntent);

            statusOff();
        }
    }

    @OnClick(R.id.btnDiscoverability)
    public void enableDisableDiscoverability(){
        Log.d(TAG, "enableDisableDiscoverability: Making device discoverable.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter((mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED));
        registerReceiver(mBroadcastReceiver2, intentFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnDiscover)
    public void discovery(View view){
        Log.d(TAG, "discovery: Looking for unpaired devices.");
        mBluetoothDevices.clear();
        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "discovery: Danceling discovery.");

            checkBTPermission();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoveryDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoveryDevicesIntent);
        }

        if (!mBluetoothAdapter.isDiscovering()){
            checkBTPermission();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoveryDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoveryDevicesIntent);
        }
    }

    /**
     * this method should work automatically later on (after click)
     */
    @OnClick(R.id.btnStartService)
    public void startingServiceWithBluetoothDevice(){
        // Service start
        if (mBluetoothAdapter != null){
            Intent intent = new Intent(this, BluetoothService.class);
            BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            intent.putExtra("btdevice", mDevice);
            startService(intent);
        } else {
            Toast.makeText(this, "Please bond your bluetooth connection first.", Toast.LENGTH_SHORT);
        }

    }

    // ----------------- additional methods -----------------

    public void statusOn(){
        mbtnOnOff.setBackgroundColor(Color.RED);
        mbtnOnOff.setText("Bluetooth off");
    }

    public void statusOff(){
        mbtnOnOff.setBackgroundColor(Color.GREEN);
        mbtnOnOff.setText("Bluetooth on");
    }

    public void statusConnected(){

    }

    public void statusDisabled(){
        mbtnOnOff.setEnabled(false);
    }


    /**
     * This method is required for all devices running on API23 or higher.
     * Android must check the permissions for bluetooth. Putting the proper permissions only in the
     * manifest isn't enough.
     *
     * NOTE: Executes only if version > LOLLIPOP. Not needed otherwise.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermission(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCES_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCES_COARSE_LOCATION");
            if (permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
            } else {
                Log.d(TAG, "checkBTPermission: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: Clicked on a device");
        String deviceName = mBluetoothDevices.get(i).getName();
        String deviceAddress = mBluetoothDevices.get(i).getAddress();
        Log.d(TAG, "onItemClick: deviceName: " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress: " + deviceAddress);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBluetoothDevices.get(i).createBond();
        }
    }
}
