package com.example.sebipops.sigme;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.os.Handler;

import android.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import net.openspatial.EulerAngle;
import net.openspatial.GestureEvent;
import net.openspatial.OpenSpatialEvent;
import net.openspatial.OpenSpatialException;
import net.openspatial.OpenSpatialService;
import net.openspatial.Pose6DEvent;
import net.openspatial.GestureEvent.GestureEventType;
import android.os.CountDownTimer;

 class MyCountDownTimer extends CountDownTimer
{
    public boolean isRunning = false;
    public MyCountDownTimer(long startTime, long interval) {

        super(startTime, interval);
        isRunning = true;
    }

    @Override

    public void onFinish() {

        isRunning = false;
    }

    @Override

    public void onTick(long millisUntilFinished) {

        isRunning = true;
    }
}

public class MainActivity extends Activity {

    public static final String TAG = "SigMe";
    OpenSpatialService mOpenSpatialService;//nod. only nod
    private static final UUID WATCHAPP_UUID = UUID.fromString("9c9a43e6-1e4c-47fe-83fc-2ac7ec69d4e1");
    MyCountDownTimer timer = null;
    static boolean timerset = false;
    public Integer counter = 0;

    private static final int
            MV_SWIPE_UP = 0,
            MV_SWIPE_DOWN = 1,
            MV_SWIPE_LEFT = 2,
            MV_SWIPE_RIGHT = 3,
            MV_SCROLL_UP = 4,
            MV_SCROLL_DOWN = 5,
            MV_CLOCKWISE_ROTATION = 6,
            MV_COUNTERCLOCKWISE_ROTATION = 7;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_item);
        Log.d("TESTOSS", Boolean.toString(PebbleKit.isWatchConnected(getApplicationContext())));

         bindService(new Intent(this,
                        OpenSpatialService.class),
                mOpenSpatialServiceConnection,
                BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mOpenSpatialServiceConnection);
    }

    public void UpdateAngle(String ring, EulerAngle angle) {
        String logline = ring + " " + Double.toString(angle.roll)
                + " " + Double.toString(angle.pitch)
                + " " + Double.toString(angle.yaw);
        Log.d(TAG, logline);
    }

    public void CheckGesture(String ring, GestureEvent.GestureEventType gestureEvent)
    {
        if (!timerset) {
            timerset = true;
            timer = new MyCountDownTimer(500,100);
            timer.start();
        }
        else
        {
            if (timer.isRunning)
                return;
            else
            {
                timerset = false;
                timer = null;
            }
        }
        PebbleDictionary out = new PebbleDictionary();
        switch (gestureEvent)
        {
            case SWIPE_LEFT:
                Log.i("SIGNATURE :", "SWIPE LEFT");
                out.addInt32(0, (int) MV_SWIPE_LEFT);
                PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, out);

                break;
            case SWIPE_RIGHT:
                Log.i("SIGNATURE :", "SWIPE RIGHT");
                out.addInt32(0, (int) MV_SWIPE_RIGHT);
                PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, out);

                break;
            case SWIPE_DOWN:
                Log.i("SIGNATURE :", "SWIPE DOWN");
                out.addInt32(0, (int) MV_SWIPE_DOWN);
                PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, out);


                break;
            case SWIPE_UP:
                Log.i("SIGNATURE :", "SWIPE UP");
                out.addInt32(0, (int) MV_SWIPE_UP);
                PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, out);


                break;
            case SCROLL_DOWN:
                Log.i("SIGNATURE :", "SCROLL DOWN");
                out.addInt32(0, (int) MV_SCROLL_DOWN);
                PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, out);


                break;
            case SCROLL_UP:
                Log.i("SIGNATURE :", "SCROLL UP");
                out.addInt32(0, (int) MV_SCROLL_UP);
                PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, out);


                break;
            case CLOCKWISE_ROTATION:
                Log.i("SIGNATURE :", "CLOCKWISE_ROTATION");
                out.addInt32(0, (int) MV_CLOCKWISE_ROTATION);
                PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, out);


                break;
            case COUNTERCLOCKWISE_ROTATION:
                Log.i("SIGNATURE :", "COUNTERCLOCKWISE_ROTATION");
                out.addInt32(0, (int) MV_COUNTERCLOCKWISE_ROTATION);
                PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, out);

                break;
        }
    }

    private ServiceConnection mOpenSpatialServiceConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mOpenSpatialService = ((OpenSpatialService.OpenSpatialServiceBinder)service).getService();
            mOpenSpatialService.initialize(TAG, new OpenSpatialService.OpenSpatialServiceCallback() {
                @Override
                public void deviceConnected(final BluetoothDevice bluetoothDevice) {
                    try {
                        mOpenSpatialService.registerForPose6DEvents(bluetoothDevice, new OpenSpatialEvent.EventListener() {
                            @Override
                            public void onEventReceived(OpenSpatialEvent openSpatialEvent) {
                                String ringname = bluetoothDevice.getName();
                                Pose6DEvent event = (Pose6DEvent)openSpatialEvent;
                                EulerAngle angle = event.getEulerAngle();
                                UpdateAngle(ringname, angle);
                            }
                        });
                        mOpenSpatialService.registerForGestureEvents(bluetoothDevice, new OpenSpatialEvent.EventListener() {
                            @Override
                            public void onEventReceived(OpenSpatialEvent openSpatialEvent) {
                                String ringname = bluetoothDevice.getName();
                                GestureEvent gestureEvent = (GestureEvent)openSpatialEvent;
                                CheckGesture(ringname, gestureEvent.gestureEventType);
                            }
                        });
                    } catch (OpenSpatialException e) {
                        Log.e(TAG, "Could not register for Pose6D event " + e);
                    }
                }

                @Override
                public void deviceDisconnected(BluetoothDevice bluetoothDevice) {
                }
                @Override
                public void buttonEventRegistrationResult(BluetoothDevice bluetoothDevice, int i) {
                }
                @Override
                public void pointerEventRegistrationResult(BluetoothDevice bluetoothDevice, int i) {
                }
                @Override
                public void pose6DEventRegistrationResult(BluetoothDevice bluetoothDevice, int i) {
                }
                @Override
                public void gestureEventRegistrationResult(BluetoothDevice bluetoothDevice, int i) {
                }
                @Override
                public void motion6DEventRegistrationResult(BluetoothDevice bluetoothDevice, int i) {
                }
            } );
            mOpenSpatialService.getConnectedDevices();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mOpenSpatialService = null;
        }
    };

    class MyTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    counter = 1;
                }
            });
        }
    }
}