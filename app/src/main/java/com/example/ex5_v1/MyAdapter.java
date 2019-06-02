package com.example.ex5_v1;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.viewHold > {

    public int counterID  = 0;
    public ArrayList<MyMessage> messages ;
    public LongClick  click;
    public int dataLength  ;
    private Gson G_son ;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    private static final int EMPTYING = 0;
    public static final String DOCUMENT_ID = "ofuuEKxarEiR2P8JFVcj";
    public static final String FIELD_NAME_ID = "project_id";
    public static final String COLLECTION  = "messages";
    public static final String TEXT_KEY  = "content";
    public static final String ID_KEY = "id";
    public static final String TIME_STAMP_KEY   = "timestamp";
    public static final String DEVICE_KEY   = "device";
    private FirebaseFirestore dataBase  ;

    
    public MyAdapter(int size, SharedPreferences sp, SharedPreferences.Editor edit,
                     FirebaseFirestore dataBase  ){
        this.messages  = new ArrayList<MyMessage>();
        this.dataLength   = size;
        this.sharedPreferences = sp;
        this.editor = edit;
        this.G_son  = new Gson();
        this.dataBase   = dataBase  ;
    }

    public interface LongClick  {
        void msgClicked(View view, final int position);
    }

    public void setData(ArrayList<MyMessage> list) {
        messages = list;
    }


    public class viewHold  extends RecyclerView.ViewHolder implements View.OnLongClickListener{
        TextView textView;
        TextView timestamp;

        public viewHold (View v){
            super(v);
             textView = v.findViewById(R.id.TextView_msg);
             timestamp = v.findViewById(R.id.timestamp);
            v.setOnLongClickListener(this);
        }

        public boolean onLongClick(View v){
            if (click != null) {
                click.msgClicked(v, getAdapterPosition());
            }
            return true;
        }
    }



    @Override
    public viewHold  onCreateViewHolder(ViewGroup parent, int viewType) {

        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg, parent, false);

        viewHold VH = new viewHold(v);
        return VH; }

    @Override
    public void onBindViewHolder(viewHold  holder, int position) {
        String message = messages.get(position).getMsgText();
        String timestamp = messages.get(position).getMsgTimeStamp();
        holder.textView.setText(message);
        holder.timestamp.setText(timestamp);
    }

    @Override
    public int getItemCount() {
        if(messages != null){
            return messages.size();
        }
        else {
            messages = new ArrayList<>();
            return EMPTYING;
        }
    }

    public void saveEditions() {
        editor.putInt(MainActivity.DATA_SIZE_KEY, this.dataLength  );
        String json  = G_son.toJson(messages );
        editor.putString(MainActivity.DATA_LIST_KEY, json );
        editor.apply();
    }

    public void addMsg(String id, String timestamp, String message, String device){
        messages .add(new MyMessage(id, timestamp, message, device));
        dataLength = dataLength+ 1;
        saveEditions();
        notifyDataSetChanged();
    }

    public void deleteMessage(int pos){
        new DeleteFromFB().execute(this.messages .get(pos).getMsgId());
        this.messages .remove(pos);
        dataLength   =dataLength- 1;
        saveEditions();
        notifyItemRemoved(pos);
    }



    public void setClickListener(LongClick  itemClick) {
        this.click = itemClick;
    }

    public void updateData() {
        String j_son  = sharedPreferences .getString(MainActivity.DATA_LIST_KEY, "");
        Type typeToken = new TypeToken<List<MyMessage>>() {
        }.getType();
        this.messages  = G_son.fromJson(j_son , typeToken);
    }

    public static String clock() {
        DateFormat dateFormat = new SimpleDateFormat("kk:mm");
        return dateFormat.format(new Date());
    }

    private class DeleteFromFB extends AsyncTask<String, Void, Void>
    {
        @Override
        protected Void doInBackground(String... strings) {
            deleteDocument(strings[0]);
            return null;
        }
    }


    public void addToFB(final String message)
    {
        String currentTime = clock();
        String device = Build.MANUFACTURER + " " + Build.MODEL + " " + Build.VERSION.RELEASE;
        updateFBId(counterID );
        Map<String, Object> sent_message = new HashMap<>();
        int increment_id = counterID  + 1;

        sent_message.put(TEXT_KEY , message);
        sent_message.put(TIME_STAMP_KEY  ,currentTime);
        sent_message.put(ID_KEY, increment_id);
        sent_message.put(DEVICE_KEY  , device);

        dataBase  .collection(COLLECTION )
                .document(increment_id + "")
                .set(sent_message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(" ", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(" ", "Error writing document", e);
                    }
                });
        counterID ++;
    }

    public void updateFBId(int id)
    {
        DocumentReference washingtonRef = dataBase  .collection(COLLECTION ).
                document(DOCUMENT_ID);

        washingtonRef
                .update(FIELD_NAME_ID, id + 1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("", "Error updateFBId", e);
                    }
                });
    }

    public void getIdFb()

    {
        DocumentReference docRef = dataBase  .collection(COLLECTION ).
                document(DOCUMENT_ID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        final String id = document.getData().get(FIELD_NAME_ID) + "";
                        counterID  = Integer.parseInt(id);
                    } else {
                        Log.d("", "No such document");
                    }
                } else {
                    Log.d("", "get failed with ", task.getException());
                }
            }
        });
    }

    public void deleteDocument(String docId)
    {
        dataBase  .collection(COLLECTION ).document(docId)
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(" ", "Error deleteDocument", e);
                    }
                });
    }

    public void updateFB()
    {
        final ArrayList<MyMessage> dataArray = new ArrayList<MyMessage>();
        dataBase  .collection(COLLECTION )
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            String id ;
                            String timestamp;
                            String  content;
                            String device;
                            Map<String, Object> singleMsg;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(!document.getId().equals(DOCUMENT_ID) &&
                                        !document.getId().equals(MainActivity.FIRESTORE_USER_ID))
                                {
                                    singleMsg = document.getData();
                                    id = singleMsg.get(ID_KEY) + "";
                                    timestamp = singleMsg.get(TIME_STAMP_KEY  ) + "";
                                    content = singleMsg.get(TEXT_KEY ) + "";
                                    device = singleMsg.get(DEVICE_KEY  ) + "";
                                    dataArray.add(new MyMessage(id, timestamp, content, device));
                                }
                            }

                            for (MyMessage msg: dataArray)
                                addMsg(msg.getMsgId(), msg.getMsgTimeStamp()
                                        , msg.getMsgText(), msg.getDevice());
                        } else {
                            Log.d(" ", "Error updateFB", task.getException());
                        }
                    }
                });
    }



}

