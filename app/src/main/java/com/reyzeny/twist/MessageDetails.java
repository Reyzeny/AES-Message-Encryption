package com.reyzeny.twist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MessageDetails extends AppCompatActivity {
    private TextView tvEncryptedMessage, tvDecryptedMessage, tvGetKey, tvDecrypt;
    private EditText edtKey;
    private RelativeLayout decryptionLayout, originalMessageLayout;

    private String documentID, SenderUsername, Message, MessageKey;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_message_display);
        documentID = getIntent().getStringExtra(Constant.DOCUMENT_ID);
        SenderUsername = getIntent().getStringExtra(Constant.SENDER_USERNAME);
        Message = getIntent().getStringExtra(Constant.MESSAGE);
        MessageKey = getIntent().getStringExtra(Constant.MESSAGE_KEY);
        getSupportActionBar().setTitle(SenderUsername);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initComponents();
    }

    private void initComponents() {
        tvEncryptedMessage = findViewById(R.id.text_encrypted_text);
        tvDecryptedMessage = findViewById(R.id.text_original_message);
        edtKey = findViewById(R.id.edit_text_key);
        tvGetKey = findViewById(R.id.text_get_key);
        tvDecrypt = findViewById(R.id.text_decrypt);
        decryptionLayout = findViewById(R.id.decryption_layout);
        originalMessageLayout = findViewById(R.id.original_message_layout);

        tvGetKey.setOnClickListener(view->beginGetMessageKey());
        tvDecrypt.setOnClickListener(view->beginMessageDecryption());

        showEncrypted();
        tvEncryptedMessage.setText(Message);
    }

    private void showEncrypted() {
        tvEncryptedMessage.setVisibility(View.VISIBLE);
        decryptionLayout.setVisibility(View.VISIBLE);
        originalMessageLayout.setVisibility(View.GONE);
    }

    private void showDecryption() {
        tvEncryptedMessage.setVisibility(View.VISIBLE);
        decryptionLayout.setVisibility(View.GONE);
        originalMessageLayout.setVisibility(View.VISIBLE);
        edtKey.setText("");
    }

    private void beginGetMessageKey() {
        showInputKeyRetrievalPasswordDialog();
    }

    private void showInputKeyRetrievalPasswordDialog() {
        final EditText input = new EditText(this);
        input.setHint("Enter your key retrieval password");
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setView(input);
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                validateKeyPassword(input, dialog);
            }
        });
        alertDialog.show();
    }

    private void validateKeyPassword(EditText input, DialogInterface dialog) {
        String password = input.getText().toString().toLowerCase();
        if (password.isEmpty()) {
            input.setError("Enter a password");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constant.FIREBASE_USER_PROFILE_COLLECTION).document(LocalData.getUserId(MessageDetails.this))
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful() && password.equals(task.getResult().getString(Constant.KEY_RETRIEVAL_PASSWORD))) {
                            displayKey();
                            dialog.dismiss();
                        }
                    }

                });
    }

    private void displayKey() {
        edtKey.setText(MessageKey);
    }

    private void beginMessageDecryption() {
        String inputKey = edtKey.getText().toString();
        if (inputKey.isEmpty()) {
            edtKey.setError("Enter decryption key");
            return;
        }
        showDecryption();
        String decryptedMessage = AESUtil.decryptMessage(Message, inputKey);
        if (decryptedMessage==null || decryptedMessage.isEmpty()){
            showKeyError();
            showEncrypted();
            return;
        }
        tvDecryptedMessage.setText(decryptedMessage);
    }

    private void showKeyError() {
        new AlertDialog.Builder(this)
                .setMessage("Invalid decryption key")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId()==R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }
        return false;
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message")
                .setNegativeButton("NO", null)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem();
                    }
                })
                .show();
    }

    public void deleteItem() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constant.FIREBASE_MESSAGE_COLLECTION).document(documentID)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MessageDetails.this, "Message deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessageDetails.this, "Error deleting message " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
