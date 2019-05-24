package com.mabapps.speak.activity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mabapps.speak.R;
import com.mabapps.speak.helper.classes.UserInformation;

import java.util.Calendar;
import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText email, fullName, userName, pass, conPass, birthday, address;
    private String userEmail,Name,UserName,Pass,ConPass,Address,Birthday,status;
    private FirebaseAuth firebaseAuth;
    Spinner religion;
    private Button btnReg;
    private CheckBox checkBox;
    private RadioGroup genderRadioGroup;
    private RadioButton male,female;
    private DatePickerDialog datePickerDialog;
    //Database

    DatabaseReference mReference;

    private String[] type = {"Muslim", "Hindu", "Christian"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        btnReg = (Button) findViewById(R.id.btnReg);
        checkBox = (CheckBox)findViewById(R.id.checkBox);
        fullName = (EditText) findViewById(R.id.fullName);
        userName = (EditText) findViewById(R.id.user);
        conPass = (EditText) findViewById(R.id.conPass);
        birthday = (EditText) findViewById(R.id.birthday);
        address = (EditText) findViewById(R.id.address);
        genderRadioGroup = (RadioGroup) findViewById(R.id.gender_radiogroup);
        email = (EditText) findViewById(R.id.textEmailReg);
        pass = (EditText) findViewById(R.id.textPasswordReg);
        religion = (Spinner) findViewById(R.id.religion);
        firebaseAuth = FirebaseAuth.getInstance();
        male=(RadioButton)findViewById(R.id.male);
        female=(RadioButton)findViewById(R.id.female);

        //Database
        //databaseReference= FirebaseDatabase.getInstance().getReference();


        //firebaseAuth = FirebaseAuth.getInstance();

        prepareDatePickerDialog();

        birthday.setOnClickListener(this) ;
        btnReg.setOnClickListener(this);

    }

    //Initialize Variables
    public void initialize() {
        Name = fullName.getText().toString().trim();
        UserName = userName.getText().toString().trim();
        Pass = pass.getText().toString().trim();
        ConPass = conPass.getText().toString().trim();
        userEmail = email.getText().toString().trim();
        Address = address.getText().toString().trim();
        Birthday = birthday.getText().toString().trim();
    }

    //Database
    private void saveUserInformation() {
        final String radioValue =
                ((RadioButton) findViewById(genderRadioGroup.getCheckedRadioButtonId()))
                        .getText().toString();
        String Religion = religion.getSelectedItem().toString().trim();
        UserInformation userInfo = new UserInformation(Name, UserName, Pass, userEmail, Address, Birthday, Religion, radioValue);
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        String id = firebaseUser.getUid();
        //String id=databaseReference.push().getKey();


        if (firebaseUser!=null) {
            //databaseReference.child("Users").child(id);

            mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(id);

            HashMap<String,String> users = new HashMap<>();
            users.put("Name",Name);
            users.put("UserName",UserName);
            users.put("Email",userEmail);
            users.put("Password",Pass);
            users.put("Address",Address);
            users.put("Birthday",Birthday);
            users.put("Religion",Religion);
            users.put("Gender",radioValue);
            users.put("Status","Hi there,I am using Speak app.");
            users.put("image","default");
            users.put("thumb_image","default");


            mReference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegistrationActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(RegistrationActivity.this, TabbedActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);

                    }
                }
            });
            Toast.makeText(this, "Information saved", Toast.LENGTH_SHORT).show();


        }
        else {
            Toast.makeText(this,"Null Pointer exception",Toast.LENGTH_LONG).show();
            email.setText("");
            fullName.setText("");
            userName.setText("");
            pass.setText("");
            conPass.setText("");
            birthday.setText("");
            address.setText("");
        }
    }

    public boolean validate(){
        boolean valid=true;

        if (Name.isEmpty() || Name.length()>32){
            fullName.setError("Please Enter a Valid Name");
            valid=false;
        }

        if (userEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()){
            email.setError("Please Enter a valid Email Address");
            valid=false;
        }
        if (Pass.isEmpty()){
            pass.setError("Please enter Password!");
            valid=false;
        }
        if (ConPass.isEmpty()){
            conPass.setError("Please Confirm Your Password!!");
            valid=false;
        }
        if (!Pass.equals(ConPass)) {
            conPass.setError("Password do not matched!!");
            valid=false;
        }else if(Pass.equals(ConPass)){
            valid=true;
        }
        if (Address.isEmpty()){
            address.setError("Please Enter your address");
            valid=false;
        }
        if (Birthday.isEmpty()){
            birthday.setError("Please Select your birthday");
            valid=false;
        }
        if (!checkBox.isChecked()){
            Toast.makeText(this,"Please Agree with Terms and Conditions",Toast.LENGTH_LONG).show();
            valid=false;
        }
    return valid;
    }

    public boolean createUser(){

            final ProgressDialog progressDialog = ProgressDialog.show(RegistrationActivity.this, "Please Wait...", "While we process your information", true);
            progressDialog.setCanceledOnTouchOutside(false);
                firebaseAuth.createUserWithEmailAndPassword(this.userEmail, pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( @NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            saveUserInformation();

                        } else {
                            progressDialog.hide();
                            Toast.makeText(RegistrationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
        return true;
        }

    private void prepareDatePickerDialog() {
        //Get current date
        Calendar calendar = Calendar.getInstance();

        //Create datePickerDialog with initial date which is current and decide what happens when a date is selected.
        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                //When a date is selected, it comes here.
                //Change birthdayEdittext's text and dismiss dialog.
                int month=monthOfYear+1;
                birthday.setText(dayOfMonth + "/" + month + "/" + year);
                datePickerDialog.dismiss();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }


    @Override
    public void onClick(View v) {
        if (v==birthday) {
            datePickerDialog.show();
            String dob = birthday.getText().toString().trim();
        }
        if (v==btnReg){
            initialize();
            if (!validate()){
                Toast.makeText(this,"Registration Failed.Please Try Again.",Toast.LENGTH_LONG).show();
            }else {
                createUser();

                }
            }

    }


}


