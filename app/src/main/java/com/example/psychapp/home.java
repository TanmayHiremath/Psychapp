package com.example.psychapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.client.SocketIOException;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;

public class home extends AppCompatActivity {
    private static final String TAG = home.class.getSimpleName();
    private String displayName, photoUrl, userId;
    public EditText roomCode;
    private SharedPreferences sharedPref;
    public Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedPref = home.this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        photoUrl = sharedPref.getString(getString(R.string.user_photo_url), String.valueOf(R.string.user_def_photo));
        userId = sharedPref.getString(getString(R.string.user_id), String.valueOf(R.string.user_def_id));
        displayName = sharedPref.getString(getString(R.string.user_display_name), String.valueOf(R.string.user_def_name));
        Toast.makeText(home.this, displayName, Toast.LENGTH_SHORT).show();

        SocketManager.getInstance().connectSocket();
        mSocket = SocketManager.getInstance().getSocket();
        mSocket.on("room created", onRoomCreated);
        mSocket.on("room joined", onRoomJoined);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ic_arrow:
                        Intent intent0 = new Intent(home.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent0);
                        break;
                    case R.id.home:
                        break;
                    case R.id.profile:
                        Intent intent2 = new Intent(home.this, profile.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent2);
                        break;
                }
                return false;
            }
        });

    }

    public void createNewRoom(View v) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("username",userId);
        mSocket.emit("new room",data );
    }

    public void joinRoom(View v) throws JSONException {
        roomCode = findViewById(R.id.roomCode);
        JSONObject data = new JSONObject();
        data.put("username",userId);
        data.put("displayName",displayName);
        data.put("roomName",roomCode.getText());
        mSocket.emit("join room",data );
    }


    private final Emitter.Listener onRoomCreated = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            home.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("mytag", "room created");
                    JSONObject data = (JSONObject) args[0];
                    String roomName="empty";
                    try {
                        roomName = data.getString("roomName");
                        Log.d("mytag", roomName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // add the message to view
                    roomCode = findViewById(R.id.roomCode);
                    roomCode.setText(roomName);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.room_code), roomName);
                    editor.apply();
                    Intent intent = new Intent(home.this, chat.class);
                    startActivity(intent);
                    // addMessage(username, message);
                }
            });
        }
    };

    private final Emitter.Listener onRoomJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            home.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(getString(R.string.room_code), String.valueOf(roomCode.getText()));
                    editor.apply();
                    JSONObject data = (JSONObject) args[0];
                    // add the message to view
                    JSONObject message = new JSONObject();
                    try {
                        message.put("username", userId);
                        message.put("displayName", displayName);
                        message.put("message", "I have joined the room");
                        message.put("roomName", String.valueOf(roomCode.getText()));
                        message.put("created", "12:00");
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                    mSocket.emit("chat message",message);
                    Intent intent = new Intent(home.this, chat.class);
                    startActivity(intent);
                    // addMessage(username, message);
                }
            });
        }
    };


}
