package com.example.hayway;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PlaceDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        ImageView placeImage = findViewById(R.id.placeImage);
        TextView placeName = findViewById(R.id.placeName);
        TextView placeDescription = findViewById(R.id.placeDescription);
        Button directionsButton = findViewById(R.id.directionsButton);

        // Get data from intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        String imageUrl = intent.getStringExtra("imageUrl");
        double latitude = intent.getDoubleExtra("latitude", 0);
        double longitude = intent.getDoubleExtra("longitude", 0);

        // Set data
        placeName.setText(name);
        placeDescription.setText(description);
        Glide.with(this).load(imageUrl).into(placeImage);

        // Google Maps button
        directionsButton.setOnClickListener(v -> {
            String uri = "geo:" + latitude + "," + longitude + "?q=" + Uri.encode(name);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        });
    }
}
