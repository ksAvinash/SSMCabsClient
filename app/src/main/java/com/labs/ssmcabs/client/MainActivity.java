package com.labs.ssmcabs.client;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labs.ssmcabs.client.helper.CoordinateAdapter;
import com.labs.ssmcabs.client.helper.HttpHelper;
import com.labs.ssmcabs.client.helper.PolyLineTaskLoadedCallback;
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;
import com.sa90.materialarcmenu.ArcMenu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, View.OnClickListener, PolyLineTaskLoadedCallback {

    private GoogleMap mMap;
    private LatLng driverLatLng;
    private Marker myMarker;
    private String last_updated_time = "";
    private TextView last_updated_text;
    private RelativeLayout last_updated_tab;
    boolean doubleBackToExitPressedOnce = false;
    FirebaseDatabase database;
    Switch phoneVisibilitySwitch;
    ArcMenu arcMenu;
    private Polyline currentPolyline;
    boolean isPathSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        initializeViews();

    }

    private void setUpPathToMyStop(LatLng updated_location){
        if(!isPathSet){
            new HttpHelper.FetchMapDirectionsTask(MainActivity.this).execute(new LatLng(SharedPreferenceHelper.fetchMyStopLatitude(MainActivity.this),
                    SharedPreferenceHelper.fetchMyStopLongitude(MainActivity.this)), updated_location);
            isPathSet = true;
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        isPathSet = false;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        isPathSet = false;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.getBackStackEntryCount() > 0)
            super.onBackPressed();
        else {
            if (doubleBackToExitPressedOnce)
                super.onBackPressed();

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Double tap BACK to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 1000);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_feedback) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setupMarkers();


        try {
            // Customise the styling of the base map using a JSON object defined
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.maps_style));

            if (!success) {
                Log.e("FB_MAPS", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("FB_MAPS", "Can't find style. Error: ", e);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                displayLastUpdated();
            }
        }).start();


        String driver_number = SharedPreferenceHelper.fetchDriverPhoneNo(MainActivity.this);
        if(driver_number.equals("")){
            Log.e("SP_SAVED", "saved shared preference driver_number: " + driver_number);
            finish();
        }else{
            Log.d("SP_SAVED", "saved shared preference driver_number: " + driver_number);
            setFirebaseLocationListener();
        }
    }




    private void initializeViews(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        phoneVisibilitySwitch = findViewById(R.id.phoneVisibilitySwitch);
        database = FirebaseDatabase.getInstance();
        arcMenu = findViewById(R.id.arcMenu);

        FloatingActionButton call_driver_fb = findViewById(R.id.call_driver_fb);
        call_driver_fb.setOnClickListener(this);

        FloatingActionButton user_board_logs_fb = findViewById(R.id.user_board_logs_fb);
        user_board_logs_fb.setOnClickListener(this);

        FloatingActionButton update_profile_fb = findViewById(R.id.update_profile_fb);
        update_profile_fb.setOnClickListener(this);

        last_updated_tab = findViewById(R.id.last_updated_tab);
        last_updated_tab.setOnClickListener(this);

        last_updated_text = findViewById(R.id.last_updated_text);
        last_updated_text.setOnClickListener(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);


        setPhoneVisibilitySwitchState();
        phoneVisibilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    Snackbar.make(buttonView, "Showing phone number to driver", Snackbar.LENGTH_SHORT).show();
                    setPhoneVisibilitySwitchState(true);
                }
                else{
                    Snackbar.make(buttonView, "Phone number not shown to driver ", Snackbar.LENGTH_SHORT).show();
                    setPhoneVisibilitySwitchState(false);
                }
            }
        });
    }



    private void setPhoneVisibilitySwitchState(){
        boolean state = SharedPreferenceHelper.fetchPhoneNumberVisibilityStatus(MainActivity.this);
        if(state){
            phoneVisibilitySwitch.setText("Phone number visible to driver");
            phoneVisibilitySwitch.setChecked(true);
        }else{
            phoneVisibilitySwitch.setText("Phone number not shown");
            phoneVisibilitySwitch.setChecked(false);
        }
    }


    private void setPhoneVisibilitySwitchState(boolean new_state){
        SharedPreferenceHelper.savePhoneNumberVisibilityStatus(MainActivity.this, new_state);

        DatabaseReference userRef = database.getReference("stops/"+SharedPreferenceHelper.fetchStopName(MainActivity.this)
                +"/users/"+SharedPreferenceHelper.fetchUserName(MainActivity.this));
        if(new_state){
            userRef.setValue(SharedPreferenceHelper.fetchUserPhoneNumber(MainActivity.this));
            phoneVisibilitySwitch.setText("Phone number visible to driver");
            phoneVisibilitySwitch.setChecked(true);
        }else{
            userRef.setValue("0000000000");
            phoneVisibilitySwitch.setText("Phone number not shown");
            phoneVisibilitySwitch.setChecked(false);
        }
    }


    private void setFirebaseLocationListener(){
        DatabaseReference myRef = database.getReference("location_coordinates/"+SharedPreferenceHelper.fetchDriverPhoneNo(MainActivity.this));
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("FB_MAP_UPDATE", "last updated value is: " + dataSnapshot.getValue());
                CoordinateAdapter myAdaptor = dataSnapshot.getValue(CoordinateAdapter.class);
                if(myAdaptor != null){
                    updateDriverMarKer(myAdaptor.getLatitude(), myAdaptor.getLongitude(), myAdaptor.getLast_updated(), myAdaptor.getBearing());
                    setUpPathToMyStop(new LatLng(myAdaptor.getLatitude(), myAdaptor.getLongitude()));
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("FB_MAP_UPDATE", "Failed to read value.", error.toException());
            }
        });
    }



    private void displayLastUpdated(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            Date last_updated_date = formatter.parse(last_updated_time);
            Date current_date = new Date();

            long difference = current_date.getTime() - last_updated_date.getTime();
            final double diffInMinutes = difference / ((double) 1000 * 60);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(diffInMinutes < 2){
                        last_updated_tab.setBackgroundColor(Color.parseColor("#00c853"));
                        last_updated_text.setText("updated less than 2 mins ago");
                    }else if(diffInMinutes < 5){
                        last_updated_tab.setBackgroundColor(Color.parseColor("#ffb300"));
                        last_updated_text.setText("updated less than 5 mins ago");
                    }else if(diffInMinutes < 15){
                        last_updated_tab.setBackgroundColor(Color.parseColor("#f4511e"));
                        last_updated_text.setText("updated less than 15 mins ago");
                    }else{
                        last_updated_tab.setBackgroundColor(Color.parseColor("#263238"));
                        last_updated_text.setText("updated more than 15 mins ago");
                    }
                }
            });

        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        displayLastUpdated();
    }


    private String convertStopName(String stop_name){
        String[] words = stop_name.split("_");
        if(words.length == 1)
            return words[0];


        String res_name  = "";
        for(String word : words){
            res_name += word.substring(0, 1).toUpperCase()+word.substring(1)+" ";
        }
        return res_name;
    }


    private void setupMarkers(){
        LatLng myLatLng = new LatLng(SharedPreferenceHelper.fetchMyStopLatitude(MainActivity.this), SharedPreferenceHelper.fetchMyStopLongitude(MainActivity.this));

        MarkerOptions myMarkerOptions = new MarkerOptions().position(myLatLng).title(convertStopName(SharedPreferenceHelper.fetchStopName(MainActivity.this)))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("stop",120,120)));
        myMarker = mMap.addMarker(myMarkerOptions);
        myMarker.setSnippet("MY STOP");

        driverLatLng = new LatLng(0.0, 0.0);
        MarkerOptions myMarkerOptions2 = new MarkerOptions().position(driverLatLng).title(SharedPreferenceHelper.fetchDriverName(MainActivity.this))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("my_location",90,168)));

        myMarker = mMap.addMarker(myMarkerOptions2);
        myMarker.setTitle(SharedPreferenceHelper.fetchDriverName(MainActivity.this));
        myMarker.setSnippet("Vehicle Number : "+SharedPreferenceHelper.fetchDriverVehicleNumber(MainActivity.this));

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getApplicationContext());
                title.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getApplicationContext());
                snippet.setTextColor(Color.BLACK);
                snippet.setText(marker.getSnippet());
                snippet.setGravity(Gravity.CENTER);

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });

    }


    private Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }


    private void updateDriverMarKer(Double latitude, Double longitude, String last_updated, Float bearing){
        last_updated_time = last_updated;
        driverLatLng = new LatLng(latitude, longitude);

        myMarker.setPosition(driverLatLng);
        myMarker.setRotation(bearing);
        myMarker.setAnchor(0.5f, 0.5f);
        myMarker.setFlat(true);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(driverLatLng)
                .zoom(16)
                .bearing(bearing)
                .tilt(0)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.update_profile_fb:
                    arcMenu.toggleMenu();
                break;


            case R.id.call_driver_fb:
                    arcMenu.toggleMenu();
                    Snackbar.make(view, "Calling driver..", Snackbar.LENGTH_SHORT).show();
                    System.out.println(new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String driver_number = SharedPreferenceHelper.fetchDriverPhoneNo(MainActivity.this);
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:"+driver_number));
                            startActivity(intent);
                        }
                    }, 500));
                break;

            case R.id.user_board_logs_fb:
                    arcMenu.toggleMenu();
                    updateBoardedTime();
                break;

            case R.id.last_updated_tab:
                    Snackbar.make(view, "Location last updated at "+last_updated_time, Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.last_updated_text:
                    Snackbar.make(view, "Location last updated at "+last_updated_time, Snackbar.LENGTH_SHORT).show();
                break;
        }
    }


    private void updateBoardedTime(){
        Snackbar.make(findViewById(android.R.id.content), "Your cab board time has been recorded", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        final Date date = new Date();
        final SimpleDateFormat month_formatter = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        final SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd a", Locale.getDefault());


        final DatabaseReference userBoardLogRef = database.getReference("user_board_logs/"+SharedPreferenceHelper.fetchUserPhoneNumber(MainActivity.this)+"/"+
                month_formatter.format(date)+"/"+date_formatter.format(date)+"/");
        userBoardLogRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userBoardLogRef.removeEventListener(this);

                if(dataSnapshot.getValue() == null){
                    SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a", Locale.getDefault());
                    HashMap<String, String> map = new HashMap<>();
                    map.put("board_time", time_formatter.format(date));
                    userBoardLogRef.setValue(map);
                    SharedPreferenceHelper.saveLastBoardTime(MainActivity.this, time_formatter.format(date));
                    Log.i("BOARD_TIME", date_formatter.format(date));
                }else{
                    Log.i("BOARD_TIME", "Cab already boarded today");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                userBoardLogRef.removeEventListener(this);
            }
        });




    }

    @Override
    public void onTaskDone(Object... values) {
        Log.i("MAP_API_MAIN", "onTaskDone interface invoked");
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}
