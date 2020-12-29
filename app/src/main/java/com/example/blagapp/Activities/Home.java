package com.example.blagapp.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.example.blagapp.Models.Post;
import com.example.blagapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.Gravity;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends AppCompatActivity {

    private static final int PReqCode=2;
    private static final int REQUESCODE=2;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
//    SharedPreferences sharedpreferences;
    ImageView popupUserImg,popupPostImage,popupAddBtn;
    EditText popupTitle,popupDescription;
    ProgressBar popupprogressbar;

    Dialog popup;


    private AppBarConfiguration mAppBarConfiguration;
    private Uri pickedImgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // init

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        //init popup

        initpopup();
        setupPopUpImageClick();

//        sharedpreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
//        SharedPreferences.Editor myEdit = sharedpreferences.edit();
//        myEdit.putInt("shortcut",1);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                popup.show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        updateNavHeader();

//        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new HomeFragment()).commit();
    }

    private void setupPopUpImageClick() {

        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestForPermission();

            }
        });

    }

    private void initpopup() {

        popup=new Dialog(this);
        popup.setContentView(R.layout.popup_post);
        popup.getWindow().setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));
        popup.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
        popup.getWindow().getAttributes().gravity= Gravity.TOP;


        //init

        popupAddBtn=(ImageView)popup.findViewById(R.id.popup_add);
        popupPostImage=(ImageView)popup.findViewById(R.id.popupPostIm);
        popupUserImg=(ImageView)popup.findViewById(R.id.popup_user_image);
        popupTitle=(EditText) popup.findViewById(R.id.popup_title);
        popupDescription=(EditText) popup.findViewById(R.id.popup_desc);
        popupprogressbar=(ProgressBar)popup.findViewById(R.id.popup_progressbar);
        Glide.with(this).load(currentUser.getPhotoUrl()).into(popupUserImg);



        //Add Post Click Listener

        popupAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupprogressbar.setVisibility(View.VISIBLE);
                popupAddBtn.setVisibility(View.INVISIBLE);

                if(!popupTitle.getText().toString().trim().isEmpty()
                && !popupDescription.getText().toString().trim().isEmpty()
                &&pickedImgUri!=null){

                    StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("blog_images");
                    final StorageReference imageFilePath=storageReference.child(pickedImgUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink= uri.toString();
                                    //create post object
                                    Post post = new Post(popupTitle.getText().toString(),
                                            popupDescription.getText().toString(),
                                            imageDownloadLink,
                                            currentUser.getUid(),
                                            currentUser.getPhotoUrl().toString());
                                    addPost(post);




                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    showmessage(e.getMessage());

                                    popupprogressbar.setVisibility(View.INVISIBLE);
                                    popupAddBtn.setVisibility(View.VISIBLE);

                                }
                            });
                        }
                    });

                }
                else {
                    showmessage("Please Verify all input fields abd choose Post Image ");
                    popupprogressbar.setVisibility(View.INVISIBLE);
                    popupAddBtn.setVisibility(View.VISIBLE);
                }

            }
        });



    }

    private void addPost(Post post) {

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myRef=database.getReference("Posts").push();


        String key=myRef.getKey();
        post.setPostKey(key);


        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                showmessage("Post Added Successfully");
                popupprogressbar.setVisibility(View.INVISIBLE);
                popupAddBtn.setVisibility(View.VISIBLE);
                popup.dismiss();

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

/*
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {

            Toast.makeText(this, "Time to Exit", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginActivity);
            finish();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

 */

    private void showmessage(String string) {

        Toast.makeText(this,string, Toast.LENGTH_SHORT).show();
    }
private void checkAndRequestForPermission()
{
    if(ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED)
    {
        if(ActivityCompat.shouldShowRequestPermissionRationale(Home.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(Home.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PReqCode);
        }
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
    public void updateNavHeader(){

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername= headerView.findViewById(R.id.nav_username);
        TextView navUsermail= headerView.findViewById(R.id.nav_usermail);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_user_photo);

        navUsermail.setText(currentUser.getEmail());
        navUsername.setText(currentUser.getDisplayName());

        Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhoto);


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==RESULT_OK && requestCode == REQUESCODE && data != null){

            pickedImgUri=data.getData();
            popupPostImage.setImageURI(pickedImgUri);

        }
    }
}
