package com.example.mycamera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ShowNameActivity extends AppCompatActivity {
    TextView showText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_name);
        showText= (TextView) findViewById(R.id.show_text);
        showText.setText("Welcome "+ showText.getText().toString());
    }
}
