package com.kik.atn.atnsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kik.atn.ATN;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ATN atn = new ATN();
        findViewById(R.id.btnReceived).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atn.onMessageReceived(MainActivity.this);
            }
        });
        findViewById(R.id.btnSent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atn.onMessageSent(MainActivity.this);
            }
        });
    }
}
