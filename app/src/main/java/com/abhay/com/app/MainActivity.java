package com.abhay.com.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    ImageButton searchBynumberButton;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    int clicks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clicks=0;
        searchBynumberButton=(ImageButton)findViewById(R.id.searchNumberButton);
        Button ee = findViewById(R.id.eebutton);
        searchBynumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBynumber();
            }
        });
        ee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clicks +=1;
                if(clicks==5){
                    Toast toast = Toast.makeText(getApplicationContext(), "App Made by Abhay Chirania!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 0, 300);
                    toast.show();
                    clicks=0;
                }
            }
        });

    }
    void searchBynumber() {
        EditText busNumber = findViewById(R.id.searchNumber);
        final String number = busNumber.getText().toString().toUpperCase().replace("-", "").replace(" ", "");
        if (number.isEmpty()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Bus Number Not Found!", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 300);
            toast.show();
        } else {
            DatabaseReference myRef = database.getReference().child("Buses");
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(number)) {
                        //TextView tb = findViewById(R.id.textView);
                        //tb.setText(dataSnapshot.child(number).child("Speed").getValue().toString());
                        //Intent here insted of changing text
                        Intent i = new Intent(MainActivity.this, MapsActivity.class);
                        i.putExtra("Bus_Number", number);
                        startActivity(i);
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Bus Number Not Found!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 300);
                        toast.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }
}
