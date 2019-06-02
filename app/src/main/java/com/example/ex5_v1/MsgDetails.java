package com.example.ex5_v1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MsgDetails extends AppCompatActivity {
    public static final String KEY_MESSAGE_ID = "id";
    public static final String KEY_MESSAGE_TIMESTAMP = "timestamp";
    public static final String KEY_MESSAGE_CONTENT = "content";
    public static final String KEY_MESSAGE_DEVICE = "device";
    public TextView message;
    public TextView device;
    public TextView id;
    public TextView timestamp;
    public Button deleteButton;
    public int pos;
    public String myUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_details);
        message = (TextView) findViewById(R.id.textView_MessageContent);
        device = (TextView) findViewById(R.id.textView_DeviceInfo);
        id = (TextView) findViewById(R.id.textView_messageKey);
        timestamp = (TextView) findViewById(R.id.textView_Timestamp);
        deleteButton = (Button) findViewById(R.id.button_DeleteMessage);

        Bundle extras = getIntent().getExtras();
        message.setText("Message Text: " + extras.getString(KEY_MESSAGE_CONTENT));
        device.setText("Sent From: " + extras.getString(KEY_MESSAGE_DEVICE));
        timestamp.setText("Sent At: " + extras.getString(KEY_MESSAGE_TIMESTAMP));
        id.setText("Message Key: " + extras.getString(KEY_MESSAGE_ID));
        pos = extras.getInt(MainActivity.DELETE_MSG);
        myUsername = extras.getString(MainActivity.USERNAME);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MsgDetails.this, MainActivity.class);
                intent.putExtra(MainActivity.NAVIGATION, MainActivity.DELETE);
                intent.putExtra(MainActivity.DELETE_MSG, pos);
                intent.putExtra(MainActivity.USERNAME, myUsername);
                startActivity(intent);
                finishAffinity();
            }
        });
    }
}
