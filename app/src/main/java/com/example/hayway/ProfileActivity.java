package com.example.hayway;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    TextView profileName, profileEmail, profileUsername, profilePassword;
    TextView titleName, titleUsername;
    Button editProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profileUsername = findViewById(R.id.profileUsername);
        profilePassword = findViewById(R.id.profilePassword);
        titleName = findViewById(R.id.titleName);
        titleUsername = findViewById(R.id.titleUsername);
        editProfile = findViewById(R.id.editButton);
        showAllUserData();
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passUserData();
            }
        });
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, v);
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
                    startActivity(new Intent(getApplicationContext(), MapActivity.class));
                    return true;
                } else if (id == R.id.menu_profile) {

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
                    icon.mutate().setTint(ContextCompat.getColor(ProfileActivity.this, R.color.purple));
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public void showAllUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("HayWayPrefs", MODE_PRIVATE);
        String usernameUser = sharedPreferences.getString("username", null);

        if (usernameUser == null) {
            // handle error, like showing "not logged in"
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(usernameUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nameFromDB = snapshot.child("name").getValue(String.class);
                    String emailFromDB = snapshot.child("email").getValue(String.class);
                    String usernameFromDB = snapshot.child("username").getValue(String.class);
                    String passwordFromDB = snapshot.child("password").getValue(String.class);

                    titleName.setText(nameFromDB);
                    titleUsername.setText(usernameFromDB);
                    profileName.setText(nameFromDB);
                    profileEmail.setText(emailFromDB);
                    profileUsername.setText(usernameFromDB);
                    profilePassword.setText(passwordFromDB);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void passUserData(){
        SharedPreferences sharedPreferences = getSharedPreferences("HayWayPrefs", MODE_PRIVATE);
        String usernameUser = sharedPreferences.getString("username", null);

        if (usernameUser == null) {
            return;
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        reference.child(usernameUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String nameFromDB = snapshot.child("name").getValue(String.class);
                    String emailFromDB = snapshot.child("email").getValue(String.class);
                    String usernameFromDB = snapshot.child("username").getValue(String.class);
                    String passwordFromDB = snapshot.child("password").getValue(String.class);

                    Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                    intent.putExtra("name", nameFromDB);
                    intent.putExtra("email", emailFromDB);
                    intent.putExtra("username", usernameFromDB);
                    intent.putExtra("password", passwordFromDB);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

}