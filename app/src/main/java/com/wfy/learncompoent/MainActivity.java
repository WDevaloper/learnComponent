package com.wfy.learncompoent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.wfy.annotation.ARouter;


@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
