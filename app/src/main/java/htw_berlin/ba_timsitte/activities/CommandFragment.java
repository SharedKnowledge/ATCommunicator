package htw_berlin.ba_timsitte.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.communication.BluetoothService;
import htw_berlin.ba_timsitte.communication.TerminalText;
import htw_berlin.ba_timsitte.persistence.FileHandler;

public class CommandFragment extends Fragment {

    private static final String TAG = "CommandFragment";

    @BindView(R.id.terminalView) TextView terminalView;
    @BindView(R.id.btnSend) Button btnSend;
    @BindView(R.id.sendCommand) EditText sendCommand;
    @BindView(R.id.connectedToTextView) TextView connectedToTextView;

    private enum Connected { False, Pending, True }
    private Connected connected = Connected.False;
    private String newline = "\r\n";

    private BluetoothService mBluetoothService;
    private FileHandler mFileHandler;
    private String terminalData;

    private TerminalText mTerminalText = TerminalText.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_command, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // terminalView.setText(mTerminalText.getText());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
    }

    @OnClick(R.id.btnSend)
    public void send(View view){
        String str = sendCommand.getText().toString();
//        if (connected != Connected.True){
//            Toast.makeText(getActivity(), "Not connected", Toast.LENGTH_SHORT).show();
//            return;
//        }
        try {
            SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorTextSend)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTerminalText.appendText(str);
            terminalView.append(spn);
            byte[] data = (str + newline).getBytes();
            mBluetoothService.write(data);
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    public void receiveData(byte[] data){
        terminalView.append(new String(data));
    }

    public void appendTextToTerminalView(String str) {
        try {
            SpannableStringBuilder spn = new SpannableStringBuilder(str+'\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorTextSend)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTerminalText.appendText(str);
            terminalView.append(spn);
            byte[] data = (str + newline).getBytes();
            mBluetoothService.write(data);
        } catch (Exception e) {
            Log.e(TAG, String.valueOf(e));
        }
    }

    // ----------------- Getter/Setter -----------------

    public void setConnectedToTextViewText(String str) {
        this.connectedToTextView.setText(str);
    }
}
