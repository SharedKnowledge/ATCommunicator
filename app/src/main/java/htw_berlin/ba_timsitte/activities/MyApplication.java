package htw_berlin.ba_timsitte.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import htw_berlin.ba_timsitte.communication.BluetoothService;
import htw_berlin.ba_timsitte.communication.Constants;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks{

    public static final String TAG = "MyApplication";

    public static final String CHANNEL_ID = "ServiceChannel";

    private Context ctx;





    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MyApplication created");
        createNotificationChannel();

        ctx = getApplicationContext();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }

    Handler.Callback realCallback = null;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("ResourceType")
        public void handleMessage(android.os.Message msg) {
            if (realCallback != null) {
                realCallback.handleMessage(msg);
            }
            switch (msg.what){
                case Constants
                        .MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case BluetoothService.STATE_CONNECTED:
                            Log.d(TAG, "handleMessage: STATE_CONNECTED");
                            break;
                        case  BluetoothService.STATE_CONNECTING:
                            Log.d(TAG, "handleMessage: STATE_CONNECTING");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Log.d(TAG, "handleMessage: STATE_NONE");
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    Log.d(TAG, "handleMessage: MESSAGE_WRITE");
                    break;
                case Constants.MESSAGE_READ:
                    Log.d(TAG, "handleMessage: MESSAGE_READ");
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "handleMessage: MESSAGE_DEVICE_NAME");
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(ctx, msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    public Handler getHandler() {
        return handler;
    }
    public void setCallBack(Handler.Callback callback) {
        this.realCallback = callback;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
