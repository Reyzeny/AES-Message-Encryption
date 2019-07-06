package com.reyzeny.twist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class NewMessage extends AppCompatActivity {
    EditText edtRecipient, edtMessage;
    Button btnSendMessage;
    ProgressBar pgbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("New Message");
        initComponents();
    }

    private void initComponents() {
        edtRecipient = findViewById(R.id.text_recipient);
        edtMessage = findViewById(R.id.text_message);
        btnSendMessage = findViewById(R.id.button_send_message);
        pgbar = findViewById(R.id.new_message_pgbar);
        btnSendMessage.setOnClickListener(view->{startSendMessage();});
        pgbar.setVisibility(View.GONE);
    }

    private void startSendMessage() {
        String recipient = edtRecipient.getText().toString();
        String message = edtMessage.getText().toString();
        if (recipient.isEmpty()) {
            Snackbar.make(edtRecipient, "Enter the recipient's username", Snackbar.LENGTH_LONG).show();
            return;
        }
        if (message.isEmpty()) {
            Snackbar.make(edtRecipient, "No message to send", Snackbar.LENGTH_LONG).show();
            return;
        }
        showConfirmationDialog(recipient, message);
    }

    private void showConfirmationDialog(String recipient, String message) {
        new AlertDialog.Builder(this).setTitle("Send Message")
                .setMessage("Send Message to " + recipient)
                .setNegativeButton("NO", null)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pgbar.setVisibility(View.VISIBLE);
                        validateAndSendMessage(recipient, message);
                    }
                })
                .show();
    }

    private void validateAndSendMessage(String recipient, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constant.FIREBASE_USER_PROFILE_COLLECTION).whereEqualTo(Constant.USERNAME, recipient).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult()==null || task.getResult().getDocuments().size() == 0){
                            showUserNameDoesNotExists();
                            pgbar.setVisibility(View.GONE);
                            return;
                        }
                        sendMessage(db, recipient, message);
                    }
                });
    }

    private void showUserNameDoesNotExists() {
        new AlertDialog.Builder(this)
                .setMessage("Username does not exists")
                .setPositiveButton("OK", null)
                .show();
    }

    private void sendMessage(FirebaseFirestore db, String recipient, String message) {
        String generatedKey = KeyGenerator.generateKeyWord();
        String encryptedMessage = AESUtil.encryptMessage(message, generatedKey);

        if (encryptedMessage==null || encryptedMessage.isEmpty()){
            showEncryptionError();
            pgbar.setVisibility(View.GONE);
            return;
        }

        Map<String, String> messageBundle = new HashMap<>();
        messageBundle.put(Constant.SENDER_USERNAME, LocalData.getUserName(this));
        messageBundle.put(Constant.MESSAGE, encryptedMessage);
        messageBundle.put(Constant.RECIPIENT_USERNAME, recipient);
        messageBundle.put(Constant.MESSAGE_KEY, generatedKey);
        messageBundle.put(Constant.CREATED_AT, String.valueOf(new Date().getTime()));

        db.collection(Constant.FIREBASE_MESSAGE_COLLECTION)
                .add(messageBundle)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        pgbar.setVisibility(View.GONE);
                        Toast.makeText(NewMessage.this, "Message Sent", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewMessage.this, "Could not send message. Try again later", Toast.LENGTH_LONG).show();
                        pgbar.setVisibility(View.GONE);
                    }
                });
    }

    private void showEncryptionError() {
        new AlertDialog.Builder(this)
                .setMessage("An error occurred while encrypting message. Please try again")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
        }
        return true;
    }
}
