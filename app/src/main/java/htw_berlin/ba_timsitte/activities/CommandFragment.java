package htw_berlin.ba_timsitte.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.communication.BluetoothService;
import htw_berlin.ba_timsitte.communication.Constants;
import htw_berlin.ba_timsitte.network.AODVConstants;
import htw_berlin.ba_timsitte.network.AODVMessage;
import htw_berlin.ba_timsitte.network.AODVNetworkProtocol;
import htw_berlin.ba_timsitte.network.AODVRERR;
import htw_berlin.ba_timsitte.network.AODVRREP;
import htw_berlin.ba_timsitte.network.AODVRREQ;

public class CommandFragment extends Fragment {

    private static final String TAG = "CommandFragment";

    @BindView(R.id.btnSend) Button btnSend;
    @BindView(R.id.sendCommand) EditText mOutEditText;
    @BindView(R.id.connectedToTextView) TextView connectedToTextView;
    @BindView(R.id.communicationView) ListView mConversationView;
    @BindView(R.id.aodvView) ListView mAODVView;
    @BindView(R.id.openDeviceSecure) Button btnOpenDeviceSecure;
    @BindView(R.id.openDeviceInsecure) Button btnOpenDeviceInsecure;

    private BluetoothService mBluetoothService = null;
    private AODVNetworkProtocol mAODVNetworkProtocol = null;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Socket status
    public static final int AT_STATE_NONE = 0;       // we're doing nothing
    public static final int AT_STATE_LISTEN = 1;     // now listening for incoming messages
    public static final int AT_STATE_BUSY = 2;       // performing
    public static final int AT_STATE_WRITING = 3;    // writing/sending

    // name of the connected bluetooth device
    private String mConnectedDeviceName = null;

