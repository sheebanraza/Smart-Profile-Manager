package com.devatwork.sheebanraza.smartsoundprofiler;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.PlaceTypes;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import se.walkercrou.places.GooglePlaces;
import se.walkercrou.places.Place;

import static android.widget.Toast.*;
import static com.devatwork.sheebanraza.smartsoundprofiler.SilentLocations.*;

public class MainActivity extends AppCompatActivity implements LocationListener {

    TextView longitude, latitude , textView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        longitude = (TextView) findViewById(R.id.longitudeID);
        latitude = (TextView) findViewById(R.id.latitudeID);
        textView = (TextView)findViewById(R.id.silentZoneText);
        if (status != ConnectionResult.SUCCESS) {
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
                final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this);
            }
        }
        onLocationChanged(new Location(LocationManager.GPS_PROVIDER));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

    }

    @Override
    public void onLocationChanged(Location location) {
        Double lat =   location.getLatitude();//37.391216; //37.341372;
        Double lng =   location.getLongitude();//-121.982046; //-121.892727;
        latitude.setText(String.valueOf(lat));
        longitude.setText(String.valueOf(lng));

        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address address = addresses.get(0);
            String add = address.getAddressLine(0) + address.getAddressLine(1) + address.getAddressLine(2);
            TextView addText = (TextView)findViewById(R.id.address);
            addText.setText(add);
            textView.setText(String.valueOf("This place is a Silent Zone."));
            new PlacesScan().changeSoundProfile();
            new PlacesScan().execute(add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        makeText(this, "Enabled new provider " + provider, LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled new provider " + provider, Toast.LENGTH_SHORT).show();
    }

    public class PlacesScan extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            GooglePlaces googlePlaces = new GooglePlaces("MY_KEY");
            String address = "";
            for (String add : params) {
                address = address + " " + add;
            }

            HashSet<String> placesTypes = getSilentZonesList();

            List<Place> placeList =
                    googlePlaces.getPlacesByQuery(address, 1);
            for (se.walkercrou.places.Place place : placeList) {
                for(String placeType: placesTypes) {
                    if (place.getTypes().contains(placeType)) {
                        changeSoundProfile();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            textView.setText(String.valueOf("This place is a Silent Zone."));
        }

        @NonNull
        private HashSet<String> getSilentZonesList() {
            HashSet<String> placesTypes = new HashSet<String>();
            placesTypes.add(BANK);
            placesTypes.add(CHURCH);
            placesTypes.add(DENTIST);
            placesTypes.add(DOCTOR);
            placesTypes.add(BANK);
            placesTypes.add(EMBASSY);
            placesTypes.add(FUNERAL_HOME);
            placesTypes.add(HOSPITAL);
            placesTypes.add(HEALTH);
            placesTypes.add(HINDU_TEMPLE);
            placesTypes.add(UNIVERSITY);
            placesTypes.add(MOSQUE);
            placesTypes.add(LIBRARY);
            return placesTypes;
        }

        private void changeSoundProfile() {
            AudioManager audioManager = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            Toast.makeText(getBaseContext(),"Profile set to Silent Mode",Toast.LENGTH_LONG).show();
        }
    }
}
