package com.gahlot.neverendingservice;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class RestartBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "RestartBroadcastReceive";
    private static JobScheduler jobScheduler;
    private RestartBroadcastReceiver restartBroadcastReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(RestartBroadcastReceiver.class.getSimpleName(), "Service Stopped, but this is a never ending service.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            scheduleJob(context);
        } else {
            registerRestarterReceiver(context);
            ServiceAdmin bck = new ServiceAdmin();
            bck.launchService(context);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        if (jobScheduler == null) {
            jobScheduler = (JobScheduler) context
                    .getSystemService(JOB_SCHEDULER_SERVICE);
        }
        ComponentName componentName = new ComponentName(context,
                JobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                // setOverrideDeadline runs it immediately - you must have at least one constraint
                // https://stackoverflow.com/questions/51064731/firing-jobservice-without-constraints
                .setOverrideDeadline(0)
                .setPersisted(true).build();
        jobScheduler.schedule(jobInfo);
    }

    public static void reStartTracker(Context context) {
        // restart the never ending service
        Log.i(TAG, "Restarting tracker");
        Intent broadcastIntent = new Intent(Globals.RESTART_INTENT);
        context.sendBroadcast(broadcastIntent);
    }


    private void registerRestarterReceiver(final Context context) {

        // the context can be null if app just installed and this is called from restartsensorservice
        // https://stackoverflow.com/questions/24934260/intentreceiver-components-are-not-allowed-to-register-to-receive-intents-when
        // Final decision: in case it is called from installation of new version (i.e. from manifest, the application is
        // null. So we must use context.registerReceiver. Otherwise this will crash and we try with context.getApplicationContext
        if (restartBroadcastReceiver == null)
            restartBroadcastReceiver = new RestartBroadcastReceiver();
        else try{
            context.unregisterReceiver(restartBroadcastReceiver);
        } catch (Exception e){
            // not registered
        }
        // give the time to run
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // we register the  receiver that will restart the background service if it is killed
                // see onDestroy of Service
                IntentFilter filter = new IntentFilter();
                filter.addAction(Globals.RESTART_INTENT);
                try {
                    context.registerReceiver(restartBroadcastReceiver, filter);
                } catch (Exception e) {
                    try {
                        context.getApplicationContext().registerReceiver(restartBroadcastReceiver, filter);
                    } catch (Exception ex) {

                    }
                }
            }
        }, 1000);

    }
}