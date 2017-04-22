package com.example.grim.tutmap;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.LogRecord;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Grim on 26.12.2015.
 */
public class Map extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private SupportMapFragment mapFragment;
    GoogleMap map;
    private final String TAG = "myLogs";
    private Button myLocationBtn, myNavigationBtn, myMapTypeBtn, myMorphNaviBtn, myPlacePickerBtn;
    private Context mAppContext;
    private String provider;
    private Location location;
    public double latid, longi;
    private String status;
    private String mType;
    private AutoCompleteTextView mySearcher;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private static final String LOG_TAG = "MAP in ACTION";
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(49.9944422, 36.2368201), new LatLng(49.9944422, 36.2368201));
    int PLACE_PICKER_REQUEST = 1;
    private Animation arrowAnimationRight;
    private Animation arrowAnimationLeft;
    Handler h;
    public String [] placesArray;
    public Marker markerForAuto, markerFor;
    public LatLng autoPlace;


    @TargetApi(11)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }
        markerForAuto = (Marker) getLastCustomNonConfigurationInstance();
        mGoogleApiClient = new GoogleApiClient
                .Builder(Map.this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //Отключение отображения родного компаса, инициализация переменных.
        map.getUiSettings().setCompassEnabled(false);
        myLocationBtn = (Button) findViewById(R.id.my_map_btn_location);
        myNavigationBtn = (Button) findViewById(R.id.my_map_bnt_novig);
        myMapTypeBtn = (Button) findViewById(R.id.my_map_btn_mType);
        myPlacePickerBtn = (Button)findViewById(R.id.my_map_btn_placepicker);
        myMorphNaviBtn = (Button) findViewById(R.id.my_map_edt_btn_navi);
        mAppContext = getApplicationContext();
        mySearcher = (AutoCompleteTextView) findViewById(R.id.my_map_edt_search);
        mySearcher.setThreshold(3);
        mySearcher.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_MOUNTAIN_VIEW, null);
        mySearcher.setAdapter(mPlaceArrayAdapter);
        placesArray = getResources().getStringArray(R.array.places);
                status = "off";
        h = new Handler();

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        provider = LocationManager.NETWORK_PROVIDER;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location loc) {
                latid = loc.getLatitude();
                longi = loc.getLongitude();

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
                Toast toast = Toast.makeText(getApplicationContext(), "Подключен" + provider, Toast.LENGTH_LONG);
                toast.show();
            }

            public void onProviderDisabled(String provider) {
                Toast toast = (Toast.makeText(getApplicationContext(), "Связь отсутствует", Toast.LENGTH_LONG));
                toast.show();
            }
        };

        //Адаптируем для работы под разные версии Android
        locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            location = locationManager.getLastKnownLocation(provider);
            latid = location.getLatitude();
            longi = location.getLongitude();
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getApplicationContext(), "Отсутствует Internet соединение, включите Wifi или мобильный интернет", Toast.LENGTH_LONG);
            toast.show();
        }
        // Переключение на GPS при условии, если сеть не дает результатов гео-данных
        if (location == null) {
            provider = LocationManager.GPS_PROVIDER;
            try {
                location = locationManager.getLastKnownLocation(provider);
                latid = location.getLatitude();
                longi = location.getLongitude();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Отсутствует GPS или связь со спутниками, возможны сбои изза плохих погодных условий или вы находитесь в помещении", Toast.LENGTH_LONG);
                toast.show();
            }
        }



        //Нахождение местоположения устройства, установка маркера.
        final LatLng STARTER = new LatLng(latid, longi);
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        myLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new
                        LatLng(location.getLatitude(),
                        location.getLongitude()), 15));
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.human);
                Marker marker = map.addMarker(new MarkerOptions().position(STARTER).title("You are here").icon(icon));

            }
        });
        mySearcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                myMorphNaviBtn.setBackgroundResource(R.drawable.delete_btn);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mySearcher.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    Retrofit.getPlaces(location.getLatitude(),
                        location.getLongitude(),
                        placesArray[actionId].toLowerCase().replace("-", "_").replace(" ", "_"),
                        new Callback
                        <List <Placer>>() {
                            // Не заходит в успех, потому, что возвращается объект (java.lang.Object), хотя в интерфейсе, в методах я явно указывал, что хочу получить список <List <Placer>>
                    @Override
                    public void success(List <Placer> places, retrofit.client.Response response) {
                        for(int i = 0; places.size() > i; i++){
                        String name = places.get(0).getName();
                    map.addMarker(new MarkerOptions()
                    .title(places.get(i).getName().toString())
                    .position(new LatLng(places.get(i).getLatitude(), places.get(i).getLongitude()))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ptype_icon)));
                    }
                        Log.i(TAG, places.toString());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.i(TAG, error.toString());
                        Log.i(TAG, "");
                    }
                });
                }
                return false;
            }
        });

        // Вероятно была возможность сделать переключение функционала и иконки кнопки проще....
        myMorphNaviBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String whatWeAreLookingFor = mySearcher.getText().toString();
                if (!whatWeAreLookingFor.equals("")) {
                    mySearcher.setText(null);
                    if (status.equals("off")) {
                        myMorphNaviBtn.setBackgroundResource(R.drawable.right_navi_arrow);
                    } else if (status.equals("on")) {
                        myMorphNaviBtn.setBackgroundResource(R.drawable.left_navi_arrow);
                    }
                } else {
                    if (status.equals("off")) {
                        myMorphNaviBtn.startAnimation(arrowAnimationRight);

                    } else if (status.equals("on")) {
                        myMorphNaviBtn.startAnimation(arrowAnimationLeft);
                    }

                }
            }


        });
        arrowAnimationRight = AnimationUtils.loadAnimation(this, R.anim.arrow_animation);
        arrowAnimationRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                myLocationBtn.setVisibility(View.VISIBLE);
                myMapTypeBtn.setVisibility(View.VISIBLE);
                myNavigationBtn.setVisibility(View.VISIBLE);
                myPlacePickerBtn.setVisibility(View.VISIBLE);
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        status = "on";
                        myMorphNaviBtn.setBackgroundResource(R.drawable.right_navi_arrow);
                    }
                }, 200);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        arrowAnimationLeft = AnimationUtils.loadAnimation(this, R.anim.arrow_animation_back);
        arrowAnimationLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                myLocationBtn.setVisibility(View.GONE);
                myMapTypeBtn.setVisibility(View.GONE);
                myNavigationBtn.setVisibility(View.GONE);
                myPlacePickerBtn.setVisibility(View.GONE);
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myMorphNaviBtn.setBackgroundResource(R.drawable.left_navi_arrow);
                        status = "off";
                    }
                }, 200);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // Переключение режимов карты
        mType = "normal";
        myMapTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mType.equals("normal")) {
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Режим карты: Гибрид", Toast.LENGTH_SHORT);
                    toast.show();
                    mType = "hibrid";
                } else if (mType.equals("hibrid")) {
                    map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Режим карты: Спутник", Toast.LENGTH_SHORT);
                    toast.show();
                    mType = "satt";
                } else if (mType.equals("satt")) {
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Режим карты: Стандарт", Toast.LENGTH_SHORT);
                    toast.show();
                    mType = "normal";
                }
            }
        });
        // Нормализация вида - выравнивание камеры по северу.
        myNavigationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(STARTER)
                        .zoom(map.getCameraPosition().zoom)
                        .bearing(0)
                        .tilt(map.getCameraPosition().tilt)
                        .build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                map.animateCamera(cameraUpdate);
            }
        });
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mySearcher.clearFocus();
            }
        });
        //PlacePicker на клавише
        myPlacePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(Map.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {

                }
            }
        });
    }



    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID:" + item.placeId);
            PolylineOptions line = new PolylineOptions();
            line.width(4f).color(R.color.green);
