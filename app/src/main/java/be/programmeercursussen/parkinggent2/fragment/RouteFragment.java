package be.programmeercursussen.parkinggent2.fragment;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import be.programmeercursussen.parkinggent2.R;
import be.programmeercursussen.parkinggent2.model.Parking;
import be.programmeercursussen.parkinggent2.routehelper.DirectionsJSONParser;
import be.programmeercursussen.parkinggent2.service.LocationTracker;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class RouteFragment extends Fragment implements LocationSource, RadioGroup.OnCheckedChangeListener {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private SupportMapFragment fragment;
    private Parking parking;
    private MapView mapView;
    private GoogleMap googleMap;
    private double current_latitude, current_longitude;
    private double latitude, longitude;
    private RadioButton rbDriving, rbBiCycling, rbWalking;

    private ArrayList<LatLng> markerPoints;
    private String mode;
    private int mMode=0;
    private final int MODE_DRIVING=0;
    private final int MODE_BICYCLING=1;
    private final int MODE_WALKING=2;
    private String url;

    public RouteFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate and return the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_route, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) view.findViewById(R.id.mapView_Route);
        mapView.onCreate(savedInstanceState);

        // get our object send by the Intent from the SplashScreen which had a Service running (network operation)
        parking = getActivity().getIntent().getExtras().getParcelable("parking");

        // Gets to GoogleMap from the MapView and does initialization stuff
        googleMap = mapView.getMap();

        // Android will use the fused location provider instead of the original Location Provider, and the battery usage should be low
        // you can check if your application uses low/high battery usage by going to Settings on your Android smartphone and select Location
        googleMap.setLocationSource(this);

        googleMap.setMyLocationEnabled(false);

        // Setting Zoom Controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        try {
            // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
            MapsInitializer.initialize(this.getActivity());

            // assign current latitude and longitude from Android device to variables
            latitude = Double.valueOf(parking.getLatitude());
            longitude = Double.valueOf(parking.getLongitude());

            LatLng latLng_parking = new LatLng(latitude, longitude);

            googleMap.addMarker(new MarkerOptions()
                    .position(getCurrentPosition())
                    .title("U bevindt zich hier.")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            googleMap.addMarker(new MarkerOptions()
                    .position(latLng_parking)
                    .title("Bestemming : " + parking.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            // Move the camera instantly to latlng with a zoom of 13.
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng_parking, 13));

            // Construct a CameraPosition to center the map ???
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng_parking).build();      // Sets the center of the map to latlng

            // Zoom in, animating the camera.
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(13), 2000, null);


        } catch (Exception e) {
            e.printStackTrace();
        }

        rbDriving = (RadioButton) view.findViewById(R.id.rb_route_driving);
        //rbBiCycling = (RadioButton) view.findViewById(R.id.rb_route_bycicling);
        //rbWalking = (RadioButton) view.findViewById(R.id.rb_route_walking);

        // Getting reference to rg_views of the layout
        RadioGroup rgViewsMap = (RadioGroup) view.findViewById(R.id.rg_views_map);
        rgViewsMap.clearCheck();

        // Getting reference to rg_views of the layout
        RadioGroup rgViewsRoute = (RadioGroup) view.findViewById(R.id.rg_views_route);
        rgViewsRoute.clearCheck();

        //rbDriving.setTextColor(Color.BLUE);
        //rbBiCycling.setTextColor(Color.RED);
        //rbWalking.setTextColor(Color.MAGENTA);

        // Setting Checked ChangeListener
        rgViewsMap.setOnCheckedChangeListener(this);

        // Setting Checked ChangeListener
        rgViewsRoute.setOnCheckedChangeListener(this);

        return view;
    }

    // Setting Checked ChangeListener for radiogroup with radiobuttons
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // Currently checked is rb_map_normaal
        if(checkedId==R.id.rb_map_normal){
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        // Currently checked is rb_map_satelliet
        if(checkedId==R.id.rb_map_satellite){
            // hybrid map is a kind of satellite overview with streets
            // for end user the word hybrid is a bit weird, so I use the word 'satelliet' as text on layout fragment
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        // assign current latitude and longitude from Android device to variables
        latitude = Double.valueOf(parking.getLatitude());
        longitude = Double.valueOf(parking.getLongitude());

        LatLng latLng_parking = new LatLng(latitude, longitude);

        // Getting URL to the Google Directions API
        url = getDirectionsUrl(getCurrentPosition(), latLng_parking);

        // Currently checked is rb_route_car
        if(checkedId==R.id.rb_route_driving){
            // This DownloadTask class is a private class inside fragment RouteFragment
            // it shouldn't be mistaken with the public class inside the package folder asynctask
            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }

        /*

        // Currently checked is rb_route_bycicle
        if(checkedId==R.id.rb_route_bycicling){
            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }

        // Currently checked is rb_route_walking
        if(checkedId==R.id.rb_route_walking){
            DownloadTask downloadTask = new DownloadTask();

            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        }

        */
    }

    private LatLng getCurrentPosition()
    {
        // load class LocationTracker, so our own Android device can get the current latitude and longitude
        LocationTracker locationTracker = new LocationTracker(getActivity());
        if (!locationTracker.canGetLocation()) {
            locationTracker.showSettingsAlert();
        } else {

            current_latitude = locationTracker.getLatitude();
            current_longitude = locationTracker.getLongitude();

            locationTracker.stopUsingGPS();
        }

        return new LatLng(current_latitude, current_longitude);
    }

    // most tutorials on the Internet refer that we should use SupportMapFragment and FragmentActivity
    // but here I use a Fragment to load our GoogleMap that is our MapView, but it should not work on Android 4.4, you only get a roster on the mapview with the latlng position showed
    // but if you add these override methods in the Fragment the MapView will work
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        // Travelling Mode
        String mode = "mode=driving";

        if(rbDriving.isChecked()){
            mode = "mode=driving";
            mMode = 0 ;
        }
        /*
        else if(rbBiCycling.isChecked()){
            mode = "mode=bicycling";
            mMode = 1;
        }else if(rbWalking.isChecked()){
            mode = "mode=walking";
            mMode = 2;
        }
        */

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(12);

                // Changing the color polyline according to the mode
                if(mMode==MODE_DRIVING)
                    lineOptions.color(Color.BLUE);

                /*
                else if(mMode==MODE_BICYCLING)
                    lineOptions.color(Color.RED);
                else if(mMode==MODE_WALKING)
                    lineOptions.color(Color.MAGENTA);
                */
            }

            /*
            if(result.size()<1){
                Toast.makeText(getActivity().getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
            */

            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);
        }
    }
}
