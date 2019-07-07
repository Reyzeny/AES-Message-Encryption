package com.reyzeny.twist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class SentFragment extends androidx.fragment.app.Fragment {
    View view;
    RecyclerView mRecyclerView;
    TextView tvNoMessageSent;
    MessageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_sent, container, false);
        mRecyclerView = view.findViewById(R.id.sent_messages_recyclerview);
        tvNoMessageSent = view.findViewById(R.id.tvNoMessageSent);
        tvNoMessageSent.setVisibility(View.VISIBLE);
        LoadSentMessages();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
    private void LoadSentMessages() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        System.out.println("recipeient user name is " + LocalData.getUserName(this.getContext()));
        db.collection(Constant.MESSAGES).whereEqualTo(Constant.SENDER_USERNAME, LocalData.getUserName(this.getContext()))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null || queryDocumentSnapshots==null) {
                            //Log.w(TAG, "Listen failed.", e);
                            return;
                        }
                        List<DocumentSnapshot> documentList = queryDocumentSnapshots.getDocuments();
                        System.out.println("document size is " + documentList.size());
                        if (documentList.size() > 0)
                            tvNoMessageSent.setVisibility(View.GONE);
                        adapter = new MessageAdapter(SentFragment.this.getContext(), SentFragment.this.getActivity(), documentList, Constant.SENT);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(SentFragment.this.getContext());
                        if (SentFragment.this.getContext()!=null) {
                            DividerItemDecoration itemDecoration = new DividerItemDecoration(SentFragment.this.getContext(), DividerItemDecoration.VERTICAL);
                            mRecyclerView.addItemDecoration(itemDecoration);
                        }
                        mRecyclerView.setLayoutManager(layoutManager);
                        mRecyclerView.setAdapter(adapter);
                    }
                });
    }
}
