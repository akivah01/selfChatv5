package com.example.ex5_v1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Map;
import java.util.ArrayList;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity implements MyAdapter.LongClick {

    public EditText input;
    public Button button;
    public MyAdapter adapter;
    public RecyclerView recyclerView;
    public SharedPreferences SharedPreference;
    public SharedPreferences.Editor SharedPrefranceEditor;
    public FirebaseFirestore fireBase;
    public CollectionReference collectionReference;
    public TextView helloMsg;
    public String usernameMsg;

    public static final String FIRESTORE_USER_ID = "kVcaWDmBDAmIrpbBNpgY";
    public static final String WROTE_MSG = "wrote_message";
    public static final String KYE_USER_FIELD = "User";
    public static final String NAVIGATION = "navigation_code";
    public static final String REGISTERED = "register";
    public static final String DELETE = "delete";
    public static final String FIRST_LAUNCH = "first_launch_NOT";
    public static final String USERNAME = "username";
    public static final String DELETE_MSG = "pos";
    public static final String SKIPPED = "skip";
    public static final String DATA_SIZE_KEY = "data_size";
    public static final String DATA_LIST_KEY = "sent_messages";
    public static final String SHAREDPREFRENCE_FIRST  = "first_launch";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.rec1);
        button = (Button) findViewById(R.id.button3);
        input = (EditText) findViewById(R.id.editText2);
        FirebaseApp.initializeApp(MainActivity.this);
        fireBase = FirebaseFirestore.getInstance();
        collectionReference = fireBase.collection(adapter.COLLECTION);
        SharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPrefranceEditor = SharedPreference.edit();
        int data_size = SharedPreference.getInt(DATA_SIZE_KEY, 0);
        adapter = new MyAdapter(data_size, SharedPreference, SharedPrefranceEditor, fireBase);
        adapter.setClickListener(this);
        new fireBaseId ().execute();
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        helloMsg = (TextView) findViewById(R.id.textView_HelloMessage);


        if(data_size != 0 ) { adapter.updateData(); }
        else if (SharedPreference.getBoolean(SHAREDPREFRENCE_FIRST , true))
        {
            SharedPrefranceEditor.putBoolean(SHAREDPREFRENCE_FIRST , false);
            SharedPrefranceEditor.apply();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String message ;
                message = input.getText().toString();
                input.setText("");
                if(message.length() == 0)
                {
                    Toast.makeText(getApplicationContext(),
                            "EMPTY MASSAGE !", Toast.LENGTH_LONG).show();
                    return;
                }
                new insertToDataBase ().execute(message);
            }
        });

        Bundle extras = getIntent().getExtras();
        manage_navigation(extras);

    }



    @Override
    public void msgClicked(View view, final int position) {
        MyMessage message = adapter.messages.get(position);
        Intent intent = new Intent(MainActivity.this, MsgDetails.class);
        intent.putExtra(MsgDetails.KEY_MESSAGE_CONTENT, message.getMsgText());
        intent.putExtra(MsgDetails.KEY_MESSAGE_ID, message.getMsgId());
        intent.putExtra(MsgDetails.KEY_MESSAGE_TIMESTAMP, message.getMsgTimeStamp());
        intent.putExtra(MsgDetails.KEY_MESSAGE_DEVICE, message.getDevice());
        intent.putExtra(DELETE_MSG, position);
        intent.putExtra(USERNAME, usernameMsg);
        startActivity(intent);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        adapter.messages = new ArrayList<MyMessage>();
        collectionReference.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if(e != null) { return; }
                for(DocumentChange documentChange: queryDocumentSnapshots.getDocumentChanges())
                {
                    DocumentSnapshot documentSnapshot = documentChange.getDocument();
                    String id = documentSnapshot.getId();
                    if ( documentChange.getOldIndex() != -1)
                    {
                        for(int index = 0 ; index < adapter.messages.size(); index++)
                            if (adapter.messages.get(index).getMsgId().equals(id)) {
                                adapter.deleteMessage(index);
                                break;
                            }
                    }
                    else if( documentChange.getNewIndex() != -1
                            && !documentSnapshot.getId().equals(adapter.DOCUMENT_ID)
                            && !documentSnapshot.getId().equals(FIRESTORE_USER_ID))
                    {
                        Map<String, Object> new_doc_data = documentSnapshot.getData();
                        String Id = new_doc_data.get(adapter.ID_KEY)+"";
                        String content = new_doc_data.get(adapter.TEXT_KEY)+"";
                        String timestamp = new_doc_data.get(adapter.TIME_STAMP_KEY) + "";
                        String device = new_doc_data.get(adapter.DEVICE_KEY) + "";
                        adapter.addMsg(Id, timestamp, content, device);
                    }
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(WROTE_MSG, input.getText().toString());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String wrote_message = savedInstanceState.getString(WROTE_MSG);
        input.setText(wrote_message);
        adapter.updateData();
        adapter.notifyDataSetChanged();
    }


    public void manage_navigation(Bundle extras)
    {
        String code = extras.getString(NAVIGATION);
        if(code == null)
            return;

        if(code.equals(SKIPPED)) {
            helloMsg.setVisibility(View.INVISIBLE);
            return;
        }

        if(code.equals(REGISTERED) || code.equals(FIRST_LAUNCH)) {
            String username = extras.getString(USERNAME);
            usernameMsg = username;
            helloMsg.setText("Hello " + username);
            return;
        }

        if(code.equals(DELETE)) {
            adapter.deleteMessage(extras.getInt(DELETE_MSG));
            usernameMsg = extras.getString(USERNAME);
            helloMsg.setText("Hello " + usernameMsg);
            return;
        }

    }

    public class syncLocalToRemoteFireBase extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            adapter.updateFB();
            return null;
        }
    }

    private class fireBaseId  extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            adapter.getIdFb();
            return null;
        }
    }
    private class insertToDataBase  extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... strings) {
            adapter.addToFB(strings[0]);
            return null;
        }
    }

}

