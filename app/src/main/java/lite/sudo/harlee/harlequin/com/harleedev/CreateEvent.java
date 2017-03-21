package lite.sudo.harlee.harlequin.com.harleedev;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;
import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import co.ceryle.radiorealbutton.library.RadioRealButton;
import co.ceryle.radiorealbutton.library.RadioRealButtonGroup;


import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class CreateEvent extends AppCompatActivity {

    private ImageButton eventImage;

    private EditText eventDescription;
    private EditText eventName;
    private EditText eventDate;
    private EditText eventTime;
    private Integer day,month,year,hour,minute;
    private DatabaseReference myDatabase;
    private Button submitEvent;
    private Uri imageUri = null;
    private StorageReference firebaseStorage;
    private ProgressDialog mProgressBar;
    private Uri downloadUrl;
    private Uri cropImageResultUri;
    private static final int galleryRequest = 1;
    private RadioRealButtonGroup paymentGroup;
    private RadioRealButton freeButton, paymentButton;
    private boolean isFree = true;
    private EditText price;
    private LinearLayout priceLayout;
    private GoogleApiClient mGoogleApiClient;
    private MaterialRippleLayout geoRipple;
    private int PLACE_PICKER_REQUEST = 2;
    private Button geoButton;
    private String placeName = null;
    private String placeAdress = null;
    private String placeId = null;
    private String placePhoneNumber = null;
    private LatLng placeLatLng = null;
    private Place selectedPlace = null;
    private LinearLayout geoLayout;
    private LinearLayout whenLayout;
    private Boolean hasAnImage = false;
    private Double placeLatitude,placeLongitude;
    private Calendar mCalendar;
    private  Place mPlace;
    private EditText phoneName,phoneNumber;
    private Button addNumber;
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<String> numbers = new ArrayList<>();
    private String targetCity = "Isernia";

    //TODO settare l'image cropper in modo che rientri perfettamente nella cardView
    //TODO implementare assolutamente onAuthStateListener per fixare database reference

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);


        //Elementi UI
        eventDescription = (EditText) findViewById(R.id.eDescription);

        eventName = (EditText) findViewById(R.id.eName);
        eventImage = (ImageButton) findViewById(R.id.eventImage);
        eventDate = (EditText) findViewById(R.id.eventDate);
        eventTime = (EditText) findViewById(R.id.eventTime);
        submitEvent = (Button)findViewById(R.id.submitButton);
        paymentGroup = (RadioRealButtonGroup)findViewById(R.id.costGroup);
        freeButton = (RadioRealButton)findViewById(R.id.freeRadioButton);
        paymentButton = (RadioRealButton)findViewById(R.id.payRadioButton);
        price = (EditText)findViewById(R.id.priceText);
        priceLayout = (LinearLayout)findViewById(R.id.priceLayout);
        priceLayout.setVisibility(GONE);
        geoButton = (Button)findViewById(R.id.geoButton);
        geoLayout = (LinearLayout)findViewById(R.id.geoLayout);
        whenLayout = (LinearLayout)findViewById(R.id.whenLayout);
        phoneName = (EditText)findViewById(R.id.contact_one);
        phoneNumber = (EditText)findViewById(R.id.number_one);
        addNumber = (Button)findViewById(R.id.add_number);


        eventDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    showLayout();
                }
                if(b){
                    hideLayout();
                }
            }
        });


        //Inizializzazione di Firebase per recuperare la directory in base all'uid
        myDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance().getReference();


        geoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mProgressBar.setMessage("Caricando il servizio di Geolocalizzazione");
                    mProgressBar.show();
                    goToPlacePicker();
                    mProgressBar.dismiss();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();

                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();

                }


            }
        });

        //Setta la barra di caricamento
        mProgressBar = new ProgressDialog(this);

        //fa selezionare un immagine dalla galleria
        eventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryRequest);
            }
        });

        addNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contactName = phoneName.getText().toString().trim();
                String contactNumber = phoneNumber.getText().toString().trim();
                names.add(contactName);
                numbers.add(contactNumber);
                phoneNumber.setText("");
                phoneName.setText("");
            }
        });



        //roba del calendario per la data
        Calendar mcurrentDate = Calendar.getInstance();
        year = mcurrentDate.get(Calendar.YEAR);
        month = mcurrentDate.get(Calendar.MONTH);
        day = mcurrentDate.get(Calendar.DAY_OF_MONTH);
        eventDate.setText("" + day + "/" + (month+1) + "/" + year);
        //roba del calendario per l'orario
        Calendar mcurrentTime = Calendar.getInstance();
        hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mcurrentTime.get(Calendar.MINUTE);
        eventTime.setText(setCorrectTime(hour,minute));

        //radio button per evento a pagamento o gratuito :
        paymentGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                if(position == 0){
                    isFree = true;
                    priceLayout.setVisibility(GONE);
                }
                if(position == 1){
                    isFree = false;
                    priceLayout.setVisibility(VISIBLE);
                }
            }
        });

        //fa scegliere la data ed edita il field corrispettivo
        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog mDatePicker = new DatePickerDialog(CreateEvent.this, new DatePickerDialog.OnDateSetListener() {
                    //quando viene premuto "ok"..
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        year = selectedyear;
                        month = selectedmonth;
                        day = selectedday;

                        eventDate.setText("" + day + "/" + (month+1) + "/" + year);
                        //per calcolare il tempo in millisecondi da passare ad un costruttore
                        mCalendar = Calendar.getInstance();
                        mCalendar.set(datepicker.getYear(),datepicker.getMonth(),datepicker.getDayOfMonth());


                    }
                }, year, month, day);
                mDatePicker.show();
            }


        });

        //fa scegliere l'orario
        eventTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(CreateEvent.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        eventTime.setText( setCorrectTime(selectedHour,selectedMinute));
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Seleziona l'orario");
                mTimePicker.show();

            }
        });

        //SubmitEvent button
        submitEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    writeDynamicAndStaticData();

            }
        });

    }//[END ON CREATE]

    //formattazione dell'orario in modo corretto
    public String setCorrectTime(int hour, int minute){
        String correctTime = hour+":"+minute;
        if (hour < 10 && minute <10){
            correctTime="0"+hour+":"+"0"+minute;
            return correctTime;
        }
        if(hour < 10 && minute >=10){
            correctTime="0"+hour+":"+minute;
            return correctTime;
        }
        if (hour >= 10 && minute < 10){
            correctTime=hour+":"+"0"+minute;
            return correctTime;
        }
        else return correctTime;
    }

    //Ritorna i risultati di : Immagine, Crop immagine e GoogleMaps
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //semplice attività del picker dalla galleria
        if(requestCode==galleryRequest && resultCode==RESULT_OK){
            imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropWindowSize(250,250)
                    //.setAspectRatio(1,1); setta delle impostazioni per il crop
                    //TODO studiare meglio la riga di sopra andando sul gitHub wiki che hai salvato fra i preferiti
                    .start(this);
            hasAnImage = true;
        }

        //esegue solo se ritorna un risultato da imageCropper
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                cropImageResultUri = result.getUri();
                eventImage.setImageURI(cropImageResultUri);
                eventImage.setDrawingCacheEnabled(true);
                eventImage.buildDrawingCache();
                hasAnImage = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        //esegue solo se corrisponde al placepicker
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                mPlace = PlacePicker.getPlace(this,data);
                placeName = mPlace.getName().toString();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //per selezionare da googleMaps
    private void goToPlacePicker() throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {

        Toast.makeText(CreateEvent.this,"Attiva la posizione oppure effettua una ricerca nella GoogleBar",Toast.LENGTH_LONG).show();
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
    }

    private void showLayout(){

        //controlla se deve ripristinare il prezzo oppure no
        if(isFree){
            priceLayout.setVisibility(GONE);
        }
        if(!isFree){
            priceLayout.setVisibility(VISIBLE);
        }
        paymentGroup.setVisibility(VISIBLE);
        geoLayout.setVisibility(VISIBLE);
        whenLayout.setVisibility(VISIBLE);

    }

    private void hideLayout(){
        priceLayout.setVisibility(GONE);
        paymentGroup.setVisibility(GONE);
        geoLayout.setVisibility(GONE);
        whenLayout.setVisibility(GONE);

    }

    private Boolean submitCheck(){
        boolean submitable = true;

        if(mPlace==null){
            Toast.makeText(CreateEvent.this,"Per favore aggiungi un luogo",Toast.LENGTH_LONG).show();
            submitable = false;
        }

        if(placeName.length()==0){
            Toast.makeText(CreateEvent.this,"Per favore aggiungi il nome del luogo",Toast.LENGTH_LONG).show();
            submitable = false;
        }

        if (!hasAnImage){
            Toast.makeText(CreateEvent.this,"Per favore aggiungi un immagine",Toast.LENGTH_LONG).show();
            submitable = false;
        }
        if(eventDate.getText().toString().isEmpty()){
            Toast.makeText(CreateEvent.this,"Per favore aggiungi una data",Toast.LENGTH_LONG).show();
            submitable = false;

        }
        if(eventTime.getText().toString().isEmpty()){
            Toast.makeText(CreateEvent.this,"Per favore aggiungi un orario",Toast.LENGTH_LONG).show();
            submitable = false;

        }
        if(eventName.getText().toString().isEmpty()){
            Toast.makeText(CreateEvent.this,"Per favore aggiungi il nome del'evento",Toast.LENGTH_LONG).show();
            submitable = false;

        }
        if(eventDescription.getText().toString().isEmpty()){
            Toast.makeText(CreateEvent.this,"Per favore aggiungi una descrizione",Toast.LENGTH_LONG).show();
            submitable = false;

        }

        return submitable;


    }

    //tempo in millisecondi un ora prima dell'orario effettivo
    protected long getDateDifference (String targetDate, String eventTime)  {
        //il tempo da sottrarre rispetto all'inizio dell'evento in millisecondi
        long oneHourInMilliseconds = TimeUnit.HOURS.toMillis(1);
        Log.d("HourConversion","1 hour = "+oneHourInMilliseconds);
        long timeInMilliseconds = 0;
        eventTime = eventTime+":00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        try {
            Date endDate = dateFormat.parse(targetDate+" "+eventTime);
            Log.d("END_TIME**","time"+endDate.getTime());
            timeInMilliseconds = endDate.getTime()-oneHourInMilliseconds;
            return timeInMilliseconds;
        } catch (ParseException e) {
            e.printStackTrace();
        }finally {
            return timeInMilliseconds;
        }
    }

    //tempo in millisecondi dell'orario effettivo
    protected Long getTimeMillis(String dayString, String timeString)  {
        Long timeMillis = 0L;
        String completeTime = timeString+":00";
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String completeDate = dayString+" "+completeTime;
        Date endDate = null;

        try {
            endDate = dateFormat.parse(completeDate);
            timeMillis=endDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Log.d("END_TIME**","time"+endDate.getTime());

            return endDate.getTime();


    }

    protected void writeDynamicAndStaticData()  {
        //i counter vanno inizializzati tutti a zero

      //se viene superato il test di upLoad :
      if(submitCheck()){
          mProgressBar.setMessage("Caricamento evento");
          mProgressBar.setTitle("Attendere...");
          mProgressBar.show();
          //qui vengono reperiti tutti gli input
          final Long timeMillis = getTimeMillis(eventDate.getText().toString(),eventTime.getText().toString());

          final String eName = eventName.getText().toString().trim();
          final String pName = placeName;
          final Boolean freeOrNot = isFree;
          final String desc = eventDescription.getText().toString().trim();

          //prova ad eseguire  l'upload dell'immagine:
          final StorageReference eventImagePath = firebaseStorage.child("Event_Images").child(imageUri.getLastPathSegment());
          eventImagePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
              @Override
              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  Float eventPrice = priceSetter(isFree,price);
                  //Upload nello storage effettuato, qui si inizia a scrivere l'evento nel database

                  //genero un ID unico da passare ai metodi
                  DatabaseReference pushReference = FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(targetCity).push();
                  String id = pushReference.getKey();

                  //esegue il caricaricamento di 3 tabelle diverse in 3 zone del database diverse
                  writeDynamicData(timeMillis,eventPrice,eName,pName,freeOrNot,taskSnapshot.getDownloadUrl().toString(),id);
                  writeStaticData(names,numbers,desc,pushReference.getKey());
                  writeMapData(eName,eventPrice,timeMillis,id,mPlace);
                  mProgressBar.dismiss();
                  Toast.makeText(CreateEvent.this, "Evento aggiunto con successo",Toast.LENGTH_SHORT).show();
              }




          });
      }
    }

    protected void writeDynamicData(Long time, Float price,String name,String placeName,Boolean free, String imagePath, String idKey){
        //crea la classe evento
        DynamicData newEvent = new DynamicData( 0,
                                                0,
                                                0,
                                                0,
                                                0,
                                                0,
                                                time,
                                                price,
                                                name,
                                                imagePath,
                                                placeName,
                                                free,
                                                0);

        //di base utilizza come chiave il tempo in millisecondi
       FirebaseDatabase.getInstance().getReference().child("Events").child("Dynamic").child(targetCity).child(idKey).setValue(newEvent);
    }

    protected void writeStaticData(ArrayList names,ArrayList numbers,String desc,String id){
        StaticData data = new StaticData(desc,names,numbers);
        FirebaseDatabase.getInstance().getReference().child("Events").child("Static").child(targetCity).child(id).setValue(data);

    }

    protected void writeMapData(String eName, Float price,Long time, String idKey, Place targetPlace){
        //recupera i dati dall'API di googleMaps
        double lat = targetPlace.getLatLng().latitude;
        double lng = targetPlace.getLatLng().longitude;
        String pName = targetPlace.getName().toString();
        String phone = targetPlace.getPhoneNumber().toString();
        String id = targetPlace.getId();
        //Crea l'oggeto da uplodare
        MapInfo mapInfo = new MapInfo(lat,      //latitudine
                                      lng,     //longitudine
                                      pName,    //nome posto
                                      id,       //Id googleMaps
                                      eName,    //nome evento
                                      price,    //prezzo ingresso
                                      phone,    //numero telefonico
                                      0,
                                      time);
        //esegue l'upload
        //TODO Isernia come child è solo un placeholder
        FirebaseDatabase.getInstance().getReference().child("MapData").child(targetCity).child(idKey).setValue(mapInfo);

    }

    protected Float priceSetter (Boolean bool,EditText priceField){
       Float price = 0.0f;
        if(!isFree){
            price =Float.valueOf(priceField.getText().toString());
            return price;

        }
        return price;
    }

}//[END CreateEvent.class]
