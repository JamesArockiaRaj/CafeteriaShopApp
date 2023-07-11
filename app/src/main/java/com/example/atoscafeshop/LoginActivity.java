package com.example.atoscafeshop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText etUsername, etPassword;
    MaterialButton loginbtn;
    SharedPreferences sharedPreferences;
    public static final String fileName = "login";
    public static final String Username = "username";
    public static final String Password = "password";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    HashMap<String, Object> hashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        etUsername = findViewById(R.id.etusername);
        etPassword = findViewById(R.id.etpassword);
        loginbtn = findViewById(R.id.loginbtn);

        sharedPreferences = getSharedPreferences(fileName, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(Username)){
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginSeller();
//                String username = etUsername.getText().toString();
//                String password = etPassword.getText().toString();
//
//                if(username.equals(("store@gmail.com"))&& password.equals("admin123")){
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString(Username,username);
//                    editor.putString(Password,password);
//                    editor.commit();
//                    firebaseAuth.signInWithEmailAndPassword(username,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if(task.isSuccessful()){
//                                Intent i = new Intent(LoginActivity.this,MainActivity.class);
//                                startActivity(i);
//                            }
//                        }
//                    });
//                }
//                else{
//                    Toast.makeText(getApplicationContext(), "LOGIN FAILED", Toast.LENGTH_SHORT).show();
//                    etUsername.setText("");
//                    etUsername.requestFocus();
//                    etPassword.setText("");
//                }
            }
        });

//
    }

    private void loginSeller() {
            String email = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(LoginActivity.this,"Invalid Mail Id",Toast.LENGTH_SHORT).show();
                return;
                //don't proceed further
            }

            if(password.length()<6){
                Toast.makeText(LoginActivity.this,"Enter Password",Toast.LENGTH_SHORT).show();
                return; //don't proceed further
            }

            progressDialog.setTitle("Logging In...");
            progressDialog.show();


            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            progressDialog.dismiss();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(Username,email);
                            editor.putString(Password,password);
                            editor.commit();
                            Intent i = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(i);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });
    }
}