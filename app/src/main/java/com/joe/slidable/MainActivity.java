package com.joe.slidable;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.joe.slidable.view.SlidableTextView;
import com.joe.slidabletextviewdemo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SlidableTextView stv = (SlidableTextView) findViewById(R.id.slidable_tv);
        stv.setIcon(BitmapFactory.decodeResource(getResources(), R.drawable.me_icon));
        stv.setOnIconClickListener(new SlidableTextView.OnIconClickListener() {
            @Override
            public void onIconClick() {
                Toast.makeText(MainActivity.this, "clicked",Toast.LENGTH_SHORT).show();
            }
        });

    }
}
