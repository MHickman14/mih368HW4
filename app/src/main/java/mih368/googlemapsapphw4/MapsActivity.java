package mih368.googlemapsapphw4;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.maps.android.PolyUtil;

import android.util.Log;



public class MapsActivity extends FragmentActivity {

    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";
    static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
    static final JsonFactory JSON_FACTORY = new JacksonFactory();
    static final String PLACES_API_KEY = "AIzaSyAcFgy4B0e7cpOOihtKQPVmI46MliDtdCY";
    static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/geocode/json?";
    private LatLng coors;
    private List<LatLng> latLngs = new ArrayList<LatLng>();
    private GoogleMap googleMap;
    private List<Marker> markers = new ArrayList<Marker>();
    public static final String TAG = MapsActivity.class.getSimpleName();



    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MapsActivity", ("logging enabled"));
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();


    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }


    private void addMarkerToMap(LatLng latLng) {
        Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng)
                .title("title")
                .snippet("snippet"));
        markers.add(marker);

    }

    /**
     * Adds a list of markers to the map.
     */
    public void addMarkersToMap(List<LatLng> latLngs) {
        for (LatLng latLng : latLngs) {
            addMarkerToMap(latLng);
        }
    }

    /**
     * Clears all markers from the map.
     */
    public void clearMarkers() {
        googleMap.clear();
        markers.clear();
    }

    public void sendMessage(View view) {
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        LocationParser parser = new LocationParser();
        Log.i("MapsActivity", ("Send messge has been called and is delivering this address: " + message));
        parser.execute(message);
    }


    private class LocationParser extends AsyncTask<String, Void, Void> {

        private List<LatLng> latLngs = new ArrayList<LatLng>();

        protected Void doInBackground(String... strings) {
            try {
                HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(HttpRequest request) {
                        request.setParser(new JsonObjectParser(JSON_FACTORY));
                    }
                });

                GenericUrl url = new GenericUrl(PLACES_API_BASE);
                url.put("address", strings[0]);
                Log.d(TAG, ("LocationParser has been called and is delivering this url: " + url.toString()));

                HttpRequest request = requestFactory.buildGetRequest(url);
                HttpResponse httpResponse = request.execute();
                Results directionsResult = httpResponse.parseAs(Results.class);
                if (directionsResult.results.size() > 0){
                    coors = new LatLng(directionsResult.results.get(0).location.coordinates.lat,directionsResult.results.get(0).location.coordinates.lng);
                    Log.d(TAG, ("Addresses have been found at these coordinates: " + coors.toString()));
                }
                else
                {
                    Log.i(TAG, ("No address has been found"));
                }



            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
        }


        protected void onPostExecute(Void stupidvariable) {
            mMap.addMarker(new MarkerOptions().position(coors).title("Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coors,15));
        }
    }

    public static class Results{
        @Key("results")
        public List<Geometry> results;
    }
    public static class Geometry {
        @Key("geometry")
        public  Location location;
    }

    public static class Location{
        @Key("location")
        public Coordinates coordinates;
    }

    public static class Coordinates{
        @Key("lat")
        public double lat;

        @Key("lng")
        public double lng;
    }
}


