package com.labs.ssmcabs.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labs.ssmcabs.client.helper.CoordinateAdapter;
import com.sa90.materialarcmenu.ArcMenu;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, View.OnClickListener {

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);
        initializeViews();
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
        setMyStopMarker();


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


        String driver_number = fetchDriverPhoneNo();
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
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        boolean state = sharedPreferences.getBoolean("is_phone_number_visible", false);
        if(state){
            phoneVisibilitySwitch.setText("Phone number visible to driver");
            phoneVisibilitySwitch.setChecked(true);
        }else{
            phoneVisibilitySwitch.setText("Phone number not shown");
            phoneVisibilitySwitch.setChecked(false);
        }
    }


    private void setPhoneVisibilitySwitchState(boolean new_state){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_phone_number_visible", new_state);
        editor.apply();

        DatabaseReference userRef = database.getReference("stops/"+fetchMyStopName()+"/users/"+fetchUserName());
        if(new_state){
            userRef.setValue(fetchUserPhoneNumber());
            phoneVisibilitySwitch.setText("Phone number visible to driver");
            phoneVisibilitySwitch.setChecked(true);
        }else{
            userRef.setValue("0000000000");
            phoneVisibilitySwitch.setText("Phone number not shown");
            phoneVisibilitySwitch.setChecked(false);
        }
    }


    private void setFirebaseLocationListener(){
        DatabaseReference myRef = database.getReference("location_coordinates/"+fetchDriverPhoneNo());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("FB_MAP_UPDATE", "last updated value is: " + dataSnapshot.getValue());
                CoordinateAdapter myAdaptor = dataSnapshot.getValue(CoordinateAdapter.class);
                if(myAdaptor != null)
                    updateDriverMarKer(myAdaptor.getLatitude(), myAdaptor.getLongitude(), myAdaptor.getLast_updated());

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


    private String fetchDriverPhoneNo(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("driver_number", "");
    }

    private String fetchUserName(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("user_name", "");
    }

    private String fetchUserPhoneNumber(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("phone_number", "");
    }


    private String fetchDriverName(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("driver_name", "");
    }

    private Double fetchMyStopLatitude(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return Double.longBitsToDouble(sharedPreferences.getLong("latitude", Double.doubleToLongBits(0)));
    }


    private Double fetchMyStopLongitude(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return Double.longBitsToDouble(sharedPreferences.getLong("longitude", Double.doubleToLongBits(0)));
    }


    private String fetchMyStopName(){
        SharedPreferences sharedPreferences = getSharedPreferences("ssm_cabs_client_v1", MODE_PRIVATE);
        return sharedPreferences.getString("stop_name", "");
    }


    private String convertStopName(String stop_name){
        String[] words = stop_name.split("_");
        String res_name  = "";
        for(String word : words){
            res_name += word.substring(0, 1).toUpperCase()+word.substring(1)+" ";
        }
        return res_name;
    }


    private void setMyStopMarker(){
        LatLng myLatLng = new LatLng(fetchMyStopLatitude(), fetchMyStopLongitude());
        MarkerOptions myMarkerOptions = new MarkerOptions().position(myLatLng).title(convertStopName(fetchMyStopName()))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("stop",150,150)));
        myMarker = mMap.addMarker(myMarkerOptions);

        driverLatLng = new LatLng(0.0, 0.0);
        MarkerOptions myMarkerOptions2 = new MarkerOptions().position(driverLatLng).title(fetchDriverName())
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("my_location",100,186)));
        myMarker = mMap.addMarker(myMarkerOptions2);
    }


    private Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }


    private void updateDriverMarKer(Double latitude, Double longitude, String last_updated){
        last_updated_time = last_updated;
        driverLatLng = new LatLng(latitude, longitude);
        myMarker.setTitle(last_updated);
        myMarker.setPosition(driverLatLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng,  (float)15));
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
                            String driver_number = fetchDriverPhoneNo();
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:"+driver_number));
                            startActivity(intent);
                        }
                    }, 500));
                break;

            case R.id.user_board_logs_fb:
                    arcMenu.toggleMenu();
                    Date date = new Date();
                    SimpleDateFormat date_formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    SimpleDateFormat time_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    DatabaseReference userBoardLogRef = database.getReference("user_board_logs/"+fetchDriverPhoneNo()+"/"+date_formatter.format(date)+"/"+fetchUserName());
                    HashMap<String, String> map = new HashMap<>();
                    map.put("board_time", time_formatter.format(date));
                    map.put("phone_number", fetchUserPhoneNumber());
                    userBoardLogRef.setValue(map);
                    Snackbar.make(view, "You boarded the cab today at :"+time_formatter.format(date), Snackbar.LENGTH_LONG).show();
                break;

            case R.id.last_updated_tab:
                    Snackbar.make(view, "Location last updated at "+last_updated_time, Snackbar.LENGTH_SHORT).show();
                break;

            case R.id.last_updated_text:
                    Snackbar.make(view, "Location last updated at "+last_updated_time, Snackbar.LENGTH_SHORT).show();
                break;
        }
    }
}
