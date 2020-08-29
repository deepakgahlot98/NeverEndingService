package com.gahlot.neverendingservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Timer;
import java.util.TimerTask;

public class AutoStartService extends Service {

    private static final String TAG = "AutoService";
    public int counter = 0;
    private Timer timer;
    private TimerTask timerTask;
    ResultReceiver myResultReceiver;


    public static final String ACTION_FOO = "com.gahlot.neverendingservice.FOO";
    public static final String EXTRA_PARAM_A = "com.gahlot.neverendingservice.PARAM_A";

    public AutoStartService(Context context) {
        Log.i(TAG, "AutoStartService: Here we Go!!!!!");
    }

    public AutoStartService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        startTimer(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: Service is destroyed :( ");
        Intent broadcastIntent = new Intent(this, RestartBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    public void startTimer(Context context) {
        timer = new Timer();

        //initialize the TimerTask's job
        initialiseTimerTask(context);

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    public static void broadcastActionBaz(Context context, String param) {
        Intent intent = new Intent(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM_A, param);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(context);
        bm.sendBroadcast(intent);
    }

    public void initialiseTimerTask(final Context context) {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "Timer is running " + counter++);
                broadcastActionBaz(context,String.valueOf(counter));
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
