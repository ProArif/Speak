package com.mabapps.speak.fragments;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ibm.watson.developer_cloud.discovery.v1.model.EnrichmentOptions;
import com.ibm.watson.developer_cloud.language_translator.v2.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v2.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v2.model.TranslationResult;
import com.ibm.watson.developer_cloud.service.exception.BadRequestException;
import com.ibm.watson.developer_cloud.service.exception.UnauthorizedException;
import com.mabapps.speak.R;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Ariful Islam on 28-Nov-17.
 */

public class TranslateTab extends Fragment {

    private Button btnSpeak,btnHistory;
    private Spinner languageSp;
    private TextView tvShow;
    private EditText edtText;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabbed, container, false);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        btnSpeak = (Button) rootView.findViewById(R.id.speak);
        btnHistory = (Button) rootView.findViewById(R.id.translate);
        languageSp = (Spinner) rootView.findViewById(R.id.language);
        tvShow = (TextView) rootView.findViewById(R.id.tvShowTranslate);
        edtText = (EditText) rootView.findViewById(R.id.text);

        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechInput();
            }
        });

        final LanguageTranslator service = new LanguageTranslator();
        service.setUsernameAndPassword("c65ea5f0-7ff1-4c1a-b813-177b60906e96","h0ECWLEjMqPE");
        service.setEndPoint("https://gateway.watsonplatform.net/language-translator/api");

        try {
            service.listModels(null);
        } catch (IllegalArgumentException e) {
            // Missing or invalid parameter
        } catch (BadRequestException e) {
            // Missing or invalid parameter
        } catch (UnauthorizedException e) {
            // Access is denied due to invalid credentials
            Toast.makeText(getContext(),"Unautorized",Toast.LENGTH_LONG).show();
        }

        final ProgressDialog progressDialog = new ProgressDialog(getContext());


        languageSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                progressDialog.setTitle("Please wait");
                progressDialog.setMessage("Translating into SPANISH");
                if (position == 0) {
                    Toast.makeText(getContext(), "Please Select an language to translate", Toast.LENGTH_LONG).show();
                }
                if (position == 1) {




                    TranslateOptions translateOptions = new TranslateOptions.Builder()
                            .addText(edtText.getText().toString()).source(EnrichmentOptions.Language.ENGLISH).target(EnrichmentOptions.Language.SPANISH).build();

                    TranslationResult result = service.translate(translateOptions)
                            .execute();

                    String r = result.toString();

                    tvShow.setText(r);
                    progressDialog.dismiss();
                }
                if (position == 2) {
                    TranslateOptions translateOptions = new TranslateOptions.Builder()
                            .addText(edtText.getText().toString()).source(EnrichmentOptions.Language.ENGLISH).target(EnrichmentOptions.Language.ITALIAN).build();

                    TranslationResult result = service.translate(translateOptions)
                            .execute();
                    String r = result.toString();

                    tvShow.setText(r);

                }
                if (position == 3) {
                    TranslateOptions translateOptions = new TranslateOptions.Builder()
                            .addText(edtText.getText().toString()).source(EnrichmentOptions.Language.ENGLISH).target(EnrichmentOptions.Language.GERMAN).build();

                    TranslationResult result = service.translate(translateOptions)
                            .execute();

                    String r = result.toString();

                    tvShow.setText(r);

                }

                if (position == 4) {
                    TranslateOptions translateOptions = new TranslateOptions.Builder()
                            .addText(edtText.getText().toString()).source(EnrichmentOptions.Language.ENGLISH).target(EnrichmentOptions.Language.FRENCH).build();

                    TranslationResult result = service.translate(translateOptions)
                            .execute();

                    String r = result.toString();

                    tvShow.setText(r);

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String value = edtText.getText().toString();
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = firebaseUser.getUid();
                progressDialog.setTitle("Saving History..");
                progressDialog.setMessage("Please wait while we process your history");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                String current_uid = firebaseUser.getUid();

                databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid).child("History");
                databaseReference.setValue(value).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful()){
                           Toast.makeText(getContext(),"History saved successfully",Toast.LENGTH_LONG).show();
                           progressDialog.dismiss();
                       }else {
                           Toast.makeText(getContext(),"History saving failed.Please try again.",Toast.LENGTH_LONG).show();
                       }

                    }
                });
            }
        });
        return rootView;

    }

    private void speechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(this.getContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    edtText.setText(result.get(0));
                }
                break;
            }

        }
    }


}
