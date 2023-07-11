package com.example.atoscafeshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewProduct extends AppCompatActivity {

    private RelativeLayout productsRl;
    private EditText searchProductEt;
    private ImageButton filterProductBtn, backbtn;
    private TextView filteredProductsTv;
    private RecyclerView productsRv;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_product);

        productsRl = findViewById(R.id.productsRl);
        progressDialog = new ProgressDialog(ViewProduct.this);
        progressDialog.setTitle("Loading Products...");
        progressDialog.setCanceledOnTouchOutside(false);

        searchProductEt = findViewById(R.id.searchProductEt);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        filteredProductsTv = findViewById(R.id.filteredProductsTv);
        productsRv = findViewById(R.id.productsRv);
        backbtn = findViewById(R.id.backBtn);

        firebaseAuth = FirebaseAuth.getInstance();

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        progressDialog.show();

        loadAllProducts();

        //Search
        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                try {
                    adapterProductSeller.getFilter().filter(charSequence);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewProduct.this);
                builder.setTitle("Choose Category:")
                        .setItems(Constants.productCategoriesfilter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                //Get Selected Item
                                String selected = Constants.productCategoriesfilter[which];
                                filteredProductsTv.setText("Showing " +selected);
                                if(selected.equals("All")){
                                    //Load All
                                    loadAllProducts();
                                }else{
                                    //Filtered Load
                                    loadFilteredProducts(selected);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    private void loadFilteredProducts(String selected) {
        productList = new ArrayList<>();

        //Get All Products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Before Getting reset list
                        productList.clear();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){

                            String productCategory = ""+ds.child("productCategory").getValue();

                            //If selected category matches product category then add in list
                            if (selected.equals(productCategory)){
                                ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                                productList.add(modelProduct);
                            }

                        }
                        //Setup adapter
                        adapterProductSeller = new AdapterProductSeller(ViewProduct.this,productList);
                        //Set Adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadAllProducts() {
        productList = new ArrayList<>();

        //Get All Products
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
        reference.child(firebaseAuth.getUid()).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Before Getting reset list
                        productList.clear();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productList.add(modelProduct);
                            progressDialog.dismiss();
                        }
                        //Setup adapter
                        adapterProductSeller = new AdapterProductSeller(ViewProduct.this,productList);
                        //Set Adapter
                        productsRv.setAdapter(adapterProductSeller);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}