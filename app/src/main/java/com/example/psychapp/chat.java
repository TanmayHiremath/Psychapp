package com.example.psychapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class chat extends AppCompatActivity {
    private List<JSONObject> messageList = new ArrayList<JSONObject>();
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private Socket mSocket;
    private SharedPreferences sharedPref;
    private String displayName, photoUrl, userId, roomName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        sharedPref = chat.this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        photoUrl = sharedPref.getString(getString(R.string.user_photo_url), String.valueOf(R.string.user_def_photo));
        userId = sharedPref.getString(getString(R.string.user_id), String.valueOf(R.string.user_def_id));
        roomName = sharedPref.getString(getString(R.string.room_code), String.valueOf(R.string.def_room_code));
        displayName = sharedPref.getString(getString(R.string.user_display_name), String.valueOf(R.string.user_def_name));
        setTitle(roomName);
        mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_gchat);
        mMessageAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(mMessageAdapter);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        mSocket = SocketManager.getInstance().getSocket();
        mSocket.on("receive message",onReceiveMessage);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView2);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_chat:
                        break;
                    case R.id.ic_game:
                        Intent intent1 = new Intent(chat.this, home.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent1);
                        break;
                    case R.id.ic_profile:
                        Intent intent2 = new Intent(chat.this, profile.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent2);
                        break;
                }
                return false;
            }
        });

    }

    public void sendMessage(View v) throws JSONException {
        EditText chat_text = findViewById(R.id.edit_gchat_message);
        JSONObject message = new JSONObject();
        message.put("username",userId);
        message.put("displayName",displayName);
        message.put("message",chat_text.getText());
        message.put("roomName",roomName);
        message.put("created","12:00");
        mSocket.emit("chat message",message);
        mMessageAdapter.addMessage(message);
        chat_text.setText("");
        mMessageRecycler.scrollToPosition(mMessageAdapter.getItemCount() - 1);

    }

    private final Emitter.Listener onReceiveMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            chat.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject message = (JSONObject) args[0];
                    // add the message to view
                    mMessageAdapter.addMessage(message);
                }
            });
        }
    };
}