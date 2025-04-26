package com.example.hayway;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hayway.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SearchView searchView;
    private SightPlaceAdapter adapter;
    private List<SightPlace> placeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_list);

        recyclerView = findViewById(R.id.placesRecyclerView);
        searchView = findViewById(R.id.searchView);

        placeList = new ArrayList<>();
        adapter = new SightPlaceAdapter(placeList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("places");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SightPlace> loaded = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    SightPlace p = ds.getValue(SightPlace.class);
                    if (p != null) loaded.add(p);
                }
                adapter.setPlaces(loaded);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(ListActivity.this, v);
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
                if (item.getItemId() == R.id.menu_home) return true;
                else if (item.getItemId() == R.id.menu_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_telegram) {
                    Intent telegramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/YourTelegramUsername"));
                    startActivity(telegramIntent);
                    return true;
                }
                return false;
            });
            popupMenu.show();
            for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                Drawable icon = popupMenu.getMenu().getItem(i).getIcon();
                if (icon != null) {
                    icon.mutate().setTint(ContextCompat.getColor(ListActivity.this, R.color.purple));
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }
}