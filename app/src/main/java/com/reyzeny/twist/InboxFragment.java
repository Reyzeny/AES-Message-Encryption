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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class InboxFragment extends androidx.fragment.app.Fragment {
    View view;
    RecyclerView mRecyclerView;
    TextView tvNoInboxMessage;
    MessageAdapter adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_inbox, container, false);
        mRecyclerView = view.findViewById(R.id.inbox_recyclerview);
        tvNoInboxMessage = view.findViewById(R.id.tvNoInboxMessage);
        tvNoInboxMessage.setVisibility(View.VISIBLE);

        LoadInboxMessages();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void LoadInboxMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        System.out.println("recipeient user name is " + LocalData.getUserName(this.getContext()));
        db.collection(Constant.MESSAGES).whereEqualTo(Constant.RECIPIENT_USERNAME, LocalData.getUserName(this.getContext()))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if (e != null || queryDocumentSnapshots==null) {
                            //Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<DocumentSnapshot> documentList = queryDocumentSnapshots.getDocuments();
                        if (documentList.size() > 0)
                            tvNoInboxMessage.setVisibility(View.GONE);
                        System.out.println("document size is " + documentList.size());
                        adapter = new MessageAdapter(InboxFragment.this.getContext(), InboxFragment.this.getActivity(), documentList, Constant.INBOX);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(InboxFragment.this.getContext());
                        if (InboxFragment.this.getContext()!=null) {
                            DividerItemDecoration itemDecoration = new DividerItemDecoration(InboxFragment.this.getContext(), DividerItemDecoration.VERTICAL);
                            mRecyclerView.addItemDecoration(itemDecoration);
                        }
                        mRecyclerView.setLayoutManager(layoutManager);
                        mRecyclerView.setAdapter(adapter);

                    }
                });
    }
}
