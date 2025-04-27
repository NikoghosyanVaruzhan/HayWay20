package com.example.hayway;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NewsActivity extends AppCompatActivity {
    private NewsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

        // RecyclerView setup
        RecyclerView rv = findViewById(R.id.newsRecyclerView);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(this);
        rv.setAdapter(adapter);

        // Load news from Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("news");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                List<NewsItem> list = new ArrayList<>();
                for (DataSnapshot ds : snap.getChildren()) {
                    NewsItem item = ds.getValue(NewsItem.class);
                    if (item != null) list.add(item);
                }
                adapter.setNews(list);
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {
                Toast.makeText(NewsActivity.this,
                        "Failed to load news", Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom navigation (unchanged)
        BottomNavigationView bnv = findViewById(R.id.bottom_navigation);
        bnv.setSelectedItemId(R.id.nav_news);
        bnv.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_news) return true;
            else if (id == R.id.nav_list)
                startActivity(new Intent(this, ListActivity.class));
            else if (id == R.id.nav_map)
                startActivity(new Intent(this, MapActivity.class));
            overridePendingTransition(0,0);
            return true;
        });

        // Top menu button (unchanged)
        ImageButton mb = findViewById(R.id.menu_button);
        mb.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(NewsActivity.this, v);
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

        // Insets listener
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