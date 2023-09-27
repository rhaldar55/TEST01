package com.rh.myapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rh.myapp.Adpater.LatLongListAdapter;
import com.rh.myapp.data.Dummy;
import com.rh.myapp.local.LocalLatLongStore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button button;
    static SupportMapFragment frag;
    private static final int MULTIPLE_PERMISSIONS = 101;
    AlertDialog.Builder builder;
    LocalLatLongStore localLatLongStore;
    RecyclerView rcViewList;
    private static ArrayList<Dummy> dummyArrayList;
    private static LatLongListAdapter mAdapter;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checkPermissions();
        initView();

    }


    private void initView() {
        rcViewList = (RecyclerView) findViewById(R.id.rcViewList);
        frag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        localLatLongStore = new LocalLatLongStore(MainActivity.this);
        dummyArrayList = new ArrayList<>();
        getSavedLatiLongi();
        mAdapter = new LatLongListAdapter(MainActivity.this, dummyArrayList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rcViewList.setLayoutManager(mLayoutManager);
        rcViewList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

    }

    public static void setMapViewUpdate(double lati, double longi) {
        setMapView(lati, longi);

        mAdapter.notifyDataSetChanged();
    }

    public static void setMapView(Double lati, Double longi) {

        frag.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(final GoogleMap googleMap) {


                MarkerOptions opt1 = new MarkerOptions();
                opt1.position(new LatLng(lati, longi));
                opt1.icon(BitmapDescriptorFactory.fromResource(R.drawable.placeholder));
                opt1.title("Hi I am Rameshwar");
                googleMap.addMarker(opt1);

                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lati, longi), 17f));


            }
        });
    }


    private void getSavedLatiLongi() {
        dummyArrayList = localLatLongStore.getLatiLongi();
    }


    private boolean checkPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(getApplicationContext(), p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }


        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {  // permissions granted.

                    if (checkPermissions())
                        startLocationService();

                } else {
                    builder = new AlertDialog.Builder(this);
                    builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
                    builder.setMessage(getResources().getString(R.string.permission_required))
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Toast.makeText(getApplicationContext(), "App closing ... !",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    checkPermissions();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.setTitle("Permission Is Mandate");
                    alert.show();
                }
                return;
            }
        }
    }

    private void startLocationService() {
        Utils.showSettingsAlert(MainActivity.this);
        startService(new Intent(MainActivity.this, MyLocationService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (checkPermissions()) {
            startLocationService();
        }
        getSavedLatiLongi();
        mAdapter.notifyDataSetChanged();
        if (!Utils.isOnline(this)) {
            Toast.makeText(this, "Please check Connection", Toast.LENGTH_LONG).show();
        }
    }
}