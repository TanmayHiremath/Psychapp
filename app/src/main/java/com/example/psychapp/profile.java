package com.example.psychapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.InputStream;

public class profile extends AppCompatActivity {
    private String displayName;
    private EditText editName;
    private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        sharedPref = profile.this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String photoUrl = sharedPref.getString(getString(R.string.user_photo_url), String.valueOf(R.string.user_def_photo));
        String userId = sharedPref.getString(getString(R.string.user_id), String.valueOf(R.string.user_def_id));
        displayName = sharedPref.getString(getString(R.string.user_display_name), String.valueOf(R.string.user_def_name));

        Button signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
            private void signOut() {
                MainActivity.mGoogleSignInClient.signOut().addOnCompleteListener(profile.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent i = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(i);
                        Log.d("mytag", "logged out successfully");
                        Toast.makeText(getApplicationContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        new DownloadImageFromInternet((ImageView) findViewById(R.id.image_view)).execute(photoUrl);
    }

    private class DownloadImageFromInternet extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public DownloadImageFromInternet(ImageView imageView) {
            this.imageView = imageView;
//            Toast.makeText(getApplicationContext(), "Please wait, it may take a few minute...", Toast.LENGTH_SHORT).show();
        }

        protected Bitmap doInBackground(String... urls) {
            String imageURL = urls[0];
            Bitmap bimage = null;
            try {
                InputStream in = new java.net.URL(imageURL).openStream();
                bimage = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error Message", e.getMessage());
                e.printStackTrace();
            }
            return bimage;
        }

        protected void onPostExecute(Bitmap result) {
            editName = findViewById(R.id.displayName);
            imageView.setImageBitmap(result);
            editName.setText(displayName);
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
            Menu menu = bottomNavigationView.getMenu();
            MenuItem menuItem = menu.getItem(2);
            menuItem.setChecked(true);
            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.ic_arrow:
                            finish();
                            break;
                        case R.id.home:
                            Intent intent1 = new Intent(profile.this, home.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent1);
                            break;
                        case R.id.profile:
                            break;
                    }
                    return false;
                }
            });
        }
    }
    public void saveDisplayName(View v){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.user_display_name), String.valueOf(editName.getText()));
        editor.apply();
        Toast.makeText(this, "Saved Successfully!", Toast.LENGTH_SHORT).show();
    }
}