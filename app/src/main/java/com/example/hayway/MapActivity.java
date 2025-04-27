package com.example.hayway;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.hayway.databinding.ActivityMapBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final float VISIT_THRESHOLD_METERS = 50f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    // track markers → placeData + firebase key
    private final Map<Marker, SightPlace> markerData = new HashMap<>();
    private final Map<Marker, String>      markerKey  = new HashMap<>();
    private boolean dialogShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Places API (unchanged)
        if (!com.google.android.libraries.places.api.Places.isInitialized()) {
            com.google.android.libraries.places.api.Places.initialize(
                    getApplicationContext(), "YOUR_API_KEY");
        }
        com.google.android.libraries.places.api.net.PlacesClient placesClient =
                com.google.android.libraries.places.api.Places.createClient(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // build a location callback that checks for nearby places
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) checkNearbyPlaces(loc);
            }
        };

        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
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
            public boolean onQueryTextChange(String newText) { return false; }
        });

        // Bottom navigation setup (unchanged)
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

        // Top menu button (unchanged)
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
                if (item.getItemId() == R.id.menu_home) {
                    return true;
                } else if (item.getItemId() == R.id.menu_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_telegram) {
                    Intent telegramIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://t.me/YourTelegramUsername"));
                    startActivity(telegramIntent);
                    return true;
                }
                return false;
            });
            popupMenu.show();
            for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                Drawable icon = popupMenu.getMenu().getItem(i).getIcon();
                if (icon != null) icon.mutate().setTint(ContextCompat.getColor(this, R.color.purple));
            }
        });

        // Apply window insets (avoid bottom padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, 0);
            return insets;
        });


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        loadPlacesFromFirebase();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            // move camera to last known, plus add “You are here” marker
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, loc -> {
                        if (loc != null) {
                            LatLng me = new LatLng(loc.getLatitude(), loc.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(me, 15));
                            mMap.addMarker(new MarkerOptions()
                                    .position(me)
                                    .title("You are here"));
                        }
                    });
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void startLocationUpdates() {
        LocationRequest req = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10_000);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(
                    req, locationCallback, Looper.getMainLooper()
            );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void loadPlacesFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("places");
        ref.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                mMap.clear();
                markerData.clear();
                markerKey.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    String key = ds.getKey();
                    SightPlace place = ds.getValue(SightPlace.class);
                    if (place == null) continue;

                    LatLng loc = new LatLng(place.latitude, place.longitude);
                    MarkerOptions opts = new MarkerOptions()
                            .position(loc)
                            .title(place.name);

                    Marker marker = mMap.addMarker(opts);
                    if (marker == null) continue;

                    // if already visited, make it green
                    if (place.isVisited()) {
                        marker.setIcon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }

                    markerData.put(marker, place);
                    markerKey.put(marker, key);
                }

                // click a marker to open detail
                mMap.setOnMarkerClickListener(marker -> {
                    SightPlace p = markerData.get(marker);
                    if (p != null) {
                        Intent i = new Intent(MapActivity.this,
                                PlaceDetailActivity.class);
                        i.putExtra("name", p.name);
                        i.putExtra("description", p.description);
                        i.putExtra("imageUrl", p.photoUrl);
                        i.putExtra("latitude", p.latitude);
                        i.putExtra("longitude", p.longitude);
                        startActivity(i);
                        return true;
                    }
                    return false;
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapActivity.this,
                        "Failed to load places", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkNearbyPlaces(Location me) {
        if (dialogShowing) return;

        for (Map.Entry<Marker, SightPlace> entry : markerData.entrySet()) {
            Marker marker = entry.getKey();
            SightPlace place = entry.getValue();
            if (place.isVisited()) continue;

            float[] dist = new float[1];
            Location.distanceBetween(
                    me.getLatitude(), me.getLongitude(),
                    place.latitude,  place.longitude,
                    dist
            );
            if (dist[0] < VISIT_THRESHOLD_METERS) {
                showConfirmDialog(marker, place);
                break;
            }
        }
    }

    private void showConfirmDialog(Marker marker, SightPlace place) {
        dialogShowing = true;

        View v = getLayoutInflater()
                .inflate(R.layout.dialog_confirm_visit, null);
        ImageView img       = v.findViewById(R.id.confirmPlaceImage);
        TextView question   = v.findViewById(R.id.confirmQuestion);
        Button   btnConfirm = v.findViewById(R.id.buttonConfirmVisit);

        question.setText("Are you at “" + place.name + "”?");
        Glide.with(this).load(place.photoUrl).into(img);

        AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(v)
                .setOnDismissListener(d -> dialogShowing = false)
                .create();
        dlg.show();

        btnConfirm.setOnClickListener(b -> {
            // 1) write back to Firebase
            String key = markerKey.get(marker);
            FirebaseDatabase.getInstance()
                    .getReference("places")
                    .child(key)
                    .child("visited")
                    .setValue(true);

            // 2) locally update marker
            marker.setIcon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            dlg.dismiss();
        });
    }

    @Override
    public void onRequestPermissionsResult(int rc,
                                           @NonNull String[] perms,
                                           @NonNull int[] results) {
        super.onRequestPermissionsResult(rc, perms, results);
        if (rc == LOCATION_PERMISSION_REQUEST_CODE &&
                results.length > 0 &&
                results[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            Toast.makeText(this,
                    "Location permission denied",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
