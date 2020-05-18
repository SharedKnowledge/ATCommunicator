package htw_berlin.ba_timsitte.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.communication.BluetoothDeviceListAdapter;
import htw_berlin.ba_timsitte.communication.BluetoothService;

public class BluetoothFragment extends Fragment implements AdapterView.OnItemClickListener {

    @BindView(R.id.btnDiscover) Button mbtnDiscover;
    @BindView(R.id.lvDevices) ListView mlvDevices;
    @BindView(R.id.btnStartService) Button mbtnStartService;
    @BindView(R.id.discoverState) TextView discoverStatus;

    private static final String TAG = "BluetoothFragment";

    private BluetoothDevice mBluetoothDevice;

    BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<>();
    private BluetoothDeviceListAdapter mBluetoothDeviceListAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        ButterKnife.bind(this, view);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mlvDevices.setOnItemClickListener(BluetoothFragment.this);

        discovery(view);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Register a local broadcast manager to listen
        LocalBroadcastManager locationBroadcastManager = LocalBroadcastManager.getInstance(getContext());

        // mBroadcastReceiver1 for listing devices
        IntentFilter discoveryDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        locationBroadcastManager.registerReceiver(mBroadcastReceiver1, discoveryDevicesIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager locationBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        locationBroadcastManager.unregisterReceiver(mBroadcastReceiver1);
    }

    /**
     * Broadcast receiver for listing devices
     */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mBluetoothDevices.contains(device)){
                    mBluetoothDevices.add(device);
                    Log.d(TAG, "mBroadcastReceiver1: Added to list: " + device.getName() + ": " + device.getAddress());
                }

                mBluetoothDeviceListAdapter = new BluetoothDeviceListAdapter(context, R.layout.bluetooth_device_adapter_view, mBluetoothDevices);
                mlvDevices.setAdapter(mBluetoothDeviceListAdapter);
            }

        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnClick(R.id.btnDiscover)
    public void discovery(View view){
        Log.d(TAG, "discovery: Looking for unpaired devices.");
        mBluetoothDevices.clear();
        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "discovery: Canceling discovery.");

            checkBTPermission();

            mBluetoothAdapter.startDiscovery();
        }

        if (!mBluetoothAdapter.isDiscovering()){
            checkBTPermission();
            mBluetoothAdapter.startDiscovery();
        }
        IntentFilter discoveryDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mBroadcastReceiver1, discoveryDevicesIntent);
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
        if (mBluetoothDevices.get(i).getBondState() == BluetoothDevice.BOND_BONDED){
            Log.d(TAG, "Clicked on bonded device.");
            mBluetoothDevice = mBluetoothDevices.get(i);
            ((MainActivity) Objects.requireNonNull(getActivity())).startBluetoothService(mBluetoothDevice);
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new CommandFragment()).commit();
            //startBluetoothService(getView());
        }
    }

    public void stopBluetoothService(View view){
        Intent serviceIntent = new Intent(getActivity(), BluetoothService.class);
        getActivity().stopService(serviceIntent);
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
            int permissionCheck = getActivity().checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += getActivity().checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
            } else {
                Log.d(TAG, "checkBTPermission: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }
    }

    // ----------------- Getter/Setter -----------------

    public void setDiscoverStatus(String status) {
        discoverStatus.setText(status);
    }

    public Button getMbtnDiscover() {
        return mbtnDiscover;
    }

}
