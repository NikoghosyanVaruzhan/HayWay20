package com.example.hayway;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.hayway.R;

import com.example.hayway.databinding.ActivityMapBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.example.hayway.databinding.ActivityMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAkiKeBmJTQ5WyKJkqWlLck1a8MHfJmcok");
        }
        PlacesClient placesClient = Places.createClient(this);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        binding.searchView.clearFocus(); // remove auto keyboard
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(query)
                        .build();

                placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener(response -> {
                            if (!response.getAutocompletePredictions().isEmpty()) {
                                String placeId = response.getAutocompletePredictions().get(0).getPlaceId();

                                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId,
                                        java.util.Collections.singletonList(Place.Field.LAT_LNG)).build();

                                placesClient.fetchPlace(fetchPlaceRequest)
                                        .addOnSuccessListener(fetchResponse -> {
                                            LatLng latLng = fetchResponse.getPlace().getLatLng();
                                            if (latLng != null && mMap != null) {
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                                mMap.addMarker(new MarkerOptions().position(latLng).title(query));
                                            }
                                        });
                            } else {
                                Toast.makeText(MapActivity.this, "Place not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(MapActivity.this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_map);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                return true;
            } else if (id == R.id.nav_list) {
                startActivity(new Intent(getApplicationContext(), ListActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_news) {
                startActivity(new Intent(getApplicationContext(), NewsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            Context wrapper = new ContextThemeWrapper(MapActivity.this, R.style.CustomPopupMenu);
            PopupMenu popupMenu = new PopupMenu(wrapper, v);

            popupMenu.getMenuInflater().inflate(R.menu.top_menu, popupMenu.getMenu());

            try {
                java.lang.reflect.Field mField = popupMenu.getClass().getDeclaredField("mPopup");
                mField.setAccessible(true);
                Object menuPopupHelper = mField.get(popupMenu);
                Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                java.lang.reflect.Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                setForceIcons.invoke(menuPopupHelper, true);
            } catch (Exception e) {
                e.printStackTrace();
            }


            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_home) {
                    // Already in home (map), do nothing
                    return true;
                } else if (id == R.id.menu_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    return true;
                } else if (id == R.id.menu_telegram) {
                    Intent telegramIntent = new Intent(Intent.ACTION_VIEW);
                    telegramIntent.setData(Uri.parse("https://t.me/YourTelegramUsername")); // <-- change to your real link
                    startActivity(telegramIntent);
                    return true;
                }
                return false;
            });

            popupMenu.show();

            for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                Drawable icon = popupMenu.getMenu().getItem(i).getIcon();
                if (icon != null) {
                    icon.mutate().setTint(ContextCompat.getColor(MapActivity.this, R.color.purple));
                }
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready!", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                    mMap.addMarker(new MarkerOptions().position(myLocation).title("You are here"));
                }
            });

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}