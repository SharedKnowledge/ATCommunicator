package htw_berlin.ba_timsitte.activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

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

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private static final String TAG = "BluetoothFragment";

    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;

    BluetoothAdapter mBluetoothAdapter;
    public ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<>();
    public BluetoothDeviceListAdapter mBluetoothDeviceListAdapter;

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

    /**
     * Broadcast receiver for listing devices which are not yet paired
     */
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
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
            getActivity().registerReceiver(mBroadcastReceiver1, discoveryDevicesIntent);
        }

        if (!mBluetoothAdapter.isDiscovering()){
            checkBTPermission();
            mBluetoothAdapter.startDiscovery();
            IntentFilter discoveryDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(mBroadcastReceiver1, discoveryDevicesIntent);
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
        if (mBluetoothDevices.get(i).getBondState() == BluetoothDevice.BOND_BONDED){
            Log.d(TAG, "Clicked on bonded device.");
            mConnectedDeviceName = mBluetoothDevices.get(i).getName();
            startBluetoothService(getView());
        }
    }

    @OnClick(R.id.btnStartService)
    public void startBluetoothService(View view){

        Intent serviceIntent = new Intent(getActivity(), BluetoothService.class);
        serviceIntent.putExtra("inputExtra", mConnectedDeviceName);

        getActivity().startService(serviceIntent);
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
            int permissionCheck = getActivity().checkSelfPermission("Manifest.permission.ACCES_FINE_LOCATION");
            permissionCheck += getActivity().checkSelfPermission("Manifest.permission.ACCES_COARSE_LOCATION");
            if (permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
            } else {
                Log.d(TAG, "checkBTPermission: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }
    }
}
