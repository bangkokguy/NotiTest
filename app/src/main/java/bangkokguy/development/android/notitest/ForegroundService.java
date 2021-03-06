package bangkokguy.development.android.notitest;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import static android.content.Intent.ACTION_BATTERY_CHANGED;

public class ForegroundService extends Service {

    public ForegroundService() { }

    final private static boolean DEBUG=true;
    final private static String TAG=ForegroundService.class.getSimpleName();

    OverlayView overlayView;
    ReceiveBroadcast receiveBroadcast;
    BatteryManager bm;

    int
            eCurrentNow,
            eCurrentAverage,
            eChargeCounter,
            eVoltage,
            screenWidth,
            screenHeight;

    String eCurrMaxString;

    final static int MAX_VIEW_HEIGHT = 40;

    @Override
    public void onCreate() {
        if (DEBUG) Log.d(TAG, "OnCreate()");
        NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
        nb.setContentText("a");
        nb.setContentTitle("a");
        nb.setSmallIcon(R.mipmap.ic_launcher);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            nb.setPriority(Notification.PRIORITY_MIN);
        }
        Notification n = nb.build();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        n.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(42, n);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        Point size = new Point();
        d.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        WindowManager.LayoutParams params = new
                WindowManager.LayoutParams (
                screenWidth, MAX_VIEW_HEIGHT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, //TYPE_SYSTEM_ALERT
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, //FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSPARENT
        );

        params.gravity = Gravity.BOTTOM;
        overlayView = new OverlayView(this);
        wm.addView(overlayView, params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bm = (BatteryManager) this.getSystemService(Context.BATTERY_SERVICE);
        }
        receiveBroadcast = new ReceiveBroadcast();
        this.registerReceiver(
                receiveBroadcast,
                new IntentFilter(ACTION_BATTERY_CHANGED));
    }

    String getFileText () {
        try {
            File file = new File("/sys/class/power_supply/usb/current_max");
            FileInputStream inStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(inStream);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String s;

            while ((s = br.readLine()) != null) {
                sb.append(s);

            br.close();
            inStream.close();
            isr.close();

            Log.d(TAG, "File:"+sb.toString());

            return sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG,"IOException in file read");
        }
        return "error";
    }

    public class ReceiveBroadcast extends BroadcastReceiver {

        final private static boolean DEBUG=true;
        final private String TAG=ReceiveBroadcast.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if(DEBUG)Log.d(TAG,"intent: "+intent.toString()+"intent extraInteger:"+Integer.toString(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)));
            switch (intent.getAction()) { // @formatter:off
                case ACTION_BATTERY_CHANGED: if(DEBUG)Log.d(TAG,"case battery changed");
                    if(DEBUG)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            eCurrentNow = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                            eCurrentAverage = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
                            eChargeCounter = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                            Log.d(TAG, "BATTERY_PROPERTY_CURRENT_NOW="+Long.toString(eCurrentNow));
                            Log.d(TAG, "BATTERY_PROPERTY_CURRENT_AVERAGE="+Long.toString(eCurrentAverage));
                            Log.d(TAG, "BATTERY_PROPERTY_CHARGE_COUNTER="+Long.toString(eChargeCounter));
                        }
                    //get extra info from intent
                    eVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
                    //prepare global variables
                    overlayView.invalidate();
                    break;
                default: if(DEBUG)Log.d(TAG,"case default"); break;
            } // @formatter:on

            eCurrMaxString = getFileText();

            Properties prop = System.getProperties();

            UsbConfiguration u = null;
            UsbManager um = (UsbManager) getSystemService(USB_SERVICE);

            //HashMap<String, UsbDevice> hm;

            Log.d(TAG, um.getDeviceList().toString());

            //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            //    int i = u.getMaxPower();
            //}

            Log.d(TAG,
                    System.getProperties().toString() +
                    System.getenv().toString());
        }
    }

    public class OverlayView extends View {

        final private static boolean DEBUG=true;
        final private String TAG=OverlayView.class.getSimpleName();

        Paint p;

        public OverlayView(Context context) {
            super(context);
            p = new Paint();
            p.setStyle(Paint.Style.FILL);
            p.setStyle(Paint.Style.STROKE);
            p.setTextSize(MAX_VIEW_HEIGHT);
            p.setARGB(255,255,0,0);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if(DEBUG)Log.d(TAG, "onDraw()");

            //canvas.drawLine(1,1,1,1,p);

            canvas.drawText(
                    " CurrentNow: "   + Integer.toString(eCurrentNow) +
                    " CurrentMax: "   + eCurrMaxString +
                    " ChargeCount: "  + Integer.toString(eChargeCounter) +
                    " Voltage: "      + Integer.toString(eVoltage),
                    0, MAX_VIEW_HEIGHT-10, p);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        overlayView.invalidate();

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
