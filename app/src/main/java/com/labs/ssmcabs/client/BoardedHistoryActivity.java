package com.labs.ssmcabs.client;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labs.ssmcabs.client.helper.SharedPreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BoardedHistoryActivity extends AppCompatActivity {

    TextView boarded_count;
    FirebaseDatabase database;
    DatabaseReference boardRef;
    Date curr_month;
    private final String TAG = "BOARDED_ACTIVITY";
    private List<String> boardedAdapter = new ArrayList<>();
    ListView boarded_list;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boarded_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();

        fetchBoardedActivitiesOfCurrentMonth();
    }

    private void initializeViews(){
        context = BoardedHistoryActivity.this;
        boarded_count = findViewById(R.id.boarded_count);
        boarded_list = findViewById(R.id.boarded_list);
        database = FirebaseDatabase.getInstance();
        curr_month = new Date();
        SimpleDateFormat month_formatter = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        boardRef = database.getReference("user_board_logs/"+ SharedPreferenceHelper.fetchUserPhoneNumber(BoardedHistoryActivity.this)
                    +"/"+month_formatter.format(curr_month));
    }




    private void fetchBoardedActivitiesOfCurrentMonth(){
        boardRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boardedAdapter.clear();
                boardRef.removeEventListener(this);
                boarded_count.setText(dataSnapshot.getChildrenCount()+" Activites");

                for(DataSnapshot child: dataSnapshot.getChildren()){
                    Log.v(TAG, "boarded time : "+child.getValue());
                    boardedAdapter.add(child.getValue()+"");
                }
                displayBoardedList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                boardRef.removeEventListener(this);
                Log.w(TAG, "error fetching boarded logs for user");
            }
        });
    }


    private void displayBoardedList(){
        ArrayAdapter<String> adapter = new myBoardAdapterClass();
        boarded_list.setAdapter(adapter);
    }


    private class myBoardAdapterClass extends  ArrayAdapter<String>{
        myBoardAdapterClass() {
            super(context, R.layout.item_boarded, boardedAdapter);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                itemView = inflater.inflate(R.layout.item_boarded, parent, false);
            }
            String current = boardedAdapter.get(position);

            TextView item_boarded_time = itemView.findViewById(R.id.item_boarded_time);
            item_boarded_time.setText(current);
            return itemView;
        }

    }
}
