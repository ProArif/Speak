package com.mabapps.speak.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mabapps.speak.R;
import com.mabapps.speak.helper.classes.ResetPassword;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText email;
    private EditText pass;
    private String em,pa;
    Button btnReg, btnReset,btnLogin;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.textEmailLogin);
        btnReg = (Button) findViewById(R.id.btnRegistration);
        pass = (EditText) findViewById(R.id.textPasswordLogin);
        firebaseAuth = FirebaseAuth.getInstance();
        btnReset = (Button) findViewById(R.id.btn_reset_password);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnReg.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }



    public void UserLogin() {
            final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this, "Please wait", "Logging In..", true);
            (firebaseAuth.signInWithEmailAndPassword(email.getText().toString(), pass.getText().toString())).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(LoginActivity.this, TabbedActivity.class);
                        i.putExtra("Email", firebaseAuth.getCurrentUser().getEmail());
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();

                    } else {
                        progressDialog.hide();
                        Log.e("Error", task.getException().toString());
                        Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });



    }
    public void initialize(){
         em = email.getText().toString().trim();
         pa = pass.getText().toString().trim();
    }
    public boolean validate(){
        boolean valid = true ;
        if (em.isEmpty()){
            email.setError("Please Enter Your Email");
            valid = false ;
        }
        if (pa.isEmpty() || pa.length() <6){
            pass.setError("Please Enter your Password");
            valid = false ;
        }
        return valid;
    }

    @Override
    public void onClick(View v) {
        if (v == btnReg) {
            Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(i);
        }
        if (v == btnReset) {
          Intent o = new Intent(LoginActivity.this,ResetPassword.class);
            startActivity(o);
        }
        if (v == btnLogin) {

            initialize();
            if (!validate()){
                Toast.makeText(this,"Login Failed.Please Try Again.",Toast.LENGTH_LONG).show();
            }else {
                initialize();
                UserLogin();

            }

        }

        }
    }
