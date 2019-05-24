package com.mabapps.speak.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mabapps.speak.R;

public class HistoryActivity extends AppCompatActivity {
    private Button btnHistory;
    private TextView tvShow;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private InterstitialAd interstitial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MobileAds.initialize(this,"ca-app-pub-9935976681967711~8335957731");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        AdRequest adRequest1 = new AdRequest.Builder().build();

        //prepare the interstitial ad
        interstitial = new InterstitialAd(HistoryActivity.this);
        //insert the add unit id
        interstitial.setAdUnitId(getString(R.string.ad_id_interstitial));

        interstitial.loadAd(adRequest1);

        //prepare ad interstitial ad listner
        interstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                displayInterstitial();
            }
        });



        btnHistory = (Button) findViewById(R.id.checkHistory);
        tvShow = (TextView) findViewById(R.id.show);

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = firebaseUser.getUid();
                databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String history = dataSnapshot.child("History").getValue().toString();
                        tvShow.setText(history);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

    }

    public void  displayInterstitial(){
        if(interstitial.isLoaded()){
            interstitial.show();
        }
    }
}
