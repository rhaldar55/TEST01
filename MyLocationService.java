package com.rh.myapp;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.rh.myapp.local.LocalLatLongStore;

public class MyLocationService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    LocationManager lManager;
    LocalLatLongStore localLatLongStore;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service Running....")
                .setContentText("Saving Data")
                .setSmallIcon(R.drawable.placeholder)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);


        getLatLongSpeed();
        //do heavy work on a background thread

        return START_NOT_STICKY;

    }


    @SuppressLint("MissingPermission")
    private void getLatLongSpeed() {
        localLatLongStore = new LocalLatLongStore(this);

        lManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        lManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                100, 1,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double lati = location.getLatitude();
                        double longi = location.getLongitude();
                        location.getSpeed();

                        MainActivity.setMapViewUpdate(lati, longi);

                       // Toast.makeText(getApplicationContext(), "" + lati, Toast.LENGTH_SHORT).show();

                        Log.i("TEST", "----------->" + lati);
                        Log.i("TEST", "----------->" + longi);
                        Log.i("TEST", "----------->" + location.getSpeed());


                        Boolean id = localLatLongStore.saveLatiLongi(
                                String.valueOf(System.currentTimeMillis()),
                                String.valueOf(location.getLatitude()),
                                String.valueOf(location.getLongitude()),
                                String.valueOf(location.getSpeed())
                        );

                        if (id) {
                           // Toast.makeText(getApplicationContext(), "SAVED", Toast.LENGTH_LONG).show();
                        }

                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }
        );
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }




}
