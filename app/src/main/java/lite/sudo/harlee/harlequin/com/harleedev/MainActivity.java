package lite.sudo.harlee.harlequin.com.harleedev;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String TAG ="AuthListener";
    private Button loginBtn,regBtn;
    private EditText email,pass;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginBtn = (Button)findViewById(R.id.loginBtn);
        regBtn = (Button)findViewById(R.id.regBtn);
        email = (EditText)findViewById(R.id.emailField);
        pass = (EditText)findViewById(R.id.passwordField);

        //listener autenticazione
        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Intent toConsole = new Intent(MainActivity.this,DevConsole.class);
                    toConsole.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(toConsole);
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateDataAndRegister(email.getText().toString(),pass.getText().toString());
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateDataAndLogin(email.getText().toString(),pass.getText().toString());
            }
        });


    }//fine OnCreate

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthStateListener);
    }

    private void validateDataAndRegister(String devMail,String devPass){
        if (devMail.length() != 0 && devPass.length() != 0){
            mAuth.createUserWithEmailAndPassword(devMail, devPass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Problemi con la registrazione, contatta Davide",
                                        Toast.LENGTH_SHORT).show();
                            }

                           if(task.isSuccessful()){
                               Intent toConsole = new Intent(MainActivity.this,DevConsole.class);
                               startActivity(toConsole);
                           }
                        }
                    });

        }else{
            Toast.makeText(MainActivity.this, "Riempi tutti i campi con informazioni valide", Toast.LENGTH_SHORT)
                 .show();
        }

    }

    private void validateDataAndLogin(String devmail,String devpass){


        mAuth.signInWithEmailAndPassword(devmail, devpass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(MainActivity.this, "Login Fallito, contatta Davide",
                                    Toast.LENGTH_SHORT).show();
                        }
                        if(task.isSuccessful()){
                            Intent toConsole = new Intent(MainActivity.this,DevConsole.class);
                            startActivity(toConsole);
                        }
                    }
                });
    }
}//fine MainActivity
