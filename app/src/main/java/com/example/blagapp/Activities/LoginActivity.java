package com.example.blagapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blagapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText lgn_email,lgn_pass;
    private TextView t_intentshit;
    private Button lgb_btn;
    private ProgressBar progressbar;

    private FirebaseAuth mAuth;
    private Intent Homeact;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user=mAuth.getCurrentUser();
//
//        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_APPEND);
//        int aa = sh.getInt("shortcut",0);
//        Log.v("idiotidiot", "index=" + aa);
//
//        if(aa==1){
//            FirebaseAuth.getInstance().signOut();
//            Log.v("idiotidiot", "index=" + aa);
//
//            //login
//            SharedPreferences.Editor myEdit = sh.edit();
//            myEdit.putInt("shortcut",0);
//        }
//        else {
            if (user != null) {
                updateUI();
//            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        lgb_btn=(Button)findViewById(R.id.button_login);
        progressbar=(ProgressBar)findViewById(R.id.progress);
        lgn_email=(EditText)findViewById(R.id.l_email);
        lgn_pass=(EditText)findViewById(R.id.l_pass);
        t_intentshit=(TextView)findViewById(R.id.intentshit);

        mAuth=FirebaseAuth.getInstance();
        Homeact=new Intent(this,Home.class);

        t_intentshit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(registerActivity);
                finish();
            }
        });
        lgb_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressbar.setVisibility(View.VISIBLE);
                lgb_btn.setVisibility(View.INVISIBLE);


                final String mail =lgn_email.getText().toString().trim();
                final String password =lgn_pass.getText().toString().trim();

                if( mail.isEmpty() || password.isEmpty() ){
                    showmessage("Please Verify All Field");
                    lgb_btn.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.INVISIBLE);

                }
                else {
                    signIn(mail,password);
                }
            }
        });




    }

    private void signIn(String mail, String password) {
        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    progressbar.setVisibility(View.INVISIBLE);
                    lgb_btn.setVisibility(View.VISIBLE);
                    updateUI();

                }
                else {
                    showmessage(task.getException().getMessage());

                    lgb_btn.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    private void updateUI() {


        startActivity(Homeact);
        finish();

    }

    private void showmessage(String string) {

        Toast.makeText(this,string, Toast.LENGTH_SHORT).show();
    }
}
