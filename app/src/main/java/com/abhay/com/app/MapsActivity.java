package com.abhay.com.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.se.omapi.SEService;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    TextView busNo;
    TextView route;
    TextView passengers;
    TextView status;
    TextView speed;
    TextView eta;
    TextView dist;
    TextView cltext;
    Marker marker;
    Marker user_marker;
    LocationManager locationManager;
    double user_latitude=0;
    double user_longitude=0;
    double lat;
    double lng;
    String _speed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng position = new LatLng(0 ,0);
        marker = mMap.addMarker(new MarkerOptions().position(position).title("center").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("yellow",50,50))));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position,16.0f));
        busNo = findViewById(R.id.BusNumber);
        route=findViewById(R.id.Route);
        passengers=findViewById(R.id.Passengers);
        status=findViewById(R.id.Status);
        speed=findViewById(R.id.Speed);
        dist=findViewById(R.id.DISTANCE);
        eta=findViewById(R.id.ETA);
        cltext=findViewById(R.id.textViewcl);
        final String busNumber = getIntent().getExtras().getString("Bus_Number");
        busNo.setText(busNumber.toString());
        DatabaseReference myRef = database.getReference().child("Buses").child(busNumber);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 _speed = dataSnapshot.child("Speed").getValue().toString();
                long _passengers = dataSnapshot.child("Passengers").getChildrenCount();
                String _status = dataSnapshot.child("Status").getValue().toString();
                String _route = dataSnapshot.child("Route").getValue().toString();
                lat = (Double)dataSnapshot.child("Latitude").getValue();
                lng = (Double)dataSnapshot.child("Longitude").getValue();
                route.setText(_route);
                status.setText("Status : "+ _status);
                passengers.setText("Passengers : " + _passengers);
                speed.setText("Speed : "+ _speed);
                marker.remove();
                LatLng pos = new LatLng(lat ,lng);
                marker = mMap.addMarker(new MarkerOptions().position(pos).title(busNumber).icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("yellow", 50, 50))));
                marker.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                if(user_latitude!=0 || user_longitude!=0) {
                    double diffrence = CalculationByDistance(lat,user_latitude,lng,user_longitude);
                    double time = (diffrence/(Double.parseDouble(_speed)));
                    time=time*60;
                    double hours = time/60;
                    double min = time%60;
                    Log.d("tom",String.valueOf(hours)+String.valueOf(min));
                    eta.setText("ETA : "+String.valueOf(Math.round(hours))+"H "+String.valueOf(Math.round(min))+"M");
                    dist.setText(String.format("Distance: %.1f KM", diffrence));
                }
                else {
                    eta.setText("ETA: ");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast toast = Toast.makeText(getApplicationContext(), "Please Grant Location Permission to App by going to settings!", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        else {

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        user_latitude = location.getLatitude();
                        user_longitude = location.getLongitude();
                        LatLng user_latlng = new LatLng(user_latitude, user_longitude);
                        if (user_marker != null) {
                            user_marker.remove();
                        }
                        user_marker = mMap.addMarker(new MarkerOptions().position(user_latlng).title("Your Location").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("blue", 50, 50))));
                        if (Double.parseDouble(_speed) != 0) {
                            double diffrence = CalculationByDistance(lat, user_latitude, lng, user_longitude);
                            dist.setText(String.format("Distance: %.1f KM", diffrence));
                            double time = (diffrence / (Double.parseDouble(_speed)));
                            time = time * 60;
                            double hours = time / 60;
                            double min = time % 60;
                            Log.d("tom", String.valueOf(hours) + String.valueOf(min));
                            eta.setText("ETA : " + String.valueOf(Math.round(hours)) + "H " + String.valueOf(Math.round(min)) + "M");
                        } else {
                            eta.setText("ETA: ");
                        }

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                });
            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        user_latitude = location.getLatitude();
                        user_longitude = location.getLongitude();
                        LatLng user_latlng = new LatLng(user_latitude, user_longitude);
                        if (user_marker != null) {
                            user_marker.remove();
                        }
                        user_marker = mMap.addMarker(new MarkerOptions().position(user_latlng).title("Your Location").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("blue", 50, 50))));
                        if (Double.parseDouble(_speed) != 0) {
                            double diffrence = CalculationByDistance(lat, user_latitude, lng, user_longitude);
                            dist.setText(String.format("Distance: %.1f KM", diffrence));
                            double time = (diffrence / (Double.parseDouble(_speed)));
                            time = time * 60;
                            double hours = time / 60;
                            double min = time % 60;
                            Log.d("tom", String.valueOf(hours) + String.valueOf(min));
                            eta.setText("ETA : " + String.valueOf(Math.round(hours)) + "H " + String.valueOf(Math.round(min)) + "M");
                        } else {
                            eta.setText("ETA: ");
                        }
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                });

            }
        }
    }
    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
    public double CalculationByDistance(double lat1 , double lat2,double lon1,double lon2) {
        cltext.setText("");
        int Radius = 6371;// radius of earth in Km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }
}
