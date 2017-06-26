package uct.radiouct;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Gino on 25-06-2017.
 */

public class NotificationControl extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        context.sendBroadcast(new Intent("STOPRADIO"));
        manager.cancel(intent.getExtras().getInt("id"));
        manager.cancelAll();

    }
}
