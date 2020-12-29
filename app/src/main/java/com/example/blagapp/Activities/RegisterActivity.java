package com.example.blagapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blagapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private EditText ba_name,ba_email,ba_pass,ba_conpass;
    private Button ba_btn;
    private ProgressBar loadingProgress;
    private ImageView ba_img;
    static int PReqCode=1;
    static int REQUESCODE=1;
    Uri pickedImgUri;


    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //inti
        ba_img=(ImageView) findViewById(R.id.b_imageView);
        ba_name=(EditText) findViewById(R.id.b_name);
        ba_pass=(EditText) findViewById(R.id.b_pass);
        ba_conpass=(EditText) findViewById(R.id.b_conpass);
        ba_email=(EditText) findViewById(R.id.b_email);
        ba_btn=(Button) findViewById(R.id.button_sumbit);
        loadingProgress=(ProgressBar) findViewById(R.id.progressBar);
        loadingProgress.setVisibility(View.INVISIBLE);


        mAuth=FirebaseAuth.getInstance();


        ba_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ba_btn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String email = ba_email.getText().toString();
                final String pass = ba_pass.getText().toString();
                final String connpass = ba_conpass.getText().toString();
                final String name = ba_name.getText().toString();

                if( email.isEmpty() || name.isEmpty() || pass.isEmpty() || !pass.equals(connpass))
                {
                    showmessage("Please Verify all fields");
                    ba_btn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);

                }
                else
                {
                    CreateUserAccount(email,name,pass);

                }







            }
        });

        ba_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(Build.VERSION.SDK_INT >= 22)
                {
                    checkAndRequestForPermission();
                }
                else
                {
                    opengallery();
                }
            }

            private void opengallery()
            {
                Intent galleryIntent =new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,REQUESCODE);
            }

            private void checkAndRequestForPermission()
            {
                if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
                {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                        Toast.makeText(RegisterActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        ActivityCompat.requestPermissions(RegisterActivity.this,
                                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                            PReqCode);
                    }
                }
                else
                {
                    opengallery();
                }
            }
        });



    }

    private void CreateUserAccount(String email, final String name, String pass) {

        mAuth.createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // user account created successfully
                            showmessage("Account created");
                            // after we created user account we need to update his profile picture and name
                            updateUserInfo( name,pickedImgUri,mAuth.getCurrentUser());
                        }
                        else
                        {
                            // account creation failed
                            showmessage("account creation failed" + task.getException().getMessage());
                            ba_btn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });

    }

    private void showmessage(String string) {

        Toast.makeText(this,string, Toast.LENGTH_SHORT).show();
    }

    private void updateUserInfo(final String name, Uri pickedImgUri , final FirebaseUser currentUser) {


        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // image uploaded succesfully
                // now we can get our image url

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // uri contain user image url


                        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();


                        currentUser.updateProfile(profleUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // user info updated successfully
                                            showmessage("Register Complete");
                                            updateUI();
                                        }

                                    }
                                });

                    }
                });





            }
        });



    }
    private void updateUI() {

        Intent homeActivity = new Intent(getApplicationContext(),Home.class);
        startActivity(homeActivity);
        finish();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK && requestCode == REQUESCODE && data != null){

            pickedImgUri=data.getData();
            ba_img.setImageURI(pickedImgUri);

        }
    }
}
