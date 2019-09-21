package com.example.imageuploader.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.example.imageuploader.Adapters.RecyclerViewAdapter;
import com.example.imageuploader.Models.Upload;
import com.example.imageuploader.R;
import com.example.imageuploader.Utils.InterfaceUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class ShowUploads extends AppCompatActivity implements RecyclerViewAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private FirebaseStorage firebaseStorage;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ArrayList<Upload> uploadsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_uploads);
        firebaseDatabase = firebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("imageUploaded");
        firebaseStorage = FirebaseStorage.getInstance();
        uploadsList = new ArrayList<>();
        //Inisializaing the recyclerview
        recyclerView = findViewById(R.id.recycler_v);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerViewAdapter = new RecyclerViewAdapter(ShowUploads.this,uploadsList);
        recyclerViewAdapter.SetOnItemClickListener(this);
        //adding an empty adapter
        recyclerView.setAdapter(recyclerViewAdapter);

        //Listening to the database changes
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uploadsList.clear();
                //Filling the upload arraylist with data from the server
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    uploadsList.add(upload);
                }
                //notifying the adapter of new items added
                recyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                InterfaceUtil.showSynchronizedToast(ShowUploads.this,"An error has occured try again !!!");
            }
        });
    }


    @Override
    public void onItemClick(int position) {
        InterfaceUtil.showSynchronizedToast(this,"Item : "+uploadsList.get(position).getName());
    }

    @Override
    public void onDeleteClick(final int position) {
        final Upload choosedItem = uploadsList.get(position);
        databaseReference.child(choosedItem.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                InterfaceUtil.showSynchronizedToast(ShowUploads.this,"onDelete("+position+") ;");
                //recyclerViewAdapter.notifyItemRemoved(position);
                firebaseStorage.getReference("imageUploaded").child(choosedItem.getName()).delete();
            }
        });

    }

    @Override
    public void onWhateverClick(int position) {
        InterfaceUtil.showSynchronizedToast(this,"ondoWhatever("+position+") ;");
    }
}
