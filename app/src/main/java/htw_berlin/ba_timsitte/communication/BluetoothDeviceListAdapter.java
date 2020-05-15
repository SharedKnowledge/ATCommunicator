package htw_berlin.ba_timsitte.communication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.LongDef;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.util.ArrayList;


import htw_berlin.ba_timsitte.R;

public class BluetoothDeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private final String TAG = "BluetoothDeviceListAdap";
    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewResourceId;

    public BluetoothDeviceListAdapter(Context context, int textViewResourceId, ArrayList<BluetoothDevice> devices) {
        super(context, textViewResourceId, devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    @SuppressLint("ResourceAsColor")
    public View getView(int position, View convertView, ViewGroup parent){
        convertView = mLayoutInflater.inflate(mViewResourceId, null);

        BluetoothDevice device = mDevices.get(position);

        if (device != null){
            TextView deviceName = (TextView) convertView.findViewById(R.id.tvDeviceName);
            TextView deviceAddress = (TextView) convertView.findViewById(R.id.tvDeviceAddress);
            TextView deviceStatus = (TextView) convertView.findViewById(R.id.tvOnOff);

            if (deviceName != null){
                deviceName.setText(device.getName());
            }
            if (deviceAddress != null){
                deviceAddress.setText(device.getAddress());
            }
            if (deviceStatus != null){
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    deviceStatus.setBackgroundResource(R.color.colorDeviceBondNone);
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDING){
                    deviceStatus.setBackgroundResource(R.color.colorDeviceBonding);
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    deviceStatus.setBackgroundResource(R.color.colorDeviceBonded);
                } else {
                    deviceStatus.setBackgroundResource(R.color.colorDeviceUnclear);
                }

            }
            Log.d(TAG, "getView: " + device.getName() + " with " + device.getAddress() + "STATE: " + device.getBondState());
        }
        return convertView;
    }
}
