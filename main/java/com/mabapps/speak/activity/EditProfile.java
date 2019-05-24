package com.mabapps.speak.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mabapps.speak.R;

public class EditProfile extends AppCompatActivity {

    private EditText name,status,address;
    private Button btnUpName,btnUpStatus,btnUpAddress;
    private String st,nam,addr;

    private DatabaseReference mEditUserDatabse;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mprogressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        MobileAds.initialize(this,"ca-app-pub-9935976681967711~8335957731");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        name = (EditText) findViewById(R.id.editName);
        status = (EditText) findViewById(R.id.edtStatus);
        address = (EditText) findViewById(R.id.edtAddress);

        btnUpAddress = (Button) findViewById(R.id.updateAddress);
        btnUpName = (Button) findViewById(R.id.updateName);
        btnUpStatus = (Button) findViewById(R.id.updateStatus);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mEditUserDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //Show value to edit text
        String status_value = getIntent().getStringExtra("status");
        status.setText(status_value);
        String name_value = getIntent().getStringExtra("name");
        name.setText(name_value);
        String ad = getIntent().getStringExtra("address");
        address.setText(ad);


        //button click actions implementation
        btnUpStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mprogressDialog = new ProgressDialog(EditProfile.this);
                mprogressDialog.setTitle("Updating Status");
                mprogressDialog.setMessage("Wait while we update your status");
                mprogressDialog.show();

                st = status.getText().toString();
                mEditUserDatabse.child("Status").setValue(st).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mprogressDialog.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(),"There is error updating status.Try again.",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        btnUpName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mprogressDialog = new ProgressDialog(EditProfile.this);
                mprogressDialog.setTitle("Updating Name");
                mprogressDialog.setMessage("Wait while we update your Name");
                mprogressDialog.show();

                nam = name.getText().toString();
                mEditUserDatabse.child("Name").setValue(nam).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mprogressDialog.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(),"There is error updating Name.Try again.",Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        btnUpAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mprogressDialog = new ProgressDialog(EditProfile.this);
                mprogressDialog.setTitle("Updating Address");
                mprogressDialog.setMessage("Wait while we update your Address");
                mprogressDialog.show();

                addr = address.getText().toString();
                mEditUserDatabse.child("Address").setValue(addr).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mprogressDialog.dismiss();
                        }else{
                            Toast.makeText(getApplicationContext(),"There is error updating Address.Try again.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


    }


    public void goProfile(View view) {
        Intent n = new Intent(this,UserProfile.class);
        startActivity(n);
    }
}
