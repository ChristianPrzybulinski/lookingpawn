package unisinos.lookingpawn;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener, GoogleMap.OnMapLongClickListener, ValueEventListener, GoogleMap.OnInfoWindowLongClickListener {

    private static final String TAG = "maps";
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    private Spinner s;
    private EditText titleBox;
    private LatLng curposition;
    private Circle myCircle;
    private DatabaseReference mDatabase;
    private List<markerCustom> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference("doges");
        markers = new ArrayList<>();
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

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
        enableMyLocation();
        setAddButtonDog();
        mDatabase.addValueEventListener(this);
        mDatabase.addListenerForSingleValueEvent(this);
    }

    private void setAddButtonDog() {
        Button clickButton = (Button) findViewById(R.id.btn_adddog);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "add doge", Toast.LENGTH_LONG).show();
                dialogShow();
            }
        });

    }


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, this);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Current Location change to: ["+location.getLatitude() + "] ["+location.getLongitude()+"]", Toast.LENGTH_SHORT).show();
        curposition = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curposition, 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        myCircle = mMap.addCircle(new CircleOptions().center(curposition).radius(10.0)
                .fillColor(0x01060012).strokeColor(0x01060013).strokeWidth(3));

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

    @Override
    public void onMapLongClick(LatLng latLng) {
        dialogShowCustom(latLng);
    }

    private void dialogShowCustom(final LatLng latLng) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Do you want to add a lost dog?");
            builder.setTitle("Adding a dog");

            //This will not allow to close dialogbox until user selects an option
            builder.setCancelable(false);
            //aqui

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);

            // Add a TextView here for the "Title" label, as noted in the comments
            titleBox = new EditText(this);
            titleBox.setHint("Dog Description");
            titleBox.setMaxLines(3);
            layout.addView(titleBox);
            layout.setHorizontalScrollBarEnabled(false);

            s = new Spinner(this);
            String array_spinner[] = {
                    "Vira-lata", "Pastor alemão", "indefinido"
            };
            ArrayAdapter adapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item, array_spinner);
            s.setAdapter(adapter);
            layout.addView(s);


            builder.setView(layout);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(MapsActivity.this, "positive button", Toast.LENGTH_SHORT).show();
                    if (TextUtils.isEmpty(titleBox.getText())) {
                        titleBox.setError("Description is required");
                    } else {
                        thedog dog = new thedog();
                        dog.setBreed(s.getSelectedItem().toString());
                        dog.setLatitude(latLng.latitude);
                        dog.setLongitude(latLng.longitude);
                        dog.setDescription(titleBox.getText().toString());
                        dog.setuID(mDatabase.push().getKey());
                        mDatabase.child(dog.getuID()).setValue(dog);
                    }

                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //  Action for 'NO' Button
                    Toast.makeText(MapsActivity.this, "negative button", Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                }
            });

            //Creating dialog box
            AlertDialog alert = builder.create();
            //Setting the title manually
            //alert.setTitle("AlertDialogExample");
            alert.show();
        }


    private void dialogShow(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you want to add a lost dog?");
        builder.setTitle("Adding a dog");

        //This will not allow to close dialogbox until user selects an option
        builder.setCancelable(false);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add a TextView here for the "Title" label, as noted in the comments
         titleBox = new EditText(this);
        titleBox.setHint("Dog Description");
        titleBox.setMaxLines(3);
        layout.addView(titleBox);
        layout.setHorizontalScrollBarEnabled(false);

        s = new Spinner(this);
        String array_spinner[] = {
            "Vira-lata", "Pastor alemão", "indefinido"
        };
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, array_spinner);
        s.setAdapter(adapter);
        layout.addView(s);


        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Toast.makeText(MapsActivity.this, "positive button", Toast.LENGTH_SHORT).show();
                if (TextUtils.isEmpty(titleBox.getText())) {
                    titleBox.setError("Description is required");
                } else {
                    thedog dog = new thedog();
                    dog.setBreed(s.getSelectedItem().toString());
                    dog.setLatitude(curposition.latitude);
                    dog.setLongitude(curposition.longitude);
                    dog.setDescription(titleBox.getText().toString());
                    mDatabase.push().setValue(dog);
                }

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //  Action for 'NO' Button
                Toast.makeText(MapsActivity.this, "negative button", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        //alert.setTitle("AlertDialogExample");
        alert.show();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        //iterating through all the values in database
        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            thedog post = postSnapshot.getValue(thedog.class);

            LatLng position = new LatLng(post.getLatitude(), post.getLongitude());
            CircleOptions circleOptions = new CircleOptions().center(position).radius(25.0)
                    .fillColor(0x44ff0000).strokeColor(0xffff0000).strokeWidth(2);

            MarkerOptions marker = new MarkerOptions()
                    .position(position)
                    .title(post.getBreed())
                    .snippet("Description: "+post.getDescription())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            Marker mk = mMap.addMarker(marker);
            Circle c =  mMap.addCircle(circleOptions);

            markerCustom mc = new markerCustom(postSnapshot.getKey(), mk, c);
            markers.add(mc);

        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
    }

    @Override
    public void onInfoWindowLongClick(final Marker marker) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Do you want to delete this dog marker?");
        builder.setTitle("Deleting a dog");

        //This will not allow to close dialogbox until user selects an option
        builder.setCancelable(false);
        //aqui

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                for(int i = 0; i<markers.size(); i++){
                   if(markers.get(i).getMarker().getId().equals(marker.getId())) {
                       mDatabase.child(markers.get(i).getId()).removeValue();
                       markers.get(i).getMarker().remove();
                       markers.get(i).getCircle().remove();
                       markers.remove(i);
                       break;
                   }
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //  Action for 'NO' Button
                Toast.makeText(MapsActivity.this, "negative button", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        //alert.setTitle("AlertDialogExample");
        alert.show();
    }
}
