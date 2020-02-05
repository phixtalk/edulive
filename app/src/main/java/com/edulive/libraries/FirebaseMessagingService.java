package com.edulive.libraries;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.messaging.RemoteMessage;
import com.edulive.MainActivity;
import com.edulive.R;

/**
 * Created by filipp on 5/23/2016.
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            final String pushTicker = remoteMessage.getData().get("ticker");
            final String pushTitle = remoteMessage.getData().get("title");
            final String pushContent = remoteMessage.getData().get("message");

            String gotopage = remoteMessage.getData().get("gotopage");

            String senduserid = remoteMessage.getData().get("senduserid");
            String sendusername = remoteMessage.getData().get("sendusername");
            String receiveruserid = remoteMessage.getData().get("receiveruserid");

            //next alert user on phone and navigate to appropiate page
            Intent i = null;
            final PendingIntent pendingIntent;

            if(gotopage.equals("testpush")){
                i = new Intent(this,MainActivity.class);
                i.putExtra("from_push", "true");
            }

            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

            showNotification(pendingIntent, pushTitle,pushContent,pushTicker);

        }catch (Exception ex){
            System.out.println("myfire_tag:"+ex.getMessage());//set to know how many times its called
        }
    }
    private void showNotification(PendingIntent pendingIntent, String pushTitle,String pushContent,String pushTicker){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(pushTitle)
                .setContentText(pushContent)
                .setTicker(pushTicker)
                .setSmallIcon(R.drawable.logo)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0,builder.build());
    }

    /*
    * int NOTIFICATION_ID = 234;
        String CHANNEL_ID = "my_channel_01";
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(pushTitle)
                .setContentText(pushContent)
                .setTicker(pushTicker)
                .setSmallIcon(R.drawable.logo)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
    * */
}
