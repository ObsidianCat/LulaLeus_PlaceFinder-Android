package android.lulius.com.placefinder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PowerConnectionReceiver extends BroadcastReceiver {
    public static final String TAG = "ChargerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
            Toast.makeText(context, "Device started charging", Toast.LENGTH_LONG).show();
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            Toast.makeText(context, "Device stopped charging", Toast.LENGTH_LONG).show();
        }
    }
}