package com.example.atoscafeshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrdersActivity extends AppCompatActivity {
    private ImageButton backBtn,filterOrderBtn;
    private TextView filteredOrdersTv;
    private RecyclerView ordersRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrders> ordersArrayList;
    private AdapterOrders adapterOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        backBtn = findViewById(R.id.backBtn);
        filterOrderBtn = findViewById(R.id.filterOrderBtn);
        filteredOrdersTv = findViewById(R.id.filteredOrdersTv);

        ordersRv = findViewById(R.id.ordersRv);
        firebaseAuth = FirebaseAuth.getInstance();

        loadAllOrders();
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String[] options = {"All","In Progress", "Completed","Cancelled","Delivered"};

                AlertDialog.Builder builder = new AlertDialog.Builder(OrdersActivity.this);
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i==0){
                                    filteredOrdersTv.setText("Showing All Orders");
                                    adapterOrders.getFilter().filter(""); //Showing all orders
                                }
                                else{
                                    String optionClicked = options[i];
                                    filteredOrdersTv.setText("Showing"+" "+optionClicked+" "+"Orders");
                                    adapterOrders.getFilter().filter(optionClicked);
                                }
                            }
                        }).show();
                }
        });

    }

    private void loadAllOrders() {
        //init order list
        ordersArrayList = new ArrayList<>();
        //get orders
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
        reference.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ordersArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelOrders modelOrders = ds.getValue(ModelOrders.class);
                            //add to list
                            ordersArrayList.add(modelOrders);
                        }
                        //setup adapter
                        adapterOrders = new AdapterOrders(OrdersActivity.this, ordersArrayList);
                        //set to recycler view
                        ordersRv.setAdapter(adapterOrders);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}