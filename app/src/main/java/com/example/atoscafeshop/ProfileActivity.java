package com.example.atoscafeshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private TextView userNameTv,locationTv,mailTv,phoneTv,notificationStatusTv,tv_amount;
    private SwitchCompat fcmSwitch;
    private ImageButton backBtn;
    private ImageView profileIv;

    private static final String enabledMessage = "Notifications are enabled";
    private static final String disabledMessage = "Notifications are disabled";

    private boolean isChecked=true;

    private FirebaseAuth firebaseAuth;

    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        firebaseAuth= FirebaseAuth.getInstance();

        sp = getSharedPreferences("SETTINGS_SP",MODE_PRIVATE);

        userNameTv = findViewById(R.id.userNameTv);
        locationTv = findViewById(R.id.locationTv);
        mailTv = findViewById(R.id.mailTv);
        phoneTv = findViewById(R.id.phoneTv);
        notificationStatusTv = findViewById(R.id.notificationStatusTv);
        fcmSwitch = findViewById(R.id.fcmSwitch);
        backBtn = findViewById(R.id.backBtn);
        profileIv = findViewById(R.id.profileIv);
        tv_amount = findViewById(R.id.tv_amount);

        isChecked = sp.getBoolean("FCM_ENABLED",false);
        fcmSwitch.setChecked(isChecked);
        loadMyInfo();
        if(isChecked){
            notificationStatusTv.setText(enabledMessage);
        }else{
            notificationStatusTv.setText(disabledMessage);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        fcmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    //Enable Notifications
                    subscribeToTopic();
                }else{
                    //Disable Notifications
                    unSubscribeToTopic();
                }
            }
        });

    }
    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Saving settings in shared preferences
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",true);
                        spEditor.apply();
                        Toast.makeText(ProfileActivity.this,""+enabledMessage,Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(enabledMessage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void unSubscribeToTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Saving settings in shared preferences
                        spEditor = sp.edit();
                        spEditor.putBoolean("FCM_ENABLED",false);
                        spEditor.apply();
                        Toast.makeText(ProfileActivity.this,""+disabledMessage,Toast.LENGTH_SHORT).show();
                        notificationStatusTv.setText(disabledMessage);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMyInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("shopName").getValue();
                            String email = "" + ds.child("email").getValue();
                            String location = "" + ds.child("location").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String icon = "" + ds.child("shopIcon").getValue();

                            // Calculate total delivered amount
                            double totalDeliveredAmount = 0;
                            for (DataSnapshot ordersDs : ds.child("Orders").getChildren()) {
                                ModelOrders modelOrders = ordersDs.getValue(ModelOrders.class);
                                if (modelOrders != null && modelOrders.getOrderStatus().equals("Delivered")) {
                                    totalDeliveredAmount += Double.parseDouble(modelOrders.getOrderCost());
                                }
                            }

                            //Set User data
                            userNameTv.setText(name);
                            mailTv.setText(email);
                            locationTv.setText(location);
                            phoneTv.setText(phone);
                            tv_amount.setText(String.format("₹%.2f", totalDeliveredAmount)); // Set the delivered amount to a TextView

                            try {
                                Picasso.get().load(icon).placeholder(R.drawable.ic_profile_black).into(profileIv);
                            } catch (Exception e) {
                                profileIv.setImageResource(R.drawable.ic_profile_black);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}