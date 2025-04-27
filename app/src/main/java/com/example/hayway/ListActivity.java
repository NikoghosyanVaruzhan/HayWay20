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

        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        bnv.setSelectedItemId(R.id.nav_list);
        bnv.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_list) return true;
            else if (id == R.id.nav_news)
                startActivity(new Intent(this, NewsActivity.class));
            else if (id == R.id.nav_map)
                startActivity(new Intent(this, MapActivity.class));
            overridePendingTransition(0,0);
            return true;
        });
        ImageButton mb = findViewById(R.id.menu_button);
        mb.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(ListActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.top_menu, popup.getMenu());
            try {
                java.lang.reflect.Field f = popup.getClass()
                        .getDeclaredField("mPopup");
                f.setAccessible(true);
                Object helper = f.get(popup);
                Class<?> cls = Class.forName(helper.getClass().getName());
                java.lang.reflect.Method m = cls.getMethod(
                        "setForceShowIcon", boolean.class);
                m.invoke(helper, true);
            } catch (Exception e) { e.printStackTrace(); }
            popup.setOnMenuItemClickListener(mi -> {
                int i = mi.getItemId();
                if (i == R.id.menu_home) return true;
                else if (i == R.id.menu_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                } else if (i == R.id.menu_telegram) {
                    Intent ti = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://t.me/YourTelegramUsername"));
                    startActivity(ti);
                    return true;
                }
                return false;
            });
            popup.show();
            for (int k=0; k<popup.getMenu().size(); k++) {
                Drawable ic = popup.getMenu().getItem(k).getIcon();
                if (ic!=null) ic.mutate()
                        .setTint(ContextCompat.getColor(this, R.color.purple));
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v,insets) -> {
                    Insets sys = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(sys.left, sys.top,
                            sys.right, 0);
                    return insets;
                });
    }
}