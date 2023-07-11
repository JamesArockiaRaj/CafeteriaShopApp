package com.example.atoscafeshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private ImageView AddImage;
    private EditText etTitle,etQuantity,etPrice;
    private TextView categoryTv;
    private MaterialButton updateProductBtn;

    private String productId;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //image pick constants
    private static int IMAGE_PICK_GALLERY_CODE = 400;
    private static int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String [] cameraPermissions;
    private String [] storagePermissions;

    //image picked uri
    private Uri image_uri;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        AddImage = findViewById(R.id.add);
        etTitle = findViewById(R.id.etTitle);
        categoryTv = findViewById(R.id.categoryTv);
        etQuantity = findViewById(R.id.etQuantity);
        etPrice = findViewById(R.id.etPrice);
        updateProductBtn = findViewById(R.id.updateProductbtn);

        //Get id of the products of intent
        productId = getIntent().getStringExtra("productId");


        firebaseAuth = FirebaseAuth.getInstance();
        loadProductDetails(); //To set values of products

        //SETUP PROGRESS DIALOG
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        //init permission arrays
        cameraPermissions = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        AddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show dialog to pick image
                showImagePickDialog();
            }
        });

        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pick category
                categoryDialog();
            }
        });
        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FLOW
                //1)Input Data
                //2)Validate Data
                //3) Update Data to DB

                inputData();
            }
        });

    }

    private void loadProductDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //Get Data
                        String productId = ""+snapshot.child("productId").getValue();
                        String productTitle = ""+snapshot.child("productTitle").getValue();
                        String productDescription = ""+snapshot.child("productDescription").getValue();
                        String productCategory = ""+snapshot.child("productCategory").getValue();
                        String productQuantity = ""+snapshot.child("productQuantity").getValue();
                        String productIcon = ""+snapshot.child("productIcon").getValue();
                        String Price = ""+snapshot.child("Price").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        //SET DATA TO VIEWS
                        etTitle.setText(productTitle);
                        categoryTv.setText(productCategory);
                        etQuantity.setText(productQuantity);
                        etPrice.setText(Price);

                        try {
                            Picasso.get().load(productIcon).placeholder(R.drawable.addproduct).into(AddImage);
                        } catch (Exception e){
                            AddImage.setImageResource(R.drawable.addproduct);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private String productTitle,productCategory,productQuantity,Price;
    private void inputData() {
        //1) Input Data
        productTitle = etTitle.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = etQuantity.getText().toString().trim();
        Price = etPrice.getText().toString().trim();

        //2) Validate Data

//        if(AddImage.drawable.constantState == getResources().getDrawable(R.id.add)){
//        }


        if(TextUtils.isEmpty(productTitle)){
            Toast.makeText(this,"Title is Required",Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }
        if(TextUtils.isEmpty(productCategory)){
            Toast.makeText(this,"Category is Required",Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }
        if(TextUtils.isEmpty(productQuantity)){
            Toast.makeText(this,"Quantity is Required",Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }
        if(TextUtils.isEmpty(Price)){
            Toast.makeText(this,"Price is Required",Toast.LENGTH_SHORT).show();
            return; //don't proceed further
        }

        updateProduct();
    }

    private void updateProduct() {
        //Show Progress
        progressDialog.setMessage("Updating Product...");
        progressDialog.show();

        if(image_uri == null){
            //Update without image
            //Setup data in hashmap to update
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("Price",""+Price);

            //Update to DB
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
            reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            //Update Success
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this,"Updated Successfully",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Update Failed
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            //Update with image
            //First upload image in firebase storage and set name and path
            String filePathAndName = "product_images/"+""+productId; //Override previous image with same id
            //Uploaded Image
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded
                            //get url of uploaded img
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();

                            if(uriTask.isSuccessful()) {
                                //url of img received,upload to db
                                //setup data to upload
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("productIcon",""+downloadImageUri); //set image
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("Price",""+Price);

                                //Update to DB
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Sellers");
                                reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //Update Success
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this,"Updated Successfully",Toast.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //Update Failed
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Upload Failed
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });



        }
    }

    private void categoryDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category").setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //GET PICKED CATEGORY
                String category = Constants.productCategories[i];

                //SET PICKED CATEGORY
                categoryTv.setText(category);
            }
        }).show();
    }

    private void showImagePickDialog(){
        //OPTIONS TO DISPLAY IN DIALOG
        String [] options = {"Camera","Gallery"};
        //DIALOG
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //handle item clicks
                        if(i==0){
                            //camera clicked
                            if(checkCameraPermission()){
                                //permission granted
                                pickFromCamera();
                            }else{
                                //if permission not granted, request
                                requestCameraPermission();
                            }
                        }else{
                            if(checkStoragePermission()){
                                //permission granted
                                pickFromGallery();
                            }else{
                                //if permission not granted, request
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();

    }
    private void pickFromGallery(){
        //intent to pick image from gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }
    private void pickFromCamera(){

        //intent to pick image from CAMERA

        //USING MEDIASTORE TO PICK HIGH/ORIGINAL QUALITY IMAGE
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result; //returns result as true/ false
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean results = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && results; //returns result as true/ false

    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //handle permission results

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        //both permission granted
                        pickFromCamera();
                    }else{
                        //both or one permission denied
                        Toast.makeText(this, " Camera & Storage Permission Needed", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            case STORAGE_REQUEST_CODE: {
                if(grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        //both permission granted
                        pickFromGallery();
                    }else{
                        //both or one permission denied
                        Toast.makeText(this, " Storage Permission Needed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
//            case default:{
//
//            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //handle image pick results

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //IMAGE PICKED FROM GALLERY
                //SAVE PICKED IMG URI

                image_uri = data.getData();

                //SET IMAGE
                AddImage.setImageURI(image_uri);
            } else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //IMAGE PICKED FROM CAMERA

                AddImage.setImageURI(image_uri);


            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}