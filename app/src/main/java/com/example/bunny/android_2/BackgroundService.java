package com.example.bunny.android_2;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

public class BackgroundService extends IntentService {

    String AlarmInput;
    String strOneAlarmTime = "";
    String strOneAlarm;
    String strAlarm = "";
    String strToastMessage;
    String strRouterURL = $URL$;        //TODO REPLACE THIS BEFORE USE
    String IntentAction = "com.example.intent.action.DATA_FETCHED";

    public static final String out_type = "type";
    public static final String out_strOneAlarm = "strOneAlarm";
    public static final String out_strAlarm = "strAlarm";
    public static final String out_strToastMessage = "strToastMessage";
    public static final String out_Extra = "Extra";

    public final Handler mHandler = new Handler();

    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Warner Background Service started", Toast.LENGTH_SHORT).show();
        if (intent != null && intent.getBooleanExtra("once", Boolean.FALSE)){
                System.out.println("ONE TIME TASK");
                strAlarm = "";
                CheckAlarm();
        } else {
            System.out.println("SERVICE STARTED! ! !");
            mHandler.postDelayed(runnableTimer, 3000);
        }
        return Service.START_STICKY;
    }
    public BackgroundService() { super("BackgroundService"); }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    protected void onHandleIntent(Intent intent) { }

    // TIMER DEFINITION
    final Runnable runnableTimer = new Runnable() {
        @Override
        public void run() {
            CheckAlarm();
            mHandler.postDelayed(runnableTimer, 5000);
        }
    };

    public String readResult(InputStreamReader inRd) {
        String str = null;
        char cb[] = new char[32];
        try {
            inRd.read(cb);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int i;
        for(i=0; i<cb.length;i++) {
            if ( cb[i]=='\n' || cb[i]=='\r' || cb[i]==0 )
                break;
        }
        if ( i> 0 ) i--;
        str = new String(cb, 0, i);
        return str;
    }

    public void CheckAlarm() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(strRouterURL + $SUB_URL$);        //TODO REPLACE THIS BEFORE USE
                    HttpURLConnection url_conn = (HttpURLConnection) url.openConnection();
                    if (url_conn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        InputStreamReader inStreamReader = new InputStreamReader(url_conn.getInputStream(), "US-ASCII");
                        //BufferedReader Status = new BufferedReader(inStreamReader);
                        //while (AlarmInput == Status.readLine()) {
                        AlarmInput = readResult(inStreamReader);
                        if (AlarmInput!=null) {
                            System.out.println(AlarmInput);
                            long newUnixTime = timeConversion(AlarmInput);
                            long oldUnixTime = 0;
                            if (!strOneAlarmTime.equals("")) {
                                oldUnixTime = timeConversion(strOneAlarmTime);
                            }
                            if (newUnixTime > oldUnixTime) {
                                strOneAlarmTime = AlarmInput;
                                strOneAlarm = "Alarm time: " + AlarmInput;
                                strAlarm = strOneAlarm + "\n" + strAlarm;
                                createNotification(strOneAlarm, strAlarm);
                                sendUserBroadcast(1, strOneAlarm, strAlarm, "New Alarm!!", "Normal\nPress to see image.");
                            } else {
                                sendUserBroadcast(0, strOneAlarm, strAlarm, "New Alarm!!", "Normal\nPress to see image.");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    strToastMessage = "Connection Failed";
                    strAlarm = "Connection Failed\n" + strAlarm;
                    sendUserBroadcast(2, "", strAlarm, "Connection Failed", "ERROR:\nConnection Failed");
                }
            }
        }).start();
    }

    public void sendUserBroadcast(int type, String strOneAlarm, String strAlarm, String strToastMessage, String Extra) {
        Intent broadcastIntent = new Intent()
                .setAction(IntentAction)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .putExtra(out_type, type)
                .putExtra(out_strOneAlarm, strOneAlarm)
                .putExtra(out_strAlarm, strAlarm)
                .putExtra(out_strToastMessage, strToastMessage)
                .putExtra(out_Extra, Extra);
        sendBroadcast(broadcastIntent);
    }


    public void createNotification(String ContentTitle, String BigText) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification notification = new Notification.Builder(this)
                .setTicker("Warner: New Alarm!")
                .setContentTitle("New Alarm!")
                .setContentText(ContentTitle)
                .setStyle(new Notification.BigTextStyle().bigText(BigText))
                .setSmallIcon(R.mipmap.warning)
                .setContentIntent(pendingIntent).build();
        notification.vibrate = new long[]{0, 1000};
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);
    }

    public long timeConversion(String time) {
        long UnixTime = 0;
        DateFormat dfm = new SimpleDateFormat("yyyy MMM dd EEE kk:mm:ss", Locale.US);
        dfm.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        try {
            UnixTime = dfm.parse(time).getTime();
            UnixTime = UnixTime/1000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return UnixTime;
    }
}
