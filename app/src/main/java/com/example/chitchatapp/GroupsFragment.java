package com.example.chitchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends Fragment {

    private View groupView;
    private ListView groupListView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> groupList = new ArrayList<>();

    private DatabaseReference groupRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupView = inflater.inflate(R.layout.fragment_groups, container, false);
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        initField();

        groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
               String groupName = adapterView.getItemAtPosition(position).toString();
                Intent groupChatIntent = new Intent(getContext(),GroupChatActivity.class);
                groupChatIntent.putExtra("groupName",groupName);
                startActivity(groupChatIntent);
            }
        });

        retrieveGroupsAndDisplay();
        return groupView;
    }

    private void initField() {
        groupListView = groupView.findViewById(R.id.groupListView);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, groupList);
        groupListView.setAdapter(arrayAdapter);
    }

    private void retrieveGroupsAndDisplay() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                groupList.clear();
                groupList.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                String message = databaseError.getMessage();
                Toast.makeText(getContext(), "error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
