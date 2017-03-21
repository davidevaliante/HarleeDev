package lite.sudo.harlee.harlequin.com.harleedev;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MyPicker extends AppCompatActivity {
    int PLACE_PICKER_REQUEST = 1;
    private Button myPicker ;
    private TextView mLat,mLong;
    private DatabaseReference myReference;
    private String PlaceHolderId = "This is some Value";
    private String eventName ="Lorem Ipsum";
    private Float eventPrice = 10.0f;
    private Integer likes = 100;
    private Long timeMillis = 1000000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_picker);

        myPicker = (Button)findViewById(R.id.pickerone);
        mLat = (TextView)findViewById(R.id.latValue);
        mLong = (TextView)findViewById(R.id.longValue);

        myReference = FirebaseDatabase.getInstance().getReference();
        myPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    goToPlacePicker();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);
                String toastMsg = String.format("Place: %s", place.getName());


                //lat e lan vanno presi così oppure i markers non vengono visualizzati
                MapInfo mapInfo = new MapInfo(place.getLatLng().latitude,       //latitudine
                                              place.getLatLng().longitude,      //longitudine
                                              place.getName().toString(),       //nome luogo
                                              place.getId(),                    //id google map luogo
                                              eventName,                        //nome evento
                                              eventPrice,                       //prezzo evento
                                              place.getPhoneNumber().toString(),//numero telefonico
                                              likes,                            //numero di like
                                              timeMillis);                      //tempo in millisecondi
                myReference.child(PlaceHolderId).push().setValue(mapInfo);
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void goToPlacePicker() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
    }



}
