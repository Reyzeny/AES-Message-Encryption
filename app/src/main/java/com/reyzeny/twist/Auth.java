package com.reyzeny.twist;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Auth extends AppCompatActivity {
    TextView tvWrongNumber;
    TextView tvResendCode;
    EditText edtPhoneNumber;
    EditText edtCode;
    GoogleProgressBar pgbar;
    AppCompatButton btnVerify;
    String inputState = Constant.ENTER_PHONE_NUMBER;
    Handler handler;
    boolean handler_interrupted = false;
    private String storedVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private String input;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Toast.makeText(Auth.this, "Verification completed", Toast.LENGTH_LONG).show();
            signInWithPhoneAuthCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(Auth.this, e.getMessage(), Toast.LENGTH_LONG).show();
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
                // ...
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                // ...
            }
            inputState = Constant.ENTER_PHONE_NUMBER;
            edtPhoneNumber.setEnabled(true);
            edtCode.setVisibility(View.GONE);
            pgbar.setVisibility(View.GONE);
        }

        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
            Toast.makeText(Auth.this, "code has been sent", Toast.LENGTH_LONG).show();
            tvWrongNumber.setVisibility(View.VISIBLE);
            storedVerificationId = verificationId;
            resendToken = token;
            inputState = Constant.ENTER_CODE;
            edtPhoneNumber.setEnabled(false);
            edtCode.setVisibility(View.VISIBLE);
            startResendCountDown();
            pgbar.setVisibility(View.GONE);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        initComponents();
    }

    private void initComponents() {
        tvWrongNumber = findViewById(R.id.auth_tvWrongNumber);
        tvResendCode = findViewById(R.id.auth_tvResendCode);
        edtPhoneNumber = findViewById(R.id.auth_edtPhoneNumber);
        edtCode = findViewById(R.id.auth_edtCode);
        pgbar = findViewById(R.id.auth_progressbar);
        btnVerify = findViewById(R.id.auth_btnVerify);
        tvWrongNumber.setVisibility(View.GONE);
        edtCode.setVisibility(View.GONE);
        pgbar.setVisibility(View.GONE);
        btnVerify.setOnClickListener(view->{
            switch (inputState) {
                case Constant.ENTER_PHONE_NUMBER:
                    verifyPhoneNumber();
                    break;
                case Constant.ENTER_CODE:
                    verifyCode();
            }
        });
        tvWrongNumber.setOnClickListener(v->clearNumber());
    }

    private void startResendCountDown() {
        final long[] RESEND_COUNTER = {Constant.PHONE_NUMBER_VERIFICATION_TIMEOUT};
        tvResendCode.setOnClickListener(null);
        handler_interrupted=false;
        handler = new Handler();
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                if (RESEND_COUNTER[0] < 1){
                    tvResendCode.setText(getString(R.string.resend_code));
                    tvResendCode.setOnClickListener (v-> PhoneAuthProvider.getInstance().verifyPhoneNumber(input, Constant.PHONE_NUMBER_VERIFICATION_TIMEOUT, TimeUnit.SECONDS, Auth.this, callbacks, resendToken) );
                    //handler.removeCallbacks(runnableCode);
                    return;
                }
                RESEND_COUNTER[0]--;
                tvResendCode.setText("You can resend the code in " + RESEND_COUNTER[0] + " seconds");
                if (handler_interrupted) {
                    //handler.removeCallbacks(runnableCode);
                    tvResendCode.setText("");
                    return;
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnableCode);
    }

    private void clearNumber() {
        inputState = Constant.ENTER_PHONE_NUMBER;
        edtPhoneNumber.setEnabled(true);
        edtPhoneNumber.requestFocus();
        tvWrongNumber.setVisibility(View.GONE);
        edtCode.setText("");
        edtCode.setVisibility(View.GONE);
        tvResendCode.setText("");
        tvResendCode.setOnClickListener (null);
        handler_interrupted=true;
    }

    private void verifyPhoneNumber() {
        input = edtPhoneNumber.getText().toString().trim().replace(" ", "");
        if (input.isEmpty() || !input.contains("+")) {
            edtPhoneNumber.setError(getString(R.string.invalid_phone_number));
            edtPhoneNumber.requestFocus();
            return;
        }
        pgbar.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(input, Constant.PHONE_NUMBER_VERIFICATION_TIMEOUT, TimeUnit.SECONDS, this, callbacks);


    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        pgbar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    handler_interrupted=true;
                    FirebaseUser user = task.getResult().getUser();
                    signUpIfNotExist(user);
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Snackbar.make(edtCode, getString(R.string.invalid_code), Snackbar.LENGTH_LONG).show();
                        pgbar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void signUpIfNotExist(FirebaseUser user) {
        pgbar.setVisibility(View.VISIBLE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constant.FIREBASE_USER_PROFILE_COLLECTION).document(user.getPhoneNumber()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists()) {
                    create_user(db, user);
                } else {
                    ExecuteAuthSuccess(user, documentSnapshot);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Auth.this, "sign up failure. " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                pgbar.setVisibility(View.GONE);
            }
        });
    }

    private void verifyCode() {
        String input = edtCode.getText().toString();
        if (input.isEmpty()) {
            edtCode.setError(getString(R.string.valid_code_required));
            edtCode.requestFocus();
            return;
        }
        PhoneAuthCredential phone_auth_credential = PhoneAuthProvider.getCredential(storedVerificationId, input);
        signInWithPhoneAuthCredential(phone_auth_credential);
    }

    private void create_user(FirebaseFirestore db, FirebaseUser user) {
        pgbar.setVisibility(View.VISIBLE);
        HashMap<String, String> user_profile_data = new HashMap<String, String>();
        user_profile_data.put(Constant.FIRST_NAME, "");
        user_profile_data.put(Constant.LAST_NAME, "");
        db.collection(Constant.FIREBASE_USER_PROFILE_COLLECTION).document(user.getPhoneNumber()).set(user_profile_data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Auth.this, "success creating user", Toast.LENGTH_LONG).show();
                        ExecuteAuthSuccess(user, null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Auth.this, "could not create user. " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        pgbar.setVisibility(View.GONE);
                    }
                });
    }

    private void ExecuteAuthSuccess(FirebaseUser user, DocumentSnapshot documentSnapshot) {
        LocalData.setUserId(this, user.getPhoneNumber());
        LocalData.setUserAuthentication(this, true);
        pgbar.setVisibility(View.GONE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constant.FIREBASE_USER_PROFILE_COLLECTION).document(user.getPhoneNumber()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().contains(Constant.USERNAME) && task.getResult().contains(Constant.FIRST_NAME) && task.getResult().contains(Constant.LAST_NAME)){
                    LocalData.setUserName(Auth.this, (String) task.getResult().get(Constant.USERNAME));
                    LocalData.setFirstName(Auth.this, (String) task.getResult().get(Constant.FIRST_NAME));
                    LocalData.setLastName(Auth.this, (String) task.getResult().get(Constant.LAST_NAME));
                    LocalData.setUserSetupDone(Auth.this, true);
                    showMain();
                    return;
                }
                showProfileSetUp();
            }
        });
    }

    private void showMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void showProfileSetUp() {
        startActivity(new Intent(this, Profile.class));
        finish();
    }

}
