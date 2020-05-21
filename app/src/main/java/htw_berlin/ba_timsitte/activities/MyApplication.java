package htw_berlin.ba_timsitte.activities;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;


public class MyApplication extends Application{

    public static final String TAG = "MyApplication";

    public static final String CHANNEL_ID = "ServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MyApplication created");
        createNotificationChannel();
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
}
