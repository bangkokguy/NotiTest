package bangkokguy.development.android.notitest;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

public class ForegroundService extends Service {
    //public ForegroundService() {
    //}

    @Override
    public void onCreate() {
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
        nb.setContentText("a");
        nb.setContentTitle("a");
        nb.setSmallIcon(R.mipmap.ic_launcher);
        nb.setPriority(Notification.PRIORITY_MIN);
        Notification n = nb.build();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        n.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(42, n);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
