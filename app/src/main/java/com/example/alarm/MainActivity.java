package com.example.alarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mail.GMailSender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button btnStart, btnStop;
    TextView textView, textView1;
    TimePicker timePicker;
    Calendar calendar;
    AlarmManager alarmManager;
    PendingIntent pendingIntent;
    AlarmManager sendManager;
    PendingIntent pendingSendIntent;


    Calendar cal_send;
    Calendar cal_now;

    private  static final String CHANNEL_ID ="TEST";
    private  static final String CHANNEL_NAME ="ALARM";
    private  static final String CHANNEL_DESC ="Simple";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String recv;
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        textView = (TextView) findViewById(R.id.textView);
        textView1 = (TextView) findViewById(R.id.textViewTimesend);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        calendar = Calendar.getInstance();
        alarmManager =(AlarmManager)getSystemService(ALARM_SERVICE);
        sendManager =(AlarmManager) getSystemService(ALARM_SERVICE);

        cal_now =Calendar.getInstance();
        cal_send = Calendar.getInstance();

        final Intent intent = new Intent(MainActivity.this,AlarmReceiver.class);
        final Intent intentSend = new Intent( MainActivity.this, AlarmManager.class);
        //if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.O){
        //    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        //    channel.setDescription(CHANNEL_DESC);
        //    NotificationManager manager = getSystemService(NotificationManager.class);
        //    manager.createNotificationChannel(channel);
        //}

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_SMS
                //Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        else{
            Date dat = new Date();


            cal_now.setTime(dat);
            cal_send.setTime(dat);
            Calendar calendar_temp;
            calendar_temp = Calendar.getInstance();
            calendar_temp.setTime(dat);
            cal_send.set(Calendar.HOUR_OF_DAY, 03 );
            cal_send.set(Calendar.MINUTE, 00);
            cal_send.set(Calendar.SECOND,00);
            if (cal_send.compareTo(cal_now)<=0){
                cal_send.add(Calendar.DATE, 1);
            }
            else{
//            else (cal_send.compareTo(cal_now)>0){
                calendar_temp.add(Calendar.DATE,1);
                if(calendar_temp.compareTo(cal_send) <0){
                    cal_send.set(Calendar.DATE, cal_now.get(Calendar.DATE));
                }

            }

            intent.putExtra("extra", "send");
            Log.e("Time", cal_send.getTime().toString());
            Log.e("Time now", cal_now.getTime().toString());
            pendingSendIntent = PendingIntent.getBroadcast(MainActivity.this,
                            1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            sendManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal_send.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingSendIntent);
            textView1.setText(cal_send.getTime().toString());
            btnStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int set_hour = 0;
                    int set_minute = 0;

                    calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());

                    int hour = timePicker.getCurrentHour();
                    int minute = timePicker.getCurrentMinute();

                    String Shour;
                    String Sminute;
                    if (hour < 10) {
                        Shour = "0" + String.valueOf(hour);
                    } else {
                        Shour = String.valueOf(hour);
                    }
                    if (minute < 10) {
                        Sminute = "0" + String.valueOf(minute);
                    } else {
                        Sminute = String.valueOf(minute);
                    }
                    intent.putExtra("extra", "on");
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                            0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

                    textView.setText("Giờ hẹn: " + Shour + ":" + Sminute);

                }
            });

            btnStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textView.setText("Dừng lại");
                    alarmManager.cancel(pendingIntent);
                    intent.putExtra("extra", "off");
                    sendBroadcast(intent);

                }
            });

        }
    }

//    private  void displayNotification(String s){
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
//                        .setSmallIcon(R.drawable.ic_bell).setContentTitle("ALARM !!!").
//                        setContentText("").
//                        setPriority(NotificationCompat.PRIORITY_DEFAULT);
//        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(MainActivity.this);
//        mNotificationMgr.notify(1,mBuilder.build());
//    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
