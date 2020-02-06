package com.wfy.order;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.wfy.annotation.ARouter;

@ARouter(path = "/order/Order_Main2Activity")
public class Order_Main2Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order__main);
    }
}