    /**
     * Array adapter for the conversation thread
     */
    private ArrayAdapter<String> mCommunicationArrayAdapter;
    private ArrayAdapter<String> mAODVArrayAdapter;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    private BluetoothAdapter mBluetoothAdapter = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If the adapter is null, then Bluetooth is not supported
        FragmentActivity activity = getActivity();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null && activity != null) {
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_command, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBluetoothAdapter == null) {
            return;
        }
        // If BT is not on, request that it be enabled.
        // setupCommunication() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mBluetoothService == null) {
            initiateCommunication();
        }

        FragmentActivity activity = getActivity();
        // aodv terminal
        mAODVArrayAdapter= new ArrayAdapter<>(activity, R.layout.message);
        mAODVView.setAdapter(mAODVArrayAdapter);

        // initiate aodv protocol
        mAODVNetworkProtocol = new AODVNetworkProtocol("test", aodvHandler);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBluetoothService.getState() == mBluetoothService.STATE_NONE) {
                // Start the Bluetooth communication services
                mBluetoothService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        if (mBluetoothService != null) {
            mBluetoothService.stop();
        }
        super.onDestroy();
    }

    /**
     * Initiating Bluetooth Communication and everything bound to it
     */
    private void initiateCommunication() {
        Log.d(TAG, "initiateCommunication()");

        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        // chat terminal
        mCommunicationArrayAdapter = new ArrayAdapter<>(activity, R.layout.message);
        mConversationView.setAdapter(mCommunicationArrayAdapter);

        // initiate BluetoothService
        mBluetoothService = new BluetoothService(activity, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer();
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @OnClick(R.id.btnSend)
    public void sendMessage() {
        String message = mOutEditText.getText().toString();
        String newString = "5|testOrig|testDesti|" + message;
        mAODVNetworkProtocol.handleIncomingMessages("no one", newString);
        // Check that there's actually something to send
//        if (message.length() > 0) {
//            // Get the message bytes and tell the BluetoothChatService to write
//            byte[] send = message.getBytes();
//            mBluetoothService.write(send);
//
//            // Reset out string buffer to zero and clear the edit text field
//            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
//        }
    }

    /**
     *
     */
    private class SendAODVMessageThread extends Thread {
        private String addr;
        private Boolean success = false;
        private int timeout1 = 5;
        private int timeout2 = 5;

        public SendAODVMessageThread(AODVMessage msg){
            if (msg instanceof AODVRREQ){
                addr = "AT+ADDR=FFFF\r\n";
            }
            if (msg instanceof AODVRREP){
                addr = String.format("AT+ADDR=%s\r\n", ((AODVRREP) msg).getDestination());
            }
            if (msg instanceof AODVRERR){
                addr = "AT+ADDR=FFFF\r\n";
            }
            byte[] send = msg.toString().getBytes();
        }

        @Override
        public void run() {
            // AT+ADDR=\r\n
            while (timeout1>0 || success){
                byte[] send = addr.getBytes();
                mBluetoothService.write(send);
                // mBluetoothService.
                mAODVArrayAdapter.add(addr);
                mAODVArrayAdapter.add("Waiting for response");

                timeout1 =-1;

                }
            }


            // waiting for response AT, OK, repeat until err >= timeout1

            // AT+SEND=XX\r\n

            // waiting for response AT, OK; repeat until err >= timeout1

            // AODVMessage.toString

            // waiting for AT, SENDING

            // waiting for AT, SENDED

    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            connectedToTextView.setText(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mCommunicationArrayAdapter.clear();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            connectedToTextView.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            connectedToTextView.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mCommunicationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.startsWith("AODV|")){
                        // aodv action here
                    }
                    mCommunicationArrayAdapter.add(readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * The Handler that gets information back from the BluetoothService
     */
    @SuppressLint("HandlerLeak")
    private final Handler aodvHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case AODVConstants.AODV_RREQ_SEND:
                    Log.i(TAG, "handleMessage: Send AODV RREQ");
                    String rreqSend = (String) msg.obj;
                    mAODVArrayAdapter.add("RREQ: " + rreqSend);
                    break;
                case AODVConstants.AODV_RREP_SEND:
                    Log.i(TAG, "handleMessage: Send AODV RREP");
                    String rrepSend = (String) msg.obj;
                    mAODVArrayAdapter.add(rrepSend);
                    break;
                case AODVConstants.AODV_RERR_SEND:
                    Log.i(TAG, "handleMessage: Send AODV RERR");
                    String rerrSend = (String) msg.obj;
                    mAODVArrayAdapter.add(rerrSend);
                    break;
                case AODVConstants.AODV_RERR_ACK_SEND:
                    Log.i(TAG, "handleMessage: Send AODV RERR_ACK");
                    String rerrackSend = (String) msg.obj;
                    mAODVArrayAdapter.add(rerrackSend);
                    break;
                case AODVConstants.IP_PACKET_SEND:
                    Log.i(TAG, "handleMessage: Send IP PACKET");
                    String ipPacketSend = (String) msg.obj;
                    mAODVArrayAdapter.add(ipPacketSend);
                    break;
                case AODVConstants.AODV_RREQ_RECEIVED:
                    Log.i(TAG, "handleMessage: Recived AODV RREQ");
                    break;
                case AODVConstants.AODV_RREP_RECEIVED:
                    Log.i(TAG, "handleMessage: Received AODV RREP");
                    break;
                case AODVConstants.AODV_RERR_RECEIVED:
                    Log.i(TAG, "handleMessage: Received AODV RERR");
                    break;
                case AODVConstants.AODV_RERR_ACK_RECEIVED:
                    Log.i(TAG, "handleMessage: Received AODV RERR_ACK");
                    break;
                case AODVConstants.IP_PACKET_RECEIVED:
                    Log.i(TAG, "handleMessage: Received IP PACKET");
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    initiateCommunication();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        Toast.makeText(activity, R.string.bt_not_enabled_leaving,
                                Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                }
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        Bundle extras = data.getExtras();
        if (extras == null) {
            return;
        }
        String address = extras.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mBluetoothService.connect(device, secure);
    }

    @OnClick(R.id.openDeviceSecure)
    public void openDeviceActivitySecure(){
        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
    }
    @OnClick(R.id.openDeviceInsecure)
    public void openDeviceActivityInsecure(){
        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
    }

    // ----------------- BroadcastReceiver -----------------

    private final BroadcastReceiver mATBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("AT_STATUS".equals(intent.getAction())) {
                final int state = intent.getIntExtra("AT_STATUS_EXTRA", 0);
                switch (state) {
                    case AT_STATE_NONE:
                    case AT_STATE_LISTEN:
                        btnSend.setEnabled(true);
                        mOutEditText.setEnabled(true);
                        break;
                    case AT_STATE_BUSY:
                    case AT_STATE_WRITING:
                        btnSend.setEnabled(false);
                        mOutEditText.setEnabled(false);
                        break;

                }
            }
        }
    };

    // ----------------- Getter/Setter -----------------

    public void setConnectedToTextViewText(String status) {
        this.connectedToTextView.setText(status);
    }

    public BluetoothService getmBluetoothService() {
        return mBluetoothService;
    }
}
