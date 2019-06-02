package com.example.ex5_v1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Launcher extends AppCompatActivity {

    public static final String IS_REGISTERED = "isRegistered";
    public static final String USERNAME = "username";
    public SharedPreferences SharedPreference;
    public SharedPreferences.Editor SharedPrefranceEditor;
    public FirebaseFirestore fireBase;
    public EditText username;
    public Button skipButton;
    public Button  regButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        regButton = (Button) findViewById(R.id.buttonRegisterUser);
        skipButton = (Button) findViewById(R.id.buttonSkip);
        username = (EditText) findViewById(R.id.editText_UserName);
        SharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPrefranceEditor = SharedPreference.edit();

        if (SharedPreference.getBoolean(IS_REGISTERED, false))
        {
            Intent navigate = new Intent(Launcher.this, MainActivity.class);
            navigate.putExtra(MainActivity.NAVIGATION, MainActivity.FIRST_LAUNCH);
            navigate.putExtra(MainActivity.USERNAME, SharedPreference.getString(USERNAME, ""));
            startActivity(navigate);
            finish();
        }

        FirebaseApp.initializeApp(Launcher.this);
        fireBase = FirebaseFirestore.getInstance();
        new RemoteDB().execute();
        regButton.setVisibility(View.INVISIBLE);
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                return;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() == 0)
                    regButton.setVisibility(View.INVISIBLE);
                else
                    regButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                return;
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent skipIntent = new Intent(Launcher.this, MainActivity.class);
                skipIntent.putExtra(MainActivity.NAVIGATION, MainActivity.SKIPPED);
                startActivity(skipIntent);
                finish();
            }
        });

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefranceEditor.putBoolean(IS_REGISTERED, true);
                SharedPrefranceEditor.putString(USERNAME, username.getText().toString());
                SharedPrefranceEditor.apply();
                new updateUser().execute(username.getText().toString());
                Intent registerIntent = new Intent(Launcher.this, MainActivity.class);
                registerIntent.putExtra(MainActivity.NAVIGATION, MainActivity.REGISTERED);
                registerIntent.putExtra(MainActivity.USERNAME, username.getText().toString());
                startActivity(registerIntent);
                finish();
            }
        });
    }

    public void DBRegToRem(String user)
    {
        DocumentReference washingtonRef = fireBase.collection(MyAdapter.COLLECTION).
                document(MainActivity.FIRESTORE_USER_ID);

        washingtonRef
                .update(MainActivity.KYE_USER_FIELD, user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("", "Error in DBRegToRem", e);
                    }
                });
    }
    public class RemoteDB extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            nameChecker();
            return null;
        }
    }

    public void nameChecker()
    {
        DocumentReference docRef = fireBase.collection(MyAdapter.COLLECTION).
                document(MainActivity.FIRESTORE_USER_ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String user = document.getData().get(MainActivity.KYE_USER_FIELD) + "";
                        redirect(user);
                    } else {
                        Log.d("", "No such document");
                    }
                } else {
                    Log.d("", "get failed with ", task.getException());
                }
            }
        });
    }

    public void redirect(String user)
    {
        if(!user.equals(""))
        {
            SharedPrefranceEditor.putBoolean(IS_REGISTERED, true);
            SharedPrefranceEditor.putString(USERNAME, user);
            SharedPrefranceEditor.apply();
            Intent registerIntent = new Intent(Launcher.this, MainActivity.class);
            registerIntent.putExtra(MainActivity.NAVIGATION, MainActivity.KYE_USER_FIELD);
            registerIntent.putExtra(MainActivity.USERNAME, user);
            startActivity(registerIntent);
            finish();
        }
    }


    private class updateUser extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... strings) {
            DBRegToRem(strings[0]);
            return null;
        }
    }



}
