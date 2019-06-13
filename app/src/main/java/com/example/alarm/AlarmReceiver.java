package com.example.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String chuoi = intent.getExtras().getString("extra");
        Log.e("trang thai key",chuoi);
        Intent myIntent = new Intent(context,Music.class);
        myIntent.putExtra("extra",chuoi);
        context.startService(myIntent);

    }
}
