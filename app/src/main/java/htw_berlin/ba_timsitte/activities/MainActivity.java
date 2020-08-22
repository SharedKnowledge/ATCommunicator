package htw_berlin.ba_timsitte.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import htw_berlin.ba_timsitte.R;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "MainActivity";
    @BindView(R.id.app_toolbar) Toolbar mToolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.navigationView) NavigationView navigationView;

    private CommandFragment mCommandFragment;
    private MapFragment mMapFragment;
    private SettingsFragment mSettingsFragment;
    private AODVFragment mAODVFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initiateFragments();

        if (findViewById(R.id.fragment_container) != null){

            if (savedInstanceState != null){
                return;
            }

            initiateFirstFragment();
        }

        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(mToolbar);
        setUpHomeButton();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // ----------------- Toolbar methods -----------------

    private void setUpHomeButton(){
        if (getSupportActionBar() != null){
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        } return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_command:
                loadFragment(mCommandFragment);
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_map:
                loadFragment(mMapFragment);
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_settings:
                loadFragment(mSettingsFragment);
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_aodv:
                loadFragment(mAODVFragment);
                drawerLayout.closeDrawers();
                break;
            case R.id.nav_info:
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Info");
                alertDialog.setIcon(R.drawable.ic_info);
                alertDialog.setMessage("This app was build by Tim Sitte.\n\nBachelor Thesis:\n" +
                        "Implementierung von AODV unter Android für ein schmalbandiges drahtloses Netzwerk");

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                drawerLayout.closeDrawers();
                break;
        }
        return true;
    }

    // ----------------- Fragment methods -----------------

    public void initiateFragments(){
        mCommandFragment = new CommandFragment();
        mMapFragment = new MapFragment();
        mSettingsFragment = new SettingsFragment();
        mAODVFragment = new AODVFragment();
    }

    public void initiateFirstFragment(){
        // In case this activity was started with special instructions from an Intent,
        // pass the Intent's extras to the fragment as arguments
        mCommandFragment.setArguments(getIntent().getExtras());
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, mSettingsFragment)
                .add(R.id.fragment_container, mMapFragment)
                .add(R.id.fragment_container, mAODVFragment)
                .add(R.id.fragment_container, mCommandFragment)
                .commit();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction
                .hide(mSettingsFragment)
                .hide(mAODVFragment)
                .hide(mMapFragment)
                .commit();
    }

    public void loadFragment(Fragment fragment) {
        if (fragment instanceof CommandFragment){
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .hide(mSettingsFragment)
                    .hide(mAODVFragment)
                    .hide(mMapFragment)
                    .show(fragment)
                    .commit();
        }
        if (fragment instanceof AODVFragment) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .hide(mSettingsFragment)
                    .hide(mCommandFragment)
                    .hide(mMapFragment)
                    .show(fragment)
                    .commit();
        }
        if (fragment instanceof MapFragment) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .hide(mSettingsFragment)
                    .hide(mAODVFragment)
                    .hide(mCommandFragment)
                    .show(fragment)
                    .commit();
        }
        if (fragment instanceof SettingsFragment) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction
                    .hide(mCommandFragment)
                    .hide(mAODVFragment)
                    .hide(mMapFragment)
                    .show(fragment)
                    .commit();
        }
        Log.d(TAG, "loadFragment: " + fragment.toString());
    }
}
