package code.sample.ed.chtvoice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by CTBC_CCIT3_04 on 17/2/13.
 */
public class cthservice extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context,PhoneListenerService.class);
        context.startService(service);
    }

}