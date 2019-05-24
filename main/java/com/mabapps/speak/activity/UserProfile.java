package com.mabapps.speak.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mabapps.speak.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfile extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurentUser;
    private StorageReference mUserImages;

    private ProgressDialog mProgressDialog;

    private TextView mstatus,displayName;
    private CircleImageView proImageView;

    private static final int GALLERY_PICK = 1;
    private TextView tvName,tvAddr,tvBirth,tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
               /*Intent galleryIntent = new Intent();
               galleryIntent.setType("image/*");
               galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
               startActivityForResult(Intent.createChooser(galleryIntent,"Select an Image"),GALLERY_PICK);*/

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(UserProfile.this);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mstatus = (TextView) findViewById(R.id.tvUserStatus);
        displayName = (TextView) findViewById(R.id.tvUserName);
        proImageView = (CircleImageView) findViewById(R.id.proImage) ;

        tvAddr = (TextView) findViewById(R.id.showAddr);
        tvBirth = (TextView) findViewById(R.id.showBirth);
        tvEmail = (TextView) findViewById(R.id.showEmail);
        tvName = (TextView) findViewById(R.id.showName);

        mAuth = FirebaseAuth.getInstance();
        mCurentUser = mAuth.getCurrentUser();
        String current_uid = mCurentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mUserImages = FirebaseStorage.getInstance().getReference();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("UserName").getValue().toString();
                String status = dataSnapshot.child("Status").getValue().toString();
                String proImage = dataSnapshot.child("image").getValue().toString();
                String thumbImg = dataSnapshot.child("thumb_image").getValue().toString();
                String Address = dataSnapshot.child("Address").getValue().toString();
                String fullName = dataSnapshot.child("Name").getValue().toString();
                String email = dataSnapshot.child("Email").getValue().toString();
                String birthday = dataSnapshot.child("Birthday").getValue().toString();

                mstatus.setText(status);
                displayName.setText(name);
                Picasso.with(UserProfile.this).load(proImage).into(proImageView);

                tvName.setText(fullName);
                tvAddr.setText(Address);
                tvEmail.setText(email);
                tvBirth.setText(birthday);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



   /*public static String random(){
        Random generator = new Random();
        StringBuilder randomBuilder = new StringBuilder();
        int randomLength = generator.nextInt(20);
        char tempChar;
        for (int i=0;i<randomLength;i++){
            tempChar = (char) (generator.nextInt(96)+32);
            randomBuilder.append(tempChar);
        }
        return randomBuilder.toString();
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);

        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setTitle("Uploading Image..");
                mProgressDialog.setMessage("Please wait while we process your image");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();
                String current_user = mCurentUser.getEmail();
                StorageReference filePath = mUserImages.child("profile_images").child(current_user + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            String downloadUri = task.getResult().getDownloadUrl().toString();
                            mUserDatabase.child("image").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(UserProfile.this, "Image Uploaded", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                               Toast.makeText(UserProfile.this,"Error uploading image.",Toast.LENGTH_LONG).show();
                               mProgressDialog.dismiss();

                        }
                    }
                });


            }
        }
    }





    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_history) {
            Intent n = new Intent(this,HistoryActivity.class);
            startActivity(n);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.activity) {
            // Handle the activity action
            Intent a = new Intent(this, TabbedActivity.class);
            startActivity(a);

        }else if (id == R.id.editProfile){
            Intent editProfileIntent = new Intent(this,EditProfile.class);
            String status_value = mstatus.getText().toString();
            editProfileIntent.putExtra("status",status_value);
            String n = displayName.getText().toString();
            editProfileIntent.putExtra("name",n);
            String ad = tvAddr.getText().toString();
            editProfileIntent.putExtra("address",ad);
            startActivity(editProfileIntent);
        }
        else if (id == R.id.logOut) {
            FirebaseAuth.getInstance().signOut();
            finish();
            sendtoLogin();

        }
        else if (id == R.id.share){

        }
        else if (id == R.id.recent){
            Intent n = new Intent(this,HistoryActivity.class);
            startActivity(n);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendtoLogin() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent n = new Intent(this, LoginActivity.class);
            startActivity(n);
            finish();

        }
    }

        @Override
        protected void onStart () {
            super.onStart();

            sendtoLogin();
        }

}

