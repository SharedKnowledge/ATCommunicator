package htw_berlin.ba_timsitte.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import htw_berlin.ba_timsitte.R;

import java.util.ArrayList;
import java.util.List;

import htw_berlin.ba_timsitte.network.RREQEntry;

public class RREQListAdapter extends ArrayAdapter<RREQEntry> {
    private Context mContext;
    private List<RREQEntry> rreqEntryList = new ArrayList<>();

    public RREQListAdapter(@NonNull Context context, @SuppressLint("SupportAnnotationUsage") @LayoutRes ArrayList<RREQEntry> list) {
        super(context, 0, list);
        mContext = context;
        rreqEntryList = list;
        }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.adapter_view_rreqentry, parent,false);

        RREQEntry currrentRREQEntry = rreqEntryList.get(position);

        TextView rreq_id = (TextView) listItem.findViewById(R.id.av_rreq_id);
        rreq_id.setText(currrentRREQEntry.getId());

        TextView orig = (TextView) listItem.findViewById(R.id.av_rreq_orig);
        orig.setText(currrentRREQEntry.getOriginator());

        return listItem;
        }
}
