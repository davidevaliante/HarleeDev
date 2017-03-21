package lite.sudo.harlee.harlequin.com.harleedev;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapTest extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private Float isLat = 41.596545f;
    private Float isLon = 14.233357f;
    private DatabaseReference myReference;
    private ValueEventListener mapInfo;
    String PlaceHolderId = "This is some Value";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_test);

        myReference = FirebaseDatabase.getInstance().getReference().child(PlaceHolderId);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        addMarkers(googleMap,myReference);
        LatLng currentCity = new LatLng(isLat,isLon);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentCity));
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(currentCity,15);
        googleMap.animateCamera(location);

    }

    private void addMarkers(final GoogleMap googleMap, final DatabaseReference myReference){
        ValueEventListener mapListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    MapInfo info = postSnapshot.getValue(MapInfo.class);
                    double myLat = info.getLat();
                    double myLon = info.getLng();
                    LatLng latLng = new LatLng(myLat,myLon);
                    googleMap.addMarker(new MarkerOptions().position(latLng).title(postSnapshot.getKey()));

                    //debug
                    Log.d("MAP_INFO : ", "lat:"+myLat+" lon : "+myLon);
                    Log.d("MAP_INFO : ", "title : "+googleMap.addMarker(new MarkerOptions().position(latLng)
                            .title(postSnapshot
                             .getKey()))
                            .getTitle());
                }
                myReference.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        myReference.addValueEventListener(mapListener);
    }
}
