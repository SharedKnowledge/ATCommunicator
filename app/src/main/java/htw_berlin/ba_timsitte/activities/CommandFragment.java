package htw_berlin.ba_timsitte.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.Message;
import android.text.InputType;
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

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.communication.BluetoothService;
import htw_berlin.ba_timsitte.communication.Constants;
import htw_berlin.ba_timsitte.network.AODVConstants;
import htw_berlin.ba_timsitte.network.AODVHello;
import htw_berlin.ba_timsitte.network.AODVMessage;
import htw_berlin.ba_timsitte.network.AODVNetworkProtocol;
import htw_berlin.ba_timsitte.network.AODVPacket;
import htw_berlin.ba_timsitte.network.AODVRERR;
import htw_berlin.ba_timsitte.network.AODVRREP;
import htw_berlin.ba_timsitte.network.AODVRREP_ACK;
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
    @BindView(R.id.sendTo) EditText mSendToEditText;
    @BindView(R.id.btnAODV) Button btnAODV;
    @BindView(R.id.nodeName) TextView nodeNameTextView;

    private BluetoothService mBluetoothService = null;
    private AODVNetworkProtocol mAODVNetworkProtocol = null;

    private boolean isAODVActive = false;
    private String nodeName;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Socket status
    private static final int AT_STATE_NONE = 0;       // we're doing nothing
    private static final int AT_STATE_LISTEN = 1;     // now listening for incoming messages
    private static final int AT_STATE_BUSY = 2;       // performing
    private static final int AT_STATE_WRITING = 3;    // writing/sending

    // Name of the connected bluetooth device
    private String mConnectedDeviceName = null;

    // Array adapter for the conversation thread
    private ArrayAdapter<String> mCommunicationArrayAdapter;
    private ArrayAdapter<String> mAODVArrayAdapter;

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    private BluetoothAdapter mBluetoothAdapter = null;

    // BroadcastReceiver related
    private Context _context;
    private BroadcastReceiver aodvBroadcastReceiver;
    private final String AODV_STATUS = "htw_berlin.ba_timsitte.AODV_STATUS";
    private final String AODV_STATUS_EXTRA = "htw_berlin.ba_timsitte.AODV_STATUS";
    // AODV status
    private static final int AODV_ON = 1;
    private static final int AODV_OFF = 0;

    String receivedFrom = ""; // address which was the last messages received

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
        setUpAODVBr();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        _context = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mBluetoothAdapter == null) {
            return;
        }
        // If BT is not on, request that it will be enabled.
        // setupCommunication() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, set up the communication
        } else if (mBluetoothService == null) {
            initiateCommunication();
        }

        FragmentActivity activity = getActivity();
        // AODV terminal
        mAODVArrayAdapter= new ArrayAdapter<>(activity, R.layout.message);
        mAODVView.setAdapter(mAODVArrayAdapter);
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
    public void onDestroyView()
    {
        super.onDestroyView();
        _context.unregisterReceiver(aodvBroadcastReceiver);
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
        // Communication terminal
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

    @OnClick(R.id.btnAODV)
    public void toggleAODV(){
        Log.d(TAG, "BEGIN toggleAODV");

        FragmentActivity activity = getActivity();

        // AODV is deactivated
        if (!isAODVActive){

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Name your node");

            // Set up the input
            final EditText input = new EditText(activity);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(nodeName);
            builder.setView(input);

            // Set up the button
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    nodeName = input.getText().toString();
                    Log.i(TAG, "toggleAODV onClick: new name " +  nodeName);
                    if (nodeName.length() > 0 && nodeName.length() <= 4){
                        Intent intent = new Intent(AODV_STATUS);
                        intent.putExtra(AODV_STATUS_EXTRA, AODV_ON);
                        _context.sendBroadcast(intent);
                    } else {
                        Toast.makeText(_context, "Name must be between 1 and 4 characters long.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.show();

        // AODV is activated
        } else {
            Intent intent = new Intent(AODV_STATUS);
            intent.putExtra(AODV_STATUS_EXTRA, AODV_OFF);
            _context.sendBroadcast(intent);
        }
        Log.d(TAG, "END toggleAODV");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @OnClick(R.id.btnSend)
    public void sendMessage() {
        Log.d(TAG, "BEGIN sendMessage");
        String message = mOutEditText.getText().toString();
        String sendTo = mSendToEditText.getText().toString();
        if (isAODVActive){
            // Check that there's actually something to send
            if (message.length() > 0 && sendTo.length() > 0 && sendTo.length() <= 4){
                AODVPacket aodvPacket = new AODVPacket(mAODVNetworkProtocol.getOwnNodeName(), sendTo, message);
                Log.d(TAG, "sendMessage: aodvPacket " + aodvPacket.toString());
                mAODVNetworkProtocol.handleIncomingMessage(aodvPacket.getOriginator(),
                        String.join("|", "5", mAODVNetworkProtocol.getOwnNodeName(), sendTo, message));
            } else {
                Toast.makeText(_context, "At least one text field is empty or sendTo is longer than 4 characters",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Check that there's actually something to send
            if (message.length() > 0) {
                // Get the message bytes and tell the BluetoothService to write
                byte[] send = message.getBytes();
                mBluetoothService.write(send);
                mCommunicationArrayAdapter.add(getCurrentTime() + message);
            } else {
                Toast.makeText(_context, "Text field is empty.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Reset out string buffer to zero and clear the edit text fields
        mOutStringBuffer.setLength(0);
        mOutEditText.setText(mOutStringBuffer);
        mSendToEditText.setText("");
        Log.d(TAG, "END sendMessage");
}

    /**
     *
     */
    private class SendAODVMessage extends Thread {
        private String addr;
        private Boolean success = false;
        private int timeout1 = 5;
        private int timeout2 = 5;
        private AODVMessage msg;

        public SendAODVMessage(AODVMessage msg, String nextAddr){
            // Broadcast messages
            if (msg instanceof AODVRREQ ||
                msg instanceof AODVHello){
                addr = "AT+ADDR=FFFF\r\n";
            }
            // Unicast messages
            if (msg instanceof AODVRREP ||
                msg instanceof AODVRERR ||
                msg instanceof AODVPacket){
                addr = String.format("AT+ADDR=%s\r\n", nextAddr);
            }
            if (msg instanceof AODVRREP_ACK){

            }

            this.msg = msg;
        }

        public void startThread() {
            Log.d(TAG, "BEGIN SendAODVMessage startThread");
            // AT+ADDR=\r\n
            // mBluetoothService.write();
            //msg.toString();
            Log.d(TAG, "END SendAODVMessage startThread");
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

        @RequiresApi(api = Build.VERSION_CODES.O)
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
                    // Construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mCommunicationArrayAdapter.add(getCurrentTime() + "Me:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // Construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    // "LR, XXXX, XX, Text" is the format when receiving bytes from other LoRa moduls
                    if (readMessage.startsWith("LR,")){
                        String rMsg[] = readMessage.split(",");
                        Log.i(TAG, "handleMessage: received message from another node with name " + rMsg[1]);
                        if (rMsg[3].startsWith("AODV|")){
                            String aodvMessage = readMessage.substring(6);
                            mAODVNetworkProtocol.handleIncomingMessage(rMsg[1], aodvMessage);
                        }
                    }

                    mCommunicationArrayAdapter.add(getCurrentTime() + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // Save the connected device's name
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

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(Message msg) {
//            try {
//                ((MainActivity) getActivity()).setRoutingTable(mAODVNetworkProtocol.getRoutingTable());
//                ((MainActivity) getActivity()).setRreqTable(mAODVNetworkProtocol.getRequestTable());
//                Log.i(TAG, "handleMessage: routingtable size" + mAODVNetworkProtocol.getRoutingTable().size());
//                Log.i(TAG, "handleMessage: rreqtable size" + mAODVNetworkProtocol.getRequestTable().size());
//            } catch (NullPointerException e){
//                Log.e(TAG, "handleMessage: empty routing table", e);
//            }
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case AODVConstants.AODV_RREQ:
                    Log.i(TAG, "handleMessage: AODV RREQ");
                    AODVRREQ aodvrreq = (AODVRREQ) msg.obj;
                    SendAODVMessage aodvMessageThread = new SendAODVMessage(aodvrreq, "FFFF");
                    aodvMessageThread.startThread();
                    mAODVArrayAdapter.add(getCurrentTime() + aodvrreq.toInfoString() + " CREATED");
                    break;
                case AODVConstants.AODV_RREP:
                    Log.i(TAG, "handleMessage: AODV RREP");
                    AODVRREP aodvrrep = (AODVRREP) msg.obj;
                    mAODVArrayAdapter.add(getCurrentTime() + aodvrrep.toInfoString() + " CREATED");
                    break;
                case AODVConstants.AODV_RERR:
                    Log.i(TAG, "handleMessage: AODV RERR");
                    AODVRERR aodvrerr = (AODVRERR) msg.obj;
                    mAODVArrayAdapter.add(getCurrentTime() +  aodvrerr.toInfoString() + " CREATED");
                    break;
                case AODVConstants.AODV_RERR_ACK:
                    Log.i(TAG, "handleMessage: AODV RERR_ACK");
                    AODVRREP_ACK aodvrrep_ack = (AODVRREP_ACK) msg.obj;
                    mAODVArrayAdapter.add(getCurrentTime() +  aodvrrep_ack.toInfoString() + " CREATED");
                    break;
                case AODVConstants.AODV_PACKET:
                    Log.i(TAG, "handleMessage: AODV PACKET");
                    AODVPacket aodvPacket = (AODVPacket) msg.obj;
                    mAODVArrayAdapter.add(getCurrentTime() + aodvPacket.toInfoString() + " CREATED");
                    break;
                case AODVConstants.AODV_HELLO:
                    Log.i(TAG, "handleMessage: AODV HELLO");
                    AODVHello aodvHello = (AODVHello) msg.obj;
                    mAODVArrayAdapter.add(getCurrentTime() + aodvHello.toInfoString() + " CREATED");
                    break;
                case AODVConstants.AODV_INFO:
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
                    // Bluetooth is now enabled, so set up a communication session
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

    private String getCurrentTime(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        String out = simpleDateFormat.format(new Date());
        return out + " ";
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

    private void setUpAODVBr(){
        Log.d(TAG, "BEGIN setUpAODVBr");
        aodvBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "BEGIN aodvBroadcastReceiver onReceive");
                if (AODV_STATUS.equals(intent.getAction())) {
                    int state = intent.getIntExtra(AODV_STATUS_EXTRA, 0);
                    switch (state) {
                        case AODV_ON:
                            Log.i(TAG, "aodvBroadcastReceiver onReceive: AODV ON");
                            Toast.makeText(context, "AODV turned on.",
                                    Toast.LENGTH_SHORT).show();
                            btnAODV.setText("Deactivate AODV");
                            mSendToEditText.setEnabled(true);
                            nodeNameTextView.setText("Name " + nodeName);
                            mAODVNetworkProtocol = new AODVNetworkProtocol(nodeName, aodvHandler);
                            isAODVActive = true;
                            break;
                        case AODV_OFF:
                            Log.i(TAG, "aodvBroadcastReceiver onReceive: AODV OFF");
                            Toast.makeText(context, "AODV turned off.",
                                    Toast.LENGTH_SHORT).show();
                            btnAODV.setText("Activate AODV");
                            nodeNameTextView.setText("Name ");
                            mSendToEditText.setEnabled(false);
                            isAODVActive = false;
                            break;
                    }
                }
                Log.d(TAG, "END aodvBroadcastReceiver onReceive");
            }
        };
        _context.registerReceiver(aodvBroadcastReceiver, new IntentFilter(AODV_STATUS));
        Log.d(TAG, "END setUpAODVBr");
    }


    // ----------------- Getter/Setter -----------------

    public void setConnectedToTextViewText(String status) {
        this.connectedToTextView.setText(status);
    }

    public BluetoothService getmBluetoothService() {
        return mBluetoothService;
    }
}
