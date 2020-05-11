package htw_berlin.ba_timsitte.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.concurrent.ThreadLocalRandom;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import htw_berlin.ba_timsitte.R;
import htw_berlin.ba_timsitte.network.AODVNetworkProtocol;

public class CommandActivity extends AppCompatActivity {
    @BindView(R.id.app_toolbar) Toolbar mToolbar;

    AODVNetworkProtocol protocol = AODVNetworkProtocol.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);
        ButterKnife.bind(this);

        initiateSupportActionBar();
    }

    // ----------------- Toolbar methods -----------------
    public void initiateSupportActionBar(){
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setTitle("Commander");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_command, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_btnmap:
                Intent intent = new Intent(this, MapActivity.class);
                intent.putParcelableArrayListExtra("nodeList", protocol.getNodeList());
                startActivity(intent);
                return true;

            case R.id.menu_btnsettings:
                Intent intent2 = new Intent(this, SettingsActivity.class);
                startActivity(intent2);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // ----------------- OnClick methods -----------------

    @OnClick(R.id.btnSend)
    public void sendCommand(){
        // test add node
        addRandomNode();
    }

    public void addRandomNode(){
        double lat_min = 52.564000;
        double lat_max = 52.569000;
        double lon_min = 13.404000;
        double lon_max = 13.409000;
        double lat_random = ThreadLocalRandom.current().nextDouble(lat_min, lat_max);
        double lon_random = ThreadLocalRandom.current().nextDouble(lon_min, lon_max);
        String name = "test";

        protocol.addNewNode(name, lat_random, lon_random);
    }
}