//            Polyline mPoints = ;
//            LatLngBounds.Builder latlngBuilder = new LatLngBounds.Builder();
//            for (int i = 0; i < mPpo)

        }
    };

    public void onPickButtonClick(View v) {
        try {
            PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
            Intent intent = intentBuilder.build(this);
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {

        } catch (GooglePlayServicesNotAvailableException e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            final Place place = PlacePicker.getPlace(data, this);
            final CharSequence name = place.getName();
            final CharSequence adress = place.getAddress();
            String attr = PlacePicker.getAttributions(data);
            if (attr == null) {
                attr = "";
            }
            markerFor = map.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            autoPlace = place.getLatLng();
            CharSequence attributions = places.getAttributions();
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.mmarker);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            markerForAuto = map.addMarker(new MarkerOptions().position(place.getLatLng()).title(place.getName().toString()));
//            (Html.fromHtml(place.getAddress() + ""));

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return markerForAuto;
    }
    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

//    private class GetPlace extends AsyncTask<Void, Void, ArrayList<Placer> >{
//        private ProgressDialog dialog;
//        private Context context;
//        private String places;
//
//        public GetPlace(Context context, String places) {
//            this.context = context;
//            this.places = places;
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<Placer> result) {
//            super.onPostExecute(result);
//            if(dialog.isShowing()){
//                dialog.dismiss();
//            }
//            for(int i = 0; i < result.size(); i++){
//                map.addMarker(new MarkerOptions()
//                        .title(result.get(i).getName().toString())
//                        .position(
//                                new LatLng(result.get(i).getLatitude(), result
//                                        .get(i).getLongitude()))
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ptype_icon)));
//            }
//        }
//
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        dialog = new ProgressDialog(context);
//        dialog.setCancelable(false);
//        dialog.setMessage("Loading...");
//        dialog.setIndeterminate(true);
//        dialog.show();
//    }
//
//    @Override
//    protected ArrayList<Placer> doInBackground(Void... params) {
//        PlaceService placeService = new PlaceService("AIzaSyBEchazmtBPqERNwAzqOaoDwwOegjIpRAA");
//        ArrayList<Placer> findPlaces = placeService.findPlaces(location.getLatitude(), location.getLongitude(), places);
//        for (int i = 0; i<findPlaces.size(); i++ ){
//            Placer placeDetail = findPlaces.get(i);
//        }
//        return findPlaces;
//    }
//}


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public interface RouteApi {
        @GET("/maps/api/direction/json")
        RouteResponse getRoute(@Query(value = "origin", encodeValue = false)
                               LatLng position, @Query(value = "destination", encodeValue = false)
        LatLng destination, @Query(value = "sensor") boolean sensor, @Query("language") String language );
    }

//        RestAdapter rA = new RestAdapter.Builder()
//                .setEndpoint("http://maps.googleapis.com")
//                .setLogLevel(RestAdapter.LogLevel.FULL)
//                .build();
//        RouteApi routService = rA.create(RouteApi.class);
//        RouteResponse routeResponse = routService.getRoute(new LatLng(latid, longi), new LatLng(autoPlace.latitude, autoPlace.longitude), true, "ru");

}
