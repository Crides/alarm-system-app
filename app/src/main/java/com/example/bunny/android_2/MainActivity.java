package com.example.bunny.android_2;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends Activity implements View.OnClickListener {
    Button btnStatus;
    TextView tvAlarm;
    ImageView ivImageView;
    TextView tvToastText;
    TextView tvTextStatus;
    View toast_layout;
    String strOneAlarm;
    String strAlarm = "";
    Bitmap bmCapturedImg;
    String IntentAction = "com.example.intent.action.DATA_FETCHED";
    Intent backgroundService;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(IntentAction);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        ResponseReceiver receiver = new ResponseReceiver();
        registerReceiver(receiver, filter);

        // SET VIEW
        LayoutInflater inflater = getLayoutInflater();
        setContentView(R.layout.activity_main);
        toast_layout = inflater.inflate(R.layout.toast_layout, (ViewGroup) findViewById(R.id.toast_layout));
        tvToastText = (TextView) toast_layout.findViewById(R.id.toast_text);
        toast_layout = toast_layout.findViewById(R.id.toast_layout);
        btnStatus = (Button) findViewById(R.id.btnStatus);
        tvAlarm = (TextView) findViewById(R.id.ViewTime);
        tvAlarm.setTextColor(0xFFFF0000);
        ivImageView = (ImageView) findViewById(R.id.ImageView);
        tvTextStatus = (TextView) findViewById(R.id.textStatus);
        btnStatus.setOnClickListener(this);

        backgroundService = new Intent(this, BackgroundService.class);
        backgroundService.putExtra("once", Boolean.FALSE);
        startService(backgroundService);
        System.out.println("SERVICE WILL START AUTOMATICALLY");
        ShowMessage("", "Normal\nPress to see image.", 0);
    }

    public void onClick(View view) {
//        strAlarm = "";
        tvAlarm.setText("");
        backgroundService.putExtra("once", Boolean.TRUE);
        startService(backgroundService);
        System.out.println("SERVICE WILL START");
        ShowImage();
        ShowToast("Image Updated & Status Cleared", 0);
    }

    public class ResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int type = intent.getIntExtra(BackgroundService.out_type, 0);
            strAlarm = intent.getStringExtra(BackgroundService.out_strAlarm);
            strOneAlarm = intent.getStringExtra(BackgroundService.out_strOneAlarm);
            String Extra = intent.getStringExtra(BackgroundService.out_Extra);
            System.out.println("RECEIVED BROADCAST " + type);
            if (type == 0) { //No Error or Alarm
                ShowMessage(strAlarm, Extra, 0);
            } else if (type == 1){ //Alarm
                //createNotification(strOneAlarm, strAlarm);
                ShowMessage(strAlarm, Extra, 0);
                ShowToast(intent.getStringExtra(BackgroundService.out_strToastMessage), 1);
                ShowImage();
                System.out.println("IMAGE SHOWED");
            } else { //Error
                ShowMessage(strAlarm, Extra, 1);
                ShowToast(intent.getStringExtra(BackgroundService.out_strToastMessage), 1);
            }
		}
    }

    public void ShowImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String imageUrl = $URL$;        //TODO REPLACE THIS BEFORE USE
                    URL url = new URL(imageUrl);

                    Authenticator.setDefault(
                        new Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication($USERNAME$,$PASSWD$.toCharArray());      //TODO REPLACE THIS BEFORE USE
                            }
                        }
                    );
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                        InputStream is = connection.getInputStream();
                        bmCapturedImg = BitmapFactory.decodeStream(is);

                        Runnable runnableImageUpdate = new Runnable() {
                            @Override
                            public void run() {
                                ivImageView.setImageBitmap(bmCapturedImg);
                            }
                        };
                        MainActivity.this.runOnUiThread(runnableImageUpdate);
                        ShowMessage(strAlarm, "Normal\nPress to see image.", 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    strAlarm = "Image Loading Error\n" + strAlarm;
                    ShowMessage(strAlarm, "ERROR:\nImage Loading Error", 1);
                    ShowToast("Image Loading Error", 1);
                }
            }
        }).start();
    }

    public void ShowToast(final String strToastMessage, final int type) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = new Toast(getApplicationContext());
                tvToastText.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.warning, 0, 0);
                tvToastText.setCompoundDrawablePadding(20);
                tvToastText.setText(strToastMessage);
                if (type == 1) {
                    tvToastText.setBackgroundResource(R.drawable.toast_border1);
                }else {
                    tvToastText.setBackgroundResource(R.drawable.toast_border0);
                }
                toast.setView(toast_layout);
                toast.show();
            }
        });
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

    public void ShowMessage(final String Alarm, final String Msg, final int Flag){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvAlarm.setText(Alarm);
                tvTextStatus.setText(Msg);
                if (Flag == 1) {
                    tvTextStatus.setTextColor(0xFFFF0000);
                } else {
                    tvTextStatus.setTextColor(0xFF00FF00);
                }
            }
        });
    }
}
