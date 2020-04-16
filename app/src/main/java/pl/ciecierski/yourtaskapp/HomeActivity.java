package pl.ciecierski.yourtaskapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import java.text.DateFormat;
import java.util.Date;

import pl.ciecierski.yourtaskapp.model.Data;


public class HomeActivity extends AppCompatActivity {

    FloatingActionButton fabBtn;

    //    firebase
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    Query query;

    //    recycler..
    private RecyclerView recyclerView;

    //    variable
    private String title;
    private String note;
    private String post_key;


    //    update input field
    private EditText titleUp;
    private EditText noteUp;
    private Button btnDeleteUp;
    private Button btnUpdateUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setTitle("Your Task Appss");
        getSupportActionBar().setSubtitle("of main");

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();
        String uId = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("TaskNote").child(uId);
        mDatabase.keepSynced(true);

        query = FirebaseDatabase.getInstance()
                .getReference()
                .child("TaskNote").child(uId)
                .limitToLast(50);

//recycler

        recyclerView = findViewById(R.id.recycler);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);


        fabBtn = findViewById(R.id.fab_btn);

        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);
                LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
                View myview = inflater.inflate(R.layout.custominputfield, null);
                myDialog.setView(myview);
                final AlertDialog dialog = myDialog.create();

                final EditText title = myview.findViewById(R.id.edt_title);
                final EditText note = myview.findViewById(R.id.edt_note);

                Button btnSave = myview.findViewById(R.id.btn_save);

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String mTitle = title.getText().toString().trim();
                        String mNote = note.getText().toString().trim();

                        if (TextUtils.isEmpty(mTitle)) {
                            title.setError("Required Field..");
                            return;
                        }
                        if (TextUtils.isEmpty(mNote)) {
                            note.setError("Required Field..");
                            return;
                        }

                        String id = mDatabase.push().getKey();
                        String datee = DateFormat.getDateInstance().format(new Date());
                        Data data = new Data(mTitle, mNote, datee, id);
                        mDatabase.child(id).setValue(data);
                        Toast.makeText(getApplicationContext(), "Data Insert", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();


                    }
                });


                dialog.show();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Data> options =
                new FirebaseRecyclerOptions.Builder<Data>()
                        .setQuery(query, Data.class)
                        .build();

        FirebaseRecyclerAdapter<Data, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Data, MyViewHolder>
                (
                        options
                ) {

            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder viewHolder, final int position, @NonNull final Data model) {

                viewHolder.setTitle(model.getTitle());
                viewHolder.setNote(model.getNote());
                viewHolder.setDate(model.getDate());

                viewHolder.myview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        post_key = getRef(position).getKey();
                        title = model.getTitle();
                        note = model.getNote();


                        updateData();

                    }
                });

            }

            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_data, parent, false);
                return new MyViewHolder(view);
            }


        };
        adapter.startListening();


        recyclerView.setAdapter(adapter);

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {


        View myview;


        public MyViewHolder(View itemView) {
            super(itemView);
            myview = itemView;
        }

        public void setTitle(String title) {
            TextView mTitle = myview.findViewById(R.id.title);
            mTitle.setText(title);
        }

        public void setNote(String note) {
            TextView mNote = myview.findViewById(R.id.note);
            mNote.setText(note);
        }

        public void setDate(String date) {
            TextView mDate = myview.findViewById(R.id.date);
            mDate.setText(date);

        }


    }


    public void updateData() {


        AlertDialog.Builder myDialog = new AlertDialog.Builder(HomeActivity.this);

        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);


        View myview = inflater.inflate(R.layout.updateinputfield, null);
        myDialog.setView(myview);
        final AlertDialog dialog = myDialog.create();

        titleUp = myview.findViewById(R.id.edt_title_upd);
        noteUp = myview.findViewById(R.id.edt_note_upd);
        btnDeleteUp = myview.findViewById(R.id.btn_delete_upd);
        btnUpdateUp = myview.findViewById(R.id.btn_update_upd);

        titleUp.setText(title);
        titleUp.setSelection(title.length());
        noteUp.setText(note);
        noteUp.setSelection(note.length());


        btnUpdateUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title = titleUp.getText().toString().trim();
                note = noteUp.getText().toString().trim();
                String mDate = DateFormat.getDateInstance().format(new Date());

                Data data = new Data(title, note, mDate, post_key);
                mDatabase.child(post_key).setValue(data);


                dialog.dismiss();
            }
        });


        btnDeleteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(post_key).removeValue();

                dialog.dismiss();
            }
        });

        dialog.show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
