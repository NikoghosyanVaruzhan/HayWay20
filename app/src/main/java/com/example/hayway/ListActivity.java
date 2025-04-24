package com.example.hayway;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.hayway.R;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_list);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_list) {
                return true;
            } else if (id == R.id.nav_map) {
                startActivity(new Intent(getApplicationContext(), MapActivity.class));
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
                    icon.mutate().setTint(ContextCompat.getColor(ListActivity.this, R.color.purple));
                }
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}