package com.example.atoscafeshop;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrders extends RecyclerView.Adapter<AdapterOrders.HolderOrderShop> implements Filterable {
    private Context context;
    public ArrayList<ModelOrders> orderShopList, filterList;
    private FilterOrders filter;

    private FirebaseAuth firebaseAuth;

    public AdapterOrders(Context context, ArrayList<ModelOrders> orderShopList) {
        firebaseAuth = FirebaseAuth.getInstance();
        this.context = context;
        this.orderShopList = orderShopList;
        this.filterList =orderShopList;
    }


    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Infalte layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_orders, parent, false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {
        //Get Data
        ModelOrders modelOrders = orderShopList.get(position);
        String orderId = modelOrders.getOrderId();
        String orderBy = modelOrders.getOrderBy();
        String orderCost = modelOrders.getOrderCost();
        String orderStatus = modelOrders.getOrderStatus();
        String orderTime = modelOrders.getOrderTime();
        String orderTo = modelOrders.getOrderTo();

        //getting shop info
        loadUserInfo(modelOrders, holder);

        //set data
        holder.amountTv.setText("Amount: ₹" + orderCost);
        holder.statusTv.setText(orderStatus);
        holder.orderIdTv.setText("OrderID: " + orderId);
        if (orderStatus.equals("In Progress")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.blue));
        } else if (orderStatus.equals("Completed")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.green));
        } else if (orderStatus.equals("Cancelled")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.red));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString();
        holder.dateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Open OrderDetailsActivity
                Intent intent = new Intent(context,OrderDetailsActivity.class);
                intent.putExtra("orderBy",orderBy);
                intent.putExtra("orderId",orderId);
                context.startActivity(intent);
            }
        });
    }

//    private void loadUserInfo(ModelOrders modelOrders, final HolderOrderShop holder) {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Sellers");
//        Query query = ref.child(firebaseAuth.getUid()).child("Orders");
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    String username = "" + ds.child("orderBy").getValue();
//                    Query userRef = FirebaseDatabase.getInstance().getReference("Users").orderByChild("Username").equalTo(username);
//                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            for (DataSnapshot userDs : snapshot.getChildren()) {
//                                String user = "" + userDs.child("userName").getValue();
//                                holder.userName.setText(user);
//                                // break the loop to set the value only once
//                                break;
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


    private void loadUserInfo(ModelOrders modelOrders, HolderOrderShop holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Sellers");
        ref.child(firebaseAuth.getUid()).child("Orders").child(modelOrders.orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String orderByy = "" + snapshot.child("orderBy").getValue();
                        System.out.println("orderBy" + orderByy);
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(modelOrders.orderBy);
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String uid = modelOrders.orderBy;
                                String userName = "" + snapshot.child("Username").getValue();
                                System.out.println("uid" + modelOrders.orderBy);

                                if (uid.equals(orderByy)) {
                                    holder.userName.setText(userName);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                //Handle error
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        //Handle error
                    }
                });
    }

    @Override
    public int getItemCount() {
        return orderShopList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter = new FilterOrders(this, filterList);
        }
        return filter;
    }

    //view holder class
    class HolderOrderShop extends RecyclerView.ViewHolder {

        //views of layout
        private TextView orderIdTv, dateTv, userName, amountTv, statusTv;

        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);

            //init views of layout
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            dateTv = itemView.findViewById(R.id.dateTv);
            userName = itemView.findViewById(R.id.Username);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);

        }
    }
}
