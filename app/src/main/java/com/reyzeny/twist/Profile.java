package com.reyzeny.twist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;
import com.mifmif.common.regex.Main;

import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {
    EditText edtUsername, edtFirstname, edtLastname, edtKeyRetrievalPassword;
    AppCompatButton btnSave;
    GoogleProgressBar pgbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initComponents();
    }

    private void initComponents() {
        edtUsername = findViewById(R.id.edit_text_profile_username);
        edtFirstname = findViewById(R.id.edit_text_profile_firstname);
        edtLastname = findViewById(R.id.edit_text_profile_lastname);
        edtKeyRetrievalPassword = findViewById(R.id.edit_text_profile_key_retrieval_password);
        btnSave = findViewById(R.id.button_profile_save);
        pgbar = findViewById(R.id.profile_progressbar);
        pgbar.setVisibility(View.GONE);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtUsername.getText().toString().toLowerCase();
                String firstname = edtFirstname.getText().toString().toLowerCase();
                String lastname = edtLastname.getText().toString().toLowerCase();
                String key_retrieval_password = edtKeyRetrievalPassword.getText().toString().toLowerCase();
                validateAndSaveInformation(username, firstname, lastname, key_retrieval_password);
            }
        });
    }

    private void validateAndSaveInformation(String username, String firstname, String lastname, String key_retrieval_password) {
        if (inputsValidated(username, firstname, lastname, key_retrieval_password)) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            pgbar.setVisibility(View.VISIBLE);
            db.collection(Constant.FIREBASE_USER_PROFILE_COLLECTION).whereEqualTo(Constant.USERNAME, username).get(Source.SERVER).addOnCompleteListener(Profile.this, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.getResult()==null || task.getResult().getDocuments().isEmpty()) {
                        saveUserDetails(db, username, firstname, lastname, key_retrieval_password);
                        return;
                    }
                    showUserExists();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    private boolean inputsValidated(String username, String firstname, String lastname, String key_retrieval_password) {
        if (username.isEmpty()){
            edtUsername.setError("Enter a username");
            return false;
        }
        if (firstname.isEmpty()){
            edtFirstname.setError("Enter first name");
            return false;
        }
        if (lastname.isEmpty()){
            edtLastname.setError("Enter last name");
            return false;
        }
        if (key_retrieval_password.isEmpty()){
            edtKeyRetrievalPassword.setError("Enter password");
            return false;
        }
        return true;

    }

    private void showUserExists() {
        Snackbar.make(edtUsername, "Username already exists!", Snackbar.LENGTH_LONG).show();
        pgbar.setVisibility(View.GONE);
    }

    private void saveUserDetails(FirebaseFirestore db, String username, String firstname, String lastname, String key_retrieval_password) {
        Map<String, String> profile = new HashMap<>();
        profile.put(Constant.USERNAME, username);
        profile.put(Constant.FIRST_NAME, firstname);
        profile.put(Constant.LAST_NAME, lastname);
        profile.put(Constant.KEY_RETRIEVAL_PASSWORD, key_retrieval_password);

        db.collection(Constant.FIREBASE_USER_PROFILE_COLLECTION).document(LocalData.getUserId(this))
                .set(profile)
                .addOnSuccessListener(documentReference -> {

                    LocalData.setUserName(Profile.this, username);
                    LocalData.setFirstName(Profile.this, firstname);
                    LocalData.setLastName(Profile.this, lastname);
                    LocalData.setUserSetupDone(Profile.this, true);
                    pgbar.setVisibility(View.GONE);
                    showMain();
                });
    }

    private void showMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
