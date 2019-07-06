package com.reyzeny.twist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private Context context;
    private Activity activity;
    private List<DocumentSnapshot> documents;
    private String Module;
    public MessageAdapter(Context context, Activity activity, List<DocumentSnapshot> documents, String Module) {
        this.context = context;
        this.activity = activity;
        this.documents = documents;
        this.Module = Module;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.short_message_display, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String displayName = Module.equals(Constant.SENT) ? documents.get(position).getString(Constant.RECIPIENT_USERNAME) : documents.get(position).getString(Constant.SENDER_USERNAME);
        holder.textContactName.setText(displayName);
        holder.textMessage.setText(documents.get(position).getString(Constant.MESSAGE));
        TextDrawable drawable = TextDrawable.builder()
                .buildRoundRect(displayName.substring(0, 1).toUpperCase(), Color.GRAY, 10);
        holder.image.setImageDrawable(drawable);
        holder.short_message_body.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentSnapshot documentSnapshot = documents.get(position);
                Intent intent = new Intent(v.getContext(), MessageDetails.class);
                intent.putExtra(Constant.SENDER_USERNAME, displayName);
                intent.putExtra(Constant.MESSAGE, documentSnapshot.getString(Constant.MESSAGE));
                intent.putExtra(Constant.MESSAGE_KEY, documentSnapshot.getString(Constant.MESSAGE_KEY));

                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return documents==null || documents.isEmpty() ? 0 : documents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textContactName, textMessage;
        private ImageView image;
        private ConstraintLayout short_message_body;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textContactName = itemView.findViewById(R.id.text_contact_name);
            textMessage = itemView.findViewById(R.id.text_short_message);
            image = itemView.findViewById(R.id.imageView);
            short_message_body = itemView.findViewById(R.id.short_message_body);
        }
    }
}
