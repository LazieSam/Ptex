package com.ptex;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView text = new TextView(this);
        text.setText("Ptex");
        text.setTextSize(32);
        text.setGravity(android.view.Gravity.CENTER);

        setContentView(text);
    }
}
